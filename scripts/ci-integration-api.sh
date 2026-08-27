#!/usr/bin/env bash
set -euo pipefail

if [ ! -f shopping_back/shopping_back/pom.xml ]; then
  pom="$(find . /data /var /workspace /tmp -path '*/shopping_back/shopping_back/pom.xml' 2>/dev/null | head -n 1)"
  if [ -z "${pom:-}" ]; then
    echo "找不到仓库根目录"
    exit 1
  fi
  cd "$(dirname "$(dirname "$(dirname "$pom")")")"
fi

export DB_PASSWORD="${CI_DB_PASSWORD:?CI_DB_PASSWORD is required}"
export MYSQL_ROOT_PASSWORD="${CI_MYSQL_ROOT_PASSWORD:?CI_MYSQL_ROOT_PASSWORD is required}"
export DB_USERNAME="${DB_USERNAME:-shop_user}"
export DB_URL="${DB_URL:-jdbc:mysql://127.0.0.1:3306/shop_db?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false}"
export APP_CORS_PATTERNS="${APP_CORS_PATTERNS:-http://127.0.0.1:*,http://localhost:*}"
backend_port="${BACKEND_PORT:-8080}"
base_url="http://127.0.0.1:${backend_port}"
mysql_sock="/tmp/ci-mysql.sock"
mysql_data="/tmp/ci-mysql-data"
node_home="/tmp/ci-node"

mysql_cmd() {
  mysql --socket="$mysql_sock" -uroot --protocol=SOCKET "$@"
}

install_mysql() {
  if command -v mysqld >/dev/null 2>&1 && command -v mysql >/dev/null 2>&1; then
    return 0
  fi
  echo "安装 MariaDB（不走 Docker Hub）"
  if command -v apt-get >/dev/null 2>&1; then
    export DEBIAN_FRONTEND=noninteractive
    apt-get update -y
    apt-get install -y mariadb-server mariadb-client curl ca-certificates
  elif command -v yum >/dev/null 2>&1; then
    yum install -y mariadb-server mariadb curl
  elif command -v dnf >/dev/null 2>&1; then
    dnf install -y mariadb-server mariadb curl
  elif command -v microdnf >/dev/null 2>&1; then
    microdnf install -y mariadb-server mariadb curl
  else
    echo "当前构建镜像没有包管理器，无法安装 MySQL。"
    echo "也不要改用 docker20.10：华为云执行机访问不了 registry-1.docker.io。"
    exit 1
  fi
}

start_mysql() {
  mkdir -p "$mysql_data"
  if [ ! -d "$mysql_data/mysql" ]; then
    if command -v mysql_install_db >/dev/null 2>&1; then
      mysql_install_db --datadir="$mysql_data" --user="$(id -un)" --auth-root-authentication-method=normal
    elif command -v mariadb-install-db >/dev/null 2>&1; then
      mariadb-install-db --datadir="$mysql_data" --user="$(id -un)" --auth-root-authentication-method=normal
    else
      mysqld --initialize-insecure --datadir="$mysql_data" --user="$(id -un)"
    fi
  fi
  mysqld \
    --datadir="$mysql_data" \
    --socket="$mysql_sock" \
    --pid-file=/tmp/ci-mysql.pid \
    --port=3306 \
    --bind-address=127.0.0.1 \
    --skip-networking=0 &
  echo "等待 MySQL 就绪"
  ok=0
  for _ in $(seq 1 60); do
    if mysql_cmd -e "SELECT 1" >/dev/null 2>&1; then
      ok=1
      break
    fi
    sleep 2
  done
  if [ "$ok" -ne 1 ]; then
    echo "MySQL 未启动"
    exit 1
  fi
  mysql_cmd <<SQL
CREATE DATABASE IF NOT EXISTS shop_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER IF NOT EXISTS '${DB_USERNAME}'@'127.0.0.1' IDENTIFIED BY '${DB_PASSWORD}';
CREATE USER IF NOT EXISTS '${DB_USERNAME}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON shop_db.* TO '${DB_USERNAME}'@'127.0.0.1';
GRANT ALL PRIVILEGES ON shop_db.* TO '${DB_USERNAME}'@'localhost';
FLUSH PRIVILEGES;
SQL
  mysql_cmd < shopping_back/shopping_back/doc/db.sql
}

install_node() {
  if command -v npm >/dev/null 2>&1; then
    return 0
  fi
  echo "安装 Node.js（华为云镜像）"
  node_ver="${NODE_VERSION:-v18.20.8}"
  tarball="node-${node_ver}-linux-x64.tar.gz"
  url="https://mirrors.huaweicloud.com/nodejs/${node_ver}/${tarball}"
  mkdir -p "$node_home"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$url" -o "/tmp/${tarball}"
  else
    wget -q "$url" -O "/tmp/${tarball}"
  fi
  tar -xzf "/tmp/${tarball}" -C "$node_home" --strip-components=1
  export PATH="${node_home}/bin:${PATH}"
}

echo "===== 1/3 启动测试环境 ====="
install_mysql
start_mysql

cd shopping_back/shopping_back
mvn -B -ntp -DskipTests package
jar="$(ls -1 target/shopping_back-*.jar | head -n 1)"
java -jar "$jar" &
backend_pid=$!
cd - >/dev/null

cleanup() {
  kill "$backend_pid" >/dev/null 2>&1 || true
  if [ -f /tmp/ci-mysql.pid ]; then
    kill "$(cat /tmp/ci-mysql.pid)" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

echo "等待 ${base_url}/api/products"
ok=0
for _ in $(seq 1 60); do
  if curl -fsS --max-time 5 "${base_url}/api/products" >/dev/null 2>&1; then
    ok=1
    break
  fi
  sleep 3
done
if [ "$ok" -ne 1 ]; then
  echo "后端未在时限内就绪"
  exit 1
fi

echo "===== 2/3 集成测试 ====="
SMOKE_SKIP_FRONTEND=true bash tests/blackbox/smoke.sh "${base_url}"

echo "===== 3/3 接口测试 ====="
mysql --socket="$mysql_sock" -u"$DB_USERNAME" -p"$DB_PASSWORD" --protocol=SOCKET shop_db \
  < tests/api/fixtures/setup.sql
install_node
if [ -d "${node_home}/bin" ]; then
  export PATH="${node_home}/bin:${PATH}"
fi
npm config set registry https://repo.huaweicloud.com/repository/npm/
npm ci
SKIP_API_FIXTURE_SETUP=true API_BASE_URL="${base_url}" npm run test:api

echo "集成测试和接口测试通过"
