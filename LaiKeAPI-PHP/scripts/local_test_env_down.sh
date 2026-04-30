#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/docker-compose.local-test.yml"

if [[ "${1:-}" == "--purge-data" ]]; then
    echo "关闭并清理容器与数据卷目录 ..."
    docker compose -f "${COMPOSE_FILE}" down -v
    rm -rf "${ROOT_DIR}/runtime/local-test/mysql" "${ROOT_DIR}/runtime/local-test/redis"
else
    echo "关闭容器 ..."
    docker compose -f "${COMPOSE_FILE}" down
fi

echo "本地联调环境已关闭。"
