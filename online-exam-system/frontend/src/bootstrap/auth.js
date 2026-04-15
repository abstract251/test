import { getCurrentUser } from '@/api/authApi'
import { getStudentProfile } from '@/api/profileApi'
import {
  clearSession,
  getSession,
  mergeCurrentUser,
  updateSessionRawUser
} from '@/utils/auth'
import { AUTH_ROLES } from '@/utils/constants'

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

    const nextSession = mergeCurrentUser(response.data)
    if (!nextSession) {
      clearSession()
      return null
    }

    if (nextSession.authRole === AUTH_ROLES.STUDENT) {
      const profileResponse = await getStudentProfile(nextSession.userId)
      if (profileResponse?.code === 200 && profileResponse?.data) {
        updateSessionRawUser(profileResponse.data)
      }
    }

    return getSession()
  } catch (error) {
    clearSession()
    return null
  }
}
