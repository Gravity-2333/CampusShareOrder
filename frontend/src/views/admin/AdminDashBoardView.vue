<script setup>
import { computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

import StatCard from '../../components/common/StatCard.vue'
import { useAdminStore } from '../../stores/admin'

const adminStore = useAdminStore()

const cards = computed(() => [
  { hint: 'GET /api/admin/users', label: '用户总数', value: adminStore.metrics.users },
  { hint: 'GET /api/admin/orders', label: '订单总数', value: adminStore.metrics.orders },
  { hint: 'GET /api/admin/complaints', label: '投诉总数', value: adminStore.metrics.complaints },
])

onMounted(async () => {
  try {
    await adminStore.loadDashboardMetrics()
  } catch (error) {
    ElMessage.error(error.message)
  }
})
</script>

<template>
  <div class="stats-grid">
    <StatCard v-for="item in cards" :key="item.label" :label="item.label" :value="item.value" :hint="item.hint" />
  </div>
</template>
