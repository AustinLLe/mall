#!/usr/bin/env bash
set -euo pipefail

mkdir -p e2e-tests/target/surefire-reports e2e-tests/target/e2e-artifacts

if [ ! -f e2e-image.tar ]; then
  echo "没有 e2e-image.tar，docker build 可能失败"
  printf '1\n' > e2e-exit-code.txt
  tar -czf e2e-surefire-reports.tgz -C e2e-tests/target surefire-reports
  tar -czf e2e-artifacts.tgz -C e2e-tests/target e2e-artifacts
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
  tar -czf e2e-surefire-reports.tgz -C e2e-tests/target surefire-reports
  tar -czf e2e-artifacts.tgz -C e2e-tests/target e2e-artifacts
  exit 0
fi

cp -f "${tmp}/out/e2e-exit-code.txt" e2e-exit-code.txt
cp -f "${tmp}/out/e2e-surefire-reports.tgz" e2e-surefire-reports.tgz
cp -f "${tmp}/out/e2e-artifacts.tgz" e2e-artifacts.tgz
echo "已从 docker save 镜像取出 E2E 产物"
cat e2e-exit-code.txt
