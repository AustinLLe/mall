#!/usr/bin/env bash
set -Eeuo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
test_root="$(mktemp -d)"
release_root="/opt/soft-shop/releases"
trap 'rm -rf "$test_root" "$release_root"' EXIT
mkdir -p "$test_root/bin" "$test_root/source/k8s"
cp -R "$repo_root/k8s/." "$test_root/source/k8s/"
printf '%s\n' '{"test":true}' > "$test_root/source/release-metadata.json"

cat > "$test_root/bin/kubectl" <<'EOF'
#!/usr/bin/env bash
set -e
state="${FAKE_KUBECTL_STATE:?}"
args="$*"
if [[ "$args" == *"get deployment backend -o jsonpath="* ]]; then
  printf '%s' 'swr.cn-north-4.myhuaweicloud.com/songguo/shop-backend:mytest-20260827'
  exit 0
fi
if [[ "$args" == *"get deployment frontend -o jsonpath="* ]]; then
  printf '%s' 'swr.cn-north-4.myhuaweicloud.com/songguo/shop-frontend:mytest-20260827'
  exit 0
fi
if [[ "$args" == *"apply -k"* ]]; then
  printf '%s\n' "${@: -1}" > "$state"
  exit 0
fi
if [[ "$args" == *"frontend"* ]] && [[ -f "${state}.trigger-failure" ]]; then
  rm -f "${state}.trigger-failure"
  exit 42
fi
exit 0
EOF
cat > "$test_root/bin/curl" <<'EOF'
#!/usr/bin/env bash
exit 0
EOF
chmod +x "$test_root/bin/kubectl" "$test_root/bin/curl"
export PATH="$test_root/bin:$PATH"
export FAKE_KUBECTL_STATE="$test_root/applied"

bash "$repo_root/ops/remote-deploy.sh" "$test_root/source" release-1-a http://127.0.0.1
[[ -f "$release_root/release-1-a/SUCCESS" ]]
[[ "$(readlink -f "$release_root/current")" == "$release_root/release-1-a" ]]

: > "${FAKE_KUBECTL_STATE}.trigger-failure"
set +e
bash "$repo_root/ops/remote-deploy.sh" "$test_root/source" release-2-b http://127.0.0.1
status=$?
set -e
[[ "$status" -ne 0 ]] || { echo "Expected failure was not triggered" >&2; exit 1; }
[[ ! -f "$release_root/release-2-b/SUCCESS" ]]
[[ "$(readlink -f "$release_root/current")" == "$release_root/release-1-a" ]]
[[ -f "$release_root/release-2-b/diagnostics/events.txt" ]]

echo "Remote deployment and rollback contract tests: OK"
