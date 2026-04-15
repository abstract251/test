import request from '@/utils/request'

export function getPaper(paperId) {
  return request({
    url: `/paper/${paperId}`,
    method: 'get'
  })
}

export function getPracticeBySubject(subject) {
  return request({
    url: `/practice/${encodeURIComponent(subject)}`,
    method: 'get'
  })
}

export function addPaperQuestion(data) {
  return request({
    url: '/paperManage',
    method: 'post',
    data
  })
}

export function autoGeneratePaper(data) {
  return request({
    url: '/item',
    method: 'post',
    data
  })
}

export function removePaperQuestion(paperId, type, questionId) {
  return request({
    url: `/paper/delete/${paperId}/${type}/${questionId}`,
    method: 'get'
  })
}

export function clearPaper(paperId) {
  return request({
    url: `/paper/deleteAll/${paperId}`,
    method: 'delete'
  })
}

export function getPaperScore(paperId) {
  return request({
    url: `/paper/score/${paperId}`,
    method: 'get'
  })
}
