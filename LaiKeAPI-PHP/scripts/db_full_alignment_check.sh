#!/usr/bin/env bash
set -euo pipefail

# Full schema alignment check between local_db and v3_db, excluding xxl_job tables.
# Usage:
#   # single-host mode (legacy)
#   MYSQL_HOST=127.0.0.1 MYSQL_PORT=3306 MYSQL_USER=root MYSQL_PWD=xxx \
#   LOCAL_DB=lkt_db V3_DB=v3_db ./scripts/db_full_alignment_check.sh
#
#   # cross-host mode (recommended for local lkt_db + remote v3_db)
#   LOCAL_MYSQL_HOST=127.0.0.1 LOCAL_MYSQL_PORT=3306 LOCAL_MYSQL_USER=root LOCAL_MYSQL_PWD=xxx \
#   V3_MYSQL_HOST=47.107.123.240 V3_MYSQL_PORT=3386 V3_MYSQL_USER=root V3_MYSQL_PWD=yyy \
#   LOCAL_DB=lkt_db V3_DB=v3_db ./scripts/db_full_alignment_check.sh

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PWD="${MYSQL_PWD:-}"

LOCAL_MYSQL_HOST="${LOCAL_MYSQL_HOST:-$MYSQL_HOST}"
LOCAL_MYSQL_PORT="${LOCAL_MYSQL_PORT:-$MYSQL_PORT}"
LOCAL_MYSQL_USER="${LOCAL_MYSQL_USER:-$MYSQL_USER}"
LOCAL_MYSQL_PWD="${LOCAL_MYSQL_PWD:-$MYSQL_PWD}"

V3_MYSQL_HOST="${V3_MYSQL_HOST:-$MYSQL_HOST}"
V3_MYSQL_PORT="${V3_MYSQL_PORT:-$MYSQL_PORT}"
V3_MYSQL_USER="${V3_MYSQL_USER:-$MYSQL_USER}"
V3_MYSQL_PWD="${V3_MYSQL_PWD:-$MYSQL_PWD}"

LOCAL_DB="${LOCAL_DB:-${LKT_DB:-lkt_db}}"
V3_DB="${V3_DB:-v3_db}"
OUT_DIR="${OUT_DIR:-./runtime/db_align_reports/$(date +%Y%m%d_%H%M%S)}"

if ! command -v mysql >/dev/null 2>&1; then
  echo "[ERROR] mysql client not found in PATH"
  exit 1
fi

mkdir -p "$OUT_DIR"

run_query_to_file() {
  local host="$1"
  local port="$2"
  local user="$3"
  local pwd="$4"
  local db_name="$5"
  local sql="$6"
  local out_file="$7"

  local cmd=(mysql -h"$host" -P"$port" -u"$user" "--default-character-set=utf8mb4" --batch --skip-column-names "$db_name" -e "$sql")
  if [[ -n "$pwd" ]]; then
    MYSQL_PWD="$pwd" "${cmd[@]}" | LC_ALL=C sort > "$out_file"
  else
    "${cmd[@]}" | LC_ALL=C sort > "$out_file"
  fi
}

echo "[INFO] Output directory: $OUT_DIR"
echo "[INFO] LOCAL source: ${LOCAL_MYSQL_HOST}:${LOCAL_MYSQL_PORT}/${LOCAL_DB}"
echo "[INFO] V3 source: ${V3_MYSQL_HOST}:${V3_MYSQL_PORT}/${V3_DB}"

echo "[INFO] Exporting table metadata..."
run_query_to_file "$LOCAL_MYSQL_HOST" "$LOCAL_MYSQL_PORT" "$LOCAL_MYSQL_USER" "$LOCAL_MYSQL_PWD" "$LOCAL_DB" "
SELECT table_name, engine, table_collation
FROM information_schema.tables
WHERE table_schema = '$LOCAL_DB'
  AND table_name LIKE 'lkt\\_%'
  AND table_name NOT LIKE 'xxl_job%';
" "$OUT_DIR/local_tables.tsv"

run_query_to_file "$V3_MYSQL_HOST" "$V3_MYSQL_PORT" "$V3_MYSQL_USER" "$V3_MYSQL_PWD" "$V3_DB" "
SELECT table_name, engine, table_collation
FROM information_schema.tables
WHERE table_schema = '$V3_DB'
  AND table_name LIKE 'lkt\\_%'
  AND table_name NOT LIKE 'xxl_job%';
" "$OUT_DIR/v3_tables.tsv"

echo "[INFO] Exporting column metadata..."
run_query_to_file "$LOCAL_MYSQL_HOST" "$LOCAL_MYSQL_PORT" "$LOCAL_MYSQL_USER" "$LOCAL_MYSQL_PWD" "$LOCAL_DB" "
SELECT table_name, column_name, column_type, is_nullable, IFNULL(column_default,'<NULL>'), column_comment
FROM information_schema.columns
WHERE table_schema = '$LOCAL_DB'
  AND table_name LIKE 'lkt\\_%'
  AND table_name NOT LIKE 'xxl_job%';
" "$OUT_DIR/local_columns.tsv"

run_query_to_file "$V3_MYSQL_HOST" "$V3_MYSQL_PORT" "$V3_MYSQL_USER" "$V3_MYSQL_PWD" "$V3_DB" "
SELECT table_name, column_name, column_type, is_nullable, IFNULL(column_default,'<NULL>'), column_comment
FROM information_schema.columns
WHERE table_schema = '$V3_DB'
  AND table_name LIKE 'lkt\\_%'
  AND table_name NOT LIKE 'xxl_job%';
