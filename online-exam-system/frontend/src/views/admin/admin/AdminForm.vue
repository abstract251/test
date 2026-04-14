<template>
  <PageContainer>
    <PageHeader
      :title="isEdit ? '编辑管理员' : '新增管理员'"
      description="填写管理员基础信息后保存。"
    />
    <EmptyState description="请先返回管理员列表查看账号信息。" />
  </PageContainer>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const route = useRoute()

const isEdit = computed(() => Boolean(route.params.adminId))
</script>
