<template>
  <aside class="side-menu">
    <el-menu
      :default-active="route.path"
      class="side-menu__menu"
      router
    >
      <el-menu-item
        v-for="item in items"
        :key="item.path"
        :index="item.path"
      >
        <el-icon>
          <component :is="iconMap[item.icon] || House" />
        </el-icon>
        <span>{{ item.title }}</span>
      </el-menu-item>
    </el-menu>
  </aside>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { EditPen, Histogram, House, Reading, User, UserFilled } from '@element-plus/icons-vue'

defineProps({
  items: {
    type: Array,
    default: () => []
  }
})

const route = useRoute()

const iconMap = {
  House,
  User,
  UserFilled,
  Reading,
  EditPen,
  Histogram
}
</script>

<style scoped>
.side-menu {
  width: 250px;
  flex: 0 0 250px;
}

.side-menu__menu {
  border: none;
  padding: 14px 10px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-card);
}

@media (max-width: 980px) {
  .side-menu {
    width: 100%;
    flex-basis: 100%;
  }
}
</style>
