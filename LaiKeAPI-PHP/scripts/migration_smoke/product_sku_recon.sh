#!/usr/bin/env bash
set -euo pipefail

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-lkt_db}"
DB_USER="${DB_USER:-root}"
DB_PASS="${DB_PASS:-123456}"

SQL_FILE="${SQL_FILE:-runtime/ai_migration/T09/product_sku_recon.sql}"
OUT_DIR="${OUT_DIR:-runtime/ai_migration/T10}"

mkdir -p "${OUT_DIR}"
ts="$(date +%Y%m%d_%H%M%S)"
RAW_LOG="${OUT_DIR}/product_sku_recon_${ts}.log"
RAW_ERR="${OUT_DIR}/product_sku_recon_${ts}.err"
SUMMARY_MD="${OUT_DIR}/product_sku_recon_${ts}_summary.md"

if [[ ! -f "${SQL_FILE}" ]]; then
    echo "ERROR: SQL_FILE not found: ${SQL_FILE}" >&2
    exit 1
fi

mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASS}" --default-character-set=utf8mb4 "${DB_NAME}" \
    < "${SQL_FILE}" > "${RAW_LOG}" 2> "${RAW_ERR}"

get_metric_col() {
    local key="$1"
    local col="$2"
    awk -F'\t' -v k="${key}" -v c="${col}" '$1==k{print $c; exit}' "${RAW_LOG}"
}

base_products="$(get_metric_col BASE_products 2)"
base_configures="$(get_metric_col BASE_configures 2)"
base_stocks="$(get_metric_col BASE_stocks 2)"

q2_product_bad_status="$(get_metric_col Q2_product_bad_status 2)"
q2_product_bad_mch_status="$(get_metric_col Q2_product_bad_mch_status 2)"
q2_product_bad_recycle="$(get_metric_col Q2_product_bad_recycle 2)"
q2_configure_bad_status="$(get_metric_col Q2_configure_bad_status 2)"
q2_stock_bad_type="$(get_metric_col Q2_stock_bad_type 2)"

q3_product_num_negative="$(get_metric_col Q3_product_num_negative 2)"
q3_configure_num_negative="$(get_metric_col Q3_configure_num_negative 2)"
q3_configure_total_num_negative="$(get_metric_col Q3_configure_total_num_negative 2)"

q4_mismatch_cnt="$(get_metric_col Q4_header_sku_stock_mismatch_cnt 2)"
q4_mismatch_max_abs="$(get_metric_col Q4_header_sku_stock_mismatch_cnt 3)"

q5_total_lt_num_cnt="$(get_metric_col Q5_configure_total_lt_num_cnt 2)"

q6_orphan_configure_cnt="$(get_metric_col Q6_orphan_configure_cnt 2)"
q6_orphan_stock_by_product_cnt="$(get_metric_col Q6_orphan_stock_by_product_cnt 2)"
q6_orphan_stock_by_attr_cnt="$(get_metric_col Q6_orphan_stock_by_attr_cnt 2)"
q6_pid_attr_mismatch_cnt="$(get_metric_col Q6_stock_pid_attr_pid_mismatch_cnt 2)"

q7_supplier_superior_missing_cnt="$(get_metric_col Q7_supplier_superior_missing_cnt 2)"
q7_supplier_has_id_but_no_superior_cnt="$(get_metric_col Q7_supplier_has_id_but_no_superior_cnt 2)"

q8_status_4_5_cnt="$(get_metric_col Q8_status_4_5_cnt 2)"
q8_mch_status_0_cnt="$(get_metric_col Q8_mch_status_0_cnt 2)"

cat > "${SUMMARY_MD}" <<EOM
# Product/SKU Recon Summary

Time: ${ts}

## Base

| Metric | Value |
|---|---:|
| BASE_products | ${base_products:-N/A} |
| BASE_configures | ${base_configures:-N/A} |
| BASE_stocks | ${base_stocks:-N/A} |

## Key Checks

| Metric | Value |
|---|---:|
| Q2_product_bad_status | ${q2_product_bad_status:-N/A} |
| Q2_product_bad_mch_status | ${q2_product_bad_mch_status:-N/A} |
| Q2_product_bad_recycle | ${q2_product_bad_recycle:-N/A} |
| Q2_configure_bad_status | ${q2_configure_bad_status:-N/A} |
| Q2_stock_bad_type | ${q2_stock_bad_type:-N/A} |
| Q3_product_num_negative | ${q3_product_num_negative:-N/A} |
| Q3_configure_num_negative | ${q3_configure_num_negative:-N/A} |
| Q3_configure_total_num_negative | ${q3_configure_total_num_negative:-N/A} |
| Q4_header_sku_stock_mismatch_cnt | ${q4_mismatch_cnt:-N/A} |
| Q4_header_sku_stock_mismatch_max_abs | ${q4_mismatch_max_abs:-N/A} |
| Q5_configure_total_lt_num_cnt | ${q5_total_lt_num_cnt:-N/A} |
| Q6_orphan_configure_cnt | ${q6_orphan_configure_cnt:-N/A} |
| Q6_orphan_stock_by_product_cnt | ${q6_orphan_stock_by_product_cnt:-N/A} |
| Q6_orphan_stock_by_attr_cnt | ${q6_orphan_stock_by_attr_cnt:-N/A} |
| Q6_stock_pid_attr_pid_mismatch_cnt | ${q6_pid_attr_mismatch_cnt:-N/A} |
| Q7_supplier_superior_missing_cnt | ${q7_supplier_superior_missing_cnt:-N/A} |
| Q7_supplier_has_id_but_no_superior_cnt | ${q7_supplier_has_id_but_no_superior_cnt:-N/A} |
| Q8_status_4_5_cnt | ${q8_status_4_5_cnt:-N/A} |
| Q8_mch_status_0_cnt | ${q8_mch_status_0_cnt:-N/A} |

## Artifacts

- Raw output: ${RAW_LOG}
- Raw error: ${RAW_ERR}
- Summary: ${SUMMARY_MD}
EOM

echo "Done."
echo "RAW_LOG=${RAW_LOG}"
echo "RAW_ERR=${RAW_ERR}"
echo "SUMMARY_MD=${SUMMARY_MD}"
