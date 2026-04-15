import dayjs from 'dayjs'

export function formatDateTime(value, pattern = 'YYYY-MM-DD HH:mm') {
  if (!value) {
    return '--'
  }
  return dayjs(value).format(pattern)
}

export function toBackendDateTime(value) {
  if (!value) {
    return ''
  }
  return dayjs(value).second(0).format('YYYY-MM-DD HH:mm:00')
}

export function toPickerDateTime(value) {
  if (!value) {
    return ''
  }
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}

export function isExamStarted(value) {
  if (!value) {
    return false
  }
  return dayjs().isAfter(dayjs(value)) || dayjs().isSame(dayjs(value))
}

export function isUpcoming(value) {
  if (!value) {
    return false
  }
  return dayjs(value).isAfter(dayjs())
}
