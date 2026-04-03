# Apifox 测试用例清单（可执行版）

## 1. 适用范围

本清单用于当前后端项目的功能测试执行，覆盖以下接口分组：

- Login
- Admin
- Teacher
- Student

配套文档：

- 详细测试方案：[backend-functional-test-plan.md](./backend-functional-test-plan.md)
- 接口导入文件：[apifox-openapi.json](./apifox-openapi.json)

## 2. Apifox 执行前配置

1. 环境变量：`baseUrl = http://127.0.0.1:8080`
2. 请求地址统一写为：`{{baseUrl}}/xxx`
3. 登录接口先执行一次，让 Apifox 自动保存 `rb_token` 和 `rb_role` Cookie
4. 每条新增/更新/删除用例执行后，按清单内 SQL 到数据库核验

## 3. 通用断言脚本模板（Apifox Tests）

### 3.1 通用成功断言

```javascript
const body = pm.response.json();
pm.test("HTTP 200", () => pm.response.to.have.status(200));
pm.test("业务 code = 200", () => pm.expect(body.code).to.eql(200));
pm.test("包含 message 字段", () => pm.expect(body).to.have.property("message"));
```

### 3.2 通用失败断言（示例）

```javascript
const body = pm.response.json();
pm.test("业务失败", () => pm.expect(body.code).to.not.eql(200));
```

### 3.3 登录后 Cookie 断言（示例）

```javascript
pm.test("写入 rb_token", () => {
  pm.expect(pm.cookies.has("rb_token")).to.eql(true);
});
pm.test("写入 rb_role", () => {
  pm.expect(pm.cookies.has("rb_role")).to.eql(true);
});
```

## 4. 用例清单

字段说明：

- `优先级`：P0（阻断）/ P1（高）/ P2（中）
- `SQL核验`：写明执行完接口后需要人工执行的 SQL

