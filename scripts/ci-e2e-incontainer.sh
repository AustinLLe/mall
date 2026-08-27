#!/usr/bin/env bash
set -euo pipefail

cd /work
if [ -f tests/e2e/.ci-secrets ]; then
  set -a
  # shellcheck disable=SC1091
  . tests/e2e/.ci-secrets
  set +a
fi

e2e_status=1
archive_e2e_outputs() {
  mkdir -p e2e-tests/target/e2e-artifacts e2e-tests/target/surefire-reports
  tar -czf e2e-surefire-reports.tgz -C e2e-tests/target surefire-reports || true
  tar -czf e2e-artifacts.tgz -C e2e-tests/target e2e-artifacts || true
  printf '%s\n' "${e2e_status}" > e2e-exit-code.txt
}
trap archive_e2e_outputs EXIT
archive_e2e_outputs

export DB_PASSWORD="${CI_DB_PASSWORD:?CI_DB_PASSWORD is required}"
export MYSQL_ROOT_PASSWORD="${CI_MYSQL_ROOT_PASSWORD:?CI_MYSQL_ROOT_PASSWORD is required}"
export DB_USERNAME="${DB_USERNAME:-shop_user}"
export PUBLIC_ORIGIN="${PUBLIC_ORIGIN:-http://127.0.0.1:18080}"
export DB_URL="${DB_URL:-jdbc:mysql://127.0.0.1:3306/shop_db?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false}"
export APP_CORS_PATTERNS="${APP_CORS_PATTERNS:-http://127.0.0.1:*,http://localhost:*}"
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:--XX:MaxRAMPercentage=50.0}"
node_home="/tmp/ci-node"

mkdir -p /root/.m2
cat > /root/.m2/settings.xml <<'EOF'
<settings>
  <mirrors>
    <mirror>
      <id>huawei</id>
      <mirrorOf>*</mirrorOf>
      <url>https://repo.huaweicloud.com/repository/maven/</url>
    </mirror>
  </mirrors>
</settings>
EOF

echo "===== 启动 MariaDB ====="
mkdir -p /run/mysqld
chown mysql:mysql /run/mysqld
mysqld --user=mysql --bind-address=127.0.0.1 --port=3306 &
ok=0
for _ in $(seq 1 60); do
  if mysqladmin --protocol=SOCKET ping >/dev/null 2>&1 || mysql -uroot -e "SELECT 1" >/dev/null 2>&1; then
    ok=1
    break
  fi
  sleep 2
done
if [ "$ok" -ne 1 ]; then
  echo "MariaDB 未启动"
  exit 1
fi
mysql -uroot <<SQL
CREATE DATABASE IF NOT EXISTS shop_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER IF NOT EXISTS '${DB_USERNAME}'@'127.0.0.1' IDENTIFIED BY '${DB_PASSWORD}';
CREATE USER IF NOT EXISTS '${DB_USERNAME}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON shop_db.* TO '${DB_USERNAME}'@'127.0.0.1';
GRANT ALL PRIVILEGES ON shop_db.* TO '${DB_USERNAME}'@'localhost';
FLUSH PRIVILEGES;
SQL
mysql -uroot shop_db < shopping_back/shopping_back/doc/db.sql

echo "===== 安装 Node.js ====="
if ! command -v npm >/dev/null 2>&1; then
  node_ver="${NODE_VERSION:-v18.20.8}"
  tarball="node-${node_ver}-linux-x64.tar.gz"
  curl -fsSL "https://mirrors.huaweicloud.com/nodejs/${node_ver}/${tarball}" -o "/tmp/${tarball}"
  mkdir -p "$node_home"
  tar -xzf "/tmp/${tarball}" -C "$node_home" --strip-components=1
  export PATH="${node_home}/bin:${PATH}"
fi

echo "===== 构建前端 ====="
(
  cd shopping_front
  npm config set registry https://repo.huaweicloud.com/repository/npm/
  npm ci
  npm run build:h5
)
mkdir -p /usr/share/nginx/html
cp -a shopping_front/dist/build/h5/. /usr/share/nginx/html/
if [ -d shopping_front/static ]; then
  cp -a shopping_front/static/. /usr/share/nginx/html/static/ 2>/dev/null || true
fi
rm -f /etc/nginx/sites-enabled/default
cp tests/e2e/nginx.ci.conf /etc/nginx/conf.d/e2e.conf
nginx -t
nginx

echo "===== 构建并启动后端 ====="
(
  cd shopping_back/shopping_back
  mvn -B -ntp -DskipTests package
)
jar="$(ls -1 shopping_back/shopping_back/target/shopping_back-*.jar | head -n 1)"
java -jar "$jar" &

echo "等待 http://127.0.0.1:18080/api/products"
ok=0
for _ in $(seq 1 60); do
  if curl -fsS --max-time 5 "http://127.0.0.1:18080/api/products" >/dev/null 2>&1; then
    ok=1
    break
  fi
  sleep 3
done
if [ "$ok" -ne 1 ]; then
  echo "前端/后端未就绪"
  curl -sv "http://127.0.0.1:8080/api/products" || true
  curl -sv "http://127.0.0.1:18080/" || true
  exit 1
fi

chrome_bin=""
for candidate in /usr/bin/chromium /usr/bin/chromium-browser /usr/bin/google-chrome; do
  if [ -x "$candidate" ]; then
    chrome_bin="$candidate"
    break
  fi
done
if [ -z "$chrome_bin" ]; then
  echo "找不到 Chrome/Chromium"
  exit 1
fi
echo "使用浏览器: ${chrome_bin}"

echo "===== 运行 E2E ====="
set +e
mvn -B -ntp -f e2e-tests/pom.xml test \
  -De2e.browser=chrome \
  -De2e.headless=true \
  -De2e.baseUrl=http://127.0.0.1:18080 \
  -De2e.chromeBinary="${chrome_bin}" \
  -De2e.timeoutSeconds=20
e2e_status=$?
set -e
exit "${e2e_status}"
