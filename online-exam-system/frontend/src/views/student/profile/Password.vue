<template>
  <PageContainer>
    <PageHeader
      title="修改密码"
      description="请输入新密码并确认后提交。"
    />

    <el-form label-position="top" class="password-form">
      <el-form-item label="新密码">
        <el-input v-model="form.password" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认新密码">
        <el-input v-model="form.confirmPassword" type="password" show-password />
      </el-form-item>
      <div class="actions">
        <el-button type="primary" @click="handleSubmit">保存新密码</el-button>
      </div>
    </el-form>
  </PageContainer>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import { useAuthSession } from '@/composables/useAuthSession'
import { changeStudentPassword } from '@/api/profileApi'

const { session } = useAuthSession()

const form = reactive({
  password: '',
  confirmPassword: ''
})

async function handleSubmit() {
  if (!form.password.trim()) {
    ElMessage.warning('请输入新密码')
    return
  }

  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  const response = await changeStudentPassword({
    studentId: session.value.userId,
    pwd: form.password
  })

  if (response.code === 200) {
    ElMessage.success('密码已更新')
    form.password = ''
    form.confirmPassword = ''
  }
}
</script>

<style scoped>
.password-form {
  max-width: 420px;
}

.actions {
  display: flex;
  justify-content: flex-end;
}
</style>
