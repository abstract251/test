<template>
  <section class="reply-list">
    <div class="reply-list__header">
      <div>
        <h3 class="reply-list__title">回复记录</h3>
        <p class="reply-list__description">查看这条留言的处理进展和回复内容。</p>
      </div>
      <el-tag type="success">{{ `${normalizedReplies.length} 条` }}</el-tag>
    </div>

    <div v-loading="loading" class="reply-list__body">
      <EmptyState
        v-if="!loading && !normalizedReplies.length"
        :description="emptyText"
        emoji="💬"
      />

      <div v-else class="reply-list__items">
        <article
          v-for="(reply, index) in normalizedReplies"
          :key="resolveReplyKey(reply, index)"
          class="reply-item"
        >
          <div class="reply-item__meta">
            <strong>{{ `回复 ${index + 1}` }}</strong>
            <span>{{ formatDateTime(reply.replayTime, 'YYYY-MM-DD') }}</span>
          </div>
          <p class="reply-item__text">{{ reply.replay || '--' }}</p>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { formatDateTime } from '@/utils/date'
import { normalizeReplyList } from '@/utils/message'

const props = defineProps({
  loading: {
    type: Boolean,
    default: false
  },
  replies: {
    type: Array,
    default: () => []
  },
  emptyText: {
    type: String,
    default: '暂时还没有回复记录。'
  }
})

const normalizedReplies = computed(() => normalizeReplyList(props.replies))

function resolveReplyKey(reply, index) {
  return reply.replayId || `${reply.messageId || 'message'}_${reply.replayTime || index}`
}
</script>

<style scoped>
.reply-list {
  padding: 20px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-card);
}

.reply-list__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.reply-list__title {
  margin: 0;
  font-size: 18px;
}

.reply-list__description {
  margin: 8px 0 0;
  color: var(--text-secondary);
}

.reply-list__body {
  min-height: 120px;
}

.reply-list__items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reply-item {
  padding: 16px 18px;
  border-radius: var(--radius-md);
  background: var(--bg-soft);
}

.reply-item__meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.reply-item__meta span {
  color: var(--text-secondary);
  font-size: 12px;
}

.reply-item__text {
  margin: 0;
  line-height: 1.8;
  white-space: pre-wrap;
}

@media (max-width: 768px) {
  .reply-list {
    padding: 18px;
  }

  .reply-list__header,
  .reply-item__meta {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
