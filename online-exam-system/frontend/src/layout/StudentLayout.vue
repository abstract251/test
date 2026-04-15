<template>
  <div class="student-layout">
    <AppHeader
      mode="student"
      :session="session"
      :nav-items="navItems"
      @logout="handleLogout"
    />

    <main class="student-layout__content page-shell">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/common/AppHeader.vue'
import { logout } from '@/api/authApi'
import { useAuthSession } from '@/composables/useAuthSession'
import { clearSession } from '@/utils/auth'
import { STUDENT_NAV_ITEMS } from '@/utils/constants'

const router = useRouter()
const { session, syncSession } = useAuthSession()

const navItems = STUDENT_NAV_ITEMS

async function handleLogout() {
  try {
    await logout()
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
.student-layout {
  min-height: 100vh;
}

.student-layout__content {
  padding: 24px 0 48px;
}

@media (max-width: 768px) {
  .student-layout__content {
    padding-top: 18px;
  }
}
</style>
