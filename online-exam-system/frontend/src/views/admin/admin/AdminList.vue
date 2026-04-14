<template>
  <PageContainer>
    <PageHeader
      title="管理员管理"
      description="统一维护管理员账号与联系方式。"
    />
    <EmptyState description="暂无管理员记录。" />
  </PageContainer>
</template>

<script setup>
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
</script>
