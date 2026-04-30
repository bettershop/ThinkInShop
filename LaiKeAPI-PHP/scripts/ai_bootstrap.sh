#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PORT="${PORT:-8001}"
HOST="${HOST:-127.0.0.1}"

cd "$ROOT_DIR"

echo "[1/6] Check PHP..."
PHP_VERSION="$(php -r 'echo PHP_MAJOR_VERSION.\".\".PHP_MINOR_VERSION;' 2>/dev/null || echo '0.0')"
php -v | head -n 1
if [[ "$PHP_VERSION" < "8.4" ]]; then
  echo "ERROR: PHP ${PHP_VERSION} detected, require >= 8.4 and < 8.5"
  exit 1
fi

echo "[2/6] Check Composer..."
composer --version

echo "[3/6] Install dependencies..."
composer install --no-dev --optimize-autoloader

echo "[4/6] Discover services..."
php think service:discover

echo "[5/6] Publish vendor assets..."
php think vendor:publish || true

echo "[6/6] Start dev server and health-check..."
php think run -H "$HOST" -p "$PORT" >/tmp/laikeapi_tp8_bootstrap.log 2>&1 &
SERVER_PID=$!

cleanup() {
  if kill -0 "$SERVER_PID" >/dev/null 2>&1; then
    kill "$SERVER_PID" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

sleep 2
HEALTH_URL="http://${HOST}:${PORT}/?api=admin.Index.index"
if curl -fsS "$HEALTH_URL" >/dev/null; then
  echo "Bootstrap success."
  echo "Health URL: $HEALTH_URL"
  echo "If you want to keep server running manually:"
  echo "  php think run -H 0.0.0.0 -p ${PORT}"
else
  echo "Bootstrap failed: health check not passed."
  echo "Check log: /tmp/laikeapi_tp8_bootstrap.log"
  exit 1
fi
