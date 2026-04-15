<template>
  <section class="message-detail">
    <EmptyState
      v-if="!message && !loading"
      description="请选择一条留言查看详情。"
      emoji="🗂️"
    />

    <div v-else v-loading="loading" class="message-detail__body">
      <div class="message-detail__header">
        <div class="message-detail__heading">
          <p class="message-detail__eyebrow">
            {{ messageId ? `留言 #${messageId}` : '留言详情' }}
          </p>
          <h2 class="message-detail__title">
            {{ message?.title || '未命名留言' }}
          </h2>
          <p class="message-detail__meta">
            {{ formatDateTime(message?.time, 'YYYY-MM-DD') }} 提交
          </p>
        </div>

        <div class="message-detail__tags">
          <el-tag :type="replyCount ? 'success' : 'warning'">
            {{ replyCount ? '已回复' : '待回复' }}
          </el-tag>
          <el-tag type="info">
            {{ `共 ${replyCount} 条回复` }}
          </el-tag>
        </div>
      </div>

      <div class="message-detail__content">
        <span class="message-detail__label">留言内容</span>
        <p class="message-detail__text">
          {{ message?.content || '暂无留言内容。' }}
        </p>
      </div>

      <div class="message-detail__stats">
        <article class="message-detail__stat">
          <span>提交日期</span>
          <strong>{{ formatDateTime(message?.time, 'YYYY-MM-DD') }}</strong>
        </article>
        <article class="message-detail__stat">
          <span>最新回复</span>
          <strong>{{ latestReplyText }}</strong>
        </article>
        <article class="message-detail__stat">
          <span>处理进度</span>
          <strong>{{ replyCount ? '处理中' : '等待回复' }}</strong>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { formatDateTime } from '@/utils/date'
import {
  getLatestReplyTime,
  getMessageReplyCount,
  resolveMessageId
} from '@/utils/message'

const props = defineProps({
  loading: {
    type: Boolean,
    default: false
  },
  message: {
    type: Object,
    default: null
  }
})

const messageId = computed(() => resolveMessageId(props.message))
const replyCount = computed(() => getMessageReplyCount(props.message))
const latestReplyText = computed(() => {
  const latestReplyTime = getLatestReplyTime(props.message)
  return latestReplyTime ? formatDateTime(latestReplyTime, 'YYYY-MM-DD') : '暂无回复'
})
</script>

<style scoped>
.message-detail__body {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.message-detail__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.message-detail__eyebrow {
  margin: 0 0 8px;
  color: var(--text-secondary);
}

.message-detail__title {
  margin: 0;
  font-size: 24px;
  line-height: 1.4;
}

.message-detail__meta {
  margin: 10px 0 0;
  color: var(--text-secondary);
}

.message-detail__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.message-detail__content {
  padding: 18px 20px;
  border-radius: var(--radius-md);
  background: var(--bg-soft);
}

.message-detail__label {
  display: inline-block;
  margin-bottom: 10px;
  color: var(--text-secondary);
  font-size: 13px;
}

.message-detail__text {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.8;
}

.message-detail__stats {
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
}

.message-detail__stat {
  padding: 16px 18px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: var(--shadow-card);
}

.message-detail__stat span {
  display: block;
  margin-bottom: 10px;
  color: var(--text-secondary);
  font-size: 12px;
}

.message-detail__stat strong {
  font-size: 16px;
}

@media (max-width: 768px) {
  .message-detail__header {
    flex-direction: column;
  }

  .message-detail__title {
    font-size: 21px;
  }
}
</style>
