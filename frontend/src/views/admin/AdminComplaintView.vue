<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import AppPagination from '../../components/common/AppPagination.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useAdminStore } from '../../stores/admin'
import { formatComplaintStatus, formatComplaintType, formatDateTime } from '../../utils/format'

const router = useRouter()
const adminStore = useAdminStore()

const stats = computed(() => [
  {
    label: '投诉总量',
    value: adminStore.complaintsPage.total,
    hint: '管理端列表与用户端列表共用固定分页协议',
  },
  {
    label: '待处理',
    value: adminStore.complaintsPage.list.filter((item) => item.status === 'PENDING').length,
    hint: '优先关注待处理投诉',
  },
])

const summaryText = computed(() => {
  if (!adminStore.complaintsPage.list.length) {
    return '当前没有待展示的投诉记录。'
  }

  return '投诉管理页建议先看详情，再填写处理结果，避免只在列表里做过度判断。'
})

const loadComplaints = async () => {
  try {
    await adminStore.loadComplaints({ page: 1, pageSize: 10 })
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleComplaint = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入处理结果摘要', '处理投诉', {
      cancelButtonText: '取消',
      confirmButtonText: '确认处理',
      inputPlaceholder: '例如：投诉成立，订单已取消',
      inputValidator: (inputValue) => {
        if (!inputValue?.trim()) {
          return '请输入处理结果'
        }

        return true
      },
    })

    await adminStore.processComplaint(row.complaintId, { handleResult: value.trim() })
    ElMessage.success('投诉已处理')
  } catch (error) {
    if (error === 'cancel' || error?.message === 'cancel') {
      return
    }

    ElMessage.error(error.message || '处理失败，请稍后重试')
  }
}

const handlePageChange = async ({ page, pageSize }) => {
  try {
    await adminStore.loadComplaints({ page, pageSize })
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadComplaints)
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
      title="投诉管理"
      description="管理员处理投诉前，先查看详情，再决定处理结果。"
    >
      <p class="muted-text">
        {{ summaryText }}
      </p>

      <div class="table-toolbar">
        <span class="table-caption">
          共 {{ adminStore.complaintsPage.total }} 条投诉，当前待处理 {{ stats[1].value }} 条。
        </span>
      </div>

      <div
        v-if="adminStore.complaintsPage.list.length"
        class="table-stack"
      >
        <div class="desktop-table">
          <el-table
            v-loading="adminStore.complaintsLoading"
            :data="adminStore.complaintsPage.list"
            stripe
          >
            <el-table-column
              prop="complaintNo"
              label="投诉单号"
            />
            <el-table-column label="类型">
              <template #default="{ row }">
                {{ formatComplaintType(row.type) }}
              </template>
            </el-table-column>
            <el-table-column
              prop="productName"
              label="商品"
            />
            <el-table-column
              prop="accusedNickname"
              label="被投诉人"
            />
            <el-table-column label="状态">
              <template #default="{ row }">
                <StatusTag
                  :value="row.status"
                  :text="formatComplaintStatus(row.status)"
                />
              </template>
            </el-table-column>
            <el-table-column label="提交时间">
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
                    @click="router.push(`/admin/complaints/${row.complaintId}`)"
                  >
                    查看详情
                  </el-button>
                  <el-button
                    v-if="row.status === 'PENDING'"
                    link
                    type="danger"
                    @click="handleComplaint(row)"
                  >
                    标记处理
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="mobile-record-list">
          <article
            v-for="row in adminStore.complaintsPage.list"
            :key="row.complaintId"
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
            <ul class="mobile-record-fields">
              <li><span>投诉类型</span><strong>{{ formatComplaintType(row.type) }}</strong></li>
              <li><span>被投诉人</span><strong>{{ row.accusedNickname || '--' }}</strong></li>
              <li><span>提交时间</span><strong>{{ formatDateTime(row.createdAt) }}</strong></li>
            </ul>
            <div class="page-actions">
              <el-button
                type="primary"
                plain
                @click="router.push(`/admin/complaints/${row.complaintId}`)"
              >
                查看详情
              </el-button>
              <el-button
                v-if="row.status === 'PENDING'"
                type="danger"
                plain
                @click="handleComplaint(row)"
              >
                处理投诉
              </el-button>
            </div>
          </article>
        </div>

        <AppPagination
          :page="adminStore.complaintsPage.page"
          :page-size="adminStore.complaintsPage.pageSize"
          :pages="adminStore.complaintsPage.pages"
          :total="adminStore.complaintsPage.total"
          @change="handlePageChange"
        />
      </div>
      <EmptyState
        v-else
        title="暂无投诉"
        description="当前没有投诉记录，可在用户端提交投诉后回到这里处理。"
      />
    </PageSection>
  </div>
</template>
