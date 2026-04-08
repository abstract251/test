<template>
  <PageContainer>
    <PageHeader
      :title="isEdit ? '编辑教师' : '新增教师'"
      description="教师账号默认密码为 123456，也可以在创建或编辑时手动修改。"
    />

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <TeacherFormFields :model="form" :is-edit="isEdit" />

      <div class="actions">
        <el-button @click="router.push('/console/admin/teachers')">取消</el-button>
        <el-button type="primary" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '创建教师' }}
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
import TeacherFormFields from '@/components/forms/TeacherFormFields.vue'
import { createTeacher, getTeacherById, updateTeacher } from '@/api/teacherApi'
import { DEFAULT_PASSWORD } from '@/utils/constants'
import { REGEX, patternRule, requiredRule } from '@/utils/validators'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)

const isEdit = computed(() => Boolean(route.params.teacherId))

const form = reactive({
  teacherId: '',
  teacherName: '',
  institute: '',
  sex: 'M',
  tel: '',
  email: '',
  pwd: DEFAULT_PASSWORD,
  cardId: '',
  type: '',
  role: '1'
})

const rules = {
  teacherName: [
    requiredRule('请输入教师姓名'),
    patternRule(/^.{2,20}$/, '姓名长度需为 2-20 个字符')
  ],
  institute: [requiredRule('请选择或输入学院', 'change')],
  type: [requiredRule('请选择或输入职称', 'change')],
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
  ],
  pwd: [
    requiredRule('请输入密码'),
    patternRule(REGEX.password, '密码需为 6-32 位且不能包含空格')
  ]
}

async function fetchTeacher() {
  if (!isEdit.value) {
    return
  }
  try {
    const response = await getTeacherById(route.params.teacherId)
    if (response.code === 200 && response.data) {
      Object.assign(form, response.data)
      return
    }
    ElMessage.error(response.message || '加载教师信息失败')
  } catch (error) {
    ElMessage.error('加载教师信息失败，请稍后重试')
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  try {
    const response = isEdit.value
      ? await updateTeacher({ ...form })
      : await createTeacher({ ...form, role: '1' })

    if (response.code === 200) {
      ElMessage.success(isEdit.value ? '教师信息已更新' : '教师已创建')
      router.push('/console/admin/teachers')
      return
    }

    ElMessage.error(response.message || '保存失败，请检查后重试')
  } catch (error) {
    ElMessage.error('保存失败，请稍后重试')
  }
}

onMounted(fetchTeacher)
</script>

<style scoped>
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}
</style>
