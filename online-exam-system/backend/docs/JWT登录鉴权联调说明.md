# JWT 登录鉴权联调说明

## 后端认证接口

- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /auth/me`

统一返回仍为：

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {}
}
```

## 登录请求

```json
{
  "username": "10001",
  "password": "123456",
  "role": "ADMIN"
}
```

`role` 取值：

- `ADMIN`
- `TEACHER`
- `STUDENT`

## 登录成功响应

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {
    "accessToken": "jwt-access-token",
    "refreshToken": "jwt-refresh-token",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "userId": 10001,
      "username": "10001",
      "displayName": "张三",
      "role": "STUDENT"
    }
  }
}
```

## 前端改造点

- 不再依赖 `rb_token` 和 `rb_role` Cookie。
- 登录成功后把 `accessToken`、`refreshToken`、`user` 保存到 `sessionStorage`。
- 业务请求统一加请求头：`Authorization: Bearer <accessToken>`。
- 接口返回 `401` 时，使用 `refreshToken` 调用 `/auth/refresh`，成功后重放原请求。
- `/auth/logout` 请求体携带当前 `refreshToken`，无论后端是否成功，前端都应清空本地登录态。

## 当前权限约束

- `ADMIN`：管理员、教师、学生账号管理；考试撤销。
- `ADMIN` / `TEACHER`：题库、试卷、考试管理；冻结试卷查看；成绩总览与统计。
- `STUDENT`：学生考试会话、直接交卷；本人资料与本人成绩查询/改密。

## 迁移提示

- 旧 `/login`、`/logout` Cookie 登录流已经被 `/auth/*` 替代。
- 数据库需先执行 [auth_security_upgrade.sql](/D:/test/online-exam-system/backend/sql/auth_security_upgrade.sql) 创建 `auth_refresh_token` 表。
- 现有账号密码需要先迁移为 BCrypt 哈希后，再使用新的 `/auth/login`。
