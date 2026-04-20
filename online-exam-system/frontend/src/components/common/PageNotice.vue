<template>
  <div class="page-notice" :class="`page-notice--${type}`">
    <div class="page-notice__icon">
      <el-icon v-if="type === 'error'"><WarningFilled /></el-icon>
      <el-icon v-else-if="type === 'warning'"><Warning /></el-icon>
      <el-icon v-else-if="type === 'success'"><CircleCheckFilled /></el-icon>
      <el-icon v-else><InfoFilled /></el-icon>
    </div>
    <div class="page-notice__content">
      <h3>{{ title }}</h3>
      <p v-if="description">{{ description }}</p>
      <slot />
    </div>
    <div v-if="$slots.actions" class="page-notice__actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup>
import { CircleCheckFilled, InfoFilled, Warning, WarningFilled } from '@element-plus/icons-vue'

defineProps({
  type: {
    type: String,
    default: 'info'
  },
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.page-notice {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 14px;
  align-items: start;
  padding: 16px 18px;
  border-radius: var(--radius-md);
  border: 1px solid transparent;
  margin-bottom: 18px;
  box-shadow: var(--shadow-card);
}

.page-notice__icon {
  width: 38px;
  height: 38px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-size: 18px;
}

.page-notice__content h3 {
  margin: 0;
  font-size: 16px;
}

.page-notice__content p {
  margin: 6px 0 0;
  color: var(--text-secondary);
  line-height: 1.6;
}

.page-notice--error {
  background: var(--error-bg);
  border-color: var(--error-border);
}

.page-notice--error .page-notice__icon {
  color: var(--error-text);
  background: rgba(217, 106, 95, 0.12);
}

.page-notice--warning {
  background: var(--warning-bg);
  border-color: var(--warning-border);
}

.page-notice--warning .page-notice__icon {
  color: var(--warning-text);
  background: rgba(255, 200, 87, 0.18);
}

.page-notice--success {
  background: var(--success-bg);
  border-color: var(--success-border);
}

.page-notice--success .page-notice__icon {
  color: var(--success-text);
  background: rgba(79, 175, 143, 0.14);
}

.page-notice--info {
  background: var(--info-bg);
  border-color: var(--info-border);
}

.page-notice--info .page-notice__icon {
  color: var(--info-text);
  background: rgba(47, 79, 70, 0.08);
}

@media (max-width: 768px) {
  .page-notice {
    grid-template-columns: auto 1fr;
  }

  .page-notice__actions {
    grid-column: 1 / -1;
  }
}
</style>
