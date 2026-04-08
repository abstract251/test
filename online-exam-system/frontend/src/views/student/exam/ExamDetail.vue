<template>
  <div v-if="exam" class="student-page">
    <PageContainer>
      <PageHeader
        title="考试详情"
        description="你可以在这里查看考试说明、试卷结构和考生提示。到达考试时间后，开始考试按钮才会激活。"
      >
        <template #actions>
          <el-button @click="router.push('/student/home')">返回考试中心</el-button>
          <el-button type="primary" :disabled="!started" @click="router.push(`/student/exams/${exam.examCode}/answer`)">
            {{ started ? '进入答题页' : '未到开考时间' }}
          </el-button>
        </template>
      </PageHeader>

      <ExamMetaPanel :exam="exam" />
    </PageContainer>

    <PageContainer>
      <PageHeader
        title="试卷结构"
        description="可按题型查看本场考试题目。"
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
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import ExamMetaPanel from '@/components/exam/ExamMetaPanel.vue'
import PaperQuestionGroup from '@/components/exam/PaperQuestionGroup.vue'
import { getExamById } from '@/api/examApi'
import { getPaper } from '@/api/paperApi'
import { ensureQuestionMap } from '@/utils/adapters'
import { isExamStarted } from '@/utils/date'
import { QUESTION_TYPE_OPTIONS } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const exam = ref(null)
const questionMap = ref(ensureQuestionMap())

const started = computed(() => isExamStarted(exam.value?.examDate))
const groupedQuestions = computed(() =>
  QUESTION_TYPE_OPTIONS.map((item) => ({
    type: item.value,
    title: item.label,
    description: `当前试卷中的${item.label}`,
    questions: questionMap.value[item.value]
  }))
)

async function fetchExamDetail() {
  const examResponse = await getExamById(route.params.examCode)
  if (examResponse.code === 200 && examResponse.data) {
    exam.value = examResponse.data
    const paperResponse = await getPaper(exam.value.paperId)
    if (paperResponse.code === 200) {
      questionMap.value = ensureQuestionMap(paperResponse.data)
    }
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
