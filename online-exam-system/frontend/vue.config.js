/**
 * Vue CLI 配置文件（可选）
 *
 * 文档：https://cli.vuejs.org/zh/config/
 *
 * 说明：
 * - transpileDependencies：是否对 node_modules 里指定依赖做 Babel 转译（部分老库需要）。
 * - devServer：本地开发服务器（npm run serve）选项。
 *
 * 端口说明（与本项目联调后端有关）：
 * - 后端 Spring Boot 默认使用 8080（见后端 application.properties）。
 * - 前端开发服务器若也占用 8080 会冲突，因此此处将前端改为 8081。
 * 访问前端：http://localhost:8081/
 * 请求后端：axios baseURL 指向 http://localhost:8080（见 src/utils/request.js）
 */
const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8081
  }
})
