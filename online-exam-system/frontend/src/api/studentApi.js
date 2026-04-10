import request from '@/utils/request'

function toSegment(value) {
  return encodeURIComponent(value?.trim() ? value.trim() : '@')
}

export function getStudentPage({ page, size, filters }) {
  const path = [
    page,
    size,
    toSegment(filters.studentId),
    toSegment(filters.name),
    toSegment(filters.grade),
    toSegment(filters.tel),
    toSegment(filters.institute),
    toSegment(filters.major),
    toSegment(filters.clazz)
  ].join('/')

  return request({
    url: `/students/${path}`,
    method: 'get'
  })
}

export function getStudentById(studentId) {
  return request({
    url: `/student/${studentId}`,
    method: 'get'
  })
}

export function createStudent(data) {
  return request({
    url: '/student',
    method: 'post',
    data
  })
}

export function updateStudent(data) {
  return request({
    url: '/student',
    method: 'put',
    data
  })
}

export function deleteStudent(studentId) {
  return request({
    url: `/student/${studentId}`,
    method: 'delete'
  })
}

export function updateStudentPassword(data) {
  return request({
    url: '/studentPWD',
    method: 'put',
    data
  })
}
