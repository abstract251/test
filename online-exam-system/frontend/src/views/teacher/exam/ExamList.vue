<template>
  <PageContainer>
    <PageHeader
      title="考试管理"
      description="维护考试基础信息，查看状态、预览试卷，并在需要时处理考试撤销。"
    >
      <template #actions>
        <el-button type="primary" @click="router.push(createExamPath)">
          新增考试
        </el-button>
      </template>
    </PageHeader>

    <el-table v-loading="loadingList" :data="pagination.records" stripe empty-text="暂无考试数据">
      <el-table-column prop="examCode" label="考试编号" min-width="90" />
      <el-table-column prop="source" label="科目" min-width="140" />
      <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
      <el-table-column label="状态" min-width="240">
        <template #default="{ row }">
          <div class="status-cell">
            <el-space wrap>
              <el-tag
                v-for="tag in resolveStatusTags(row)"
                :key="`${row.examCode}-${tag.label}`"
                :type="tag.type"
              >
                {{ tag.label }}
              </el-tag>
            </el-space>
            <span class="status-cell__meta">{{ resolveStatusMeta(row) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="考试时间" min-width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.examDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="totalTime" label="时长" min-width="90">
        <template #default="{ row }">{{ row.totalTime }} 分钟</template>
      </el-table-column>
      <el-table-column prop="grade" label="年级" min-width="90" />
      <el-table-column prop="major" label="专业" min-width="160" />
      <el-table-column prop="institute" label="学院" min-width="150" />
      <el-table-column prop="totalScore" label="总分" min-width="90" />
      <el-table-column fixed="right" label="操作" width="360">
        <template #default="{ row }">
          <el-space wrap>
            <el-button link type="primary" @click="openPreview(row)">
              试卷详情
            </el-button>

            <el-tooltip
              v-if="isPaperReadOnly(row)"
              content="试卷已冻结或考试已撤销时不可再组卷"
              placement="top"
            >
              <span>
                <el-button link type="primary" disabled>进入组卷</el-button>
              </span>
            </el-tooltip>
            <el-button
              v-else
              link
              type="primary"
              @click="openPaperBuilder(row.examCode)"
            >
              进入组卷
            </el-button>

            <el-button link type="primary" @click="openExamEdit(row.examCode)">
              编辑
            </el-button>

            <template v-if="isAdminConsole">
              <el-tooltip
                v-if="isExamRevoked(row)"
                content="本场考试已撤销"
                placement="top"
              >
                <span>
                  <el-button link type="warning" disabled>撤销考试</el-button>
                </span>
              </el-tooltip>
              <el-button
                v-else
                link
                type="warning"
                :loading="revokingExamCode === row.examCode"
                @click="handleRevoke(row)"
              >
                撤销考试
              </el-button>
            </template>

            <el-tooltip
              v-if="isPaperReadOnly(row)"
              content="冻结后或已撤销的考试不可删除"
              placement="top"
            >
              <span>
                <el-button link type="danger" disabled>删除</el-button>
              </span>
            </el-tooltip>
            <el-button
              v-else
              link
              type="danger"
              @click="handleDelete(row.examCode)"
            >
              删除
            </el-button>
          </el-space>
        </template>
      </el-table-column>
    </el-table>

    <div class="footer">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="fetchExams"
        @current-change="fetchExams"
      />
    </div>

    <el-drawer v-model="previewVisible" size="55%" :title="previewTitle">
      <div v-loading="previewLoading" class="drawer-content">
        <template v-if="previewExam">
          <el-alert
            :title="previewAlert.title"
            :type="previewAlert.type"
            show-icon
            :closable="false"
            :description="previewAlert.description"
          />
          <ExamMetaPanel :exam="previewExamForPanel" />
          <PaperQuestionGroup
            v-for="item in groupedPreview"
            :key="item.type"
            :title="item.title"
            :description="item.description"
            :questions="item.questions"
            :type="item.type"
            :show-action="false"
          />
        </template>
      </div>
    </el-drawer>
  </PageContainer>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import ExamMetaPanel from '@/components/exam/ExamMetaPanel.vue'
import PaperQuestionGroup from '@/components/exam/PaperQuestionGroup.vue'
import { usePagination } from '@/composables/usePagination'
import {
  deleteExam,
  getExamList,
  getExamPolicy,
  getFrozenExamPaper,
  revokeExamAsAdmin
} from '@/api/examApi'
import { getPaper } from '@/api/paperApi'
import { ensureQuestionMap } from '@/utils/adapters'
import { getSession } from '@/utils/auth'
import { formatDateTime } from '@/utils/date'
import { AUTH_ROLES, buildConsolePath, QUESTION_TYPE_OPTIONS } from '@/utils/constants'

const router = useRouter()
const consoleRole = getSession()?.authRole
const isAdminConsole = consoleRole === AUTH_ROLES.ADMIN
const pagination = usePagination(10)
const loadingList = ref(false)
const revokingExamCode = ref(null)
const previewVisible = ref(false)
const previewLoading = ref(false)
const previewExam = ref(null)
const previewPolicy = ref(null)
const previewQuestionMap = ref(ensureQuestionMap())
const previewMode = ref('current')
const policyMap = ref({})
const createExamPath = buildConsolePath(consoleRole, 'exams/new')

const groupedPreview = computed(() =>
  QUESTION_TYPE_OPTIONS.map((item) => ({
    type: item.value,
    title: item.label,
    description: `${previewMode.value === 'frozen' ? '冻结后的固定题面' : '当前试卷中的'}${item.label}`,
    questions: previewQuestionMap.value[item.value]
  }))
)

const previewTitle = computed(() =>
  previewMode.value === 'frozen' ? '固定题面预览' : '当前试卷预览'
)

const previewAlert = computed(() => {
  if (previewPolicy.value?.revoked || previewExam.value?.revokedAt) {
    return {
      title: '本场考试已撤销，当前仅展示试卷内容供查看。',
      type: 'error',
      description: previewExam.value?.revokeReason || '撤销后不会再进入正常考试流程。'
    }
  }

  if (previewMode.value === 'frozen') {
    return {
      title: '当前展示冻结后的固定题面。',
      type: 'success',
      description: previewPolicy.value?.freezeAt
        ? `试卷已在 ${formatDateTime(previewPolicy.value.freezeAt)} 进入冻结状态。`
        : '冻结后题目与顺序将保持固定。'
    }
  }

  return {
    title: '当前展示尚未冻结的试卷内容。',
    type: 'info',
    description: '考试进入冻结窗口后，这里会优先展示固定题面。'
  }
})

const previewExamForPanel = computed(() => {
  if (!previewExam.value) {
    return null
  }

  return {
    ...previewExam.value,
    examStartAt: previewPolicy.value?.examStartAt || previewExam.value.examStartAt,
    paperLocked: previewPolicy.value?.paperLocked ?? previewExam.value.paperLocked,
    inExamWindow: previewPolicy.value?.inExamWindow,
    snapshotReady: previewPolicy.value?.snapshotReady,
    freezeAt: previewPolicy.value?.freezeAt,
    windowEndAt: previewPolicy.value?.windowEndAt
  }
})

function getCachedPolicy(examCode) {
  return policyMap.value[String(examCode)] || null
}

function getPolicyValue(row, key, fallback = null) {
  const policy = getCachedPolicy(row.examCode)
  return policy && key in policy ? policy[key] : fallback
}

function buildFallbackFreezeAt(row) {
  if (!row?.examDate) {
    return ''
  }
  const parsed = dayjs(row.examDate)
  return parsed.isValid() ? parsed.subtract(1, 'hour').format('YYYY-MM-DD HH:mm:ss') : ''
}

function buildFallbackWindowEndAt(row) {
  if (!row?.examDate) {
    return ''
  }
  const parsed = dayjs(row.examDate)
  return parsed.isValid()
    ? parsed.add(Number(row.totalTime || 0), 'minute').format('YYYY-MM-DD HH:mm:ss')
    : ''
}

function isExamRevoked(row) {
  const policyRevoked = getPolicyValue(row, 'revoked', null)
  if (typeof policyRevoked === 'boolean') {
    return policyRevoked
  }
  return Boolean(row?.revokedAt)
}

function isPaperLocked(row) {
  const policyLocked = getPolicyValue(row, 'paperLocked', null)
  if (typeof policyLocked === 'boolean') {
    return policyLocked
  }
  return Boolean(row?.paperLocked || row?.revokedAt)
}

function isPaperReadOnly(row) {
  return isPaperLocked(row) || isExamRevoked(row)
}

function isInExamWindow(row) {
  const policyWindow = getPolicyValue(row, 'inExamWindow', null)
  if (typeof policyWindow === 'boolean') {
    return policyWindow
  }

  if (!row?.examDate) {
    return false
  }

  const start = dayjs(row.examDate)
  if (!start.isValid()) {
    return false
  }
  const end = start.add(Number(row.totalTime || 0), 'minute')
  const now = dayjs()
  return (now.isAfter(start) || now.isSame(start)) && (now.isBefore(end) || now.isSame(end))
}

function resolveStatusTags(row) {
  const tags = []
  const revoked = isExamRevoked(row)

  if (revoked) {
    tags.push({ label: '已撤销', type: 'danger' })
  } else if (isInExamWindow(row)) {
    tags.push({ label: '进行中', type: 'success' })
  } else if (isPaperLocked(row)) {
    tags.push({ label: '已冻结', type: 'warning' })
  } else {
    tags.push({ label: '待开考', type: 'info' })
  }

  if (!revoked && getPolicyValue(row, 'snapshotReady', false)) {
    tags.push({ label: '固定题面可预览', type: 'success' })
  }

  return tags
}

function resolveStatusMeta(row) {
  if (isExamRevoked(row)) {
    return row.revokeReason || '本场考试已撤销'
  }

  if (isInExamWindow(row)) {
    const windowEndAt = getPolicyValue(row, 'windowEndAt', buildFallbackWindowEndAt(row))
    return `考试窗口截止 ${formatDateTime(windowEndAt)}`
  }

  if (isPaperLocked(row)) {
    const freezeAt = getPolicyValue(row, 'freezeAt', buildFallbackFreezeAt(row))
    return `已在 ${formatDateTime(freezeAt)} 锁定题面`
  }

  const freezeAt = getPolicyValue(row, 'freezeAt', buildFallbackFreezeAt(row))
  return `预计 ${formatDateTime(freezeAt)} 进入冻结窗口`
}

async function fetchExamPolicies(exams) {
  const results = await Promise.allSettled(
    exams.map((item) => getExamPolicy(item.examCode))
  )

  const nextMap = {}
  results.forEach((result, index) => {
    if (result.status === 'fulfilled' && result.value.code === 200 && result.value.data) {
      nextMap[String(exams[index].examCode)] = result.value.data
    }
  })
  policyMap.value = nextMap
}

async function ensureExamPolicy(examCode) {
  const cached = getCachedPolicy(examCode)
  if (cached) {
    return cached
  }

  const response = await getExamPolicy(examCode)
  if (response.code === 200 && response.data) {
    policyMap.value = {
      ...policyMap.value,
      [String(examCode)]: response.data
    }
    return response.data
  }
  return null
}

function openPaperBuilder(examCode) {
  router.push(buildConsolePath(consoleRole, `exams/${examCode}/paper`))
}

function openExamEdit(examCode) {
  router.push(buildConsolePath(consoleRole, `exams/${examCode}/edit`))
}

async function fetchExams() {
  loadingList.value = true
  try {
    const response = await getExamList(pagination.current, pagination.size)
    if (response.code === 200 && response.data) {
      Object.assign(pagination, response.data)
      await fetchExamPolicies(Array.isArray(response.data.records) ? response.data.records : [])
      return
    }
    ElMessage.error(response.message || '加载考试列表失败')
  } catch (error) {
    ElMessage.error('加载考试列表失败，请稍后重试')
  } finally {
    loadingList.value = false
  }
}

async function openPreview(exam) {
  previewLoading.value = true
  try {
    const policy = await ensureExamPolicy(exam.examCode)
    previewPolicy.value = policy
    previewExam.value = {
      ...exam,
      freezeAt: policy?.freezeAt || buildFallbackFreezeAt(exam),
      windowEndAt: policy?.windowEndAt || buildFallbackWindowEndAt(exam)
    }

    const shouldUseFrozen = !!policy?.paperLocked && !policy?.revoked
    const response = shouldUseFrozen
      ? await getFrozenExamPaper(exam.examCode)
      : await getPaper(exam.paperId)

    if (response.code === 200) {
      previewMode.value = shouldUseFrozen ? 'frozen' : 'current'
      previewQuestionMap.value = ensureQuestionMap(response.data)
      previewVisible.value = true
      return
    }

    ElMessage.error(response.message || '加载试卷详情失败')
  } catch (error) {
    const message = error?.response?.data?.message
    ElMessage.error(message || '加载试卷详情失败，请稍后重试')
  } finally {
    previewLoading.value = false
  }
}

async function handleDelete(examCode) {
  try {
    await ElMessageBox.confirm('删除考试会一并清空试卷题目关联，是否继续？', '删除考试', {
      type: 'warning'
    })
  } catch (error) {
    return
  }

  try {
    const response = await deleteExam(examCode)
    if (response.code === 200) {
      ElMessage.success('考试已删除')
      await fetchExams()
      return
    }
    ElMessage.error(response.message || '删除考试失败')
  } catch (error) {
    const message = error?.response?.data?.message
    ElMessage.error(message || '删除考试失败，请稍后重试')
  }
}

async function handleRevoke(row) {
  let reason = ''
  try {
    const result = await ElMessageBox.prompt(
      `请填写撤销考试 ${row.examCode} 的原因`,
      '撤销考试',
      {
        type: 'warning',
        confirmButtonText: '确认撤销',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请输入撤销原因',
        inputValidator: (value) => {
          if (!String(value || '').trim()) {
            return '请填写撤销原因'
          }
          return true
        }
      }
    )
    reason = String(result.value || '').trim()
  } catch (error) {
    return
  }

  revokingExamCode.value = row.examCode
  try {
    const response = await revokeExamAsAdmin(row.examCode, reason)
    if (response.code === 200) {
      ElMessage.success(response.message || '本场考试已撤销')
      await fetchExams()
      return
    }
    ElMessage.error(response.message || '撤销考试失败')
  } catch (error) {
    const message = error?.response?.data?.message
    ElMessage.error(message || '撤销考试失败，请稍后重试')
  } finally {
    revokingExamCode.value = null
  }
}

onMounted(fetchExams)
</script>

<style scoped>
.status-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-cell__meta {
  color: var(--text-secondary);
  font-size: 12px;
  line-height: 1.6;
}

.footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.drawer-content {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
</style>
