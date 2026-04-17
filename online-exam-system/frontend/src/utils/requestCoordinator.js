/* global globalThis */

function now() {
  return Date.now()
}

const coordinatorRegistry = globalThis.__oesRequestCoordinatorRegistry ||
  (globalThis.__oesRequestCoordinatorRegistry = new Map())

export function createRequestCoordinator(_namespace, { defaultTtlMs = 0 } = {}) {
  const namespace = String(_namespace || '')
  if (namespace && coordinatorRegistry.has(namespace)) {
    return coordinatorRegistry.get(namespace)
  }

  const cache = new Map()
  const inflight = new Map()

  function getStorage() {
    if (typeof window === 'undefined' || !window.sessionStorage) {
      return null
    }
    return window.sessionStorage
  }

  function getStorageKey(key) {
    return `oes:request-cache:${namespace}:${String(key)}`
  }

  function readStoredValue(key) {
    const storage = getStorage()
    if (!storage || !namespace) {
      return null
    }

    try {
      const raw = storage.getItem(getStorageKey(key))
      if (!raw) {
        return null
      }
      const parsed = JSON.parse(raw)
      if (parsed.expiresAt > 0 && parsed.expiresAt <= now()) {
        storage.removeItem(getStorageKey(key))
        return null
      }
      return parsed.value
    } catch {
      return null
    }
  }

  function writeStoredValue(key, value, expiresAt) {
    const storage = getStorage()
    if (!storage || !namespace) {
      return
    }

    try {
      storage.setItem(getStorageKey(key), JSON.stringify({ expiresAt, value }))
    } catch {
      // Ignore storage quota and serialization errors, keep in-memory cache only.
    }
  }

  function removeStoredValue(key) {
    const storage = getStorage()
    if (!storage || !namespace) {
      return
    }
    storage.removeItem(getStorageKey(key))
  }

  function getCachedValue(key) {
    const cached = cache.get(key)
    if (!cached) {
      const storedValue = readStoredValue(key)
      if (storedValue != null) {
        return storedValue
      }
      return null
    }
    if (cached.expiresAt > 0 && cached.expiresAt <= now()) {
      cache.delete(key)
      removeStoredValue(key)
      return null
    }
    return cached.value
  }

  async function load(key, loader, { ttlMs = defaultTtlMs, force = false } = {}) {
    if (!force) {
      const cachedValue = getCachedValue(key)
      if (cachedValue != null) {
        return cachedValue
      }

      if (inflight.has(key)) {
        return inflight.get(key)
      }
    }

    const task = Promise.resolve()
      .then(loader)
      .then((value) => {
        const expiresAt = ttlMs > 0 ? now() + ttlMs : 0
        if (ttlMs > 0) {
          cache.set(key, { expiresAt, value })
          writeStoredValue(key, value, expiresAt)
        } else {
          cache.delete(key)
          removeStoredValue(key)
        }
        return value
      })
      .finally(() => {
        if (inflight.get(key) === task) {
          inflight.delete(key)
        }
      })

    inflight.set(key, task)
    return task
  }

  function invalidate(key) {
    cache.delete(key)
    inflight.delete(key)
    removeStoredValue(key)
  }

  function invalidatePrefix(prefix) {
    for (const key of [...cache.keys()]) {
      if (String(key).startsWith(prefix)) {
        cache.delete(key)
      }
    }
    for (const key of [...inflight.keys()]) {
      if (String(key).startsWith(prefix)) {
        inflight.delete(key)
      }
    }
    const storage = getStorage()
    if (storage && namespace) {
      const fullPrefix = getStorageKey(prefix)
      for (let index = storage.length - 1; index >= 0; index -= 1) {
        const key = storage.key(index)
        if (key && key.startsWith(fullPrefix)) {
          storage.removeItem(key)
        }
      }
    }
  }

  function clear() {
    cache.clear()
    inflight.clear()
    const storage = getStorage()
    if (storage && namespace) {
      const fullPrefix = getStorageKey('')
      for (let index = storage.length - 1; index >= 0; index -= 1) {
        const key = storage.key(index)
        if (key && key.startsWith(fullPrefix)) {
          storage.removeItem(key)
        }
      }
    }
  }

  const coordinator = {
    clear,
    invalidate,
    invalidatePrefix,
    load
  }

  if (namespace) {
    coordinatorRegistry.set(namespace, coordinator)
  }

  return coordinator
}
