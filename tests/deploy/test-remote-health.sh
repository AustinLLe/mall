#!/usr/bin/env bash
set -Eeuo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
test_root="$(mktemp -d)"
trap 'rm -rf "$test_root"' EXIT
mkdir -p "$test_root/bin"

cat > "$test_root/bin/kubectl" <<'EOF'
#!/usr/bin/env bash
set -e
args="$*"
if [[ "$args" == *"get deployment mysql -o jsonpath="* ]] || \
   [[ "$args" == *"get deployment backend -o jsonpath="* ]] || \
   [[ "$args" == *"get deployment frontend -o jsonpath="* ]]; then
  if [[ "$args" == *readyReplicas* ]]; then
    printf '1'
    exit 0
  fi
  if [[ "$args" == *spec.replicas* ]]; then
    printf '1'
    exit 0
  fi
  if [[ "$args" == *songguo.dev/image-tag* ]] || [[ "$args" == *annotations.songguo* ]]; then
    printf 'release-27-abc12345'
    exit 0
  fi
  if [[ "$args" == *backend*image* ]] || [[ "$args" == *"containers[0].image"* ]]; then
    if [[ "$args" == *frontend* ]]; then
      printf 'swr.cn-north-4.myhuaweicloud.com/songguo/shop-frontend:release-27-abc12345'
    else
      printf 'swr.cn-north-4.myhuaweicloud.com/songguo/shop-backend:release-27-abc12345'
    fi
    exit 0
  fi
fi
if [[ "$args" == *"get deployment backend -o jsonpath="* ]]; then
  printf 'swr.cn-north-4.myhuaweicloud.com/songguo/shop-backend:release-27-abc12345'
  exit 0
fi
if [[ "$args" == *"get deployment frontend -o jsonpath="* ]]; then
  printf 'swr.cn-north-4.myhuaweicloud.com/songguo/shop-frontend:release-27-abc12345'
  exit 0
fi
exit 0
EOF
cat > "$test_root/bin/curl" <<'EOF'
#!/usr/bin/env bash
set -e
# Support curl -w '%{http_code}' used by remote-health.sh
while [[ $# -gt 0 ]]; do
  case "$1" in
    -w)
      printf '%s' "200"
      exit 0
      ;;
    --write-out)
      printf '%s' "200"
      exit 0
      ;;
  esac
  shift
done
exit 0
EOF
chmod +x "$test_root/bin/kubectl" "$test_root/bin/curl"
export PATH="$test_root/bin:$PATH"

bash "$repo_root/ops/remote-health.sh" http://127.0.0.1 release-27-abc12345 "$test_root/health-out"
[[ -f "$test_root/health-out/health.log" ]]
grep -q "Health check succeeded" "$test_root/health-out/health.log"

echo "Remote health contract tests: OK"
