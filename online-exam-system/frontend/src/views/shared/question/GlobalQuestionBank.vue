<template>
  <PageContainer>
    <PageHeader
      title="全局题库管理"
      description="按科目、题型和关键词统一查看题库内容。"
    />
    <EmptyState description="暂无题库记录。" />
  </PageContainer>
</template>

<script setup>
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
</script>
