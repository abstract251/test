import { getSession, getSessionRef } from '@/utils/auth'

export function useAuthSession() {
  const session = getSessionRef()

  const syncSession = () => {
    session.value = getSession()
  }

  return {
    session,
    syncSession
  }
}
