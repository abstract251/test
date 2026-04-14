<template>
  <section class="score-chart">
    <div class="score-chart__header">
      <div>
        <h3 class="score-chart__title">{{ title }}</h3>
        <p class="score-chart__description">{{ description }}</p>
      </div>
      <span class="score-chart__total">总人数 {{ total }}</span>
    </div>

    <div v-if="hasData" class="score-chart__body">
      <div v-for="item in normalizedItems" :key="item.label" class="score-chart__row">
        <span class="score-chart__label">{{ item.label }}</span>
        <div class="score-chart__track">
          <div class="score-chart__fill" :style="{ width: `${item.ratio * 100}%` }" />
        </div>
        <strong class="score-chart__value">{{ item.value }}</strong>
      </div>
    </div>

    <EmptyState v-else description="暂无成绩分布数据" />
  </section>
</template>

<script setup>
import { computed } from 'vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { SCORE_DISTRIBUTION_LABELS } from '@/utils/constants'

const props = defineProps({
  title: {
    type: String,
    default: '成绩分布'
  },
  description: {
    type: String,
    default: '按分数段查看本场考试成绩分布。'
  },
  distribution: {
    type: [Array, Object],
    default: () => ({})
  }
})

const distributionMap = computed(() => {
  if (Array.isArray(props.distribution)) {
    return props.distribution.reduce((accumulator, item) => {
      const key = item?.label || item?.range || item?.scoreRange || item?.scoreSegment
      if (!key) {
        return accumulator
      }
      accumulator[key] = Number(item?.value ?? item?.count ?? item?.total ?? 0)
      return accumulator
    }, {})
  }

  return props.distribution || {}
})

const normalizedItems = computed(() => {
  const items = SCORE_DISTRIBUTION_LABELS.map((label) => ({
    label,
    value: Number(distributionMap.value[label] || 0)
  }))
  const maxValue = Math.max(...items.map((item) => item.value), 1)

  return items.map((item) => ({
    ...item,
    ratio: item.value / maxValue
  }))
})

const total = computed(() =>
  normalizedItems.value.reduce((sum, item) => sum + item.value, 0)
)

const hasData = computed(() => total.value > 0)
</script>

<style scoped>
.score-chart {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 22px;
  border-radius: var(--radius-md);
  background: linear-gradient(180deg, rgba(232, 243, 237, 0.8), rgba(255, 255, 255, 0.92));
  border: 1px solid var(--border-soft);
}

.score-chart__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.score-chart__title {
  margin: 0;
  font-size: 20px;
}

.score-chart__description {
  margin: 8px 0 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.score-chart__total {
  padding: 10px 14px;
  border-radius: 999px;
  color: var(--brand-primary-deep);
  background: rgba(79, 175, 143, 0.14);
  white-space: nowrap;
}

.score-chart__body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.score-chart__row {
  display: grid;
  grid-template-columns: 70px minmax(0, 1fr) 40px;
  align-items: center;
  gap: 12px;
}

.score-chart__label {
  color: var(--text-secondary);
}

.score-chart__track {
  position: relative;
  height: 12px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(47, 79, 70, 0.1);
}

.score-chart__fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--brand-primary), #ffc857);
}

.score-chart__value {
  text-align: right;
  color: var(--text-main);
}

@media (max-width: 768px) {
  .score-chart {
    padding: 18px;
  }

  .score-chart__header {
    flex-direction: column;
  }
}
</style>
