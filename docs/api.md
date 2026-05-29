# API 接口说明

后端项目位于 `shopping_back/shopping_back/`，当前接口以 Spring Boot 内存模拟数据为主，后续可替换为 MySQL 持久化。

前端统一请求封装位于 `shopping_front/utils/request.js`，业务服务层位于：

- `shopping_front/services/auth.js`
- `shopping_front/services/center.js`
- `shopping_front/services/shop.js`

其中 `services/shop.js` 负责商品、店铺、话题、订单、审核和 AI 议价相关接口，页面可在接口失败时保留本地演示数据兜底。

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

前端接入页面：

- `shopping_front/pages/home/home.vue`
- `shopping_front/pages/store/store.vue`

首页会优先读取该接口，失败时回退到 `shopping_front/data/catalog.js`，以保证课堂演示不被本地后端状态阻断。

### 商品详情

- Method: `GET`
- URL: `/api/products/{id}`

前端接入页面：`shopping_front/pages/goods/detail.vue`

详情页兼容两种参数结构：

- 本地演示数据：`params: [["品牌", "AirWave"]]`
- 后端 DTO：`params: [{ "key": "品牌", "value": "AirWave" }]`

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

前端接入页面：`shopping_front/pages/store/store.vue`

### 话题列表

- Method: `GET`
- URL: `/api/topics`

## 订单接口

### 订单列表

- Method: `GET`
- URL: `/api/orders`

当前返回模拟订单，用于订单列表、物流和评价页面演示。

前端接入页面：`shopping_front/pages/order/list.vue`

返回字段：

- `id`
- `shop`
- `status`: 如 `待收货`、`待评价`
- `title`
- `cover`
- `type`: `新品` 或 `二手`
- `service`
- `amount`

## 角色中心接口

角色中心接口需要登录态，请求头携带 `Authorization: Bearer <token>`。

### 买家中心

- Method: `GET`
- URL: `/api/center/buyer`

用于个人中心展示账号信息、信用分、收藏、足迹、关注店铺、实名认证状态等。

### 卖家中心

- Method: `GET`
- URL: `/api/center/seller`

用于卖家工作台展示店铺状态、待发货订单、销售额、实名状态和店铺信用。

### 管理员中心

- Method: `GET`
- URL: `/api/center/admin`

用于管理员工作台展示用户状态、实名审核、信用调整等管理数据。

### 买家互动记录

- Method: `GET`
- URL: `/api/center/buyer/items/{type}`

`type` 可使用：

- `favorite`
- `history`
- `follow`
- `credit`

新增记录：

- Method: `POST`
- URL: `/api/center/buyer/items/{type}`

清空记录：

- Method: `PUT`
- URL: `/api/center/buyer/items/{type}/clear`

### 实名认证模拟

买家提交实名：

- Method: `POST`
- URL: `/api/center/buyer/realname`

买家取消实名：

- Method: `PUT`
- URL: `/api/center/buyer/realname/cancel`

卖家提交实名：

- Method: `POST`
- URL: `/api/center/seller/realname`

卖家取消实名：

- Method: `PUT`
- URL: `/api/center/seller/realname/cancel`

## 管理审核接口

### 审核列表

- Method: `GET`
- URL: `/api/admin/audit`

前端接入页面：`shopping_front/pages/admin/audit.vue`

当前返回待审核商品演示列表，页面支持通过/拒绝后从当前列表移除。

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

前端接入页面：`shopping_front/pages/message/message.vue`

当前实现为规则模拟，不依赖真实大模型，适合课程展示；后续如接入真实模型，应保持返回字段不变。

## 联调验收建议

按六条业务路径验收：

1. 登录/注册后进入个人中心，检查信用分、实名状态、收藏/足迹/关注模块。
2. 首页和发现页可以浏览商品、故事、店铺，并能跳转详情和店铺。
3. 商品详情能展示参数、评价、信用标签和二手时间线。
4. 购物车能进入确认订单，订单列表能从 `/api/orders` 获取模拟订单。
5. 发布页能提交商品，审核页能从 `/api/admin/audit` 获取列表并模拟通过/拒绝。
6. 消息页能调用 `/api/ai/assist` 展示 AI 回复、验货清单和交易共识。

## 文档同步要求

修改后端接口、DTO、字段名、路径或返回结构时，必须同步更新本文档。
