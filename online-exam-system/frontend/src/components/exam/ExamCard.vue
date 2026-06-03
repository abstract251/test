<template>
  <article class="exam-card" @click="$emit('select', exam)">
    <div class="exam-card__meta">
      <span class="exam-card__tag">{{ exam.type || '考试' }}</span>
      <span class="exam-card__status" :class="statusClass">
        {{ statusLabel }}
      </span>
    </div>
    <h3>{{ exam.source }}</h3>
    <p>{{ exam.description || '暂无说明' }}</p>
    <div class="exam-card__info">
      <span>{{ formatDateTime(examTime) }}</span>
      <span>{{ exam.totalTime }} 分钟</span>
      <span>{{ exam.totalScore }} 分</span>
    </div>
    <p class="exam-card__action">{{ actionLabel }}</p>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { formatDateTime, resolveExamDateTime } from '@/utils/date'

const props = defineProps({
  exam: {
    type: Object,
    required: true
  }
})

defineEmits(['select'])

const examTime = computed(() => resolveExamDateTime(props.exam))

const statusLabel = computed(() => {
  switch (props.exam.examState) {
    case 'ONGOING':
      return props.exam.attemptStatus === 'SUBMITTED' ? '已交卷' : '进行中'
    case 'ENDED':
      return '已结束'
    case 'REVOKED':
      return '已撤销'
    default:
      return '未开始'
  }
})

const actionLabel = computed(() => {
  if (props.exam.canEnter) {
    return '点击后直接进入作答'
  }
  if (props.exam.examState === 'UPCOMING') {
    return '点击查看考试说明、题型分布和开考倒计时'
  }
  if (props.exam.attemptStatus === 'SUBMITTED') {
    return '本场考试已交卷，可查看状态信息'
  }
  return '点击查看考试详情'
})

const statusClass = computed(() => ({
  'is-upcoming': props.exam.examState === 'UPCOMING',
  'is-ongoing': props.exam.examState === 'ONGOING' && props.exam.attemptStatus !== 'SUBMITTED',
  'is-ended': props.exam.examState === 'ENDED' || props.exam.attemptStatus === 'SUBMITTED',
  'is-revoked': props.exam.examState === 'REVOKED'
}))
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

.exam-card__status.is-ongoing {
  background: rgba(79, 175, 143, 0.18);
  color: #1c6b53;
}

.exam-card__status.is-ended {
  background: rgba(148, 163, 184, 0.18);
  color: #475569;
}

.exam-card__status.is-revoked {
  background: rgba(239, 68, 68, 0.16);
  color: #b42318;
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

.exam-card__action {
  margin-top: 4px;
  color: var(--brand-primary-deep);
  font-size: 13px;
}
</style>
