<template>
  <PageContainer>
    <PageHeader
      title="学生管理"
      description="支持按学号、姓名、年级、学院、专业、班级和手机号筛选学生。"
    >
      <template #actions>
        <el-button type="primary" @click="router.push(createStudentPath)">
          新增学生
        </el-button>
      </template>
    </PageHeader>

    <el-form class="filter-grid" label-position="top" @submit.prevent>
      <el-form-item label="学号">
        <el-input v-model="filters.studentId" placeholder="请输入学号" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="filters.name" placeholder="请输入学生姓名" />
      </el-form-item>
      <el-form-item label="学院">
        <el-input v-model="filters.institute" placeholder="请输入学院" />
      </el-form-item>
      <el-form-item label="专业">
        <el-input v-model="filters.major" placeholder="请输入专业" />
      </el-form-item>
      <el-form-item label="年级">
        <el-input v-model="filters.grade" placeholder="请输入年级" />
      </el-form-item>
      <el-form-item label="班级">
        <el-input v-model="filters.clazz" placeholder="请输入班级" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="filters.tel" placeholder="请输入手机号" />
      </el-form-item>
      <div class="filter-actions">
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="primary" @click="handleSearch">筛选</el-button>
      </div>
    </el-form>

    <el-table :data="pagination.records" stripe empty-text="暂无学生数据">
      <el-table-column prop="studentId" label="学号" min-width="120" />
      <el-table-column prop="studentName" label="姓名" min-width="100" />
      <el-table-column prop="institute" label="学院" min-width="140" />
      <el-table-column prop="major" label="专业" min-width="160" />
      <el-table-column prop="grade" label="年级" min-width="90" />
      <el-table-column prop="clazz" label="班级" min-width="90" />
      <el-table-column prop="sex" label="性别" min-width="90" :formatter="formatSexDisplay" />
      <el-table-column prop="tel" label="手机号" min-width="140" />
      <el-table-column fixed="right" label="操作" width="180">
        <template #default="{ row }">
          <el-space>
            <el-button link type="primary" @click="openStudentEdit(row.studentId)">
              编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row.studentId)">
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
        @size-change="fetchStudents"
        @current-change="fetchStudents"
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
import { deleteStudent, getStudentPage } from '@/api/studentApi'
import { getSession } from '@/utils/auth'
import { buildConsolePath, normalizeSex } from '@/utils/constants'

const router = useRouter()
const consoleRole = getSession()?.authRole
const pagination = usePagination(6)
const filters = reactive({
  studentId: '',
  name: '',
  grade: '',
  tel: '',
  institute: '',
  major: '',
  clazz: ''
})
const createStudentPath = buildConsolePath(consoleRole, 'students/new')

function formatSexDisplay(row) {
  return normalizeSex(row.sex, '-')
}

function openStudentEdit(studentId) {
  router.push(buildConsolePath(consoleRole, `students/${studentId}/edit`))
}

async function fetchStudents() {
  try {
    const response = await getStudentPage({
      page: pagination.current,
      size: pagination.size,
      filters
    })
    if (response.code === 200) {
      Object.assign(pagination, response.data)
      return
    }
    ElMessage.error(response.message || '加载学生列表失败')
  } catch (error) {
    ElMessage.error('加载学生列表失败，请稍后重试')
  }
}

async function handleDelete(studentId) {
  try {
    await ElMessageBox.confirm('删除后无法恢复，是否继续？', '删除学生', {
      type: 'warning'
    })
  } catch (error) {
    return
  }

  try {
    const response = await deleteStudent(studentId)
    if (response.code === 200) {
      ElMessage.success('学生已删除')
      await fetchStudents()
      return
    }
    ElMessage.error(response.message || '删除学生失败')
  } catch (error) {
    ElMessage.error('删除学生失败，请稍后重试')
  }
}

function resetFilters() {
  Object.assign(filters, {
    studentId: '',
    name: '',
    grade: '',
    tel: '',
    institute: '',
    major: '',
    clazz: ''
  })
  pagination.current = 1
  void fetchStudents()
}

function handleSearch() {
  pagination.current = 1
  void fetchStudents()
}

onMounted(fetchStudents)
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
