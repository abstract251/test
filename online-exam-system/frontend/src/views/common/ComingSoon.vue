<template>
  <PageContainer>
    <PageHeader :title="title" :description="description" />
    <EmptyState :description="message" emoji="🚧" />
  </PageContainer>
</template>

<script setup>
import EmptyState from '@/components/common/EmptyState.vue'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'

defineProps({
  title: {
    type: String,
    default: '页面准备中'
  },
  description: {
    type: String,
    default: '该功能暂不可用。'
  },
  message: {
    type: String,
    default: '请先使用其他已开放功能。'
  }
})
</script>
