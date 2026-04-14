<template>
  <PageContainer>
    <PageHeader
      title="全局题库管理"
      description="按科目、题型和关键词统一查看题库内容。"
    >
      <template #actions>
        <el-space wrap>
          <el-button :loading="loading" @click="refreshQuestions">刷新数据</el-button>
          <el-button type="primary" @click="handleCreate">新增题目</el-button>
        </el-space>
      </template>
    </PageHeader>

    <el-form class="filter-grid" label-position="top" @submit.prevent>
      <el-form-item label="科目">
        <el-input
          v-model="filters.subject"
          clearable
          placeholder="请输入科目，例如 Java"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="题型">
        <el-select v-model="filters.questionType" clearable placeholder="全部题型">
          <el-option
            v-for="item in typeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="按题干关键词搜索"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <div class="filter-actions">
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="primary" @click="handleSearch">筛选题目</el-button>
      </div>
    </el-form>

    <div class="summary-grid">
      <StatusCard label="匹配题量" :value="pagination.total" hint="按当前筛选范围统计" />
      <StatusCard
        label="当前页"
        :value="`${pagination.current}/${totalPages}`"
        :hint="`每页展示 ${pagination.size} 道题`"
      />
      <StatusCard
        label="筛选条件"
        :value="activeFilterCount"
        :hint="activeFilterCount ? `已应用 ${activeFilterCount} 个筛选项` : '当前查看全部题目'"
      />
      <StatusCard
        label="题型范围"
        :value="selectedTypeLabel"
        :hint="rangeHint"
      />
    </div>

    <EmptyState
      v-if="!loading && !pagination.records.length"
      class="empty-panel"
      :description="activeFilterCount ? '没有找到符合条件的题目，请调整筛选条件后重试。' : '当前还没有题库记录。'"
      emoji="📚"
    >
      <div class="empty-actions">
        <el-button v-if="activeFilterCount" @click="resetFilters">清空筛选</el-button>
        <el-button type="primary" @click="handleCreate">新增题目</el-button>
      </div>
    </EmptyState>

    <template v-else>
      <el-table
        v-loading="loading"
        :data="pagination.records"
        stripe
        empty-text="暂无题库记录"
        :row-key="resolveRowKey"
      >
        <el-table-column prop="questionId" label="题号" min-width="100" />
        <el-table-column label="题型" min-width="110">
          <template #default="{ row }">
            <el-tag :type="resolveTypeTag(row)">
              {{ resolveQuestionTypeLabel(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="科目" min-width="140" show-overflow-tooltip />
        <el-table-column label="章节" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.section || '未分章节' }}
          </template>
        </el-table-column>
        <el-table-column label="题干" min-width="360" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="question-cell">
              <strong>{{ row.question || '--' }}</strong>
              <span>{{ row.level || '未设难度' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="参考答案" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatReferenceAnswer(row) }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="160">
          <template #default="{ row }">
            <el-space>
              <el-button link type="primary" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button
                link
                type="danger"
                :loading="deletingKey === resolveRowKey(row)"
                @click="handleDelete(row)"
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
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </template>
  </PageContainer>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusCard from '@/components/common/StatusCard.vue'
import { deleteQuestionBankItem, getQuestionBankPage } from '@/api/questionBankApi'
import { usePagination } from '@/composables/usePagination'
import { getSession, saveQuestionDraft } from '@/utils/auth'
import {
  buildConsolePath,
  QUESTION_TYPE_LABELS,
  QUESTION_TYPE_OPTIONS
} from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const pagination = usePagination(10)

const consoleRole = getSession()?.role
const questionBankPath = buildConsolePath(consoleRole, 'question-bank')
const createQuestionPath = buildConsolePath(consoleRole, 'questions/new')
const questionEditPath = buildConsolePath(consoleRole, 'questions/edit')

const filters = reactive({
  subject: '',
  keyword: '',
  questionType: null
})

const loading = ref(false)
const deletingKey = ref('')

const typeOptions = QUESTION_TYPE_OPTIONS

const totalPages = computed(() => Math.max(1, Math.ceil((pagination.total || 0) / pagination.size || 1)))
const activeFilterCount = computed(() => {
  return [
    filters.subject.trim(),
    filters.keyword.trim(),
    filters.questionType
  ].filter(Boolean).length
})

const selectedTypeLabel = computed(() => {
  return filters.questionType ? QUESTION_TYPE_LABELS[filters.questionType] : '全部题型'
})

const rangeHint = computed(() => {
  const parts = []

  if (filters.subject.trim()) {
    parts.push(`科目：${filters.subject.trim()}`)
  }

  if (filters.keyword.trim()) {
    parts.push(`关键词：${filters.keyword.trim()}`)
  }

  return parts.join(' · ') || '支持科目、题型和关键词组合筛选'
})

function parsePositiveNumber(value, fallback) {
  const parsed = Number(value)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback
}

function parseQuestionType(value) {
  const parsed = Number(value)
  return [1, 2, 3].includes(parsed) ? parsed : null
}

function buildListQuery() {
  const query = {}
  const subject = filters.subject.trim()
  const keyword = filters.keyword.trim()

  if (subject) {
    query.subject = subject
  }

  if (keyword) {
    query.keyword = keyword
  }

  if (filters.questionType) {
    query.questionType = String(filters.questionType)
  }

  if (pagination.current !== 1) {
    query.page = String(pagination.current)
  }

  if (pagination.size !== 10) {
    query.size = String(pagination.size)
  }

  return query
}

function getReturnToPath() {
  return router.resolve({
    path: questionBankPath,
    query: buildListQuery()
  }).fullPath
}

function syncStateFromQuery() {
  filters.subject = typeof route.query.subject === 'string' ? route.query.subject : ''
  filters.keyword = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  filters.questionType = parseQuestionType(route.query.questionType)
  pagination.current = parsePositiveNumber(route.query.page, 1)
  pagination.size = parsePositiveNumber(route.query.size, 10)
}

function normalizeFilterInputs() {
  filters.subject = filters.subject.trim()
  filters.keyword = filters.keyword.trim()
}

function resolveQuestionType(row) {
  return Number(row.questionType || row.type || 0)
}

function resolveQuestionTypeLabel(row) {
  return row.questionTypeName || row.typeLabel || QUESTION_TYPE_LABELS[resolveQuestionType(row)] || '未知题型'
}

function resolveTypeTag(row) {
  const type = resolveQuestionType(row)
  if (type === 2) {
    return 'success'
  }
  if (type === 3) {
    return 'warning'
  }
  return ''
}

function resolveRowKey(row) {
  return `${resolveQuestionType(row)}_${row.questionId}`
}

function formatReferenceAnswer(row) {
  const type = resolveQuestionType(row)
  const rawAnswer = String(row.answer || row.rightAnswer || '--').trim()

  if (type === 3) {
    if (rawAnswer === 'T') {
      return '正确'
    }
    if (rawAnswer === 'F') {
      return '错误'
    }
  }

  return rawAnswer || '--'
}

function createEditorDraft(row) {
  const questionType = resolveQuestionType(row)
  return {
    ...row,
    type: questionType,
    typeLabel: QUESTION_TYPE_LABELS[questionType],
    rightAnswer: questionType === 1 ? String(row.answer || row.rightAnswer || 'A') : undefined,
    answer: questionType === 1 ? String(row.answer || row.rightAnswer || 'A') : row.answer
  }
}

function extractErrorMessage(error, fallback) {
  return error?.response?.data?.message || fallback
}

async function fetchQuestions() {
  normalizeFilterInputs()
  void router.replace({
    path: questionBankPath,
    query: buildListQuery()
  })

  loading.value = true
  try {
    const response = await getQuestionBankPage({
      page: pagination.current,
      size: pagination.size,
      filters
    })

    if (response.code === 200 && response.data) {
      Object.assign(pagination, response.data)
      return
    }

    ElMessage.error(response.message || '加载题库失败')
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '加载题库失败，请稍后重试'))
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  void fetchQuestions()
}

function resetFilters() {
  Object.assign(filters, {
    subject: '',
    keyword: '',
    questionType: null
  })
  pagination.current = 1
  void fetchQuestions()
}

function refreshQuestions() {
  void fetchQuestions()
}

function handleCurrentChange(page) {
  pagination.current = page
  void fetchQuestions()
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.current = 1
  void fetchQuestions()
}

function handleCreate() {
  normalizeFilterInputs()
  router.push({
    path: createQuestionPath,
    query: {
      subject: filters.subject,
      returnTo: getReturnToPath()
    }
  })
}

function handleEdit(row) {
  saveQuestionDraft(createEditorDraft(row))
  router.push({
    path: questionEditPath,
    query: {
      returnTo: getReturnToPath()
    }
  })
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `删除后将同时移除题目和试卷中的可删关联，是否继续删除题号 ${row.questionId}？`,
      '删除题目',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消'
      }
    )
  } catch (error) {
    return
  }

  deletingKey.value = resolveRowKey(row)
  try {
    const response = await deleteQuestionBankItem(resolveQuestionType(row), row.questionId)

    if (response.code === 200) {
      const deletedRelations = Number(response.data?.deletedPaperRelations || 0)
      ElMessage.success(
        deletedRelations > 0
          ? `题目已删除，并移除了 ${deletedRelations} 条试卷关联`
          : '题目已删除'
      )

      if (pagination.records.length === 1 && pagination.current > 1) {
        pagination.current -= 1
      }

      await fetchQuestions()
      return
    }

    ElMessage.error(response.message || '删除题目失败')
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '删除题目失败，请稍后重试'))
  } finally {
    deletingKey.value = ''
  }
}

onMounted(() => {
  syncStateFromQuery()
  void fetchQuestions()
})
</script>

<style scoped>
.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 0 16px;
}

.filter-actions {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding-bottom: 22px;
}

.summary-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  margin-bottom: 20px;
}

.empty-panel {
  margin-top: 20px;
}

.empty-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.question-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.question-cell strong {
  font-size: 14px;
  line-height: 1.6;
}

.question-cell span {
  color: var(--text-secondary);
  font-size: 12px;
}

.footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .filter-actions {
    padding-bottom: 0;
  }

  .empty-actions {
    flex-wrap: wrap;
  }
}
</style>
