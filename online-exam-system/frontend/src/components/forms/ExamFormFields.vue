<!-- eslint-disable vue/no-mutating-props -->
<template>
  <div class="form-grid">
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
    <el-form-item label="考试时间" prop="examDate">
      <el-date-picker
        v-model="model.examDate"
        type="datetime"
        format="YYYY-MM-DD HH:mm"
        value-format="YYYY-MM-DD HH:mm"
        placeholder="选择考试时间"
      />
    </el-form-item>
    <el-form-item label="考试时长" prop="totalTime">
      <el-input-number v-model="model.totalTime" :min="1" :max="300" controls-position="right" />
    </el-form-item>
    <el-form-item label="说明" class="full-width" prop="description">
      <el-input v-model="model.description" type="textarea" :rows="3" placeholder="考试说明或副标题" />
    </el-form-item>
    <el-form-item label="考生提示" class="full-width" prop="tips">
      <el-input v-model="model.tips" type="textarea" :rows="3" placeholder="例如：请独立完成，提前 10 分钟进入考场" />
    </el-form-item>
  </div>
</template>

<script setup>
import { EXAM_TYPE_OPTIONS, GRADE_OPTIONS, INSTITUTE_OPTIONS, MAJOR_OPTIONS, TERM_OPTIONS } from '@/utils/constants'

defineProps({
  model: {
    type: Object,
    required: true
  }
})

const examTypeOptions = EXAM_TYPE_OPTIONS
const gradeOptions = GRADE_OPTIONS
const instituteOptions = INSTITUTE_OPTIONS
const majorOptions = MAJOR_OPTIONS
const termOptions = TERM_OPTIONS
</script>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 0 18px;
}

.full-width {
  grid-column: 1 / -1;
}
</style>
