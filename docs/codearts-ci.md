# CodeArts CI

作业要的是**流水线多个板块**，不是把所有步骤塞进一个构建任务。

华为云里真正干活的还是「编译构建」任务（自带 checkout）。流水线只负责串卡片：上一张失败，后面不跑。

当前规划：

| 流水线板块 | 实现方式 | 仓库文件 |
| --- | --- | --- |
| 1. 构建 | 已有构建任务 NewSecondMall | `.cloudbuild/build.yml` |
| 2. 单元测试 | 再建一个构建任务，流水线里第二张卡片 | `.cloudbuild/unit-test.yml` |
| 3. 启动测试环境 + 集成测试 + 接口测试 | 再建一个构建任务，流水线第三张卡片 | `.cloudbuild/integration-api.yml` |
| 4. 构建镜像 | 以后加 | Dockerfile |
| 5. 部署 | 以后加 | `k8s/` |
| 6. 健康检查 | 以后加 | 探活脚本 |

不要用流水线里的「下载仓库」+ shell 自己编译。那条路会空目录、`auth info is empty`。

## 为什么构建步骤里看不到「单元测试」

构建任务左侧列表显示的是 YAML 里每个插件的 `name`：

- `checkout`
- `build-frontend`
- `build-backend`

`wait_job_depends`、`环境准备` 是平台自己加的，不是仓库写的。

编译任务里的 `build-backend` 只打包，不跑测试。单元测试在独立任务 `NewSecondMall-unit-test` 里。

## 编译构建（第 1 张卡片）

文件：`.cloudbuild/build.yml`

1. 检出（`${codeBranch}`）
2. Node 18：`npm ci` + `npm run build:h5`
3. Maven JDK 17：`mvn -DskipTests package`（只编译打包；单元测试在另一张卡片）

参数在**该构建任务**的「参数设置」里：

- `CI_DB_PASSWORD` = `CiShop2026_Test`
- `CI_MYSQL_ROOT_PASSWORD` = `CiRoot2026_Test`

规格必须是 `2U8G`。Maven 必须 `cache: false`，命令用 `mvn` 不用 `./mvnw`。

## 单元测试（第 2 张卡片）

文件：`.cloudbuild/unit-test.yml`

这是另一个构建任务，不要改现有 NewSecondMall 那份 YAML 路径。步骤只有：checkout → `mvn test`。

控制台操作：

1. 编译构建 → 新建任务，名称例如 `NewSecondMall-unit-test`。
2. 源码仍选仓库 NewSecondMall，默认分支 `feature/lqy-first-stage`。
3. 构建方式选 YAML，路径填 `.cloudbuild/unit-test.yml`。
4. 参数同样填 `CI_DB_PASSWORD`、`CI_MYSQL_ROOT_PASSWORD`，规格 `2U8G`。
5. 先单独执行一次，确认步骤列表出现 `unit-test`。
6. 打开流水线 → 任务编排 → 在「构建」后面新增阶段，名称「单元测试」。
7. 拖入 **Build 构建**，任务选 `NewSecondMall-unit-test`，依赖上一张「构建」。
8. 保存并执行，分支 `feature/lqy-first-stage`。

流水线画布上应看到两块：构建 → 单元测试。点进第二块，步骤里才会出现名为 `unit-test` 的行。

`NewSecondMall` 任务不要加 `CB_BUILD_YAML_PATH`。每个构建任务都要有 `codeBranch`，默认值 `feature/lqy-first-stage`。

## 集成与接口测试（第 3 张卡片）

文件：`.cloudbuild/integration-api.yml`  
脚本：`scripts/ci-integration-api.sh`

一张卡片里顺序做三件事：本机起 MariaDB + 后端 jar → `tests/blackbox/smoke.sh`（接口冒烟）→ Newman `npm run test:api`。必须放在同一个步骤里，环境才能一直活着。

不要用 `cloudbuild@docker20.10` 或 Docker Compose：华为云执行机访问不了 Docker Hub（`registry-1.docker.io` 超时）。这张卡片沿用已经跑通的 `cloudbuild@maven3.9.5-jdk17`。

控制台操作（和单元测试任务同一套办法）：

1. 编译构建 → 新建任务，名称 `NewSecondMall-integration-api`。
2. 源码仍选 NewSecondMall，默认分支 `feature/lqy-first-stage`。
3. 先随便保存，**不要**在代码化里改 `build.yml`。
4. 参数设置增加：

| 名称 | 默认值 |
| --- | --- |
| `CB_BUILD_YAML_PATH` | `.cloudbuild/integration-api.yml` |
| `codeBranch` | `feature/lqy-first-stage`（运行时设置打开） |
| `CI_DB_PASSWORD` | `CiShop2026_Test` |
| `CI_MYSQL_ROOT_PASSWORD` | `CiRoot2026_Test` |

5. 规格 `2U8G`。保存后从任务列表点 **执行**，不要从代码化点保存并执行。
6. 单独跑绿后，流水线在「单元测试」后新增阶段「集成测试」，拖入 Build，任务选 `NewSecondMall-integration-api`，依赖单元测试。

执行记录里步骤名是 `start-env-integration-api`。日志里会有 `1/3 启动测试环境`、`2/3 集成测试`、`3/3 接口测试`。

## 流水线 YAML 占位

`.codearts/workflow/pipeline.yml` 里：

- `REPLACE_WITH_BUILD_JOB_ID`：现有构建任务详情页 URL 末尾 32 位
- `REPLACE_WITH_UNIT_TEST_JOB_ID`：单元测试构建任务同样位置
- `REPLACE_WITH_INTEGRATION_API_JOB_ID`：集成与接口测试构建任务同样位置

控制台选好任务后会自动填 `jobId`。不要手写，不要用 `official_git_clone`。
