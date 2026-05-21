# 部署说明

当前项目包含两个部分：

- 前端：`shopping_front/`，uni-app，可构建 H5 或微信小程序。
- 后端：`shopping_back/shopping_back/`，Spring Boot，默认端口 `8080`。

## 本地开发

### 后端

进入后端目录：

```powershell
cd D:\Pro\soft-shop-pro\shopping_back\shopping_back
mvn spring-boot:run
```

当前后端接口以内存数据为主，后续接入 MySQL 时再补充数据库初始化脚本。

### 前端 H5

使用 HBuilderX 打开：

```text
D:\Pro\soft-shop-pro\shopping_front
```

运行到浏览器即可。H5 开发阶段通过 `manifest.json` 中的 devServer proxy 把 `/api` 转发到：

```text
http://127.0.0.1:8080
```

### 微信小程序

请参考：

```text
docs/wechat-devtools.md
```

## H5 构建

在 HBuilderX 中选择 H5 发行构建后，产物通常位于：

```text
shopping_front/unpackage/dist/build/h5/
```

部署时将该目录内容放到 Nginx 静态目录，例如：

```text
/var/www/shop-h5/
```

## Nginx 示例

```nginx
server {
    listen 80;
    server_name example.com;

    root /var/www/shop-h5;
    index index.html;

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

## 生产环境注意事项

- H5 部署建议使用 HTTPS。
- 微信小程序请求后端必须使用 HTTPS 域名。
- 后端跨域配置位于 `shopping_back/.../config/CorsConfig.java`。
- 统一异常处理位于 `shopping_back/.../config/GlobalExceptionHandler.java`。
- 若后端接入 MySQL，需要设置 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 等环境变量。

## 验收检查

部署或运行方式变更后，至少检查：

- 首页可打开。
- 登录接口可访问。
- 商品列表、详情、购物车、订单页面可跳转。
- `/api/products` 返回统一 `ApiResult`。
- 微信小程序端重新编译后没有 `subPackages of undefined` 等配置错误。

## 修改文档要求

如果修改构建方式、端口、代理、Nginx、后端启动方式、环境变量或部署路径，必须同步更新本文档。
