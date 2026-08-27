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
export HTTP_PORT="${HTTP_PORT:-8088}"
export PUBLIC_ORIGIN="${PUBLIC_ORIGIN:-http://127.0.0.1:${HTTP_PORT}}"
export IMAGE_TAG="${IMAGE_TAG:-ci-integration}"
export DOCKER_BUILDKIT=1

compose=(docker compose --env-file deploy/.env -f deploy/docker-compose.yml)
if ! docker compose version >/dev/null 2>&1; then
  compose=(docker-compose --env-file deploy/.env -f deploy/docker-compose.yml)
fi

cat > deploy/.env <<EOF
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
DB_USERNAME=${DB_USERNAME}
DB_PASSWORD=${DB_PASSWORD}
PUBLIC_ORIGIN=${PUBLIC_ORIGIN}
HTTP_PORT=${HTTP_PORT}
IMAGE_TAG=${IMAGE_TAG}
EOF

cleanup() {
  "${compose[@]}" down --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT

echo "===== 1/3 启动测试环境 ====="
"${compose[@]}" up -d --build

base_url="http://127.0.0.1:${HTTP_PORT}"
echo "等待 ${base_url}/api/products"
ok=0
for _ in $(seq 1 72); do
  if curl -fsS --max-time 5 "${base_url}/api/products" >/dev/null 2>&1; then
    ok=1
    break
  fi
  sleep 5
done
if [ "$ok" -ne 1 ]; then
  echo "测试环境未在时限内就绪"
  "${compose[@]}" ps || true
  "${compose[@]}" logs --tail=80 || true
  exit 1
fi

echo "===== 2/3 集成测试 ====="
bash tests/blackbox/smoke.sh "${base_url}"

echo "===== 3/3 接口测试 ====="
"${compose[@]}" exec -T mysql sh -lc \
  'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"' \
  < tests/api/fixtures/setup.sql

npm_image="${NPM_IMAGE:-node:18-bookworm}"
docker run --rm --network host \
  -v "$(pwd)":/work -w /work \
  -e SKIP_API_FIXTURE_SETUP=true \
  -e API_BASE_URL="${base_url}" \
  "${npm_image}" \
  bash -lc 'npm config set registry https://repo.huaweicloud.com/repository/npm/ && npm ci && npm run test:api'

echo "集成测试和接口测试通过"
