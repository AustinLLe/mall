# 松果集市

松果集市是一个面向新品与二手商品流转的交易平台，支持商品浏览、发布与审核、购物车与订单、用户中心、即时消息、社区互动和 AI 辅助议价。

## 技术栈

- 前端：uni-app、Vue 3，支持 H5 和微信小程序。
- 后端：Spring Boot 3、MyBatis、MySQL、WebSocket/STOMP。
- 开发平台：CodeArts Repo、看板/Scrum、流水线与制品管理。

## 目录

- `shopping_front/`：前端工程。
- `shopping_back/shopping_back/`：后端工程。
- `shopping_back/shopping_back/doc/db.sql`：数据库初始化脚本。
- `docs/`：需求、接口、测试、部署和用户文档。

## 本地启动

1. 按 [环境搭建说明](ENV_SETUP.md) 创建数据库并配置本地凭据。
2. 在 `shopping_back/shopping_back/` 启动 Spring Boot，确认 `http://127.0.0.1:8080/api/products` 返回数据。
3. 首次运行前端时，在 `shopping_front/` 执行 `npm install`。
4. 使用 HBuilderX 打开 `shopping_front/`，运行到浏览器；默认地址为 `http://localhost:5173`。

任何密码、Token 和 AI Key 都不得提交到仓库。请通过环境变量或未纳入版本控制的 `application-local.properties` 配置。

## Docker 一键部署

新机器只需安装 Git 和 Docker Desktop，不需要预先安装 Maven、Java、Node.js 或 HBuilderX。

```powershell
Copy-Item deploy\.env.example deploy\.env
# 编辑 deploy\.env，替换数据库密码和 PUBLIC_ORIGIN
docker compose --env-file deploy\.env -f deploy\docker-compose.yml up -d --build
docker compose --env-file deploy\.env -f deploy\docker-compose.yml ps
```

也可双击 `NewSecondMall-启动面板.cmd`，选择第 9 项。Docker 会在多阶段构建中自动安装依赖、编译前后端并启动 MySQL、backend 和 frontend 三个容器。

## 文档入口

- [项目详细说明](docs/README.md)
- [接口文档](docs/api.md)
- [测试文档](docs/测试文档.md)
- [部署文档](docs/部署文档.md)
- [用户手册](docs/用户手册.md)
- [代码与文档同步规范](docs/change-policy.md)

## 当前改进方向

项目将先建立测试、部署、安全、性能和可观测性基线，再从模块化单体逐步演进。只有在边界、数据所有权和回归测试明确后，才抽取独立服务，避免为了“微服务”而增加不必要的分布式复杂度。
