<template>
  <div class="login-view">
    <div class="login-view__hero">
      <div class="login-view__hero-inner">
        <span class="hero-tag">在线考试</span>
        <h1>在线考试系统</h1>
        <p>
          支持管理员、教师、学生三类账号登录，登录后自动进入对应工作区。
          请使用学校分配的账号密码进行操作。
        </p>
        <ul class="hero-points">
          <li>登录后自动识别角色并跳转首页</li>
          <li>学生考试中心支持手机端访问</li>
          <li>支持在页面内修改密码与退出登录</li>
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
            <p>请输入账号和密码，系统会自动识别管理员、教师或学生身份。</p>
          </div>
        </div>

        <el-form @submit.prevent="handleLogin">
          <el-form-item label="账号">
            <el-input
              v-model="form.username"
              placeholder="请输入数字账号"
              inputmode="numeric"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="密码">
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
          <span>如无法登录，请联系管理员</span>
          <span>默认密码可在首次登录后修改</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Lock, School, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login } from '@/api/authApi'
import { resolveHomePath, setSessionFromUser } from '@/utils/auth'

const route = useRoute()
const router = useRouter()

const submitting = ref(false)
const form = reactive({
  username: '',
  password: ''
})

function resolveLoginErrorMessage(response) {
  if (response?.code === 400) {
    return '账号或密码错误'
  }

  if (response?.message) {
    return response.message
  }

  return '登录失败，请稍后重试'
}

async function handleLogin() {
  if (!/^\d+$/.test(form.username)) {
    ElMessage.warning('账号必须是数字')
    return
  }

  if (!form.password.trim()) {
    ElMessage.warning('请输入密码')
    return
  }

  submitting.value = true

  try {
    const response = await login({
      username: Number(form.username),
      password: form.password
    })

    if (response.code !== 200 || !response.data) {
      ElMessage.error(resolveLoginErrorMessage(response))
      return
    }

    const session = setSessionFromUser(response.data)
    if (!session) {
      ElMessage.error('登录成功，但会话初始化失败，请联系管理员检查返回数据')
      return
    }
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''

    ElMessage.success('登录成功')
    await router.replace(redirect || resolveHomePath(session.role))
  } catch (error) {
    ElMessage.error(error?.message || '登录请求失败，请确认后端服务已启动')
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
}
</style>
