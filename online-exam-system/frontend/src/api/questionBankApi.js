import request from '@/utils/request'

function buildQueryParams(filters = {}) {
  const params = {}

  if (filters.questionType) {
    params.questionType = filters.questionType
  }

  if (filters.subject?.trim()) {
    params.subject = filters.subject.trim()
  }

  if (filters.keyword?.trim()) {
    params.keyword = filters.keyword.trim()
  }

  return params
}

export function getQuestionBankPage({ page, size, filters = {} }) {
  return request({
    url: `/question-bank/${page}/${size}`,
    method: 'get',
    params: buildQueryParams(filters)
  })
}

export function deleteQuestionBankItem(questionType, questionId) {
  return request({
    url: `/question-bank/${questionType}/${questionId}`,
    method: 'delete'
  })
}
