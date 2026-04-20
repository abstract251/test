<!-- eslint-disable vue/no-mutating-props -->
<template>
  <div>
    <el-alert
      v-if="revoked"
      title="这场考试已取消，暂时不能再修改。"
      type="error"
      show-icon
      :closable="false"
      class="form-alert"
    />
    <el-alert
      v-else-if="freezeLocked"
      title="这场考试已进入锁定阶段：目前只能调整考试说明、考生提示和延长考试时长。"
      type="warning"
      show-icon
      :closable="false"
      class="form-alert"
    />

    <!-- 冻结或撤销：只读摘要 -->
    <div v-if="freezeLocked || revoked" class="readonly-summary">
      <p><strong>科目</strong>：{{ model.source || '—' }}</p>
      <p><strong>考试类型</strong>：{{ model.type || '—' }}</p>
      <p><strong>开考时间</strong>：{{ formatDateTime(model.examDate) }}</p>
      <p><strong>学院 / 专业 / 年级</strong>：{{ model.institute || '—' }} / {{ model.major || '—' }} / {{ model.grade || '—' }}</p>
      <p><strong>学期</strong>：{{ model.term || '—' }}</p>
      <p><strong>试卷编号</strong>：{{ model.paperId ?? '—' }}</p>
    </div>

    <div v-if="!revoked" class="form-grid" :class="{ 'whitelist-only': freezeLocked }">
      <template v-if="!freezeLocked">
        <el-form-item label="科目" prop="source">
          <el-input v-model="model.source" placeholder="例如：Java / 数据库 / 操作系统" />
        </el-form-item>
        <el-form-item label="考试类型" prop="type">
          <el-select
            v-model="model.type"
            filterable
            allow-create
            default-first-option
            placeholder="选择或输入考试类型"
          >
            <el-option v-for="item in examTypeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="学院" prop="institute">
          <el-select
            v-model="model.institute"
            filterable
            allow-create
            default-first-option
            placeholder="选择或输入学院"
          >
            <el-option v-for="item in instituteOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-select
            v-model="model.major"
            filterable
            allow-create
            default-first-option
            placeholder="选择或输入专业"
          >
            <el-option v-for="item in majorOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-select
            v-model="model.grade"
            filterable
            allow-create
            default-first-option
            placeholder="选择或输入年级"
          >
            <el-option v-for="item in gradeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="term">
          <el-select v-model="model.term" placeholder="请选择学期">
            <el-option v-for="item in termOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="开考时间" prop="examDate">
          <el-date-picker
            v-model="model.examDate"
            type="datetime"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
            placeholder="选择开考时间"
          />
        </el-form-item>
        <el-form-item label="考试时长（分钟）" prop="totalTime">
          <el-input-number v-model="model.totalTime" :min="1" :max="300" controls-position="right" />
        </el-form-item>
      </template>

      <template v-else>
        <el-form-item label="考试时长（分钟）" prop="totalTime">
          <el-input-number
            v-model="model.totalTime"
            :min="minTotalTimeWhenFrozen"
            :max="300"
            controls-position="right"
          />
          <span class="hint">冻结后仅可延长，当前不少于 {{ minTotalTimeWhenFrozen }} 分钟</span>
        </el-form-item>
      </template>

      <el-form-item label="考试说明" class="full-width" prop="description">
        <el-input v-model="model.description" type="textarea" :rows="3" placeholder="考试说明或副标题" />
      </el-form-item>
      <el-form-item label="考生提示" class="full-width" prop="tips">
        <el-input v-model="model.tips" type="textarea" :rows="3" placeholder="例如：请独立完成，提前 10 分钟进入考场" />
      </el-form-item>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  EXAM_TYPE_OPTIONS,
  GRADE_OPTIONS,
  INSTITUTE_OPTIONS,
  MAJOR_OPTIONS,
  TERM_OPTIONS
} from '@/utils/constants'
import { formatDateTime } from '@/utils/date'

const props = defineProps({
  model: {
    type: Object,
    required: true
  },
  /** 试卷已冻结（仅少量内容可改） */
  freezeLocked: {
    type: Boolean,
    default: false
  },
  revoked: {
    type: Boolean,
    default: false
  },
  /** 锁定前记录的时长下限（锁定后不可低于此值） */
  baselineTotalTime: {
    type: Number,
    default: 1
  }
})

const examTypeOptions = EXAM_TYPE_OPTIONS
const gradeOptions = GRADE_OPTIONS
const instituteOptions = INSTITUTE_OPTIONS
const majorOptions = MAJOR_OPTIONS
const termOptions = TERM_OPTIONS

const minTotalTimeWhenFrozen = computed(() =>
  Math.max(1, Number(props.baselineTotalTime) || Number(props.model.totalTime) || 1)
)
</script>

<style scoped>
.form-alert {
  margin-bottom: 16px;
}

.readonly-summary {
  margin-bottom: 18px;
  padding: 12px 14px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
  font-size: 13px;
  line-height: 1.7;
}

.readonly-summary p {
  margin: 0;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 0 18px;
}

.form-grid.whitelist-only {
  grid-template-columns: 1fr;
}

.full-width {
  grid-column: 1 / -1;
}

.hint {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
