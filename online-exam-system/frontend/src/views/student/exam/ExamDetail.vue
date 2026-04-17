<template>
  <div class="student-page">
    <PageContainer>
      <PageHeader
        title="考试详情"
        description="考前可查看考试说明、时间窗口和题型分布，开考后将直接进入作答。"
      >
        <template #actions>
          <el-button @click="router.push('/student/home')">返回考试中心</el-button>
          <el-button type="primary" :disabled="!exam || !canEnter" @click="goAnswer">
            {{ enterButtonText }}
          </el-button>
        </template>
      </PageHeader>

      <el-alert
        v-if="loadError"
        :title="loadError"
        type="error"
        show-icon
        :closable="false"
      />

      <template v-else-if="exam">
        <ExamMetaPanel :exam="examForPanel" />

        <div class="detail-summary">
          <div class="detail-summary__item">
            <span>当前状态</span>
            <strong>{{ statusLabel }}</strong>
          </div>
          <div class="detail-summary__item">
            <span>作答记录</span>
            <strong>{{ attemptLabel }}</strong>
          </div>
          <div class="detail-summary__item">
            <span>开考倒计时</span>
            <strong>{{ countdownLabel }}</strong>
          </div>
          <div class="detail-summary__item">
            <span>总题数</span>
            <strong>{{ exam.totalQuestionCount ?? '--' }}</strong>
          </div>
        </div>

        <section class="question-summary">
          <header class="question-summary__header">
            <h3>题型分布</h3>
            <span>{{ exam.summarySource === 'FROZEN_SNAPSHOT' ? '按冻结后固定题面统计' : '按当前试卷统计' }}</span>
          </header>
          <div class="question-summary__grid">
            <article
              v-for="item in exam.questionSummary || []"
              :key="item.questionType"
              class="question-summary__card"
            >
              <p>{{ item.label }}</p>
              <strong>{{ item.count }} 题</strong>
              <span>{{ item.score }} 分</span>
            </article>
          </div>
        </section>

        <el-alert
          v-if="exam.revoked"
          title="本场考试已被撤销，无法参加。"
          type="error"
          show-icon
          :closable="false"
        />
        <el-alert
          v-else-if="exam.examState === 'UPCOMING'"
          title="考试尚未开始。"
          :description="windowHint"
          type="warning"
          show-icon
          :closable="false"
        />
        <el-alert
          v-else-if="exam.examState === 'ENDED' || exam.attemptStatus === 'SUBMITTED'"
          :title="exam.attemptStatus === 'SUBMITTED' ? '本场考试已交卷。' : '考试时间已结束，无法再进入作答。'"
          :description="windowHint"
          type="info"
          show-icon
          :closable="false"
        />
        <el-alert
          v-else
          title="考试已开始，系统将直接进入答题页并恢复你的作答进度。"
          type="info"
          show-icon
          :closable="false"
        />
      </template>
    </PageContainer>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import ExamMetaPanel from '@/components/exam/ExamMetaPanel.vue'
import { getStudentExamDetail } from '@/api/examApi'
import { formatDateTime } from '@/utils/date'
import { createRequestCoordinator } from '@/utils/requestCoordinator'

const examDetailCoordinator = createRequestCoordinator('student-exams:detail', {
  defaultTtlMs: 3000
})

const route = useRoute()
const router = useRouter()
const exam = ref(null)
const loadError = ref('')
const countdownLabel = ref('--')
const serverSkewMs = ref(0)
let countdownTimer = null

const canEnter = computed(() => Boolean(exam.value?.canEnter))

const enterButtonText = computed(() => {
  if (!exam.value) {
    return '加载中'
  }
  if (exam.value.revoked) {
    return '本场考试已取消'
  }
  if (exam.value.canEnter) {
    return '进入答题页'
  }
  if (exam.value.attemptStatus === 'SUBMITTED') {
    return '已交卷'
  }
  return '当前不可进入'
})

const examForPanel = computed(() => {
  if (!exam.value) {
    return null
  }

  return {
    ...exam.value,
    freezeAt: exam.value.freezeAt,
    windowEndAt: exam.value.windowEndAt,
    inExamWindow: exam.value.inExamWindow
  }
})

const statusLabel = computed(() => {
  if (!exam.value) {
    return '--'
  }
  switch (exam.value.examState) {
    case 'ONGOING':
      return '进行中'
    case 'ENDED':
      return '已结束'
    case 'REVOKED':
      return '已撤销'
    default:
      return '未开始'
  }
})

const attemptLabel = computed(() => {
  if (!exam.value) {
    return '--'
  }
  switch (exam.value.attemptStatus) {
    case 'IN_PROGRESS':
      return '已开始，未交卷'
    case 'SUBMITTED':
      return '已交卷'
    default:
      return '尚未作答'
  }
})

const windowHint = computed(() => {
  if (!exam.value) {
    return ''
  }

  const startAt = exam.value.examStartAt ? formatDateTime(exam.value.examStartAt) : '--'
  const endAt = exam.value.windowEndAt ? formatDateTime(exam.value.windowEndAt) : '--'
  return `考试窗口：${startAt} 至 ${endAt}`
})

function goAnswer() {
  if (!exam.value || !canEnter.value) {
    return
  }
  router.push(`/student/exams/${exam.value.examCode}/answer`)
}

function updateCountdown() {
  if (!exam.value) {
    countdownLabel.value = '--'
    return
  }
  if (exam.value.examState === 'UPCOMING') {
    const startAt = dayjs(exam.value.examStartAt)
    const now = dayjs(Date.now() + serverSkewMs.value)
    const diffSeconds = startAt.diff(now, 'second')
    if (diffSeconds <= 0) {
      countdownLabel.value = '00:00:00'
      return
    }
    const hours = Math.floor(diffSeconds / 3600)
    const minutes = Math.floor((diffSeconds % 3600) / 60)
    const seconds = diffSeconds % 60
    countdownLabel.value = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
    return
  }
  countdownLabel.value = exam.value.examState === 'ONGOING' ? '考试已开始' : '--'
}

async function fetchExamDetail() {
  loadError.value = ''

  try {
    const cacheKey = `student-exams:detail:${route.params.examCode}`
    const response = await examDetailCoordinator.load(
      cacheKey,
      () => getStudentExamDetail(route.params.examCode)
    )
    if (response.code !== 200 || !response.data?.exam) {
      loadError.value = response.message || '考试详情加载失败'
      return
    }

    exam.value = response.data.exam
    if (response.data.serverTime) {
      serverSkewMs.value = dayjs(response.data.serverTime).valueOf() - Date.now()
    }
    updateCountdown()
    if (exam.value.canEnter) {
      router.replace(`/student/exams/${exam.value.examCode}/answer`)
    }
  } catch (error) {
    loadError.value = error?.response?.data?.message || '考试详情加载失败，请稍后重试'
  }
}

onMounted(() => {
  countdownTimer = setInterval(updateCountdown, 1000)
  void fetchExamDetail()
})

onBeforeUnmount(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
.student-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-summary {
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
}

.detail-summary__item,
.question-summary__card {
  padding: 16px 18px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-card);
}

.detail-summary__item span,
.question-summary__card p,
.question-summary__header span {
  color: var(--text-secondary);
  font-size: 12px;
}

.detail-summary__item strong,
.question-summary__card strong {
  display: block;
  margin-top: 8px;
  font-size: 18px;
}

.question-summary {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.question-summary__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.question-summary__header h3 {
  margin: 0;
}

.question-summary__grid {
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.question-summary__card span {
  display: inline-block;
  margin-top: 8px;
}
</style>
