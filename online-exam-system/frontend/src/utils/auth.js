import { ref } from 'vue'
import {
  AUTH_ROLE_TO_LEGACY_ROLE,
  AUTH_ROLES,
  LEGACY_ROLE_TO_AUTH_ROLE,
  ROLES,
  STORAGE_KEYS
} from '@/utils/constants'
import {
  readSessionStorage,
  removeSessionStorage,
  writeSessionStorage
} from '@/utils/storage'

function normalizeAuthRole(role) {
  const normalized = String(role || '').trim().toUpperCase()
  if (Object.values(AUTH_ROLES).includes(normalized)) {
    return normalized
  }
  return LEGACY_ROLE_TO_AUTH_ROLE[role] || ''
}

function normalizeCurrentUser(user) {
  if (!user) {
    return null
  }

  const authRole = normalizeAuthRole(user.role)
  const role = AUTH_ROLE_TO_LEGACY_ROLE[authRole] || ''
  const userId = user.userId ?? user.studentId ?? user.teacherId ?? user.adminId ?? ''
  const username = user.username ?? user.userName ?? String(userId || '')
  const displayName =
    user.displayName ?? user.studentName ?? user.teacherName ?? user.adminName ?? username

  if (!authRole || !userId) {
    return null
  }

  return {
    userId: String(userId),
    username: String(username),
    displayName: String(displayName),
    role,
    authRole
  }
}

function buildSessionPayload({
  accessToken,
  refreshToken,
  expiresIn,
  user,
  rawUser
}) {
  const normalizedUser = normalizeCurrentUser(user)
  if (!normalizedUser || !accessToken || !refreshToken) {
    return null
  }

  const safeExpiresIn = Number(expiresIn || 0)
  const expiresAt = safeExpiresIn > 0 ? Date.now() + safeExpiresIn * 1000 : null
  const profile = rawUser || null

  return {
    accessToken,
    refreshToken,
    expiresIn: safeExpiresIn,
    expiresAt,
    user: normalizedUser,
    profile,
    userId: normalizedUser.userId,
    username: normalizedUser.username,
    userName: normalizedUser.displayName,
    displayName: normalizedUser.displayName,
    role: normalizedUser.role,
    authRole: normalizedUser.authRole,
    rawUser: profile || normalizedUser
  }
}

function readSessionState() {
  const accessToken = readSessionStorage(STORAGE_KEYS.ACCESS_TOKEN, '')
  const refreshToken = readSessionStorage(STORAGE_KEYS.REFRESH_TOKEN, '')
  const currentUser = readSessionStorage(STORAGE_KEYS.CURRENT_USER, null)
  const session = readSessionStorage(STORAGE_KEYS.SESSION, null)

  if (!accessToken || !refreshToken || !currentUser) {
    return null
  }

  return {
    ...(session || {}),
    accessToken,
    refreshToken,
    user: currentUser,
    userId: currentUser.userId,
    username: currentUser.username,
    userName: currentUser.displayName,
    displayName: currentUser.displayName,
    role: currentUser.role,
    authRole: currentUser.authRole,
    rawUser: session?.profile || session?.rawUser || currentUser
  }
}

const sessionState = ref(readSessionState())

export function getSession() {
  return sessionState.value
}

export function getSessionRef() {
  return sessionState
}

export function hasSession() {
  return Boolean(sessionState.value?.accessToken && sessionState.value?.user)
}

export function getAccessToken() {
  return sessionState.value?.accessToken || ''
}

export function getRefreshToken() {
  return sessionState.value?.refreshToken || ''
}

export function setSession(session) {
  if (!session) {
    clearSession()
    return null
  }

  writeSessionStorage(STORAGE_KEYS.ACCESS_TOKEN, session.accessToken)
  writeSessionStorage(STORAGE_KEYS.REFRESH_TOKEN, session.refreshToken)
  writeSessionStorage(STORAGE_KEYS.CURRENT_USER, session.user)
  writeSessionStorage(STORAGE_KEYS.SESSION, session)
  sessionState.value = session
  return session
}

export function setSessionFromAuthResponse(data) {
  return setSession(
    buildSessionPayload({
      accessToken: data?.accessToken,
      refreshToken: data?.refreshToken,
      expiresIn: data?.expiresIn,
      user: data?.user
    })
  )
}

export function mergeCurrentUser(user) {
  const session = getSession()
  if (!session) {
    return null
  }

  const normalizedUser = normalizeCurrentUser(user)
  if (!normalizedUser) {
    return null
  }

  return setSession({
    ...session,
    user: normalizedUser,
    userId: normalizedUser.userId,
    username: normalizedUser.username,
    userName: normalizedUser.displayName,
    displayName: normalizedUser.displayName,
    role: normalizedUser.role,
    authRole: normalizedUser.authRole,
    rawUser: session.profile || session.rawUser || normalizedUser
  })
}

export function updateSessionRawUser(rawUser) {
  const session = getSession()
  if (!session) {
    return null
  }

  const displayName =
    rawUser?.studentName || rawUser?.teacherName || rawUser?.adminName || session.displayName

  return setSession({
    ...session,
    profile: rawUser,
    rawUser: rawUser || session.rawUser,
    user: {
      ...session.user,
      displayName
    },
    userName: displayName,
    displayName
  })
}

export function clearSession() {
  removeSessionStorage(STORAGE_KEYS.SESSION)
  removeSessionStorage(STORAGE_KEYS.ACCESS_TOKEN)
  removeSessionStorage(STORAGE_KEYS.REFRESH_TOKEN)
  removeSessionStorage(STORAGE_KEYS.CURRENT_USER)
  removeSessionStorage(STORAGE_KEYS.QUESTION_DRAFT)
  sessionState.value = null
}

export function saveQuestionDraft(question) {
  writeSessionStorage(STORAGE_KEYS.QUESTION_DRAFT, question)
}

export function readQuestionDraft() {
  return readSessionStorage(STORAGE_KEYS.QUESTION_DRAFT, null)
}

export function clearQuestionDraft() {
  removeSessionStorage(STORAGE_KEYS.QUESTION_DRAFT)
}

export function resolveHomePath(role) {
  const normalizedRole = AUTH_ROLE_TO_LEGACY_ROLE[normalizeAuthRole(role)] || String(role || '')
  if (normalizedRole === ROLES.ADMIN || normalizedRole === ROLES.TEACHER) {
    return '/console/home'
  }
  if (normalizedRole === ROLES.STUDENT) {
    return '/student/home'
  }
  return '/login'
}
