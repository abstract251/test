import request from '@/utils/request'

export function login(payload) {
  return request({
    url: '/login',
    method: 'post',
    data: payload
  })
}

export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}

export function resetConsolePassword(userId, oldPassword, newPassword) {
  return request({
    url: `/admin/resetPsw/${encodeURIComponent(userId)}/${encodeURIComponent(oldPassword)}/${encodeURIComponent(newPassword)}`,
    method: 'get'
  })
}
