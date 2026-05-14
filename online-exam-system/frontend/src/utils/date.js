import dayjs from 'dayjs'

export function resolveExamDateTime(exam) {
  if (!exam) {
    return ''
  }
  return exam.examStartAt || exam.examDate || ''
}

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

export function formatDurationFromNow(value) {
  if (!value) {
    return '--'
  }
  const target = dayjs(value)
  if (!target.isValid()) {
    return '--'
  }
  const diffSeconds = target.diff(dayjs(), 'second')
  if (diffSeconds <= 0) {
    return '00:00:00'
  }
  const hours = Math.floor(diffSeconds / 3600)
  const minutes = Math.floor((diffSeconds % 3600) / 60)
  const seconds = diffSeconds % 60
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}
