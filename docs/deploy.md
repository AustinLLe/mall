# 网站（H5）部署：让别人通过网址访问（还没部署）

你现在的前端是 **uni-app**，网页端对应产物是 **H5 静态文件**。要让朋友输入网址就能打开，需要三步：**本机构建 → 上传到服务器 → 绑定域名（可选 HTTPS）**。

## 1. 本地打包 H5

在 **HBuilderX** 中：

1. 打开项目目录 `shopping_front`
2. 菜单 **发行 → 网站-H5-手机版**（或同类「发行到 H5」）
3. 等待编译完成，产物一般在：  
   `shopping_front/unpackage/dist/build/h5/`

该目录内会有 `index.html` 以及 `static` 等静态资源。

## 2. 准备一台公网服务器

任选其一即可：

- 云厂商 ECS（阿里云 / 华为云等）一台 Linux 虚拟机
- 或支持托管「静态网站」的对象存储 + CDN（需按厂商文档配置默认首页与路由）

下面以最常见的 **Nginx + Linux** 为例。

## 3. 把静态文件放到服务器

例如放到：`/var/www/shop-h5/`

将 `unpackage/dist/build/h5/` 目录下的**全部内容**上传到此目录（保持目录结构）。

## 4. Nginx 配置示例（同域反代 API）

前端代码在 H5 下请求的是 **相对路径** `/api/...`，因此 Nginx 需要把 `/api` 转发到你的 Spring Boot（例如 `127.0.0.1:8080`），这样浏览器只访问一个域名，避免跨域问题。

```nginx
server {
    listen 80;
    server_name www.你的域名.com;

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

说明：

- 本项目 H5 使用 **hash 路由**（`manifest.json` 中 `router.mode` 为 `hash`），一般 **`try_files` 可简化**；保留也不影响。
- 后端建议用 **systemd** 或 **Docker** 常驻运行 `java -jar shopping_back-....jar`，不要依赖「你本机 IDEA 点运行」。

## 5. 域名与 HTTPS

- 在域名服务商处把 **A 记录** 指到你的服务器公网 IP。
- 生产环境强烈建议配置 **HTTPS**（Let’s Encrypt 或云厂商证书），并把 `proxy_pass` 与 Spring Boot 前再加一层 TLS 终止（常见做法由 Nginx 处理 HTTPS）。

## 6. 后端环境变量（服务器上）

与本地类似，在运行 Jar 的环境中配置：

- `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`
- `APP_CORS_PATTERNS`：若前端与 API **不同域名**，需把前端来源模式配进去；若 **同域反代**（上文示例），浏览器对 API 为同域，CORS 压力较小，但仍可按需收紧。

## 7. 与「每次都要运行才跳出来」的区别

- **开发**：HBuilderX / 浏览器访问 `http://localhost:5173` 等，是临时开发服务器。
- **上线**：把构建产物放到 Nginx（或 OSS）后，由服务器 **7×24 监听 80/443**，用户随时访问你的域名即可。

---

更细的 CodeArts 流水线可把「打包 H5 + 上传服务器」做成自动化，见 `docs/pipeline.md`。
