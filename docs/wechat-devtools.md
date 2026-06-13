# 微信开发者工具运行说明

本项目是 uni-app 项目，不能直接把 `shopping_front` 源码目录当成微信小程序项目导入。必须先由 HBuilderX 编译生成微信小程序产物。

## 正确运行步骤

1. 使用 HBuilderX 打开项目根目录下的 `shopping_front` 文件夹。

2. 在 HBuilderX 中选择：

```text
运行 -> 运行到小程序模拟器 -> 微信开发者工具
```

3. 微信开发者工具实际打开的目录应该是：

```text
项目根目录/shopping_front/unpackage/dist/dev/mp-weixin
```

4. 如果手动导入微信开发者工具，也应导入 `mp-weixin` 产物目录，而不是 `shopping_front` 源码目录。

## AppID 配置

微信小程序 AppID 位于：

```text
shopping_front/manifest.json
```

字段：

```json
{
  "mp-weixin": {
    "appid": "your-appid"
  }
}
```

课程演示阶段可以使用测试 AppID 或开发者工具测试号。

## 常见错误：subPackages of undefined

错误示例：

```text
Cannot read property 'subPackages' of undefined
simulator not found
```

常见原因：

- `shopping_front/pages.json` 不是合法 JSON。
- `pages.json` 中有字符串引号断裂、逗号错误、乱码导致 JSON 解析失败。
- `unpackage/dist/dev/mp-weixin/app.json` 是旧的坏产物。
- 微信开发者工具打开了错误目录。

处理方式：

1. 检查 `shopping_front/pages.json` 是否能被 JSON 解析。
2. 删除旧产物目录：

```text
shopping_front/unpackage/dist/dev/mp-weixin
```

3. 重新通过 HBuilderX 运行到微信开发者工具。
4. 确认微信开发者工具导入的是 `unpackage/dist/dev/mp-weixin`。

当前项目为了避免 Windows 编码损坏导致 `pages.json` 断裂，`pages.json` 中导航标题和 tabBar 文案暂时使用英文；页面内部仍可以显示中文。

## 小程序端 API 注意事项

- H5 可以通过 `manifest.json` 的 devServer proxy 访问 `/api`。
- 微信小程序不能使用 H5 代理，后续需要在 `shopping_front/config/env.js` 中配置 HTTPS 后端地址。
- 微信小程序正式请求需要在微信公众平台配置合法 request 域名。

## 修改文档要求

如果修改以下内容，必须同步更新本文档：

- `pages.json`
- `manifest.json` 的 `mp-weixin` 配置
- 小程序运行方式
- API 域名配置方式
- 微信开发者工具报错处理方式