" "$OUT_DIR/v3_columns.tsv"

echo "[INFO] Exporting index metadata..."
run_query_to_file "$LOCAL_MYSQL_HOST" "$LOCAL_MYSQL_PORT" "$LOCAL_MYSQL_USER" "$LOCAL_MYSQL_PWD" "$LOCAL_DB" "
SELECT table_name, index_name, non_unique, seq_in_index, column_name
FROM information_schema.statistics
WHERE table_schema = '$LOCAL_DB'
  AND table_name LIKE 'lkt\\_%'
  AND table_name NOT LIKE 'xxl_job%';
" "$OUT_DIR/local_indexes.tsv"

run_query_to_file "$V3_MYSQL_HOST" "$V3_MYSQL_PORT" "$V3_MYSQL_USER" "$V3_MYSQL_PWD" "$V3_DB" "
SELECT table_name, index_name, non_unique, seq_in_index, column_name
FROM information_schema.statistics
WHERE table_schema = '$V3_DB'
  AND table_name LIKE 'lkt\\_%'
  AND table_name NOT LIKE 'xxl_job%';
" "$OUT_DIR/v3_indexes.tsv"

# Diff helpers
comm -23 "$OUT_DIR/local_tables.tsv" "$OUT_DIR/v3_tables.tsv" > "$OUT_DIR/diff_tables_local_only.tsv" || true
comm -13 "$OUT_DIR/local_tables.tsv" "$OUT_DIR/v3_tables.tsv" > "$OUT_DIR/diff_tables_v3_only.tsv" || true

comm -23 "$OUT_DIR/local_columns.tsv" "$OUT_DIR/v3_columns.tsv" > "$OUT_DIR/diff_columns_local_only.tsv" || true
comm -13 "$OUT_DIR/local_columns.tsv" "$OUT_DIR/v3_columns.tsv" > "$OUT_DIR/diff_columns_v3_only.tsv" || true

comm -23 "$OUT_DIR/local_indexes.tsv" "$OUT_DIR/v3_indexes.tsv" > "$OUT_DIR/diff_indexes_local_only.tsv" || true
comm -13 "$OUT_DIR/local_indexes.tsv" "$OUT_DIR/v3_indexes.tsv" > "$OUT_DIR/diff_indexes_v3_only.tsv" || true

count_lines() {
  local f="$1"
  if [[ -f "$f" ]]; then
    wc -l < "$f" | tr -d ' '
  else
    echo 0
  fi
}

LOCAL_TABLE_COUNT=$(count_lines "$OUT_DIR/local_tables.tsv")
V3_TABLE_COUNT=$(count_lines "$OUT_DIR/v3_tables.tsv")
LOCAL_ONLY_TABLES=$(count_lines "$OUT_DIR/diff_tables_local_only.tsv")
V3_ONLY_TABLES=$(count_lines "$OUT_DIR/diff_tables_v3_only.tsv")
LOCAL_ONLY_COLUMNS=$(count_lines "$OUT_DIR/diff_columns_local_only.tsv")
V3_ONLY_COLUMNS=$(count_lines "$OUT_DIR/diff_columns_v3_only.tsv")
LOCAL_ONLY_INDEXES=$(count_lines "$OUT_DIR/diff_indexes_local_only.tsv")
V3_ONLY_INDEXES=$(count_lines "$OUT_DIR/diff_indexes_v3_only.tsv")

SUMMARY_FILE="$OUT_DIR/summary.txt"
{
  echo "Full DB alignment check (excluding xxl_job%)"
  echo "timestamp: $(date '+%Y-%m-%d %H:%M:%S')"
  echo "local_db: $LOCAL_DB"
  echo "v3_db: $V3_DB"
  echo "local_source: ${LOCAL_MYSQL_HOST}:${LOCAL_MYSQL_PORT}"
  echo "v3_source: ${V3_MYSQL_HOST}:${V3_MYSQL_PORT}"
  echo
  echo "table_count_local=$LOCAL_TABLE_COUNT"
  echo "table_count_v3=$V3_TABLE_COUNT"
  echo "diff_tables_local_only=$LOCAL_ONLY_TABLES"
  echo "diff_tables_v3_only=$V3_ONLY_TABLES"
  echo "diff_columns_local_only=$LOCAL_ONLY_COLUMNS"
  echo "diff_columns_v3_only=$V3_ONLY_COLUMNS"
  echo "diff_indexes_local_only=$LOCAL_ONLY_INDEXES"
  echo "diff_indexes_v3_only=$V3_ONLY_INDEXES"
} > "$SUMMARY_FILE"

cat "$SUMMARY_FILE"

echo
if [[ "$LOCAL_ONLY_TABLES" == "0" && "$V3_ONLY_TABLES" == "0" && "$LOCAL_ONLY_COLUMNS" == "0" && "$V3_ONLY_COLUMNS" == "0" && "$LOCAL_ONLY_INDEXES" == "0" && "$V3_ONLY_INDEXES" == "0" ]]; then
  echo "[PASS] No table/column/index diffs found (excluding xxl_job%)."
else
  echo "[WARN] Diffs found. Please inspect: $OUT_DIR"
fi
