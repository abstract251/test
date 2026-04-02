/**
 * HTTP 请求封装（基于 Axios）
 *
 * 为什么封装：
 * 1. 统一配置后端地址（baseURL），避免每个接口写完整 URL。
 * 2. 统一超时、请求头、token（后续可加）、错误处理。
 * 3. 响应拦截里直接返回 res.data，与后端约定的 JSON 结构一致。
 *
 * 与后端约定（本项目）：
 * 后端 Controller 多返回 ApiResult<T> 形如：
 * { code: 200, message: "...", data: ... }
 * 拦截器里 return res.data，即业务代码里拿到的就是整个 ApiResult 对象。
 *
 * 跨域（CORS）：
 * 浏览器从 localhost:8081 访问 localhost:8080 属于跨域，需后端开启 CORS（本项目后端已配置）。
 */
import axios from 'axios'

const service = axios.create({
  /** 后端服务根地址（与后端 server.port 一致） */
  baseURL: 'http://localhost:8080',
  /** 超时时间（毫秒），超时会在 catch 里进错误分支 */
  timeout: 5000
})

/**
 * 请求拦截器：在请求发出前执行
 * 典型用途：附加 Authorization、统一 Content-Type、打印调试日志
 */
service.interceptors.request.use(
  (config) => config,
  (error) => Promise.reject(error)
)

/**
 * 响应拦截器：在收到响应后、交给业务代码前执行
 * 这里直接返回 res.data，即 axios 默认的「响应体」里的 data 字段（不是 HTTP 状态码）
 */
service.interceptors.response.use(
  (res) => res.data,
  (error) => Promise.reject(error)
)

export default service
