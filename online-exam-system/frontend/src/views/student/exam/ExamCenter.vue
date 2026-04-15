<template>
  <div class="student-page">
    <PageContainer>
      <PageHeader
        title="考试中心"
        description="这里只显示与当前学生年级、专业、学院匹配的考试。未到开考时间可查看详情，但不能进入答题。"
      />

      <div class="student-page__summary">
        <StatusCard label="匹配到的考试" :value="filteredExams.length" hint="根据你的专业和年级自动筛选" />
        <StatusCard label="当前身份" :value="session?.userName || '--'" hint="如需修改个人资料，请进入个人资料页" />
      </div>

      <div class="toolbar">
        <el-input v-model="keyword" placeholder="按科目、说明、专业搜索" clearable />
      </div>

      <EmptyState
        v-if="!filteredExams.length"
        description="当前没有匹配到考试，可能是还未发布或筛选条件不匹配。"
        emoji="🪴"
      />

      <div v-else class="exam-grid">
        <ExamCard
          v-for="item in filteredExams"
          :key="item.examCode"
          :exam="item"
          @select="router.push(`/student/exams/${item.examCode}`)"
        />
      </div>
    </PageContainer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusCard from '@/components/common/StatusCard.vue'
import ExamCard from '@/components/exam/ExamCard.vue'
import { useAuthSession } from '@/composables/useAuthSession'
import { useStudentExamFilter } from '@/composables/useStudentExamFilter'
import { getAllExams } from '@/api/examApi'

const router = useRouter()
const { session } = useAuthSession()
const exams = ref([])
const keyword = ref('')

const filteredExams = useStudentExamFilter(exams, session, keyword)

async function fetchExams() {
  const response = await getAllExams()
  if (response.code === 200) {
    exams.value = response.data || []
  }
}

onMounted(fetchExams)
</script>

<style scoped>
.student-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.student-page__summary {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  margin-bottom: 18px;
}

.toolbar {
  margin-bottom: 18px;
}

.exam-grid {
  display: grid;
  gap: 18px;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

@media (max-width: 640px) {
  .exam-grid {
    grid-template-columns: 1fr;
  }
}
</style>
