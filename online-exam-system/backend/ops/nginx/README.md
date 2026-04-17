# Nginx Frontend Template

用于 `phase 1` 的静态资源减载联调模板。

## 用法

1. 在 `frontend/` 下执行 `npm run build`
2. 根据实际环境调整 `root` 与 `upstream online_exam_backend`
3. 执行 `nginx -t` 校验配置
4. 重载 Nginx 并验证以下内容：
   - `GET /` 与前端深链路都能返回 SPA 页面
   - `GET /api/*` 转发到后端应用
   - `js/css/img/font` 响应头带 `Cache-Control: public, max-age=31536000, immutable`
   - `index.html` 不带长缓存
   - 文本资源启用 gzip
