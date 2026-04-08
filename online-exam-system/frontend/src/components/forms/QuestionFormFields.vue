<!-- eslint-disable vue/no-mutating-props -->
<template>
  <div class="question-form-fields">
    <el-form-item label="题型" prop="type">
      <el-radio-group v-model="model.type" :disabled="isEdit">
        <el-radio-button
          v-for="item in typeOptions"
          :key="item.value"
          :label="item.value"
        >
          {{ item.label }}
        </el-radio-button>
      </el-radio-group>
    </el-form-item>

    <div class="form-grid">
      <el-form-item label="科目" prop="subject">
        <el-input v-model="model.subject" placeholder="请输入科目" />
      </el-form-item>
      <el-form-item label="章节" prop="section">
        <el-input v-model="model.section" placeholder="请输入章节" />
      </el-form-item>
      <el-form-item label="难度" prop="level">
        <el-select
          v-model="model.level"
          filterable
          allow-create
          default-first-option
          placeholder="选择或输入难度"
        >
          <el-option v-for="item in levelOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="题干" class="full-width" prop="question">
        <el-input v-model="model.question" type="textarea" :rows="4" placeholder="请输入题干内容" />
      </el-form-item>
      <template v-if="model.type === 1">
        <el-form-item label="选项 A" prop="answerA">
          <el-input v-model="model.answerA" />
        </el-form-item>
        <el-form-item label="选项 B" prop="answerB">
          <el-input v-model="model.answerB" />
        </el-form-item>
        <el-form-item label="选项 C" prop="answerC">
          <el-input v-model="model.answerC" />
        </el-form-item>
        <el-form-item label="选项 D" prop="answerD">
          <el-input v-model="model.answerD" />
        </el-form-item>
        <el-form-item label="正确答案" prop="rightAnswer">
          <el-radio-group v-model="model.rightAnswer">
            <el-radio-button label="A" />
            <el-radio-button label="B" />
            <el-radio-button label="C" />
            <el-radio-button label="D" />
          </el-radio-group>
        </el-form-item>
      </template>
      <template v-else-if="model.type === 2">
        <el-form-item label="参考答案" class="full-width" prop="answer">
          <el-input v-model="model.answer" type="textarea" :rows="3" placeholder="请输入填空题答案" />
        </el-form-item>
      </template>
      <template v-else>
        <el-form-item label="参考答案" prop="answer">
          <el-radio-group v-model="model.answer">
            <el-radio-button label="T">正确</el-radio-button>
            <el-radio-button label="F">错误</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </template>
      <el-form-item label="解析" class="full-width" prop="analysis">
        <el-input v-model="model.analysis" type="textarea" :rows="3" placeholder="请输入题目解析" />
      </el-form-item>
    </div>
  </div>
</template>

<script setup>
import { LEVEL_OPTIONS, QUESTION_TYPE_OPTIONS } from '@/utils/constants'

defineProps({
  model: {
    type: Object,
    required: true
  },
  isEdit: {
    type: Boolean,
    default: false
  }
})

const levelOptions = LEVEL_OPTIONS
const typeOptions = QUESTION_TYPE_OPTIONS
</script>

<style scoped>
.question-form-fields {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 0 18px;
}

.full-width {
  grid-column: 1 / -1;
}
</style>
