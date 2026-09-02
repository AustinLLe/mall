# E2E 端到端测试

本项目的 E2E 测试既支持在本地使用 Edge 浏览器运行，也支持在 CI 环境中使用远程 Chrome 容器运行。

## 本地运行

执行下面的命令，即可启动一套独立的 Docker/Selenium 测试环境并运行全部 E2E 用例：

```powershell
npm run test:e2e
```

完整的四微服务门禁使用：

```powershell
npm ci
npm run test:e2e:microservices
```

该命令会创建独立 Compose 项目，启动旧单体兼容接口、user/catalog/trade/interaction
四个微服务、前端网关和 Selenium。它先运行微服务 API 全量回归；API 通过后才运行
全部 UI E2E。任一阶段失败都会返回非零退出码。

运行脚本会使用：

- 独立端口 `18080`；
- 独立的 Docker 网络；
- 独立的 MySQL 和上传文件数据卷；
- Selenium Chrome 浏览器容器。

测试结束后，无论成功还是失败，脚本都会自动停止并删除本次 E2E 测试创建的容器、网络和数据卷，不会修改日常一键部署环境使用的数据库数据卷。

运行前需要保证：

- Docker Desktop 已经启动；
- 项目根目录下的 `deploy/.env` 已正确配置；
- Docker 可以正常拉取所需镜像。

## 测试报告和失败现场

测试产生的报告和现场文件保存在以下位置：

- JUnit/Surefire 测试报告：`e2e-tests/target/surefire-reports/*.xml`
- 测试截图、失败页面源码和容器日志：`e2e-tests/target/e2e-artifacts/`
- Newman JUnit/HTML/JSON：`tests/api/reports/microservices/`
- Newman 统计：`tests/api/reports/microservices/newman-stats.json`（同时复制到 `tests/e2e/results/`）
- Newman 失败摘要：`tests/api/reports/microservices/failure-summary.md`
- 环境和统一退出码：`tests/e2e/results/`
- 截图清单：`tests/e2e/results/screenshot-index.md`
- 失败/业务截图副本：`tests/e2e/results/screenshots/`

当任何一条 E2E 用例失败时，执行命令会返回非零状态码。因此在 CI 流水线中，E2E 测试失败会导致当前阶段失败，后续部署阶段不会继续执行。

## CI 环境

CodeArts 使用以下文件运行独立的 E2E 测试阶段：

- 流水线配置：`.cloudbuild/e2e.yml`
- CI 执行脚本：`scripts/ci-e2e.sh`

在 CodeArts 中需要配置以下加密环境变量：

- `CI_DB_PASSWORD`：E2E 数据库普通用户密码；
- `CI_MYSQL_ROOT_PASSWORD`：E2E MySQL root 密码。

CI 使用与本地相同的完整 Compose 拓扑，先串行运行 Newman，再运行 Selenium。API
失败时 UI 会被跳过，以保留最直接的服务故障信号。所有失败都会在最终 gate 阻断流水线：

- `e2e-surefire-reports.tgz`（Surefire 报告）
- `e2e-artifacts.tgz`（截图、页面源码、容器日志）
- `e2e-api-reports.tgz`（Newman JUnit、HTML、JSON 和失败摘要）
- `e2e-run-results.tgz`（阶段摘要和环境结果）

并上传到软件发布库 `/NewSecondMall-e2e/`，再按测试退出码决定阶段红绿。

## 暂不自动执行的用例

头像上传用例目前保持禁用状态。原因是 uni-app H5 文件选择组件在无头浏览器中没有暴露稳定的文件输入元素，因此该功能暂时保留人工测试记录。

## 当前自动化基线（2026-09-01）

- API 阶段：82 个请求、163 个断言全部通过。
- UI 阶段：15 个场景，其中头像上传 1 项按设计跳过，其余场景均作为流水线门禁。Surefire 若未发现任何用例，退出码非零。
- 测试数据会自动创建并审核 3 个可浏览、收藏、关注、加购和下单的商品，避免下单后唯一商品变为已售导致后续首页为空。
- `/api/center` 已路由到 user-service，收藏/足迹和店铺关系路由到 catalog-service。
- 旧单体聊天兼容接口会向 user-service 验证令牌，从而支持微服务登录会话。

## 服务缺陷处理

测试配置不会通过 `continue-on-error` 忽略建表或启动错误。若某个服务不能启动、响应契约
不符或跨服务调用失败，门禁保持红色；查看 `failure-summary.md`、`compose-ps.txt` 和
`compose.log`。当前任务允许在定位明确后同步修复服务代码，但不通过放宽断言掩盖问题。
