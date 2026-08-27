#!/usr/bin/env bash
set -euo pipefail

if [ ! -f shopping_back/shopping_back/pom.xml ]; then
  pom="$(find . /data /var /workspace /tmp -path '*/shopping_back/shopping_back/pom.xml' 2>/dev/null | head -n 1)"
  [ -n "${pom:-}" ] || { echo "找不到仓库根目录"; exit 1; }
  cd "$(dirname "$(dirname "$(dirname "$pom")")")"
fi

export DB_PASSWORD="${CI_DB_PASSWORD:?CI_DB_PASSWORD is required}"
export MYSQL_ROOT_PASSWORD="${CI_MYSQL_ROOT_PASSWORD:?CI_MYSQL_ROOT_PASSWORD is required}"
export DB_USERNAME="${DB_USERNAME:-shop_user}"
export PUBLIC_ORIGIN="${PUBLIC_ORIGIN:-http://127.0.0.1:18080}"
export HTTP_PORT="${E2E_HTTP_PORT:-18080}"
export IMAGE_TAG="${IMAGE_TAG:-ci-e2e}"
export E2E_NETWORK_NAME="${E2E_NETWORK_NAME:-soft-shop-e2e-net}"
export COMPOSE_PROJECT_NAME="${E2E_PROJECT_NAME:-soft-shop-e2e}"
export DOCKER_BUILDKIT="${DOCKER_BUILDKIT:-1}"

e2e_status=1

cat > deploy/.env <<EOF
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
DB_USERNAME=${DB_USERNAME}
DB_PASSWORD=${DB_PASSWORD}
PUBLIC_ORIGIN=${PUBLIC_ORIGIN}
HTTP_PORT=${HTTP_PORT}
IMAGE_TAG=${IMAGE_TAG}
SELENIUM_IMAGE=${SELENIUM_IMAGE:-selenium/standalone-chrome:4.35.0}
EOF

compose=(docker compose --project-name "$COMPOSE_PROJECT_NAME" --env-file deploy/.env -f deploy/docker-compose.yml -f tests/e2e/docker-compose.e2e.yml)
if ! docker compose version >/dev/null 2>&1; then
  compose=(docker-compose --project-name "$COMPOSE_PROJECT_NAME" --env-file deploy/.env -f deploy/docker-compose.yml -f tests/e2e/docker-compose.e2e.yml)
fi

mkdir -p e2e-tests/target/e2e-artifacts e2e-tests/target/surefire-reports

archive_e2e_outputs() {
  mkdir -p e2e-tests/target/e2e-artifacts e2e-tests/target/surefire-reports
  tar -czf e2e-surefire-reports.tgz -C e2e-tests/target surefire-reports || true
  tar -czf e2e-artifacts.tgz -C e2e-tests/target e2e-artifacts || true
  printf '%s\n' "${e2e_status}" > e2e-exit-code.txt
}

cleanup() {
  "${compose[@]}" logs --no-color > e2e-tests/target/e2e-artifacts/compose.log 2>&1 || true
  archive_e2e_outputs
  "${compose[@]}" down -v --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT

"${compose[@]}" up -d --build
frontend_ok=0
selenium_ok=0
for _ in $(seq 1 72); do
  frontend_ok=0
  selenium_ok=0
  curl -fsS --max-time 5 "http://127.0.0.1:${HTTP_PORT}/api/products" >/dev/null 2>&1 && frontend_ok=1
  "${compose[@]}" exec -T selenium curl -fsS http://127.0.0.1:4444/status 2>/dev/null | grep -q '"ready": true' && selenium_ok=1
  [ "$frontend_ok" -eq 1 ] && [ "$selenium_ok" -eq 1 ] && break
  sleep 5
done
if [ "${frontend_ok:-0}" -ne 1 ] || [ "${selenium_ok:-0}" -ne 1 ]; then
  echo "E2E 环境未就绪"
  e2e_status=1
  exit 1
fi

set +e
docker run --rm \
  --network "$E2E_NETWORK_NAME" \
  -v "$(pwd):/work" \
  -v soft-shop-e2e-m2-cache:/root/.m2 \
  -w /work/e2e-tests \
  -e LANG=C.UTF-8 \
  -e JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 \
  "${E2E_MAVEN_IMAGE:-maven:3.9.11-eclipse-temurin-17}" \
  mvn -B -ntp clean test \
  -De2e.remoteUrl=http://selenium:4444/wd/hub \
  -De2e.baseUrl=http://frontend \
  -De2e.apiUrl=http://frontend \
  -De2e.headless=true \
  -De2e.timeoutSeconds=20
e2e_status=$?
set -e
exit "${e2e_status}"
