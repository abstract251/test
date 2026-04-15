import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

export function useViewport(breakpoint = 768) {
  const width = ref(typeof window === 'undefined' ? 1440 : window.innerWidth)

  const syncWidth = () => {
    width.value = window.innerWidth
  }

  onMounted(() => {
    syncWidth()
    window.addEventListener('resize', syncWidth)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', syncWidth)
  })

  return {
    width,
    isMobileViewport: computed(() => width.value <= breakpoint)
  }
}
