<template>
  <PageContainer>
    <PageHeader
      title="个人资料"
      description="这里可以查看和编辑你的基础信息。修改后会同步更新当前前端会话。"
    />

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <div class="form-grid">
        <el-form-item label="学号">
          <el-input :model-value="form.studentId" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="studentName">
          <el-input v-model="form.studentName" />
        </el-form-item>
        <el-form-item label="学院" prop="institute">
          <el-input v-model="form.institute" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="form.major" />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="form.grade" />
        </el-form-item>
        <el-form-item label="班级" prop="clazz">
          <el-input v-model="form.clazz" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-select v-model="form.sex">
            <el-option label="男" value="M" />
            <el-option label="女" value="F" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="tel">
          <el-input v-model="form.tel" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="身份证号" prop="cardId">
          <el-input v-model="form.cardId" />
        </el-form-item>
      </div>

      <div class="actions">
        <el-button type="primary" @click="handleSubmit">保存资料</el-button>
      </div>
    </el-form>
  </PageContainer>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import { useAuthSession } from '@/composables/useAuthSession'
import { getStudentProfile, updateStudentProfile } from '@/api/profileApi'
import { updateSessionRawUser } from '@/utils/auth'

const { session, syncSession } = useAuthSession()
const formRef = ref(null)

const form = reactive({
  studentId: '',
  studentName: '',
  grade: '',
  major: '',
  clazz: '',
  institute: '',
  tel: '',
  email: '',
  pwd: '',
  cardId: '',
  sex: 'M',
  role: '2'
})

const rules = {
  studentName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  institute: [{ required: true, message: '请输入学院', trigger: 'blur' }],
  major: [{ required: true, message: '请输入专业', trigger: 'blur' }],
  grade: [{ required: true, message: '请输入年级', trigger: 'blur' }],
  clazz: [{ required: true, message: '请输入班级', trigger: 'blur' }],
  tel: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  cardId: [{ required: true, message: '请输入身份证号', trigger: 'blur' }]
}

async function fetchProfile() {
  const response = await getStudentProfile(session.value.userId)
  if (response.code === 200 && response.data) {
    Object.assign(form, response.data)
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  const response = await updateStudentProfile({ ...form, role: '2' })
  if (response.code === 200) {
    updateSessionRawUser({ ...form, role: '2' })
    syncSession()
    ElMessage.success('个人资料已更新')
  }
}

onMounted(fetchProfile)
</script>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 0 18px;
}

.actions {
  display: flex;
  justify-content: flex-end;
}
</style>
