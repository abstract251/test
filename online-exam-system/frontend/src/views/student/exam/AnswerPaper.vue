<template>
  <div v-if="exam" class="student-page">
    <PageContainer>
      <PageHeader
        title="在线答题"
        description="题目来自本场冻结快照；答案将自动暂存，请在考试结束前交卷。"
      >
        <template #actions>
          <el-button @click="router.push(`/student/exams/${exam.examCode}`)">返回考试详情</el-button>
        </template>
      </PageHeader>

      <ExamMetaPanel :exam="exam" />
      <el-alert v-if="bootError" :title="bootError" type="error" show-icon :closable="false" />
    </PageContainer>

    <PageContainer v-if="!bootError && ready">
      <div class="answer-layout">
        <div class="answer-layout__content">
          <section v-for="type in [1, 2, 3]" :key="type" class="q-block">
            <h3>{{ typeTitle(type) }}</h3>
            <article v-for="q in questionsOf(type)" :key="q.questionId" class="q-item">
              <p class="q-stem">{{ q.question }}</p>
              <div v-if="type === 1" class="q-opts">
                <el-radio-group v-model="answers[answerKey(type, q.questionId)]">
                  <el-radio v-if="q.answerA" label="A">A. {{ q.answerA }}</el-radio>
                  <el-radio v-if="q.answerB" label="B">B. {{ q.answerB }}</el-radio>
                  <el-radio v-if="q.answerC" label="C">C. {{ q.answerC }}</el-radio>
                  <el-radio v-if="q.answerD" label="D">D. {{ q.answerD }}</el-radio>
                </el-radio-group>
              </div>
              <div v-else-if="type === 2">
                <el-input
                  v-model="answers[answerKey(type, q.questionId)]"
                  placeholder="请填写答案"
                  clearable
                />
              </div>
              <div v-else class="q-opts">
                <el-radio-group v-model="answers[answerKey(type, q.questionId)]">
                  <el-radio label="正确">正确</el-radio>
                  <el-radio label="错误">错误</el-radio>
                </el-radio-group>
              </div>
            </article>
          </section>
        </div>

        <aside class="answer-layout__sidebar">
          <StatusCard label="作答状态" :value="submitting ? '交卷中…' : '进行中'" hint="请勿关闭页面前交卷" />
          <StatusCard label="剩余时间" :value="remainLabel" hint="以服务器时间为准" />
          <el-button type="primary" :loading="submitting" :disabled="!ready || ended" @click="handleSubmit">
            {{ ended ? '考试已结束' : '交卷' }}
          </el-button>
        </aside>
      </div>
    </PageContainer>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusCard from '@/components/common/StatusCard.vue'
import ExamMetaPanel from '@/components/exam/ExamMetaPanel.vue'
import {
  getExamById,
  saveStudentExamAnswers,
  startStudentExamSession,
  submitStudentExamSession
} from '@/api/examApi'
import { QUESTION_TYPE_LABELS } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const exam = ref(null)
const ready = ref(false)
const bootError = ref('')
const answers = reactive({})
const sessionPayload = ref(null)
const serverSkewMs = ref(0)
const remainLabel = ref('--:--')
const submitting = ref(false)
const ended = ref(false)

let saveTimer = null
let tickTimer = null

function answerKey(type, qid) {
  return `${type}_${qid}`
}

function typeTitle(type) {
  return QUESTION_TYPE_LABELS[type] || `题型${type}`
}

function questionsOf(type) {
  const paper = sessionPayload.value?.paper
  if (!paper) {
    return []
  }
  return paper[type] || paper[String(type)] || []
}

function scheduleSave() {
  if (saveTimer) {
    clearTimeout(saveTimer)
  }
  saveTimer = setTimeout(async () => {
    try {
      await saveStudentExamAnswers(route.params.examCode, { ...answers })
    } catch {
      /* 忽略 */
    }
  }, 900)
}

function updateClock() {
  const end = sessionPayload.value?.windowEndAt
  if (!end) {
    remainLabel.value = '--:--'
    return
  }
  const endAt = dayjs(end)
  const now = dayjs(Date.now() + serverSkewMs.value)
  const sec = endAt.diff(now, 'second')
  if (sec <= 0) {
    remainLabel.value = '已结束'
    ended.value = true
    return
  }
  const m = Math.floor(sec / 60)
  const s = sec % 60
  remainLabel.value = `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

watch(
  () => ({ ...answers }),
  () => {
    if (ready.value && !ended.value) {
      scheduleSave()
    }
  },
  { deep: true }
)

async function flushSave() {
  if (!ready.value || ended.value) {
    return
  }
  try {
    await saveStudentExamAnswers(route.params.examCode, { ...answers })
  } catch {
    /* 忽略 */
  }
}

async function bootstrap() {
  try {
    const examRes = await getExamById(route.params.examCode)
    if (examRes.code !== 200 || !examRes.data) {
      bootError.value = examRes.message || '无法加载考试'
      return
    }
    exam.value = examRes.data

    const startRes = await startStudentExamSession(route.params.examCode)
    if (startRes.code !== 200 || !startRes.data) {
      bootError.value = startRes.message || '无法进入考试（时间、资格或快照未就绪）'
      return
    }
    sessionPayload.value = startRes.data
    const srv = startRes.data.serverTime
    if (srv) {
      serverSkewMs.value = dayjs(srv).valueOf() - Date.now()
    }
    Object.keys(answers).forEach((k) => delete answers[k])
    Object.assign(answers, startRes.data.answers || {})
    ready.value = true
    updateClock()
    tickTimer = setInterval(updateClock, 1000)
  } catch {
    bootError.value = '加载失败，请稍后重试'
  }
}

async function handleSubmit() {
  if (ended.value) {
    return
  }
  try {
    await ElMessageBox.confirm('确定交卷吗？提交后不可修改。', '交卷确认', {
      type: 'warning',
      confirmButtonText: '交卷',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  submitting.value = true
  await flushSave()
  try {
    const res = await submitStudentExamSession(route.params.examCode)
    if (res.code === 200 && res.data) {
      ElMessage.success(`交卷成功，得分 ${res.data.etScore} / ${res.data.maxScore}`)
      router.push('/student/scores')
      return
    }
    ElMessage.error(res.message || '交卷失败')
  } catch {
    ElMessage.error('交卷失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(bootstrap)

onBeforeUnmount(() => {
  if (saveTimer) {
    clearTimeout(saveTimer)
  }
  if (tickTimer) {
    clearInterval(tickTimer)
  }
  flushSave()
})
</script>

<style scoped>
.student-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.answer-layout {
  display: grid;
  gap: 18px;
  grid-template-columns: minmax(0, 1fr) 280px;
}

.answer-layout__content {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.q-block h3 {
  margin: 0 0 12px;
  font-size: 1.05rem;
}

.q-item {
  padding: 14px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.q-stem {
  margin: 0 0 10px;
  line-height: 1.5;
}

.q-opts :deep(.el-radio) {
  display: flex;
  margin-right: 0;
  margin-bottom: 6px;
}

.answer-layout__sidebar {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

@media (max-width: 900px) {
  .answer-layout {
    grid-template-columns: 1fr;
  }
}
</style>
