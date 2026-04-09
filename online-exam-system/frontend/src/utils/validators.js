export const REGEX = {
  phone: /^1\d{10}$/,
  idCard: /^(?:\d{15}|\d{17}[\dXx])$/,
  password: /^\S{6,32}$/,
  institute: /^[\u4e00-\u9fa5A-Za-z0-9()（）\s-]{2,30}$/,
  major: /^[\u4e00-\u9fa5A-Za-z0-9()（）\s-]{2,30}$/
}

export function requiredRule(message, trigger = 'blur') {
  return { required: true, message, trigger }
}

export function patternRule(pattern, message, trigger = 'blur') {
  return { pattern, message, trigger }
}
