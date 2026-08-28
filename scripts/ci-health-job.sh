#!/usr/bin/env bash
set -euo pipefail

write_status() {
  mkdir -p ci-artifacts
  printf '%s\n' "$1" > ci-artifacts/health-status
}

script="$(find . /data /var /workspace /tmp -path '*/scripts/ci-cd-common.sh' 2>/dev/null | head -n 1)"
if [[ -z "${script:-}" ]]; then
  echo "找不到仓库根目录" >&2
  write_status 1
  exit 0
fi
# shellcheck disable=SC1090
source "$script"
ci_enter_repo
ci_release_env

if ! command -v ssh >/dev/null 2>&1 || ! command -v scp >/dev/null 2>&1; then
  echo "当前执行器没有 ssh/scp" >&2
  write_status 2
  exit 0
fi

mkdir -p ci-artifacts
set +e
bash scripts/ci-k8s-health.sh 2>&1 | tee ci-artifacts/kubernetes-health.log
status=${PIPESTATUS[0]}
set -e
write_status "$status"
exit 0
