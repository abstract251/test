<template>
  <div class="exam-meta-panel">
    <div class="exam-meta-panel__grid">
      <div>
        <span class="label">科目</span>
        <strong>{{ exam.source }}</strong>
      </div>
      <div>
        <span class="label">考试时间</span>
        <strong>{{ formatDateTime(exam.examStartAt || exam.examDate) }}</strong>
      </div>
      <div>
        <span class="label">时长</span>
        <strong>{{ exam.totalTime }} 分钟</strong>
      </div>
      <div>
        <span class="label">总分</span>
        <strong>{{ exam.totalScore }} 分</strong>
      </div>
      <div>
        <span class="label">年级 / 专业</span>
        <strong>{{ exam.grade }} / {{ exam.major }}</strong>
      </div>
      <div>
        <span class="label">学院</span>
        <strong>{{ exam.institute }}</strong>
      </div>
      <div v-if="exam.windowEndAt">
        <span class="label">结束时间</span>
        <strong>{{ formatDateTime(exam.windowEndAt) }}</strong>
      </div>
    </div>
    <el-alert
      v-if="exam.tips"
      :title="exam.tips"
      type="success"
      show-icon
      :closable="false"
    />
  </div>
</template>

<script setup>
import { formatDateTime } from '@/utils/date'

defineProps({
  exam: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.exam-meta-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.exam-meta-panel__grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.label {
  display: block;
  margin-bottom: 8px;
  color: var(--text-muted);
  font-size: 12px;
}

strong {
  font-size: 16px;
}
</style>
