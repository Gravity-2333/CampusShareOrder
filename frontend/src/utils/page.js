export const defaultPageData = () => ({
  list: [],
  page: 1,
  pageSize: 10,
  pages: 0,
  total: 0,
})

const normalizePositiveNumber = (value, fallback) => {
  const number = Number(value)
  return Number.isFinite(number) && number > 0 ? number : fallback
}

export const normalizePageData = (pageData = {}, fallback = {}) => {
  const safePageSize = normalizePositiveNumber(pageData.pageSize ?? pageData.size ?? fallback.pageSize, 10)
  const total = Number(pageData.total ?? 0)
  const pages = Number(pageData.pages ?? Math.ceil(total / safePageSize) ?? 0)
  const page = normalizePositiveNumber(pageData.page ?? pageData.current ?? fallback.page, 1)

  return {
    list: Array.isArray(pageData.list) ? pageData.list : Array.isArray(pageData.records) ? pageData.records : [],
    page,
    pageSize: safePageSize,
    pages: Number.isFinite(pages) ? pages : 0,
    total: Number.isFinite(total) ? total : 0,
  }
}

export const loadNormalizedPage = async (loader, filters = {}) => {
  const pageData = normalizePageData(await loader(filters), filters)

  if (pageData.pages > 0 && pageData.page > pageData.pages) {
    const fallbackFilters = {
      ...filters,
      page: pageData.pages,
    }
    return {
      filters: fallbackFilters,
      pageData: normalizePageData(await loader(fallbackFilters), fallbackFilters),
    }
  }

  return {
    filters,
    pageData,
  }
}
