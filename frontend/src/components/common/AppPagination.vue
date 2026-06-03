<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  page: {
    default: 1,
    type: Number,
  },
  pageSize: {
    default: 10,
    type: Number,
  },
  pages: {
    default: 0,
    type: Number,
  },
  total: {
    default: 0,
    type: Number,
  },
})

const emit = defineEmits(['change'])
const currentPage = ref(props.page)
const isMobileViewport = ref(false)

const paginationLayout = computed(() => (isMobileViewport.value ? 'prev, pager, next' : 'prev, pager, next'))
const pagerCount = computed(() => (isMobileViewport.value ? 5 : 7))

watch(
  () => props.page,
  (page) => {
    currentPage.value = page
  },
)

const handleCurrentChange = (nextPage) => {
  currentPage.value = nextPage
  emit('change', {
    page: nextPage,
    pageSize: props.pageSize,
  })
}

let mediaQuery = null
let handleChange = null

onMounted(() => {
  if (typeof window === 'undefined') {
    return
  }

  mediaQuery = window.matchMedia('(max-width: 640px)')
  handleChange = (event) => {
    isMobileViewport.value = event.matches
  }

  isMobileViewport.value = mediaQuery.matches
  mediaQuery.addEventListener('change', handleChange)
})

onBeforeUnmount(() => {
  if (mediaQuery && handleChange) {
    mediaQuery.removeEventListener('change', handleChange)
  }
})
</script>

<template>
  <div
    v-if="total > 0"
    class="pagination-bar surface-card"
  >
    <p class="pagination-summary">
      共 <strong>{{ total }}</strong> 条，当前第 <strong>{{ page }}</strong> / {{ pages || 1 }} 页
    </p>
    <el-pagination
      v-model:current-page="currentPage"
      background
      :layout="paginationLayout"
      :pager-count="pagerCount"
      :page-size="pageSize"
      :total="total"
      @current-change="handleCurrentChange"
    />
  </div>
</template>
