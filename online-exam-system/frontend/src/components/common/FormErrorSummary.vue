<template>
  <div v-if="title || items.length" class="form-error-summary">
    <div class="form-error-summary__head">
      <el-icon><WarningFilled /></el-icon>
      <strong>{{ title || '请先处理以下内容后再继续' }}</strong>
    </div>
    <ul v-if="items.length" class="form-error-summary__list">
      <li v-for="item in items" :key="item">{{ item }}</li>
    </ul>
  </div>
</template>

<script setup>
import { WarningFilled } from '@element-plus/icons-vue'

defineProps({
  title: {
    type: String,
    default: ''
  },
  items: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.form-error-summary {
  margin-bottom: 18px;
  padding: 14px 16px;
  border-radius: var(--radius-md);
  border: 1px solid var(--error-border);
  background: var(--error-bg);
  color: var(--error-text);
}

.form-error-summary__head {
  display: flex;
  gap: 8px;
  align-items: center;
}

.form-error-summary__list {
  margin: 10px 0 0;
  padding-left: 22px;
}

.form-error-summary__list li + li {
  margin-top: 6px;
}
</style>
