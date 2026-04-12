import request from '@/utils/request'

export function getExamList(page, size) {
  return request({
    url: `/exams/${page}/${size}`,
    method: 'get'
  })
}

export function getAllExams() {
  return request({
    url: '/exams',
    method: 'get'
  })
}

export function getExamById(examCode) {
  return request({
    url: `/exam/${examCode}`,
    method: 'get'
  })
}

export function addExam(data) {
  return request({
    url: '/exam',
    method: 'post',
    data
  })
}

export function updateExam(data) {
  return request({
    url: '/exam',
    method: 'put',
    data
  })
}

export function deleteExam(examCode) {
  return request({
    url: `/exam/${examCode}`,
    method: 'delete'
  })
}

export function getLatestPaperId() {
  return request({
    url: '/examManagePaperId',
    method: 'get'
  })
}

/** 开考/冻结/撤销/快照等业务策略（评审稿） */
export function getExamPolicy(examCode) {
  return request({
    url: `/exam/${examCode}/exam-policy`,
    method: 'get'
  })
}

/** 冻结后的本场共用题目（结构与 /paper/{paperId} 一致） */
export function getFrozenExamPaper(examCode) {
  return request({
    url: `/exam/${examCode}/frozen-paper`,
    method: 'get'
  })
}

/** 超级管理员撤销考试 */
export function revokeExamAsAdmin(examCode, reason) {
  return request({
    url: `/admin/exam/${examCode}/revoke`,
    method: 'post',
    data: { reason }
  })
}
