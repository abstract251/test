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
 * 请求后端：前端统一走 /api，由 devServer 代理到 http://localhost:8080
 */
const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  productionSourceMap: false,
  configureWebpack: {
    optimization: {
      splitChunks: {
        chunks: 'all',
        cacheGroups: {
          vueCore: {
            name: 'chunk-vue-core',
            test: /[\\/]node_modules[\\/](vue|vue-router)[\\/]/,
            priority: 30,
            enforce: true
          },
          elementPlus: {
            name: 'chunk-element-plus',
            test: /[\\/]node_modules[\\/](element-plus|@element-plus)[\\/]/,
            priority: 25,
            enforce: true
          },
          axiosDayjs: {
            name: 'chunk-axios-dayjs',
            test: /[\\/]node_modules[\\/](axios|dayjs)[\\/]/,
            priority: 20,
            enforce: true
          }
        }
      }
    },
    performance: {
      hints: 'warning',
      maxAssetSize: 460000,
      maxEntrypointSize: 980000
    }
  },
  devServer: {
    port: 8081,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: {
          '^/api': ''
        }
      }
    }
  }
})
