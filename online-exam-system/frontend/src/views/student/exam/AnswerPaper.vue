<template>
  <div v-if="exam" class="student-page">
    <PageContainer>
      <PageHeader
        title="在线答题"
        description="题目已按本场考试固定，系统会自动暂存作答，请在结束前完成并交卷。"
      >
        <template #actions>
          <el-button @click="router.push(`/student/exams/${exam.examCode}`)">返回考试详情</el-button>
        </template>
      </PageHeader>

      <ExamMetaPanel :exam="examForPanel" />
      <el-alert v-if="bootError" :title="bootError" type="error" show-icon :closable="false" />
      <el-alert
        v-else-if="restoreNotice"
        :title="restoreNotice"
        type="success"
        show-icon
        :closable="false"
      />
    </PageContainer>

    <PageContainer v-if="!bootError && ready">
      <div class="answer-layout">
        <div class="answer-layout__content">
          <section v-for="type in [1, 2, 3]" :key="type" class="q-block">
            <div class="q-block__header">
              <h3>{{ typeTitle(type) }}</h3>
              <span>{{ `${questionsOf(type).length} 题` }}</span>
            </div>
            <article v-for="(q, index) in questionsOf(type)" :key="q.questionId" class="q-item">
              <p class="q-index">{{ `${typeTitle(type)} ${index + 1}` }}</p>
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
          <StatusCard
            label="已作答"
            :value="`${answeredQuestionCount}/${totalQuestionCount}`"
            :hint="answerProgressHint"
          />
          <StatusCard label="作答状态" :value="submitting ? '交卷中' : ended ? '已结束' : '进行中'" hint="请勿在未交卷时关闭页面" />
          <StatusCard label="暂存状态" :value="saveStatusLabel" :hint="saveStatusHint" />
          <StatusCard label="剩余时间" :value="remainLabel" hint="以服务器时间为准" />
          <div class="answer-layout__actions">
            <el-button type="primary" :loading="submitting" :disabled="!ready || ended" @click="handleSubmit">
              {{ ended ? '考试已结束' : '交卷' }}
            </el-button>
          </div>
        </aside>
      </div>
    </PageContainer>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
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
const restoreNotice = ref('')
const saveState = ref('idle')
const lastSavedAt = ref('')

const totalQuestionCount = computed(() => {
  return [1, 2, 3].reduce((sum, type) => sum + questionsOf(type).length, 0)
})

const answeredQuestionCount = computed(() => {
  return Object.values(answers).filter((value) => String(value || '').trim()).length
})

const answerProgressHint = computed(() => {
  if (!totalQuestionCount.value) {
    return '试题加载完成后会显示进度'
  }
  if (answeredQuestionCount.value >= totalQuestionCount.value) {
    return '已完成全部作答，请确认后交卷'
  }
  return `还有 ${totalQuestionCount.value - answeredQuestionCount.value} 题未作答`
})

const saveStatusLabel = computed(() => {
  if (saveState.value === 'saving') {
    return '暂存中'
  }
  if (saveState.value === 'saved') {
    return '已暂存'
  }
  if (saveState.value === 'failed') {
    return '暂存失败'
  }
  return '等待作答'
})

const saveStatusHint = computed(() => {
  if (saveState.value === 'saving') {
    return '答案变化后会自动写回服务器'
  }
  if (saveState.value === 'saved') {
    return lastSavedAt.value ? `最近暂存于 ${lastSavedAt.value}` : '答案已写回服务器'
  }
  if (saveState.value === 'failed') {
    return '稍后会继续尝试，交卷前也会再保存一次'
  }
  return '开始作答后将自动暂存'
})

const examForPanel = computed(() => {
  if (!exam.value) {
    return null
  }

  return {
    ...exam.value,
    windowEndAt: sessionPayload.value?.windowEndAt || exam.value.windowEndAt
  }
})

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

function updateSavedAt() {
  lastSavedAt.value = dayjs(Date.now() + serverSkewMs.value).format('YYYY-MM-DD HH:mm:ss')
}

async function persistAnswers() {
  if (!ready.value || ended.value) {
    return true
  }

  saveState.value = 'saving'
  try {
    const response = await saveStudentExamAnswers(route.params.examCode, { ...answers })
    if (response?.code === 200) {
      saveState.value = 'saved'
      updateSavedAt()
      return true
    }
    saveState.value = 'failed'
    return false
  } catch {
    saveState.value = 'failed'
    return false
  }
}

function scheduleSave() {
  if (saveTimer) {
    clearTimeout(saveTimer)
  }
  saveTimer = setTimeout(() => {
    void persistAnswers()
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

function handleVisibilityChange() {
  if (document.visibilityState === 'hidden') {
    void persistAnswers()
  }
}

function handlePageHide() {
  void persistAnswers()
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
  if (saveTimer) {
    clearTimeout(saveTimer)
    saveTimer = null
  }
  return persistAnswers()
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

    Object.keys(answers).forEach((key) => delete answers[key])
    Object.assign(answers, startRes.data.answers || {})
    restoreNotice.value = Object.keys(startRes.data.answers || {}).length
      ? '已恢复上次暂存的作答内容'
      : ''
    ready.value = true
    saveState.value = Object.keys(startRes.data.answers || {}).length ? 'saved' : 'idle'
    if (saveState.value === 'saved') {
      updateSavedAt()
    }
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
      ElMessage.success(`交卷成功，得到 ${res.data.etScore} / ${res.data.maxScore}`)
      router.push({
        path: '/student/scores',
        query: {
          submitted: '1',
          examCode: String(route.params.examCode),
          score: String(res.data.etScore ?? ''),
          maxScore: String(res.data.maxScore ?? '')
        }
      })
      return
    }
    ElMessage.error(res.message || '交卷失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '交卷失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onBeforeRouteLeave(() => {
  void flushSave()
})

onMounted(() => {
  document.addEventListener('visibilitychange', handleVisibilityChange)
  window.addEventListener('pagehide', handlePageHide)
  void bootstrap()
})

onBeforeUnmount(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  window.removeEventListener('pagehide', handlePageHide)
  if (saveTimer) {
    clearTimeout(saveTimer)
  }
  if (tickTimer) {
    clearInterval(tickTimer)
  }
  void flushSave()
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

.q-block__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.q-block h3 {
  margin: 0;
  font-size: 1.05rem;
}

.q-block__header span {
  color: var(--text-secondary);
  font-size: 12px;
}

.q-item {
  padding: 14px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.q-index {
  margin: 0 0 8px;
  color: var(--text-secondary);
  font-size: 12px;
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
  position: sticky;
  top: 92px;
  align-self: start;
}

.answer-layout__actions :deep(.el-button) {
  width: 100%;
  min-height: 44px;
}

@media (max-width: 900px) {
  .answer-layout {
    grid-template-columns: 1fr;
  }

  .answer-layout__sidebar {
    position: static;
    order: -1;
  }
}

@media (max-width: 768px) {
  .q-block__header {
    flex-direction: column;
    align-items: flex-start;
  }

  .q-opts :deep(.el-radio-group) {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
}
</style>
