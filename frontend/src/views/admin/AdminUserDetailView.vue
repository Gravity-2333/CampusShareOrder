<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useAdminStore } from '../../stores/admin'
import { formatDateTime, formatSignedNumber, formatUserStatus } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const user = computed(() => adminStore.userDetail)

const stats = computed(() => {
  if (!user.value) {
    return []
  }

  return [
    {
      label: '账号状态',
      value: formatUserStatus(user.value.status),
      hint: '后台详情页直接消费用户详情字段',
    },
    {
      label: '信用分',
      value: user.value.creditScore,
      hint: '便于后台快速判断风险等级',
    },
    {
      label: '信用记录',
      value: user.value.creditRecords?.length || 0,
      hint: '来自用户详情聚合信息',
    },
  ]
})

const loadDetail = async (userId = route.params.userId) => {
  if (!userId) {
    return
  }

  try {
    await adminStore.loadUserDetail(userId)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const toggleStatus = async () => {
  if (!user.value) {
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
      <StatCard v-for="item in stats" :key="item.label" :label="item.label" :value="item.value" :hint="item.hint" />
    </div>

    <PageSection
      v-loading="adminStore.userDetailLoading"
      title="用户详情"
      description="后台详情页以治理视角查看基础资料、认证状态和信用记录。"
    >
      <template v-if="user">
        <div class="card-header-row">
          <div>
            <p class="section-kicker">用户 #{{ user.userId }}</p>
            <h2>{{ user.nickname }}</h2>
          </div>
          <StatusTag :value="user.status" :text="formatUserStatus(user.status)" />
        </div>

        <div class="detail-grid">
          <div class="surface-card detail-panel">
            <h3>基础信息</h3>
            <ul class="detail-list">
              <li><span>手机号</span><strong>{{ user.phone || '--' }}</strong></li>
              <li><span>联系方式</span><strong>{{ user.contactInfo || '--' }}</strong></li>
              <li><span>认证状态</span><strong>{{ user.isVerified ? '已认证' : '未认证' }}</strong></li>
              <li><span>学号</span><strong>{{ user.studentNo || '--' }}</strong></li>
              <li><span>注册时间</span><strong>{{ formatDateTime(user.createdAt) }}</strong></li>
            </ul>
          </div>

          <div class="surface-card detail-panel">
            <h3>治理信息</h3>
            <ul class="detail-list">
              <li><span>账号状态</span><strong>{{ formatUserStatus(user.status) }}</strong></li>
              <li><span>信用分</span><strong>{{ user.creditScore }}</strong></li>
            </ul>
          </div>
        </div>

        <PageSection title="信用记录" description="便于后台查看该用户过往信用变化。">
          <div class="desktop-table">
            <el-table :data="user.creditRecords || []" stripe>
              <el-table-column label="时间">
                <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
              </el-table-column>
              <el-table-column prop="changeReason" label="变更原因" />
              <el-table-column label="变更值">
                <template #default="{ row }">{{ formatSignedNumber(row.delta) }}</template>
              </el-table-column>
            </el-table>
          </div>

          <div class="mobile-record-list">
            <article
              v-for="(row, index) in user.creditRecords || []"
              :key="`${row.createdAt}-${index}`"
              class="surface-card mobile-record-card"
            >
              <div class="mobile-record-title">
                <span>{{ formatDateTime(row.createdAt) }}</span>
                <strong>{{ row.changeReason || '信用分变更' }}</strong>
              </div>
              <ul class="mobile-record-fields">
                <li><span>变更值</span><strong>{{ formatSignedNumber(row.delta) }}</strong></li>
              </ul>
            </article>
          </div>
        </PageSection>

        <div class="page-actions wrap-actions">
          <el-button @click="router.push('/admin/users')">返回用户管理</el-button>
          <el-button :type="user.status === 'NORMAL' ? 'danger' : 'primary'" @click="toggleStatus">
            {{ user.status === 'NORMAL' ? '封禁用户' : '解封用户' }}
          </el-button>
        </div>
      </template>
    </PageSection>
  </div>
</template>
