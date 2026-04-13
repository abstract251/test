# Online Exam System Frontend API

最后更新：`2026-04-03`

本文档只整理当前后端已经实现、且可以直接供前端调用的接口。

## 1. 基础约定

- 后端基地址：`http://localhost:8080`
- 通用响应结构：除 `POST /logout` 外，当前接口基本都返回 `ApiResult`

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {}
}
```

- 登录态基于 Cookie，不是 JWT
- 登录成功后后端会写入两个 Cookie：`rb_token`、`rb_role`
- 当前拦截器对除 `/login`、`/logout`、`/error`、`/favicon.ico` 外的接口都做登录校验
- 当前只校验“是否登录”，还没有做按角色的接口权限控制
- 角色值约定：`0=管理员`，`1=教师`，`2=学生`
- 题型值约定：`1=选择题`，`2=填空题`，`3=判断题`

跨域调用时，前端请求必须带 Cookie。当前 [request.js](D:\test\online-exam-system\frontend\src\utils\request.js) 里还没有开启 `withCredentials`，调用受拦截接口前需要补上：

```js
const service = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000,
  withCredentials: true
})
```

## 2. 常见返回说明

- `code=200`：成功
- `code=400`：常见于登录失败、新增失败、更新失败
- `code=401`：未登录或登录失效，由拦截器返回
- `code=404`：学生不存在时由 `/student/{studentId}` 返回
- `code=10000`：考试编号不存在时由 `/exam/{examCode}` 返回
- `code=20000`：考试更新参数错误时由 `PUT /exam` 返回
- `POST /logout` 当前返回 `HTTP 200`，但响应体为空，不是 `ApiResult`

分页接口的 `data` 为 MyBatis-Plus 的 `IPage<T>`，前端重点取这些字段：

```json
{
  "records": [],
  "total": 0,
  "size": 10,
  "current": 1,
  "pages": 0
}
```

## 3. 数据模型

### 3.1 Login

```json
{
  "username": 9991,
  "password": "123456"
}
```

说明：

- `username` 必须是数字，后端类型是 `Integer`

### 3.2 Admin

```json
{
  "adminId": 1,
  "adminName": "admin",
  "sex": "M",
  "tel": "13800000000",
  "email": "admin@example.com",
  "pwd": "123456",
  "cardId": "123456789012345678",
  "role": "0"
}
```

### 3.3 Teacher

```json
{
  "teacherId": "20081001",
  "teacherName": "teacher",
  "institute": "计算机学院",
  "sex": "M",
  "tel": "13900000000",
  "email": "teacher@example.com",
  "pwd": "123456",
  "cardId": "123456789012345678",
  "type": "讲师",
  "role": "1"
}
```

### 3.4 Student

```json
{
  "studentId": "20224001",
  "studentName": "student",
  "grade": "2022",
  "major": "软件工程",
  "clazz": "1班",
  "institute": "计算机学院",
  "tel": "13700000000",
  "email": "student@example.com",
  "pwd": "123456",
  "cardId": "123456789012345678",
  "sex": "M",
  "role": "2"
}
```

### 3.5 ExamManage

```json
{
  "examCode": 1,
  "description": "Java期末考试",
  "source": "Java",
  "paperId": 1001,
  "examDate": "2026-06-30 09:00:00",
  "totalTime": 120,
  "grade": "2022",
  "term": "2",
  "major": "软件工程",
  "institute": "计算机学院",
  "totalScore": 100,
  "type": "正式考试",
  "tips": "请独立完成"
}
```

### 3.6 PaperManage

```json
{
  "paperId": 1001,
  "questionType": 1,
  "questionId": 5001
}
```

### 3.7 MultiQuestion

```json
{
  "questionId": 5001,
  "subject": "Java",
  "section": "集合",
  "answerA": "A",
  "answerB": "B",
  "answerC": "C",
  "answerD": "D",
  "question": "题目内容",
  "level": "中等",
  "rightAnswer": "A",
  "analysis": "解析",
  "score": 2
}
```

### 3.8 FillQuestion

```json
{
  "questionId": 6001,
  "subject": "Java",
  "question": "填空题内容",
  "answer": "答案",
  "score": 2,
  "level": "中等",
  "section": "基础",
  "analysis": "解析"
}
```

### 3.9 JudgeQuestion

```json
{
  "questionId": 7001,
  "subject": "Java",
  "question": "判断题内容",
  "answer": "T",
  "level": "简单",
  "section": "基础",
  "score": 2,
  "analysis": "解析"
}
```

## 4. 登录接口

### `POST /login`

用途：登录并按身份分流。

请求体：

```json
{
  "username": 9991,
  "password": "123456"
}
```

成功响应：

- `data` 为管理员、教师或学生对象之一
- 响应头会写入 `rb_token`、`rb_role`

失败响应：

```json
{
  "code": 400,
  "message": "请求失败",
  "data": null
}
```

### `POST /logout`

用途：清理登录 Cookie。

说明：

- 当前响应体为空
- 响应头会把 `rb_token`、`rb_role` 的 `Max-Age` 设为 `0`

## 5. 管理员接口

### `GET /admins`

用途：查询管理员列表。

返回：

- `data` 为 `Admin[]`
- 当前后端 SQL 只返回这些字段：`adminName`、`sex`、`tel`、`email`、`cardId`、`role`
- `adminId` 和 `pwd` 不会在这个列表接口里返回

### `GET /admin/{adminId}`

用途：按 `adminId` 查询管理员详情。

返回：

- `data` 为单个 `Admin`
- 当前会返回 `adminId` 和 `pwd`

### `POST /admin`

用途：新增管理员。

请求体：`Admin`

成功响应：

```json
{
  "code": 200,
  "message": "请求成功",
  "data": 1
}
```

### `PUT /admin/{adminId}`

用途：更新管理员。

说明：

- 路径里的 `adminId` 会覆盖请求体里的 `adminId`

### `DELETE /admin/{adminId}`

用途：删除管理员。

返回：

- `data=null`

### `GET /admin/resetPsw/{adminId}/{oldPsw}/{newPsw}`

用途：重置密码。

返回：

- 成功时：`data=true`
- 失败时：`data` 是错误提示字符串

说明：

- 当前实现里，如果 `adminId` 查不到管理员，会再尝试按同一个编号查教师并修改教师密码

## 6. 教师接口

### `GET /teachers/{page}/{size}`

用途：分页查询教师列表。

返回：

- `data` 为 `IPage<Teacher>`

### `GET /teacher/{teacherId}`

用途：查询教师详情。

### `POST /teacher`

用途：新增教师。

请求体：`Teacher`

说明：

- 后端会自动把 `role` 设为 `"1"`

### `PUT /teacher`

用途：更新教师。

请求体：`Teacher`

说明：

- 更新时必须传 `teacherId`

### `DELETE /teacher/{teacherId}`

用途：删除教师。

## 7. 学生接口

### `GET /students/{page}/{size}/{name}/{grade}/{tel}/{institute}/{major}/{clazz}`

用途：分页查询学生，并支持按多个条件模糊筛选。

参数说明：

- `page`：页码，从 `1` 开始
- `size`：每页条数
- `name`、`grade`、`tel`、`institute`、`major`、`clazz`：筛选条件

特殊约定：

- 某个筛选条件如果不想过滤，前端必须传 `@`
- 例如：

```text
/students/1/10/@/@/@/@/@/@
```

### `GET /student/{studentId}`

用途：查询学生详情。

返回：

- 找到时：`code=200`
- 找不到时：`code=404`

### `POST /student`

用途：新增学生。

请求体：`Student`

说明：

- 当前后端不会自动补 `role`
- 前端新增学生时应显式传 `"role": "2"`
- 成功时响应 `data=null`

### `PUT /student`

用途：更新学生信息。

请求体：`Student`

说明：

- 更新时必须传 `studentId`

### `PUT /studentPWD`

用途：单独修改学生密码。

最小请求体：

```json
{
  "studentId": "20224001",
  "pwd": "654321"
}
```

成功响应：

- `code=200`
- `data=null`

### `DELETE /student/{studentId}`

用途：删除学生。

## 8. 考试管理接口

### `GET /exams`

用途：查询全部考试，不分页。

返回：

- `data` 为 `ExamManage[]`

### `GET /exams/{page}/{size}`

用途：分页查询考试列表。

返回：

- `data` 为 `IPage<ExamManage>`

### `GET /exam/{examCode}`

用途：按考试编号查询考试详情。

说明：

- 找不到时返回 `code=10000`

### `POST /exam`

用途：新增考试。

请求体：`ExamManage`

### `PUT /exam`

用途：更新考试。

请求体：`ExamManage`

说明：

- 更新时必须传 `examCode`

### `DELETE /exam/{examCode}`

用途：删除考试。

说明：

- 当前实现会先删除该考试对应试卷在 `paper_manage` 里的题目关联，再删除考试本身

### `GET /examManagePaperId`

用途：获取当前最后一条考试记录的 `paperId`。

返回：

- `data` 为 `ExamManage` 对象
- 当前通常只有 `paperId` 字段有值

## 9. 试卷接口

### `GET /papers`

用途：查询全部试卷题目关联。

返回：

- `data` 为 `PaperManage[]`

### `GET /paper/{paperId}`

用途：查询某张试卷下的所有题目详情。

返回结构：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "1": [],
    "2": [],
    "3": []
  }
}
```

