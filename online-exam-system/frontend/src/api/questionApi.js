import request from '@/utils/request'

const latestIdPathMap = {
  1: '/multiQuestionId',
  2: '/fillQuestionId',
  3: '/judgeQuestionId'
}

const createPathMap = {
  1: '/MultiQuestion',
  2: '/fillQuestion',
  3: '/judgeQuestion'
}

const updatePathMap = {
  1: '/editMultiQuestion',
  2: '/editFillQuestion',
  3: '/editJudgeQuestion'
}

export function getLatestQuestionId(type) {
  return request({
    url: latestIdPathMap[type],
    method: 'get'
  })
}

export function createQuestion(type, data) {
  return request({
    url: createPathMap[type],
    method: 'post',
    data
  })
}

export function updateQuestion(type, data) {
  return request({
    url: updatePathMap[type],
    method: 'post',
    data
  })
}
