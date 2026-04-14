# OnlineExamSystem Backend 功能测试方案

## 1. 文档目的

本文用于指导当前后端项目在本地环境下开展功能测试，覆盖以下已实现模块：

- 登录与登出：`LoginController`
- 管理员管理：`AdminController`
- 教师管理：`TeacherController`
- 学生管理：`StudentController`

目标是通过统一步骤验证：

- 接口可用性（路由、请求参数、返回结构）
- 业务正确性（新增/修改/删除/查询行为）
- 数据一致性（接口返回与数据库数据一致）
- 错误处理行为（错误参数、错误账号、不存在数据）

## 2. 当前基线信息

- 项目路径：`D:\test\online-exam-system\backend`
- 数据库脚本：`online_exam.sql`
- 服务端口：`8080`
- 数据库连接：`jdbc:mysql://localhost:3306/online_exam`
- 返回结构：`ApiResult{ code, message, data }`
- 登录 Cookie：`rb_token`、`rb_role`

说明：

- 当前代码里仅写入登录 Cookie，但尚未发现已启用的 `LoginInterceptor` 拦截链路，因此本阶段“权限拒绝”测试应标记为待实现或预期失败项。

## 3. 测试范围与不在范围

### 3.1 在范围

- `/login`、`/logout`
- `/admins`、`/admin/**`
- `/teachers/**`、`/teacher/**`
- `/students/**`、`/student/**`、`/studentPWD`

### 3.2 不在范围

- 考试、试卷、题库相关接口（`ExamManage/Paper/MultiQuestion/FillQuestion/JudgeQuestion`）
- 前端页面 UI 细节
- 性能压测与安全渗透测试

## 4. 测试前准备

## 4.1 环境准备

- JDK 17
- Maven 3.8+
- MySQL 8.x
- 推荐工具：Postman 或 Apifox（支持 Cookie 自动管理）
- 可选工具：`curl` + `cookies.txt`

## 4.2 数据库初始化

在 MySQL 中执行以下步骤（建议先重建库，避免脏数据）：

```sql
DROP DATABASE IF EXISTS online_exam;
CREATE DATABASE online_exam DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE online_exam;
SOURCE D:/test/online-exam-system/backend/online_exam.sql;
```

## 4.3 初始化校验 SQL

```sql
USE online_exam;
SHOW TABLES;
SELECT COUNT(*) AS admin_count FROM admin;
SELECT COUNT(*) AS teacher_count FROM teacher;
SELECT COUNT(*) AS student_count FROM student;
```

关键测试账号（来自 `online_exam.sql`，密码通常为 `123456`）：

- 管理员：`adminId=9991`
- 教师：`teacherId=20081001`
- 学生：`studentId=20224001`

## 4.4 启动与冒烟

```powershell
cd D:\test\online-exam-system\backend
mvn -q -DskipTests compile
mvn spring-boot:run
```

冒烟检查：

- 控制台无启动异常
- 访问不存在路由应返回统一错误 JSON（由全局异常处理器接管）

## 5. 接口总览（本阶段）

| 模块 | 方法 | 路径 |
|---|---|---|
| 登录 | POST | `/login` |
| 登出 | POST | `/logout` |
| 管理员 | GET | `/admins` |
| 管理员 | GET | `/admin/{adminId}` |
| 管理员 | POST | `/admin` |
| 管理员 | PUT | `/admin/{adminId}` |
| 管理员 | DELETE | `/admin/{adminId}` |
| 管理员 | GET | `/admin/resetPsw/{adminId}/{oldPsw}/{newPsw}` |
| 教师 | GET | `/teachers/{page}/{size}` |
| 教师 | GET | `/teacher/{teacherId}` |
| 教师 | POST | `/teacher` |
| 教师 | PUT | `/teacher` |
| 教师 | DELETE | `/teacher/{teacherId}` |
| 学生 | GET | `/students/{page}/{size}/{name}/{grade}/{tel}/{institute}/{major}/{clazz}` |
| 学生 | GET | `/student/{studentId}` |
| 学生 | POST | `/student` |
| 学生 | PUT | `/student` |
| 学生 | PUT | `/studentPWD` |
| 学生 | DELETE | `/student/{studentId}` |

## 6. 执行策略

推荐按以下顺序执行，减少定位成本：

1. 登录与登出
2. 管理员模块
3. 教师模块
4. 学生模块
5. 数据库一致性复核
6. 回归冒烟

每个测试用例都记录：

- 请求参数
- 响应体（至少 `code`、`data`）
- 关键 SQL 校验结果
- 是否通过
- 缺陷编号（如失败）

