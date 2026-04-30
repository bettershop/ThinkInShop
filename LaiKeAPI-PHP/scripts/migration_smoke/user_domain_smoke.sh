#!/usr/bin/env bash
set -euo pipefail

GATEWAY_ROOT="${1:-http://www.laike.com:82}"
STORE_ID="${STORE_ID:-1}"
MALL_STORE_TYPE="${MALL_STORE_TYPE:-6}"
APP_STORE_TYPE="${APP_STORE_TYPE:-2}"
LANGUAGE="${LANGUAGE:-zh_CN}"
ACCOUNT="${ACCOUNT:-000000}"
PASSWORD="${PASSWORD:-000000}"
COUNTRY_NUM="${COUNTRY_NUM:-156}"
CPC="${CPC:-86}"
OUT_DIR="${OUT_DIR:-runtime/ai_migration/T10}"

mkdir -p "${OUT_DIR}"
REPORT_FILE="${OUT_DIR}/user_domain_smoke_$(date +%Y%m%d_%H%M%S).log"

MALL_BASE_URL="${GATEWAY_ROOT}/gw?store_id=${STORE_ID}&store_type=${MALL_STORE_TYPE}"
APP_BASE_URL="${GATEWAY_ROOT}/gw?store_id=${STORE_ID}&store_type=${APP_STORE_TYPE}"

json_field() {
    local json="$1"
    local field="$2"
    echo "${json}" \
        | grep -oE "\"${field}\":[[:space:]]*\"[^\"]*\"" \
        | head -n 1 \
        | sed -E "s/\"${field}\":[[:space:]]*\"([^\"]*)\"/\\1/"
}

json_code() {
    local json="$1"
    echo "${json}" \
        | grep -oE '"code":[[:space:]]*"?[0-9]+' \
        | head -n 1 \
        | grep -oE '[0-9]+'
}

json_msg() {
    local json="$1"
    echo "${json}" \
        | grep -oE '"message":[[:space:]]*"[^"]*"' \
        | head -n 1 \
        | sed -E 's/"message":[[:space:]]*"([^"]*)"/\1/'
}

json_first_id() {
    local json="$1"
    echo "${json}" \
        | grep -oE '"id":[[:space:]]*[0-9]+' \
        | head -n 1 \
        | grep -oE '[0-9]+'
}

print_result() {
    local label="$1"
    local response="$2"
    local code
    local message
    code="$(json_code "${response}")"
    message="$(json_msg "${response}")"
    [[ -z "${code}" ]] && code="(non-json)"
    [[ -z "${message}" ]] && message="(empty)"
    printf '%-36s code=%-10s message=%s\n' "${label}" "${code}" "${message}" | tee -a "${REPORT_FILE}"
}

echo "Gateway Root: ${GATEWAY_ROOT}" | tee -a "${REPORT_FILE}"
echo "Store: ${STORE_ID} MallStoreType: ${MALL_STORE_TYPE} AppStoreType: ${APP_STORE_TYPE}" | tee -a "${REPORT_FILE}"
echo "Account: ${ACCOUNT}" | tee -a "${REPORT_FILE}"
echo | tee -a "${REPORT_FILE}"

# 1) mall 登录
mall_login_resp="$(
    curl -sS "${MALL_BASE_URL}" \
        --data-urlencode "store_id=${STORE_ID}" \
        --data-urlencode "store_type=${MALL_STORE_TYPE}" \
        --data-urlencode "language=${LANGUAGE}" \
        --data-urlencode "api=mall.Login.login" \
        --data-urlencode "type=3" \
        --data-urlencode "phone=${ACCOUNT}" \
        --data-urlencode "password=${PASSWORD}" \
        --data-urlencode "country_num=${COUNTRY_NUM}" \
        --data-urlencode "cpc=${CPC}"
)"
print_result "mall.Login.login" "${mall_login_resp}"
mall_access_id="$(json_field "${mall_login_resp}" "access_id")"

