<template>
  <PageContainer>
    <PageHeader
      title="管理员管理"
      description="统一维护管理员账号与联系方式。"
    >
      <template #actions>
        <el-space wrap>
          <el-button :loading="loadingList" @click="refreshAdmins">刷新列表</el-button>
          <el-button type="primary" @click="router.push(createAdminPath)">
            新增管理员
          </el-button>
        </el-space>
      </template>
    </PageHeader>

    <el-alert
      v-if="listMissingIds"
      class="notice"
      type="warning"
      show-icon
      title="如需编辑或删除指定管理员，请先输入管理员编号进行定位。"
      description="当前列表会展示管理员信息，但部分记录未直接返回编号；定位后可进入完整维护。"
    />

    <el-form class="filter-grid" label-position="top" @submit.prevent>
      <el-form-item label="管理员编号定位">
        <el-input
          v-model="locatorId"
          clearable
          placeholder="请输入管理员编号"
          @keyup.enter="locateAdmin"
        />
      </el-form-item>
      <el-form-item label="快速筛选">
        <el-input
          v-model="keyword"
          clearable
          placeholder="按姓名、手机号、邮箱筛选当前列表"
        />
      </el-form-item>
      <div class="filter-actions">
        <el-button @click="clearLocate">清空定位</el-button>
        <el-button type="primary" :loading="locating" @click="locateAdmin">
          定位管理员
        </el-button>
      </div>
    </el-form>

    <div class="summary-grid">
      <StatusCard label="管理员数量" :value="adminRows.length" hint="当前列表中的管理员记录" />
      <StatusCard label="可直接维护" :value="manageableCount" hint="已定位出管理员编号的记录数" />
      <StatusCard
        label="当前账号"
        :value="currentAdminLabel"
        :hint="currentAdminName ? `${currentAdminName} 正在使用系统` : '未读取到当前账号信息'"
      />
      <StatusCard
        label="定位结果"
        :value="locatedAdminLabel"
        :hint="locatedAdmin ? '已支持编辑和删除操作' : '输入管理员编号后可快速定位'"
      />
    </div>

    <EmptyState
      v-if="!loadingList && !adminRows.length"
      class="empty-panel"
      description="暂无管理员记录。"
      emoji="🧾"
    >
      <el-button type="primary" @click="router.push(createAdminPath)">新增管理员</el-button>
    </EmptyState>

    <EmptyState
      v-else-if="!loadingList && !displayRows.length"
      class="empty-panel"
      description="没有找到符合条件的管理员，请调整筛选条件后重试。"
      emoji="🔎"
    >
      <el-button @click="keyword = ''">清空筛选</el-button>
    </EmptyState>

    <template v-else>
      <el-table
        v-loading="loadingList || locating"
        :data="displayRows"
        stripe
        empty-text="暂无管理员记录"
        :row-class-name="resolveRowClass"
      >
        <el-table-column label="管理员编号" min-width="120">
          <template #default="{ row }">
            {{ formatAdminId(row) }}
          </template>
        </el-table-column>
        <el-table-column label="姓名" min-width="180">
          <template #default="{ row }">
            <div class="admin-cell">
              <strong>{{ row.adminName || '--' }}</strong>
              <div class="admin-cell__tags">
                <el-tag v-if="isCurrentAdmin(row)" size="small" type="success">当前账号</el-tag>
                <el-tag v-if="row._located" size="small">已定位</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="性别" min-width="90">
          <template #default="{ row }">
            {{ normalizeSex(row.sex, '-') }}
          </template>
        </el-table-column>
        <el-table-column prop="tel" label="手机号" min-width="150" />
        <el-table-column prop="email" label="邮箱" min-width="220" show-overflow-tooltip />
        <el-table-column label="身份证号" min-width="180">
          <template #default="{ row }">
            {{ maskCardId(row.cardId) }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="{ row }">
            <el-space wrap>
              <el-tooltip
                v-if="!canEditRow(row)"
                content="请先输入管理员编号定位该账号后再编辑。"
                placement="top"
              >
                <span>
                  <el-button link type="primary" disabled>编辑</el-button>
                </span>
              </el-tooltip>
              <el-button
                v-else
                link
                type="primary"
                @click="openAdminEdit(resolveAdminId(row))"
              >
                编辑
              </el-button>

              <el-tooltip
                v-if="deleteDisabledReason(row)"
                :content="deleteDisabledReason(row)"
                placement="top"
              >
                <span>
                  <el-button link type="danger" disabled>删除</el-button>
                </span>
              </el-tooltip>
              <el-button
                v-else
                link
                type="danger"
                :loading="deletingAdminId === resolveAdminId(row)"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </template>
  </PageContainer>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusCard from '@/components/common/StatusCard.vue'
import { useAuthSession } from '@/composables/useAuthSession'
import { deleteAdmin, getAdminById, getAdmins } from '@/api/adminApi'
import { getSession } from '@/utils/auth'
import { buildConsolePath, normalizeSex } from '@/utils/constants'

const router = useRouter()
const { session } = useAuthSession()
const consoleRole = getSession()?.authRole
const createAdminPath = buildConsolePath(consoleRole, 'admins/new')

const loadingList = ref(false)
const locating = ref(false)
const deletingAdminId = ref(null)
const keyword = ref('')
const locatorId = ref('')
const adminRows = ref([])
const locatedAdmin = ref(null)

const currentAdminId = computed(() => {
  const parsed = Number(session.value?.userId)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null
})

const currentAdminName = computed(() => session.value?.userName || '')

const currentAdminDetail = computed(() => {
  if (!session.value?.rawUser) {
    return null
  }

  return {
    ...session.value.rawUser,
    adminId: currentAdminId.value
  }
})

const listMissingIds = computed(() =>
  adminRows.value.some((row) => resolveAdminId(row) == null)
)

const currentAdminLabel = computed(() =>
  currentAdminId.value ? `#${currentAdminId.value}` : '--'
)

const locatedAdminLabel = computed(() => {
  if (!locatedAdmin.value) {
    return '--'
  }
  return `#${locatedAdmin.value.adminId}`
})

const enrichedRows = computed(() => {
  const rows = adminRows.value.map((row) => enrichAdminRow(row))

  if (locatedAdmin.value && !rows.some((row) => row._located)) {
    rows.unshift({
      ...locatedAdmin.value,
      _located: true
    })
  }

  return rows
})

const displayRows = computed(() => {
  const filterValue = keyword.value.trim().toLowerCase()
  const rows = [...enrichedRows.value]
    .sort((left, right) => {
      return (
        Number(Boolean(right._located)) - Number(Boolean(left._located)) ||
        Number(Boolean(isCurrentAdmin(right))) - Number(Boolean(isCurrentAdmin(left))) ||
        String(left.adminName || '').localeCompare(String(right.adminName || '')) ||
        String(left.email || '').localeCompare(String(right.email || ''))
      )
    })

  if (!filterValue) {
    return rows
  }

  return rows.filter((row) =>
    [
      row.adminName,
      row.tel,
      row.email,
      formatAdminId(row)
    ]
      .filter(Boolean)
      .some((field) => String(field).toLowerCase().includes(filterValue))
  )
})

const manageableCount = computed(() =>
  enrichedRows.value.filter((row) => canEditRow(row)).length
)

function normalizeText(value) {
  return String(value || '').trim()
}

function resolveAdminId(row) {
  const parsed = Number(row?.adminId)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null
}

function isSameAdmin(left, right) {
  if (!left || !right) {
    return false
  }

  const leftId = resolveAdminId(left)
  const rightId = resolveAdminId(right)
  if (leftId && rightId) {
    return leftId === rightId
  }

  if (normalizeText(left.cardId) && normalizeText(right.cardId)) {
    return normalizeText(left.cardId) === normalizeText(right.cardId)
  }

  if (normalizeText(left.email) && normalizeText(right.email) && normalizeText(left.tel) && normalizeText(right.tel)) {
    return normalizeText(left.email) === normalizeText(right.email) &&
      normalizeText(left.tel) === normalizeText(right.tel)
  }

  return Boolean(normalizeText(left.adminName)) &&
    Boolean(normalizeText(right.adminName)) &&
    Boolean(normalizeText(left.tel)) &&
    Boolean(normalizeText(right.tel)) &&
    normalizeText(left.adminName) === normalizeText(right.adminName) &&
    normalizeText(left.tel) === normalizeText(right.tel)
}

function enrichAdminRow(row) {
  const nextRow = { ...row }

  if (currentAdminDetail.value && isSameAdmin(nextRow, currentAdminDetail.value)) {
    Object.assign(nextRow, {
      ...currentAdminDetail.value,
      _self: true
    })
  }

  if (locatedAdmin.value && isSameAdmin(nextRow, locatedAdmin.value)) {
    Object.assign(nextRow, {
      ...locatedAdmin.value,
      _located: true
    })
  }

  if (currentAdminId.value && resolveAdminId(nextRow) === currentAdminId.value) {
    nextRow._self = true
  }

  return nextRow
}

function formatAdminId(row) {
  const adminId = resolveAdminId(row)
  return adminId ? String(adminId) : '请先定位'
}

function maskCardId(value) {
  const cardId = normalizeText(value)
  if (!cardId) {
    return '--'
  }
  if (cardId.length <= 8) {
    return cardId
  }
  return `${cardId.slice(0, 4)}********${cardId.slice(-4)}`
}

function isCurrentAdmin(row) {
  return Boolean(row?._self) || resolveAdminId(row) === currentAdminId.value
}

function canEditRow(row) {
  return resolveAdminId(row) != null
}

function deleteDisabledReason(row) {
  if (!canEditRow(row)) {
    return '请先输入管理员编号定位该账号后再删除。'
  }

  if (isCurrentAdmin(row)) {
    return '当前登录账号暂不支持在此页删除。'
  }

  return ''
}

function resolveRowClass({ row }) {
  if (row._located) {
    return 'admin-row--located'
  }
  if (isCurrentAdmin(row)) {
    return 'admin-row--self'
  }
  return ''
}

function openAdminEdit(adminId) {
  router.push(buildConsolePath(consoleRole, `admins/${adminId}/edit`))
}

async function fetchAdmins() {
  loadingList.value = true
  try {
    const response = await getAdmins()
    if (response.code === 200) {
      adminRows.value = Array.isArray(response.data) ? response.data : []
      return
    }
    ElMessage.error(response.message || '加载管理员列表失败')
  } catch (error) {
    ElMessage.error('加载管理员列表失败，请稍后重试')
  } finally {
    loadingList.value = false
  }
}

async function locateAdmin() {
  const adminId = Number(locatorId.value)
  if (!Number.isInteger(adminId) || adminId <= 0) {
    ElMessage.warning('请输入有效的管理员编号')
    return
  }

  locating.value = true
  try {
    const response = await getAdminById(adminId)
    if (response.code === 200 && response.data) {
      locatedAdmin.value = response.data
      ElMessage.success('已定位到管理员账号')
      return
    }

    locatedAdmin.value = null
    ElMessage.error(response.message || '未找到对应管理员')
  } catch (error) {
    locatedAdmin.value = null
    ElMessage.error('定位管理员失败，请稍后重试')
  } finally {
    locating.value = false
  }
}

function clearLocate() {
  locatorId.value = ''
  locatedAdmin.value = null
}

function refreshAdmins() {
  void fetchAdmins()
}

async function handleDelete(row) {
  const adminId = resolveAdminId(row)
  if (!adminId) {
    return
  }

  try {
    await ElMessageBox.confirm(
      `删除管理员 ${row.adminName || adminId} 后将无法恢复，是否继续？`,
      '删除管理员',
      {
        type: 'warning'
      }
    )
  } catch (error) {
    return
  }

  deletingAdminId.value = adminId
  try {
    const response = await deleteAdmin(adminId)
    if (response.code === 200) {
      ElMessage.success('管理员已删除')

      if (locatedAdmin.value && resolveAdminId(locatedAdmin.value) === adminId) {
        locatedAdmin.value = null
        locatorId.value = ''
      }

      await fetchAdmins()
      return
    }

    ElMessage.error(response.message || '删除管理员失败')
  } catch (error) {
    const message = error?.response?.data?.message
    ElMessage.error(message || '删除管理员失败，请稍后重试')
  } finally {
    deletingAdminId.value = null
  }
}

onMounted(fetchAdmins)
</script>

<style scoped>
.notice {
  margin-bottom: 18px;
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

.summary-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  margin-bottom: 20px;
}

.empty-panel {
  margin-top: 20px;
}

.admin-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.admin-cell strong {
  font-size: 14px;
}

.admin-cell__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

:deep(.admin-row--located td) {
  background: rgba(255, 248, 223, 0.95) !important;
}

:deep(.admin-row--self td) {
  background: rgba(235, 248, 241, 0.95);
}

@media (max-width: 768px) {
  .filter-actions {
    padding-bottom: 0;
  }
}
</style>
