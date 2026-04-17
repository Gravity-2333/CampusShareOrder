<script setup>
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

const handleCurrentChange = (nextPage) => {
  emit('change', {
    page: nextPage,
    pageSize: props.pageSize,
  })
}
</script>

<template>
  <div v-if="total > 0" class="pagination-bar surface-card">
    <p class="pagination-summary">
      共 <strong>{{ total }}</strong> 条，当前第 <strong>{{ page }}</strong> / {{ pages || 1 }} 页
    </p>
    <el-pagination
      background
      layout="prev, pager, next"
      :current-page="page"
      :page-size="pageSize"
      :total="total"
      @current-change="handleCurrentChange"
    />
  </div>
</template>
