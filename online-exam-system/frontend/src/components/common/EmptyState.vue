<template>
  <div class="empty-state">
    <el-empty :description="description">
      <template #image>
        <div class="empty-state__image">{{ emoji }}</div>
      </template>
      <slot />
    </el-empty>
  </div>
</template>

<script setup>
defineProps({
  description: {
    type: String,
    default: '暂无内容'
  },
  emoji: {
    type: String,
    default: '🌿'
  }
})
</script>

<style scoped>
.empty-state {
  border-radius: var(--radius-md);
  background: var(--bg-soft);
}

.empty-state__image {
  display: grid;
  place-items: center;
  width: 92px;
  height: 92px;
  margin: 0 auto;
  border-radius: 30px;
  font-size: 38px;
  background: white;
  box-shadow: var(--shadow-card);
}
</style>