if [[ -z "${mall_access_id}" ]]; then
    echo "ERROR: mall.Login.login 未拿到 access_id，停止。" | tee -a "${REPORT_FILE}"
    exit 1
fi

# 2) app 登录
app_login_resp="$(
    curl -sS "${APP_BASE_URL}" \
        --data-urlencode "store_id=${STORE_ID}" \
        --data-urlencode "store_type=${APP_STORE_TYPE}" \
        --data-urlencode "access_id=" \
        --data-urlencode "language=${LANGUAGE}" \
        --data-urlencode "lang_code=${LANGUAGE}" \
        --data-urlencode "isLogin=1" \
        --data-urlencode "api=app.login.login" \
        --data-urlencode "type=3" \
        --data-urlencode "phone=${ACCOUNT}" \
        --data-urlencode "password=${PASSWORD}" \
        --data-urlencode "country_num=${COUNTRY_NUM}"
)"
print_result "app.login.login" "${app_login_resp}"
app_access_id="$(json_field "${app_login_resp}" "access_id")"

if [[ -z "${app_access_id}" ]]; then
    echo "ERROR: app.login.login 未拿到 access_id，停止。" | tee -a "${REPORT_FILE}"
    exit 1
fi

echo "Tokens Ready: mall=${mall_access_id:0:24}... app=${app_access_id:0:24}..." | tee -a "${REPORT_FILE}"
echo | tee -a "${REPORT_FILE}"

# 3) 用户域读接口回归（不做写入）
app_user_index_resp="$(
    curl -sS "${APP_BASE_URL}" \
        --data-urlencode "api=app.user.index" \
        --data-urlencode "access_id=${app_access_id}" \
        --data-urlencode "language=${LANGUAGE}" \
        --data-urlencode "lang_code=${LANGUAGE}"
)"
print_result "app.user.index" "${app_user_index_resp}"

app_wallet_resp="$(
    curl -sS "${APP_BASE_URL}" \
        --data-urlencode "api=app.user.wallet_detailed" \
        --data-urlencode "access_id=${app_access_id}" \
        --data-urlencode "page=1" \
        --data-urlencode "pagesize=10" \
        --data-urlencode "language=${LANGUAGE}" \
        --data-urlencode "lang_code=${LANGUAGE}"
)"
print_result "app.user.wallet_detailed" "${app_wallet_resp}"

app_address_index_resp="$(
    curl -sS "${APP_BASE_URL}" \
        --data-urlencode "api=app.Address.index" \
        --data-urlencode "access_id=${app_access_id}" \
        --data-urlencode "language=${LANGUAGE}" \
        --data-urlencode "lang_code=${LANGUAGE}"
)"
print_result "app.Address.index" "${app_address_index_resp}"

first_addr_id="$(json_first_id "${app_address_index_resp}")"
if [[ -n "${first_addr_id}" ]]; then
    app_address_detail_resp="$(
        curl -sS "${APP_BASE_URL}" \
            --data-urlencode "api=app.Address.up_addsindex" \
            --data-urlencode "addr_id=${first_addr_id}" \
            --data-urlencode "access_id=${app_access_id}" \
            --data-urlencode "language=${LANGUAGE}" \
            --data-urlencode "lang_code=${LANGUAGE}"
    )"
    print_result "app.Address.up_addsindex" "${app_address_detail_resp}"
else
    echo "INFO: 地址列表为空，跳过 app.Address.up_addsindex" | tee -a "${REPORT_FILE}"
fi

admin_user_info_resp="$(
    curl -sS "${MALL_BASE_URL}" \
        --data-urlencode "api=admin.user.getUserInfo" \
        --data-urlencode "access_id=${mall_access_id}" \
        --data-urlencode "page=1" \
        --data-urlencode "pagesize=10" \
        --data-urlencode "language=${LANGUAGE}"
)"
print_result "admin.user.getUserInfo" "${admin_user_info_resp}"

echo | tee -a "${REPORT_FILE}"
echo "Done. report=${REPORT_FILE}" | tee -a "${REPORT_FILE}"

