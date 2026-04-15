<template>
  <PageContainer>
    <PageHeader
      title="题库工作台"
      description="先按科目加载题目，再按关键词、章节和题型筛选并编辑。"
    >
      <template #actions>
        <el-button
          type="primary"
          @click="router.push({ path: createQuestionPath, query: { subject: subjectInput } })"
        >
          新增题目
        </el-button>
      </template>
    </PageHeader>

    <div class="filters">
      <el-input v-model="subjectInput" placeholder="请输入科目后加载题库，例如 Java" />
      <el-input v-model="keyword" placeholder="按题干或解析筛选" />
      <el-input v-model="section" placeholder="按章节筛选" />
      <el-select v-model="typeFilter" clearable placeholder="题型">
        <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <div class="filters__actions">
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="primary" @click="loadQuestions">加载题库</el-button>
      </div>
    </div>

    <EmptyState
      v-if="!loaded && !tableData.length"
      description="先输入科目并点击“加载题库”，再进行筛选和编辑。"
      emoji="📚"
    />

    <template v-else>
      <el-table :data="tableData" stripe>
        <el-table-column prop="questionId" label="题号" min-width="90" />
        <el-table-column prop="typeLabel" label="题型" min-width="100" />
        <el-table-column prop="subject" label="科目" min-width="120" />
        <el-table-column prop="section" label="章节" min-width="120" />
        <el-table-column prop="question" label="题干" min-width="320" show-overflow-tooltip />
        <el-table-column prop="level" label="难度" min-width="100" />
        <el-table-column fixed="right" label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
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
import { getPracticeBySubject } from '@/api/paperApi'
import { flattenQuestionMap } from '@/utils/adapters'
import { getSession, saveQuestionDraft } from '@/utils/auth'
import { buildConsolePath, QUESTION_TYPE_OPTIONS } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const consoleRole = getSession()?.role

const subjectInput = ref('')
const keyword = ref('')
const section = ref('')
const typeFilter = ref(null)
const loaded = ref(false)
const questions = ref([])
const createQuestionPath = buildConsolePath(consoleRole, 'questions/new')
const questionEditPath = buildConsolePath(consoleRole, 'questions/edit')

const typeOptions = QUESTION_TYPE_OPTIONS

const tableData = computed(() =>
  questions.value.filter((item) => {
    const keywordMatch = !keyword.value.trim() || [item.question, item.analysis]
      .filter(Boolean)
      .some((field) => field.toLowerCase().includes(keyword.value.trim().toLowerCase()))
    const sectionMatch = !section.value.trim() || String(item.section || '').toLowerCase().includes(section.value.trim().toLowerCase())
    const typeMatch = !typeFilter.value || item.type === typeFilter.value
    return keywordMatch && sectionMatch && typeMatch
  })
)

async function loadQuestions() {
  if (!subjectInput.value.trim()) {
    ElMessage.warning('请先输入科目')
    return
  }

  const response = await getPracticeBySubject(subjectInput.value.trim())
  if (response.code === 200) {
    questions.value = flattenQuestionMap(response.data)
    loaded.value = true
  }
}

function handleEdit(row) {
  saveQuestionDraft(row)
  router.push(questionEditPath)
}

function resetFilters() {
  keyword.value = ''
  section.value = ''
  typeFilter.value = null
}

onMounted(() => {
  if (typeof route.query.subject === 'string' && route.query.subject) {
    subjectInput.value = route.query.subject
    loadQuestions()
  }
})
</script>

<style scoped>
.filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}

.filters__actions {
  display: flex;
  gap: 12px;
  align-items: center;
}
</style>
