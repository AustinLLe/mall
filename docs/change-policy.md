# 代码修改与文档同步规范

从本规范创建后，项目中的每次代码修改都必须同步检查文档是否需要更新。

## 基本原则

代码和文档视为同一个交付物。只要代码行为、页面、接口、运行方式或协作方式发生变化，就需要更新对应文档。

## 修改类型与对应文档

### 页面或路由变更

包括新增页面、删除页面、重命名页面、修改 tabBar、修改页面入口。

必须检查：

- `shopping_front/pages.json`
- `docs/frontend-structure.md`
- `docs/wechat-devtools.md`，如果影响小程序运行

### 前端功能变更

包括页面交互、购物车、本地存储、登录状态、地址管理、mock 数据。

必须检查：

- `docs/frontend-structure.md`
- `docs/api.md`，如果字段来自后端接口

### 后端接口变更

包括新增接口、删除接口、修改路径、请求参数、返回字段、DTO、异常结构。

必须检查：

- `docs/api.md`
- `docs/deploy.md`，如果影响运行或环境变量

### 运行或部署变更

包括端口、代理、构建方式、微信小程序导入方式、Nginx、环境变量。

必须检查：

- `docs/deploy.md`
- `docs/wechat-devtools.md`
- `docs/README.md`

### 协作规则变更

包括分工方式、提交流程、测试要求、文档要求。

必须检查：

- `docs/change-policy.md`
- `docs/README.md`

## 每次交付前检查清单

交付前必须自查：

- 是否有新增或删除文件。
- 是否有新增或删除页面路由。
- 是否有新增或修改接口。
- 是否有新增或修改字段。
- 是否有运行方式变化。
- 是否已经更新对应文档。
- 最终回复是否说明了文档同步内容。

## 回复格式要求

每次完成代码修改后的最终回复，需要包含：

- 代码修改摘要。
- 文档修改摘要。
- 测试或检查结果。
- 未能验证的事项。

## 当前文档状态

本次已将以下文档重写为干净版本：

- `docs/README.md`
- `docs/frontend-structure.md`
- `docs/api.md`
- `docs/wechat-devtools.md`
- `docs/deploy.md`

并新增本文档：

- `docs/change-policy.md`
