# Twisted Momos — Frontend

React 19 + Vite + TypeScript site and ordering UI for Twisted Momos, talking to the Spring Boot API in `../backend`.

## Stack

React 19 · Vite · TypeScript · Tailwind CSS · Framer Motion · React Router · Axios

## Prerequisites

- Node.js 20+ and npm
- The backend running locally (see `../backend/README.md`) — or any reachable instance, pointed to via `VITE_API_BASE_URL`

## Running locally

1. Install dependencies:
   ```bash
   npm install
   ```

2. Copy the env template and adjust if needed (defaults already point at a local backend on port 8080):
   ```bash
   cp .env.example .env
   ```

3. Start the dev server:
   ```bash
   npm run dev
   ```
   Opens on `http://localhost:5173` (or the next free port).

## Scripts

| Command | Purpose |
|---|---|
| `npm run dev` | Vite dev server with HMR |
| `npm run build` | Type-checks (`tsc -b`) then builds a production bundle to `dist/` |
| `npm run preview` | Serves the built `dist/` locally, for a final sanity check before deploying |
| `npm run lint` | Runs oxlint |

## Configuration

Environment variables are read by Vite at build/dev time and must be prefixed `VITE_` to be exposed to client code (see `src/vite-env.d.ts` for the typed shape).

| Variable | Purpose | Local default |
|---|---|---|
| `VITE_API_BASE_URL` | Base URL the app calls for all backend API requests (see `src/lib/api.ts`) | `http://localhost:8080/api/v1` |

`.env` is gitignored — never commit real values. `.env.example` documents the shape and is committed.

For production (Vercel), set `VITE_API_BASE_URL` to the deployed backend's URL (e.g. `https://<your-render-app>.onrender.com/api/v1`) in the Vercel project's environment variables.

## Project layout

```
src/
├── pages/            route-level components (Home, Menu, Cart, Checkout, Orders, admin/, ...)
├── components/        reusable UI (ui/, layout/, sections/, auth/, admin/)
├── context/           React context providers (AuthContext, CartContext)
├── lib/                API clients and data-transform helpers (api.ts, catalog.ts, cart.ts, orders.ts, admin.ts, ...)
└── main.tsx, App.tsx   entry point and route table
```

The `@/*` import alias resolves to `src/*` (configured in `vite.config.ts` and `tsconfig.app.json`).

## Deployment

Deployed on Vercel with this directory (`frontend/`) as the project's **Root Directory** — set that in the Vercel project settings. Build command and output directory are the framework defaults for Vite (`npm run build`, `dist/`).
