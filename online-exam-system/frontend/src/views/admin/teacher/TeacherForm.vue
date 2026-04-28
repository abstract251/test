<template>
  <PageContainer>
    <PageHeader
      :title="isEdit ? '编辑教师' : '新增教师'"
      description="请填写教师的基本信息。创建后，教师可使用默认密码登录并自行修改。"
    />

    <PageNotice v-if="pageError" type="error" title="页面暂时打不开" :description="pageError" />
    <FormErrorSummary v-if="formSummary.title || formSummary.items.length" :title="formSummary.title" :items="formSummary.items" />

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
import FormErrorSummary from '@/components/common/FormErrorSummary.vue'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import PageNotice from '@/components/common/PageNotice.vue'
import TeacherFormFields from '@/components/forms/TeacherFormFields.vue'
import { createTeacher, getTeacherById, updateTeacher } from '@/api/teacherApi'
import { DEFAULT_PASSWORD, normalizeSex } from '@/utils/constants'
import { buildFormSummary, resolveUserFacingError, scrollToFirstError, showActionSuccess } from '@/utils/feedback'
import { REGEX, patternRule, requiredRule } from '@/utils/validators'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const pageError = ref('')
const formSummary = reactive({
  title: '',
  items: []
})

const isEdit = computed(() => Boolean(route.params.teacherId))

const form = reactive({
  teacherId: '',
  teacherName: '',
  institute: '',
  sex: '男',
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
      Object.assign(form, {
        ...response.data,
        sex: normalizeSex(response.data.sex)
      })
      return
    }
    pageError.value = resolveUserFacingError(response, '请稍后再试')
  } catch (error) {
    pageError.value = resolveUserFacingError(error, '请稍后再试')
  }
}

async function handleSubmit() {
  pageError.value = ''
  formSummary.title = ''
  formSummary.items = []

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    Object.assign(formSummary, buildFormSummary(formRef, '请先补全教师信息'))
    await scrollToFirstError(formRef)
    return
  }

  try {
    const response = isEdit.value
      ? await updateTeacher({ ...form, sex: normalizeSex(form.sex) })
      : await createTeacher({ ...form, role: '1', sex: normalizeSex(form.sex) })

    if (response.code === 200) {
      showActionSuccess(isEdit.value ? '教师信息已保存' : '教师账号已创建')
      router.push('/console/admin/teachers')
      return
    }

    formSummary.title = resolveUserFacingError(response, '教师信息还没有保存成功')
  } catch (error) {
    formSummary.title = resolveUserFacingError(error, '教师信息还没有保存成功')
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
