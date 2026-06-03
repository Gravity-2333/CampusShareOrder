<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import AppPagination from '../../components/common/AppPagination.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useAdminStore } from '../../stores/admin'
import { formatDateTime, formatUserStatus } from '../../utils/format'
import { normalizePageFilters } from '../../utils/page'

const router = useRouter()
const adminStore = useAdminStore()

const filters = reactive({
  keyword: '',
  page: 1,
  pageSize: 10,
  status: '',
})

const stats = computed(() => [
  {
    label: '用户总量',
    value: adminStore.usersPage.total,
    hint: '管理端用户列表遵循固定分页结构',
  },
  {
    label: '正常账号',
    value: adminStore.usersPage.list.filter((item) => item.status === 'NORMAL').length,
    hint: '用于观察当前可用账号规模',
  },
  {
    label: '封禁账号',
    value: adminStore.usersPage.list.filter((item) => item.status === 'BANNED').length,
    hint: '后台治理重点关注异常账号',
  },
])

const summaryText = computed(() => {
  if (!adminStore.usersPage.list.length) {
    return '当前筛选条件下没有用户数据。'
  }

  if (filters.keyword || filters.status) {
    return '当前列表已经按关键词或状态过滤，适合快速处理目标用户。'
  }

  return '用户列表优先用于筛选、查看详情和治理账号状态，详细资料和信用记录在详情页查看。'
})

const loadUsers = async () => {
  try {
    const page = await adminStore.loadUsers(filters)
    filters.page = page.page
    filters.pageSize = page.pageSize
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const normalizeFilters = () => {
  Object.assign(filters, normalizePageFilters(filters))
}

const submitFilters = async () => {
  normalizeFilters()
  filters.page = 1
  await loadUsers()
}

const resetFilters = async () => {
  filters.keyword = ''
  filters.page = 1
  filters.pageSize = 10
  filters.status = ''
  await loadUsers()
}

const toggleStatus = async (row) => {
  if (adminStore.submitting) {
    return
  }

  try {
    if (row.status === 'NORMAL') {
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

      await adminStore.toggleUserStatus(row, { reason: value.trim() })
      ElMessage.success('用户已封禁')
      return
    }

    await adminStore.toggleUserStatus(row)
    ElMessage.success('用户已解封')
  } catch (error) {
    if (error === 'cancel' || error?.message === 'cancel') {
      return
    }

    ElMessage.error(error.message || '操作失败，请稍后重试')
  }
}

const handlePageChange = async ({ page, pageSize }) => {
  normalizeFilters()
  filters.page = page
  filters.pageSize = pageSize
  await loadUsers()
}

onMounted(loadUsers)
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
      title="用户管理"
      description="先收口用户筛选、详情查看和封禁治理这一条后台链路。"
    >
      <p class="muted-text">
        {{ summaryText }}
      </p>
      <div class="toolbar-row">
        <el-input
          v-model="filters.keyword"
          placeholder="按昵称或手机号搜索"
          clearable
          @keyup.enter="submitFilters"
        />
        <el-select
          v-model="filters.status"
          placeholder="按状态筛选"
          clearable
        >
          <el-option
            label="正常"
            value="NORMAL"
          />
          <el-option
            label="封禁"
            value="BANNED"
          />
        </el-select>
        <el-button
          type="primary"
          :loading="adminStore.usersLoading"
          @click="submitFilters"
        >
          查询
        </el-button>
      </div>

      <div class="table-toolbar">
        <span class="table-caption">共 {{ adminStore.usersPage.total }} 个账号，详情页承接认证和信用记录等完整信息。</span>
        <div class="page-actions">
          <el-button
            :disabled="adminStore.usersLoading"
            @click="resetFilters"
          >
            恢复默认筛选
          </el-button>
        </div>
      </div>

      <div
        v-if="adminStore.usersPage.list.length"
        class="table-stack"
      >
        <div class="desktop-table">
          <el-table
            v-loading="adminStore.usersLoading"
            :data="adminStore.usersPage.list"
            stripe
          >
            <el-table-column
              prop="userId"
              label="用户 ID"
              width="88"
            />
            <el-table-column
              prop="nickname"
              label="昵称"
            />
            <el-table-column
              prop="phone"
              label="手机号"
            />
            <el-table-column label="认证状态">
              <template #default="{ row }">
                {{ row.isVerified ? '已认证' : '未认证' }}
              </template>
            </el-table-column>
            <el-table-column
              prop="creditScore"
              label="信用分"
              width="90"
            />
            <el-table-column label="状态">
              <template #default="{ row }">
                <StatusTag
                  :value="row.status"
                  :text="formatUserStatus(row.status)"
                />
              </template>
            </el-table-column>
            <el-table-column label="注册时间">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column
              label="操作"
              width="220"
            >
              <template #default="{ row }">
                <div class="row-action-group">
                  <el-button
                    link
                    type="primary"
                    @click="router.push(`/admin/users/${row.userId}`)"
                  >
                    查看详情
                  </el-button>
                  <el-button
                    link
                    :type="row.status === 'NORMAL' ? 'danger' : 'primary'"
                    :disabled="adminStore.submitting"
                    @click="toggleStatus(row)"
                  >
                    {{ row.status === 'NORMAL' ? '封禁' : '解封' }}
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="mobile-record-list">
          <article
            v-for="row in adminStore.usersPage.list"
            :key="row.userId"
            class="surface-card mobile-record-card"
          >
            <div class="mobile-record-header">
              <div class="mobile-record-title">
                <span>用户 #{{ row.userId }}</span>
                <strong>{{ row.nickname || '--' }}</strong>
              </div>
              <StatusTag
                :value="row.status"
                :text="formatUserStatus(row.status)"
              />
            </div>
            <ul class="mobile-record-fields">
              <li><span>手机号</span><strong>{{ row.phone || '--' }}</strong></li>
              <li><span>认证状态</span><strong>{{ row.isVerified ? '已认证' : '未认证' }}</strong></li>
              <li><span>信用分</span><strong>{{ row.creditScore ?? '--' }}</strong></li>
              <li><span>注册时间</span><strong>{{ formatDateTime(row.createdAt) }}</strong></li>
            </ul>
            <div class="page-actions">
              <el-button
                type="primary"
                plain
                @click="router.push(`/admin/users/${row.userId}`)"
              >
                查看详情
              </el-button>
              <el-button
                :type="row.status === 'NORMAL' ? 'danger' : 'primary'"
                plain
                :loading="adminStore.submitting"
                @click="toggleStatus(row)"
              >
                {{ row.status === 'NORMAL' ? '封禁用户' : '解封用户' }}
              </el-button>
            </div>
          </article>
        </div>

        <AppPagination
          :page="adminStore.usersPage.page"
          :page-size="adminStore.usersPage.pageSize"
          :pages="adminStore.usersPage.pages"
          :total="adminStore.usersPage.total"
          @change="handlePageChange"
        />
      </div>
      <EmptyState
        v-else-if="!adminStore.usersLoading"
        title="暂无用户"
        description="用户列表统一通过 store -> api 层消费，便于持续维护。"
      />
    </PageSection>
  </div>
</template>
