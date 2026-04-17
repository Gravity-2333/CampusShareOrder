<script setup>
import { computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import { useAppStore } from '../../stores/app'
import { useAdminStore } from '../../stores/admin'
import { useUserStore } from '../../stores/user'

const appStore = useAppStore()
const adminStore = useAdminStore()
const userStore = useUserStore()

const cards = computed(() => [
  { hint: 'GET /api/admin/users', label: '用户总数', value: adminStore.metrics.users },
  { hint: 'GET /api/admin/orders', label: '订单总数', value: adminStore.metrics.orders },
  { hint: 'GET /api/admin/complaints', label: '投诉总数', value: adminStore.metrics.complaints },
])

const overviewRows = computed(() => [
  { label: '当前模式', value: appStore.apiMode },
  { label: '当前身份', value: userStore.displayName },
  { label: '角色', value: userStore.session.role || '--' },
  { label: '管理员账号', value: userStore.session.username || '--' },
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
  <div class="stack-page">
    <div class="stats-grid">
      <StatCard
        v-for="item in cards"
        :key="item.label"
        :label="item.label"
        :value="item.value"
        :hint="item.hint"
      />
    </div>

    <PageSection
      title="管理台概览"
      description="这里展示当前运行模式、身份信息以及管理端核心计数，方便演示和联调时快速确认上下文。"
    >
      <div class="detail-grid">
        <div class="surface-card detail-panel">
          <h3>当前上下文</h3>
          <ul class="detail-list">
            <li v-for="item in overviewRows" :key="item.label">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </li>
          </ul>
        </div>

        <div class="surface-card detail-panel">
          <h3>当前阶段说明</h3>
          <ul class="detail-list">
            <li><span>分支</span><strong>app-shell</strong></li>
            <li><span>数据源</span><strong>{{ appStore.apiMode }}</strong></li>
            <li><span>当前目标</span><strong>前端主干与契约展示基座</strong></li>
            <li><span>下一重点</span><strong>订单详情与动作闭环深化</strong></li>
          </ul>
        </div>
      </div>
    </PageSection>
  </div>
</template>
