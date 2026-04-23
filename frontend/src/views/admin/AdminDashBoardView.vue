<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useAppStore } from '../../stores/app'
import { useAdminStore } from '../../stores/admin'
import { useUserStore } from '../../stores/user'
import { formatComplaintStatus, formatDateTime, formatOrderStatus } from '../../utils/format'

const router = useRouter()
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

const quickLinks = [
  { description: '优先处理异常订单与流转问题', label: '订单治理', path: '/admin/orders' },
  { description: '查看待处理投诉并填写处理结果', label: '投诉处理', path: '/admin/complaints' },
  { description: '处理封禁、解封和信用风险用户', label: '用户治理', path: '/admin/users' },
  { description: '查看后台操作与资金变动记录', label: '操作记录', path: '/admin/records/logs' },
]

const recentSummary = computed(() => [
  {
    label: '近期订单',
    value: adminStore.dashboardOverview.recentOrders.length,
    hint: '帮助后台快速关注当前流转中的订单',
    path: '/admin/orders',
  },
  {
    label: '近期投诉',
    value: adminStore.dashboardOverview.recentComplaints.length,
    hint: '优先查看需要处理的异常反馈',
    path: '/admin/complaints',
  },
  {
    label: '近期日志',
    value: adminStore.dashboardOverview.recentLogs.length,
    hint: '便于回看后台最近的治理动作',
    path: '/admin/records/logs',
  },
])

