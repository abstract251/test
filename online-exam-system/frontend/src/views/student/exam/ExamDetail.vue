<template>
  <div v-if="exam" class="student-page">
    <PageContainer>
      <PageHeader
        title="考试详情"
        description="考前注意查看查看考试说明与时间安排；准备无误后可开始答题。"
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

  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import ExamMetaPanel from '@/components/exam/ExamMetaPanel.vue'
import { getExamById, getExamPolicy } from '@/api/examApi'

const route = useRoute()
const router = useRouter()
const exam = ref(null)
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
</style>
