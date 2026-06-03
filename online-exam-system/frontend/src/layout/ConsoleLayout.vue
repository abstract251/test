<template>
  <div class="console-layout">
    <AppHeader
      mode="console"
      :session="session"
      @logout="handleLogout"
    />

    <div class="console-layout__body page-shell">
      <SideMenu :items="menuItems" />

      <main class="console-layout__content">
        <div class="console-layout__crumbs">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>后台</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title || '控制台' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/common/AppHeader.vue'
import SideMenu from '@/components/common/SideMenu.vue'
import { logout } from '@/api/authApi'
import { useAuthSession } from '@/composables/useAuthSession'
import { clearSession, getRefreshToken } from '@/utils/auth'
import { CONSOLE_MENU } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const { session, syncSession } = useAuthSession()

const menuItems = computed(() => CONSOLE_MENU[session.value?.authRole] || [])

async function handleLogout() {
  try {
    const refreshToken = getRefreshToken()
    if (refreshToken) {
      await logout({ refreshToken })
    }
  } catch (error) {
    // ignore and continue cleanup
  }

  clearSession()
  syncSession()
  ElMessage.success('已退出登录')
  router.replace('/login')
}
</script>

<style scoped>
.console-layout {
  min-height: 100vh;
}

.console-layout__body {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 24px 0 28px;
}

.console-layout__content {
  min-width: 0;
  flex: 1;
}

.console-layout__crumbs {
  margin-bottom: 16px;
  padding: 0 4px;
}

@media (max-width: 980px) {
  .console-layout__body {
    flex-direction: column;
  }
}
</style>
