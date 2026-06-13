# 用户账号与个人中心模块交付说明

## 负责范围

本次开发主要负责平台的用户账号体系、分角色登录注册、买家/卖家/管理员个人中心、实名认证模拟、信用分展示与管理员维护，以及买家收藏/足迹/关注店铺等互动数据的数据库同步。

## 已完成内容

### 1. 分角色登录注册

- 登录支持三类角色账号：买家、卖家、管理员。
- 注册页面支持用户自行选择买家或卖家。
- 管理员不开放注册，由系统预置账号登录。
- 登录成功后会根据角色进入不同端：
  - 买家：进入默认买家端页面。
  - 卖家：进入卖家工作台。
  - 管理员：进入管理员工作台。
- 未登录时默认展示买家端浏览体验。

### 2. 预置演示账号

后端启动时会自动保证以下账号存在：

| 角色 | 用户名 | 密码 | 说明 |
| --- | --- | --- | --- |
| 买家 | `demo` | `demo123` | 买家演示账号 |
| 卖家 | `seller` | `seller123` | 卖家演示账号 |
| 管理员 | `admin` | `admin123` | 管理员预置账号 |

### 3. 买家个人中心

买家“我的”页面已改为数据库驱动，主要展示 4 个模块：

- 账号信息：展示昵称、手机号脱敏、账号状态。
- 信用分：读取 `users.credit`，并展示信用分变动记录。
- 我的互动：展示收藏、浏览足迹、关注店铺数量与明细。
- 实名认证：支持提交实名、查看脱敏姓名和身份证号、取消认证。

买家互动数据不再使用手动“添加演示”按钮，而是通过真实页面行为产生：

- 首页/商品详情点击收藏，写入收藏表。
- 打开商品详情时自动记录浏览足迹。
- 店铺页点击关注店铺，写入关注店铺表。

### 4. 卖家个人中心

卖家工作台已独立成卖家角色页面，保持与买家端统一的简洁风格，主要展示 4 个模块：

- 店铺信息：展示店铺名称、店铺状态等信息。
- 商品概览：统计卖家商品数量与待审核商品。
- 订单概览：统计待发货订单与累计成交金额。
- 店铺信用：读取 `users.credit`，与管理员信用分调整同步。

卖家也增加了和买家一致的实名认证模拟功能：

- 可进入实名认证板块。
- 可提交实名信息。
- 可查看脱敏姓名和身份证号。
- 可取消认证。

### 5. 管理员工作台

管理员端已独立成管理员角色页面，包含平台概览、审核、用户管理、我的等区域。

用户管理能力包括：

- 查看用户列表。
- 冻结/解冻用户。
- 给买家和卖家增加或扣减信用分。
- 删除普通用户。

删除用户功能有保护规则：

- 不能删除 `demo`。
- 不能删除 `seller`。
- 不能删除 `admin`。
- 前端不显示这三个账号的删除按钮。
- 后端也会强制校验，防止绕过前端直接调用接口删除。

信用分维护规则：

- 买家和卖家不能自己加减信用分。
- 信用分只能由管理员在管理员端调整。
- 信用分范围限制在 `0-100`。
- 每次调整都会写入 `credit_record`，买家可在信用分记录中查看。

### 6. 实名认证审核

实名认证数据统一写入 `user_realname_auth`。

买家和卖家提交后进入待审核状态，管理员可以：

- 通过实名认证。
- 驳回实名认证。
- 查看待处理实名申请。

实名信息展示时会进行脱敏处理，不直接展示完整身份证号。

### 7. 前端体验重构

本次前端从偏移动端小程序风格，调整为更适合网页演示的角色化页面结构：

- 登录、注册页面改为中文界面。
- 注册页面增加买家/卖家角色选择。
- 买家、卖家、管理员登录后进入不同页面。
- 顶部导航和底部角色导航风格统一。
- 首页商品卡片增加收藏和进店入口。
- 商品详情页增加收藏入口。
- 店铺页增加关注店铺入口。
- 整体视觉采用简约、留白、绿色系、高级感方向。

## 后端新增/调整接口

### 个人中心接口

| 方法 | 地址 | 说明 |
| --- | --- | --- |
| `GET` | `/api/center/buyer` | 获取买家中心数据 |
| `GET` | `/api/center/seller` | 获取卖家中心数据 |
| `GET` | `/api/center/admin` | 获取管理员中心数据 |

### 实名认证接口

| 方法 | 地址 | 说明 |
| --- | --- | --- |
| `POST` | `/api/center/buyer/realname` | 买家提交实名认证 |
| `PUT` | `/api/center/buyer/realname/cancel` | 买家取消实名认证 |
| `POST` | `/api/center/seller/realname` | 卖家提交实名认证 |
| `PUT` | `/api/center/seller/realname/cancel` | 卖家取消实名认证 |
| `PUT` | `/api/center/admin/realname/{id}/approve` | 管理员通过实名审核 |
| `PUT` | `/api/center/admin/realname/{id}/reject` | 管理员驳回实名审核 |

### 买家互动接口

| 方法 | 地址 | 说明 |
| --- | --- | --- |
| `GET` | `/api/center/buyer/items/{type}` | 查询信用记录、收藏、足迹、关注店铺 |
| `POST` | `/api/center/buyer/items/{type}` | 写入收藏、足迹、关注店铺 |
| `PUT` | `/api/center/buyer/items/{type}/clear` | 清空指定类型互动记录 |

