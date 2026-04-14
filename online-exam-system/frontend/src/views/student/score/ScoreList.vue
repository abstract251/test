<template>
  <PageContainer>
    <PageHeader
      title="我的成绩"
      description="查看历次考试成绩和得分情况。"
    />
    <EmptyState description="交卷后可在这里查看成绩。" />
  </PageContainer>
</template>

<script setup>
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
</script>
