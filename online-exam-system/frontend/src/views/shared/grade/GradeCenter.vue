<template>
  <PageContainer>
    <PageHeader
      title="成绩管理"
      description="按考试查看成绩概况、分布和成绩明细。"
    >
      <template #actions>
        <el-button :loading="loadingData" @click="refreshCurrentExam">刷新数据</el-button>
      </template>
    </PageHeader>

    <div class="toolbar">
      <el-select
        v-model="selectedExamCode"
        class="toolbar__select"
        filterable
        clearable
        placeholder="请选择考试"
        :loading="loadingExamOptions"
        @change="handleExamChange"
      >
        <el-option
          v-for="item in examOptions"
          :key="item.examCode"
          :label="formatExamLabel(item)"
          :value="String(item.examCode)"
        />
      </el-select>

      <el-input
        v-model="clazzFilter"
        class="toolbar__input"
        clearable
        placeholder="请输入班级"
        @clear="handleClazzClear"
        @keyup.enter="handleClazzSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="handleClazzSearch" />
        </template>
      </el-input>

      <p v-if="selectedExam" class="toolbar__meta">
        {{ selectedExam.source || '未命名考试' }} · {{ formatDateTime(selectedExam.examDate) }}
        <span v-if="clazzFilter" class="toolbar__meta-tag">班级: {{ clazzFilter }}</span>
      </p>
    </div>

    <div class="summary-grid">
      <StatusCard label="参考人数" :value="summary.totalCount" hint="已生成成绩记录的学生数量" />
      <StatusCard label="平均分" :value="summary.averageScore" hint="按本场考试成绩计算" />
      <StatusCard label="最高分" :value="summary.maxScore" hint="展示本场考试最高成绩" />
      <StatusCard label="最低分" :value="summary.minScore" hint="展示本场考试最低成绩" />
      <StatusCard label="及格率" :value="summary.passRate" hint="以成绩统计接口或当前列表计算" />
    </div>

    <ScoreDistributionChart
      :distribution="distributionData"
      :description="selectedExam ? `按 ${selectedExam.source || '当前考试'} 的分数段查看成绩分布。` : '请选择考试后查看成绩分布。'"
    />

    <EmptyState
      v-if="!loadingExamOptions && !examOptions.length"
      class="empty-panel"
      description="暂无可查看的考试，请先创建考试并生成成绩记录。"
      emoji="📊"
    />

    <EmptyState
      v-else-if="!loadingData && !selectedExamCode"
      class="empty-panel"
      description="请选择一场考试后查看成绩概况和明细。"
      emoji="🧭"
    />

    <EmptyState
      v-else-if="!loadingData && !scoreRows.length"
      class="empty-panel"
      description="当前考试还没有成绩记录。"
      emoji="🗂️"
    />

    <template v-else>
      <el-table v-loading="loadingData" :data="scoreRows" stripe empty-text="暂无成绩记录">
        <el-table-column label="学生" min-width="180">
          <template #default="{ row }">
            <div class="student-cell">
              <strong>{{ row.studentName || row.studentId || '--' }}</strong>
              <span v-if="row.studentName && row.studentId">{{ row.studentId }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="科目" min-width="160" show-overflow-tooltip />
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
    </template>
  </PageContainer>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusCard from '@/components/common/StatusCard.vue'
import ScoreDistributionChart from '@/components/charts/ScoreDistributionChart.vue'
import { getAllExams } from '@/api/examApi'
import { getExamScores, getScoreStatistics, getScoresByClazz } from '@/api/scoreApi'
import { formatDateTime } from '@/utils/date'
import { SCORE_DISTRIBUTION_LABELS } from '@/utils/constants'

const route = useRoute()

const exams = ref([])
const selectedExamCode = ref('')
const loadingExamOptions = ref(false)
const loadingData = ref(false)
const scoreRecords = ref([])
const statistics = ref(null)
const clazzFilter = ref('')

const examOptions = computed(() =>
  [...exams.value].sort((left, right) => {
    const leftTime = parseTime(left.examDate)
    const rightTime = parseTime(right.examDate)
    return rightTime - leftTime || Number(right.examCode || 0) - Number(left.examCode || 0)
  })
)

const selectedExam = computed(() =>
  examOptions.value.find((item) => String(item.examCode) === String(selectedExamCode.value)) || null
)

const scoreRows = computed(() =>
  [...scoreRecords.value].sort((left, right) => {
    return (
      (extractActualScore(right) || 0) - (extractActualScore(left) || 0) ||
      parseTime(right.answerDate) - parseTime(left.answerDate) ||
      Number(right.studentId || 0) - Number(left.studentId || 0)
    )
  })
)

const distributionData = computed(() => {
  const distribution = statistics.value?.distribution
  if (Array.isArray(distribution) && distribution.length) {
    return distribution
  }
  return buildDistributionFromRows(scoreRows.value)
})

const summary = computed(() => {
  const stat = statistics.value || {}
  const totalCount = toNumber(stat.totalCount) ?? scoreRows.value.length
  const averageScore = toNumber(stat.avgScore) ?? calculateAverage(scoreRows.value)
  const maxScore = toNumber(stat.maxScore) ?? calculateExtremum(scoreRows.value, 'max')
  const minScore = toNumber(stat.minScore) ?? calculateExtremum(scoreRows.value, 'min')
  const passRate = normalizeRate(stat.passRate) ?? calculatePassRate(scoreRows.value)

  return {
    totalCount: totalCount ?? '--',
    averageScore: formatNumber(averageScore, 1),
    maxScore: formatNumber(maxScore, 0),
    minScore: formatNumber(minScore, 0),
    passRate: passRate == null ? '--' : `${formatNumber(passRate, passRate % 1 === 0 ? 0 : 1)}%`
  }
})

function toNumber(value) {
  if (value === '' || value == null) {
    return null
  }
  const parsed = Number(String(value).replace('%', ''))
  return Number.isFinite(parsed) ? parsed : null
}

function parseTime(value) {
  if (!value) {
    return 0
  }
  const timestamp = new Date(value).getTime()
  return Number.isFinite(timestamp) ? timestamp : 0
}

function normalizeRate(value) {
  const parsed = toNumber(value)
  if (parsed == null) {
    return null
  }
  return parsed <= 1 ? parsed * 100 : parsed
}

function formatNumber(value, digits = 0) {
  const parsed = toNumber(value)
  if (parsed == null) {
    return '--'
  }
  return parsed.toFixed(digits)
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

function calculateAverage(rows) {
  if (!rows.length) {
    return null
  }
  const total = rows.reduce((sum, row) => sum + (extractActualScore(row) || 0), 0)
  return total / rows.length
}

function calculateExtremum(rows, type) {
  if (!rows.length) {
    return null
  }
  const values = rows
    .map((row) => extractActualScore(row))
    .filter((value) => value != null)
  if (!values.length) {
    return null
  }
  return type === 'max' ? Math.max(...values) : Math.min(...values)
}

function calculatePassRate(rows) {
  if (!rows.length) {
    return null
  }
  return (rows.filter(isPassed).length / rows.length) * 100
}

function buildDistributionFromRows(rows) {
  const distribution = SCORE_DISTRIBUTION_LABELS.reduce((accumulator, label) => {
    accumulator[label] = 0
    return accumulator
  }, {})

  rows.forEach((row) => {
    const score = extractActualScore(row)
    if (score == null) {
      return
    }
    if (score < 60) {
      distribution['0-59'] += 1
      return
    }
    if (score < 70) {
      distribution['60-69'] += 1
      return
    }
    if (score < 80) {
      distribution['70-79'] += 1
      return
    }
    if (score < 90) {
      distribution['80-89'] += 1
      return
    }
    distribution['90-100'] += 1
  })

  return distribution
}

function formatActualScore(row) {
  return formatNumber(extractActualScore(row), 0)
}

function formatFullScore(row) {
  return formatNumber(extractFullScore(row), 0)
}

function formatExamLabel(exam) {
  const labelParts = [
    exam.source || '未命名考试',
    `编号 ${exam.examCode}`
  ]
  if (exam.examDate) {
    labelParts.push(formatDateTime(exam.examDate))
  }
  return labelParts.join(' · ')
}

async function fetchExamOptions() {
  loadingExamOptions.value = true
  try {
    const response = await getAllExams()
    if (response.code === 200) {
      exams.value = Array.isArray(response.data) ? response.data : []

      const presetExamCode = String(route.query.examCode || '')
      if (presetExamCode && exams.value.some((item) => String(item.examCode) === presetExamCode)) {
        selectedExamCode.value = presetExamCode
      } else if (!selectedExamCode.value && examOptions.value.length) {
        selectedExamCode.value = String(examOptions.value[0].examCode)
      }
      return
    }
    ElMessage.error(response.message || '加载考试列表失败')
  } catch (error) {
    ElMessage.error('加载考试列表失败，请稍后重试')
  } finally {
    loadingExamOptions.value = false
  }
}

async function fetchGradeData() {
  if (!selectedExamCode.value) {
    scoreRecords.value = []
    statistics.value = null
    return
  }

  loadingData.value = true

  const examCode = Number(selectedExamCode.value)
  let scoresResult

  if (clazzFilter.value) {
    scoresResult = await getScoresByClazz(examCode, clazzFilter.value)
  } else {
    scoresResult = await getExamScores(examCode)
  }

  const statisticsResult = await getScoreStatistics(examCode)

  if (scoresResult.code === 200) {
    scoreRecords.value = Array.isArray(scoresResult.data) ? scoresResult.data : []
  } else {
    scoreRecords.value = []
    ElMessage.error(scoresResult.message || '加载成绩数据失败')
  }

  if (statisticsResult.code === 200) {
    statistics.value = statisticsResult.data || null
  } else {
    statistics.value = null
  }

  loadingData.value = false
}

function handleClazzSearch() {
  void fetchGradeData()
}

function handleClazzClear() {
  clazzFilter.value = ''
  void fetchGradeData()
}

function handleExamChange() {
  void fetchGradeData()
}

function refreshCurrentExam() {
  void fetchGradeData()
}

onMounted(async () => {
  await fetchExamOptions()
  await fetchGradeData()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 18px;
  margin-bottom: 20px;
}

.toolbar__select {
  width: min(420px, 100%);
}

.toolbar__input {
  width: min(280px, 100%);
}

.toolbar__meta {
  margin: 0;
  color: var(--text-secondary);
}

.toolbar__meta-tag {
  margin-left: 8px;
  padding: 2px 8px;
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  border-radius: 4px;
  font-size: 12px;
}

.summary-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  margin-bottom: 20px;
}

.empty-panel {
  margin-top: 20px;
}

.student-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.student-cell strong {
  font-size: 14px;
}

.student-cell span {
  color: var(--text-secondary);
  font-size: 12px;
}
</style>
