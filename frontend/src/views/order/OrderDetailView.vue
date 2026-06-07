<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElImage, ElMessage, ElMessageBox } from 'element-plus'

import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useOrderStore } from '../../stores/order'
import { useUserStore } from '../../stores/user'
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
const orderStore = useOrderStore()
const userStore = useUserStore()

const receiptDialogVisible = ref(false)
const uploadDialogVisible = ref(false)
const activeReceiptUrl = ref('')
const receiptFileList = ref([])
const receiptForm = reactive({
  actualTotalAmount: '',
  expectedDeliveryEndAt: '',
  expectedDeliveryStartAt: '',
  image: null,
})
const now = ref(Date.now())
let countdownTimer = null

const detail = computed(() => orderStore.detail)
const currentOrderId = computed(() => route.params.orderId)
const normalizedOrderId = computed(() => Number(currentOrderId.value || 0))
const isValidOrderId = computed(() => Number.isInteger(normalizedOrderId.value) && normalizedOrderId.value > 0)

const activeMemberList = computed(
  () => detail.value?.memberList?.filter((member) => member.joinStatus === 'ACTIVE') || [],
)

const paidMemberCount = computed(
  () => activeMemberList.value.filter((member) => member.payStatus === 'PAID').length,
)

const deadlineRemainingText = computed(() => {
  const deadlineAt = detail.value?.basicInfo?.deadlineAt
  if (!deadlineAt) {
    return '--'
  }

  if (detail.value?.basicInfo?.status !== 'OPEN') {
    return '已结束'
  }

  const remainingMs = new Date(String(deadlineAt).replace(' ', 'T')).getTime() - now.value
  if (Number.isNaN(remainingMs) || remainingMs <= 0) {
    return '已截止'
  }

  const totalSeconds = Math.floor(remainingMs / 1000)
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60

  if (hours > 0) {
    return `${hours}小时${String(minutes).padStart(2, '0')}分${String(seconds).padStart(2, '0')}秒`
  }

  return `${minutes}分${String(seconds).padStart(2, '0')}秒`
})

const detailErrorText = computed(() => {
  if (!isValidOrderId.value) {
    return '当前路由中的订单 ID 无效，请返回大厅重新进入详情页。'
  }

  return orderStore.detailError || '当前未能加载到订单详情，请稍后重试。'
})

const getMemberLatestEvent = (member) => {
  if (!member) {
    return '--'
  }

  if (member.receivedAt) {
    return `最近收货：${formatDateTime(member.receivedAt)}`
  }

  if (member.exitedAt) {
    return `最近退出：${formatDateTime(member.exitedAt)}`
  }

  if (member.paidAt) {
    return `最近支付：${formatDateTime(member.paidAt)}`
  }

  if (member.joinedAt) {
    return `最近加入：${formatDateTime(member.joinedAt)}`
  }

  return '--'
}

const stats = computed(() => {
  if (!detail.value) {
    return []
  }

  return [
    {
      hint: '当前拼单所处阶段',
      label: '订单状态',
      value: formatOrderStatus(detail.value.basicInfo.status),
    },
    {
      hint: '根据当前订单状态汇总',
      label: '可执行动作',
      value: Object.values(detail.value.actionFlags).filter(Boolean).length,
    },
    {
      hint: '仅统计 ACTIVE 成员',
      label: '支付进度',
      value: `${paidMemberCount.value}/${activeMemberList.value.length || 0}`,
    },
    {
      hint: '当前成员数 / 目标成员数',
      label: '成员进度',
      value: `${detail.value.basicInfo.currentMemberCount}/${detail.value.basicInfo.totalMemberCount}`,
    },
    {
      hint: '招募截止倒计时',
      label: '剩余时间',
      value: deadlineRemainingText.value,
    },
  ]
})

