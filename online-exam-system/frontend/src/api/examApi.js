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
