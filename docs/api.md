# 接口说明（骨架）

统一响应结构：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

`code === 0` 表示成功；非 0 为业务或错误码（后续扩展）。

## 认证（当前为内存用户，便于联调）

请求需登录的接口时，在 Header 携带：`Authorization: Bearer <token>`。

### 注册

- **路径**：`POST /api/auth/register`
- **Body**：`{ "username": "string", "password": "string", "phone": "可选 11 位" }`
- **成功**：`data` 内含 `token` 与 `user`（与登录相同）

### 登录

- **路径**：`POST /api/auth/login`
- **Body**：`{ "username": "string", "password": "string" }`
- **成功示例**：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "token": "……",
    "user": {
      "username": "demo",
      "nickname": "演示用户",
      "phoneMasked": "138****8000",
      "creditScore": 100
    }
  }
}
```

### 当前用户

- **路径**：`GET /api/auth/me`
- **Header**：`Authorization: Bearer <token>`

### 内置体验账号

- 用户名：`demo`，密码：`demo123`

## 联通测试

- **路径**：`GET /api/hello`
- **说明**：健康检查 / 前后端联通
- **响应示例**：

```json
{
  "code": 0,
  "message": "ok",
  "data": "后端连接成功！二手交易平台正式启动！"
}
```
