<template>
  <section class="paper-question-group">
    <header class="paper-question-group__header">
      <div>
        <h3>{{ title }}</h3>
        <p>{{ description }}</p>
      </div>
      <span>{{ questions.length }} 题</span>
    </header>

    <div v-if="questions.length" class="paper-question-group__list">
      <article
        v-for="item in questions"
        :key="item.questionId"
        class="paper-question-group__item"
      >
        <div class="paper-question-group__content">
          <strong>{{ item.question }}</strong>
          <p v-if="item.analysis">解析：{{ item.analysis }}</p>
          <p class="paper-question-group__meta">
            <span>{{ item.section || '未分节' }}</span>
            <span>{{ item.level || '未标注难度' }}</span>
          </p>
        </div>
        <el-button
          v-if="showAction"
          size="small"
          :loading="actionLoading"
          :disabled="actionDisabled || disabledIds.includes(item.questionId)"
          @click="$emit('action', { type, question: item })"
        >
          {{ actionText }}
        </el-button>
      </article>
    </div>

    <EmptyState v-else :description="emptyText" emoji="📘" />
  </section>
</template>

<script setup>
import EmptyState from '@/components/common/EmptyState.vue'

defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  },
  questions: {
    type: Array,
    default: () => []
  },
  type: {
    type: Number,
    required: true
  },
  showAction: {
    type: Boolean,
    default: false
  },
  actionText: {
    type: String,
    default: '操作'
  },
  actionLoading: {
    type: Boolean,
    default: false
  },
  actionDisabled: {
    type: Boolean,
    default: false
  },
  disabledIds: {
    type: Array,
    default: () => []
  },
  emptyText: {
    type: String,
    default: '暂无题目'
  }
})

defineEmits(['action'])
</script>

<style scoped>
.paper-question-group {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.paper-question-group__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.paper-question-group__header h3 {
  margin: 0;
}

.paper-question-group__header p {
  margin: 8px 0 0;
  color: var(--text-secondary);
}

.paper-question-group__list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.paper-question-group__item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  border-radius: var(--radius-sm);
  background: var(--bg-soft);
}

.paper-question-group__content {
  min-width: 0;
}

.paper-question-group__content strong,
.paper-question-group__content p {
  display: block;
  line-height: 1.7;
}

.paper-question-group__content p {
  margin: 8px 0 0;
}

.paper-question-group__meta {
  color: var(--text-muted);
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .paper-question-group__item {
    flex-direction: column;
  }
}
</style>
