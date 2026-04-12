import { computed } from 'vue'

/**
 * 与后端 StudentExamSessionServiceImpl.matchesScope 一致：
 * 考试在某维度（年级/专业/学院）为空或未填时，表示该维度不限制。
 */
function dimensionMatch(examValue, userValue) {
  const ev = examValue != null ? String(examValue).trim() : ''
  if (!ev) {
    return true
  }
  const uv = userValue != null ? String(userValue).trim() : ''
  return uv === ev
}

export function useStudentExamFilter(exams, session, keyword) {
  return computed(() => {
    const safeKeyword = keyword.value.trim().toLowerCase()
    const rawUser = session.value?.rawUser || {}

    return exams.value
      .filter((item) => {
        return (
          dimensionMatch(item.grade, rawUser.grade) &&
          dimensionMatch(item.major, rawUser.major) &&
          dimensionMatch(item.institute, rawUser.institute)
        )
      })
      .filter((item) => {
        if (!safeKeyword) {
          return true
        }

        return [item.source, item.description, item.major, item.institute]
          .filter(Boolean)
          .some((field) => field.toLowerCase().includes(safeKeyword))
      })
  })
}
