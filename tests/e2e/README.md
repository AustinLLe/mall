# E2E 端到端测试

本项目的 E2E 测试既支持在本地使用 Edge 浏览器运行，也支持在 CI 环境中使用远程 Chrome 容器运行。

## 本地运行

执行下面的命令，即可启动一套独立的 Docker/Selenium 测试环境并运行全部 E2E 用例：

```powershell
npm run test:e2e
```

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

当任何一条 E2E 用例失败时，执行命令会返回非零状态码。因此在 CI 流水线中，E2E 测试失败会导致当前阶段失败，后续部署阶段不会继续执行。

## CI 环境

CodeArts 使用以下文件运行独立的 E2E 测试阶段：

- 流水线配置：`.cloudbuild/e2e.yml`
- CI 执行脚本：`scripts/ci-e2e.sh`

在 CodeArts 中需要配置以下加密环境变量：

- `CI_DB_PASSWORD`：E2E 数据库普通用户密码；
- `CI_MYSQL_ROOT_PASSWORD`：E2E MySQL root 密码。

CI 会依次构建并启动 MySQL、后端、前端和 Selenium Chrome 容器，等待服务健康后执行全部 E2E 用例，最后保存测试报告并清理测试环境。

## 暂不自动执行的用例

头像上传用例目前保持禁用状态。原因是 uni-app H5 文件选择组件在无头浏览器中没有暴露稳定的文件输入元素，因此该功能暂时保留人工测试记录。