## 7. 详细测试用例

## 7.1 登录模块

### L-001 管理员登录成功

- 前置条件：`admin.adminId=9991` 且密码正确
- 请求：

```http
POST /login
Content-Type: application/json

{
  "username": 9991,
  "password": "123456"
}
```

- 预期：
  - 响应 `code=200`
  - `data` 为管理员信息对象
  - 响应头包含 `Set-Cookie: rb_token=...`、`Set-Cookie: rb_role=0`

### L-002 教师登录成功

- 请求账号示例：`20081001 / 123456`
- 预期：`code=200`，`rb_role=1`

### L-003 学生登录成功

- 请求账号示例：`20224001 / 123456`
- 预期：`code=200`，`rb_role=2`

### L-004 密码错误

- 请求：合法账号 + 错误密码
- 预期：`code=400`，`data=null`

### L-005 账号不存在

- 请求：不存在的 ID（如 `99999999`）
- 预期：`code=400`，`data=null`

### L-006 username 非数字类型

- 请求示例：`"username": "abc"`
- 预期：反序列化失败，返回 4xx 或统一异常 JSON（记录实际行为）

### L-007 缺少 password 字段

- 请求示例仅传 `username`
- 预期：登录失败（记录返回码与异常消息）

### L-008 登出

- 请求：

```http
POST /logout
```

- 预期：
  - 返回成功响应
  - `rb_token`、`rb_role` 被清除（`Max-Age=0`）

## 7.2 管理员模块

### A-001 查询管理员列表

- 请求：`GET /admins`
- 预期：
  - `code=200`
  - `data` 为数组
  - 与 SQL `SELECT COUNT(*) FROM admin;` 量级一致

### A-002 按 ID 查询管理员

- 请求：`GET /admin/9991`
- 预期：`code=200`，`data.adminId=9991`

### A-003 查询不存在管理员

- 请求：`GET /admin/999999`
- 预期：应返回空对象或错误（记录当前实际行为）

### A-004 删除管理员

- 步骤：
  - 先插入一条测试管理员
  - 调用 `DELETE /admin/{id}`
  - SQL 校验数据已删除
- 预期：`code=200`

### A-005 删除不存在管理员

- 请求：`DELETE /admin/999999`
- 预期：返回成功或受影响行数为 0（记录当前行为）

### A-006 新增管理员

- 请求：

```http
POST /admin
Content-Type: application/json

{
  "adminName": "test_admin",
  "sex": "M",
  "tel": "13800000000",
  "email": "test_admin@example.com",
  "pwd": "123456",
  "cardId": "123456789012345678",
  "role": "0"
}
```

- 预期：
  - 业务上应新增成功
  - SQL：`SELECT * FROM admin WHERE adminName='test_admin';` 应查到记录
- 备注：若接口返回成功但 DB 未新增，记为缺陷

### A-007 修改管理员

- 请求：`PUT /admin/{adminId}`，Body 携带完整管理员字段
- 预期：`code=200` 且 SQL 数据确实变更

### A-008 重置密码成功（管理员自身）

- 请求：`GET /admin/resetPsw/9991/123456/654321`
- 预期：`code=200`，返回 `true`；SQL 中 `pwd` 已更新

### A-009 重置密码失败（旧密码错误）

- 请求：`GET /admin/resetPsw/9991/wrong/654321`
- 预期：`code=200`，`data` 为失败提示

### A-010 重置密码（教师 ID 路径）

- 请求：`GET /admin/resetPsw/20081001/123456/654321`
- 预期：可更新教师密码（用于验证跨角色逻辑）

## 7.3 教师模块

### T-001 教师分页查询

- 请求：`GET /teachers/1/10`
- 预期：
  - `code=200`
  - `data.records` 存在
  - `data.current=1`、`data.size=10`

### T-002 按 ID 查询教师

- 请求：`GET /teacher/20081001`
- 预期：`code=200`，返回教师对象

### T-003 新增教师

- 请求：

```http
POST /teacher
Content-Type: application/json

{
  "teacherName": "test_teacher",
  "institute": "SE",
  "sex": "M",
  "tel": "13900000000",
  "email": "test_teacher@example.com",
  "pwd": "123456",
  "cardId": "987654321098765432",
  "type": "lecturer"
}
```

- 预期：
  - `code=200`
  - SQL 新增成功
  - `role` 自动为 `"1"`

### T-004 修改教师

- 请求：`PUT /teacher`
- 预期：`code=200`，SQL 对应字段更新

