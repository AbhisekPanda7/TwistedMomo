# Local development image only — runs the Vite dev server with HMR.
# Production deploys from Vercel and builds `dist/` there; there is no
# nginx/static stage here on purpose.
FROM node:22-alpine
WORKDIR /app

# Lockfile-only install layer: `npm ci` reruns solely when a manifest changes.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .

EXPOSE 5173

# --host binds 0.0.0.0; Vite defaults to localhost, which inside a container
# means the port is unreachable from the host even when published.
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
