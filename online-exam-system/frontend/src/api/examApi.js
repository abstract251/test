/**
 * 考试管理相关 API（与后端 ExamManageController 对应）
 *
 * 调用方式：
 * 在组件中 import { getExamList } from '@/api/examApi'
 * （@ 指向 src/，见 jsconfig.json 的 paths 配置）
 *
 * HTTP 方法约定（REST 常见风格）：
 * - GET    查询
 * - POST   新增
 * - PUT    全量/部分更新（本项目用于更新考试）
 * - DELETE 删除
 *
 * 后端接口一览（路径以 baseURL 为前缀，见 src/utils/request.js）：
 * | 方法   | 路径                    | 说明           |
 * |--------|-------------------------|----------------|
 * | GET    | /exams/{page}/{size}   | 分页考试列表   |
 * | DELETE | /exam/{examCode}       | 按编号删除考试 |
 * | POST   | /exam                  | 新增考试       |
 * | PUT    | /exam                  | 更新考试       |
 * | GET    | /exam/{examCode}       | 按编号查询考试 |
 *
 * 返回值：
 * 均为 Promise，resolve 值为 ApiResult（因 axios 拦截器已解包 res.data）
 */
import request from '../utils/request'

// --- 查询（分页）---

/**
 * 分页查询考试列表
 * @param {number} page 当前页码（从 1 开始，与后端 MyBatis-Plus Page 一致）
 * @param {number} size 每页条数
 * @returns {Promise<{ code: number, message: string, data: { records: Array, total: number, ... } }>}
 */
export function getExamList(page, size) {
  return request({
    url: `/exams/${page}/${size}`,
    method: 'get'
  })
}

// --- 删除 ---

/**
 * 删除考试
 * @param {number} examCode 考试编号（主键）
 */
export function deleteExam(examCode) {
  return request({
    url: `/exam/${examCode}`,
    method: 'delete'
  })
}

// --- 新增 / 更新（当前页面未使用，预留接口）---

/**
 * 新增考试
 * @param {object} data 请求体，字段需与后端 ExamManage 实体一致
 */
export function addExam(data) {
  return request({
    url: '/exam',
    method: 'post',
    data
  })
}

/**
 * 更新考试
 * @param {object} data 请求体，需包含 examCode 等主键/更新字段
 */
export function updateExam(data) {
  return request({
    url: '/exam',
    method: 'put',
    data
  })
}

/**
 * 按考试编号查询单条
 * @param {number} examCode 考试编号
 */
export function getExamById(examCode) {
  return request({
    url: `/exam/${examCode}`,
    method: 'get'
  })
}
