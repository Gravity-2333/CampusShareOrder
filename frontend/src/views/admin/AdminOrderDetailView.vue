<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useAdminStore } from '../../stores/admin'
import {
  formatCurrency,
  formatDateTime,
  formatJoinStatus,
  formatOrderStatus,
  formatPayStatus,
  formatReceiveStatus,
  formatRole,
} from '../../utils/format'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const detail = computed(() => adminStore.orderDetail)

const stats = computed(() => {
  if (!detail.value) {
    return []
  }

  return [
    {
      label: '订单状态',
      value: formatOrderStatus(detail.value.basicInfo.status),
      hint: '展示订单金额、人数与处理状态',
    },
    {
      label: '当前成员',
      value: `${detail.value.basicInfo.currentMemberCount}/${detail.value.basicInfo.totalMemberCount}`,
      hint: '方便后台快速判断是否满员',
    },
    {
      label: '投诉数量',
      value: detail.value.complaintInfo.complaintCount,
      hint: '后台治理重点关注投诉风险',
    },
  ]
})

const loadDetail = async (orderId = route.params.orderId) => {
  if (!orderId) {
    return
  }

  try {
    await adminStore.loadOrderDetail(orderId)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleCancel = async () => {
  if (!detail.value) {
    return
  }

  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      cancelButtonText: '取消',
      confirmButtonText: '确认取消',
      inputPlaceholder: '例如：投诉成立，后台介入取消',
      inputValidator: (inputValue) => {
        if (!inputValue?.trim()) {
          return '请输入取消原因'
        }

        return true
      },
    })

    await adminStore.cancelOrder(detail.value.basicInfo.orderId, { reason: value.trim() })
    ElMessage.success('订单已取消')
  } catch (error) {
    if (error === 'cancel' || error?.message === 'cancel') {
      return
    }

    ElMessage.error(error.message || '取消失败，请稍后重试')
  }
}

watch(
  () => route.params.orderId,
  (orderId, previousOrderId) => {
    if (orderId && orderId !== previousOrderId) {
      loadDetail(orderId)
    }
  },
)

onMounted(() => {
  loadDetail()
})
</script>

<template>
  <div class="stack-page">
    <div class="stats-grid">
      <StatCard
        v-for="item in stats"
        :key="item.label"
        :label="item.label"
        :value="item.value"
        :hint="item.hint"
      />
    </div>

    <PageSection
      v-loading="adminStore.orderDetailLoading"
      title="订单详情"
      description="帮助管理员查看订单全貌、成员进度和异常处理线索。"
    >
      <template v-if="detail">
        <div class="card-header-row">
          <div>
            <p class="section-kicker">
              {{ detail.basicInfo.orderNo }}
            </p>
            <h2>{{ detail.basicInfo.productName }}</h2>
          </div>
          <StatusTag
            :value="detail.basicInfo.status"
            :text="formatOrderStatus(detail.basicInfo.status)"
          />
        </div>

        <div class="detail-grid">
          <div class="surface-card detail-panel">
            <h3>基础信息</h3>
            <ul class="detail-list">
              <li><span>发起人</span><strong>{{ detail.initiatorInfo.nickname }}</strong></li>
              <li><span>取货点</span><strong>{{ detail.basicInfo.pickupPoint }}</strong></li>
              <li><span>截止时间</span><strong>{{ formatDateTime(detail.basicInfo.deadlineAt) }}</strong></li>
              <li><span>预计送达截止</span><strong>{{ formatDateTime(detail.basicInfo.expectedDeliveryEndAt) }}</strong></li>
              <li><span>投诉通道</span><strong>{{ detail.basicInfo.complaintOpened ? '已开放' : '未开放' }}</strong></li>
            </ul>

            <div class="detail-note">
              <span>商品备注</span>
              <p>{{ detail.basicInfo.productDesc || '暂无备注说明' }}</p>
            </div>
          </div>

          <div class="surface-card detail-panel">
            <h3>支付与凭证</h3>
            <ul class="detail-list">
              <li><span>预计总额</span><strong>{{ formatCurrency(detail.paymentSummary.estimatedTotalAmount) }}</strong></li>
              <li><span>实付总额</span><strong>{{ formatCurrency(detail.paymentSummary.actualTotalAmount) }}</strong></li>
              <li><span>累计支付</span><strong>{{ formatCurrency(detail.paymentSummary.totalPaidAmount) }}</strong></li>
              <li><span>退款总额</span><strong>{{ formatCurrency(detail.paymentSummary.refundAmountTotal) }}</strong></li>
              <li><span>凭证上传时间</span><strong>{{ formatDateTime(detail.receiptInfo?.uploadedAt) }}</strong></li>
            </ul>
          </div>
        </div>

        <PageSection
          title="成员列表"
          description="后台可直接查看角色、支付和收货状态。"
        >
          <div class="desktop-table">
            <el-table
              :data="detail.memberList"
              stripe
            >
              <el-table-column
                prop="nickname"
                label="成员"
              />
              <el-table-column label="角色">
                <template #default="{ row }">
                  {{ formatRole(row.role) }}
                </template>
              </el-table-column>
              <el-table-column label="加入状态">
                <template #default="{ row }">
                  {{ formatJoinStatus(row.joinStatus) }}
                </template>
              </el-table-column>
              <el-table-column label="支付状态">
                <template #default="{ row }">
                  {{ formatPayStatus(row.payStatus) }}
                </template>
              </el-table-column>
              <el-table-column label="收货状态">
                <template #default="{ row }">
                  {{ formatReceiveStatus(row.receiveStatus) }}
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="mobile-record-list">
            <article
              v-for="(row, index) in detail.memberList"
              :key="`${row.nickname}-${index}`"
              class="surface-card mobile-record-card"
            >
              <div class="mobile-record-title">
                <span>{{ formatRole(row.role) }}</span>
                <strong>{{ row.nickname || '--' }}</strong>
              </div>
              <ul class="mobile-record-fields">
                <li><span>加入状态</span><strong>{{ formatJoinStatus(row.joinStatus) }}</strong></li>
                <li><span>支付状态</span><strong>{{ formatPayStatus(row.payStatus) }}</strong></li>
                <li><span>收货状态</span><strong>{{ formatReceiveStatus(row.receiveStatus) }}</strong></li>
              </ul>
            </article>
          </div>
        </PageSection>

        <div class="detail-grid">
          <PageSection
            title="投诉信息"
            description="后台查看该订单当前的投诉风险情况。"
          >
            <ul class="detail-list">
              <li><span>投诉数量</span><strong>{{ detail.complaintInfo.complaintCount }}</strong></li>
              <li><span>投诉通道</span><strong>{{ detail.complaintInfo.complaintOpened ? '已开放' : '未开放' }}</strong></li>
            </ul>
          </PageSection>

          <PageSection
            title="时间线"
            description="按时间顺序查看订单关键事件。"
          >
            <el-timeline>
              <el-timeline-item
                v-for="item in detail.timeline"
                :key="`${item.action}-${item.at}`"
                :timestamp="formatDateTime(item.at)"
              >
                {{ item.description }}
              </el-timeline-item>
            </el-timeline>
          </PageSection>
        </div>

        <div class="page-actions wrap-actions">
          <el-button @click="router.push('/admin/orders')">
            返回订单管理
          </el-button>
          <el-button
            v-if="detail.basicInfo.status !== 'CANCELED' && detail.basicInfo.status !== 'COMPLETED'"
            type="danger"
            :loading="adminStore.submitting"
            @click="handleCancel"
          >
            取消订单
          </el-button>
        </div>
      </template>
    </PageSection>
  </div>
</template>
