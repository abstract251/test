export const REGEX = {
  phone: /^1\d{10}$/,
  idCard: /^(?:\d{15}|\d{17}[\dXx])$/,
  password: /^\S{6,32}$/
}

export function requiredRule(message, trigger = 'blur') {
  return { required: true, message, trigger }
}

export function patternRule(pattern, message, trigger = 'blur') {
  return { pattern, message, trigger }
}
