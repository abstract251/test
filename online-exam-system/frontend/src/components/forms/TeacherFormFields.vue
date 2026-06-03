<!-- eslint-disable vue/no-mutating-props -->
<template>
  <div class="form-grid">
    <el-form-item v-if="isEdit" label="教师编号">
      <el-input :model-value="model.teacherId" disabled />
    </el-form-item>
    <el-form-item label="姓名" prop="teacherName">
      <el-input v-model="model.teacherName" placeholder="请输入教师姓名" />
    </el-form-item>
    <el-form-item label="学院" prop="institute">
      <el-select
        v-model="model.institute"
        filterable
        allow-create
        default-first-option
        placeholder="选择或输入学院"
      >
        <el-option
          v-for="item in instituteOptions"
          :key="item"
          :label="item"
          :value="item"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="职称" prop="type">
      <el-select
        v-model="model.type"
        filterable
        allow-create
        default-first-option
        placeholder="选择或输入职称"
      >
        <el-option
          v-for="item in teacherTypeOptions"
          :key="item"
          :label="item"
          :value="item"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="性别" prop="sex">
      <el-select v-model="model.sex" placeholder="请选择性别">
        <el-option
          v-for="item in sexOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="手机号" prop="tel">
      <el-input v-model="model.tel" placeholder="请输入手机号" />
    </el-form-item>
    <el-form-item label="邮箱" prop="email">
      <el-input v-model="model.email" placeholder="请输入邮箱" />
    </el-form-item>
    <el-form-item label="身份证号" prop="cardId">
      <el-input v-model="model.cardId" placeholder="请输入身份证号" />
    </el-form-item>
    <el-form-item label="密码" prop="pwd">
      <el-input 
        v-model="model.pwd" 
        :placeholder="isEdit ? '如需修改密码请输入新密码，留空则不修改' : '默认 123456，可手动修改'" 
        show-password 
      />
    </el-form-item>
  </div>
</template>

<script setup>
import { INSTITUTE_OPTIONS, SEX_OPTIONS, TEACHER_TYPE_OPTIONS } from '@/utils/constants'

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

const instituteOptions = INSTITUTE_OPTIONS
const sexOptions = SEX_OPTIONS
const teacherTypeOptions = TEACHER_TYPE_OPTIONS
</script>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 0 18px;
}
</style>
