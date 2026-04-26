import { STORAGE_KEYS } from '@/utils/constants'

export function readStorage(key, fallback = null) {
  const raw = window.localStorage.getItem(key)
  if (!raw) {
    return fallback
  }

  try {
    return JSON.parse(raw)
  } catch (error) {
    return fallback
  }
}

export function writeStorage(key, value) {
  window.localStorage.setItem(key, JSON.stringify(value))
}

export function removeStorage(key) {
  window.localStorage.removeItem(key)
}

export function readSessionStorage(key, fallback = null) {
  const raw = window.sessionStorage.getItem(key)
  if (!raw) {
    return fallback
  }

  try {
    return JSON.parse(raw)
  } catch (error) {
    return fallback
  }
}

export function writeSessionStorage(key, value) {
  window.sessionStorage.setItem(key, JSON.stringify(value))
}

export function removeSessionStorage(key) {
  window.sessionStorage.removeItem(key)
}

export function readAppSession() {
  return readSessionStorage(STORAGE_KEYS.SESSION, null)
}
