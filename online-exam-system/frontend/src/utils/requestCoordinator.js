function now() {
  return Date.now()
}

export function createRequestCoordinator(_namespace, { defaultTtlMs = 0 } = {}) {
  const cache = new Map()
  const inflight = new Map()

  function getCachedValue(key) {
    const cached = cache.get(key)
    if (!cached) {
      return null
    }
    if (cached.expiresAt > 0 && cached.expiresAt <= now()) {
      cache.delete(key)
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
        } else {
          cache.delete(key)
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
  }

  function clear() {
    cache.clear()
    inflight.clear()
  }

  return {
    clear,
    invalidate,
    invalidatePrefix,
    load
  }
}
