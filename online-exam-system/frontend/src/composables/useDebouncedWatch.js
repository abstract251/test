import { onBeforeUnmount, watch } from 'vue'

export function useDebouncedWatch(source, callback, delayMs = 400, options = {}) {
  let timer = null
  let latestValue
  let latestOldValue

  async function run() {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
    return callback(latestValue, latestOldValue)
  }

  function cancel() {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  const stop = watch(
    source,
    (value, oldValue) => {
      latestValue = value
      latestOldValue = oldValue
      cancel()
      timer = setTimeout(() => {
        void run()
      }, delayMs)
    },
    options
  )

  onBeforeUnmount(() => {
    cancel()
    stop()
  })

  return {
    cancel,
    flush: run,
    stop
  }
}
