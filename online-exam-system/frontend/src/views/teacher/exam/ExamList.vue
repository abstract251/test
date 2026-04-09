<template>
  <PageContainer>
    <PageHeader
      title="考试管理"
      description="维护考试基础信息，进入试卷编辑页后可以按科目从题库加入或移除题目。"
    >
      <template #actions>
        <el-button type="primary" @click="router.push('/console/teacher/exams/new')">
          新增考试
        </el-button>
      </template>
    </PageHeader>

    <el-table :data="pagination.records" stripe empty-text="暂无考试数据">
      <el-table-column prop="examCode" label="考试编号" min-width="90" />
      <el-table-column prop="source" label="科目" min-width="140" />
      <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
      <el-table-column prop="grade" label="年级" min-width="90" />
      <el-table-column prop="major" label="专业" min-width="160" />
      <el-table-column prop="institute" label="学院" min-width="150" />
      <el-table-column label="考试时间" min-width="150">
        <template #default="{ row }">
          {{ formatDateTime(row.examDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="totalTime" label="时长" min-width="90">
        <template #default="{ row }">{{ row.totalTime }} 分钟</template>
      </el-table-column>
      <el-table-column prop="totalScore" label="总分" min-width="90" />
      <el-table-column fixed="right" label="操作" width="260">
        <template #default="{ row }">
          <el-space wrap>
            <el-button link type="primary" @click="openPreview(row)">
              试卷详情
            </el-button>
            <el-button link type="primary" @click="router.push(`/console/teacher/exams/${row.examCode}/paper`)">
              进入组卷
            </el-button>
            <el-button link type="primary" @click="router.push(`/console/teacher/exams/${row.examCode}/edit`)">
              编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row.examCode)">
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
        :page-sizes="[4, 8, 10]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="fetchExams"
        @current-change="fetchExams"
      />
    </div>

    <el-drawer v-model="previewVisible" size="55%" title="试卷详情">
      <div v-if="previewExam" class="drawer-content">
        <ExamMetaPanel :exam="previewExam" />
        <PaperQuestionGroup
          v-for="item in groupedPreview"
          :key="item.type"
          :title="item.title"
          :description="item.description"
          :questions="item.questions"
          :type="item.type"
          :show-action="false"
        />
      </div>
    </el-drawer>
  </PageContainer>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import ExamMetaPanel from '@/components/exam/ExamMetaPanel.vue'
import PaperQuestionGroup from '@/components/exam/PaperQuestionGroup.vue'
import { usePagination } from '@/composables/usePagination'
import { deleteExam, getExamList } from '@/api/examApi'
import { getPaper } from '@/api/paperApi'
import { ensureQuestionMap } from '@/utils/adapters'
import { formatDateTime } from '@/utils/date'
import { QUESTION_TYPE_OPTIONS } from '@/utils/constants'

const router = useRouter()
const pagination = usePagination(4)
const previewVisible = ref(false)
const previewExam = ref(null)
const previewQuestionMap = ref(ensureQuestionMap())

const groupedPreview = computed(() =>
  QUESTION_TYPE_OPTIONS.map((item) => ({
    type: item.value,
    title: item.label,
    description: `当前试卷中的${item.label}`,
    questions: previewQuestionMap.value[item.value]
  }))
)

async function fetchExams() {
  try {
    const response = await getExamList(pagination.current, pagination.size)
    if (response.code === 200) {
      Object.assign(pagination, response.data)
      return
    }
    ElMessage.error(response.message || '加载考试列表失败')
  } catch (error) {
    ElMessage.error('加载考试列表失败，请稍后重试')
  }
}

async function openPreview(exam) {
  try {
    previewExam.value = exam
    const response = await getPaper(exam.paperId)
    if (response.code === 200) {
      previewQuestionMap.value = ensureQuestionMap(response.data)
      previewVisible.value = true
      return
    }
    ElMessage.error(response.message || '加载试卷详情失败')
  } catch (error) {
    ElMessage.error('加载试卷详情失败，请稍后重试')
  }
}

async function handleDelete(examCode) {
  try {
    await ElMessageBox.confirm('删除考试会一并清空试卷题目关联，是否继续？', '删除考试', {
      type: 'warning'
    })
  } catch (error) {
    return
  }

  try {
    const response = await deleteExam(examCode)
    if (response.code === 200) {
      ElMessage.success('考试已删除')
      await fetchExams()
      return
    }
    ElMessage.error(response.message || '删除考试失败')
  } catch (error) {
    ElMessage.error('删除考试失败，请稍后重试')
  }
}

onMounted(fetchExams)
</script>

<style scoped>
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
</style>
