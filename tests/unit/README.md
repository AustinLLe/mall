# 单元测试说明

## 运行方式

请通过仓库提供的脚本调用 Maven Wrapper，不建议直接使用本机安装的 Maven。

Windows PowerShell：

```powershell
.\scripts\test-unit.ps1 monolith
.\scripts\test-unit.ps1 microservices
.\scripts\test-unit.ps1 all
```

Linux 或 CI：

```bash
./scripts/test-unit.sh monolith
./scripts/test-unit.sh microservices
./scripts/test-unit.sh all
```

参数含义：

- `monolith`：只运行旧单体单元测试。
- `microservices`：运行 `common` 和四个微服务的测试及覆盖率门禁。
- `all`：先运行旧单体测试，再运行全部微服务测试。

本地支持 JDK 17 和 JDK 21，流水线统一使用 JDK 17。脚本启动时会输出 Java、Maven 和 Maven 本地仓库信息；其他 JDK 版本会在测试前直接报错。

## 微服务测试执行规则

微服务测试使用 Maven `--fail-at-end`。断言失败后仍会继续执行其他模块，避免一个服务的问题导致后续模块没有测试结果。

为生成完整报告，Maven 测试阶段允许执行到结束，但这不代表忽略失败。汇总脚本和流水线最终门禁会检查所有 Surefire、Failsafe 和覆盖率报告。只要存在测试失败、错误、缺失报告或覆盖率不足，最终退出码就是非零。

## 覆盖率门禁

- `common`：行覆盖率不得低于 60%；没有业务分支时不检查分支覆盖率。
- `user-service`、`catalog-service`、`trade-service`、`interaction-service`：行覆盖率不得低于 60%，分支覆盖率不得低于 50%。

汇总报告生成在 `tests/unit/results/`，该目录已加入 Git 忽略列表。原始报告位于：

- 单体测试：`shopping_back/shopping_back/target/surefire-reports/`
- 单体覆盖率：`shopping_back/shopping_back/target/site/jacoco/`
- 微服务单测：`services/<模块名>/target/surefire-reports/`
- 微服务上下文测试：`services/<模块名>/target/failsafe-reports/`
- 微服务覆盖率：`services/<模块名>/target/site/jacoco/`

## Maven 依赖解析问题

如果 Maven 在编译测试前报告父 POM 或依赖无法解析，应检查网络、`~/.m2/settings.xml` 中的镜像地址、Maven 本地缓存和代理配置。可以使用 Maven Wrapper 加 `-U` 强制刷新依赖。此类错误发生在测试执行之前，不属于单元测试断言失败。

## 当前已发现的生产问题

以下问题由自动化测试暴露。测试侧没有禁用用例、放宽断言、吞掉异常或修改生产代码绕过问题。

### user-service 数据库初始化失败

`schema.sql` 已经创建 `users.role` 字段，但 `AuthService.ensureSchema()` 启动时再次尝试添加该字段，H2 上下文测试因此报告 `Duplicate column name "ROLE"`。

该上下文测试会保持失败，直到服务维护者修复生产初始化逻辑。

### common 鉴权响应反序列化失败

`AuthClient` 需要把鉴权响应反序列化为 `ApiResult<AuthUser>`，但 `ApiResult` 没有可供 Jackson 使用的 Creator 或默认构造器。因此，即使鉴权服务返回正常响应，也可能被转换成 HTTP 503。

该测试会保持失败，直到服务维护者修复生产响应对象的反序列化能力。

## 流水线结果判定

流水线会归档单体和微服务的 Surefire、Failsafe、JaCoCo 及失败摘要。产物收集步骤在测试失败后仍会执行，但以下任意情况都会让最终门禁返回非零并阻止后续发布或部署：

- 任意测试失败或报错。
- 任意模块没有执行测试或缺少报告。
- 任意模块行覆盖率低于门槛。
- 任意业务微服务分支覆盖率低于门槛。
