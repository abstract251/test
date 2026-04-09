<template>
  <section class="paper-builder-panel">
    <header class="paper-builder-panel__header">
      <div>
        <h2>{{ title }}</h2>
        <p>{{ description }}</p>
      </div>
      <span>{{ count }} 题</span>
    </header>
    <slot />
  </section>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  },
  count: {
    type: Number,
    default: 0
  }
})
</script>

<style scoped>
.paper-builder-panel {
  padding: 20px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-card);
}

.paper-builder-panel__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 18px;
}

.paper-builder-panel__header h2 {
  margin: 0;
  font-size: 24px;
}

.paper-builder-panel__header p {
  margin: 8px 0 0;
  color: var(--text-secondary);
  line-height: 1.7;
}
</style>