| 用例ID | 分组 | 优先级 | 方法 | 路径 | 场景说明 | 请求体/参数摘要 | 断言重点 | SQL核验 |
|---|---|---|---|---|---|---|---|---|
| L-001 | Login | P0 | POST | `/login` | 管理员登录成功 | `{"username":9991,"password":"123456"}` | `code=200`，写入 `rb_token`，`rb_role=0` | 无 |
| L-002 | Login | P0 | POST | `/login` | 教师登录成功 | `{"username":20081001,"password":"123456"}` | `code=200`，`rb_role=1` | 无 |
| L-003 | Login | P0 | POST | `/login` | 学生登录成功 | `{"username":20224001,"password":"123456"}` | `code=200`，`rb_role=2` | 无 |
| L-004 | Login | P0 | POST | `/login` | 密码错误 | 合法账号 + 错误密码 | `code=400` | 无 |
| L-005 | Login | P1 | POST | `/login` | 账号不存在 | `username=99999999` | `code=400` | 无 |
| L-006 | Login | P1 | POST | `/login` | username 非数字 | `username="abc"` | 返回 4xx 或统一异常 JSON | 无 |
| L-007 | Login | P1 | POST | `/login` | 缺少 password | 不传 password | 返回失败，记录实际码 | 无 |
| L-008 | Login | P0 | POST | `/logout` | 登出清理 Cookie | 无 | 清理 `rb_token`、`rb_role` | 无 |
| A-001 | Admin | P0 | GET | `/admins` | 查询管理员列表 | 无 | `code=200`，`data` 为数组 | `SELECT COUNT(*) FROM admin;` |
| A-002 | Admin | P0 | GET | `/admin/{adminId}` | 查询管理员详情 | `adminId=9991` | `code=200`，返回对象 | `SELECT * FROM admin WHERE adminId=9991;` |
| A-003 | Admin | P1 | GET | `/admin/{adminId}` | 查询不存在管理员 | `adminId=999999` | 记录返回行为（空/错） | 无 |
| A-004 | Admin | P1 | POST | `/admin` | 新增管理员 | 传完整 admin JSON | 预期新增成功 | `SELECT * FROM admin WHERE adminName='test_admin';` |
| A-005 | Admin | P1 | PUT | `/admin/{adminId}` | 更新管理员 | `adminId + body` | `code=200`，字段被更新 | `SELECT tel,email,pwd FROM admin WHERE adminId=?;` |
| A-006 | Admin | P1 | DELETE | `/admin/{adminId}` | 删除管理员 | 用新增的测试 ID | `code=200` | `SELECT COUNT(*) FROM admin WHERE adminId=?;` |
| A-007 | Admin | P2 | DELETE | `/admin/{adminId}` | 删除不存在管理员 | `adminId=999999` | 记录返回行为 | 无 |
| A-008 | Admin | P1 | GET | `/admin/resetPsw/{adminId}/{oldPsw}/{newPsw}` | 管理员改密成功 | `9991/123456/654321` | `code=200`，data 为 true | `SELECT pwd FROM admin WHERE adminId=9991;` |
| A-009 | Admin | P1 | GET | `/admin/resetPsw/{adminId}/{oldPsw}/{newPsw}` | 旧密码错误 | `9991/wrong/123456` | 返回失败提示 | 无 |
| A-010 | Admin | P2 | GET | `/admin/resetPsw/{adminId}/{oldPsw}/{newPsw}` | 通过 teacherId 改教师密码 | `20081001/123456/654321` | 验证跨角色逻辑 | `SELECT pwd FROM teacher WHERE teacherId=20081001;` |
| T-001 | Teacher | P0 | GET | `/teachers/{page}/{size}` | 教师分页查询 | `1/10` | `code=200`，`data.records` 存在 | 无 |
| T-002 | Teacher | P0 | GET | `/teacher/{teacherId}` | 教师详情 | `teacherId=20081001` | `code=200` | `SELECT * FROM teacher WHERE teacherId=20081001;` |
| T-003 | Teacher | P1 | POST | `/teacher` | 新增教师 | 传 teacher JSON | `code=200`，默认 `role=1` | `SELECT role FROM teacher WHERE teacherName='test_teacher';` |
| T-004 | Teacher | P1 | PUT | `/teacher` | 更新教师 | 传完整 teacher JSON | `code=200` | `SELECT tel,email,type FROM teacher WHERE teacherId=?;` |
| T-005 | Teacher | P1 | DELETE | `/teacher/{teacherId}` | 删除教师 | 删除测试数据 | `code=200` | `SELECT COUNT(*) FROM teacher WHERE teacherId=?;` |
| T-006 | Teacher | P2 | DELETE | `/teacher/{teacherId}` | 删除不存在教师 | `teacherId=29999999` | 记录返回行为 | 无 |
| S-001 | Student | P0 | GET | `/students/{page}/{size}/{name}/{grade}/{tel}/{institute}/{major}/{clazz}` | 全量分页 | `1/10/@/@/@/@/@/@` | `code=200`，有分页记录 | 无 |
| S-002 | Student | P1 | GET | `/students/...` | 按条件分页 | `1/10/@/2023/@/@/软件工程/@` | 返回数据满足过滤条件 | 无 |
| S-003 | Student | P0 | GET | `/student/{studentId}` | 学生详情存在 | `20224001` | `code=200` | `SELECT * FROM student WHERE studentId=20224001;` |
| S-004 | Student | P1 | GET | `/student/{studentId}` | 学生详情不存在 | `20999999` | `code=404` | 无 |
| S-005 | Student | P1 | POST | `/student` | 新增学生 | 传 student JSON | `code=200` | `SELECT * FROM student WHERE studentName='test_student';` |
| S-006 | Student | P1 | PUT | `/student` | 更新学生 | 传完整 student JSON | `code=200` | `SELECT tel,email,major FROM student WHERE studentId=?;` |
| S-007 | Student | P0 | PUT | `/studentPWD` | 更新学生密码 | `{"studentId":"20224001","pwd":"654321"}` | `code=200` | `SELECT pwd FROM student WHERE studentId=20224001;` |
| S-008 | Student | P1 | DELETE | `/student/{studentId}` | 删除学生 | 删除测试数据 | `code=200` | `SELECT COUNT(*) FROM student WHERE studentId=?;` |
| S-009 | Student | P2 | GET | `/students/...` | 分页边界 | `page=0,size=0` 或负数 | 不应引发服务崩溃 | 无 |
| S-010 | Student | P2 | GET | `/student/{studentId}` | 非法ID类型 | `studentId=abc` | 4xx 或统一异常 | 无 |

## 5. 场景测试编排建议（Apifox 场景）

建议在 Apifox 新建 2 个场景：

1. `Smoke-Minimum`（每次改动后跑）
2. `Full-Regression`（提测前跑）

`Smoke-Minimum` 推荐顺序：

1. L-001
2. A-001
3. T-001
4. S-001
5. S-007
6. L-008

## 6. 执行记录模板

每条用例执行后在 Apifox 或表格记录：

| 用例ID | 执行人 | 执行时间 | 结果(PASS/FAIL) | 缺陷ID | 备注 |
|---|---|---|---|---|---|
| L-001 |  |  |  |  |  |

## 7. 当前版本备注

- 当前项目已写登录 Cookie，但尚未发现启用的拦截器链路；涉及“未登录访问应被拒绝”的用例可先记录为 `待实现`。
- 若你修复了后端逻辑（例如新增管理员/参数绑定类问题），请在执行时同步更新该清单中的预期结果。
