<template>
  <PageContainer v-if="ready">
    <PageHeader
      :title="isEdit ? '编辑题目' : '新增题目'"
      description="支持选择题、填空题和判断题。"
    />

    <el-form label-position="top">
      <QuestionFormFields :model="form" :is-edit="isEdit" />

      <div class="actions">
        <el-button @click="handleCancel">{{ backActionLabel }}</el-button>
        <el-button type="primary" @click="handleSubmit">
          {{ isEdit ? '保存题目' : '创建题目' }}
        </el-button>
      </div>
    </el-form>
  </PageContainer>

  <PageContainer v-else>
    <EmptyState
      description="未找到可编辑题目，请从题库列表重新进入。"
      emoji="🧩"
    >
      <el-button type="primary" @click="navigateBack">
        {{ backActionLabel }}
      </el-button>
    </EmptyState>
  </PageContainer>
</template>

<script setup>
import { computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import QuestionFormFields from '@/components/forms/QuestionFormFields.vue'
import { createQuestion, updateQuestion } from '@/api/questionApi'
import { clearQuestionDraft, getSession, readQuestionDraft } from '@/utils/auth'
import { buildConsolePath } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const consoleRole = getSession()?.role
const draft = readQuestionDraft()
const questionWorkbenchPath = buildConsolePath(consoleRole, 'questions')
const isEdit = computed(() => String(route.name || '').endsWith('question-edit'))
const returnToPath = computed(() => {
  return typeof route.query.returnTo === 'string' ? route.query.returnTo : ''
})
const backActionLabel = computed(() => (returnToPath.value ? '返回题库列表' : '返回工作台'))

const form = reactive({
  questionId: draft?.questionId || null,
  type: draft?.type || Number(route.query.type || 1),
  subject: draft?.subject || String(route.query.subject || ''),
  section: draft?.section || '',
  question: draft?.question || '',
  level: draft?.level || '基础',
  answerA: draft?.answerA || '',
  answerB: draft?.answerB || '',
  answerC: draft?.answerC || '',
  answerD: draft?.answerD || '',
  rightAnswer: draft?.rightAnswer || 'A',
  answer: draft?.answer || 'T',
  analysis: draft?.analysis || ''
})

const ready = computed(() => !isEdit.value || Boolean(draft))

function buildWorkbenchTarget() {
  const query = {}
  if (form.subject.trim()) {
    query.subject = form.subject.trim()
  }

  return {
    path: questionWorkbenchPath,
    query
  }
}

function navigateBack() {
  clearQuestionDraft()

  if (returnToPath.value) {
    router.push(returnToPath.value)
    return
  }

  router.push(buildWorkbenchTarget())
}

function buildPayload() {
  const basePayload = {
    subject: form.subject,
    section: form.section,
    question: form.question,
    level: form.level,
    analysis: form.analysis
  }

  if (Number(form.type) === 1) {
    return {
      ...basePayload,
      answerA: form.answerA,
      answerB: form.answerB,
      answerC: form.answerC,
      answerD: form.answerD,
      rightAnswer: form.rightAnswer
    }
  }

  return {
    ...basePayload,
    answer: form.answer
  }
}

function validateForm() {
  if (!form.subject.trim() || !form.question.trim() || !form.section.trim()) {
    ElMessage.warning('请先填写科目、章节和题干')
    return false
  }

  if (Number(form.type) === 1) {
    const requiredOptions = [form.answerA, form.answerB, form.answerC, form.answerD]
    if (requiredOptions.some((item) => !String(item).trim()) || !form.rightAnswer) {
      ElMessage.warning('请选择题需要完整填写四个选项和正确答案')
      return false
    }
  } else if (!String(form.answer).trim()) {
    ElMessage.warning('请填写参考答案')
    return false
  }

  return true
}

async function handleSubmit() {
  if (!validateForm()) {
    return
  }

  const payload = buildPayload()
  const response = isEdit.value
    ? await updateQuestion(Number(form.type), { ...payload, questionId: form.questionId })
    : await createQuestion(Number(form.type), payload)

  if (response.code === 200) {
    ElMessage.success(isEdit.value ? '题目已更新' : '题目已创建')
    navigateBack()
  }
}

function handleCancel() {
  navigateBack()
}
</script>

<style scoped>
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
