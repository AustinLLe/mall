#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
image_tag="${IMAGE_TAG:-ci-$(git -C "$root_dir" rev-parse --short HEAD)}"
export DB_PASSWORD="${DB_PASSWORD:?DB_PASSWORD is required}"
export MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD is required}"
export PUBLIC_ORIGIN="${PUBLIC_ORIGIN:-http://localhost}"
export HTTP_PORT="${HTTP_PORT:-8088}"
export IMAGE_TAG="$image_tag"

java_version="$(java -version 2>&1 | awk -F '"' '/version/ {print $2; exit}')"
java_major="${java_version%%.*}"
if [[ "$java_major" != "17" ]]; then
  echo "ERROR: Java 17 is required, current java version is: ${java_version:-unknown}" >&2
  echo "Please select JDK 17 in the CodeArts build environment before running this script." >&2
  exit 1
fi

cd "$root_dir/shopping_front"
npm ci
npm run build:h5

cd "$root_dir/shopping_back/shopping_back"
./mvnw test
./mvnw package -DskipTests

cd "$root_dir"
docker compose -f deploy/docker-compose.yml up -d --build
trap 'docker compose -f deploy/docker-compose.yml down' EXIT
docker compose -f deploy/docker-compose.yml ps
tests/blackbox/smoke.sh "http://127.0.0.1:${HTTP_PORT}"

if [[ "${RUN_E2E:-false}" == "true" ]]; then
  cd "$root_dir/e2e-tests"
  "$root_dir/shopping_back/shopping_back/mvnw" -f pom.xml test \
    -De2e.baseUrl="http://127.0.0.1:${HTTP_PORT}" \
    -De2e.headless=true
fi
