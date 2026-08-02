# CLAUDE.md

Guidance for Claude Code working in this repository.

## What this is

Twisted Momos — a street-food momo brand's marketing site plus a full ordering system. Monorepo with two independently deployable packages:

- `frontend/` — React 19 + Vite + TypeScript site and ordering UI. Built to static assets and served by nginx on the same VPS as the backend, reached at `twistedmomos.tech` through the Cloudflare Tunnel.
- `backend/` — Java 21 + Spring Boot 4.1 REST API. Deployed to a Hostinger VPS managed by Dokploy, reached publicly through a Cloudflare Tunnel; MySQL runs as a container in the same stack. Infrastructure lives in `infra/` — see `infra/README.md` for the provisioning runbook and `infra/SECURITY.md` for the access model.

They are self-contained: separate dependency manifests, lockfiles, `.gitignore`s, and READMEs. **Nothing in one references a path inside the other** — they only ever talk over HTTP. Preserve this when adding files; do not introduce shared build config, cross-package imports, or a root-level `package.json`.

`README.md`, `frontend/README.md`, and `backend/README.md` are detailed and current. `backend/README.md` in particular holds the full endpoint table, request/response shapes, and env-var reference — read it before touching API surface rather than re-deriving from controllers.

## Commands

Frontend (from `frontend/`):

```bash
npm install
npm run dev        # Vite dev server, port 5173 (or next free)
npm run build      # tsc -b (type-check) then production build to dist/
npm run preview    # serve built dist/
npm run lint       # oxlint
```

Backend (from `backend/`, uses the bundled wrapper — no local Maven needed):

```bash
./mvnw spring-boot:run                     # dev profile by default
./mvnw test                                # unit tests, no DB required
./mvnw clean package                       # -> target/backend.jar
java -jar target/backend.jar --spring.profiles.active=dev
```

Backend needs a MySQL 8.x instance on `localhost:3306` (`root`/`root` by default; override with `DB_USERNAME`/`DB_PASSWORD`). The dev datasource URL has `createDatabaseIfNotExist=true`, so don't create the schema by hand. Docker one-liner is in `backend/README.md`.

Running a single backend test: `./mvnw test -Dtest=CartServiceImplTest`.

CI is one workflow per package, both on push to `main` touching that package's directory: `backend-image.yml` runs `./mvnw test`, and `frontend-image.yml` runs `npm run lint` and `npm run build`. Each then builds its image, pushes to GHCR, and triggers the same Dokploy redeploy. Before claiming work is done, run `./mvnw test` and `npm run build` yourself rather than waiting on CI.

## Architecture

### Backend layering

Strict `controller → service (interface) → repository` flow, package-by-layer under `com.twistedmomos.backend`:

- `controller/` — HTTP only. Validates with `@Valid`, resolves the caller via `@AuthenticationPrincipal CustomUserDetails`, returns `ResponseEntity`. No business logic, no repository access.
- `service/` — interface; `service/impl/` — the `*Impl`. Controllers depend on the interface. New services follow the same split.
- `repository/` — Spring Data JPA. Dynamic filtering goes in `repository/specification/`.
- `dto/request/`, `dto/response/` — entities never cross the HTTP boundary in either direction.
- `mapper/` — MapStruct, `@Mapper(componentModel = "spring")`, entity → response only.
- `entity/`, `exception/`, `security/`, `config/`.

Constructor injection via Lombok `@RequiredArgsConstructor` on `private final` fields. No field injection, no `@Autowired`.

### Error handling

Every error — validation, auth, not-found, unexpected — leaves through `GlobalExceptionHandler` as one `ErrorResponse` envelope (`timestamp, status, error, message, path, validationErrors`). A new failure mode means a new domain exception in `exception/` plus a handler method mapping it to a status. Never return a bare string or a map from a controller, and never let a raw exception message reach the client — the catch-all deliberately returns a generic "An unexpected error occurred".

Established mappings: `ResourceNotFoundException` → 404; `DuplicateResourceException`, `ResourceInUseException`, `ItemUnavailableException`, `InvalidOrderStatusTransitionException` → 409; `EmptyCartException`, `InvalidFileException` → 400; `InvalidRefreshTokenException` → 401; `TooManyAttemptsException` → 429.

### Auth

Stateless JWT. Access token 15 min, refresh token 30 days and **revocable server-side** via the `refresh_tokens` table, so logout takes effect immediately.

`POST /auth/refresh` rotates: the presented token is revoked and a new one issued every time. Presenting an already-revoked token is treated as theft-and-replay — every refresh token that user holds is revoked. Don't "simplify" this back into reissuing against a stable long-lived token.

