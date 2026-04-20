<template>
  <PageContainer>
    <PageHeader
      :title="headerTitle"
      description="请先确认考试时间、范围和说明。考试进入锁定阶段后，可调整的内容会明显减少。"
    />

    <PageNotice v-if="pageError" type="error" title="考试信息暂时打不开" :description="pageError" />
    <FormErrorSummary v-if="formSummary.title || formSummary.items.length" :title="formSummary.title" :items="formSummary.items" />

    <el-form ref="formRef" :model="form" :rules="dynamicRules" label-position="top">
      <ExamFormFields
        :model="form"
        :freeze-locked="freezeLocked"
        :revoked="revoked"
        :baseline-total-time="baselineTotalTime"
      />

      <div class="summary">
        <span>试卷编号：{{ form.paperId || '创建时生成' }}</span>
        <span>当前总分：{{ form.totalScore || 0 }}</span>
      </div>

      <div v-if="!revoked" class="actions">
        <el-button @click="router.push(examListPath)">取消</el-button>
        <el-button type="primary" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '创建考试' }}
        </el-button>
      </div>
    </el-form>
  </PageContainer>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import FormErrorSummary from '@/components/common/FormErrorSummary.vue'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import PageNotice from '@/components/common/PageNotice.vue'
import ExamFormFields from '@/components/forms/ExamFormFields.vue'
import { addExam, getExamById, getLatestPaperId, updateExam } from '@/api/examApi'
import { getSession } from '@/utils/auth'
import { buildConsolePath } from '@/utils/constants'
import { toBackendDateTime, toPickerDateTime } from '@/utils/date'
import { buildFormSummary, resolveUserFacingError, scrollToFirstError, showActionSuccess } from '@/utils/feedback'
import { patternRule, requiredRule } from '@/utils/validators'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const pageError = ref('')
const formSummary = reactive({
  title: '',
  items: []
})
const consoleRole = getSession()?.authRole
const examListPath = buildConsolePath(consoleRole, 'exams')

const isEdit = computed(() => Boolean(route.params.examCode))

const paperLocked = ref(false)
const revoked = ref(false)
const baselineTotalTime = ref(1)

const freezeLocked = computed(() => paperLocked.value && !revoked.value)

const headerTitle = computed(() => {
  if (!isEdit.value) {
    return '新增考试'
  }
  if (revoked.value) {
    return '编辑考试（已撤销）'
  }
  if (freezeLocked.value) {
    return '编辑考试（已锁定）'
  }
  return '编辑考试'
})

const form = reactive({
  examCode: null,
  description: '',
  source: '',
  paperId: null,
  examDate: '',
  totalTime: 120,
  grade: '',
  term: '1',
  major: '',
  institute: '',
  totalScore: 0,
  type: '正式考试',
  tips: ''
})

const rules = {
  source: [
    requiredRule('请输入科目'),
    patternRule(/^.{2,30}$/, '科目长度需为 2-30 个字符')
  ],
  description: [
    requiredRule('请输入考试说明'),
    patternRule(/^.{2,100}$/, '考试说明长度需为 2-100 个字符')
  ],
  institute: [requiredRule('请选择或输入学院', 'change')],
  major: [requiredRule('请选择或输入专业', 'change')],
  grade: [requiredRule('请选择或输入年级', 'change')],
  examDate: [requiredRule('请选择开考时间', 'change')],
  totalTime: [
    requiredRule('请输入考试时长', 'change'),
    {
      validator: (_, value, callback) => {
        if (!Number.isInteger(value) || value < 1 || value > 300) {
          callback(new Error('考试时长需在 1-300 分钟之间'))
          return
        }
        if (freezeLocked.value && value < baselineTotalTime.value) {
          callback(new Error(`锁定后时长不能少于 ${baselineTotalTime.value} 分钟`))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  type: [requiredRule('请选择或输入考试类型', 'change')],
  tips: [
    requiredRule('请输入考生提示'),
    patternRule(/^.{2,200}$/, '考生提示长度需为 2-200 个字符')
  ]
}

const dynamicRules = computed(() => {
  if (revoked.value) {
    return {}
  }
  if (freezeLocked.value) {
    return {
      description: rules.description,
      tips: rules.tips,
      totalTime: rules.totalTime
    }
  }
  return rules
})

async function fetchExam() {
  if (!isEdit.value) {
    return
  }

  try {
    const response = await getExamById(route.params.examCode)
    if (response.code === 200 && response.data) {
      const d = response.data
      paperLocked.value = !!d.paperLocked
      revoked.value = !!d.revokedAt
      baselineTotalTime.value = Number(d.totalTime) > 0 ? Number(d.totalTime) : 1
      Object.assign(form, d, {
        examDate: toPickerDateTime(d.examDate)
      })
      return
    }
    pageError.value = resolveUserFacingError(response, '请稍后再试')
  } catch (error) {
    pageError.value = resolveUserFacingError(error, '请稍后再试')
  }
}

async function assignPaperId() {
  if (form.paperId) {
    return
  }

  try {
    const response = await getLatestPaperId()
    const latest = response.code === 200 && response.data?.paperId ? Number(response.data.paperId) : 1000
    form.paperId = latest + 1
  } catch {
    form.paperId = 1001
  }
}

function buildPayload() {
  if (freezeLocked.value) {
    return {
      examCode: form.examCode,
      description: form.description,
      tips: form.tips,
      totalTime: form.totalTime
    }
  }

  const payload = {
    ...form,
    examDate: toBackendDateTime(form.examDate),
    totalScore: Number(form.totalScore || 0)
  }
  if (form.examDate) {
    const d = dayjs(form.examDate, 'YYYY-MM-DD HH:mm')
    if (d.isValid()) {
      payload.examStartAt = d.format('YYYY-MM-DDTHH:mm:00')
    }
  }
  return payload
}

async function handleSubmit() {
  if (revoked.value) {
    return
  }

  pageError.value = ''
  formSummary.title = ''
  formSummary.items = []

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    Object.assign(formSummary, buildFormSummary(formRef, '请先补全考试信息'))
    await scrollToFirstError(formRef)
    return
  }

  if (!isEdit.value) {
    await assignPaperId()
  }

  const payload = buildPayload()

  try {
    const response = isEdit.value ? await updateExam(payload) : await addExam(payload)

    if (response.code === 200) {
      showActionSuccess(isEdit.value ? '考试信息已保存' : '考试已创建')
      router.push(examListPath)
      return
    }

    formSummary.title = resolveUserFacingError(response, '考试信息还没有保存成功')
  } catch (error) {
    formSummary.title = resolveUserFacingError(error, '考试信息还没有保存成功')
  }
}

onMounted(fetchExam)
</script>

<style scoped>
.summary {
  display: flex;
  gap: 18px;
  flex-wrap: wrap;
  margin: 0 0 20px;
  color: var(--text-secondary);
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
