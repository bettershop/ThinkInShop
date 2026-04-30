#!/usr/bin/env bash
set -e

echo "========================================"
echo "  LaiKeJavaViews 项目安装脚本"
echo "========================================"
echo ""

# 检测 nvm
if [ -d "$NVM_DIR" ] || [ -f "$HOME/.nvm/nvm.sh" ]; then
  export NVM_DIR="$HOME/.nvm"
  [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
  if command -v nvm &> /dev/null; then
    echo "→ 使用 nvm 切换到 Node v14.20..."
    nvm use 14.20 2>/dev/null || nvm install 14.20
  fi
fi

echo "→ 当前 Node 版本: $(node -v)"
echo "→ 当前 npm 版本: $(npm -v)"
echo ""

# 清空缓存
echo "→ 清空 npm 缓存..."
npm cache clean --force
echo ""

# 安装依赖
echo "→ 安装依赖 (npm install --legacy-peer-deps)..."
if npm install --legacy-peer-deps; then
  echo ""
  echo "========================================"
  echo "  ✅ 安装完成！"
  echo "========================================"
  echo ""
  echo "  启动开发服务器: npm run dev"
  echo "  访问地址: http://localhost:9528"
  echo ""
  echo "  构建命令:"
  echo "    npm run java:dev   - Java 开发环境"
  echo "    npm run java:prod  - Java 生产环境"
  echo "    npm run java:test  - Java 测试环境"
  echo "    npm run php:dev    - PHP 开发环境"
  echo "    npm run php:prod   - PHP 生产环境"
  echo "    npm run php:test   - PHP 测试环境"
  echo "========================================"
else
  echo ""
  echo "⚠️  安装失败，尝试降级 npm..."
  npm install npm@6.14.10 -g
  npm cache clean --force
  npm install --legacy-peer-deps
fi
