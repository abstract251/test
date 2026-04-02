<!--
  根组件（Root Component）

  说明：
  - 本页面不写具体业务，只放 <router-view />，由「路由」决定显示哪个页面。
  - 例如访问 / 时，router 会渲染 ExamList.vue（见 src/router/index.js）。

  为什么这样设计：
  - 单页应用（SPA）通常只有一个 index.html，页面切换靠路由换组件，而不是整页刷新。
-->
<template>
  <!-- 路由出口：当前 URL 匹配的组件会渲染在这里 -->
  <router-view />
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
/* 全局作用于 #app 容器，子组件可继承字体等基础样式 */
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
}
</style>
