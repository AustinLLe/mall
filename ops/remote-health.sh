#!/usr/bin/env bash
set -Eeuo pipefail

namespace="${K8S_NAMESPACE:-shop}"
health_base_url="${1:-http://127.0.0.1}"
expected_tag="${2:-}"
artifact_dir="${3:-/tmp/soft-shop-health}"

mkdir -p "$artifact_dir"
exec > >(tee "$artifact_dir/health.log") 2>&1

command -v kubectl >/dev/null || { echo "kubectl is required" >&2; exit 2; }
command -v curl >/dev/null || { echo "curl is required" >&2; exit 2; }

kubectl -n "$namespace" get all,ingress,pvc -o wide
kubectl -n "$namespace" get events --sort-by=.lastTimestamp | tail -n 40 || true

workload_deployments=(mysql backend frontend user-service catalog-service trade-service interaction-service)

for deployment in "${workload_deployments[@]}"; do
  ready="$(kubectl -n "$namespace" get deployment "$deployment" -o jsonpath='{.status.readyReplicas}')"
  spec="$(kubectl -n "$namespace" get deployment "$deployment" -o jsonpath='{.spec.replicas}')"
  echo "$deployment readyReplicas=$ready specReplicas=$spec"
  [[ "$ready" == "1" && "$spec" == "1" ]] || {
    echo "$deployment is not 1/1 Running" >&2
    kubectl -n "$namespace" get pods -l "app.kubernetes.io/name=$deployment" -o wide >&2 || true
    exit 3
  }
done

kubectl -n "$namespace" get deployment "${workload_deployments[@]}" \
  -o custom-columns=NAME:.metadata.name,IMAGE:.spec.template.spec.containers[0].image,TAG:.metadata.annotations.songguo\\.dev/image-tag,COMMIT:.metadata.annotations.songguo\\.dev/commit-id,PIPELINE:.metadata.annotations.songguo\\.dev/pipeline-number

if [[ -n "$expected_tag" ]]; then
  actual_tag="$(kubectl -n "$namespace" get deployment backend -o jsonpath='{.metadata.annotations.songguo\.dev/image-tag}')"
  [[ "$actual_tag" == "$expected_tag" ]] || {
    echo "backend annotation tag '$actual_tag' != expected '$expected_tag'" >&2
    exit 4
  }
  for deployment in backend frontend user-service catalog-service trade-service interaction-service; do
    image="$(kubectl -n "$namespace" get deployment "$deployment" -o jsonpath='{.spec.template.spec.containers[0].image}')"
    [[ "$image" == *":$expected_tag" ]] || {
      echo "$deployment image '$image' does not end with :$expected_tag" >&2
      exit 4
    }
  done
fi

home_code="$(curl -sS -o /tmp/health-home.body -w '%{http_code}' --retry 5 --retry-delay 3 --max-time 10 "$health_base_url/")"
api_code="$(curl -sS -o /tmp/health-api.body -w '%{http_code}' --retry 5 --retry-delay 3 --max-time 10 "$health_base_url/api/products")"
echo "GET / -> $home_code"
echo "GET /api/products -> $api_code"
[[ "$home_code" == "200" ]] || { echo "homepage did not return HTTP 200" >&2; exit 5; }
[[ "$api_code" == "200" ]] || { echo "/api/products did not return HTTP 200" >&2; exit 5; }

# 四服务公网路由可达性检查：公开接口必须被 Nginx 路由到对应微服务且服务在线。
# 判定：返回码不是 404（未路由）/ 502（网关无可用后端）/ 503（服务不可用）。
echo "--- microservice public route reachability ---"
route_check() {
  local path="$1" service="$2"
  local code
  code="$(curl -sS -o /dev/null -w '%{http_code}' --max-time 10 "$health_base_url$path")"
  echo "GET $path -> $code (${service})"
  case "$code" in
    404|502|503) echo "route check failed: $path ($service) returned $code" >&2; return 1 ;;
  esac
}
route_check "/api/auth/me" "user-service"       || exit 6
route_check "/api/products" "catalog-service"   || exit 6
route_check "/api/orders" "trade-service"       || exit 6
route_check "/api/topics" "interaction-service" || exit 6

echo "Health check succeeded"
