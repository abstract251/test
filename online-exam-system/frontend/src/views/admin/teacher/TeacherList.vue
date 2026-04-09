<template>
  <PageContainer>
    <PageHeader
      title="教师管理"
      description="管理员可以在这里维护教师账号，并按教师编号、姓名、学院、职称、手机号和邮箱筛选。"
    >
      <template #actions>
        <el-button type="primary" @click="router.push('/console/admin/teachers/new')">
          新增教师
        </el-button>
      </template>
    </PageHeader>

    <el-form class="filter-grid" label-position="top" @submit.prevent>
      <el-form-item label="教师编号">
        <el-input v-model="filters.teacherId" placeholder="请输入教师编号" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="filters.teacherName" placeholder="请输入教师姓名" />
      </el-form-item>
      <el-form-item label="学院">
        <el-input v-model="filters.institute" placeholder="请输入学院" />
      </el-form-item>
      <el-form-item label="职称">
        <el-input v-model="filters.type" placeholder="请输入职称" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="filters.tel" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="filters.email" placeholder="请输入邮箱" />
      </el-form-item>
      <div class="filter-actions">
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="primary" @click="handleSearch">筛选</el-button>
      </div>
    </el-form>

    <el-table :data="pagination.records" stripe empty-text="暂无教师数据">
      <el-table-column prop="teacherId" label="教师编号" min-width="110" />
      <el-table-column prop="teacherName" label="姓名" min-width="120" />
      <el-table-column prop="institute" label="学院" min-width="150" />
      <el-table-column prop="type" label="职称" min-width="120" />
      <el-table-column prop="sex" label="性别" min-width="90" :formatter="formatSexDisplay" />
      <el-table-column prop="tel" label="手机号" min-width="140" />
      <el-table-column prop="email" label="邮箱" min-width="220" />
      <el-table-column fixed="right" label="操作" width="180">
        <template #default="{ row }">
          <el-space>
            <el-button link type="primary" @click="router.push(`/console/admin/teachers/${row.teacherId}/edit`)">
              编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row.teacherId)">
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
        :page-sizes="[6, 10, 20]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="fetchTeachers"
        @current-change="fetchTeachers"
      />
    </div>
  </PageContainer>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import { usePagination } from '@/composables/usePagination'
import { deleteTeacher, getTeacherPage } from '@/api/teacherApi'
import { normalizeSex } from '@/utils/constants'

const router = useRouter()
const pagination = usePagination(6)
const filters = reactive({
  teacherId: '',
  teacherName: '',
  institute: '',
  type: '',
  tel: '',
  email: ''
})

function formatSexDisplay(row) {
  return normalizeSex(row.sex, '-')
}

async function fetchTeachers() {
  try {
    const response = await getTeacherPage({
      page: pagination.current,
      size: pagination.size,
      filters
    })
    if (response.code === 200) {
      Object.assign(pagination, response.data)
      return
    }
    ElMessage.error(response.message || '加载教师列表失败')
  } catch (error) {
    ElMessage.error('加载教师列表失败，请稍后重试')
  }
}

async function handleDelete(teacherId) {
  try {
    await ElMessageBox.confirm('删除后无法恢复，是否继续？', '删除教师', {
      type: 'warning'
    })
  } catch (error) {
    return
  }

  try {
    const response = await deleteTeacher(teacherId)
    if (response.code === 200) {
      ElMessage.success('教师已删除')
      await fetchTeachers()
      return
    }
    ElMessage.error(response.message || '删除教师失败')
  } catch (error) {
    ElMessage.error('删除教师失败，请稍后重试')
  }
}

function resetFilters() {
  Object.assign(filters, {
    teacherId: '',
    teacherName: '',
    institute: '',
    type: '',
    tel: '',
    email: ''
  })
  pagination.current = 1
  void fetchTeachers()
}

function handleSearch() {
  pagination.current = 1
  void fetchTeachers()
}

onMounted(fetchTeachers)
</script>

<style scoped>
.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 0 16px;
  margin-bottom: 12px;
}

.filter-actions {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding-bottom: 22px;
}

.footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
