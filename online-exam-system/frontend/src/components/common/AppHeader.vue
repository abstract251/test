<template>
  <header :class="['app-header', `app-header--${mode}`]">
    <div class="brand">
      <div class="brand-mark">
        <el-icon><School /></el-icon>
      </div>
      <div>
        <div class="brand-title">{{ brandTitle }}</div>
        <div class="brand-subtitle">{{ subtitle }}</div>
      </div>
    </div>

    <nav v-if="mode === 'student'" class="student-nav">
      <RouterLink
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="student-nav__link"
        :class="{ 'is-active': route.path.startsWith(item.path) }"
      >
        {{ item.title }}
      </RouterLink>
    </nav>

    <div class="header-actions">
      <div class="user-chip">
        <span class="user-chip__role">{{ roleText }}</span>
        <strong>{{ session?.displayName || session?.userName || '未登录' }}</strong>
      </div>

      <el-dropdown trigger="click">
        <button class="header-actions__button" type="button">
          账户操作
          <el-icon><ArrowDown /></el-icon>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-if="mode === 'console' && canResetConsolePassword"
              @click="dialogVisible = true"
            >
              修改密码
            </el-dropdown-item>
            <el-dropdown-item
              v-if="mode === 'student'"
              @click="router.push('/student/password')"
            >
              修改密码
            </el-dropdown-item>
            <el-dropdown-item @click="$emit('logout')">
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <el-dialog
      v-model="dialogVisible"
      title="修改密码"
      width="420px"
      align-center
      append-to-body
      destroy-on-close
    >
      <el-form label-position="top">
        <el-form-item label="旧密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPassword">确认修改</el-button>
      </template>
    </el-dialog>
  </header>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { ArrowDown, School } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { resetConsolePassword } from '@/api/authApi'
import { AUTH_ROLE_LABELS, AUTH_ROLES } from '@/utils/constants'

const props = defineProps({
  mode: {
    type: String,
    default: 'console'
  },
  session: {
    type: Object,
    default: null
  },
  navItems: {
    type: Array,
    default: () => []
  }
})

defineEmits(['logout'])

const route = useRoute()
const router = useRouter()

const dialogVisible = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const roleText = computed(() => AUTH_ROLE_LABELS[props.session?.authRole] || '访客')
const brandTitle = computed(() => (props.mode === 'console' ? '在线考试管理台' : '在线考试系统'))
const subtitle = computed(() => route.meta.title || '考试管理')
const canResetConsolePassword = computed(() => props.session?.authRole === AUTH_ROLES.ADMIN)

async function handleResetPassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning('请完整填写密码信息')
    return
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  const response = await resetConsolePassword(
    props.session.userId,
    passwordForm.oldPassword,
    passwordForm.newPassword
  )

  if (response.code !== 200) {
    ElMessage.error(response.message || '密码修改失败')
    return
  }

  if (response.data !== true) {
    ElMessage.warning(String(response.data))
    return
  }

  ElMessage.success('密码修改成功')
  dialogVisible.value = false
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 20px 24px;
  backdrop-filter: blur(14px);
  background: rgba(255, 255, 255, 0.84);
  border-bottom: 1px solid var(--border-soft);
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.brand-mark {
  width: 52px;
  height: 52px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  color: white;
  font-size: 24px;
  background: linear-gradient(140deg, var(--brand-primary), var(--brand-primary-deep));
  box-shadow: var(--shadow-card);
}

.brand-title {
  font-size: 20px;
  font-weight: 700;
}

.brand-subtitle {
  color: var(--text-muted);
  font-size: 13px;
}

.student-nav {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.student-nav__link {
  padding: 10px 14px;
  border-radius: 999px;
  color: var(--text-secondary);
  transition: background var(--transition-base), color var(--transition-base);
}

.student-nav__link.is-active,
.student-nav__link:hover {
  color: var(--brand-primary-deep);
  background: var(--bg-soft);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-chip {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.user-chip__role {
  font-size: 12px;
  color: var(--text-muted);
}

.header-actions__button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 42px;
  padding: 0 16px;
  border: none;
  border-radius: 999px;
  color: var(--text-main);
  background: var(--bg-soft);
  cursor: pointer;
}

@media (max-width: 768px) {
  .app-header {
    flex-wrap: wrap;
    padding: 16px;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }

  .user-chip {
    align-items: flex-start;
  }
}
</style>
