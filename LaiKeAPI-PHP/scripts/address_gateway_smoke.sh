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
    printf '%-36s code=%-10s message=%s\n' "${label}" "${code}" "${message}"
}

echo "Gateway Root: ${GATEWAY_ROOT}"
echo "Store: ${STORE_ID}  MallStoreType: ${MALL_STORE_TYPE}  AppStoreType: ${APP_STORE_TYPE}"
echo "Account: ${ACCOUNT}"
echo

# 1) PC 商城账号登录（type=3 账号登录）
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
    echo "ERROR: mall.Login.login 未拿到 access_id，停止后续回测。"
    exit 1
fi

# 2) 移动端账号登录（type=3 账号登录）
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
    echo "ERROR: app.login.login 未拿到 access_id，停止后续回测。"
    exit 1
fi

echo
echo "Tokens Ready:"
echo "mall_access_id=${mall_access_id:0:24}..."
echo "app_access_id=${app_access_id:0:24}..."
echo

# 3) Address 接口回测（读接口优先，避免改脏数据）
mall_resp_1="$(curl -sS "${MALL_BASE_URL}" --data-urlencode "api=mall.Address.addressManagement" --data-urlencode "access_id=${mall_access_id}" --data-urlencode "language=${LANGUAGE}")"
print_result "mall.Address.addressManagement" "${mall_resp_1}"

mall_resp_2="$(curl -sS "${MALL_BASE_URL}" --data-urlencode "api=mall.Address.index" --data-urlencode "access_id=${mall_access_id}" --data-urlencode "language=${LANGUAGE}")"
print_result "mall.Address.index" "${mall_resp_2}"

app_resp_1="$(curl -sS "${APP_BASE_URL}" --data-urlencode "api=app.Address.AddressManagement" --data-urlencode "access_id=${app_access_id}" --data-urlencode "language=${LANGUAGE}" --data-urlencode "lang_code=${LANGUAGE}")"
print_result "app.Address.AddressManagement" "${app_resp_1}"

app_resp_2="$(curl -sS "${APP_BASE_URL}" --data-urlencode "api=app.Address.index" --data-urlencode "access_id=${app_access_id}" --data-urlencode "language=${LANGUAGE}" --data-urlencode "lang_code=${LANGUAGE}")"
print_result "app.Address.index" "${app_resp_2}"

app_resp_3="$(curl -sS "${APP_BASE_URL}" --data-urlencode "api=app.Address.getCityArr" --data-urlencode "GroupID=2" --data-urlencode "access_id=${app_access_id}" --data-urlencode "language=${LANGUAGE}" --data-urlencode "lang_code=${LANGUAGE}")"
print_result "app.Address.getCityArr" "${app_resp_3}"

app_resp_4="$(curl -sS "${APP_BASE_URL}" --data-urlencode "api=app.Address.getCountyInfo" --data-urlencode "GroupID=3" --data-urlencode "access_id=${app_access_id}" --data-urlencode "language=${LANGUAGE}" --data-urlencode "lang_code=${LANGUAGE}")"
print_result "app.Address.getCountyInfo" "${app_resp_4}"

first_addr_id="$(json_first_id "${mall_resp_2}")"
if [[ -n "${first_addr_id}" ]]; then
    mall_resp_3="$(curl -sS "${MALL_BASE_URL}" --data-urlencode "api=mall.Address.upAddsindex" --data-urlencode "addr_id=${first_addr_id}" --data-urlencode "access_id=${mall_access_id}" --data-urlencode "language=${LANGUAGE}")"
    print_result "mall.Address.upAddsindex" "${mall_resp_3}"

    app_resp_5="$(curl -sS "${APP_BASE_URL}" --data-urlencode "api=app.Address.up_addsindex" --data-urlencode "addr_id=${first_addr_id}" --data-urlencode "access_id=${app_access_id}" --data-urlencode "language=${LANGUAGE}" --data-urlencode "lang_code=${LANGUAGE}")"
    print_result "app.Address.up_addsindex" "${app_resp_5}"
else
    echo "INFO: 地址列表为空，跳过 upAddsindex/up_addsindex 回测。"
fi
