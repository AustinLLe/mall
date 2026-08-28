#!/usr/bin/env bash
set -euo pipefail

script="$(find . /data /var /workspace /tmp -path '*/scripts/ci-cd-common.sh' 2>/dev/null | head -n 1)"
if [[ -z "${script:-}" ]]; then
  echo "找不到仓库根目录" >&2
  exit 1
fi
# shellcheck disable=SC1090
source "$script"
ci_enter_repo
status="$(tr -d '[:space:]' < ci-artifacts/deploy-status)"
echo "deploy status: ${status}"
exit "${status}"
