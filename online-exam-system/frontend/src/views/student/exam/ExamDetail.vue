<template>
  <div v-if="exam" class="student-page">
    <PageContainer>
      <PageHeader
        title="考试详情"
        description="开考时间、时长与服务器策略一致时方可进入答题；试卷预览来自当前试卷关联，正式答题以冻结快照为准。"
      >
        <template #actions>
          <el-button @click="router.push('/student/home')">返回考试中心</el-button>
          <el-button type="primary" :disabled="!canEnter" @click="goAnswer">
            {{ enterButtonText }}
          </el-button>
        </template>
      </PageHeader>

      <ExamMetaPanel :exam="exam" />
      <el-alert
        v-if="policy?.revoked"
        title="本场考试已由管理员撤销，无法参加。"
        type="error"
        show-icon
        :closable="false"
      />
      <el-alert
        v-else-if="policy && !policy.inExamWindow"
        title="当前不在考试开放时间内（未到开考时间或考试已结束）。"
        type="warning"
        show-icon
        :closable="false"
      />
    </PageContainer>

    <PageContainer>
      <PageHeader
        title="试卷结构（预览）"
        description="题目列表来自组卷；正式答题页面题目与冻结快照一致。"
      />

      <div class="question-groups">
        <PaperQuestionGroup
          v-for="item in groupedQuestions"
          :key="item.type"
          :title="item.title"
          :description="item.description"
          :questions="item.questions"
          :type="item.type"
          :show-action="false"
        />
      </div>
    </PageContainer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import ExamMetaPanel from '@/components/exam/ExamMetaPanel.vue'
import PaperQuestionGroup from '@/components/exam/PaperQuestionGroup.vue'
import { getExamById, getExamPolicy } from '@/api/examApi'
import { getPaper } from '@/api/paperApi'
import { ensureQuestionMap } from '@/utils/adapters'
import { QUESTION_TYPE_OPTIONS } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const exam = ref(null)
const questionMap = ref(ensureQuestionMap())
const policy = ref(null)

const canEnter = computed(
  () => Boolean(policy.value?.inExamWindow) && !policy.value?.revoked
)

const enterButtonText = computed(() => {
  if (!policy.value) {
    return '加载中…'
  }
  if (policy.value.revoked) {
    return '本场考试已取消'
  }
  if (policy.value.inExamWindow) {
    return '进入答题页'
  }
  return '当前不可进入'
})

const groupedQuestions = computed(() =>
  QUESTION_TYPE_OPTIONS.map((item) => ({
    type: item.value,
    title: item.label,
    description: `当前试卷中的${item.label}`,
    questions: questionMap.value[item.value]
  }))
)

function goAnswer() {
  if (!canEnter.value) {
    return
  }
  router.push(`/student/exams/${exam.value.examCode}/answer`)
}

async function fetchExamDetail() {
  const examResponse = await getExamById(route.params.examCode)
  if (examResponse.code !== 200 || !examResponse.data) {
    ElMessage.error(examResponse.message || '加载失败')
    return
  }
  exam.value = examResponse.data
  const paperResponse = await getPaper(exam.value.paperId)
  if (paperResponse.code === 200) {
    questionMap.value = ensureQuestionMap(paperResponse.data)
  }
  const pol = await getExamPolicy(route.params.examCode)
  if (pol.code === 200 && pol.data) {
    policy.value = pol.data
  }
}

onMounted(fetchExamDetail)
</script>

<style scoped>
.student-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.question-groups {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
</style>