### T-005 删除教师

- 请求：`DELETE /teacher/{teacherId}`
- 预期：`code=200`，SQL 无该记录

### T-006 删除不存在教师

- 请求：`DELETE /teacher/29999999`
- 预期：受影响行数可为 0，接口正常返回

## 7.4 学生模块

### S-001 学生分页查询（全量）

- 请求：`GET /students/1/10/@/@/@/@/@/@`
- 说明：`@` 在服务层被替换为空字符串，表示不过滤
- 预期：
  - `code=200`
  - `data.records` 返回分页数据

### S-002 学生分页查询（带过滤）

- 请求示例：`GET /students/1/10/@/2023/@/@/软件工程/@`
- 预期：结果均满足过滤条件

### S-003 按 ID 查询学生（存在）

- 请求：`GET /student/20224001`
- 预期：`code=200`，`data.studentId` 匹配

### S-004 按 ID 查询学生（不存在）

- 请求：`GET /student/20999999`
- 预期：`code=404`，`data=null`

### S-005 新增学生

- 请求：

```http
POST /student
Content-Type: application/json

{
  "studentName": "test_student",
  "grade": "2023",
  "major": "SE",
  "clazz": "1",
  "institute": "SE",
  "tel": "13700000000",
  "email": "test_student@example.com",
  "pwd": "123456",
  "cardId": "112233445566778899",
  "sex": "M",
  "role": "2"
}
```

- 预期：`code=200`，SQL 新增成功

### S-006 更新学生信息

- 请求：`PUT /student`
- 预期：`code=200`，SQL 字段更新

### S-007 更新学生密码

- 请求：`PUT /studentPWD`

```json
{
  "studentId": "20224001",
  "pwd": "654321"
}
```

- 预期：`code=200`，SQL 密码更新

### S-008 删除学生

- 请求：`DELETE /student/{studentId}`
- 预期：`code=200`，SQL 无该记录

### S-009 路径过滤参数边界

- 请求：`GET /students/0/0/@/@/@/@/@/@`、`GET /students/-1/10/...`
- 预期：返回合理错误或空分页，不能导致服务异常

### S-010 非法 studentId 类型

- 请求：`GET /student/abc`
- 预期：参数类型错误，返回 4xx 或统一异常 JSON

## 8. 数据库一致性校验模板

每执行一条新增/更新/删除接口后，至少执行一条对应 SQL：

```sql
-- 新增校验
SELECT * FROM teacher WHERE teacherName = 'test_teacher';

-- 更新校验
SELECT pwd FROM student WHERE studentId = 20224001;

-- 删除校验
SELECT COUNT(*) FROM admin WHERE adminId = 99999;
```

校验原则：

- 接口返回成功但 SQL 未变化，记为严重缺陷
- 接口返回失败但 SQL 已变化，记为严重缺陷

## 9. Cookie 与权限专项（当前阶段）

当前阶段专项检查点：

- 登录成功后，浏览器/工具内是否保存 `rb_token`、`rb_role`
- 登出后 Cookie 是否被清除
- 未携带 Cookie 访问业务接口时是否被拦截

判定说明：

- 若“未携带 Cookie 仍可访问业务接口”，在当前代码状态下可能是预期（尚未接入拦截器）；需记录为“权限链路待实现”而非“功能通过”。

## 10. 已知高风险点（建议重点验证）

以下点来自当前源码实现，测试时建议重点观察：

- 管理员新增接口是否真实入库
- 管理员按 ID 查询是否出现参数绑定异常
- 管理员更新接口是否出现字段绑定异常
- 学生筛选接口必须传满 8 个路径参数，且通配符需使用 `@`
- 登录 `username` 仅接受数字 ID（整数）

## 11. 缺陷记录模板

```text
缺陷ID：
标题：
环境：
接口：
请求数据：
实际结果：
预期结果：
数据库核查结果：
日志片段：
严重级别（P0/P1/P2/P3）：
复现步骤：
```

## 12. 回归用例最小集合（每次改动后执行）

1. `L-001` 管理员登录成功
2. `L-004` 登录失败（错误密码）
3. `A-001` 管理员列表查询
4. `T-001` 教师分页查询
5. `S-001` 学生分页查询（`@` 全量）
6. `S-007` 学生密码更新
7. `L-008` 登出

---

执行建议：

- 第一轮先跑“主路径 + 入库校验”，快速发现阻断问题。
- 第二轮跑“异常路径 + 边界值”，补齐鲁棒性结论。
- 每发现缺陷后，先复现并固化请求样例，再进入修复流程。
