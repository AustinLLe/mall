# SSH helpers for CodeArts -> ECS. Source this file; do not execute it.

ecs_ssh_setup() {
  ecs_host="${ECS_HOST:?ECS_HOST is required}"
  ecs_user="${ECS_USER:-root}"
  ecs_host_key="${ECS_HOST_KEY:?ECS_HOST_KEY is required}"

  [[ "$ecs_host" =~ ^[a-zA-Z0-9.:-]+$ ]] || { echo "Invalid ECS_HOST" >&2; return 2; }
  [[ "$ecs_user" =~ ^[a-z_][a-z0-9_-]*$ ]] || { echo "Invalid ECS_USER" >&2; return 2; }

  SSH_DIR="$(mktemp -d)"
  key_file="${ECS_SSH_KEY_FILE:-$SSH_DIR/id_deploy}"

  cleanup_ecs_ssh() {
    if [[ -z "${ECS_SSH_KEY_FILE:-}" ]]; then
      rm -f -- "$key_file"
    fi
    rm -rf -- "$SSH_DIR"
  }
  trap cleanup_ecs_ssh EXIT

  mkdir -p "$SSH_DIR"
  chmod 700 "$SSH_DIR"

  if [[ -z "${ECS_SSH_KEY_FILE:-}" ]]; then
    [[ -n "${ECS_SSH_PRIVATE_KEY:-}" ]] || { echo "ECS_SSH_PRIVATE_KEY or ECS_SSH_KEY_FILE is required" >&2; return 2; }
    printf '%s\n' "$ECS_SSH_PRIVATE_KEY" > "$key_file"
  fi
  chmod 600 "$key_file"
  printf '%s\n' "$ecs_host_key" > "$SSH_DIR/known_hosts"
  chmod 600 "$SSH_DIR/known_hosts"

  SSH_OPTS=(-i "$key_file" -o BatchMode=yes -o IdentitiesOnly=yes -o UserKnownHostsFile="$SSH_DIR/known_hosts" -o StrictHostKeyChecking=yes)
  REMOTE="${ecs_user}@${ecs_host}"
}

ecs_ssh() {
  ssh "${SSH_OPTS[@]}" "$REMOTE" "$@"
}

ecs_scp() {
  scp "${SSH_OPTS[@]}" "$@"
}
