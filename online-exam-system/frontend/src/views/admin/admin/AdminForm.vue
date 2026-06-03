<template>
  <PageContainer>
    <PageHeader
      :title="isEdit ? '编辑管理员' : '新增管理员'"
      :description="isEdit ? '调整管理员信息并保存。' : '填写管理员基础信息后保存，编号会由系统自动生成。'"
    />

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
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import AdminFormFields from '@/components/forms/AdminFormFields.vue'
import { createAdmin, getAdminById, updateAdmin } from '@/api/adminApi'
import { getSession } from '@/utils/auth'
import { buildConsolePath, normalizeSex } from '@/utils/constants'
import { REGEX, patternRule, requiredRule } from '@/utils/validators'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const consoleRole = getSession()?.authRole
const adminListPath = buildConsolePath(consoleRole, 'admins')

const isEdit = computed(() => Boolean(route.params.adminId))

const form = reactive({
  adminId: '',
  adminName: '',
  sex: '男',
  tel: '',
  email: '',
  pwd: '',
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
  pwd: computed(() => {
    if (isEdit.value) {
      // 编辑模式：密码为可选，不填写则不修改
      return [
        { validator: validatePasswordEdit, trigger: 'blur' }
      ]
    }
    // 新增模式：密码必填
    return [
      requiredRule('请输入密码'),
      patternRule(REGEX.password, '密码需为 6-32 位且不能包含空格')
    ]
  }),
  cardId: [
    requiredRule('请输入身份证号'),
    patternRule(REGEX.idCard, '身份证号格式不正确')
  ]
}

// 编辑模式下的密码验证
function validatePasswordEdit(rule, value, callback) {
  if (value && value.trim() !== '') {
    if (!REGEX.password.test(value)) {
      callback(new Error('密码需为 6-32 位且不能包含空格'))
    }
  }
  callback()
}

async function fetchAdmin() {
  if (!isEdit.value) {
    return
  }

  try {
    const response = await getAdminById(route.params.adminId)
    if (response.code === 200 && response.data) {
      Object.assign(form, response.data)
      form.sex = normalizeSex(response.data.sex)
      form.pwd = '' // 编辑时密码框保持为空
      return
    }
    ElMessage.error(response.message || '加载管理员信息失败')
  } catch (error) {
    ElMessage.error('加载管理员信息失败，请稍后重试')
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  try {
    // 构建payload
    const payload = {
      ...form,
      role: '0',
      sex: normalizeSex(form.sex)
    }
    
    // 编辑模式下，如果密码为空，则移除pwd字段，不更新密码
    if (isEdit.value && (!payload.pwd || payload.pwd.trim() === '')) {
      delete payload.pwd
    }

    const response = isEdit.value
      ? await updateAdmin(route.params.adminId, payload)
      : await createAdmin(payload)

    if (response.code === 200) {
      ElMessage.success(isEdit.value ? '管理员信息已更新' : '管理员已创建')
      router.push(adminListPath)
      return
    }

    ElMessage.error(response.message || '保存失败，请检查后重试')
  } catch (error) {
    const message = error?.response?.data?.message
    ElMessage.error(message || '保存失败，请稍后重试')
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
