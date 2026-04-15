import { getCurrentUser } from '@/api/authApi'
import { clearSession, getSession, mergeCurrentUser } from '@/utils/auth'

export async function restoreAuthSession() {
  const session = getSession()
  if (!session?.refreshToken || !session?.accessToken) {
    clearSession()
    return null
  }

  try {
    const response = await getCurrentUser()
    if (response?.code !== 200 || !response?.data) {
      clearSession()
      return null
    }

    return mergeCurrentUser(response.data)
  } catch (error) {
    clearSession()
    return null
  }
}
