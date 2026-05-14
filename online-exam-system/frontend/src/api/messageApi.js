import request from '@/utils/request'

export function getMessagePage(page, size) {
  return request({
    url: `/messages/${page}/${size}`,
    method: 'get'
  })
}

export function getMessageById(messageId) {
  return request({
    url: `/message/${messageId}`,
    method: 'get'
  })
}

export function createMessage(data) {
  return request({
    url: '/message',
    method: 'post',
    data
  })
}

export function deleteMessage(messageId) {
  return request({
    url: `/message/${messageId}`,
    method: 'delete'
  })
}

export function createReply(data) {
  return request({
    url: '/replay',
    method: 'post',
    data
  })
}

export function getReplyList(messageId) {
  return request({
    url: `/replay/${messageId}`,
    method: 'get'
  })
}
