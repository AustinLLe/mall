#!/usr/bin/env bash
set -Eeuo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

registry="${SWR_REGISTRY:-swr.cn-north-4.myhuaweicloud.com}"
organization="${SWR_ORGANIZATION:-songguo}"
image_tag="${IMAGE_TAG:?IMAGE_TAG is required}"
pipeline_number="${PIPELINE_NUMBER:?PIPELINE_NUMBER is required}"
commit_id="${COMMIT_ID:?COMMIT_ID is required}"
commit_short="${COMMIT_ID_SHORT:?COMMIT_ID_SHORT is required}"
source_branch="${SOURCE_BRANCH:-master}"
artifact_root="${CI_ARTIFACT_DIR:-$repo_root/ci-artifacts}"
release_dir="$artifact_root/release"
catalog="$repo_root/ops/services.conf"
swr_username="${SWR_USERNAME:?SWR_USERNAME is required}"
swr_password="${SWR_PASSWORD:?SWR_PASSWORD is required}"

expected_tag="release-${pipeline_number}-${commit_short}"
if [[ "$image_tag" != "$expected_tag" ]]; then
  echo "IMAGE_TAG must be exactly $expected_tag" >&2
  exit 2
fi
if [[ ! "$image_tag" =~ ^[a-z0-9][a-z0-9._-]{0,127}$ ]] || [[ "$image_tag" == "latest" ]]; then
  echo "Invalid immutable image tag: $image_tag" >&2
  exit 2
fi
[[ -f "$catalog" ]] || { echo "Missing service catalog: $catalog" >&2; exit 2; }

mkdir -p "$release_dir/image-metadata" "$release_dir/k8s"
rm -rf "$release_dir/k8s"
cp -R "$repo_root/k8s" "$release_dir/k8s"

printf '%s' "$swr_password" | docker login "$registry" --username "$swr_username" --password-stdin

images_json=""
while IFS='|' read -r service dockerfile image_name deployment container; do
  [[ -z "$service" || "$service" == \#* ]] && continue
  full_name="${registry}/${organization}/${image_name}"
  image_ref="${full_name}:${image_tag}"
  if docker manifest inspect "$image_ref" >/dev/null 2>&1; then
    echo "Refusing to overwrite existing image: $image_ref" >&2
    exit 3
  fi

  metadata_file="$release_dir/image-metadata/${service}.json"
  docker buildx build \
    --provenance=false \
    --sbom=false \
    --platform linux/amd64 \
    --file "$dockerfile" \
    --tag "$image_ref" \
    --metadata-file "$metadata_file" \
    --push \
    .

  digest="$(docker buildx imagetools inspect "$image_ref" --format '{{json .Manifest.Digest}}' | tr -d '"')"
  [[ -n "$digest" && "$digest" != "null" ]] || { echo "Cannot resolve digest for $image_ref" >&2; exit 4; }
  item="{\"service\":\"$service\",\"image\":\"$image_ref\",\"digest\":\"$digest\",\"deployment\":\"$deployment\",\"container\":\"$container\"}"
  images_json="${images_json}${images_json:+,}${item}"
done < "$catalog"

# Both image entries intentionally use the same immutable release tag.
sed -i -E "s#(^[[:space:]]*newTag:)[[:space:]].*#\\1 ${image_tag}#" "$release_dir/k8s/kustomization.yaml"
sed -i -E \
  -e "s#(^[[:space:]]*songguo.dev/image-tag:)[[:space:]].*#\\1 \"${image_tag}\"#" \
  -e "s#(^[[:space:]]*songguo.dev/commit-id:)[[:space:]].*#\\1 \"${commit_id}\"#" \
  -e "s#(^[[:space:]]*songguo.dev/pipeline-number:)[[:space:]].*#\\1 \"${pipeline_number}\"#" \
  "$release_dir/k8s/kustomization.yaml"

if [[ "${FAILURE_DEMO:-false}" == "true" ]]; then
  # Explicit, manual-only rollback demonstration. The second image is made unavailable.
  awk -v bad="${image_tag}-missing" '
    /^  - name: .*shop-frontend$/ { frontend=1 }
    frontend && /^    newTag:/ { sub(/newTag:.*/, "newTag: " bad); frontend=0 }
    { print }
  ' "$release_dir/k8s/kustomization.yaml" > "$release_dir/k8s/kustomization.yaml.tmp"
  mv "$release_dir/k8s/kustomization.yaml.tmp" "$release_dir/k8s/kustomization.yaml"
fi

cat > "$release_dir/release-metadata.json" <<EOF
{
  "imageTag": "$image_tag",
  "pipelineNumber": "$pipeline_number",
  "commitId": "$commit_id",
  "commitIdShort": "$commit_short",
  "sourceBranch": "$source_branch",
  "createdAt": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "failureDemo": ${FAILURE_DEMO:-false},
  "images": [$images_json]
}
EOF

tar -C "$release_dir" -czf "$artifact_root/release-${image_tag}.tgz" k8s release-metadata.json image-metadata
echo "Published immutable release: $image_tag"
