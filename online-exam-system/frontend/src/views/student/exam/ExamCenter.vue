<template>
  <div class="student-page">
    <PageContainer>
      <PageHeader
        title="考试中心"
        description="这里会显示你可以查看或参加的考试。考试开始后，可以直接进入答题。"
      />

      <div class="student-page__summary">
        <StatusCard label="可参加考试" :value="filteredExams.length" hint="这里只展示当前与你有关的考试" />
        <StatusCard label="当前账号" :value="session?.displayName || session?.userName || '--'" hint="如需修改信息，可前往个人资料页" />
        <StatusCard label="正在进行" :value="ongoingCount" hint="点击卡片可进入考试或查看详情" />
      </div>

      <PageNotice
        v-if="loadError"
        type="error"
        title="考试列表暂时没有加载出来"
        :description="loadError"
      />

      <template v-else>
        <div class="toolbar">
          <el-input v-model="keyword" placeholder="按考试名称、科目或状态查找" clearable />
        </div>

        <EmptyState
          v-if="!filteredExams.length"
          description="目前没有可参加或可查看的考试。"
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
import EmptyState from '@/components/common/EmptyState.vue'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import PageNotice from '@/components/common/PageNotice.vue'
import StatusCard from '@/components/common/StatusCard.vue'
import ExamCard from '@/components/exam/ExamCard.vue'
import { getStudentExams } from '@/api/examApi'
import { useAuthSession } from '@/composables/useAuthSession'
import { useStudentExamFilter } from '@/composables/useStudentExamFilter'
import { resolveUserFacingError } from '@/utils/feedback'
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

    loadError.value = resolveUserFacingError(response, '请刷新页面后再试')
  } catch (error) {
    loadError.value = resolveUserFacingError(error, '请检查网络后刷新页面')
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
