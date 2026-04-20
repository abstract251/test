import { ElNotification } from 'element-plus'

const TECH_COPY_RULES = [
  [/JWT|access token|refresh token|BCrypt|接口|后端|鉴权接口|认证返回结构|统计接口|联调|runtime|Outbox|Redis|RabbitMQ|Prometheus|Nginx|网关|主库|从库/gi, '']
]

export function sanitizeUserCopy(text, fallback = '') {
  const raw = String(text || '').trim()
  if (!raw) {
    return fallback
  }

  const sanitized = TECH_COPY_RULES.reduce((message, [pattern, replacement]) => {
    return message.replace(pattern, replacement)
  }, raw).replace(/\s{2,}/g, ' ').trim()

  return sanitized || fallback
}

export function resolveUserFacingError(source, fallback = '系统暂时无法处理这次操作，请稍后再试') {
  const response = source?.response?.data || source
  const code = Number(response?.code || source?.response?.status || 0)
  const message = sanitizeUserCopy(response?.message, '')

  if (code === 401) {
    return '登录状态已过期，请重新登录后继续'
  }
  if (code === 403) {
    return '你当前没有权限执行这项操作'
  }
  if (code === 404) {
    return '没有找到你要查看的内容'
  }
  if (code === 410) {
    return message || '这项内容已失效，暂时不能继续操作'
  }
  if (code === 429) {
    return '操作有点频繁，请稍后再试'
  }
  if (message.includes('已交卷')) {
    return '这场考试已经提交，不能再修改答案'
  }
  if (message.includes('考试已结束')) {
    return '考试时间已结束，不能继续作答'
  }
  if (message.includes('已撤销')) {
    return '这场考试已取消，暂时不能参加'
  }
  if (message.includes('留言功能已临时降级')) {
    return '留言功能暂时不可用，请稍后再试'
  }
  if (message.includes('练习功能已临时降级')) {
    return '练习功能暂时不可用，请稍后再试'
  }
  if (message.includes('题库功能已临时降级')) {
    return '题库功能暂时不可用，请稍后再试'
  }
  if (source?.message?.includes?.('Network Error')) {
    return '网络连接出现波动，请确认网络后重试'
  }

  return message || fallback
}

export function showActionError(source, fallback) {
  const message = resolveUserFacingError(source, fallback)
  ElNotification({
    title: '操作未完成',
    message,
    type: 'error',
    duration: 4500,
    position: 'top-right'
  })
  return message
}

export function showActionSuccess(message = '操作已完成') {
  ElNotification({
    title: '操作成功',
    message,
    type: 'success',
    duration: 2500,
    position: 'top-right'
  })
}

export function buildFormSummary(formRef, fallback = '请按提示补充完整后再继续') {
  const form = formRef?.value
  const fields = Array.isArray(form?.fields) ? form.fields : []
  const items = fields
    .map((field) => field?.validateMessage || '')
    .filter(Boolean)
  return {
    title: fallback,
    items: [...new Set(items)]
  }
}

export async function scrollToFirstError(formRef) {
  const form = formRef?.value
  const firstField = Array.isArray(form?.fields) ? form.fields.find((field) => field?.validateState === 'error') : null
  if (firstField?.prop && typeof form?.scrollToField === 'function') {
    await Promise.resolve()
    form.scrollToField(firstField.prop)
  }
}
