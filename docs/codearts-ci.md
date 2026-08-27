# CodeArts CI

当前分两层：

- **编译构建**：`.cloudbuild/build.yml`，检出、编译前后端，并跑后端单元测试。
- **流水线**：调用这个构建任务。Newman 接口测试、K8s 部署、健康检查仍放在流水线后续阶段，不进 `build.yml`。

## 编译构建

1. 检出代码（分支用 `${codeBranch}`，跟执行时选的分支走）
2. Node 18：前端 `npm ci` + `npm run build:h5`
3. Maven JDK 17：`mvn package`（含 `ciTestByHe` 单元测试；关掉华为云 Maven 缓存，不用 `mvnw`）

参数在构建任务「参数设置」里：

- `CI_DB_PASSWORD`（测试值 `CiShop2026_Test`）
- `CI_MYSQL_ROOT_PASSWORD`（测试值 `CiRoot2026_Test`）

规格必须是 `2U8G`，不能写 `2U4G`。

## 把已跑通的构建接到流水线

不要再用「下载仓库」和 `official_shell_plugin`。流水线里只加 **Build 构建** 插件。

1. 打开已有流水线，进入任务编排。
2. 删掉「下载仓库」「构建前端和后端」这两步。
3. 从右侧插件列表拖入 **Build 构建**。
4. 「请选择需要调用的任务」选构建任务 **NewSecondMall**（就是刚才跑绿的那个）。
5. 仓库选 **NewSecondMall**。
6. 保存并执行，分支选 `feature/lqy-first-stage`。

控制台选好任务后，YAML 里会自动填上 32 位 `jobId`。不要手写、不要用 `official_git_clone`。

构建任务详情页的浏览器地址末尾那 32 位，就是 `jobId`。若要用仓库里的 `pipeline.yml`，把 `REPLACE_WITH_BUILD_JOB_ID` 换成这个值。

构建失败时流水线应直接失败，不会继续后面的部署步骤。成功/失败记录在流水线和构建历史里都能看到。
