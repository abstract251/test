import { onBeforeUnmount, onMounted, ref } from 'vue'
import { getSession } from '@/utils/auth'

export function useAuthSession() {
  const session = ref(getSession())

  const syncSession = () => {
    session.value = getSession()
  }

  onMounted(() => {
    window.addEventListener('storage', syncSession)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('storage', syncSession)
  })

  return {
    session,
    syncSession
  }
}
