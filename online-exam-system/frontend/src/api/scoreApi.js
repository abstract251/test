import request from '@/utils/request'

function toSegment(value) {
  return encodeURIComponent(value)
}

export function getAllScores() {
  return request({
    url: '/scores',
    method: 'get'
  })
}

export function getStudentScorePage({ page, size, studentId }) {
  return request({
    url: `/score/${page}/${size}/${toSegment(studentId)}`,
    method: 'get'
  })
}

export function getStudentScores(studentId) {
  return request({
    url: `/score/${toSegment(studentId)}`,
    method: 'get'
  })
}

export function createScore(data) {
  return request({
    url: '/score',
    method: 'post',
    data
  })
}

export function getExamScores(examCode) {
  return request({
    url: `/scores/${examCode}`,
    method: 'get'
  })
}

export function getScoreStatistics(examCode) {
  return request({
    url: `/score/statistics/${examCode}`,
    method: 'get'
  })
}

export function getScoresByClazz(examCode, clazz) {
  return request({
    url: `/scores/by-clazz/${examCode}/${toSegment(clazz)}`,
    method: 'get'
  })
}

export function getScoresByClazzPage({ page, size, examCode, clazz }) {
  return request({
    url: `/scores/by-clazz/${examCode}/${page}/${size}/${toSegment(clazz)}`,
    method: 'get'
  })
}
