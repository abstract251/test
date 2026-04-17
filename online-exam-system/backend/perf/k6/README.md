# k6 基线压测脚本

这组脚本用于 `phase 0` 的基线采样，目标是先稳定拿到当前单体系统的吞吐、延迟和错误率，再进入后续高并发优化阶段。

## 覆盖链路

- 登录：`POST /auth/login`
- 学生考试中心：`GET /student/exams`
- 开始考试：`POST /student/exam/{examCode}/attempt/start`
- 暂存答案：`PUT /student/exam/{examCode}/attempt/answers`
- 交卷：`POST /student/exam/{examCode}/attempt/submit`
- 题库分页：`GET /question-bank/{page}/{size}`

## 使用前提

- 已启动后端应用，默认地址为 `http://localhost:8080`
- 数据库中存在可登录的学生和教师账号
- `EXAM_CODE` 指向一个允许学生进入的考试
- 若要让“交卷”链路的成功率具有参考意义，建议使用可重复重置的数据或学生账号池

## 推荐命令

```powershell
k6 run backend/perf/k6/phase0-baseline.js
```

```powershell
$env:BASE_URL="http://localhost:8080"
$env:STUDENT_USERNAME="20224001"
$env:STUDENT_PASSWORD="Student@123"
$env:TEACHER_USERNAME="20081001"
$env:TEACHER_PASSWORD="Teacher@123"
$env:EXAM_CODE="20230001"
k6 run backend/perf/k6/phase0-baseline.js
```

## 常用环境变量

- `BASE_URL`：后端地址，默认 `http://localhost:8080`
- `STUDENT_USERNAME` / `STUDENT_PASSWORD`：学生账号
- `TEACHER_USERNAME` / `TEACHER_PASSWORD`：教师账号
- `EXAM_CODE`：考试编号
- `QUESTION_BANK_PAGE` / `QUESTION_BANK_SIZE`：题库分页参数
- `SMOKE_ONLY=true`：只跑低压基线，方便先校验脚本和环境

## 结果采集建议

- 应用侧：抓取 `/actuator/prometheus`
- JVM/Tomcat/Hikari：使用 Prometheus 或直接查看 `metrics` 端点
- MySQL：打开慢查询日志并记录连接数、QPS、慢 SQL
- 若后续引入 Nginx/Redis/RabbitMQ，再把对应指标一起纳入同一张基线对比表
