# Shared helpers for CodeArts publish / deploy / health scripts.
# Source this file; do not execute it.

ci_enter_repo() {
  local here
  here="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
  cd "$here"
}

ci_release_env() {
  : "${PIPELINE_NUMBER:?PIPELINE_NUMBER is required}"
  : "${COMMIT_ID:?COMMIT_ID is required}"
  COMMIT_ID_SHORT="${COMMIT_ID_SHORT:-$(printf '%s' "$COMMIT_ID" | cut -c1-8)}"
  export COMMIT_ID_SHORT
  IMAGE_TAG="${IMAGE_TAG:-release-${PIPELINE_NUMBER}-${COMMIT_ID_SHORT}}"
  export IMAGE_TAG
  SOURCE_BRANCH="${SOURCE_BRANCH:-${codeBranch:-feature/lqy-first-stage}}"
  export SOURCE_BRANCH
  CI_ARTIFACT_DIR="${CI_ARTIFACT_DIR:-$PWD/ci-artifacts}"
  export CI_ARTIFACT_DIR
  DOCKER_HUB="${DOCKER_HUB:-swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io}"
  export DOCKER_HUB
  SWR_REGISTRY="${SWR_REGISTRY:-swr.cn-north-4.myhuaweicloud.com}"
  export SWR_REGISTRY
  SWR_ORGANIZATION="${SWR_ORGANIZATION:-songguo}"
  export SWR_ORGANIZATION
  mkdir -p "$CI_ARTIFACT_DIR"
}
