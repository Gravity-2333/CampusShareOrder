const isBlank = (value) => !String(value ?? '').trim()

export const requireValue = (value, message) => (isBlank(value) ? message : '')

export const validatePhone = (value) => {
  if (isBlank(value)) {
    return '请输入手机号'
  }

  if (!/^1\d{10}$/.test(String(value).trim())) {
    return '请输入 11 位手机号'
  }

  return ''
}

export const validatePassword = (value, label = '密码') => {
  if (isBlank(value)) {
    return `请输入${label}`
  }

  if (String(value).trim().length < 6) {
    return `${label}至少需要 6 位`
  }

  return ''
}

export const validateNickname = (value) => {
  if (isBlank(value)) {
    return '请输入昵称'
  }

  const trimmed = String(value).trim()

  if (trimmed.length < 2 || trimmed.length > 20) {
    return '昵称长度需在 2 到 20 个字符之间'
  }

  return ''
}

export const validateStudentNo = (value) => {
  if (isBlank(value)) {
    return '请输入学号'
  }

  if (String(value).trim().length < 4) {
    return '学号长度不能少于 4 位'
  }

  if (String(value).trim().length > 10) {
    return '学号长度不能超过 10 位'
  }

  if (!/^[A-Za-z0-9]+$/.test(String(value).trim())) {
    return '学号只能包含字母和数字'
  }

  return ''
}

export const validatePositiveNumber = (value, message) =>
  Number(value) > 0 ? '' : message

export const validateApiDateTime = (value, message = '时间格式应为 yyyy-MM-dd HH:mm:ss') => {
  if (isBlank(value)) {
    return message
  }

  const text = String(value).trim()
  if (!/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(text)) {
    return message
  }

  const [datePart, timePart] = text.split(' ')
  const [year, month, day] = datePart.split('-').map(Number)
  const [hours, minutes, seconds] = timePart.split(':').map(Number)
  const normalized = text.replace(' ', 'T')
  const date = new Date(normalized)
  if (
    Number.isNaN(date.getTime()) ||
    date.getFullYear() !== year ||
    date.getMonth() + 1 !== month ||
    date.getDate() !== day ||
    date.getHours() !== hours ||
    date.getMinutes() !== minutes ||
    date.getSeconds() !== seconds
  ) {
    return '请输入有效时间'
  }

  return ''
}

export const validateFutureApiDateTime = (value, message = '时间必须晚于当前时间') => {
  const formatError = validateApiDateTime(value)
  if (formatError) {
    return formatError
  }

  return new Date(String(value).trim().replace(' ', 'T')).getTime() > Date.now() ? '' : message
}

export const firstValidationError = (messages) => messages.find(Boolean) || ''

export const validationSummary = (messages) =>
  messages.filter(Boolean).join('；')
