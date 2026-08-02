#!/usr/bin/env bash
# Back up the database and the uploads volume. Run nightly from cron; see
# infra/README.md.
#
# This is the entire recovery plan for a single host with no replication. A backup
# that has never been restored is a guess — test a restore quarterly.
set -euo pipefail

BACKUP_DIR="/opt/twistedmomos/backups"
RETENTION_DAYS=14
STAMP=$(date +%Y%m%d-%H%M%S)

# Dokploy runs the stack under Swarm, so container names carry a generated
# suffix and cannot be hardcoded.
MYSQL_CONTAINER=$(docker ps --filter "name=mysql" --format '{{.Names}}' | head -1)
if [ -z "$MYSQL_CONTAINER" ]; then
  echo "ERROR: no running mysql container found" >&2
  exit 1
fi

mkdir -p "$BACKUP_DIR"

# --- Database ---------------------------------------------------------------
DB_OUT="$BACKUP_DIR/twisted_momos-$STAMP.sql.gz"
echo "==> Dumping database from $MYSQL_CONTAINER"

# --single-transaction takes a consistent snapshot without locking InnoDB tables,
# so the application keeps serving for the duration of the dump.
docker exec "$MYSQL_CONTAINER" sh -c \
  'exec mysqldump --single-transaction --quick --routines --triggers \
     -u root -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"' \
  | gzip > "$DB_OUT"

# A dump that failed partway still leaves a file behind, so a zero exit code is
# not evidence of a usable backup. Verify the archive itself.
if ! gzip -t "$DB_OUT" 2>/dev/null; then
  echo "ERROR: $DB_OUT is not a valid gzip archive — removing" >&2
  rm -f "$DB_OUT"
  exit 1
fi

DB_SIZE=$(stat -c%s "$DB_OUT")
if [ "$DB_SIZE" -lt 1000 ]; then
  echo "ERROR: $DB_OUT is only ${DB_SIZE} bytes — almost certainly a failed dump" >&2
  rm -f "$DB_OUT"
  exit 1
fi
echo "==> Wrote $DB_OUT (${DB_SIZE} bytes)"

# --- Uploads ----------------------------------------------------------------
# Menu images uploaded through the admin UI. Re-uploadable by hand, but only if
# someone still has the originals — cheap enough to include here.
UP_OUT="$BACKUP_DIR/uploads-$STAMP.tar.gz"
UPLOADS_VOLUME=$(docker volume ls --format '{{.Name}}' | grep 'backend-uploads' | head -1)

if [ -n "$UPLOADS_VOLUME" ]; then
  echo "==> Archiving uploads volume $UPLOADS_VOLUME"
  docker run --rm \
    -v "$UPLOADS_VOLUME":/data:ro \
    -v "$BACKUP_DIR":/backup \
    alpine:3.21 \
    tar czf "/backup/$(basename "$UP_OUT")" -C /data .

  if gzip -t "$UP_OUT" 2>/dev/null; then
    echo "==> Wrote $UP_OUT ($(stat -c%s "$UP_OUT") bytes)"
  else
    echo "WARNING: uploads archive is corrupt — removing" >&2
    rm -f "$UP_OUT"
  fi
else
  echo "WARNING: no backend-uploads volume found — skipping uploads" >&2
fi

# --- Prune ------------------------------------------------------------------
echo "==> Pruning backups older than $RETENTION_DAYS days"
find "$BACKUP_DIR" -name 'twisted_momos-*.sql.gz' -mtime "+$RETENTION_DAYS" -print -delete
find "$BACKUP_DIR" -name 'uploads-*.tar.gz'       -mtime "+$RETENTION_DAYS" -print -delete

echo "==> Done. Current backups:"
ls -lh "$BACKUP_DIR" | tail -6
