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
    return '昵称长度需为 2 到 20 个字符'
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

  return ''
}

export const validatePositiveNumber = (value, message) =>
  Number(value) > 0 ? '' : message

export const firstValidationError = (messages) => messages.find(Boolean) || ''
