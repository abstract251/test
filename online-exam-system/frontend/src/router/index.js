/**
 * Vue Router 配置
 *
 * 概念：
 * - route（路由）：URL 路径 与 组件 的对应关系。
 * - router（路由器）：根据当前 URL 选择要渲染哪个组件，并渲染到 <router-view />。
 *
 * 本项目使用 HTML5 History 模式（createWebHistory），URL 形如 http://localhost:8081/ ，无 # 号。
 * 若部署到子目录服务器，可能需要配置 publicPath 或 history 的 base，见 vue.config.js 文档。
 */
import { createRouter, createWebHistory } from 'vue-router'
import ExamList from '../components/ExamList.vue'

const routes = [
  {
    /**
     * path: 浏览器地址栏路径（以 / 开头）
     * name: 路由名称，可在代码里用 name 跳转（本项目暂未使用）
     * component: 该路径下要显示的页面组件
     */
    path: '/',
    name: 'ExamList',
    component: ExamList
  }
  // 后续可在此追加更多路由，例如 { path: '/login', component: Login }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
