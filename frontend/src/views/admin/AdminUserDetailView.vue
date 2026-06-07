<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useAdminStore } from '../../stores/admin'
import { creditReasonTypeTextMap } from '../../utils/enum'
import { formatDateTime, formatSignedNumber, formatUserStatus } from '../../utils/format'
import { isValidId, normalizeId } from '../../utils/id'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const user = computed(() => adminStore.userDetail)
const currentUserId = computed(() => route.params.userId)
const normalizedUserId = computed(() => normalizeId(currentUserId.value))
const isValidUserId = computed(() => isValidId(normalizedUserId.value))
const detailErrorText = computed(() =>
  isValidUserId.value ? '当前未能加载到用户详情，请返回列表重新选择。' : '当前路由中的用户 ID 无效，请返回用户列表重新进入详情页。',
)

const stats = computed(() => {
  if (!user.value) {
    return []
  }

  return [
    {
      label: '账号状态',
      value: formatUserStatus(user.value.status),
      hint: '用于判断账号是否可正常使用',
    },
    {
      label: '信用分',
      value: user.value.creditScore,
      hint: '便于后台快速判断风险等级',
    },
    {
      label: '信用记录',
      value: user.value.creditRecords?.length || 0,
      hint: '展示该用户近期信用变化次数',
    },
  ]
})

const formatReasonType = (value) => creditReasonTypeTextMap[value] || value || '--'

const formatRelatedBiz = (row) => {
  if (row.relatedComplaintId) return `投诉 #${row.relatedComplaintId}`
  if (row.relatedOrderId) return `订单 #${row.relatedOrderId}`
  return '--'
}

const loadDetail = async (userId = route.params.userId) => {
  if (!isValidUserId.value) {
    adminStore.userDetail = null
    return
  }

  try {
    await adminStore.loadUserDetail(userId)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const toggleStatus = async () => {
  if (!user.value || adminStore.submitting) {
    return
  }

  try {
    if (user.value.status === 'NORMAL') {
      const { value } = await ElMessageBox.prompt('请输入封禁原因', '封禁用户', {
        cancelButtonText: '取消',
        confirmButtonText: '确认封禁',
        inputPlaceholder: '例如：恶意投诉、骚扰他人',
        inputValidator: (inputValue) => {
          if (!inputValue?.trim()) {
            return '请输入封禁原因'
          }

          if (inputValue.trim().length > 255) {
            return '封禁原因长度不能超过 255 个字符'
          }

          return true
        },
      })

      await adminStore.toggleUserStatus(user.value, { reason: value.trim() })
      ElMessage.success('用户已封禁')
      return
    }

    await adminStore.toggleUserStatus(user.value)
    ElMessage.success('用户已解封')
  } catch (error) {
    if (error === 'cancel' || error?.message === 'cancel') {
      return
    }

    ElMessage.error(error.message || '操作失败，请稍后重试')
  }
}

watch(
  () => route.params.userId,
  (userId, previousUserId) => {
    if (userId && userId !== previousUserId) {
      loadDetail(userId)
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
      v-loading="adminStore.userDetailLoading"
      title="用户详情"
      description="后台详情页以治理视角查看基础资料、认证状态和信用记录。"
    >
      <template v-if="user">
        <div class="card-header-row">
          <div>
            <p class="section-kicker">
              用户 #{{ user.userId }}
            </p>
            <h2>{{ user.nickname }}</h2>
          </div>
          <StatusTag
            :value="user.status"
            :text="formatUserStatus(user.status)"
          />
        </div>

        <div class="detail-grid">
          <div class="detail-panel">
            <h4>基础信息</h4>
            <ul class="detail-list">
              <li><span>手机号</span><strong>{{ user.phone || '--' }}</strong></li>
              <li><span>联系方式</span><strong>{{ user.contactInfo || '--' }}</strong></li>
              <li><span>认证状态</span><strong>{{ user.isVerified ? '已认证' : '未认证' }}</strong></li>
              <li><span>学号</span><strong>{{ user.studentNo || '--' }}</strong></li>
              <li><span>注册时间</span><strong>{{ formatDateTime(user.createdAt) }}</strong></li>
            </ul>
          </div>

          <div class="detail-panel">
            <h4>治理信息</h4>
            <ul class="detail-list">
              <li><span>账号状态</span><strong>{{ formatUserStatus(user.status) }}</strong></li>
              <li><span>信用分</span><strong>{{ user.creditScore }}</strong></li>
            </ul>
          </div>
        </div>

        <div
          class="detail-panel"
          style="margin-top: 1rem;"
        >
          <h4>信用记录</h4>
          <template v-if="user.creditRecords?.length">
            <div class="desktop-table">
              <el-table
                :data="user.creditRecords"
                stripe
              >
                <el-table-column label="时间">
                  <template #default="{ row }">
                    {{ formatDateTime(row.createdAt) }}
                  </template>
                </el-table-column>
                <el-table-column
                  prop="changeReason"
                  label="变更原因"
                />
                <el-table-column label="变更值">
                  <template #default="{ row }">
                    {{ formatSignedNumber(row.delta) }}
                  </template>
                </el-table-column>
                <el-table-column label="变更后分数">
                  <template #default="{ row }">
                    {{ row.currentScore ?? '--' }}
                  </template>
                </el-table-column>
                <el-table-column label="原因类型">
                  <template #default="{ row }">
                    {{ formatReasonType(row.reasonType) }}
                  </template>
                </el-table-column>
                <el-table-column label="关联业务">
                  <template #default="{ row }">
                    {{ formatRelatedBiz(row) }}
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div class="mobile-record-list">
              <article
                v-for="(row, index) in user.creditRecords"
                :key="`${row.createdAt}-${index}`"
                class="mobile-record-card"
              >
                <div class="mobile-record-title">
                  <span>{{ formatDateTime(row.createdAt) }}</span>
                  <strong>{{ row.changeReason || '信用分变更' }}</strong>
                </div>
                <ul class="mobile-record-fields">
                  <li><span>变更值</span><strong>{{ formatSignedNumber(row.delta) }}</strong></li>
                  <li><span>变更后分数</span><strong>{{ row.currentScore ?? '--' }}</strong></li>
                  <li><span>原因类型</span><strong>{{ formatReasonType(row.reasonType) }}</strong></li>
                  <li><span>关联业务</span><strong>{{ formatRelatedBiz(row) }}</strong></li>
                </ul>
              </article>
            </div>
          </template>

          <EmptyState
            v-else
            title="暂无信用记录"
            description="该用户当前没有可展示的信用分变更记录。"
          />
        </div>

        <div
          class="page-actions wrap-actions"
          style="margin-top: 1.5rem; padding: 1rem; background: #f8f6f2; border-radius: 10px;"
        >
          <el-button @click="router.push('/admin/users')">
            返回用户管理
          </el-button>
          <el-button
            :type="user.status === 'NORMAL' ? 'danger' : 'primary'"
            :loading="adminStore.submitting"
            @click="toggleStatus"
          >
            {{ user.status === 'NORMAL' ? '封禁用户' : '解封用户' }}
          </el-button>
        </div>
      </template>
      <EmptyState
        v-else-if="!adminStore.userDetailLoading"
        title="用户详情不可用"
        :description="detailErrorText"
      >
        <div class="page-actions">
          <el-button @click="router.push('/admin/users')">
            返回用户管理
          </el-button>
          <el-button
            type="primary"
            plain
            :disabled="!isValidUserId"
            @click="loadDetail()"
          >
            重新加载
          </el-button>
        </div>
      </EmptyState>
    </PageSection>
  </div>
</template>
