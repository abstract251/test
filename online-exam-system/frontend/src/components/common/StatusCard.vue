<template>
  <article class="status-card">
    <span class="status-card__label">{{ label }}</span>
    <strong class="status-card__value">{{ value }}</strong>
    <p v-if="hint" class="status-card__hint">{{ hint }}</p>
  </article>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    required: true
  },
  hint: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.status-card {
  padding: 18px 20px;
  border-radius: var(--radius-md);
  background: linear-gradient(160deg, rgba(79, 175, 143, 0.12), rgba(255, 200, 87, 0.12));
}

.status-card__label {
  color: var(--text-muted);
  font-size: 13px;
}

.status-card__value {
  display: block;
  margin-top: 10px;
  font-size: 28px;
}

.status-card__hint {
  margin: 10px 0 0;
  color: var(--text-secondary);
}
</style>
