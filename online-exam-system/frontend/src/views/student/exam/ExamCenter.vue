<template>
  <div class="student-page">
    <PageContainer>
      <PageHeader
        title="考试中心"
        description="这里只显示与当前学生相关的考试，开考后可直接进入作答。"
      />

      <div class="student-page__summary">
        <StatusCard label="可见考试" :value="filteredExams.length" hint="后端已按当前学生参考范围筛选" />
        <StatusCard label="当前身份" :value="session?.displayName || session?.userName || '--'" hint="如需修改个人资料，请进入个人资料页" />
        <StatusCard label="进行中" :value="ongoingCount" hint="点击卡片可直接进入作答" />
      </div>

      <el-alert
        v-if="loadError"
        :title="loadError"
        type="error"
        show-icon
        :closable="false"
      />

      <template v-else>
        <div class="toolbar">
          <el-input v-model="keyword" placeholder="按科目、说明、专业、状态搜索" clearable />
        </div>

        <EmptyState
          v-if="!filteredExams.length"
          description="当前没有可参加或可查看的考试。"
          emoji="📝"
        />

        <div v-else class="exam-grid">
          <ExamCard
            v-for="item in filteredExams"
            :key="item.examCode"
            :exam="item"
            @select="openExam(item)"
          />
        </div>
      </template>
    </PageContainer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusCard from '@/components/common/StatusCard.vue'
import ExamCard from '@/components/exam/ExamCard.vue'
import { useAuthSession } from '@/composables/useAuthSession'
import { useStudentExamFilter } from '@/composables/useStudentExamFilter'
import { getStudentExams } from '@/api/examApi'
import { createRequestCoordinator } from '@/utils/requestCoordinator'

const examCenterCoordinator = createRequestCoordinator('student-exams:list', {
  defaultTtlMs: 15000
})

const router = useRouter()
const { session } = useAuthSession()
const exams = ref([])
const keyword = ref('')
const loadError = ref('')

const filteredExams = useStudentExamFilter(exams, session, keyword)
const ongoingCount = computed(() => exams.value.filter((item) => item.examState === 'ONGOING' && item.canEnter).length)

function openExam(item) {
  if (item?.canEnter) {
    router.push(`/student/exams/${item.examCode}/answer`)
    return
  }
  router.push(`/student/exams/${item.examCode}`)
}

async function fetchExams() {
  loadError.value = ''

  try {
    const response = await examCenterCoordinator.load(
      'student-exams:list',
      () => getStudentExams()
    )
    if (response.code === 200 && response.data) {
      exams.value = response.data.records || []
      return
    }

    loadError.value = response.message || '考试列表加载失败'
  } catch (error) {
    loadError.value = error?.response?.data?.message || '考试列表加载失败，请稍后重试'
  }
}

onMounted(() => {
  void fetchExams()
})
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
  margin: 18px 0;
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
