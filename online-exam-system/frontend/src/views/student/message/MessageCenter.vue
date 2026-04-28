<template>
  <PageContainer>
    <PageHeader
      title="消息中心"
      description="提交问题并查看老师或管理员的回复。"
    >
      <template #actions>
        <el-button :loading="loading" @click="refreshMessages">刷新留言</el-button>
      </template>
    </PageHeader>

    <section class="composer-card">
      <div class="composer-card__header">
        <div>
          <p class="composer-card__eyebrow">发起留言</p>
          <h2 class="composer-card__title">提交问题或需要协助的事项</h2>
          <p class="composer-card__description">
            {{ `当前账号：${displayUserName}，提交后可在下方留言列表查看回复进展。` }}
          </p>
        </div>
      </div>

      <el-form class="composer-form" label-position="top" @submit.prevent>
        <el-form-item label="标题">
          <el-input
            v-model="composeForm.title"
            maxlength="60"
            show-word-limit
            placeholder="请输入留言标题"
          />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="composeForm.content"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="请描述需要协助的问题或想反馈的内容"
          />
        </el-form-item>
        <div class="composer-form__actions">
          <el-button @click="resetComposer">清空内容</el-button>
          <el-button type="primary" :loading="submitting" @click="handleCreateMessage">
            提交留言
          </el-button>
        </div>
      </el-form>
    </section>

    <div class="summary-grid">
      <StatusCard label="留言总数" :value="pagination.total" hint="留言列表中的全部记录总量" />
      <StatusCard label="已回复" :value="repliedCount" hint="当前页已收到回复的留言" />
      <StatusCard label="待回复" :value="pendingCount" hint="当前页暂未收到回复的留言" />
      <StatusCard label="最近更新" :value="latestActivityText" hint="当前页最新一条留言或回复时间" />
    </div>

    <el-form class="filter-grid" label-position="top" @submit.prevent>
      <el-form-item label="当前页筛选">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="按标题或内容筛选当前页"
        />
      </el-form-item>
      <el-form-item label="回复状态">
        <el-select v-model="filters.status" placeholder="全部留言">
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <div class="filter-actions">
        <el-button @click="resetQuickFilter">清空筛选</el-button>
      </div>
    </el-form>

    <EmptyState
      v-if="!loading && !pagination.total"
      class="empty-panel"
      description="暂时还没有留言记录，提交后会显示在这里。"
      emoji="📨"
    />

    <EmptyState
      v-else-if="!loading && !displayRows.length"
      class="empty-panel"
      description="当前页没有符合条件的留言，请调整筛选条件后再试。"
      emoji="🔍"
    >
      <el-button @click="resetQuickFilter">清空筛选</el-button>
    </EmptyState>

    <template v-else>
      <div v-if="isMobileViewport" class="mobile-message-list">
        <article
          v-for="row in displayRows"
          :key="resolveMessageId(row)"
          class="mobile-message-card"
        >
          <div class="mobile-message-card__header">
            <div>
              <p class="mobile-message-card__eyebrow">
                {{ resolveMessageId(row) ? `留言 #${resolveMessageId(row)}` : '留言详情' }}
              </p>
              <h3 class="mobile-message-card__title">{{ row.title || '未命名留言' }}</h3>
            </div>
            <el-tag :type="hasReplies(row) ? 'success' : 'warning'">
              {{ hasReplies(row) ? '已回复' : '待回复' }}
            </el-tag>
          </div>

          <p class="mobile-message-card__preview">{{ getMessagePreview(row, 90) }}</p>

          <div class="mobile-message-card__meta">
            <span>{{ `提交于 ${formatDateTime(row.time, 'YYYY-MM-DD')}` }}</span>
            <span>{{ `回复 ${getMessageReplyCount(row)} 条` }}</span>
          </div>

          <el-button type="primary" plain @click="openDetail(row)">
            查看详情
          </el-button>
        </article>
      </div>

      <el-table
        v-else
        v-loading="loading"
        :data="displayRows"
        stripe
        empty-text="暂无留言记录"
      >
        <el-table-column label="留言标题" min-width="320">
          <template #default="{ row }">
            <div class="message-cell">
              <strong>{{ row.title || '未命名留言' }}</strong>
              <span>{{ getMessagePreview(row, 60) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="提交日期" min-width="120">
          <template #default="{ row }">
            {{ formatDateTime(row.time, 'YYYY-MM-DD') }}
          </template>
        </el-table-column>
        <el-table-column label="回复状态" min-width="140">
          <template #default="{ row }">
            <div class="status-cell">
              <el-tag :type="hasReplies(row) ? 'success' : 'warning'">
                {{ hasReplies(row) ? '已回复' : '待回复' }}
              </el-tag>
              <span>{{ `共 ${getMessageReplyCount(row)} 条回复` }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="最近回复" min-width="120">
          <template #default="{ row }">
            {{ formatLatestReplyTime(row) }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="footer">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[6, 10, 20]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </template>

    <el-drawer v-model="detailVisible" :size="drawerSize" :title="drawerTitle">
      <div class="drawer-content">
        <MessageDetailPanel :loading="detailLoading" :message="activeMessage" />
        <ReplyList
          :loading="detailLoading"
          :replies="detailReplies"
          empty-text="这条留言暂时还没有回复，请稍后回来查看。"
        />
      </div>
    </el-drawer>
  </PageContainer>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusCard from '@/components/common/StatusCard.vue'
import MessageDetailPanel from '@/components/message/MessageDetailPanel.vue'
import ReplyList from '@/components/message/ReplyList.vue'
import { useAuthSession } from '@/composables/useAuthSession'
import { usePagination } from '@/composables/usePagination'
import { useViewport } from '@/composables/useViewport'
import {
  createMessage,
  getMessageById,
  getMessagePage,
  getReplyList
} from '@/api/messageApi'
import { formatDateTime } from '@/utils/date'
import {
  buildMessageSearchText,
  getLatestActivityTime,
  getLatestReplyTime,
  getMessagePreview,
  getMessageReplyCount,
  hasReplies,
  normalizeMessage,
  normalizeMessageList,
  normalizeReplyList,
  resolveMessageId
} from '@/utils/message'
import { createRequestCoordinator } from '@/utils/requestCoordinator'

const messageDetailCoordinator = createRequestCoordinator('student-messages:detail', {
  defaultTtlMs: 15000
})

const statusOptions = [
  { label: '全部留言', value: 'all' },
  { label: '待回复', value: 'pending' },
  { label: '已回复', value: 'resolved' }
]

const { session } = useAuthSession()
const { isMobileViewport } = useViewport()
const pagination = usePagination(6)
const loading = ref(false)
const submitting = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)

const composeForm = reactive({
  title: '',
  content: ''
})

const filters = reactive({
  keyword: '',
  status: 'all'
})

const detailMessage = ref(null)
const detailReplies = ref([])

const displayUserName = computed(() => session.value?.userName || '当前用户')

const activeMessage = computed(() => {
  if (!detailMessage.value) {
    return null
  }
  return normalizeMessage({
    ...detailMessage.value,
    replays: detailReplies.value
  })
})

const displayRows = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()

  return pagination.records.filter((row) => {
    const matchKeyword = !keyword || buildMessageSearchText(row).includes(keyword)
    const matchStatus = filters.status === 'all' ||
      (filters.status === 'pending' ? !hasReplies(row) : hasReplies(row))

    return matchKeyword && matchStatus
  })
})

const repliedCount = computed(() => displayRows.value.filter((row) => hasReplies(row)).length)
const pendingCount = computed(() => displayRows.value.filter((row) => !hasReplies(row)).length)

const latestActivityText = computed(() => {
  const latestValue = displayRows.value.reduce((latest, row) => {
    const currentValue = getLatestActivityTime(row)
    return parseTime(currentValue) > parseTime(latest) ? currentValue : latest
  }, '')

  return latestValue ? formatDateTime(latestValue, 'YYYY-MM-DD') : '--'
})

const drawerTitle = computed(() => activeMessage.value?.title || '留言详情')
const drawerSize = computed(() => (isMobileViewport.value ? '100%' : '55%'))

function parseTime(value) {
  if (!value) {
    return 0
  }

  const timestamp = new Date(value).getTime()
  return Number.isFinite(timestamp) ? timestamp : 0
}

function formatLatestReplyTime(row) {
  const latestReplyTime = getLatestReplyTime(row)
  return latestReplyTime ? formatDateTime(latestReplyTime, 'YYYY-MM-DD') : '暂无回复'
}

function extractErrorMessage(error, fallback) {
  return error?.response?.data?.message || fallback
}

function applyPageData(data = {}) {
  Object.assign(pagination, data, {
    records: normalizeMessageList(data.records)
  })
}

function syncMessageRow(message) {
  const messageId = resolveMessageId(message)
  if (!messageId) {
    return
  }

  pagination.records = pagination.records.map((row) => {
    return resolveMessageId(row) === messageId
      ? normalizeMessage({
          ...row,
          ...message,
          replays: message.replays
        })
      : row
  })
}

async function fetchMessages() {
  loading.value = true
  try {
    const response = await getMessagePage(pagination.current, pagination.size)
    if (response.code === 200 && response.data) {
      applyPageData(response.data)
      return
    }

    ElMessage.error(response.message || '加载留言列表失败')
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '加载留言列表失败，请稍后重试'))
  } finally {
    loading.value = false
  }
}

