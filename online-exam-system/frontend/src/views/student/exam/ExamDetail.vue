<template>
  <div v-if="exam" class="student-page">
    <PageContainer>
      <PageHeader
        title="考试详情"
        description="考前请先确认考试说明、开始时间和作答时长，进入答题页后系统会自动暂存作答。"
      >
        <template #actions>
          <el-button @click="router.push('/student/home')">返回考试中心</el-button>
          <el-button type="primary" :disabled="!canEnter" @click="goAnswer">
            {{ enterButtonText }}
          </el-button>
        </template>
      </PageHeader>

      <ExamMetaPanel :exam="examForPanel" />

      <el-alert
        v-if="policy?.revoked"
        title="本场考试已由管理员撤销，无法参加。"
        type="error"
        show-icon
        :closable="false"
      />
      <el-alert
        v-else-if="policy && !policy.inExamWindow"
        title="当前不在考试开放时间内。"
        :description="windowHint"
        type="warning"
        show-icon
        :closable="false"
      />
      <el-alert
        v-else-if="policy"
        title="进入答题页后将按后端考试会话接口开始或恢复作答。"
        type="info"
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
import { formatDateTime } from '@/utils/date'

const route = useRoute()
const router = useRouter()
const exam = ref(null)
const policy = ref(null)

const canEnter = computed(() => Boolean(policy.value?.inExamWindow) && !policy.value?.revoked)

const enterButtonText = computed(() => {
  if (!policy.value) {
    return '加载中'
  }
  if (policy.value.revoked) {
    return '本场考试已取消'
  }
  if (policy.value.inExamWindow) {
    return '进入答题页'
  }
  return '当前不可进入'
})

const examForPanel = computed(() => {
  if (!exam.value) {
    return null
  }

  return {
    ...exam.value,
    freezeAt: policy.value?.freezeAt,
    windowEndAt: policy.value?.windowEndAt,
    inExamWindow: policy.value?.inExamWindow,
    snapshotReady: policy.value?.snapshotReady
  }
})

const windowHint = computed(() => {
  if (!policy.value) {
    return ''
  }

  const startAt = policy.value.examStartAt ? formatDateTime(policy.value.examStartAt) : '--'
  const endAt = policy.value.windowEndAt ? formatDateTime(policy.value.windowEndAt) : '--'
  return `考试窗口：${startAt} 至 ${endAt}`
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
