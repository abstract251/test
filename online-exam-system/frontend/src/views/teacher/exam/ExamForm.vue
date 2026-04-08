<template>
  <PageContainer>
    <PageHeader
      :title="isEdit ? '编辑考试' : '新增考试'"
      description="考试时间精确到分钟，试卷编号会在创建时自动分配。"
    />

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <ExamFormFields :model="form" />

      <div class="summary">
        <span>试卷编号：{{ form.paperId || '创建时生成' }}</span>
        <span>当前总分：{{ form.totalScore || 0 }}</span>
      </div>

      <div class="actions">
        <el-button @click="router.push('/console/teacher/exams')">取消</el-button>
        <el-button type="primary" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '创建考试' }}
        </el-button>
      </div>
    </el-form>
  </PageContainer>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import ExamFormFields from '@/components/forms/ExamFormFields.vue'
import { addExam, getExamById, getLatestPaperId, updateExam } from '@/api/examApi'
import { toBackendDateTime, toPickerDateTime } from '@/utils/date'
import { patternRule, requiredRule } from '@/utils/validators'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)

const isEdit = computed(() => Boolean(route.params.examCode))

const form = reactive({
  examCode: null,
  description: '',
  source: '',
  paperId: null,
  examDate: '',
  totalTime: 120,
  grade: '',
  term: '1',
  major: '',
  institute: '',
  totalScore: 0,
  type: '正式考试',
  tips: ''
})

const rules = {
  source: [
    requiredRule('请输入科目'),
    patternRule(/^.{2,30}$/, '科目长度需为 2-30 个字符')
  ],
  description: [
    requiredRule('请输入考试说明'),
    patternRule(/^.{2,100}$/, '考试说明长度需为 2-100 个字符')
  ],
  institute: [requiredRule('请选择或输入学院', 'change')],
  major: [requiredRule('请选择或输入专业', 'change')],
  grade: [requiredRule('请选择或输入年级', 'change')],
  examDate: [requiredRule('请选择考试时间', 'change')],
  totalTime: [
    requiredRule('请输入考试时长', 'change'),
    {
      validator: (_, value, callback) => {
        if (!Number.isInteger(value) || value < 1 || value > 300) {
          callback(new Error('考试时长需在 1-300 分钟之间'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  type: [requiredRule('请选择或输入考试类型', 'change')],
  tips: [
    requiredRule('请输入考生提示'),
    patternRule(/^.{2,200}$/, '考生提示长度需为 2-200 个字符')
  ]
}

async function fetchExam() {
  if (!isEdit.value) {
    return
  }

  try {
    const response = await getExamById(route.params.examCode)
    if (response.code === 200 && response.data) {
      Object.assign(form, response.data, {
        examDate: toPickerDateTime(response.data.examDate)
      })
      return
    }
    ElMessage.error(response.message || '加载考试信息失败')
  } catch (error) {
    ElMessage.error('加载考试信息失败，请稍后重试')
  }
}

async function assignPaperId() {
  if (form.paperId) {
    return
  }

  try {
    const response = await getLatestPaperId()
    const latest = response.code === 200 && response.data?.paperId ? Number(response.data.paperId) : 1000
    form.paperId = latest + 1
  } catch (error) {
    form.paperId = 1001
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  if (!isEdit.value) {
    await assignPaperId()
  }

  const payload = {
    ...form,
    examDate: toBackendDateTime(form.examDate),
    totalScore: Number(form.totalScore || 0)
  }

  try {
    const response = isEdit.value
      ? await updateExam(payload)
      : await addExam(payload)

    if (response.code === 200) {
      ElMessage.success(isEdit.value ? '考试信息已更新' : '考试已创建')
      router.push('/console/teacher/exams')
      return
    }

    ElMessage.error(response.message || '保存失败，请检查后重试')
  } catch (error) {
    ElMessage.error('保存失败，请稍后重试')
  }
}

onMounted(fetchExam)
</script>

<style scoped>
.summary {
  display: flex;
  gap: 18px;
  flex-wrap: wrap;
  margin: 0 0 20px;
  color: var(--text-secondary);
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
