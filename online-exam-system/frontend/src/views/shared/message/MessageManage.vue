<template>
  <PageContainer>
    <PageHeader
      title="消息管理"
      description="查看留言、跟进回复并处理问题。"
    >
      <template #actions>
        <el-button :loading="loading" @click="refreshMessages">刷新列表</el-button>
      </template>
    </PageHeader>

    <el-form class="filter-grid" label-position="top" @submit.prevent>
      <el-form-item label="快速筛选">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="按标题或内容筛选当前页"
        />
      </el-form-item>
      <el-form-item label="处理状态">
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

    <div class="summary-grid">
      <StatusCard label="留言总数" :value="pagination.total" hint="全部留言记录总量" />
      <StatusCard
        label="当前页展示"
        :value="displayRows.length"
        :hint="activeFilterCount ? '已按当前筛选条件展示' : '当前页留言数量'"
      />
      <StatusCard label="待跟进" :value="pendingCount" hint="当前页尚未回复的留言" />
      <StatusCard label="最近更新" :value="latestActivityText" hint="当前页最新一条留言或回复时间" />
    </div>

    <EmptyState
      v-if="!loading && !pagination.total"
      class="empty-panel"
      description="暂时还没有留言记录。"
      emoji="📮"
    />

    <EmptyState
      v-else-if="!loading && !displayRows.length"
      class="empty-panel"
      description="当前页没有符合条件的留言，请调整筛选条件后再试。"
      emoji="🔎"
    >
      <el-button @click="resetQuickFilter">清空筛选</el-button>
    </EmptyState>

    <template v-else>
      <el-table
        v-loading="loading"
        :data="displayRows"
        stripe
        empty-text="暂无留言记录"
      >
        <el-table-column label="编号" min-width="90">
          <template #default="{ row }">
            {{ resolveMessageId(row) || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="留言标题" min-width="300">
          <template #default="{ row }">
            <div class="message-cell">
              <strong>{{ row.title || '未命名留言' }}</strong>
              <span>{{ getMessagePreview(row, 56) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="提交日期" min-width="120">
          <template #default="{ row }">
            {{ formatDateTime(row.time, 'YYYY-MM-DD') }}
          </template>
        </el-table-column>
        <el-table-column label="处理状态" min-width="140">
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
        <el-table-column fixed="right" label="操作" width="190">
          <template #default="{ row }">
            <el-space wrap>
              <el-button link type="primary" @click="openDetail(row)">
                查看详情
              </el-button>
              <el-button
                link
                type="danger"
                :loading="deletingMessageId === resolveMessageId(row)"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="footer">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </template>

    <el-drawer v-model="detailVisible" size="55%" :title="drawerTitle">
      <div class="drawer-content">
        <MessageDetailPanel :loading="detailLoading" :message="activeMessage" />
        <ReplyList
          :loading="detailLoading"
          :replies="detailReplies"
          empty-text="这条留言还没有收到回复。"
        />

        <section class="reply-editor">
          <div class="reply-editor__header">
            <h3>发送回复</h3>
            <p>填写处理意见后，学生可在消息中心查看最新回复。</p>
          </div>

          <el-input
            v-model="replyForm.content"
            type="textarea"
            :rows="5"
            maxlength="300"
            show-word-limit
            placeholder="请输入回复内容"
          />

          <div class="reply-editor__actions">
            <el-button @click="replyForm.content = ''">清空内容</el-button>
            <el-button
              type="primary"
              :loading="submittingReply"
              @click="handleReplySubmit"
            >
              发送回复
            </el-button>
          </div>
        </section>
      </div>
    </el-drawer>
  </PageContainer>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusCard from '@/components/common/StatusCard.vue'
import MessageDetailPanel from '@/components/message/MessageDetailPanel.vue'
import ReplyList from '@/components/message/ReplyList.vue'
import { usePagination } from '@/composables/usePagination'
import {
  createReply,
  deleteMessage,
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

const statusOptions = [
  { label: '全部留言', value: 'all' },
  { label: '待回复', value: 'pending' },
  { label: '已回复', value: 'resolved' }
]

const pagination = usePagination(10)
const loading = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)
const submittingReply = ref(false)
const deletingMessageId = ref(null)

const filters = reactive({
  keyword: '',
  status: 'all'
})

const replyForm = reactive({
  content: ''
})

const detailMessage = ref(null)
const detailReplies = ref([])

const activeMessage = computed(() => {
  if (!detailMessage.value) {
    return null
  }
  return normalizeMessage({
    ...detailMessage.value,
    replays: detailReplies.value
  })
})

const activeMessageId = computed(() => resolveMessageId(activeMessage.value))

const activeFilterCount = computed(() => {
  return [
    filters.keyword.trim(),
    filters.status !== 'all' ? filters.status : ''
  ].filter(Boolean).length
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

const pendingCount = computed(() => displayRows.value.filter((row) => !hasReplies(row)).length)

const latestActivityText = computed(() => {
  const latestValue = displayRows.value.reduce((latest, row) => {
    const currentValue = getLatestActivityTime(row)
    return parseTime(currentValue) > parseTime(latest) ? currentValue : latest
  }, '')

  return latestValue ? formatDateTime(latestValue, 'YYYY-MM-DD') : '--'
})

const drawerTitle = computed(() => activeMessage.value?.title || '留言详情')

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

function resetQuickFilter() {
  filters.keyword = ''
  filters.status = 'all'
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
    const [messageResult, replyResult] = await Promise.allSettled([
      getMessageById(messageId),
      getReplyList(messageId)
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

  replyForm.content = ''
  detailVisible.value = true
  void loadMessageDetail(messageId, row)
}

async function handleReplySubmit() {
  const messageId = activeMessageId.value
  const content = replyForm.content.trim()

  if (!messageId) {
    ElMessage.warning('请先选择一条留言')
    return
  }

  if (!content) {
    ElMessage.warning('请输入回复内容')
    return
  }

  submittingReply.value = true
  try {
    const response = await createReply({
      messageId,
      replay: content,
      replayTime: new Date()
    })

    if (response.code === 200) {
      replyForm.content = ''
      ElMessage.success('回复已发送')
      await loadMessageDetail(messageId, activeMessage.value)
      await fetchMessages()
      return
    }

    ElMessage.error(response.message || '发送回复失败')
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '发送回复失败，请稍后重试'))
  } finally {
    submittingReply.value = false
  }
}

async function handleDelete(row) {
  const messageId = resolveMessageId(row)
  if (!messageId) {
    ElMessage.warning('未找到这条留言的编号')
    return
  }

  try {
    await ElMessageBox.confirm(
      `删除留言“${row.title || `#${messageId}`}”后将无法恢复，是否继续？`,
      '删除留言',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消'
      }
    )
  } catch (error) {
    return
  }

  deletingMessageId.value = messageId
  try {
    const response = await deleteMessage(messageId)
    if (response.code === 200) {
      ElMessage.success('留言已删除')

      if (activeMessageId.value === messageId) {
        detailVisible.value = false
        detailMessage.value = null
        detailReplies.value = []
        replyForm.content = ''
      }

      if (pagination.records.length === 1 && pagination.current > 1) {
        pagination.current -= 1
      }

      await fetchMessages()
      return
    }

    ElMessage.error(response.message || '删除留言失败')
  } catch (error) {
    ElMessage.error(extractErrorMessage(error, '删除留言失败，请稍后重试'))
  } finally {
    deletingMessageId.value = null
  }
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

.summary-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  margin-bottom: 20px;
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

.reply-editor {
  padding: 20px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-card);
}

.reply-editor__header h3 {
  margin: 0;
  font-size: 18px;
}

.reply-editor__header p {
  margin: 8px 0 16px;
  color: var(--text-secondary);
}

.reply-editor__actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

@media (max-width: 768px) {
  .filter-actions {
    padding-bottom: 0;
  }

  .reply-editor__actions {
    flex-wrap: wrap;
  }
}
</style>
