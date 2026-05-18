# 前端目录约定（uni-app / 多端）

```
shopping_front/
├── api/           按业务拆分的接口函数（内部调用 utils/request）
├── config/        环境、API 基址等多端差异配置
├── pages/         页面
│   ├── home/      首页（自定义顶栏）
│   ├── browse/    逛逛 / 分类与列表骨架
│   ├── cart/      购物车占位
│   ├── user/      我的（登录入口、菜单骨架）
│   ├── auth/      登录、注册
│   ├── goods/     商品详情占位
│   ├── message/   消息列表占位
│   └── publish/   发布闲置占位
├── static/        静态资源
├── utils/         请求封装、工具函数
├── manifest.json  各端配置（含 H5 devServer 代理）
└── pages.json     路由与 TabBar
```

## 请求约定

- 页面与组件不要直接写死 `http://localhost:8080`，统一走 `utils/request.js` + `api/*`。
- **H5**：使用相对路径 `/api/...`，开发依赖 `manifest.json` → `h5.devServer.proxy`；上线依赖 Nginx 反代（见 `docs/deploy.md`）。
- **微信小程序 / App**：在 `config/env.js` 中配置 `https://` 的线上 API 根地址，并在各平台后台配置合法域名。
