import { normalizeLoginUser } from '@/utils/adapters'
import { ROLES, STORAGE_KEYS } from '@/utils/constants'
import { readStorage, removeSessionStorage, removeStorage, writeStorage, writeSessionStorage, readSessionStorage } from '@/utils/storage'

export function getSession() {
  return readStorage(STORAGE_KEYS.SESSION, null)
}

export function hasSession() {
  return Boolean(getSession())
}

export function setSessionFromUser(user) {
  const normalized = normalizeLoginUser(user)
  if (!normalized) {
    return null
  }

  const payload = {
    ...normalized,
    loginAt: Date.now()
  }
  writeStorage(STORAGE_KEYS.SESSION, payload)
  return payload
}

export function updateSessionRawUser(rawUser) {
  const session = getSession()
  if (!session) {
    return null
  }

  const updated = {
    ...session,
    rawUser,
    userName: rawUser.studentName || rawUser.teacherName || rawUser.adminName || session.userName
  }
  writeStorage(STORAGE_KEYS.SESSION, updated)
  return updated
}

export function clearSession() {
  removeStorage(STORAGE_KEYS.SESSION)
  removeSessionStorage(STORAGE_KEYS.QUESTION_DRAFT)
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
  if (role === ROLES.ADMIN || role === ROLES.TEACHER) {
    return '/console/home'
  }
  if (role === ROLES.STUDENT) {
    return '/student/home'
  }
  return '/login'
}
