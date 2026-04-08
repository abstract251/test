import { computed } from 'vue'

export function useStudentExamFilter(exams, session, keyword) {
  return computed(() => {
    const safeKeyword = keyword.value.trim().toLowerCase()
    const rawUser = session.value?.rawUser || {}

    return exams.value
      .filter((item) => {
        const gradeMatch = !rawUser.grade || item.grade === rawUser.grade
        const majorMatch = !rawUser.major || item.major === rawUser.major
        const instituteMatch = !rawUser.institute || item.institute === rawUser.institute
        return gradeMatch && majorMatch && instituteMatch
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
