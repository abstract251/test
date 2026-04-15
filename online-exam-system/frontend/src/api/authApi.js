import request from '@/utils/request'

export function login(payload) {
  return request({
    url: '/auth/login',
    method: 'post',
    data: payload
  })
}

export function refreshToken(payload) {
  return request({
    url: '/auth/refresh',
    method: 'post',
    data: payload,
    skipAuthRefresh: true
  })
}

export function logout(payload) {
  return request({
    url: '/auth/logout',
    method: 'post',
    data: payload,
    skipAuthRefresh: true
  })
}

export function getCurrentUser() {
  return request({
    url: '/auth/me',
    method: 'get'
  })
}

export function resetConsolePassword(userId, oldPassword, newPassword) {
  return request({
    url: `/admin/resetPsw/${encodeURIComponent(userId)}/${encodeURIComponent(oldPassword)}/${encodeURIComponent(newPassword)}`,
    method: 'get'
  })
}
