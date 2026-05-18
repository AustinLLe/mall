# 微信开发者工具运行说明

## 1. 运行前提

- 前端工程：`shopping_front`
- 开发工具：`HBuilderX` + `微信开发者工具`
- 后端服务：本地 Spring Boot 已启动，或你已经准备好一个可访问的线上 HTTPS API

## 2. 需要配置的两个关键值

### AppID

编辑 `shopping_front/manifest.json`：

- `mp-weixin.appid`

说明：

- 开发调试建议填你自己的微信小程序 AppID。
- 如果暂时没有正式小程序，可先在 HBuilderX/微信开发者工具里使用测试方式调试，但后续真机和接口联调仍建议替换为正式 AppID。

### 小程序 API 地址

编辑 `shopping_front/config/env.js`：

- 非 H5 环境使用 `RUNTIME_API_BASE`

示例：

```js
// #ifndef H5
const RUNTIME_API_BASE = 'https://api.your-domain.com'
// #endif
```

说明：

- 小程序不能像 H5 一样依赖本地 `/api` 代理。
- 小程序端推荐使用可公网访问的 `HTTPS` 接口地址。
- 真机和提审时，需要把该域名加入微信小程序后台“合法 request 域名”。

## 3. HBuilderX 运行到微信开发者工具

1. 打开 `shopping_front`。
2. 在 HBuilderX 中配置微信开发者工具安装路径。
3. 点击“运行 -> 运行到小程序模拟器 -> 微信开发者工具”。
4. HBuilderX 会编译出微信小程序项目，并自动拉起微信开发者工具。

## 4. 开发者工具内建议

- 开发环境可关闭“校验合法域名、web-view、TLS 版本及 HTTPS 证书”限制，便于本地联调。
- 若接口仍失败，优先检查：
  - `config/env.js` 是否已改成真实可访问地址
  - 后端是否允许对应来源访问
  - 微信后台是否已配置合法域名

## 5. 当前项目的多端策略

- H5：走相对路径 `/api`，通过 `manifest.json` 中的 `devServer.proxy` 转发到本地后端。
- 微信小程序：走 `config/env.js` 中配置的 HTTPS API 根地址。

## 6. 推荐调试顺序

1. 先跑通 H5，确认页面与接口无误。
2. 再配置小程序 AppID 与 API 地址。
3. 用微信开发者工具验证登录、注册、购物车、地址管理等核心流程。


下载好微信开发者工具然后注册一下，用自己的appID换一下应该就可以