Authorization is URL-based in `SecurityConfig`: `/api/v1/auth/**`, `/actuator/health*`, `/uploads/**` are public; `GET` on `/api/v1/categories/**` and `/api/v1/menu/**` is public; `/api/v1/admin/**` requires `ROLE_ADMIN`; everything else requires authentication. **A new admin endpoint must live under `/api/v1/admin/` or it will not be role-guarded.** `@EnableMethodSecurity` is on if a finer-grained rule is genuinely needed.

Ownership is enforced in the service layer, not by URL: cart and order endpoints take the caller's `userId` and scope the query by it. There is no cart-by-id lookup, and fetching someone else's order returns 404 (not 403 — don't leak existence). Keep this shape for any new user-owned resource.

Login rate limiting: 5 failed attempts per email in 15 min → 15 min lockout (`LoginRateLimiter`, in-memory, per-instance by design).

### Persistence

Flyway is the **only** source of truth for schema. `hibernate.ddl-auto` is pinned to `validate` in every profile — Hibernate never creates or alters anything. A schema change means a new `src/main/resources/db/migration/V{n}__description.sql`; never edit an applied migration.

`spring.jpa.open-in-view` is deliberately `false`. Fetch every association a request needs explicitly (see `UserRepository.findByEmail`'s join fetch) — a `LazyInitializationException` means a missing fetch join, not a reason to turn open-in-view back on.

Entities extend `BaseEntity` for audited `created_at`/`updated_at`.

Orders **snapshot** item name and price at placement time. Later menu edits or deletions must never retroactively change a placed order; `OrderItemResponse.menuItemId` is nullable for exactly this reason. Cart, by contrast, always reflects current price.

### Frontend

- Routing in `src/App.tsx`. Only `Home` is eager; every other page is `lazy()`. Guarded routes wrap in `<ProtectedRoute>` (add `adminOnly` for `/admin/*`).
- `src/lib/api.ts` is the single axios instance for all API calls. It attaches the bearer token and handles 401 by refreshing once and retrying, using a **single-flight** promise so concurrent 401s share one refresh. A separate `refreshClient` makes the refresh call so it can't re-enter that handler. On refresh failure it clears the session and fires a `window` `auth:logout` event. Don't call `axios` directly from components or pages.
- `src/lib/tokenStorage.ts` owns the localStorage keys (`tm_access_token`, `tm_refresh_token`, `tm_user`). Read/write through it, never `localStorage` directly.
- Per-domain API modules in `src/lib/` (`catalog.ts`, `cart.ts`, `orders.ts`, `admin.ts`) — components call these, not `api` directly.
- Global state is `AuthContext` and `CartContext` only. No Redux/Zustand.
- `@/*` resolves to `src/*` (set in both `vite.config.ts` and `tsconfig.app.json` — changing one alone breaks the other).

### Styling

Tailwind CSS v4, configured entirely in `src/index.css` via `@theme` — **there is no `tailwind.config.js`**. Brand tokens live there: `ink-*` (near-black), `gold-*`, `ember-*`, `paper-*`, `chili-*`, the four font families (`display`/Anton, `masthead`/Playfair, `marker`/Permanent Marker, `sans`/Inter), and named keyframe animations. Use these tokens; don't introduce raw hex values or arbitrary one-off colors. Framer Motion handles animation, with shared variants in `src/lib/motion.ts`.

The frontend design is finalized and client-approved (v1.0). Treat visual changes as out of scope unless asked — wiring existing UI to the API is the ongoing work.

## Configuration

Frontend: `VITE_API_BASE_URL` (default `http://localhost:8080/api/v1`). Only `VITE_`-prefixed vars reach client code; typed in `src/vite-env.d.ts`. `.env` is gitignored, `.env.example` is committed.

Backend profiles: `dev` (local MySQL, permissive fallbacks) and `prod` (`application-prod.yml`, **every value from an env var with no default** so it fails fast on startup). Selected by `SPRING_PROFILES_ACTIVE`, defaults to `dev`.

Never add a fallback default to `application-prod.yml`, and never commit a real secret. The dev JWT secret is intentionally an obvious throwaway.

## Known limitations

Documented in `backend/README.md` and intentionally deferred — don't "fix" these unprompted: uploaded images live on a Docker volume on one box (needs object storage before it matters at scale), rate limiting is in-memory/per-instance, Dokploy stores environment variables in plaintext in its internal database, and there is a single production host with no replication.

## Conventions

- Commits: follow the user's `/caveman-commit` convention. Never add a Claude co-author trailer.
- Backend tests are unit-level with Mockito-mocked repositories — no database, safe to run anywhere. Two existing tests are regression guards for real bugs (cart-item removal via the `Cart.items` collection rather than a direct repository delete; reuse-detection's revoke-all actually committing). Don't rewrite them without understanding what they pin down.
- Comments in this codebase explain *why*, often flagging a deliberate non-obvious choice. Match that: don't strip them, don't add comments that restate the code.
