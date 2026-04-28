<template>
  <div class="login-view">
    <div class="login-view__hero">
      <div class="login-view__hero-inner">
        <span class="hero-tag">在线考试</span>
        <h1>{{ LOGIN_COPY.heroTitle }}</h1>
        <p>{{ LOGIN_COPY.heroDescription }}</p>
        <ul class="hero-points">
          <li v-for="item in LOGIN_COPY.heroPoints" :key="item">{{ item }}</li>
        </ul>
      </div>
    </div>

    <div class="login-view__panel">
      <div class="login-card">
        <div class="login-card__header">
          <div class="login-card__badge">
            <el-icon><School /></el-icon>
          </div>
          <div>
            <h2>账号登录</h2>
            <p>{{ LOGIN_COPY.cardDescription }}</p>
          </div>
        </div>

        <PageNotice
          v-if="pageError"
          type="error"
          title="暂时还不能登录"
          :description="pageError"
        />
        <FormErrorSummary
          v-if="formSummary.title || formSummary.items.length"
          :title="formSummary.title"
          :items="formSummary.items"
        />

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
          <el-form-item label="登录身份" prop="role">
            <el-radio-group v-model="form.role" class="login-role-group">
              <el-radio-button
                v-for="item in AUTH_ROLE_OPTIONS"
                :key="item.value"
                :label="item.value"
              >
                {{ item.label }}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="账号" prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入学校发放的数字账号"
              inputmode="numeric"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-button
            class="login-card__submit"
            type="primary"
            :loading="submitting"
            @click="handleLogin"
          >
            登录并进入系统
          </el-button>
        </el-form>

        <div class="login-card__footer">
          <span>{{ LOGIN_COPY.footerLeft }}</span>
          <span>{{ LOGIN_COPY.footerRight }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Lock, School, User } from '@element-plus/icons-vue'
import { login } from '@/api/authApi'
import FormErrorSummary from '@/components/common/FormErrorSummary.vue'
import PageNotice from '@/components/common/PageNotice.vue'
import {
  resolveHomePath,
  setSessionFromAuthResponse,
  updateSessionRawUser
} from '@/utils/auth'
import { getStudentProfile } from '@/api/profileApi'
import { AUTH_ROLES, AUTH_ROLE_OPTIONS } from '@/utils/constants'
import { buildFormSummary, resolveUserFacingError, scrollToFirstError, showActionSuccess } from '@/utils/feedback'
import { LOGIN_COPY } from '@/utils/userCopy'

const route = useRoute()
const router = useRouter()

const formRef = ref(null)
const submitting = ref(false)
const pageError = ref('')
const formSummary = reactive({
  title: '',
  items: []
})
const form = reactive({
  username: '',
  password: '',
  role: AUTH_ROLES.STUDENT
})

const rules = {
  role: [{ required: true, message: '请先选择你的身份', trigger: 'change' }],
  username: [
    { required: true, message: '请输入学校发放的账号', trigger: 'blur' },
    { pattern: /^\d+$/, message: '账号通常是一串数字，请重新检查', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function resolveLoginErrorMessage(response) {
  if (response?.code === 400 || response?.code === 401) {
    return '账号、密码或身份选择不正确，请重新确认后再试'
  }
  return resolveUserFacingError(response, '登录暂时不可用，请稍后再试')
}

function resetFormFeedback() {
  pageError.value = ''
  formSummary.title = ''
  formSummary.items = []
}

async function handleLogin() {
  resetFormFeedback()
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    Object.assign(formSummary, buildFormSummary(formRef, '请先补全登录信息'))
    await scrollToFirstError(formRef)
    return
  }

  submitting.value = true

  try {
    const response = await login({
      username: String(form.username).trim(),
      password: form.password,
      role: form.role
    })

    if (response?.code !== 200 || !response?.data) {
      pageError.value = resolveLoginErrorMessage(response)
      return
    }

    const session = setSessionFromAuthResponse(response.data)
    if (!session) {
      pageError.value = '登录信息处理失败，请重新登录一次'
      return
    }

    if (session.authRole === AUTH_ROLES.STUDENT) {
      const profileResponse = await getStudentProfile(session.userId)
      if (profileResponse?.code === 200 && profileResponse?.data) {
        updateSessionRawUser(profileResponse.data)
      }
    }

    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''

    showActionSuccess('登录成功，正在进入系统')
    await router.replace(redirect || resolveHomePath(session.authRole))
  } catch (error) {
    pageError.value = resolveUserFacingError(error, '当前无法连接系统，请稍后再试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-view {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  background:
    radial-gradient(circle at top left, rgba(79, 175, 143, 0.24), transparent 38%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(232, 243, 237, 0.94));
}

.login-view__hero,
.login-view__panel {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
}

.login-view__hero::before,
.login-view__hero::after {
  content: '';
  position: absolute;
  border-radius: 999px;
  background: rgba(79, 175, 143, 0.12);
}

.login-view__hero::before {
  width: 220px;
  height: 220px;
  top: 10%;
  left: 10%;
}

.login-view__hero::after {
  width: 160px;
  height: 160px;
  bottom: 12%;
  right: 14%;
  background: rgba(255, 200, 87, 0.18);
}

.login-view__hero-inner {
  max-width: 520px;
  position: relative;
  z-index: 1;
}

.hero-tag {
  display: inline-flex;
  padding: 8px 14px;
  border-radius: 999px;
  color: var(--brand-primary-deep);
  background: rgba(255, 255, 255, 0.88);
  box-shadow: var(--shadow-card);
}

.login-view h1 {
  margin: 24px 0 16px;
  font-size: clamp(42px, 5vw, 64px);
  line-height: 1.04;
}

.login-view p {
  line-height: 1.9;
  color: var(--text-secondary);
}

.hero-points {
  margin: 28px 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.hero-points li {
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.74);
  box-shadow: var(--shadow-card);
}

.login-card {
  width: min(460px, 100%);
  padding: 28px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-soft);
}

.login-card__header {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 18px;
}

.login-card__badge {
  width: 58px;
  height: 58px;
  border-radius: 20px;
  display: grid;
  place-items: center;
  color: white;
  font-size: 24px;
  background: linear-gradient(140deg, var(--brand-primary), var(--brand-primary-deep));
}

.login-card__header h2 {
  margin: 0;
  font-size: 28px;
}

.login-card__header p {
  margin: 8px 0 0;
}

.login-role-group {
  display: flex;
  width: 100%;
}

.login-role-group :deep(.el-radio-button) {
  flex: 1;
}

.login-role-group :deep(.el-radio-button__inner) {
  width: 100%;
}

.login-card__submit {
  width: 100%;
  height: 48px;
  margin-top: 6px;
}

.login-card__footer {
  margin-top: 18px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  color: var(--text-muted);
  font-size: 12px;
}

@media (max-width: 900px) {
  .login-view {
    grid-template-columns: 1fr;
  }

  .login-view__hero {
    padding-bottom: 0;
  }
}

@media (max-width: 640px) {
  .login-view__hero,
  .login-view__panel {
    padding: 18px;
  }

  .login-card {
    padding: 20px;
    border-radius: 22px;
  }

  .login-view h1 {
    font-size: 40px;
  }

  .login-role-group {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
