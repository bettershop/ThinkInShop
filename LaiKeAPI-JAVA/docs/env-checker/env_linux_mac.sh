#!/bin/bash
# env_linux_mac.sh - Compatible with Bash 3.x (e.g., macOS default)

# Colors (safe for old bash)
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Detect OS
OS=$(uname -s)
case "$OS" in
  Linux*)   PLATFORM="Linux" ;;
  Darwin*)  PLATFORM="macOS" ;;
  *)        echo "❌ Unsupported OS: $OS";   exit 1 ;;
esac

# We'll store results in parallel arrays
SERVICES=("MySQL" "Redis" "PHP" "Java" "Node" "Nacos")
VERSIONS=("" "" "" "" "" "")
REQUIREMENTS=("8.0.29" "6+" "8.0.1x" "1.8" "14.20" "2.1.1")

# Helper: Set version for a service
set_version() {
  local service="$1"
  local ver="$2"
  case "$service" in
    "MySQL")   VERSIONS[0]="$ver" ;;
    "Redis")   VERSIONS[1]="$ver" ;;
    "PHP")     VERSIONS[2]="$ver" ;;
    "Java")    VERSIONS[3]="$ver" ;;
    "Node")    VERSIONS[4]="$ver" ;;
    "Nacos")   VERSIONS[5]="$ver" ;;
  esac
}

# Helper: Get version from command
get_cmd_version() {
  local cmd="$1"
  local bin="${cmd%% *}"
  if command -v "$bin" >/dev/null 2>&1; then
    local ver
    ver=$(eval "$cmd" 2>&1 | grep -Eo '[0-9]+\.[0-9]+\.?[0-9]*' | head -1)
    if [ -n "$ver" ]; then
      echo "$ver"
    else
      eval "$cmd" 2>&1 | head -1 | cut -c1-50
    fi
  else
    echo "Not found"
  fi
}

# Helper: Get jar version
get_jar_version() {
  local pattern="$1"
  if pgrep -f "$pattern" >/dev/null 2>&1; then
    ver=$(ps aux 2>/dev/null | grep -v grep | grep -o "${pattern}-[0-9.]*\.jar" | head -1 | sed -E "s/${pattern}-([0-9.]+)\.jar/\1/" 2>/dev/null)
    echo "${ver:-Running (unknown)}"
  else
    echo "Not running"
  fi
}

# --- Collect versions ---
set_version "Redis" "$(get_cmd_version "redis-server --version")"
set_version "PHP" "$(get_cmd_version "php --version")"
set_version "Java" "$(get_cmd_version "java -version")"
set_version "Node" "$(get_cmd_version "node --version")"
set_version "Nacos" "$(get_jar_version "nacos-server")"

# --- MySQL: Enhanced detection ---
echo "🔍 Detecting MySQL version..."

if command -v mysql >/dev/null 2>&1; then
  MYSQL_VER=$(mysql --version 2>/dev/null | grep -Eo '[0-9]+\.[0-9]+\.?[0-9]*' | head -1)
  if [ -n "$MYSQL_VER" ]; then
    set_version "MySQL" "$MYSQL_VER"
  else
    set_version "MySQL" "$(mysql --version 2>/dev/null | head -1 | cut -c1-50)"
  fi
else
  set_version "MySQL" "Client missing"
fi

# Try mysqld process
if [ "${VERSIONS[0]}" = "Client missing" ] && pgrep -f mysqld >/dev/null 2>&1; then
  MYSQLD_PATH=$(pgrep -a mysqld | head -1 | awk '{print $NF}' | grep -E '/.*mysqld$')
  if [ -n "$MYSQLD_PATH" ] && [ -x "$MYSQLD_PATH" ]; then
    VER=$("$MYSQLD_PATH" --version 2>/dev/null | grep -Eo '[0-9]+\.[0-9]+\.?[0-9]*' | head -1)
    if [ -n "$VER" ]; then
      set_version "MySQL" "(from mysqld) $VER"
    else
      set_version "MySQL" "mysqld running (unknown ver)"
    fi
  else
    set_version "MySQL" "mysqld running (no client)"
  fi
fi

# --- Docker fallback ---
if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
  # MySQL
  CID=$(docker ps --format '{{.Names}}' | grep -i -E 'mysql|mysqld' | head -1)
  if [ -n "$CID" ]; then
    VER=$(docker exec "$CID" mysql --version 2>/dev/null | grep -Eo '[0-9]+\.[0-9]+\.?[0-9]*' | head -1)
    if [ -n "$VER" ]; then
      set_version "MySQL" "(Docker) $VER"
    else
      set_version "MySQL" "(Docker) Running"
    fi
  fi

  # Redis
  CID=$(docker ps --format '{{.Names}}' | grep -i redis | head -1)
  if [ -n "$CID" ] && [ "${VERSIONS[1]}" = "Not found" ]; then
    VER=$(docker exec "$CID" redis-server --version 2>/dev/null | grep -Eo 'v=[0-9.]*' | cut -d= -f2 | head -1)
    set_version "Redis" "${VER:-(Docker) Running}"
  fi

  # Nacos
  CID=$(docker ps --format '{{.Names}}' | grep -i nacos | head -1)
  if [ -n "$CID" ] && [ "${VERSIONS[5]}" = "Not running" ]; then
    VER=$(docker exec "$CID" ps aux 2>/dev/null | grep -o 'nacos-server-[0-9.]*\.jar' | head -1 | sed -E 's/nacos-server-([0-9.]+)\.jar/\1/' 2>/dev/null)
    set_version "Nacos" "${VER:-(Docker) Running}"
  fi

fi

# --- Output Table ---
printf "\n${GREEN}✅ Platform: %s${NC}\n" "$PLATFORM"
printf "${GREEN}%s${NC}\n" "========================================"
printf "${YELLOW}%-10s | %-30s | %-18s${NC}\n" "服务" "当前机器版本" "要求版本"
printf "%-10s-+-%-30s-+-%-18s\n" "----------" "------------------------------" "------------------"

i=0
while [ $i -lt ${#SERVICES[@]} ]; do
  svc="${SERVICES[$i]}"
  ver="${VERSIONS[$i]}"
  req="${REQUIREMENTS[$i]}"
  if [ ${#ver} -gt 30 ]; then
    ver="${ver:0:27}..."
  fi
  printf "%-10s | %-30s | %-18s\n" "$svc" "$ver" "$req"
  i=$((i+1))
done

printf "\n💡 Tip: Compare current versions with 要求版本s before deployment.\n"
