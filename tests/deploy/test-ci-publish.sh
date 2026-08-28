#!/usr/bin/env bash
set -Eeuo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
test_root="$(mktemp -d)"
trap 'rm -rf "$test_root"' EXIT
mkdir -p "$test_root/bin" "$test_root/artifacts"

cat > "$test_root/bin/docker" <<'EOF'
#!/usr/bin/env bash
set -e
if [[ "$1" == "login" ]]; then exit 0; fi
if [[ "$1 $2" == "manifest inspect" ]]; then exit 1; fi
if [[ "$1 $2" == "buildx build" ]]; then
  while [[ $# -gt 0 ]]; do
    if [[ "$1" == "--metadata-file" ]]; then
      printf '%s\n' '{"containerimage.digest":"sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"}' > "$2"
      exit 0
    fi
    shift
  done
fi
if [[ "$1 $2 $3" == "buildx imagetools inspect" ]]; then
  printf '%s\n' '"sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"'
  exit 0
fi
echo "Unexpected fake docker call: $*" >&2
exit 90
EOF
chmod +x "$test_root/bin/docker"

export PATH="$test_root/bin:$PATH"
export IMAGE_TAG="release-27-abc12345"
export PIPELINE_NUMBER="27"
export COMMIT_ID="abc1234567890def"
export COMMIT_ID_SHORT="abc12345"
export SOURCE_BRANCH="master"
export CI_ARTIFACT_DIR="$test_root/artifacts"
export SWR_USERNAME="test-user"
export SWR_PASSWORD="test-password"

bash "$repo_root/scripts/ci-publish-images.sh"

kustomization="$test_root/artifacts/release/k8s/kustomization.yaml"
[[ "$(grep -c 'newTag: release-27-abc12345' "$kustomization")" -eq 2 ]]
grep -q 'songguo.dev/image-tag: "release-27-abc12345"' "$kustomization"
grep -q 'songguo.dev/commit-id: "abc1234567890def"' "$kustomization"
grep -q 'songguo.dev/pipeline-number: "27"' "$kustomization"
grep -q '"imageTag": "release-27-abc12345"' "$test_root/artifacts/release/release-metadata.json"
[[ -f "$test_root/artifacts/release-release-27-abc12345.tgz" ]]

export IMAGE_TAG="wrong-tag"
if bash "$repo_root/scripts/ci-publish-images.sh" >/dev/null 2>&1; then
  echo "Invalid tag unexpectedly passed validation" >&2
  exit 1
fi

echo "CI publish contract tests: OK"
