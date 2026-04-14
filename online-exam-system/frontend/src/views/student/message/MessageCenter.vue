<template>
  <PageContainer>
    <PageHeader
      title="消息中心"
      description="提交问题并查看老师或管理员的回复。"
    />
    <EmptyState description="暂无消息记录。" />
  </PageContainer>
</template>

<script setup>
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
</script>
