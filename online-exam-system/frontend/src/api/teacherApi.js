import request from '@/utils/request'

function toSegment(value) {
  return encodeURIComponent(value?.trim() ? value.trim() : '@')
}

export function getTeacherPage({ page, size, filters }) {
  return request({
    url: `/teachers/${[
      page,
      size,
      toSegment(filters.teacherId),
      toSegment(filters.teacherName),
      toSegment(filters.institute),
      toSegment(filters.type),
      toSegment(filters.tel),
      toSegment(filters.email)
    ].join('/')}`,
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
