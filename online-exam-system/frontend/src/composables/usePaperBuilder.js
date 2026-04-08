import { computed, ref } from 'vue'
import { ensureQuestionMap } from '@/utils/adapters'

export function usePaperBuilder() {
  const availableQuestions = ref(ensureQuestionMap())
  const selectedQuestions = ref(ensureQuestionMap())

  const totals = computed(() => ({
    available: Object.values(availableQuestions.value).reduce((sum, rows) => sum + rows.length, 0),
    selected: Object.values(selectedQuestions.value).reduce((sum, rows) => sum + rows.length, 0)
  }))

  function setAvailableQuestions(questionMap) {
    availableQuestions.value = ensureQuestionMap(questionMap)
  }

  function setSelectedQuestions(questionMap) {
    selectedQuestions.value = ensureQuestionMap(questionMap)
  }

  function hasQuestion(type, questionId) {
    return selectedQuestions.value[type]?.some((item) => item.questionId === questionId) || false
  }

  return {
    availableQuestions,
    selectedQuestions,
    totals,
    setAvailableQuestions,
    setSelectedQuestions,
    hasQuestion
  }
}
