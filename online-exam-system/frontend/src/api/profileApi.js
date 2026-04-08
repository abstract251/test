import { getStudentById, updateStudent, updateStudentPassword } from '@/api/studentApi'

export function getStudentProfile(studentId) {
  return getStudentById(studentId)
}

export function updateStudentProfile(data) {
  return updateStudent(data)
}

export function changeStudentPassword(data) {
  return updateStudentPassword(data)
}
