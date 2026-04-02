<!--
  考试列表页（示例业务页面）

  知识点：
  1. 模板 template：描述界面结构，使用 Element Plus 的表格、分页、按钮。
  2. 脚本 script：data 存放状态，methods 里写方法，mounted 里做首屏请求。
  3. 样式 style scoped：只作用于本组件，避免污染全局。

  数据流：
  用户打开页面 → mounted 调用 getList → 请求后端分页接口 → 把返回的 records 填进表格、total 填进分页器。
-->
<template>
  <div class="exam-container">
    <h2>考试管理</h2>

    <!--
      el-table：数据表格
      :data="tableData" 绑定行数据（数组），每一行对应一场考试
      border 显示边框；style 宽度 100% 占满容器
    -->
    <el-table :data="tableData" border style="width: 100%">
      <!-- prop 对应后端 ExamManage 实体字段名（JSON 驼峰） -->
      <el-table-column prop="examCode" label="考试编号" />
      <el-table-column prop="description" label="考试描述" />
      <el-table-column prop="source" label="课程名称" />
      <el-table-column prop="examDate" label="考试日期" />
      <el-table-column prop="totalTime" label="时长(分钟)" />
      <el-table-column prop="totalScore" label="总分" />
      <!-- 自定义列：操作列，用 scope.row 取当前行数据 -->
      <el-table-column label="操作">
        <template #default="scope">
          <!--
            @click：点击删除当前行
            :loading：删除进行中时显示加载状态，防止重复点击
            scope.row.examCode：当前行的考试编号，传给后端删除接口
          -->
          <el-button
            @click="handleDelete(scope.row.examCode)"
            type="danger"
            size="small"
            :loading="deletingId === scope.row.examCode"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!--
      el-pagination：分页组件
      v-model:current-page / v-model:page-size：双向绑定当前页、每页条数（Vue 3 语法）
      :total：总条数（来自后端分页结果）
      layout：显示的控件布局
      @size-change / @current-change：每页条数或页码变化时重新拉取列表
    -->
    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next"
      @size-change="getList"
      @current-change="getList"
      style="margin-top:20px;text-align:right"
    />
  </div>
</template>

<script>
/**
 * @ 为路径别名，指向 src/（见项目根目录 jsconfig.json）
 * ElMessage：Element Plus 的消息提示（成功/失败气泡）
 */
import { getExamList, deleteExam } from '@/api/examApi'
import { ElMessage } from 'element-plus'

export default {
  name: 'ExamList',
  data() {
    return {
      /** 表格行数据：后端分页结果里的 records */
      tableData: [],
      /** 当前页码（与 el-pagination 绑定） */
      pageNum: 1,
      /** 每页条数 */
      pageSize: 10,
      /** 总条数（与 el-pagination 的 total 绑定） */
      total: 0,
      /**
       * 正在删除的考试编号；用于按钮 loading
       * 为 null 表示没有删除请求在进行
       */
      deletingId: null
    }
  },
  /**
   * 生命周期：组件挂载到页面后执行（只执行一次）
   * 适合发起首屏数据请求
   */
  mounted() {
    this.getList()
  },
  methods: {
    /**
     * 拉取考试分页列表
     * 后端成功时 code === 200，data 为 MyBatis-Plus 分页对象（含 records、total 等）
     */
    async getList() {
      try {
        const res = await getExamList(this.pageNum, this.pageSize)
        if (res?.code !== 200) {
          ElMessage.error(res?.message || '获取列表失败')
          return
        }
        this.tableData = res.data?.records || []
        this.total = res.data?.total || 0
      } catch (e) {
        // 网络错误、超时、后端未启动等会进入 catch
        ElMessage.error(e?.message || '无法连接后端，请确认后端已启动且地址正确')
      }
    },

    /**
     * 删除指定考试
     * @param {number} examCode 考试编号
     */
    async handleDelete(examCode) {
      this.deletingId = examCode
      try {
        const res = await deleteExam(examCode)
        if (res?.code !== 200) {
          ElMessage.error(res?.message || '删除失败')
          return
        }
        ElMessage.success('删除成功')
        await this.getList()
      } catch (e) {
        ElMessage.error(e?.message || '删除失败：无法连接后端')
      } finally {
        // 无论成功失败，结束 loading
        this.deletingId = null
      }
    }
  }
}
</script>

<style scoped>
/* scoped：样式只作用于本组件的根元素及其子元素，不会影响其他页面 */
.exam-container {
  padding: 20px;
}
</style>
