<template>
  <div class="not-found page-shell">
    <PageContainer>
      <EmptyState description="你访问的页面不存在或已被移动。" emoji="🧭">
        <el-button type="primary" @click="router.push('/login')">回到登录页</el-button>
      </EmptyState>
    </PageContainer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import EmptyState from '@/components/common/EmptyState.vue'
import PageContainer from '@/components/common/PageContainer.vue'

const router = useRouter()
</script>

<style scoped>
.not-found {
  padding: 48px 0;
}
</style>