const loadDashboard = async () => {
  try {
    await adminStore.loadDashboardOverview()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleResetMock = async () => {
  try {
    const reset = await appStore.resetMockData()

    if (reset) {
      userStore.clearSession()
      ElMessage.success('Mock 数据已重置，请重新登录查看初始状态')
      router.push('/admin/login')
    }
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadDashboard)
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
      v-loading="adminStore.dashboardLoading"
      title="管理台概览"
      description="这里集中展示后台上下文、快捷入口与近期动态，便于联调和演示。"
    >
      <div class="detail-grid">
        <div class="surface-card detail-panel">
          <h3>当前上下文</h3>
          <ul class="detail-list">
            <li
              v-for="item in overviewRows"
              :key="item.label"
            >
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </li>
          </ul>
          <div
            v-if="appStore.apiMode === 'mock'"
            class="page-actions"
          >
            <el-button
              type="warning"
              plain
              :loading="appStore.mockResetting"
              @click="handleResetMock"
            >
              重置 Mock 数据
            </el-button>
          </div>
        </div>

        <div class="surface-card detail-panel">
          <h3>快捷入口</h3>
          <div class="admin-quick-grid">
            <button
              v-for="item in quickLinks"
              :key="item.path"
              type="button"
              class="admin-quick-card"
              @click="router.push(item.path)"
            >
              <strong>{{ item.label }}</strong>
              <span>{{ item.description }}</span>
            </button>
          </div>
        </div>
      </div>
    </PageSection>

    <div class="stats-grid">
      <StatCard
        v-for="item in recentSummary"
        :key="item.label"
        :label="item.label"
        :value="item.value"
        :hint="item.hint"
      />
    </div>

    <div class="detail-grid">
      <PageSection
        title="近期订单"
        description="帮助后台快速定位当前需要关注的订单流转。"
      >
        <div class="table-toolbar">
          <span class="table-caption">最近 {{ adminStore.dashboardOverview.recentOrders.length }} 条订单摘要。</span>
          <div class="page-actions">
            <el-button
              link
              type="primary"
              @click="router.push('/admin/orders')"
            >
              前往订单治理
            </el-button>
          </div>
        </div>
        <div
          v-if="adminStore.dashboardOverview.recentOrders.length"
          class="table-stack"
        >
          <div class="desktop-table">
            <el-table
              :data="adminStore.dashboardOverview.recentOrders"
              stripe
            >
              <el-table-column
                prop="orderNo"
                label="订单号"
              />
              <el-table-column
                prop="productName"
                label="商品"
              />
              <el-table-column label="状态">
                <template #default="{ row }">
                  <StatusTag
                    :value="row.status"
                    :text="formatOrderStatus(row.status)"
                  />
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="mobile-record-list">
            <article
              v-for="row in adminStore.dashboardOverview.recentOrders"
              :key="row.orderId || row.orderNo"
              class="surface-card mobile-record-card"
            >
              <div class="mobile-record-header">
                <div class="mobile-record-title">
                  <span>{{ row.orderNo }}</span>
                  <strong>{{ row.productName }}</strong>
                </div>
                <StatusTag
                  :value="row.status"
                  :text="formatOrderStatus(row.status)"
                />
              </div>
            </article>
          </div>
        </div>
        <p
          v-else
          class="muted-text"
        >
          暂无订单数据。
        </p>
      </PageSection>

      <PageSection
        title="近期投诉"
        description="帮助后台优先处理待响应投诉。"
      >
        <div class="table-toolbar">
          <span class="table-caption">最近 {{ adminStore.dashboardOverview.recentComplaints.length }} 条投诉摘要。</span>
          <div class="page-actions">
            <el-button
              link
              type="primary"
              @click="router.push('/admin/complaints')"
            >
              前往投诉处理
            </el-button>
          </div>
        </div>
        <div
          v-if="adminStore.dashboardOverview.recentComplaints.length"
          class="table-stack"
        >
          <div class="desktop-table">
            <el-table
              :data="adminStore.dashboardOverview.recentComplaints"
              stripe
            >
              <el-table-column
                prop="complaintNo"
                label="投诉单号"
              />
              <el-table-column
                prop="productName"
                label="商品"
              />
              <el-table-column label="状态">
                <template #default="{ row }">
                  <StatusTag
                    :value="row.status"
                    :text="formatComplaintStatus(row.status)"
                  />
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="mobile-record-list">
            <article
              v-for="row in adminStore.dashboardOverview.recentComplaints"
              :key="row.complaintId || row.complaintNo"
              class="surface-card mobile-record-card"
            >
              <div class="mobile-record-header">
                <div class="mobile-record-title">
                  <span>{{ row.complaintNo }}</span>
                  <strong>{{ row.productName }}</strong>
                </div>
                <StatusTag
                  :value="row.status"
                  :text="formatComplaintStatus(row.status)"
                />
              </div>
            </article>
          </div>
        </div>
        <p
          v-else
          class="muted-text"
        >
          暂无投诉数据。
        </p>
      </PageSection>
    </div>

    <PageSection
      title="近期日志"
      description="展示后台最近的关键操作，方便回看治理动作。"
    >
      <div class="table-toolbar">
        <span class="table-caption">最近 {{ adminStore.dashboardOverview.recentLogs.length }} 条后台日志摘要。</span>
        <div class="page-actions">
          <el-button
            link
            type="primary"
            @click="router.push('/admin/records/logs')"
          >
            查看完整日志
          </el-button>
        </div>
      </div>
      <div
        v-if="adminStore.dashboardOverview.recentLogs.length"
        class="table-stack"
      >
        <div class="desktop-table">
          <el-table
            :data="adminStore.dashboardOverview.recentLogs"
            stripe
          >
            <el-table-column
              prop="action"
              label="动作"
            />
            <el-table-column
              prop="operatorName"
              label="操作人"
            />
            <el-table-column
              prop="targetNo"
              label="目标编号"
            />
            <el-table-column label="时间">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="mobile-record-list">
          <article
            v-for="(row, index) in adminStore.dashboardOverview.recentLogs"
            :key="`${row.action}-${row.createdAt}-${index}`"
            class="surface-card mobile-record-card"
          >
            <div class="mobile-record-title">
              <span>{{ formatDateTime(row.createdAt) }}</span>
              <strong>{{ row.action || '--' }}</strong>
            </div>
            <ul class="mobile-record-fields">
              <li><span>操作人</span><strong>{{ row.operatorName || '--' }}</strong></li>
              <li><span>目标编号</span><strong>{{ row.targetNo || '--' }}</strong></li>
            </ul>
          </article>
        </div>
      </div>
      <p
        v-else
        class="muted-text"
      >
        暂无日志数据。
      </p>
    </PageSection>
  </div>
</template>
