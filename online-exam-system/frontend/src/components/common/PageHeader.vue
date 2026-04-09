<template>
  <div class="page-header">
    <div>
      <h1 class="page-header__title">{{ title }}</h1>
      <p v-if="description" class="page-header__description">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="page-header__actions">
      <slot name="actions" />
    </div>
  </div>
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
  }
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 22px;
}

.page-header__title {
  margin: 0;
  font-size: 28px;
}

.page-header__description {
  margin: 8px 0 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
  }

  .page-header__title {
    font-size: 24px;
  }
}
</style>
