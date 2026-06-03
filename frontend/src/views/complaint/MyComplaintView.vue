<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import AppPagination from '../../components/common/AppPagination.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useComplaintStore } from '../../stores/complaint'
import { formatComplaintStatus, formatDateTime } from '../../utils/format'

const router = useRouter()
const complaintStore = useComplaintStore()

const loadComplaints = async () => {
  try {
    await complaintStore.loadMyComplaints()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handlePageChange = async ({ page, pageSize }) => {
  try {
    await complaintStore.loadMyComplaints({ page, pageSize })
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadComplaints)
</script>

<template>
  <div class="stack-page complaint-list-page">
    <div class="section">
      <div class="section-header">
        <h3>我的投诉</h3>
        <div class="page-actions">
          <el-button
            type="primary"
            @click="router.push('/complaints/create')"
          >
            发起投诉
          </el-button>
        </div>
      </div>

      <div
        v-if="complaintStore.myComplaintsPage.list.length"
        class="table-stack complaint-table-stack"
      >
        <el-table
          v-loading="complaintStore.myComplaintsLoading"
          :data="complaintStore.myComplaintsPage.list"
          stripe
        >
          <el-table-column
            prop="complaintNo"
            label="投诉单号"
            min-width="140"
          />
          <el-table-column
            prop="orderNo"
            label="关联订单"
            min-width="140"
          />
          <el-table-column
            prop="productName"
            label="商品"
            min-width="120"
          />
          <el-table-column
            label="状态"
            width="120"
          >
            <template #default="{ row }">
              <StatusTag
                :value="row.status"
                :text="formatComplaintStatus(row.status)"
              />
            </template>
          </el-table-column>
          <el-table-column
            label="创建时间"
            width="180"
          >
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            width="120"
          >
            <template #default="{ row }">
              <el-button
                link
                type="primary"
                @click="router.push(`/complaints/${row.complaintId}`)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <AppPagination
          :page="complaintStore.myComplaintsPage.page"
          :page-size="complaintStore.myComplaintsPage.pageSize"
          :pages="complaintStore.myComplaintsPage.pages"
          :total="complaintStore.myComplaintsPage.total"
          @change="handlePageChange"
        />
      </div>

      <EmptyState
        v-else-if="!complaintStore.myComplaintsLoading"
        title="暂无投诉"
        description="订单出现异常时，你可以从订单详情页发起投诉。"
      />
    </div>
  </div>
</template>
