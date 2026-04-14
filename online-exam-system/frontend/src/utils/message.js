function toPositiveNumber(value) {
  const parsed = Number(value)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null
}

export function resolveMessageId(message) {
  return toPositiveNumber(message?.id) || toPositiveNumber(message?.temp_id)
}

export function normalizeReply(reply = {}) {
  return {
    ...reply,
    messageId: toPositiveNumber(reply.messageId),
    replayId: toPositiveNumber(reply.replayId),
    replay: String(reply.replay || '').trim(),
    replayTime: reply.replayTime || ''
  }
}

export function normalizeReplyList(list = []) {
  if (!Array.isArray(list)) {
    return []
  }

  return [...list]
    .map((item) => normalizeReply(item))
    .sort((left, right) => {
      return (
        new Date(right.replayTime || 0).getTime() - new Date(left.replayTime || 0).getTime() ||
        (right.replayId || 0) - (left.replayId || 0)
      )
    })
}

export function normalizeMessage(message = {}) {
  const replies = normalizeReplyList(message.replays)
  const messageId = resolveMessageId(message)

  return {
    ...message,
    id: messageId,
    temp_id: messageId,
    title: String(message.title || '').trim(),
    content: String(message.content || '').trim(),
    time: message.time || '',
    replays: replies
  }
}

export function normalizeMessageList(list = []) {
  if (!Array.isArray(list)) {
    return []
  }

  return list.map((item) => normalizeMessage(item))
}

export function getMessageReplyCount(message) {
  return Array.isArray(message?.replays) ? message.replays.length : normalizeReplyList(message?.replays).length
}

export function hasReplies(message) {
  return getMessageReplyCount(message) > 0
}

export function getLatestReplyTime(message) {
  if (Array.isArray(message?.replays) && message.replays.length) {
    return message.replays[0]?.replayTime || ''
  }
  return normalizeReplyList(message?.replays)[0]?.replayTime || ''
}

export function getLatestActivityTime(message) {
  return getLatestReplyTime(message) || message?.time || ''
}

export function buildMessageSearchText(message) {
  return [
    resolveMessageId(message),
    message?.title,
    message?.content,
    message?.time
  ]
    .filter(Boolean)
    .join(' ')
    .toLowerCase()
}

export function getMessagePreview(message, length = 48) {
  const content = String(message?.content || '').trim()
  if (!content) {
    return '--'
  }
  return content.length > length ? `${content.slice(0, length)}...` : content
}
