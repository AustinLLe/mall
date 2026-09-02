#!/usr/bin/env bash
set -euo pipefail

mkdir -p e2e-tests/target/surefire-reports e2e-tests/target/e2e-artifacts \
  tests/api/reports/microservices tests/e2e/results

write_empty_artifacts() {
  tar -czf e2e-surefire-reports.tgz -C e2e-tests/target surefire-reports
  tar -czf e2e-artifacts.tgz -C e2e-tests/target e2e-artifacts
  tar -czf e2e-api-reports.tgz -C tests/api/reports microservices
  tar -czf e2e-run-results.tgz -C tests/e2e results
}

if [ ! -f e2e-image.tar ]; then
  echo "没有 e2e-image.tar，docker build 可能失败"
  printf '1\n' > e2e-exit-code.txt
  write_empty_artifacts
  exit 0
fi

tmp="$(mktemp -d)"
mkdir -p "${tmp}/out"
tar -xf e2e-image.tar -C "$tmp"
found=0
while IFS= read -r layer; do
  tar -xf "$layer" -C "${tmp}/out" || true
  if [ -f "${tmp}/out/e2e-exit-code.txt" ]; then
    found=1
  fi
done < <(find "$tmp" -name 'layer.tar' -print)

if [ "$found" -ne 1 ]; then
  echo "镜像里没有 E2E 产物"
  printf '1\n' > e2e-exit-code.txt
  write_empty_artifacts
  exit 0
fi

cp -f "${tmp}/out/e2e-exit-code.txt" e2e-exit-code.txt
for artifact in e2e-surefire-reports.tgz e2e-artifacts.tgz e2e-api-reports.tgz e2e-run-results.tgz; do
  if [ -f "${tmp}/out/${artifact}" ]; then
    cp -f "${tmp}/out/${artifact}" "${artifact}"
  fi
done
echo "已从 docker save 镜像取出 E2E 产物"
cat e2e-exit-code.txt
