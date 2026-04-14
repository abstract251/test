<template>
  <PageContainer>
    <PageHeader
      title="我的成绩"
      description="查看最近交卷结果和历次考试成绩。"
    >
      <template #actions>
        <el-button :loading="loading" @click="refreshScores">刷新成绩</el-button>
      </template>
    </PageHeader>

    <el-alert
      v-if="submitResult"
      class="submit-alert"
      type="success"
      show-icon
      :title="`本场考试已交卷，得分 ${submitResult.score} / ${submitResult.maxScore}`"
      :description="`考试编号 ${submitResult.examCode} 的成绩已写入记录。`"
      @close="clearSubmitQuery"
    />

    <div class="summary-grid">
      <StatusCard label="累计考试" :value="summary.totalCount" hint="已生成成绩记录的考试场次" />
      <StatusCard label="最近得分" :value="summary.latestScore" :hint="summary.latestHint" />
      <StatusCard label="平均分" :value="summary.averageScore" hint="按历次得分计算" />
      <StatusCard label="通过情况" :value="summary.passCount" :hint="summary.passRate" />
    </div>

    <section v-if="latestRecord" class="latest-card">
      <div>
        <p class="latest-card__eyebrow">{{ submitResult ? '本次交卷结果' : '最近一次成绩' }}</p>
        <h3 class="latest-card__title">{{ latestRecord.subject || `考试 ${latestRecord.examCode}` }}</h3>
        <p class="latest-card__meta">
          考试编号 {{ latestRecord.examCode }} · {{ formatDateTime(latestRecord.answerDate, 'YYYY-MM-DD') }}
        </p>
      </div>
      <strong class="latest-card__score">{{ formatScore(latestRecord) }}</strong>
      <el-tag :type="isPassed(latestRecord) ? 'success' : 'danger'">
        {{ isPassed(latestRecord) ? '已通过' : '未通过' }}
      </el-tag>
    </section>

    <EmptyState
      v-if="!loading && !pagination.records.length"
      description="暂无成绩记录，交卷后会在这里展示。"
      emoji="📝"
    />

    <template v-else>
      <el-table
        v-loading="loading"
        class="score-table"
        :data="pagination.records"
        stripe
        empty-text="暂无成绩记录"
        :row-class-name="resolveRowClass"
      >
        <el-table-column prop="examCode" label="考试编号" min-width="120" />
        <el-table-column prop="subject" label="科目" min-width="180" show-overflow-tooltip />
        <el-table-column label="得分" min-width="110">
          <template #default="{ row }">
            {{ formatActualScore(row) }}
          </template>
        </el-table-column>
        <el-table-column label="满分" min-width="110">
          <template #default="{ row }">
            {{ formatFullScore(row) }}
          </template>
        </el-table-column>
        <el-table-column label="结果" min-width="110">
          <template #default="{ row }">
            <el-tag :type="isPassed(row) ? 'success' : 'danger'">
              {{ isPassed(row) ? '已通过' : '未通过' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="交卷日期" min-width="140">
          <template #default="{ row }">
            {{ formatDateTime(row.answerDate, 'YYYY-MM-DD') }}
          </template>
        </el-table-column>
      </el-table>

      <div class="footer">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[6, 10, 20]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="fetchScorePage"
          @current-change="fetchScorePage"
        />
      </div>
    </template>
  </PageContainer>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusCard from '@/components/common/StatusCard.vue'
import { useAuthSession } from '@/composables/useAuthSession'
import { usePagination } from '@/composables/usePagination'
import { getStudentScorePage, getStudentScores } from '@/api/scoreApi'
import { formatDateTime } from '@/utils/date'

const route = useRoute()
const router = useRouter()
const { session } = useAuthSession()

const pagination = usePagination(6)
const loading = ref(false)
const historyScores = ref([])

const studentId = computed(() => String(session.value?.userId || ''))
const submittedExamCode = computed(() => String(route.query.examCode || ''))
const highlightKey = computed(() => buildScoreKey(latestRecord.value))

const submitResult = computed(() => {
  if (route.query.submitted !== '1') {
    return null
  }

  const score = toNumber(route.query.score)
  const maxScore = toNumber(route.query.maxScore)
  if (score == null || maxScore == null) {
    return null
  }

  return {
    examCode: submittedExamCode.value || '--',
    score,
    maxScore
  }
})

const sortedScores = computed(() =>
  [...historyScores.value].sort(compareByLatest)
)

const latestRecord = computed(() => {
  if (submittedExamCode.value) {
    const currentExamRecord = sortedScores.value.find(
      (item) => String(item.examCode) === submittedExamCode.value
    )
    if (currentExamRecord) {
      return currentExamRecord
    }
  }
  return sortedScores.value[0] || null
})

const summary = computed(() => {
  const totalCount = sortedScores.value.length
  if (!totalCount) {
    return {
      totalCount: 0,
      latestScore: '--',
      latestHint: '暂无最近成绩',
      averageScore: '--',
      passCount: '--',
      passRate: '通过率 --'
    }
  }

  const passCount = sortedScores.value.filter(isPassed).length
  const totalScore = sortedScores.value.reduce((sum, item) => sum + (extractActualScore(item) || 0), 0)
  const averageScore = totalScore / totalCount

  return {
    totalCount,
    latestScore: formatScore(latestRecord.value),
    latestHint: latestRecord.value
      ? `${latestRecord.value.subject || `考试 ${latestRecord.value.examCode}`} · ${formatDateTime(latestRecord.value.answerDate, 'YYYY-MM-DD')}`
      : '暂无最近成绩',
    averageScore: formatDecimal(averageScore),
    passCount: `${passCount}/${totalCount}`,
    passRate: `通过率 ${formatPercent((passCount / totalCount) * 100)}`
  }
})

function toNumber(value) {
  if (value === '' || value == null) {
    return null
  }
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : null
}

function buildScoreKey(row) {
  if (!row) {
    return ''
  }
  return `${row.scoreId || ''}_${row.examCode || ''}_${row.studentId || ''}_${row.answerDate || ''}`
}

function parseTime(value) {
  if (!value) {
    return 0
  }
  const timestamp = new Date(value).getTime()
  return Number.isFinite(timestamp) ? timestamp : 0
}

function compareByLatest(left, right) {
  return (
    parseTime(right.answerDate) - parseTime(left.answerDate) ||
    (toNumber(right.scoreId) || 0) - (toNumber(left.scoreId) || 0) ||
    String(right.examCode || '').localeCompare(String(left.examCode || ''))
  )
}

function extractActualScore(row) {
  return toNumber(row?.etScore)
}

function extractFullScore(row) {
  return toNumber(row?.score)
}

function isPassed(row) {
  return Number(row?.ptScore) === 1
}

function formatDecimal(value) {
  const parsed = toNumber(value)
  if (parsed == null) {
    return '--'
  }
  return parsed.toFixed(1)
}

function formatPercent(value) {
  const parsed = toNumber(value)
  if (parsed == null) {
    return '--'
  }
  return `${parsed.toFixed(parsed % 1 === 0 ? 0 : 1)}%`
}

function formatActualScore(row) {
  const score = extractActualScore(row)
  return score == null ? '--' : score
}

function formatFullScore(row) {
  const score = extractFullScore(row)
  return score == null ? '--' : score
}

function formatScore(row) {
  if (!row) {
    return '--'
  }
  const actualScore = formatActualScore(row)
  const fullScore = formatFullScore(row)
  return fullScore === '--' ? String(actualScore) : `${actualScore} / ${fullScore}`
}

function resolveRowClass({ row }) {
  return buildScoreKey(row) === highlightKey.value ? 'score-row--latest' : ''
}

async function fetchScorePage() {
  if (!studentId.value) {
    return
  }

  loading.value = true
  try {
    const response = await getStudentScorePage({
      page: pagination.current,
      size: pagination.size,
      studentId: studentId.value
    })
    if (response.code === 200 && response.data) {
      Object.assign(pagination, response.data)
      return
    }
    ElMessage.error(response.message || '加载成绩记录失败')
  } catch (error) {
    ElMessage.error('加载成绩记录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function refreshScores() {
  if (!studentId.value) {
    return
  }

  loading.value = true
  const [pageResult, summaryResult] = await Promise.allSettled([
    getStudentScorePage({
      page: pagination.current,
      size: pagination.size,
      studentId: studentId.value
    }),
    getStudentScores(studentId.value)
  ])

  let pageLoaded = false
  let summaryLoaded = false

  if (pageResult.status === 'fulfilled' && pageResult.value.code === 200 && pageResult.value.data) {
    Object.assign(pagination, pageResult.value.data)
    pageLoaded = true
  }

  if (summaryResult.status === 'fulfilled' && summaryResult.value.code === 200) {
    historyScores.value = Array.isArray(summaryResult.value.data) ? summaryResult.value.data : []
    summaryLoaded = true
  }

  if (!pageLoaded) {
    ElMessage.error(
      pageResult.status === 'fulfilled'
        ? pageResult.value.message || '加载成绩记录失败'
        : '加载成绩记录失败，请稍后重试'
    )
  }

  if (!summaryLoaded && !pageLoaded) {
    historyScores.value = []
  }

  loading.value = false
}

function clearSubmitQuery() {
  const nextQuery = { ...route.query }
  delete nextQuery.submitted
  delete nextQuery.examCode
  delete nextQuery.score
  delete nextQuery.maxScore
  router.replace({ query: nextQuery })
}

onMounted(refreshScores)
</script>

<style scoped>
.submit-alert {
  margin-bottom: 20px;
}

.summary-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  margin-bottom: 20px;
}

.latest-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  align-items: center;
  gap: 18px;
  padding: 20px 22px;
  margin-bottom: 20px;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, rgba(79, 175, 143, 0.16), rgba(255, 200, 87, 0.14));
}

.latest-card__eyebrow {
  margin: 0 0 8px;
  color: var(--text-secondary);
}

.latest-card__title {
  margin: 0;
  font-size: 22px;
}

.latest-card__meta {
  margin: 8px 0 0;
  color: var(--text-secondary);
}

.latest-card__score {
  font-size: 30px;
  white-space: nowrap;
}

.footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.score-table :deep(.score-row--latest td) {
  background: rgba(255, 248, 223, 0.95) !important;
}

@media (max-width: 900px) {
  .latest-card {
    grid-template-columns: 1fr;
    align-items: flex-start;
  }

  .latest-card__score {
    font-size: 26px;
  }
}
</style>
