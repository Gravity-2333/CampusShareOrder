export const normalizeId = (value) => {
  if (value === null || value === undefined) {
    return ''
  }

  return String(value).trim()
}

export const isValidId = (value) => normalizeId(value).length > 0

export const sameId = (left, right) => normalizeId(left) === normalizeId(right)
