# CodeArts 流水线

当前先用最小可过版本。

## 入口

使用 `.cloudbuild/build.yml`

## 逻辑

1. 检出代码
2. 执行前端 H5 构建
3. 执行后端 `package -DskipTests`
4. 不跑 Docker、Compose、E2E

## 必需变量

- `CI_DB_PASSWORD`
- `CI_MYSQL_ROOT_PASSWORD`
