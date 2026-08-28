#!/usr/bin/env bash
set -Eeuo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

image_tag="${IMAGE_TAG:?IMAGE_TAG is required}"
ecs_host="${ECS_HOST:?ECS_HOST is required}"
ecs_user="${ECS_USER:-root}"
ecs_host_key="${ECS_HOST_KEY:?ECS_HOST_KEY is required}"
health_base_url="${HEALTHCHECK_BASE_URL:-http://127.0.0.1}"
artifact_root="${CI_ARTIFACT_DIR:-$repo_root/ci-artifacts}"
release_dir="$artifact_root/release"
remote_incoming="/tmp/soft-shop-${image_tag}"

[[ "$image_tag" =~ ^release-[a-zA-Z0-9._-]+$ ]] || { echo "Invalid IMAGE_TAG" >&2; exit 2; }
[[ "$ecs_host" =~ ^[a-zA-Z0-9.:-]+$ ]] || { echo "Invalid ECS_HOST" >&2; exit 2; }
[[ "$ecs_user" =~ ^[a-z_][a-z0-9_-]*$ ]] || { echo "Invalid ECS_USER" >&2; exit 2; }

ssh_dir="$(mktemp -d)"
key_file="${ECS_SSH_KEY_FILE:-$ssh_dir/id_deploy}"

cleanup_local_credentials() {
  if [[ -z "${ECS_SSH_KEY_FILE:-}" ]]; then
    rm -f -- "$key_file"
  fi
  rm -rf -- "$ssh_dir"
}
trap cleanup_local_credentials EXIT

[[ -d "$release_dir/k8s" && -f "$release_dir/release-metadata.json" ]] || {
  echo "Release artifacts are missing; publish images first." >&2
  exit 2
}
mkdir -p "$ssh_dir" "$artifact_root/deploy"
chmod 700 "$ssh_dir"

if [[ -z "${ECS_SSH_KEY_FILE:-}" ]]; then
  [[ -n "${ECS_SSH_PRIVATE_KEY:-}" ]] || { echo "ECS_SSH_PRIVATE_KEY or ECS_SSH_KEY_FILE is required" >&2; exit 2; }
  printf '%s\n' "$ECS_SSH_PRIVATE_KEY" > "$key_file"
fi
chmod 600 "$key_file"
printf '%s\n' "$ecs_host_key" > "$ssh_dir/known_hosts"
chmod 600 "$ssh_dir/known_hosts"

ssh_opts=(-i "$key_file" -o BatchMode=yes -o IdentitiesOnly=yes -o UserKnownHostsFile="$ssh_dir/known_hosts" -o StrictHostKeyChecking=yes)
remote="${ecs_user}@${ecs_host}"

ssh "${ssh_opts[@]}" "$remote" "rm -rf '$remote_incoming' && mkdir -p '$remote_incoming'"
scp "${ssh_opts[@]}" -r "$release_dir/k8s" "$release_dir/release-metadata.json" "$repo_root/ops/remote-deploy.sh" "$remote:$remote_incoming/"

set +e
ssh "${ssh_opts[@]}" "$remote" \
  "chmod 700 '$remote_incoming/remote-deploy.sh' && sudo '$remote_incoming/remote-deploy.sh' '$remote_incoming' '$image_tag' '$health_base_url'"
deploy_status=$?
set -e

scp "${ssh_opts[@]}" -r "$remote:/opt/soft-shop/releases/$image_tag/diagnostics" "$artifact_root/deploy/" 2>/dev/null || true
scp "${ssh_opts[@]}" "$remote:/opt/soft-shop/releases/$image_tag/deploy.log" "$artifact_root/deploy/deploy.log" 2>/dev/null || true
ssh "${ssh_opts[@]}" "$remote" "rm -rf '$remote_incoming'" || true

exit "$deploy_status"
