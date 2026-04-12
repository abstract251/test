<template>
  <div v-if="exam" class="paper-builder">
    <PageContainer>
      <PageHeader
        title="试卷编辑"
        :description="paperReadOnly ? '当前试卷已冻结或考试已撤销，题目不可再增删。' : '左侧为科目题库，右侧为当前试卷题目。'"
      >
        <template #actions>
          <el-button @click="router.push('/console/teacher/exams')">返回考试列表</el-button>
          <el-tooltip v-if="paperReadOnly" content="试卷已冻结或考试已撤销，不可再改题" placement="bottom">
            <span>
              <el-button type="danger" plain disabled>清空试卷</el-button>
            </span>
          </el-tooltip>
          <el-button
            v-else
            type="danger"
            plain
            :loading="operating"
            :disabled="operating"
            @click="handleClearPaper"
          >
            清空试卷
          </el-button>
        </template>
      </PageHeader>

      <ExamMetaPanel :exam="exam" />

      <div class="builder-summary">
        <StatusCard label="题库题量" :value="totals.available" hint="按当前考试科目加载" />
        <StatusCard label="已选题量" :value="totals.selected" hint="每次增删题后立即刷新" />
        <StatusCard label="试卷总分" :value="paperScore" hint="按题目数量自动更新总分" />
      </div>

      <el-input
        v-model="keyword"
        placeholder="按题干或章节筛选当前页面题目"
        clearable
      />
    </PageContainer>

    <div class="builder-grid">
      <PaperBuilderPanel
        title="科目题库"
        description="可将题目加入当前试卷。"
        :count="totals.available"
      >
        <div class="panel-groups">
          <PaperQuestionGroup
            v-for="item in availableGroups"
            :key="`available-${item.type}`"
            :title="item.title"
            :description="item.description"
            :questions="item.questions"
            :type="item.type"
            :disabled-ids="selectedIds[item.type]"
            action-text="加入试卷"
            :action-loading="operating"
            :action-disabled="operating"
            :show-action="!paperReadOnly"
            empty-text="当前科目下暂无此题型题目"
            @action="handleAddQuestion"
          />
        </div>
      </PaperBuilderPanel>

      <PaperBuilderPanel
        title="当前试卷"
        description="可将题目从试卷中移出。"
        :count="totals.selected"
      >
        <div class="panel-groups">
          <PaperQuestionGroup
            v-for="item in selectedGroups"
            :key="`selected-${item.type}`"
            :title="item.title"
            :description="item.description"
            :questions="item.questions"
            :type="item.type"
            action-text="移出试卷"
            :action-loading="operating"
            :action-disabled="operating"
            :show-action="!paperReadOnly"
            empty-text="当前试卷下暂无此题型题目"
            @action="handleRemoveQuestion"
          />
        </div>
      </PaperBuilderPanel>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusCard from '@/components/common/StatusCard.vue'
import ExamMetaPanel from '@/components/exam/ExamMetaPanel.vue'
import PaperBuilderPanel from '@/components/exam/PaperBuilderPanel.vue'
import PaperQuestionGroup from '@/components/exam/PaperQuestionGroup.vue'
import { getExamById } from '@/api/examApi'
import { addPaperQuestion, clearPaper, getPaper, getPaperScore, getPracticeBySubject, removePaperQuestion } from '@/api/paperApi'
import { usePaperBuilder } from '@/composables/usePaperBuilder'
import { QUESTION_TYPE_OPTIONS } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const exam = ref(null)
const paperScore = ref(0)
const keyword = ref('')
const operating = ref(false)

const paperReadOnly = computed(
  () => !!(exam.value?.paperLocked || exam.value?.revokedAt)
)

const {
  availableQuestions,
  selectedQuestions,
  totals,
  setAvailableQuestions,
  setSelectedQuestions,
  hasQuestion
} = usePaperBuilder()

const selectedIds = computed(() => ({
  1: selectedQuestions.value[1].map((item) => item.questionId),
  2: selectedQuestions.value[2].map((item) => item.questionId),
  3: selectedQuestions.value[3].map((item) => item.questionId)
}))

