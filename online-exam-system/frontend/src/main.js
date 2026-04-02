/**
 * 应用入口文件（Entry）
 *
 * 作用：
 * 1. 创建 Vue 根实例，挂载到 public/index.html 里的 <div id="app">。
 * 2. 注册全局插件：Element Plus（UI 组件库）、Vue Router（路由）。
 *
 * 执行顺序：
 * createApp → use(插件) → mount('#app')
 *
 * 参考：
 * - Vue 3 文档：https://cn.vuejs.org/guide/essentials/application.html
 * - Element Plus：https://element-plus.org/zh-CN/
 */
import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'

// 创建应用实例，根组件为 App.vue
const app = createApp(App)

// 全局注册 Element Plus：所有组件里可直接用 <el-table>、<el-button> 等，无需再单独 import
app.use(ElementPlus)

// 注册路由：根据 URL 显示不同页面组件（见 src/router/index.js）
app.use(router)

// 挂载到 DOM：与 public/index.html 中 id="app" 的节点对应
app.mount('#app')
