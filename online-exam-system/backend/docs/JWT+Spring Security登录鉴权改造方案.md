# JWT + Spring Security 登录鉴权改造方案

Summary

- 后端从 Cookie + HandlerInterceptor 改为 Spring Security + JWT + BCrypt，认证改为无状态访问令牌，刷新令牌落库管理。
- 前端从“自动带 Cookie”改为“统一注入 Authorization: Bearer <accessToken>”，并接入登录态存储、自动刷新、路由守卫。
- 本次默认采用：accessToken + refreshToken、登录请求显式带 role、密码统一升级为 BCrypt、刷新令牌存 MySQL，不引入 Redis。

Public APIs

- 新增 POST /auth/login，请求体改为 { "username": 10001, "password": "123456", "role": "ADMIN|TEACHER|STUDENT" }。
- 新增 POST /auth/refresh，请求体 { "refreshToken": "..." }，成功后轮换返回新的 accessToken 和 refreshToken。
- 新增 POST /auth/logout，请求体 { "refreshToken": "..." }，后端删除对应 refresh 记录，前端清空本地登录态。
- 新增 GET /auth/me，返回当前登录用户基础信息与角色，供页面刷新后恢复用户态。
- 所有受保护接口统一改为读取 Authorization 头，不再读取 rb_token、rb_role Cookie。
- 旧 POST /login、POST /logout 进入废弃态；若要平滑迁移，可保留 1 个版本周期并在文档中标注 deprecated。

Backend Changes

- pom.xml 增加 spring-boot-starter-security、JWT 库（建议 jjwt）、必要测试依赖。
- 新增安全配置模块，包含 SecurityConfig、JwtAuthenticationFilter、AuthenticationEntryPoint、AccessDeniedHandler、CorsConfigurationSource；移除
  LoginInterceptor 注册，CrossConfig 只保留必要 MVC 配置或拆除。
- 新增统一认证模型：LoginRequest、AuthTokenResponse、RefreshTokenRequest、CurrentUserVO、AccountRole 枚举、LoginUser implements UserDetails。
- 新增账号查询服务，不再用“用户名+密码直接查表”；改为“按 role 查账号 -> 用 BCryptPasswordEncoder.matches 校验密码 -> 组装 Authentication”。
- 登录查询补齐按账号 ID 查询且返回 pwd 的方法；管理员、教师、学生三类账号分别适配到同一套 UserDetailsService/AuthenticationProvider。
- JWT accessToken 载荷固定为：sub=username、uid=业务主键、role=ROLE_ADMIN|ROLE_TEACHER|ROLE_STUDENT、accountType、displayName、tokenType=access。
- JWT refreshToken 载荷固定为：uid、role、jti、tokenType=refresh；只用于换新 token，不用于直接访问业务接口。
- 默认时效：accessToken=15 分钟，refreshToken=7 天；密钥、过期时间、前端来源域名全部放 application.properties/环境变量。
- 新增 auth_refresh_token 表，字段至少包括 id、jti、user_id、role、username、expires_at、revoked、created_at；刷新时执行轮换并废弃旧 token。
- 所有手工读 Cookie 的控制器改为从 Security 上下文取当前人；使用 @AuthenticationPrincipal LoginUser 或统一 CurrentUserService，不再传 HttpServletRequest 解
  析身份。
- 开启 @EnableMethodSecurity，权限默认矩阵固定为：
- permitAll: /auth/login、/auth/refresh、/error、/favicon.ico
- authenticated: /auth/me、/auth/logout、/messages/**、/message/**、/replay/**、/practice/**
- ROLE_ADMIN: 管理员 CRUD、教师/学生账号增删改、考试撤销
- ROLE_ADMIN 或 ROLE_TEACHER: 题库维护、题库聚合删除、试卷管理、自动组卷、考试管理、冻结试卷查看、成绩总览与统计
- ROLE_STUDENT: /student/exam/**、/answer/submit
- ROLE_STUDENT 访问个人资料、改密、个人成绩时只允许本人；ROLE_ADMIN/ROLE_TEACHER 可查任意学生
- 现有“仅学生不可访问”的接口改为显式角色控制，不再依赖 rb_role == 2 的业务判断。
- 所有“客户端可传本人 ID”的接口改为以后端身份为准或做一致性校验；重点修正 /answer/submit、/studentPWD、/score/{studentId}、/student/{studentId}。
- 密码体系同步升级为 BCrypt：新增管理员/教师/学生、修改资料、重置密码、学生改密全部统一 encode 后入库。
- 提供一次性密码迁移脚本到 sql/，把三张账号表现有明文密码批量转为 BCrypt；迁移完成后再切换登录逻辑，避免新老口径混用。
- 统一异常返回保持现有 ApiResult 结构，401/403 分别由 Security 层输出，避免默认 HTML 错误页。
- 更新 docs/前端接口文档.md 与 docs/apifox-openapi.json，删除 Cookie 说明，改为 Bearer Token 与刷新流程说明。

Frontend Changes

- 登录页增加角色选择，提交 { username, password, role } 到 /auth/login。
- 登录成功后保存 accessToken、refreshToken、user 到 sessionStorage；页面初始化先读缓存，再按需调用 /auth/me 校验。
- API 请求层统一加请求拦截器，存在 accessToken 时自动注入 Authorization 头。
- 响应拦截器处理 401：若存在 refreshToken 且本次请求未重试，则调用 /auth/refresh，拿到新 token 后重放原请求；刷新失败则跳转登录页。
- 退出登录时先调 /auth/logout，无论后端是否成功都清空本地 token 和用户信息。
- 路由守卫按 user.role 控制菜单和页面访问；页面展示可按角色裁剪，但真正权限以后端 403 为准。
- 前端不再手工处理 Cookie，也不再依赖 withCredentials；跨域改为普通 Bearer 请求。
- 个人中心、学生成绩页、学生考试页等“本人接口”不再从前端拼任意 studentId；优先从 /auth/me 或登录返回的当前用户信息取值。
- 若现有前端仍依赖旧 /login 返回体，可在过渡期保留 user 字段结构不变，只新增 token 字段，先完成页面改造再切主接口。

Test Plan

- 后端集成测试覆盖：正确登录、错误密码、错误 role、accessToken 访问受保护接口、refresh 成功轮换、logout 后 refresh 失效。
- 后端权限测试覆盖：管理员可管理账号，教师不能撤销考试，学生只能访问 /student/exam/** 和本人数据，学生访问完整试卷/冻结试卷返回 403。
- 后端安全测试覆盖：伪造 studentId 提交答案被拒绝或被忽略、过期 token 返回 401、篡改 token 签名返回 401、被吊销 refreshToken 不可再次使用。
- 密码测试覆盖：新增账号入库为 BCrypt、修改密码后旧密码失效、迁移后旧明文登录不再可用。
- 前端联调覆盖：首次登录、刷新页面恢复登录态、access 过期自动刷新、refresh 过期跳回登录、登出后菜单和接口全部失效。

Assumptions

- 前端是典型 SPA，具备统一请求封装与路由守卫能力；具体是 Vue 还是 React 不影响方案。
- 现阶段不引入单点登录、OAuth2、短信验证码、多端会话管理。
- refreshToken 只在数据库中保存可校验记录，不做设备指纹和 IP 绑定。
- 旧 Cookie 登录方式不再长期兼容，完成联调后应整体切换到 /auth/**。
