<template>
  <PageContainer>
    <PageHeader
      title="消息管理"
      description="查看留言、跟进回复并处理问题。"
    />
    <EmptyState description="暂无消息记录。" />
  </PageContainer>
</template>

<script setup>
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
</script>