`type` 可选值：

- `credit`：信用分记录。
- `favorite`：收藏。
- `history`：浏览足迹。
- `follow`：关注店铺。

### 管理员用户管理接口

| 方法 | 地址 | 说明 |
| --- | --- | --- |
| `PUT` | `/api/center/admin/users/{userId}/status` | 冻结或解冻用户 |
| `PUT` | `/api/center/admin/users/{userId}/credit` | 管理员调整买家/卖家信用分 |
| `DELETE` | `/api/center/admin/users/{userId}` | 管理员删除普通用户 |

## 数据库相关表

本模块主要使用或新增以下数据表：

| 表名 | 作用 |
| --- | --- |
| `users` | 用户基础信息、角色、状态、信用分 |
| `user_realname_auth` | 实名认证模拟数据 |
| `favorite_goods` | 买家收藏商品 |
| `browse_history` | 买家浏览足迹 |
| `follow_store` | 买家关注店铺 |
| `credit_record` | 信用分变动记录 |
| `store` | 卖家店铺信息 |
| `orders` | 卖家订单概览统计 |

后端启动时会自动补充本模块需要的部分表结构和字段，例如收藏标题、足迹标题、关注店铺名称等。

## 主要修改文件

### 前端

- `shopping_front/pages/auth/login.vue`
- `shopping_front/pages/auth/register.vue`
- `shopping_front/pages/user/index.vue`
- `shopping_front/pages/seller/dashboard.vue`
- `shopping_front/pages/admin/dashboard.vue`
- `shopping_front/pages/home/home.vue`
- `shopping_front/pages/goods/detail.vue`
- `shopping_front/pages/store/store.vue`
- `shopping_front/services/center.js`
- `shopping_front/utils/auth.js`
- `shopping_front/utils/request.js`
- `shopping_front/pages.json`

### 后端

- `shopping_back/shopping_back/src/main/java/com/example/shopping_back/auth/AuthService.java`
- `shopping_back/shopping_back/src/main/java/com/example/shopping_back/auth/dto/AuthUserView.java`
- `shopping_back/shopping_back/src/main/java/com/example/shopping_back/auth/dto/RegisterRequest.java`
- `shopping_back/shopping_back/src/main/java/com/example/shopping_back/auth/mapper/UserMapper.java`
- `shopping_back/shopping_back/src/main/java/com/example/shopping_back/auth/model/StoredUser.java`
- `shopping_back/shopping_back/src/main/java/com/example/shopping_back/center/CenterController.java`
- `shopping_back/shopping_back/src/main/java/com/example/shopping_back/center/CenterService.java`
- `shopping_back/shopping_back/src/main/java/com/example/shopping_back/center/CenterMapper.java`
- `shopping_back/shopping_back/src/main/java/com/example/shopping_back/center/CenterDtos.java`
- `shopping_back/shopping_back/doc/db.sql`

## 建议测试场景

### 登录注册

- 使用 `demo/demo123` 登录买家端。
- 使用 `seller/seller123` 登录卖家端。
- 使用 `admin/admin123` 登录管理员端。
- 注册买家账号，确认登录后进入买家端。
- 注册卖家账号，确认登录后进入卖家端。
- 尝试重复注册同名账号，确认提示失败。
- 未登录访问个人中心，确认要求登录。

### 买家个人中心

- 登录买家账号后进入“我的”，确认展示昵称、脱敏手机号、信用分。
- 打开商品详情，确认浏览足迹被记录。
- 在首页或详情页收藏商品，确认收藏夹出现记录。
- 进入店铺页关注店铺，确认关注店铺出现记录。
- 进入实名认证板块，提交实名信息。
- 提交后查看脱敏姓名和身份证号。
- 点击取消认证，确认认证信息被清除。

### 卖家工作台

- 登录卖家账号，确认进入卖家工作台。
- 查看店铺信息、商品概览、订单概览、店铺信用。
- 提交卖家实名认证。
- 查看脱敏实名信息。
- 取消卖家实名认证。

### 管理员工作台

- 登录管理员账号，确认进入管理员工作台。
- 在审核页通过或驳回实名认证。
- 在用户页冻结/解冻普通用户。
- 对买家或卖家执行信用分 `+5` / `-5`，确认数据库和页面同步。
- 确认 `demo`、`seller`、`admin` 不显示删除按钮。
- 注册一个新账号后，在管理员端删除该账号。
- 删除后刷新用户列表，确认该用户消失。

## 启动与检查

### 后端启动

```powershell
cd shopping_back/shopping_back
.\mvnw.cmd spring-boot:run
```

### 前端启动

使用 HBuilderX 打开项目下的 `shopping_front` 目录。

然后运行到浏览器或 H5 预览。

### 本次收尾检查结果

已执行以下检查：

```text
pages.json ok
后端 .\mvnw.cmd -q -DskipTests compile 通过
```

说明当前前端页面配置可解析，后端 Java 编译通过。

## 当前交付结论

本次负责的注册、登录、个人中心、信用分展示、实名认证模拟、用户状态维护、买家互动记录、管理员信用分维护和删除用户功能已经完成。相关数据均通过后端接口与 MySQL 数据库同步，不再只是前端静态展示。
