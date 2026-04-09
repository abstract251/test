import request from '@/utils/request'

export function getTeacherPage(page, size) {
  return request({
    url: `/teachers/${page}/${size}`,
    method: 'get'
  })
}

export function getTeacherById(teacherId) {
  return request({
    url: `/teacher/${teacherId}`,
    method: 'get'
  })
}

export function createTeacher(data) {
  return request({
    url: '/teacher',
    method: 'post',
    data
  })
}

export function updateTeacher(data) {
  return request({
    url: '/teacher',
    method: 'put',
    data
  })
}

export function deleteTeacher(teacherId) {
  return request({
    url: `/teacher/${teacherId}`,
    method: 'delete'
  })
}