说明：

- `data.1`：选择题数组
- `data.2`：填空题数组
- `data.3`：判断题数组

### `GET /practice/{source}`

用途：按学科查询练习题库。

说明：

- 返回结构和 `/paper/{paperId}` 一样
- `source` 例如：`Java`、`数据库`

### `POST /paperManage`

用途：给试卷添加一道题目关联。

请求体：

```json
{
  "paperId": 1001,
  "questionType": 1,
  "questionId": 5001
}
```

### `GET /paper/delete/{paperId}/{type}/{questionId}`

用途：删除试卷中的单道题目关联。

说明：

- 这里虽然是删除动作，但当前后端实现使用的是 `GET`
- `type` 对应题型值：`1/2/3`

### `DELETE /paper/deleteAll/{paperId}`

用途：清空某张试卷的全部题目关联。

### `GET /paper/score/{paperId}`

用途：获取试卷总分。

说明：

- 当前总分不是按题目 `score` 字段累加
- 现实现为：`(选择题数量 + 填空题数量 + 判断题数量) * 2`

## 10. 题库接口

### 10.1 选择题

#### `GET /multiQuestionId`

用途：查询当前最后一条选择题的 `questionId`。

返回：

- `data` 为 `MultiQuestion`
- 当前通常只有 `questionId` 字段有值