function matchKeyword(question) {
  const safeKeyword = keyword.value.trim().toLowerCase()
  if (!safeKeyword) {
    return true
  }
  return [question.question, question.section, question.analysis]
    .filter(Boolean)
    .some((field) => String(field).toLowerCase().includes(safeKeyword))
}

const availableGroups = computed(() =>
  QUESTION_TYPE_OPTIONS.map((item) => ({
    type: item.value,
    title: item.label,
    description: `科目题库中的${item.label}`,
    questions: availableQuestions.value[item.value].filter(matchKeyword)
  }))
)

const selectedGroups = computed(() =>
  QUESTION_TYPE_OPTIONS.map((item) => ({
    type: item.value,
    title: item.label,
    description: `当前试卷中的${item.label}`,
    questions: selectedQuestions.value[item.value].filter(matchKeyword)
  }))
)

async function fetchPaperData() {
  try {
    const [paperResponse, scoreResponse] = await Promise.all([
      getPaper(exam.value.paperId),
      getPaperScore(exam.value.paperId)
    ])
    if (paperResponse.code === 200) {
      setSelectedQuestions(paperResponse.data)
    }
    if (scoreResponse.code === 200) {
      paperScore.value = scoreResponse.data || 0
    }
  } catch (error) {
    ElMessage.error('加载试卷数据失败，请稍后重试')
  }
}

async function fetchExamAndPractice() {
  try {
    const examResponse = await getExamById(route.params.examCode)
    if (examResponse.code !== 200 || !examResponse.data) {
      ElMessage.error(examResponse.message || '获取考试信息失败')
      return
    }

    exam.value = examResponse.data
    const practiceResponse = await getPracticeBySubject(exam.value.source)
    if (practiceResponse.code === 200) {
      setAvailableQuestions(practiceResponse.data)
    } else {
      ElMessage.warning(practiceResponse.message || '题库数据加载不完整')
    }
    await fetchPaperData()
  } catch (error) {
    ElMessage.error('获取试卷信息失败，请稍后重试')
  }
}

async function handleAddQuestion({ type, question }) {
  if (paperReadOnly.value) {
    return
  }
  if (operating.value) {
    return
  }
  if (hasQuestion(type, question.questionId)) {
    ElMessage.warning('这道题已经在当前试卷中')
    return
  }

  operating.value = true
  try {
    const response = await addPaperQuestion({
      paperId: exam.value.paperId,
      questionType: type,
      questionId: question.questionId
    })

    if (response.code === 200) {
      ElMessage.success('题目已加入试卷')
      await fetchPaperData()
      return
    }

    ElMessage.error(response.message || '加入试卷失败')
  } catch (error) {
    ElMessage.error('加入试卷失败，请稍后重试')
  } finally {
    operating.value = false
  }
}

async function handleRemoveQuestion({ type, question }) {
  if (paperReadOnly.value) {
    return
  }
  if (operating.value) {
    return
  }
  operating.value = true
  try {
    const response = await removePaperQuestion(exam.value.paperId, type, question.questionId)
    if (response.code === 200) {
      ElMessage.success('题目已从试卷移除')
      await fetchPaperData()
      return
    }
    ElMessage.error(response.message || '移出试卷失败')
  } catch (error) {
    ElMessage.error('移出试卷失败，请稍后重试')
  } finally {
    operating.value = false
  }
}

async function handleClearPaper() {
  if (paperReadOnly.value) {
    return
  }
  if (operating.value) {
    return
  }
  try {
    await ElMessageBox.confirm('确认清空整张试卷吗？该操作无法恢复。', '清空试卷', {
      type: 'warning'
    })
  } catch (error) {
    return
  }

  operating.value = true
  try {
    const response = await clearPaper(exam.value.paperId)
    if (response.code === 200) {
      ElMessage.success('试卷已清空')
      await fetchPaperData()
      return
    }
    ElMessage.error(response.message || '清空试卷失败')
  } catch (error) {
    ElMessage.error('清空试卷失败，请稍后重试')
  } finally {
    operating.value = false
  }
}

onMounted(fetchExamAndPractice)
</script>

<style scoped>
.paper-builder {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.builder-summary {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  margin: 20px 0;
}

.builder-grid {
  display: grid;
  gap: 20px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.panel-groups {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

@media (max-width: 1100px) {
  .builder-grid {
    grid-template-columns: 1fr;
  }
}
</style>
