<template>
  <article class="exam-card" @click="$emit('select', exam)">
    <div class="exam-card__meta">
      <span class="exam-card__tag">{{ exam.type || '考试' }}</span>
      <span class="exam-card__status" :class="{ 'is-upcoming': upcoming }">
        {{ upcoming ? '未开始' : '可查看' }}
      </span>
    </div>
    <h3>{{ exam.source }}</h3>
    <p>{{ exam.description || '暂无说明' }}</p>
    <div class="exam-card__info">
      <span>{{ formatDateTime(exam.examDate) }}</span>
      <span>{{ exam.totalTime }} 分钟</span>
      <span>{{ exam.totalScore }} 分</span>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { formatDateTime, isUpcoming } from '@/utils/date'

const props = defineProps({
  exam: {
    type: Object,
    required: true
  }
})

defineEmits(['select'])

const upcoming = computed(() => isUpcoming(props.exam.examDate))
</script>

<style scoped>
.exam-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
  min-height: 220px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-card);
  cursor: pointer;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}

.exam-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-soft);
}

.exam-card__meta,
.exam-card__info {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.exam-card__tag,
.exam-card__status {
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  background: var(--bg-soft);
  color: var(--brand-primary-deep);
}

.exam-card__status.is-upcoming {
  background: rgba(255, 200, 87, 0.22);
  color: #996b09;
}

.exam-card h3 {
  margin: 0;
  font-size: 22px;
}

.exam-card p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.exam-card__info {
  margin-top: auto;
  color: var(--text-muted);
  font-size: 13px;
}
</style>
