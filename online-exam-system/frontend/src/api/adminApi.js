import request from '@/utils/request'

function toSegment(value) {
  return encodeURIComponent(value)
}

export function getAdmins() {
  return request({
    url: '/admins',
    method: 'get'
  })
}

export function getAdminById(adminId) {
  return request({
    url: `/admin/${adminId}`,
    method: 'get'
  })
}

export function createAdmin(data) {
  return request({
    url: '/admin',
    method: 'post',
    data
  })
}

export function updateAdmin(adminId, data) {
  return request({
    url: `/admin/${adminId}`,
    method: 'put',
    data
  })
}

export function deleteAdmin(adminId) {
  return request({
    url: `/admin/${adminId}`,
    method: 'delete'
  })
}

export function resetAdminPassword(adminId, oldPassword, newPassword) {
  return request({
    url: `/admin/resetPsw/${adminId}/${toSegment(oldPassword)}/${toSegment(newPassword)}`,
    method: 'get'
  })
}
