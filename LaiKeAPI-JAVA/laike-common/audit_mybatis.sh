#!/bin/bash

# MyBatis Mapper XML ${} 安全审计脚本 (macOS / Linux)
# 作者: Qwen
# 用法: 在 laike-common 目录下运行 ./audit_mybatis.sh

set -euo pipefail

HIGH_RISK_KEYWORDS="keyword|search|name|username|email|phone|mobile|id|ids|user_id|order|sort|field|column|table|sql|query"
MAPPER_DIR="src/main/resources/db/mapping"

if [ ! -d "$MAPPER_DIR" ]; then
  echo "❌ 错误: Mapper 目录不存在: $MAPPER_DIR"
  echo "请将此脚本放在 laike-common 目录下运行（与 src/ 同级）"
  exit 1
fi

echo "🔍 正在扫描 MyBatis Mapper XML 中的 \${} 用法..."
echo

TEMP_FILE=$(mktemp)
HIGH_RISK_FILE=$(mktemp)
MEDIUM_RISK_FILE=$(mktemp)

trap 'rm -f "$TEMP_FILE" "$HIGH_RISK_FILE" "$MEDIUM_RISK_FILE"' EXIT

# 关键修复：添加 || true 防止 grep 无结果时脚本退出
while IFS= read -r -d '' file; do
  if [ -f "$file" ]; then
    grep -n '\${' "$file" 2>/dev/null | grep -v '^[0-9]*:[[:space:]]*<!--' >> "$TEMP_FILE" || true
  fi
done < <(find "$MAPPER_DIR" -name "*Mapper.xml" -type f -print0)

if [ ! -s "$TEMP_FILE" ]; then
  echo "✅ 未发现非注释中的 \${} 用法，MyBatis SQL 很安全！"
  exit 0
fi

# 分析风险
while IFS= read -r line; do
  [ -z "$line" ] && continue
  file=$(echo "$line" | cut -d: -f1)
  lineno=$(echo "$line" | cut -d: -f2)
  content=$(echo "$line" | cut -d: -f3-)

  echo "$content" | grep -o '\${[^}]*}' | sed 's/\${\([^}]*\)}/\1/g' | while read -r param; do
    param_clean=$(echo "$param" | xargs)
    [ -z "$param_clean" ] && continue
    param_lower=$(echo "$param_clean" | tr '[:upper:]' '[:lower:]')
    if echo "$param_lower" | grep -qE "$HIGH_RISK_KEYWORDS"; then
      echo "$file|$lineno|$param_clean|$content" >> "$HIGH_RISK_FILE"
    else
      echo "$file|$lineno|$param_clean|$content" >> "$MEDIUM_RISK_FILE"
    fi
  done
done < "$TEMP_FILE"

high_count=$(wc -l < "$HIGH_RISK_FILE" 2>/dev/null | tr -d ' ')
medium_count=$(wc -l < "$MEDIUM_RISK_FILE" 2>/dev/null | tr -d ' ')
total=$((high_count + medium_count))

if [ "$total" -eq 0 ]; then
  echo "✅ 未发现有效 \${} 用法（可能都在注释中）"
else
  echo "🚨 发现 $total 处 \${} 使用（可能存在 SQL 注入风险）:"
  echo

  if [ "$high_count" -gt 0 ]; then
    echo "🔴 高危项（疑似用户输入）:"
    while IFS='|' read -r f l p c; do
      echo "  $f : L$l"
      echo "    参数: \${$p}"
      echo "    上下文: $c"
      echo "    ───────────────────────────────"
    done < "$HIGH_RISK_FILE"
  fi

  if [ "$medium_count" -gt 0 ]; then
    echo "🟡 中低危项（可能是动态字段/表名，请人工确认）:"
    while IFS='|' read -r f l p c; do
      echo "  $f : L$l"
      echo "    参数: \${$p}"
      echo "    上下文: $c"
      echo "    ───────────────────────────────"
    done < "$MEDIUM_RISK_FILE"
  fi

  echo
  echo "📊 总结: 🔴 $high_count 高危, 🟡 $medium_count 中低危"
  echo
  echo "💡 建议:"
  echo "  1. 高危项 → 改为 #{param} + CONCAT('%', ..., '%') 或 Java 层拼接 %"
  echo "  2. 中低危项 → 确认是否白名单校验（如排序字段）"
  echo "  3. 绝对禁止用户输入直接进入 \${}！"
fi