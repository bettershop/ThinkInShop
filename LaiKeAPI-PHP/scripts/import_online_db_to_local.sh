#!/usr/bin/env bash
set -euo pipefail

# 远端库（默认按当前会话要求）
REMOTE_HOST="${REMOTE_HOST:-47.107.123.240}"
REMOTE_PORT="${REMOTE_PORT:-3386}"
REMOTE_DB="${REMOTE_DB:-v3_db}"
REMOTE_USER="${REMOTE_USER:-root}"
REMOTE_PASSWORD="${REMOTE_PASSWORD:-123456}"

# 本地库（默认对齐 LaikeAPI config/db_config.php）
LOCAL_HOST="${LOCAL_HOST:-127.0.0.1}"
LOCAL_PORT="${LOCAL_PORT:-3306}"
LOCAL_DB="${LOCAL_DB:-lkt_db}"
LOCAL_USER="${LOCAL_USER:-root}"
LOCAL_PASSWORD="${LOCAL_PASSWORD:-123456}"
LOCAL_DOCKER_CONTAINER="${LOCAL_DOCKER_CONTAINER:-laikeapi-local-mysql}"

if ! command -v mysqldump >/dev/null 2>&1; then
    echo "未找到 mysqldump，请先安装 MySQL 客户端。"
    exit 1
fi
if ! command -v mysql >/dev/null 2>&1; then
    echo "未找到 mysql 客户端，请先安装 MySQL 客户端。"
    exit 1
fi

LOCAL_USE_DOCKER=0
if command -v docker >/dev/null 2>&1 && docker ps --format '{{.Names}}' | grep -qx "${LOCAL_DOCKER_CONTAINER}"; then
    LOCAL_USE_DOCKER=1
fi

run_local_mysql() {
    if [[ "${LOCAL_USE_DOCKER}" -eq 1 ]]; then
        docker exec -i "${LOCAL_DOCKER_CONTAINER}" mysql -u"${LOCAL_USER}" -p"${LOCAL_PASSWORD}" "$@"
    else
        MYSQL_PWD="${LOCAL_PASSWORD}" mysql -h"${LOCAL_HOST}" -P"${LOCAL_PORT}" -u"${LOCAL_USER}" "$@"
    fi
}

echo "[1/5] 校验远端连接 ${REMOTE_HOST}:${REMOTE_PORT}/${REMOTE_DB} ..."
MYSQL_PWD="${REMOTE_PASSWORD}" mysql \
    -h"${REMOTE_HOST}" -P"${REMOTE_PORT}" -u"${REMOTE_USER}" \
    --connect-timeout=10 -e "SELECT 1;" >/dev/null

echo "[2/5] 校验本地连接 ${LOCAL_HOST}:${LOCAL_PORT}/${LOCAL_DB} ..."
run_local_mysql --connect-timeout=10 -e "SELECT 1;" >/dev/null

echo "[3/5] 重建本地目标库 ${LOCAL_DB} ..."
run_local_mysql <<SQL
DROP DATABASE IF EXISTS \`${LOCAL_DB}\`;
CREATE DATABASE \`${LOCAL_DB}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
SQL

echo "[4/5] 从远端导出并导入本地（耗时取决于库大小）..."
if [[ "${LOCAL_USE_DOCKER}" -eq 1 ]]; then
    MYSQL_IMPORT_CMD=(docker exec -i "${LOCAL_DOCKER_CONTAINER}" mysql -u"${LOCAL_USER}" -p"${LOCAL_PASSWORD}" "${LOCAL_DB}")
else
    MYSQL_IMPORT_CMD=(mysql -h"${LOCAL_HOST}" -P"${LOCAL_PORT}" -u"${LOCAL_USER}" "${LOCAL_DB}")
fi

MYSQL_PWD="${REMOTE_PASSWORD}" mysqldump \
    -h"${REMOTE_HOST}" -P"${REMOTE_PORT}" -u"${REMOTE_USER}" "${REMOTE_DB}" \
    --default-character-set=utf8mb4 \
    --single-transaction \
    --set-gtid-purged=OFF \
    --routines --events --triggers \
    --hex-blob \
    --column-statistics=0 \
    --no-tablespaces \
| MYSQL_PWD="${LOCAL_PASSWORD}" "${MYSQL_IMPORT_CMD[@]}"

echo "[5/5] 导入后基础校验 ..."
run_local_mysql -D"${LOCAL_DB}" -e "
SELECT
  (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${LOCAL_DB}') AS table_count,
  (SELECT COUNT(*) FROM lkt_admin) AS admin_count,
  (SELECT COUNT(*) FROM lkt_config) AS config_count,
  (SELECT COUNT(*) FROM lkt_file_delivery) AS file_delivery_count;
"

echo "完成：远端 ${REMOTE_DB} 已导入本地 ${LOCAL_DB}。"
