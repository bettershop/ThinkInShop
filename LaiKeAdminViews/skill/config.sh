#!/usr/bin/env bash
# LaiKeJavaViews 本地 API 配置脚本
# 用法: bash skill/config.sh
# 生成 .env.local 文件，自动覆盖 .env.development 中的 VUE_APP_BASE_API

set -e

cd "$(dirname "$0")/.."

ENV_FILE=".env.local"

echo "========================================"
echo "  LaiKeJavaViews 本地 API 配置"
echo "========================================"
echo ""

# 检测当前配置
CURRENT_API=""
CURRENT_TYPE=""
if [ -f "$ENV_FILE" ]; then
  CURRENT_API=$(grep "VUE_APP_BASE_API" "$ENV_FILE" 2>/dev/null | sed "s/.*= *'//" | sed "s/'.*//" || echo "")
  CURRENT_TYPE=$(grep "VUE_APP_LANG_TYPE" "$ENV_FILE" 2>/dev/null | sed "s/.*= *//" || echo "")
else
  CURRENT_API=$(grep "VUE_APP_BASE_API" .env.development | sed "s/.*= *'//" | sed "s/'.*//" || echo "http://localhost:8000")
  CURRENT_TYPE=$(grep "VUE_APP_LANG_TYPE" .env.development | sed "s/.*= *//" || echo "1")
fi

echo "当前配置:"
echo "  API 地址:  $CURRENT_API"
echo "  后端类型:  $CURRENT_TYPE (1=Java, 2=PHP)"
echo ""

echo "请选择后端类型:"
echo "  [1] Java  (默认端口 8000)"
echo "  [2] PHP   (本地 PHP 项目)"
echo "  [c] 自定义 API 地址"
echo ""
read -p "请输入 [1/2/c] (默认保留当前): " BACKEND_TYPE

# 设置默认值
DEFAULT_API=""
DEFAULT_LANG=""

case "$BACKEND_TYPE" in
  1)
    DEFAULT_API="http://localhost:8000"
    DEFAULT_LANG="1"
    ;;
  2)
    echo ""
    read -p "请输入 PHP 后端地址 (如 http://localhost/you-project): " PHP_URL
    DEFAULT_API="${PHP_URL:-http://localhost}"
    DEFAULT_LANG="2"
    ;;
  c)
    echo ""
    read -p "请输入自定义 API 地址: " CUSTOM_URL
    if [ -n "$CUSTOM_URL" ]; then
      DEFAULT_API="$CUSTOM_URL"
    else
      DEFAULT_API="$CURRENT_API"
    fi
    read -p "后端类型? [1=Java / 2=PHP] (默认: $CURRENT_TYPE): " CUSTOM_TYPE
    DEFAULT_LANG="${CUSTOM_TYPE:-$CURRENT_TYPE}"
    ;;
  *)
    echo ""
    echo "保留当前配置"
    DEFAULT_API="$CURRENT_API"
    DEFAULT_LANG="$CURRENT_TYPE"
    ;;
esac

# 去除末尾斜杠
DEFAULT_API="${DEFAULT_API%/}"

# 写入 .env.local
cat > "$ENV_FILE" << EOF
# 本地 API 配置 (由 skill/config.sh 生成)
# 此文件会覆盖 .env.development 中的同名字段
# 不会提交到版本控制

VUE_APP_BASE_API = '${DEFAULT_API}'
VUE_APP_LANG_TYPE = ${DEFAULT_LANG}
EOF

echo ""
echo "========================================"
echo "  ✅ 配置完成！"
echo "========================================"
echo ""
echo "  文件:     $ENV_FILE"
echo "  API 地址: $DEFAULT_API"
echo "  后端类型: $DEFAULT_LANG (1=Java, 2=PHP)"
echo ""
echo "  下次执行 npm run dev 将使用新配置"
echo "  如需修改配置，再次运行: bash skill/config.sh"
echo "========================================"
