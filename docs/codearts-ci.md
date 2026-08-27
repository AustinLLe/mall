# CodeArts 流水线接入

## 当前可自动执行的阶段

1. 获取仓库代码。
2. 安装前端依赖并构建 H5。
3. 执行后端单元测试并编译 JAR。
4. 构建带版本号的前后端镜像。
5. 启动 Compose 集成环境并执行集成冒烟测试。
6. 在具备 Chrome/Edge 的执行机上运行 Selenium E2E。
7. 使用可访问集群的凭据部署 Kubernetes 并等待健康检查。

在 CodeArts 的构建步骤中执行：

```bash
chmod +x scripts/build-and-test.sh tests/blackbox/smoke.sh
export DB_PASSWORD="$CI_DB_PASSWORD"
export MYSQL_ROOT_PASSWORD="$CI_MYSQL_ROOT_PASSWORD"
export IMAGE_TAG="ci-${GIT_COMMIT_ID:-manual}"
export RUN_E2E=true
./scripts/build-and-test.sh
```

## CodeArts 页面配置

1. 打开“流水线”，新建流水线并选择本仓库和 `master` 分支。
2. 添加“构建”任务，运行环境选择 Linux，并选择 JDK 21。
3. 在流水线的安全变量中添加 `CI_DB_PASSWORD` 和 `CI_MYSQL_ROOT_PASSWORD`，不要写入仓库。
4. 打开代码提交触发器，选择 push 到 `master`。
5. 保存后提交一次无功能改动，确认流水线自动启动。

如果日志出现 `release version 21 not supported`，说明构建环境仍不是 JDK 21，需要先在 CodeArts 页面修改运行环境。

## 部署阶段的前提

CodeArts 构建机不能直接访问本机 Docker Desktop Kubernetes。自动部署必须使用可访问的集群（例如 CCE）及 kubeconfig/CodeArts 集群凭据。部署步骤执行：

```bash
chmod +x scripts/deploy-k8s.sh
./scripts/deploy-k8s.sh
```

## 验收记录

至少保留一次成功记录和一次故意失败记录。失败记录应显示测试步骤失败，且没有执行部署步骤。
