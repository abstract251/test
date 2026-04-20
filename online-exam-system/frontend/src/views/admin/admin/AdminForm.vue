<template>
  <PageContainer>
    <PageHeader
      :title="isEdit ? '编辑管理员' : '新增管理员'"
      :description="isEdit ? '请核对管理员信息后保存。' : '请填写管理员基础信息后保存。'"
    />

    <PageNotice v-if="pageError" type="error" title="页面暂时打不开" :description="pageError" />
    <FormErrorSummary v-if="formSummary.title || formSummary.items.length" :title="formSummary.title" :items="formSummary.items" />

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <AdminFormFields :model="form" :is-edit="isEdit" />

      <div class="actions">
        <el-button @click="router.push(adminListPath)">取消</el-button>
        <el-button type="primary" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '创建管理员' }}
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
import AdminFormFields from '@/components/forms/AdminFormFields.vue'
import { createAdmin, getAdminById, updateAdmin } from '@/api/adminApi'
import { getSession } from '@/utils/auth'
import { buildConsolePath, DEFAULT_PASSWORD, normalizeSex } from '@/utils/constants'
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
const consoleRole = getSession()?.authRole
const adminListPath = buildConsolePath(consoleRole, 'admins')

const isEdit = computed(() => Boolean(route.params.adminId))

const form = reactive({
  adminId: '',
  adminName: '',
  sex: '男',
  tel: '',
  email: '',
  pwd: DEFAULT_PASSWORD,
  cardId: '',
  role: '0'
})

const rules = {
  adminName: [
    requiredRule('请输入管理员姓名'),
    patternRule(/^.{2,20}$/, '姓名长度需为 2-20 个字符')
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
  pwd: [
    requiredRule('请输入密码'),
    patternRule(REGEX.password, '密码需为 6-32 位且不能包含空格')
  ],
  cardId: [
    requiredRule('请输入身份证号'),
    patternRule(REGEX.idCard, '身份证号格式不正确')
  ]
}

async function fetchAdmin() {
  if (!isEdit.value) {
    return
  }

  try {
    const response = await getAdminById(route.params.adminId)
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
    Object.assign(formSummary, buildFormSummary(formRef, '请先补全管理员信息'))
    await scrollToFirstError(formRef)
    return
  }

  try {
    const payload = {
      ...form,
      role: '0',
      sex: normalizeSex(form.sex)
    }
    const response = isEdit.value
      ? await updateAdmin(route.params.adminId, payload)
      : await createAdmin(payload)

    if (response.code === 200) {
      showActionSuccess(isEdit.value ? '管理员信息已保存' : '管理员账号已创建')
      router.push(adminListPath)
      return
    }

    formSummary.title = resolveUserFacingError(response, '管理员信息还没有保存成功')
  } catch (error) {
    formSummary.title = resolveUserFacingError(error, '管理员信息还没有保存成功')
  }
}

onMounted(fetchAdmin)
</script>

<style scoped>
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}
</style>
