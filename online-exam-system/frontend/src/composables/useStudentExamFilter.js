import { computed } from 'vue'

export function useStudentExamFilter(exams, _session, keyword) {
  return computed(() => {
    const safeKeyword = keyword.value.trim().toLowerCase()

    return exams.value
      .filter((item) => {
        if (!safeKeyword) {
          return true
        }

        return [item.source, item.description, item.major, item.institute, item.examState]
          .filter(Boolean)
          .some((field) => String(field).toLowerCase().includes(safeKeyword))
      })
  })
}
