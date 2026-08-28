#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# shellcheck source=ci-cd-common.sh
source "$repo_root/scripts/ci-cd-common.sh"
ci_enter_repo
ci_release_env
meta="ci-artifacts/release/release-metadata.json"
grep -q "newTag: ${IMAGE_TAG}" ci-artifacts/release/k8s/kustomization.yaml
grep -q "shop-backend:${IMAGE_TAG}" "$meta"
grep -q "shop-frontend:${IMAGE_TAG}" "$meta"
echo "publish-gate ok: ${IMAGE_TAG}"
