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
ci_release_env
meta="ci-artifacts/release/release-metadata.json"
grep -q "newTag: ${IMAGE_TAG}" ci-artifacts/release/k8s/kustomization.yaml
grep -q "shop-backend:${IMAGE_TAG}" "$meta"
grep -q "shop-frontend:${IMAGE_TAG}" "$meta"
echo "publish-gate ok: ${IMAGE_TAG}"
