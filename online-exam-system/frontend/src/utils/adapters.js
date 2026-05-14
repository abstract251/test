import { QUESTION_TYPE_LABELS, ROLES } from '@/utils/constants'

export function normalizeLoginUser(user) {
  if (!user || !user.role) {
    return null
  }

  const role = String(user.role)
  if (role === ROLES.ADMIN) {
    return {
      role,
      userId: String(user.adminId),
      userName: user.adminName,
      rawUser: user
    }
  }

  if (role === ROLES.TEACHER) {
    return {
      role,
      userId: String(user.teacherId),
      userName: user.teacherName,
      rawUser: user
    }
  }

  return {
    role,
    userId: String(user.studentId),
    userName: user.studentName,
    rawUser: user
  }
}

export function ensureQuestionMap(questionMap = {}) {
  return {
    1: questionMap[1] || questionMap['1'] || [],
    2: questionMap[2] || questionMap['2'] || [],
    3: questionMap[3] || questionMap['3'] || []
  }
}

export function flattenQuestionMap(questionMap) {
  const safeMap = ensureQuestionMap(questionMap)
  return Object.entries(safeMap).flatMap(([type, rows]) =>
    rows.map((row) => ({
      ...row,
      type: Number(type),
      typeLabel: QUESTION_TYPE_LABELS[Number(type)]
    }))
  )
}

export function cloneRecord(record) {
  return JSON.parse(JSON.stringify(record))
}