#### `POST /MultiQuestion`

用途：新增选择题。

请求体：`MultiQuestion`

注意：

- 路径里的 `M` 是大写，前端调用时必须保持 `/MultiQuestion`

#### `POST /editMultiQuestion`

用途：修改选择题。

说明：

- 请求体里必须包含 `questionId`

### 10.2 填空题

#### `GET /fillQuestionId`

用途：查询当前最后一条填空题的 `questionId`。

#### `POST /fillQuestion`

用途：新增填空题。

请求体：`FillQuestion`

#### `POST /editFillQuestion`

用途：修改填空题。

说明：

- 请求体里必须包含 `questionId`

### 10.3 判断题

#### `GET /judgeQuestionId`

用途：查询当前最后一条判断题的 `questionId`。

#### `POST /judgeQuestion`

用途：新增判断题。

请求体：`JudgeQuestion`

#### `POST /editJudgeQuestion`

用途：修改判断题。

说明：

- 请求体里必须包含 `questionId`

## 11. 前端对接注意事项

- 登录后访问受保护接口时，必须带上 Cookie；跨域下要开启 `withCredentials`
- `/logout` 不是 `ApiResult`，前端不要按 `res.code` 解析
- `/paper/delete/{paperId}/{type}/{questionId}` 当前是 `GET`，不要按常规 REST 误写成 `DELETE`
- `/students/...` 的筛选条件为空时必须传 `@`
- `/MultiQuestion` 路径大小写敏感，必须保持首字母大写
- 题目实体里虽然有 `score` 字段，但当前新增/编辑 SQL 没有真正写入该字段，前端不要依赖题目 `score` 持久化
- 当前后端没有实现自动组卷、在线答题、成绩提交等接口，这些不在本文档范围内
