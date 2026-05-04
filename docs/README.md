# 项目文档目录

用于沉淀购物与二手交易平台的项目资料，建议按以下结构维护：

- `requirements.md`：需求说明（功能清单、非功能要求、范围边界）
- `api.md`：接口文档（按模块分组，含请求/响应示例）
- `frontend-structure.md`：前端目录约定与多端请求策略
- `database/`：数据库脚本（建库、建表、索引、初始化数据）
- `deploy.md`：H5 网站部署（域名访问、Nginx 反代 `/api`）
- `pipeline.md`：CodeArts 流水线说明（后端/前端构建步骤）

建议流程：

1. 需求变更先更新 `requirements.md`。
2. 接口或表结构变更同步更新 `api.md` 与 `database/` 脚本。
3. 发布前校验 `deploy.md` 与 `pipeline.md` 的可执行性。
