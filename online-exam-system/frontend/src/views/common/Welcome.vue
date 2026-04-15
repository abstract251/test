<template>
  <div class="welcome-view">
    <PageContainer>
      <PageHeader
        title="控制台总览"
        description="请选择左侧菜单进入具体功能页面。"
      />

      <div class="welcome-view__cards">
        <StatusCard label="当前身份" :value="roleText" hint="可使用右上角菜单修改密码或退出登录" />
        <StatusCard label="常用功能" value="3" hint="教师管理、学生管理、考试管理" />
        <StatusCard label="使用提示" value="先选菜单后操作" hint="列表页支持新增、编辑、删除" />
      </div>
    </PageContainer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusCard from '@/components/common/StatusCard.vue'
import { useAuthSession } from '@/composables/useAuthSession'
import { ROLE_LABELS } from '@/utils/constants'

const { session } = useAuthSession()
const roleText = computed(() => ROLE_LABELS[session.value?.role] || '访客')
</script>

<style scoped>
.welcome-view__cards {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}
</style>