const loadDetail = async (orderId = currentOrderId.value) => {
  if (!isValidOrderId.value) {
    orderStore.detail = null
    orderStore.detailError = '当前路由中的订单 ID 无效，请返回大厅重新进入详情页。'
    return
  }

  try {
    await orderStore.loadOrderDetail(orderId)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const confirmAction = async (action) => {
  const confirmMap = {
    cancel: '仅在没有其他参与者时可取消。确认后订单将关闭，已支付金额会全额退款，是否继续？',
    delivered: '确认已送达后，订单会进入待收货阶段，是否继续？',
    exit: '退出拼单后将失去当前成员资格，是否继续？',
    join: '确认加入该拼单吗？',
    pay: '确认将当前账号标记为已支付吗？',
    received: '确认已经收货吗？',
  }

  if (!confirmMap[action]) {
    return true
  }

  try {
    await ElMessageBox.confirm(confirmMap[action], '操作确认', {
      cancelButtonText: '取消',
      confirmButtonText: '确认',
      type: 'warning',
    })
    return true
  } catch {
    return false
  }
}

const parseApiDateTime = (value) => new Date(String(value || '').trim().replace(' ', 'T')).getTime()

const resetReceiptForm = () => {
  receiptForm.actualTotalAmount = ''
  receiptForm.expectedDeliveryEndAt = ''
  receiptForm.expectedDeliveryStartAt = ''
  receiptForm.image = null
  receiptFileList.value = []
}

const handleReceiptFileChange = (uploadFile) => {
  const file = uploadFile?.raw
  if (!file) return

  const isAllowedType = ['image/jpeg', 'image/png'].includes(file.type)
  const hasAllowedExtension = /\.(jpe?g|png)$/i.test(file.name)
  if (!isAllowedType || !hasAllowedExtension) {
    receiptFileList.value = []
    receiptForm.image = null
    ElMessage.warning('仅支持 JPG/PNG')
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    receiptFileList.value = []
    receiptForm.image = null
    ElMessage.warning('文件大小不能超过10MB')
    return
  }
  receiptForm.image = file
}

const submitReceipt = async () => {
  if (!receiptForm.image) {
    ElMessage.warning('请选择订单截图')
    return
  }
  const amount = Number(receiptForm.actualTotalAmount)
  if (!Number.isFinite(amount) || amount <= 0) {
    ElMessage.warning('实际总金额必须大于0')
    return
  }
  const estimatedTotalAmount = Number(detail.value?.basicInfo?.estimatedTotalAmount || 0)
  if (estimatedTotalAmount > 0 && amount > estimatedTotalAmount) {
    ElMessage.warning('实际总金额不能高于预计总金额')
    return
  }
  if (!receiptForm.expectedDeliveryStartAt || !receiptForm.expectedDeliveryEndAt) {
    ElMessage.warning('请填写预计送达时间区间')
    return
  }
  const startAt = parseApiDateTime(receiptForm.expectedDeliveryStartAt)
  const endAt = parseApiDateTime(receiptForm.expectedDeliveryEndAt)
  if (!Number.isFinite(startAt) || !Number.isFinite(endAt)) {
    ElMessage.warning('预计送达时间格式不正确')
    return
  }
  if (startAt <= Date.now()) {
    ElMessage.warning('预计开始送达时间必须晚于当前时间')
    return
  }
  if (endAt <= startAt) {
    ElMessage.warning('预计最晚送达时间必须晚于开始送达时间')
    return
  }

  try {
    await orderStore.runDetailAction(currentOrderId.value, 'upload', {
      actualTotalAmount: amount,
      expectedDeliveryEndAt: receiptForm.expectedDeliveryEndAt,
      expectedDeliveryStartAt: receiptForm.expectedDeliveryStartAt,
      image: receiptForm.image,
    })
    uploadDialogVisible.value = false
    resetReceiptForm()
    ElMessage.success('凭证上传成功')
  } catch (error) {
    ElMessage.error(error.message || '凭证上传失败，请稍后重试')
  }
}

const openReceiptDialog = () => {
  if (!detail.value?.receiptInfo?.receiptImageUrl) {
    ElMessage.warning('当前订单暂无凭证图片')
    return
  }

  activeReceiptUrl.value = detail.value.receiptInfo.receiptImageUrl
  receiptDialogVisible.value = true
}

const navigateToComplaint = () => {
  if (!detail.value) {
    return
  }

  if (detail.value.actionFlags.canCreateComplaint) {
    const orderId = Number(detail.value.basicInfo.orderId || 0)
    if (!Number.isInteger(orderId) || orderId <= 0) {
      ElMessage.warning('当前订单 ID 无效，请返回详情页重新加载')
      return
    }

    router.push(`/complaints/create?orderId=${orderId}`)
    return
  }

  if (detail.value.complaintInfo.myComplaintId) {
    router.push(`/complaints/${detail.value.complaintInfo.myComplaintId}`)
  }
}

const runAction = async (action) => {
  if (orderStore.submitting && action !== 'viewReceipt') {
    return
  }

  if (!isValidOrderId.value) {
    ElMessage.warning('当前订单 ID 无效，请返回大厅重新进入详情页')
    return
  }

  try {
    let payload = {}

    if (action === 'viewReceipt') {
      openReceiptDialog()
      return
    }

    if (['join', 'pay'].includes(action) && !userStore.session.isVerified) {
      ElMessage.warning('请先完成实名认证，再继续拼单操作')
      router.push('/verify-student')
      return
    }

    if (!(await confirmAction(action))) {
      return
    }

    if (action === 'upload') {
      uploadDialogVisible.value = true
      return
    }

    await orderStore.runDetailAction(currentOrderId.value, action, payload)
    const successMessageMap = {
      cancel: '取消拼单成功，已支付金额已全额退款',
      exit: '退出拼单成功，已支付金额已全额退款',
      join: '加入拼单成功',
      pay: '支付成功',
      delivered: '已确认送达',
      received: '已确认收货',
    }
    ElMessage.success(successMessageMap[action] || '操作成功')
  } catch (error) {
    if (error === 'cancel' || error?.message === 'cancel') {
      return
    }

    ElMessage.error(error.message || '操作失败，请稍后重试')
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
  countdownTimer = window.setInterval(() => {
    now.value = Date.now()
  }, 1000)
  loadDetail()
})

onBeforeUnmount(() => {
  if (countdownTimer) {
    window.clearInterval(countdownTimer)
  }
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

    <div
      v-loading="orderStore.detailLoading"
      class="section"
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
              <li><span>取货点</span><strong>{{ detail.basicInfo.pickupPoint }}</strong></li>
              <li><span>截止时间</span><strong>{{ formatDateTime(detail.basicInfo.deadlineAt) }}</strong></li>
              <li><span>发起人</span><strong>{{ detail.initiatorInfo.nickname }}</strong></li>
              <li><span>联系方式</span><strong>{{ detail.initiatorInfo.phoneMasked || '--' }}</strong></li>
              <li><span>学号</span><strong>{{ detail.initiatorInfo.studentNoMasked || '--' }}</strong></li>
              <li><span>查看身份</span><strong>{{ formatRole(detail.viewerRoleInOrder) }}</strong></li>
              <li>
                <span>成员进度</span>
                <strong>{{ detail.basicInfo.currentMemberCount }}/{{ detail.basicInfo.totalMemberCount }}</strong>
              </li>
            </ul>

            <div class="detail-note">
              <span>商品备注</span>
              <p>{{ detail.basicInfo.productDesc || '暂无备注说明' }}</p>
            </div>
          </div>

          <div class="surface-card detail-panel">
            <h3>当前成员信息</h3>
            <ul
              v-if="detail.currentUserMember"
              class="detail-list"
            >
              <li><span>我的角色</span><strong>{{ formatRole(detail.currentUserMember.myRole) }}</strong></li>
              <li>
                <span>加入状态</span>
                <StatusTag
                  :value="detail.currentUserMember.joinStatus"
                  :text="formatJoinStatus(detail.currentUserMember.joinStatus)"
                />
              </li>
              <li>
                <span>支付状态</span>
                <StatusTag
                  :value="detail.currentUserMember.payStatus"
                  :text="formatPayStatus(detail.currentUserMember.payStatus)"
                />
              </li>
              <li><span>应付金额</span><strong>{{ formatCurrency(detail.currentUserMember.payAmount) }}</strong></li>
              <li>
                <span>收货状态</span>
                <StatusTag
                  :value="detail.currentUserMember.receiveStatus"
                  :text="formatReceiveStatus(detail.currentUserMember.receiveStatus)"
                />
              </li>
              <li><span>加入时间</span><strong>{{ formatDateTime(detail.currentUserMember.joinedAt) }}</strong></li>
              <li><span>支付时间</span><strong>{{ formatDateTime(detail.currentUserMember.paidAt) }}</strong></li>
              <li><span>收货时间</span><strong>{{ formatDateTime(detail.currentUserMember.receivedAt) }}</strong></li>
              <li><span>退出时间</span><strong>{{ formatDateTime(detail.currentUserMember.exitedAt) }}</strong></li>
              <li>
                <span>退款合计</span>
                <strong>{{ formatCurrency(detail.currentUserMember.refundAmountTotal) }}</strong>
              </li>
            </ul>
            <p
              v-else
              class="muted-text"
            >
              当前账号尚未加入该订单。
            </p>
          </div>
        </div>

        <PageSection
          title="成员列表"
          description="查看成员加入、支付、收货和最近事件，快速判断当前进展。"
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
                  <div class="member-role-cell">
                    <span>{{ formatRole(row.role) }}</span>
                    <el-tag
                      v-if="row.isCreator"
                      size="small"
                      type="warning"
                      effect="light"
                    >
                      团长
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="加入状态">
                <template #default="{ row }">
                  <StatusTag
                    :value="row.joinStatus"
                    :text="formatJoinStatus(row.joinStatus)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="支付状态">
                <template #default="{ row }">
                  <StatusTag
                    :value="row.payStatus"
                    :text="formatPayStatus(row.payStatus)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="收货状态">
                <template #default="{ row }">
                  <StatusTag
                    :value="row.receiveStatus"
                    :text="formatReceiveStatus(row.receiveStatus)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="应付金额">
                <template #default="{ row }">
                  {{ formatCurrency(row.payAmount) }}
                </template>
              </el-table-column>
              <el-table-column label="退款合计">
                <template #default="{ row }">
                  {{ formatCurrency(row.refundAmountTotal) }}
                </template>
              </el-table-column>
              <el-table-column label="最近事件">
                <template #default="{ row }">
                  {{ getMemberLatestEvent(row) }}
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
              <div class="mobile-record-header">
                <div class="mobile-record-title">
                  <span>
                    {{ formatRole(row.role) }}
                    <el-tag
                      v-if="row.isCreator"
                      size="small"
                      type="warning"
                      effect="light"
                    >
                      团长
                    </el-tag>
                  </span>
                  <strong>{{ row.nickname || '--' }}</strong>
                </div>
                <StatusTag
                  :value="row.payStatus"
                  :text="formatPayStatus(row.payStatus)"
                />
              </div>
              <ul class="mobile-record-fields">
                <li><span>加入状态</span><strong>{{ formatJoinStatus(row.joinStatus) }}</strong></li>
                <li><span>收货状态</span><strong>{{ formatReceiveStatus(row.receiveStatus) }}</strong></li>
                <li><span>应付金额</span><strong>{{ formatCurrency(row.payAmount) }}</strong></li>
                <li><span>退款合计</span><strong>{{ formatCurrency(row.refundAmountTotal) }}</strong></li>
                <li><span>最近事件</span><strong>{{ getMemberLatestEvent(row) }}</strong></li>
              </ul>
            </article>
          </div>
        </PageSection>

        <div class="page-actions wrap-actions detail-actions-bar">
          <el-button @click="router.push('/orders')">
            返回大厅
          </el-button>
          <el-button
            plain
            @click="router.push('/my-orders')"
          >
            我的拼单
          </el-button>
          <el-button
            plain
            :loading="orderStore.detailLoading"
            @click="loadDetail()"
          >
            刷新详情
          </el-button>
          <el-button
            v-if="detail.actionFlags.canJoin"
            type="primary"
            :loading="orderStore.submitting"
            @click="runAction('join')"
          >
            加入拼单
          </el-button>
          <el-button
            v-if="detail.actionFlags.canCancel"
            type="danger"
            plain
            :loading="orderStore.submitting"
            @click="runAction('cancel')"
          >
            取消拼单
          </el-button>
          <el-button
            v-if="detail.actionFlags.canExit"
            type="warning"
            plain
            :loading="orderStore.submitting"
            @click="runAction('exit')"
          >
            退出拼单
          </el-button>
          <el-button
            v-if="detail.actionFlags.canPay"
            type="primary"
            :loading="orderStore.submitting"
            @click="runAction('pay')"
          >
            立即支付
          </el-button>
          <el-button
            v-if="detail.actionFlags.canUploadReceipt"
            type="primary"
            plain
            :loading="orderStore.submitting"
            @click="runAction('upload')"
          >
            上传凭证
          </el-button>
          <el-button
            v-if="detail.actionFlags.canViewReceipt"
            plain
            :disabled="!detail.receiptInfo"
            @click="runAction('viewReceipt')"
          >
            查看凭证
          </el-button>
          <el-button
            v-if="detail.actionFlags.canMarkDelivered"
            type="success"
            plain
            :loading="orderStore.submitting"
            @click="runAction('delivered')"
          >
            确认送达
          </el-button>
          <el-button
            v-if="detail.actionFlags.canConfirmReceived"
            type="success"
            :loading="orderStore.submitting"
            @click="runAction('received')"
          >
            确认收货
          </el-button>
          <el-button
            v-if="detail.actionFlags.canCreateComplaint"
            type="danger"
            plain
            @click="navigateToComplaint"
          >
            发起投诉
          </el-button>
          <el-button
            v-else-if="detail.complaintInfo.myComplaintId"
            type="danger"
            plain
            @click="navigateToComplaint"
          >
            查看投诉
          </el-button>
        </div>
      </template>

      <EmptyState
        v-else-if="!orderStore.detailLoading"
        title="订单详情不可用"
        :description="detailErrorText"
      >
        <div class="page-actions">
          <el-button @click="router.push('/orders')">
            返回大厅
          </el-button>
          <el-button
            type="primary"
            plain
            :disabled="!isValidOrderId"
            @click="loadDetail()"
          >
            重新加载
          </el-button>
        </div>
      </EmptyState>
    </div>

    <el-dialog
      v-model="uploadDialogVisible"
      title="上传订单凭证"
      width="620px"
      @closed="resetReceiptForm"
    >
      <el-form
        label-position="top"
        :model="receiptForm"
      >
        <el-form-item
          label="订单截图"
          required
        >
          <el-upload
            v-model:file-list="receiptFileList"
            :auto-upload="false"
            :limit="1"
            accept=".jpg,.jpeg,.png,image/jpeg,image/png"
            drag
            @change="handleReceiptFileChange"
          >
            <div class="upload-copy">
              <strong>点击或拖拽选择本地图片</strong>
              <span>仅支持 JPG / PNG，文件大小不超过 10MB</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item
          label="实际总金额"
          required
        >
          <p class="muted-text field-tip">
            不得高于预计总金额 {{ formatCurrency(detail?.basicInfo?.estimatedTotalAmount) }}，低于预计金额时系统会自动生成差额退款。
          </p>
          <el-input-number
            v-model="receiptForm.actualTotalAmount"
            :min="0.01"
            :max="Number(detail?.basicInfo?.estimatedTotalAmount || 999999)"
            :precision="2"
            :step="1"
            controls-position="right"
          />
        </el-form-item>
        <div class="form-grid">
          <el-form-item
            label="预计开始送达"
            required
          >
            <el-date-picker
              v-model="receiptForm.expectedDeliveryStartAt"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
              placeholder="选择开始时间"
            />
          </el-form-item>
          <el-form-item
            label="预计最晚送达"
            required
          >
            <el-date-picker
              v-model="receiptForm.expectedDeliveryEndAt"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
              placeholder="选择结束时间"
            />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="orderStore.submitting"
          @click="submitReceipt"
        >
          提交凭证
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="receiptDialogVisible"
      title="订单凭证"
      width="680px"
    >
      <div class="receipt-preview">
        <ElImage
          v-if="activeReceiptUrl"
          :preview-src-list="[activeReceiptUrl]"
          :src="activeReceiptUrl"
          fit="contain"
        />
        <ul
          v-if="detail?.receiptInfo"
          class="detail-list"
        >
          <li><span>实际总金额</span><strong>{{ formatCurrency(detail.receiptInfo.actualTotalAmount) }}</strong></li>
          <li><span>预计开始送达</span><strong>{{ formatDateTime(detail.receiptInfo.expectedDeliveryStartAt) }}</strong></li>
          <li><span>预计最晚送达</span><strong>{{ formatDateTime(detail.receiptInfo.expectedDeliveryEndAt) }}</strong></li>
          <li><span>上传时间</span><strong>{{ formatDateTime(detail.receiptInfo.uploadedAt) }}</strong></li>
        </ul>
      </div>
    </el-dialog>
  </div>
</template>
