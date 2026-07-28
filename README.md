# Twisted Momos

Bold street-food momo brand — marketing site plus a full ordering system, as a monorepo.

## Structure

```
TwistedMomo/
├── frontend/    React 19 + Vite + TypeScript site & ordering UI — see frontend/README.md
├── backend/     Spring Boot 4.1 REST API — see backend/README.md
├── .gitignore   monorepo-wide (OS/editor) ignores only — each package owns its own build-output ignores
└── README.md    you are here
```

`frontend/` and `backend/` are each self-contained: their own dependency manifest, lockfile, `.gitignore`, and README. Nothing in one references paths inside the other — they only ever talk to each other over HTTP.

## Stack

**Frontend** — React 19, Vite, TypeScript, Tailwind CSS, Framer Motion, React Router. Deployed on Vercel.

**Backend** — Java 21, Spring Boot 4.1, Spring Security, JWT auth, Spring Data JPA, MySQL, Flyway, MapStruct, Maven. Deployed on Render.

**Database** — MySQL locally for development; AWS RDS MySQL in production. Only datasource configuration changes between environments — see `backend/README.md`.

## Getting started

- Frontend: `cd frontend && npm install && npm run dev` — see [`frontend/README.md`](./frontend/README.md).
- Backend: see [`backend/README.md`](./backend/README.md).

## Status

- Frontend: finalized and client-approved (v1.0), now progressively wired to the live backend API as each phase ships. Login/register/account state, menu browsing, cart, checkout/order history, and a full `/admin` console (categories, menu, orders) are all live. Recently restructured from repo-root into `frontend/` as part of the monorepo cleanup ahead of deployment — see `frontend/README.md`.
- Backend: feature-complete and hardened — Phase 0 (bootstrap), Phase 1 (auth & users), Phase 2 (catalog), Phase 3 (cart), Phase 4 (checkout & orders), Phase 5 (admin ops), and Phase 6 (hardening: refresh token rotation with reuse detection, login rate limiting, a unit test suite) all complete. See `backend/README.md` for the phase roadmap and its "Known limitations" section for what's intentionally deferred (S3 image storage, distributed rate limiting, CI).
