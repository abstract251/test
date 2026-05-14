import { reactive } from 'vue'

export function usePagination(initialSize = 10) {
  return reactive({
    current: 1,
    size: initialSize,
    total: 0,
    records: []
  })
}
