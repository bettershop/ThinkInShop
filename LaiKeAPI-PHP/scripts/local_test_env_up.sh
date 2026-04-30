#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/docker-compose.local-test.yml"

if ! command -v docker >/dev/null 2>&1; then
    echo "docker 未安装，请先安装 Docker Desktop 或 Docker Engine。"
    exit 1
fi

mkdir -p "${ROOT_DIR}/runtime/local-test/mysql" "${ROOT_DIR}/runtime/local-test/redis"

echo "[1/3] 启动本地 MySQL/Redis ..."
SERVICES=("mysql" "redis")
if lsof -iTCP:6339 -sTCP:LISTEN >/dev/null 2>&1; then
    echo "检测到 6339 端口已有 Redis 占用，将复用现有 Redis，仅启动 MySQL 容器。"
    SERVICES=("mysql")
fi

docker compose -f "${COMPOSE_FILE}" up -d "${SERVICES[@]}"

echo "[2/3] 等待 MySQL 就绪 ..."
MYSQL_READY=0
for _ in $(seq 1 90); do
    if docker compose -f "${COMPOSE_FILE}" exec -T mysql \
        mysqladmin ping -h127.0.0.1 -uroot -p000000 --silent >/dev/null 2>&1; then
        MYSQL_READY=1
        break
    fi
    sleep 2
done

if [[ "${MYSQL_READY}" -ne 1 ]]; then
    echo "MySQL 启动超时。"
    exit 1
fi

echo "[3/3] 等待 Redis 就绪 ..."
REDIS_READY=0
if [[ " ${SERVICES[*]} " == *" redis "* ]]; then
    for _ in $(seq 1 60); do
        if docker compose -f "${COMPOSE_FILE}" exec -T redis \
            redis-cli -a laiketui18 -p 6339 ping 2>/dev/null | grep -q "PONG"; then
            REDIS_READY=1
            break
        fi
        sleep 1
    done
else
    for _ in $(seq 1 60); do
        if redis-cli -h 127.0.0.1 -p 6339 -a laiketui18 ping 2>/dev/null | grep -q "PONG"; then
            REDIS_READY=1
            break
        fi
        sleep 1
    done
fi

if [[ "${REDIS_READY}" -ne 1 ]]; then
    echo "Redis 启动超时。"
    exit 1
fi

cat <<'EOF'
本地联调环境已就绪:
  - MySQL: 127.0.0.1:3306 / root / 000000 / db=lkt_db
  - Redis: 127.0.0.1:6339 / password=laiketui18 / db=12

下一步可执行:
  1) bash scripts/import_online_db_to_local.sh
  2) php think
EOF
