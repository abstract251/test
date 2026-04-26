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
          <el-select v-model="form.sex" placeholder="请选择性别">
            <el-option
              v-for="item in sexOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
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
        <el-button type="primary" :loading="saving" :disabled="saving" @click="handleSubmit">
          保存资料
        </el-button>
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
import { normalizeSex, SEX_OPTIONS } from '@/utils/constants'
import { REGEX, patternRule, requiredRule } from '@/utils/validators'

const { session, syncSession } = useAuthSession()
const formRef = ref(null)
const saving = ref(false)
const sexOptions = SEX_OPTIONS

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
  sex: '男',
  role: '2'
})

const rules = {
  studentName: [
    requiredRule('请输入姓名'),
    patternRule(/^.{2,20}$/, '姓名长度需为 2-20 个字符')
  ],
  institute: [
    requiredRule('请输入学院'),
    patternRule(REGEX.institute, '学院格式不正确，支持中文、字母、数字、空格和括号')
  ],
  major: [
    requiredRule('请输入专业'),
    patternRule(REGEX.major, '专业格式不正确，支持中文、字母、数字、空格和括号')
  ],
  grade: [
    requiredRule('请输入年级'),
    patternRule(/^\d{4}$/, '年级格式不正确，应为 4 位数字')
  ],
  clazz: [
    requiredRule('请输入班级'),
    patternRule(/^.{1,20}$/, '班级长度不能超过 20 个字符')
  ],
  sex: [requiredRule('请选择性别', 'change')],
  tel: [
    requiredRule('请输入手机号'),
    patternRule(REGEX.phone, '手机号格式不正确，应为 11 位数字')
  ],
  email: [
    requiredRule('请输入邮箱'),
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  cardId: [
    requiredRule('请输入身份证号'),
    patternRule(REGEX.idCard, '身份证号格式不正确')
  ]
}

async function fetchProfile() {
  if (!session.value?.userId) {
    ElMessage.error('当前登录信息缺失，请重新登录')
    return
  }

  try {
    const response = await getStudentProfile(session.value.userId)
    if (response.code === 200 && response.data) {
      Object.assign(form, {
        ...response.data,
        sex: normalizeSex(response.data.sex)
      })
      return
    }
    ElMessage.error(response.message || '加载个人资料失败')
  } catch (error) {
    ElMessage.error('加载个人资料失败，请稍后重试')
  }
}

async function handleSubmit() {
  if (saving.value) {
    return
  }

  if (!session.value?.userId) {
    ElMessage.error('当前登录信息缺失，请重新登录')
    return
  }

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const payload = { ...form, studentId: session.value.userId, role: '2', sex: normalizeSex(form.sex) }
    const response = await updateStudentProfile(payload)
    if (response.code === 200) {
      updateSessionRawUser(payload)
      syncSession()
      ElMessage.success('个人资料已更新')
      return
    }
    ElMessage.error(response.message || '保存失败，请检查后重试')
  } catch (error) {
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
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