async function loadMessageDetail(messageId, fallbackRow = null) {
  detailLoading.value = true

  if (fallbackRow) {
    const normalizedFallback = normalizeMessage(fallbackRow)
    detailMessage.value = normalizedFallback
    detailReplies.value = normalizedFallback.replays
  }

  try {
    const detailKey = `messages:detail:${messageId}`
    const repliesKey = `messages:replies:${messageId}`
    const [messageResult, replyResult] = await Promise.allSettled([
      messageDetailCoordinator.load(
        detailKey,
        () => getMessageById(messageId)
      ),
      messageDetailCoordinator.load(
        repliesKey,
        () => getReplyList(messageId)
      )
    ])

    let nextMessage = fallbackRow ? normalizeMessage(fallbackRow) : null
    let nextReplies = nextMessage?.replays || []
    let loaded = false

    if (messageResult.status === 'fulfilled' && messageResult.value.code === 200 && messageResult.value.data) {
      nextMessage = normalizeMessage(messageResult.value.data)
      nextReplies = nextMessage.replays
      loaded = true
    }

    if (replyResult.status === 'fulfilled' && replyResult.value.code === 200) {
      nextReplies = normalizeReplyList(replyResult.value.data)
      loaded = true
    }

    if (nextMessage) {
      detailMessage.value = normalizeMessage({
        ...nextMessage,
        replays: nextReplies
      })
      detailReplies.value = nextReplies
      syncMessageRow(detailMessage.value)
    }

    if (!loaded) {
      ElMessage.error('加载留言详情失败，请稍后重试')
    }
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '加载留言详情失败，请稍后重试'))
  } finally {
    detailLoading.value = false
  }
}

