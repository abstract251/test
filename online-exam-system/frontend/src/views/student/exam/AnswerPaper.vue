<template>
  <div v-if="exam" class="student-page">
    <PageContainer>
      <PageHeader
        title="在线答题"
        description="请在此页面查看题目并完成作答。"
      >
        <template #actions>
          <el-button @click="router.push(`/student/exams/${exam.examCode}`)">返回考试详情</el-button>
        </template>
      </PageHeader>

      <ExamMetaPanel :exam="exam" />
      <el-alert
        title="当前页面暂不支持交卷，请先完成作答练习。"
        type="warning"
        show-icon
        :closable="false"
      />
    </PageContainer>

    <PageContainer>
      <div class="answer-layout">
        <div class="answer-layout__content">
          <PaperQuestionGroup
            v-for="item in groupedQuestions"
            :key="item.type"
            :title="item.title"
            :description="`${item.questions.length} 题`"
            :questions="item.questions"
            :type="item.type"
            :show-action="false"
          />
        </div>

        <aside class="answer-layout__sidebar">
          <StatusCard label="作答状态" value="进行中" hint="请按顺序完成所有题目" />
          <StatusCard label="倒计时" value="--:--" hint="当前页面暂不提供考试计时" />
          <el-button type="primary" disabled>交卷（暂不可用）</el-button>
        </aside>
      </div>
    </PageContainer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusCard from '@/components/common/StatusCard.vue'
import ExamMetaPanel from '@/components/exam/ExamMetaPanel.vue'
import PaperQuestionGroup from '@/components/exam/PaperQuestionGroup.vue'
import { getExamById } from '@/api/examApi'
import { getPaper } from '@/api/paperApi'
import { ensureQuestionMap } from '@/utils/adapters'
import { QUESTION_TYPE_OPTIONS } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const exam = ref(null)
const questionMap = ref(ensureQuestionMap())

const groupedQuestions = computed(() =>
  QUESTION_TYPE_OPTIONS.map((item) => ({
    type: item.value,
    title: item.label,
    questions: questionMap.value[item.value]
  }))
)

async function fetchExamContext() {
  const examResponse = await getExamById(route.params.examCode)
  if (examResponse.code === 200 && examResponse.data) {
    exam.value = examResponse.data
    const paperResponse = await getPaper(exam.value.paperId)
    if (paperResponse.code === 200) {
      questionMap.value = ensureQuestionMap(paperResponse.data)
    }
  }
}

onMounted(fetchExamContext)
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
  gap: 18px;
}

.answer-layout__sidebar {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

@media (max-width: 900px) {
  .answer-layout {
    grid-template-columns: 1fr;
  }
}
</style>
