<template>
  <section class="page-container">
    <slot />
  </section>
</template>

<style scoped>
.page-container {
  padding: 24px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.88);
  box-shadow: var(--shadow-card);
}

@media (max-width: 768px) {
  .page-container {
    padding: 18px;
    border-radius: var(--radius-md);
  }
}
</style>