function openDetail(row) {
  const messageId = resolveMessageId(row)
  if (!messageId) {
    ElMessage.warning('未找到这条留言的编号')
    return
  }

  detailVisible.value = true
  void loadMessageDetail(messageId, row)
}

async function handleCreateMessage() {
  const title = composeForm.title.trim()
  const content = composeForm.content.trim()

  if (!title || !content) {
    ElMessage.warning('请先填写留言标题和内容')
    return
  }

  submitting.value = true
  try {
    const response = await createMessage({
      title,
      content,
      time: new Date()
    })

    if (response.code === 200) {
      ElMessage.success('留言已提交')
      composeForm.title = ''
      composeForm.content = ''
      pagination.current = 1
      await fetchMessages()

      const targetMessage = pagination.records.find((row) => row.title === title && row.content === content) ||
        pagination.records[0]
      if (targetMessage) {
        openDetail(targetMessage)
      }
      return
    }

    ElMessage.error(response.message || '提交留言失败')
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '提交留言失败，请稍后重试'))
  } finally {
    submitting.value = false
  }
}

function resetComposer() {
  composeForm.title = ''
  composeForm.content = ''
}

function resetQuickFilter() {
  filters.keyword = ''
  filters.status = 'all'
}

function handleCurrentChange(page) {
  pagination.current = page
  void fetchMessages()
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.current = 1
  void fetchMessages()
}

function refreshMessages() {
  void fetchMessages()
}

onMounted(fetchMessages)
</script>

<style scoped>
.composer-card {
  padding: 22px;
  margin-bottom: 20px;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, rgba(79, 175, 143, 0.14), rgba(255, 200, 87, 0.16));
}

.composer-card__eyebrow {
  margin: 0 0 8px;
  color: var(--text-secondary);
}

.composer-card__title {
  margin: 0;
  font-size: 24px;
}

.composer-card__description {
  margin: 10px 0 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.composer-form {
  margin-top: 18px;
}

.composer-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.summary-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  margin-bottom: 20px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 0 16px;
}

.filter-actions {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding-bottom: 22px;
}

.empty-panel {
  margin-top: 20px;
}

.message-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.message-cell strong {
  font-size: 14px;
  line-height: 1.6;
}

.message-cell span,
.status-cell span {
  color: var(--text-secondary);
  font-size: 12px;
}

.status-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.drawer-content {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.mobile-message-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.mobile-message-card {
  padding: 18px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: var(--shadow-card);
}

.mobile-message-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 14px;
}

.mobile-message-card__eyebrow {
  margin: 0 0 8px;
  color: var(--text-secondary);
  font-size: 12px;
}

.mobile-message-card__title {
  margin: 0;
  font-size: 18px;
  line-height: 1.5;
}

.mobile-message-card__preview {
  margin: 14px 0;
  color: var(--text-secondary);
  line-height: 1.8;
}

.mobile-message-card__meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 14px;
  color: var(--text-secondary);
  font-size: 12px;
}

@media (max-width: 768px) {
  .composer-card {
    padding: 20px;
  }

  .composer-card__title {
    font-size: 22px;
  }

  .composer-form__actions,
  .filter-actions {
    flex-wrap: wrap;
  }

  .filter-actions {
    padding-bottom: 0;
  }

  .footer {
    justify-content: center;
  }
}
</style>
