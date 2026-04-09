<template>
  <PageContainer>
    <PageHeader
      :title="isEdit ? '编辑学生' : '新增学生'"
      description="学生账号默认密码为 123456，可按需调整。"
    />

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <StudentFormFields :model="form" :is-edit="isEdit" />

      <div class="actions">
        <el-button @click="router.push('/console/teacher/students')">取消</el-button>
        <el-button type="primary" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '创建学生' }}
        </el-button>
      </div>
    </el-form>
  </PageContainer>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StudentFormFields from '@/components/forms/StudentFormFields.vue'
import { createStudent, getStudentById, updateStudent } from '@/api/studentApi'
import { DEFAULT_PASSWORD } from '@/utils/constants'
import { REGEX, patternRule, requiredRule } from '@/utils/validators'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)

const isEdit = computed(() => Boolean(route.params.studentId))

const form = reactive({
  studentId: '',
  studentName: '',
  grade: '',
  major: '',
  clazz: '',
  institute: '',
  tel: '',
  email: '',
  pwd: DEFAULT_PASSWORD,
  cardId: '',
  sex: 'M',
  role: '2'
})

const rules = {
  studentName: [
    requiredRule('请输入学生姓名'),
    patternRule(/^.{2,20}$/, '姓名长度需为 2-20 个字符')
  ],
  grade: [requiredRule('请选择或输入年级', 'change')],
  major: [requiredRule('请选择或输入专业', 'change')],
  clazz: [
    requiredRule('请输入班级'),
    patternRule(/^.{1,20}$/, '班级长度不能超过 20 个字符')
  ],
  institute: [requiredRule('请选择或输入学院', 'change')],
  tel: [
    requiredRule('请输入手机号'),
    patternRule(REGEX.phone, '手机号格式不正确，应为 11 位数字')
  ],
  email: [
    requiredRule('请输入邮箱'),
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  pwd: [
    requiredRule('请输入密码'),
    patternRule(REGEX.password, '密码需为 6-32 位且不能包含空格')
  ],
  cardId: [
    requiredRule('请输入身份证号'),
    patternRule(REGEX.idCard, '身份证号格式不正确')
  ]
}

async function fetchStudent() {
  if (!isEdit.value) {
    return
  }

  try {
    const response = await getStudentById(route.params.studentId)
    if (response.code === 200 && response.data) {
      Object.assign(form, response.data)
      return
    }
    ElMessage.error(response.message || '加载学生信息失败')
  } catch (error) {
    ElMessage.error('加载学生信息失败，请稍后重试')
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  try {
    const payload = { ...form, role: '2' }
    const response = isEdit.value
      ? await updateStudent(payload)
      : await createStudent(payload)

    if (response.code === 200) {
      ElMessage.success(isEdit.value ? '学生信息已更新' : '学生已创建')
      router.push('/console/teacher/students')
      return
    }

    ElMessage.error(response.message || '保存失败，请检查后重试')
  } catch (error) {
    ElMessage.error('保存失败，请稍后重试')
  }
}

onMounted(fetchStudent)
</script>

<style scoped>
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}
</style>
