# 前端 JWT + Spring Security 适配规划

## 摘要

前端按后端现有改造结果统一切到 JWT 鉴权，不再依赖 Cookie，也不再沿用前端内部 0/1/2 角色编码。此次适配分三条主线同步完成：认证与会话层重构、路由与权限模型对
齐、学生在线考试页补齐“暂存/恢复/继续作答”体验。

## 关键改动

### 1. 认证与会话层

- 将登录接口从旧 /login、/logout 切到 /auth/login、/auth/refresh、/auth/logout、/auth/me。
- 登录页改为显式选择角色，登录请求固定为：
  - username: string
  - password: string
  - role: 'ADMIN' | 'TEACHER' | 'STUDENT'
- 新建统一认证会话模型：
  - AuthRole = 'ADMIN' | 'TEACHER' | 'STUDENT'
  - CurrentUser = { userId, username, displayName, role }
  - AuthSession = { accessToken, refreshToken, expiresAt, user, profile? }
- 会话存储改为 sessionStorage，至少拆出：
  - accessToken
  - refreshToken
  - currentUser
  - roleProfile，学生场景下额外缓存 grade/major/institute 等资料
- 删除请求层 withCredentials，统一改为 Authorization: Bearer <accessToken>。
- 请求拦截器改造为：
  - 请求前自动注入 Bearer Token
  - 业务接口 401 时触发单飞刷新
  - 刷新成功后重放原请求
  - /auth/refresh 自身失败时清空本地会话并跳回登录页
  - /auth/login、/auth/refresh、/auth/logout 不进入死循环重试
- 启动恢复流程固定为：
  1. 应用启动读取 sessionStorage
  2. 若有 token，先调 /auth/me
  3. /auth/me 成功后恢复 CurrentUser
  4. 若角色是 STUDENT，再调 /student/{userId} 补齐 grade/major/institute
  5. 任一步失败则清空本地会话并回登录页
- 由于 sessionStorage 不适合依赖当前的 storage 监听，前端会话状态改为单例响应式 auth store 或统一事件总线，不再只靠读写 localStorage 同步。

### 2. 路由、权限与页面基础设施

- 前端内部角色常量、菜单配置、路由 meta.roles、首页跳转逻辑全部改成后端枚举角色，不再保留 0/1/2 语义。
- 路由守卫新增“认证恢复中”状态，避免刷新页面时先被误判为未登录再跳回登录页。
- 所有“当前用户”页面都只从认证会话读取 userId，不允许用户侧再手工伪造本人 ID。
- 学生资料页、成绩页继续传当前登录学生 userId 到后端；后端会再次做本人校验，前端不再尝试自行放宽。
- 现有学生考试中心的本地筛选逻辑依赖 grade/major/institute，所以恢复会话后必须先补齐学生 profile，再渲染考试列表；资料未就绪时显示加载态，不显示未筛选的考试
  列表。
- 顶部退出登录统一调用 /auth/logout，请求体带 refreshToken；无论接口是否成功，本地都清空会话。
- 控制台密码修改入口按后端现状收敛：
  - ADMIN 保留已有自助改密能力
  - TEACHER 暂时隐藏该入口，直到后端提供明确的教师自助改密接口
  - STUDENT 继续走现有学生改密页
- 前端仓库内所有仍写着 Cookie / withCredentials 的文档与说明同步更新，避免后续联调继续按旧方案操作。

### 3. 在线考试页适配

- 学生正式考试流程统一以考试会话接口为准，不再把 /answer/submit 作为正式考试主流程。
- ExamDetail 继续基于 /exam/{examCode}/exam-policy 判断是否允许进入答题页，并明确展示：
  - 未到考试窗口
  - 已过考试窗口
  - 考试已撤销
- AnswerPaper 以 /student/exam/{examCode}/attempt/start 为唯一开场接口：
  - 第一次进入视为开始作答
  - 已有未提交记录则视为恢复作答
  - 页面初始化只信任该接口返回的 paper、answers、windowEndAt、serverTime
- 答题页补齐明确的暂存体验：
  - 输入后防抖调用 /attempt/answers
  - 显示“保存中 / 已暂存 / 暂存失败 / 最近暂存时间”
  - 路由离开、页面隐藏、刷新前做 best-effort flush
- 恢复作答体验固定为：
  - 刷新页面后自动重新调用 /attempt/start
  - 用后端返回的 answers 恢复界面状态
  - 剩余时间按 serverTime 和 windowEndAt 重新计算
- 提交流程固定为：
  - 先 flush 一次暂存
  - 再调 /attempt/submit
  - 成功后跳成绩页并展示本次得分
  - 若后端返回已过期、已提交、已撤销等错误，直接按后端 message 呈现，不做前端伪兜底
- 若后续页面仍保留 /answer/submit，只允许作为非正式会话场景的兼容入口，不在学生正式考试主路径中调用。

## 重要接口与类型对齐

- 登录请求改为：

  {
  "username": "10001",
  "password": "123456",
  "role": "STUDENT"
  }
- 登录成功响应以 data.accessToken、data.refreshToken、data.expiresIn、data.user 为准，不再从旧登录返回结构推断账号信息。
- /auth/me 只负责恢复当前登录身份摘要，不承担学生详细资料恢复。
- 路由和菜单角色值统一替换为 ADMIN、TEACHER、STUDENT。

## 测试方案

- 认证链路
  - 管理员、教师、学生三类账号均可登录成功
  - 刷新浏览器后可通过 /auth/me 自动恢复
  - access token 失效后可自动刷新并重放请求
  - refresh token 失效后应清空会话并回登录页
  - 退出登录后再次访问受保护路由会被拦截
- 权限链路
  - 学生不能进入控制台路由
  - 管理员和教师按各自菜单访问，越权路由会被重定向
  - 学生资料页、成绩页刷新后仍能正确加载本人数据
- 在线考试链路
  - 进入答题页成功触发 /attempt/start
  - 作答后自动触发 /attempt/answers
  - 刷新页面后答案可恢复
  - 超时、撤销、非考试窗口时不可继续作答
  - 提交后跳成绩页，且重复进入时按后端错误提示处理
- 回归检查
  - 管理端学生/教师/考试/题库/成绩页面请求都已自动带 Bearer Token
  - 前端构建通过，核心页面无旧 Cookie 依赖残留

## 假设与默认值

- 默认按后端联调文档要求，token 存 sessionStorage，不做跨浏览器重启持久登录。
- 默认前端内部角色模型完全切到后端枚举，不保留 0/1/2 兼容层。
- 默认登录页显式要求用户选择角色，不做多角色自动试探登录。
- 默认学生考试中心继续保留“只显示匹配考试”的前端筛选，因此会话恢复后必须额外补齐学生 profile。
- 默认教师自助改密不在本轮前端适配范围内，先隐藏入口，避免前端暴露一个后端未落地的能力。
