<template>
  <PageContainer>
    <PageHeader
      title="成绩管理"
      description="按考试查看成绩概况、分布和成绩明细。"
    />

    <div class="summary-grid">
      <StatusCard label="参考人数" value="--" hint="将在成绩载入后显示" />
      <StatusCard label="平均分" value="--" hint="用于快速了解整体表现" />
      <StatusCard label="最高分" value="--" hint="展示本场考试最高成绩" />
      <StatusCard label="及格率" value="--" hint="展示达到及格线的比例" />
    </div>

    <ScoreDistributionChart :distribution="{}" />

    <el-table :data="[]" stripe empty-text="暂无成绩记录">
      <el-table-column prop="studentId" label="学号" min-width="120" />
      <el-table-column prop="studentName" label="姓名" min-width="100" />
      <el-table-column prop="examCode" label="考试编号" min-width="120" />
      <el-table-column prop="score" label="成绩" min-width="90" />
    </el-table>
  </PageContainer>
</template>

<script setup>
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusCard from '@/components/common/StatusCard.vue'
import ScoreDistributionChart from '@/components/charts/ScoreDistributionChart.vue'
</script>

<style scoped>
.summary-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  margin-bottom: 20px;
}
</style>
