# CodeArts CI/CD 交接说明

## 目标流水线

为 `master` 配置 Push 触发，阶段严格串行：

```text
代码检出 → 编译 → 单元测试 → API 测试 → E2E 测试
        → 镜像制作与部署 → Kubernetes 健康检查
```

任一步失败时后续阶段不得运行。镜像制作与部署阶段引用 `.cloudbuild/release.yml`，E2E 阶段引用 `.cloudbuild/e2e.yml`。

## 流水线参数与凭据

CodeArts 系统参数直接传入构建任务：`PIPELINE_NUMBER`、`COMMIT_ID`、`COMMIT_ID_SHORT`。最终版本固定生成为：

```text
release-${PIPELINE_NUMBER}-${COMMIT_ID_SHORT}
```

在 CodeArts 凭据管理或加密参数中配置，禁止填写在仓库文件中：

| 名称 | 用途 |
| --- | --- |
| `SWR_USERNAME` | 登录华北-北京四 SWR |
| `SWR_PASSWORD` | SWR 流水线专用凭据 |
| `ECS_HOST` | ECS 地址，当前为 `120.46.222.10` |
| `ECS_USER` | 部署用户，当前为 `root` |
| `ECS_SSH_PRIVATE_KEY` | CodeArts 专用部署私钥；平台注入文件时改传 `ECS_SSH_KEY_FILE` |
| `ECS_HOST_KEY` | 已人工核对的 SSH known_hosts 完整行 |
| `CI_DB_PASSWORD` | E2E 临时数据库用户密码 |
| `CI_MYSQL_ROOT_PASSWORD` | E2E 临时 MySQL root 密码 |

可选参数：`HEALTHCHECK_BASE_URL` 默认 `http://127.0.0.1`；`ROLLOUT_TIMEOUT` 默认 `300s`；`FAILURE_DEMO` 默认 `false`，仅限人工回滚演示，禁止加入 master 自动触发参数。

## 阶段配置

1. 编译和单元测试：后端执行 Maven test 并保留 Surefire、JaCoCo；前端安装依赖并构建。
2. API 测试：运行 `npm run test:api`，发布 `tests/api/reports/`。
3. E2E 测试：运行 `.cloudbuild/e2e.yml`，发布 Surefire、截图、页面源码和 Compose 日志。
4. 发布部署：运行 `.cloudbuild/release.yml`。部署失败也会先上传 `ci-artifacts/**`，再由 release gate 标记失败。
5. 为部署阶段启用串行执行；新的 master Push 必须排队，不能同时操作 ECS。

## 交付证据

- `ci-artifacts/release/release-metadata.json`：Commit、流水线编号、镜像 Tag 和 Digest。
- `ci-artifacts/image-publish.log`：镜像构建/推送日志。
- `ci-artifacts/kubernetes-deploy.log`：SSH 部署与健康检查日志。
- `ci-artifacts/deploy/`：describe、events 和 Pod 日志。
- CodeArts 流水线执行历史：至少保留一次完整成功和一次受控失败记录。

## 验收标准

- 两个 SWR 仓库出现相同的新 Tag，旧 Tag 未被覆盖。
- MySQL、后端、前端均为 `1/1 Running`。
- Deployment 注解中的版本、Commit、流水线编号与发布元数据一致。
- 首页和 `/api/products` 返回 HTTP 200。
- 测试失败时不制作镜像；部署失败时恢复上一成功版本，但流水线仍显示失败。

## 受控失败演示

得到 ECS 负责人确认后，手工执行一次 `FAILURE_DEMO=true` 的发布任务。脚本让本次前端引用不存在的 Tag，旧 Pod 在滚动更新期间保持服务；超时后采集 `ImagePullBackOff` 现场并回滚。演示结束后确认首页和 API 为 200，并保留该次失败记录。
