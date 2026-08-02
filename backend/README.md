# Twisted Momos — Backend

Spring Boot 4.1 REST API for the Twisted Momos restaurant ordering system.

## Stack

Java 21 · Spring Boot 4.1 (Spring Framework 7 / Spring Security 7) · Spring Data JPA · MySQL · Flyway · MapStruct · Lombok · Maven

## Status: Phase 6 — Hardening

All customer- and admin-facing features are complete (Phases 1–5). This phase closed out known gaps rather than adding new endpoints:

- **Refresh token rotation with reuse detection.** `POST /auth/refresh` now revokes the presented token and issues a new one every time (previously it just reissued a new access token against the same long-lived refresh token — a gap flagged back in Phase 1). If an *already-revoked* token is presented — a strong signal it was stolen and replayed after the legitimate client rotated past it — every refresh token that user holds is revoked, forcing all of their sessions to re-authenticate.
- **Login rate limiting.** `POST /auth/login` now locks out an email for 15 minutes after 5 failed attempts within a 15-minute window (`429 Too Many Requests`). In-memory and per-instance by design — see `LoginRateLimiter`'s javadoc for the tradeoff if this ever needs to run on more than one instance.
- **Unit test suite** (34 tests, `./mvnw test`) covering the logic most likely to regress silently: JWT issuance/validation, refresh rotation and reuse detection, cart merge/availability/removal rules, and the order status transition allow-list. Two of these tests are direct regression guards for real bugs caught during manual verification earlier in the build (cart-item removal via the `Cart.items` collection rather than a direct repository delete, and reuse detection's revoke-all actually committing — see below).

Every prior phase remains available: auth & users (Phase 1), catalog (Phase 2), cart (Phase 3), checkout/orders (Phase 4), and the full `/admin` console (Phase 5).

## API — Auth & Users

All request/response bodies are JSON. All errors (validation, auth, not-found, unexpected) return the same envelope:
```json
{ "timestamp": "...", "status": 401, "error": "Unauthorized", "message": "...", "path": "/api/v1/...", "validationErrors": null }
```

| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| POST | `/api/v1/auth/register` | public | `{name, email, password, phone?}` | `201` `AuthResponse` |
| POST | `/api/v1/auth/login` | public | `{email, password}` | `200` `AuthResponse` (`429` after 5 failed attempts for that email within 15 minutes) |
| POST | `/api/v1/auth/refresh` | public | `{refreshToken}` | `200` `AuthResponse` (rotates: the old refresh token is revoked and a new one is returned; reusing an already-revoked token revokes every token that user holds, `401`) |
| POST | `/api/v1/auth/logout` | public | `{refreshToken}` | `204` (refresh token revoked) |
| GET | `/api/v1/users/me` | **Bearer token required** | — | `200` `UserResponse` |

`AuthResponse`: `{ accessToken, refreshToken, tokenType: "Bearer", expiresInSeconds, user: UserResponse }`
`UserResponse`: `{ id, name, email, phone, role, createdAt }`

## API — Catalog

| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| GET | `/api/v1/categories` | public | — | `200` `CategoryResponse[]` (active only) |
| GET | `/api/v1/menu?categoryId&q&veg&maxSpicy&page&size&sort` | public | — | `200` `PageResponse<MenuItemResponse>` (available only) |
| GET | `/api/v1/menu/{id}` | public | — | `200` `MenuItemResponse` |
| GET | `/api/v1/admin/categories` | **ADMIN** | — | `200` `CategoryResponse[]` (incl. inactive) |
| POST | `/api/v1/admin/categories` | **ADMIN** | `CategoryRequest` | `201` |
| PUT | `/api/v1/admin/categories/{id}` | **ADMIN** | `CategoryRequest` | `200` |
| DELETE | `/api/v1/admin/categories/{id}` | **ADMIN** | — | `204` (`409` if it still has menu items) |
| GET | `/api/v1/admin/menu?categoryId&page&size&sort` | **ADMIN** | — | `200` `PageResponse<MenuItemResponse>` (incl. unavailable) |
| GET | `/api/v1/admin/menu/{id}` | **ADMIN** | — | `200` |
| POST | `/api/v1/admin/menu` | **ADMIN** | `MenuItemRequest` | `201` |
| PUT | `/api/v1/admin/menu/{id}` | **ADMIN** | `MenuItemRequest` | `200` |
| DELETE | `/api/v1/admin/menu/{id}` | **ADMIN** | — | `204` |
| PATCH | `/api/v1/admin/menu/{id}/availability` | **ADMIN** | `{available: boolean}` | `200` |
| POST | `/api/v1/admin/menu/{id}/image` | **ADMIN** | multipart `file` (jpeg/png/webp, ≤5MB) | `200`, sets `imageUrl` |

`CategoryResponse`: `{ id, name, slug, description, displayOrder, active }`
`MenuItemResponse`: `{ id, name, slug, description, price, imageUrl, veg, spicyLevel, tag, available, displayOrder, categoryId, categoryName, categorySlug }`
`CategoryRequest` / `MenuItemRequest`: same shape as the response minus server-generated fields (`id`); `slug` must be lowercase kebab-case and unique.
`PageResponse<T>`: `{ content: T[], page, size, totalElements, totalPages }`

Uploaded images are served back from `/uploads/**` (see `app.upload.dir` — local disk in dev; in production a named Docker volume that survives redeploys and is captured by the nightly backup, until this is pointed at object storage instead).

The full menu (31 items across 8 categories) is seeded by `V4__seed_catalog_data.sql`, ported directly from the frontend's original static menu data.

Access tokens expire in 15 minutes (`app.jwt.access-token-expiration-ms`); refresh tokens in 30 days (`app.jwt.refresh-token-expiration-ms`) and are revocable server-side (`refresh_tokens` table) — logout takes effect immediately, unlike a purely stateless token.

## API — Cart

Every endpoint operates on the authenticated caller's own cart — there is no cart-by-id lookup, and a cart item can never be read or modified by anyone other than its owner.

| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| GET | `/api/v1/cart` | **Bearer token required** | — | `200` `CartResponse` (auto-creates an empty cart on first access) |
| POST | `/api/v1/cart/items` | **Bearer token required** | `{menuItemId, quantity}` (quantity 1-20) | `201` `CartResponse` (adding an item already in the cart merges quantities, capped at 20) |
| PATCH | `/api/v1/cart/items/{itemId}` | **Bearer token required** | `{quantity}` (1-20) | `200` `CartResponse` |
| DELETE | `/api/v1/cart/items/{itemId}` | **Bearer token required** | — | `200` `CartResponse` |
| DELETE | `/api/v1/cart` | **Bearer token required** | — | `200` `CartResponse` (empties the cart) |

`CartResponse`: `{ id, items: CartItemResponse[], totalItems, subtotal }`
`CartItemResponse`: `{ id, menuItemId, menuItemName, menuItemImageUrl, unitPrice, available, quantity, lineTotal }`

Adding a menu item that's been disabled by an admin returns `409 Conflict`. `unitPrice`/`lineTotal` always reflect the menu item's *current* price — orders snapshot it instead, see below.

## API — Orders

| Method | Path | Auth | Body | Response |
|---|---|---|---|---|
| POST | `/api/v1/orders` | **Bearer token required** | `PlaceOrderRequest` | `201` `OrderResponse` (empty cart → `400`; an item went unavailable since being added → `409`) |
| GET | `/api/v1/orders` | **Bearer token required** | — | `200` `PageResponse<OrderSummaryResponse>` (caller's own orders, newest first) |
| GET | `/api/v1/orders/{id}` | **Bearer token required**, own order only | — | `200` `OrderResponse` (`404` if it's not yours) |
| PATCH | `/api/v1/orders/{id}/cancel` | **Bearer token required**, own order only | — | `200` `OrderResponse` (only while status is `PENDING`, else `409`) |
| GET | `/api/v1/admin/orders?status` | **ADMIN** | — | `200` `PageResponse<OrderSummaryResponse>` (all orders, optionally filtered by status) |
| GET | `/api/v1/admin/orders/{id}` | **ADMIN** | — | `200` `OrderResponse` (any order) |
| PATCH | `/api/v1/admin/orders/{id}/status` | **ADMIN** | `{status}` | `200` `OrderResponse` (rejects an illegal transition or unknown status with `409`) |

`PlaceOrderRequest`: `{ recipientName, phone, addressLine1, addressLine2?, city, postalCode, notes? }`
`OrderResponse`: `{ id, status, items: OrderItemResponse[], totalItems, subtotal, recipientName, phone, addressLine1, addressLine2, city, postalCode, notes, customerName, customerEmail, createdAt, updatedAt }`
`OrderItemResponse`: `{ id, menuItemId, menuItemName, quantity, unitPrice, lineTotal }` — `menuItemId` can be `null` if that menu item was later hard-deleted by an admin; the name/price/line-total snapshot on the order survives regardless.
`OrderSummaryResponse`: `{ id, status, totalItems, subtotal, customerName, customerEmail, createdAt }`

Placing an order consumes the caller's cart (clears it) and snapshots each line's name and price at that moment — later menu price changes or deletions never retroactively alter a placed order. Status transitions are enforced by an explicit allow-list per status (e.g. `PENDING → {CONFIRMED, CANCELLED}`, `DELIVERED`/`CANCELLED` are terminal); a customer's self-service cancel is a stricter subset of that (`PENDING` only) than what an admin can do.

## Prerequisites

- Java 21 (JDK)
- A running MySQL 8.x instance (local install, or Docker — see below)
- No local Maven install needed — use the bundled wrapper (`./mvnw` / `mvnw.cmd`)

## Running locally

1. Start MySQL and make sure a user can connect. You do **not** need to create the `twisted_momos_dev` database yourself — the dev datasource URL includes `createDatabaseIfNotExist=true`.

   Quickest option, Docker:
   ```bash
   docker run --name twisted-momos-mysql -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql:8.4
   ```

2. (Optional) Override the default local credentials via environment variables — defaults are `root` / `root`:
   ```bash
   export DB_USERNAME=root
   export DB_PASSWORD=root
   ```

3. Run the app (defaults to the `dev` profile):
   ```bash
   ./mvnw spring-boot:run
   ```
   Windows: `mvnw.cmd spring-boot:run`

4. Confirm it's up:
   ```bash
   curl http://localhost:8080/actuator/health
   # {"status":"UP", ...}
   ```

   On startup, Flyway runs `src/main/resources/db/migration/V*.sql` against the database automatically — check the logs for `Successfully applied 1 migration`.

## Configuration

| Profile | Purpose | Config file |
|---|---|---|
| `dev` | Local development against local MySQL | `application-dev.yml` |
| `prod` | Hostinger VPS under Dokploy, MySQL as a container in the same stack | `application-prod.yml` |

Profile is selected via `SPRING_PROFILES_ACTIVE` (defaults to `dev` if unset — see `application.yml`).

**Production environment variables** (set in the Dokploy service's Environment tab, never committed — the full template is `infra/app/.env.example`):

| Variable | Example |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://mysql:3306/twisted_momos?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `SPRING_DATASOURCE_USERNAME` | application DB user (not root) |
| `SPRING_DATASOURCE_PASSWORD` | that user's password |
| `CORS_ALLOWED_ORIGINS` | `https://twistedmomos.tech` |
| `JWT_SECRET` | long random string (32+ bytes), e.g. `openssl rand -base64 48` |
| `PORT` | optional; defaults to `8080` |

`useSSL=false` is correct here: the database is on an isolated overlay network with no published port, reachable only by the backend container.

Switching the production database later (e.g. to a managed MySQL) is a matter of changing these four datasource values — no code or migration changes.

## Database migrations

Managed by Flyway, files in `src/main/resources/db/migration`, named `V{n}__description.sql`. `hibernate.ddl-auto` is fixed to `validate` in every profile — Hibernate never creates or alters schema; Flyway is the only source of truth for the schema, in dev and prod alike.

## Build

```bash
./mvnw clean package
java -jar target/backend.jar --spring.profiles.active=dev
```

## Tests

```bash
./mvnw test
```

Unit tests only (Mockito-mocked repositories, no database required) — fast and safe to run anywhere, including CI without a MySQL service attached. They cover the logic most prone to silent regressions: JWT issuance/validation (`JwtServiceTest`), refresh rotation and reuse detection (`RefreshTokenServiceTest`), cart merge/availability/removal rules (`CartServiceImplTest`), and the order status transition allow-list (`OrderServiceImplTest`). Everything else in this build was verified manually end-to-end (curl against a real MySQL instance, Playwright against the real UI) rather than via an automated suite — a natural next step if this grows past a single-maintainer project.

## Known limitations

- **Uploaded images live on a single box** — `FileStorageService` writes to local disk, bound to a named Docker volume so they survive a redeploy and are captured by the nightly backup. Swap to S3-compatible object storage before a second host exists.
- **Login rate limiting is per-instance, in-memory** (`LoginRateLimiter`) — correct for the current single-instance deployment, but wouldn't share state across multiple instances if this ever scales horizontally. Swap the backing map for Redis (or similar) at that point.
- **Dokploy stores environment variables in plaintext** in its internal Postgres, so panel access is equivalent to holding every secret. Mitigated by keeping the panel off the public internet; see `infra/SECURITY.md`.
- **One production host, no replication** — recovery is restore-from-backup (`infra/bootstrap/backup.sh`).
- **CI covers the backend only** — `.github/workflows/backend-image.yml` runs the tests, builds the image, and triggers a redeploy. The frontend's `npm run build`/`tsc` is still run by hand.

## Roadmap

See the architecture doc from planning for the full phase breakdown (Auth → Catalog → Cart → Orders → Admin → Hardening). Each phase ships as its own reviewable change.
