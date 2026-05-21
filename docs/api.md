# API 接口说明

后端项目位于 `shopping_back/shopping_back/`，当前接口以 Spring Boot 内存模拟数据为主，后续可替换为 MySQL 持久化。

## 统一返回格式

所有业务接口统一返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

- `code === 0` 表示成功。
- 非 0 表示失败，`message` 为错误说明。
- 认证接口需要在请求头携带 `Authorization: Bearer <token>`。

## 认证接口

### 注册

- Method: `POST`
- URL: `/api/auth/register`
- Body:

```json
{
  "username": "user1",
  "password": "123456",
  "phone": "13800138000"
}
```

### 登录

- Method: `POST`
- URL: `/api/auth/login`
- Body:

```json
{
  "username": "demo",
  "password": "demo123"
}
```

演示账号：

- 用户名：`demo`
- 密码：`demo123`

### 当前用户

- Method: `GET`
- URL: `/api/auth/me`
- Header: `Authorization: Bearer <token>`

返回字段：

```json
{
  "username": "demo",
  "nickname": "松果演示用户",
  "phoneMasked": "138****8000",
  "creditScore": 96
}
```

## 商品接口

### 商品列表

- Method: `GET`
- URL: `/api/products`
- Query:
  - `scene`: 可选，`new` / `used` / `all`
  - `keyword`: 可选，搜索标题、分类、店铺等

### 商品详情

- Method: `GET`
- URL: `/api/products/{id}`

商品主要字段：

- `id`
- `scene`: `new` 或 `used`
- `category`
- `title`
- `subtitle`
- `price`
- `originPrice`
- `cover`
- `tag`
- `condition`
- `credit`
- `location`
- `shopName`
- `delivery`
- `service`
- `highlights`
- `story`
- `params`
- `reviews`
- `timeline`
- `aiTips`

### 发布商品

- Method: `POST`
- URL: `/api/products`
- Body:

```json
{
  "title": "27 英寸 2K 显示器",
  "scene": "used",
  "category": "数码影音",
  "price": 680,
  "condition": "9 成新",
  "description": "无坏点，接口齐全",
  "story": "陪我完成毕业设计，现在准备流转",
  "location": "广州大学城"
}
```

当前发布后会进入模拟审核状态。

## 店铺与社区接口

### 店铺列表

- Method: `GET`
- URL: `/api/stores`

### 话题列表

- Method: `GET`
- URL: `/api/topics`

## 订单接口

### 订单列表

- Method: `GET`
- URL: `/api/orders`

当前返回模拟订单，用于订单列表、物流和评价页面演示。

## 管理审核接口

### 审核列表

- Method: `GET`
- URL: `/api/admin/audit`

### 审核操作

- Method: `POST`
- URL: `/api/admin/audit/{id}`
- Body:

```json
{
  "status": "approved"
}
```

`status` 可使用：

- `approved`
- `rejected`

## AI 助手接口

### 商品问答与议价助手

- Method: `POST`
- URL: `/api/ai/assist`
- Body:

```json
{
  "productId": "used-monitor",
  "question": "能便宜一点吗？有没有坏点？",
  "offer": 620
}
```

返回字段：

- `answer`: AI 回复文本
- `checklist`: 验货/交易检查清单
- `consensus`: 交易共识清单

## 文档同步要求

修改后端接口、DTO、字段名、路径或返回结构时，必须同步更新本文档。
