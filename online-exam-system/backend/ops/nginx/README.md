# Nginx Phase 2 入口层说明

用于 `phase 2` 的单体增强入口层联调与压测。目标拓扑为：

- Browser -> Nginx -> Spring Boot `18080` / `18081`
- 静态资源由 Nginx 托管
- `/api/*` 由 Nginx 反代到后端双实例
- 登录、刷新、开始考试、交卷在入口层做基础限流

## 当前模板覆盖能力

- 双实例 upstream，负载策略为 `least_conn`
- `/api` 统一反代，兼容当前前端 `/api` 相对路径
- 静态资源长缓存，`index.html` 不长缓存
- `gzip` 开启
- 对以下热点接口单独限流
  - `POST /api/auth/login`
  - `POST /api/auth/refresh`
  - `POST /api/student/exam/{examCode}/attempt/start`
  - `POST /api/student/exam/{examCode}/attempt/submit`
- Nginx access log 输出 upstream 地址、状态和时延，便于和 Prometheus 指标对齐
- 提供 `/backend-health/18080` 与 `/backend-health/18081` 便于快速排障

## 端口建议

- Nginx：`8080`
- Backend A：`18080`
- Backend B：`18081`
- Prometheus：`9090`
- 前端开发服务器仍可保留 `8081`，但 `phase 2` 验证应优先走 Nginx

## 启动顺序

1. 在 `frontend/` 下执行 `npm run build`
2. 执行 `backend/ops/scripts/start-backend-test-dual-instance.ps1`
3. 确认两个实例的日志文件开始输出：
   - `backend/target/backend-run-18080.out.log`
   - `backend/target/backend-run-18081.out.log`
4. 运行 `backend/ops/scripts/start-nginx-test-instance.ps1`
5. 执行 `backend/ops/scripts/start-prometheus.ps1`

## 脚本说明

- `backend/ops/scripts/start-nginx-test-instance.ps1`
  - 启动前先校验 `frontend/dist`、`nginx.exe` 和 `online-exam-test.conf`
  - 自动清理陈旧的 `online-exam-nginx.pid`
  - 先执行 `nginx -t` 再启动
  - 使用独立 PID 文件：
    - `tools/nginx/nginx-1.28.0/logs/online-exam-nginx.pid`
- `backend/ops/scripts/stop-nginx-test-instance.ps1`
  - 只会停止 PID 文件指向且进程名确认为 `nginx` 的实例
  - 若 PID 文件已脏，只清理陈旧 PID，不会误杀无关进程

## 验证项

至少验证以下内容：

1. `GET /` 与前端深链路都能返回 SPA 页面
2. `GET /api/*` 能正常访问后端接口
3. `GET /backend-health/18080` 与 `GET /backend-health/18081` 都返回 `UP`
4. `js/css/img/font` 响应头带 `Cache-Control: public, max-age=31536000, immutable`
5. `index.html` 不带长缓存
6. 文本资源启用 `gzip`
7. access log 中能看到请求被分流到 `18080` 和 `18081`

## 限流策略

模板中的初始值如下：

- `/api/auth/login`：单 IP `5 r/s`，`burst=10`
- `/api/auth/refresh`：单 IP `5 r/s`，`burst=10`
- `/api/student/exam/*/attempt/start`：单 IP `3 r/s`，`burst=10`
- `/api/student/exam/*/attempt/submit`：单 IP `2 r/s`，`burst=8`

这些值用于 `phase 2` 首轮入口保护，不是最终生产值。后续应基于真实压测结果调整。

## 故障切换说明

- 当前模板按开源 Nginx 能力设计，使用被动失败摘除，不依赖 Nginx Plus 主动健康检查
- 当某个实例返回 `502/503/504` 或连接超时，`proxy_next_upstream` 会尝试切到另一实例
- Prometheus 持续抓两个实例的 `/actuator/prometheus`，用于确认实例状态和负载分布

## 热启动边界

- `start-backend-test-instance.ps1` 和 `start-backend-test-instance-18080.ps1` 仅用于单实例本地热启动
- `PortAutoKillHotRestartConfig` 现在要求显式保护令牌 `LOCAL_SINGLE_INSTANCE_ONLY` 才会杀占用端口的旧进程
- 双实例模式下：
  - `18080` 启动脚本仍会清理本端口旧进程
  - `18081` 启动脚本不会启用应用内自动杀进程
  - 不允许让一个实例在启动时误杀另一个实例

## 压测建议

压测统一打到 Nginx 入口，不再直连某个实例：

- `backend/perf/k6/phase0-baseline.js`
- `backend/perf/k6/student-exam-concurrent-flow.js`

执行时把 `BASE_URL` 指向 Nginx 地址，例如：

```powershell
$env:BASE_URL='http://localhost:8080'
k6 run backend/perf/k6/student-exam-concurrent-flow.js
```

## Runtime Mode

phase 8 新增了入口层 runtime 模式切换脚本：

- `backend/ops/scripts/set-nginx-runtime-mode.ps1`

支持的模式：

- `normal`
- `single-18080`
- `single-18081`
- `strict-protect`
- `relaxed`

示例：

```powershell
& "D:\test\online-exam-system\backend\ops\scripts\set-nginx-runtime-mode.ps1" -Mode normal
& "D:\test\online-exam-system\backend\ops\scripts\set-nginx-runtime-mode.ps1" -Mode single-18080
& "D:\test\online-exam-system\backend\ops\scripts\set-nginx-runtime-mode.ps1" -Mode strict-protect
```

对应 runtime include 目录：

- `backend/ops/nginx/runtime/upstream-mode.conf`
- `backend/ops/nginx/runtime/login-limit.conf`
- `backend/ops/nginx/runtime/refresh-limit.conf`
- `backend/ops/nginx/runtime/start-limit.conf`
- `backend/ops/nginx/runtime/submit-limit.conf`

## 回滚方式

如果 `phase 2` 联调中发现问题，按以下顺序回滚：

1. 先在 Nginx upstream 中临时移除 `18081`，回到单实例入口
2. 如果限流误伤，先注释四个热点接口的 `limit_req`
3. 如仍异常，前端继续走开发代理或直接回到 `18080` 单实例联调
