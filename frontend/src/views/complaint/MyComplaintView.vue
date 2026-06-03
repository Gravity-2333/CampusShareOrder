<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import AppPagination from '../../components/common/AppPagination.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useComplaintStore } from '../../stores/complaint'
import { formatComplaintStatus, formatComplaintType, formatDateTime } from '../../utils/format'

const router = useRouter()
const complaintStore = useComplaintStore()

const stats = computed(() => [
  {
    label: '投诉总数',
    value: complaintStore.myComplaintsPage.total,
    hint: '展示你发起过的投诉记录',
  },
  {
    label: '待处理',
    value: complaintStore.myComplaintsPage.list.filter((item) => item.status === 'PENDING').length,
    hint: '便于快速识别仍在等待后台处理的投诉',
  },
  {
    label: '当前页',
    value: `${complaintStore.myComplaintsPage.page}/${complaintStore.myComplaintsPage.pages || 1}`,
    hint: '便于快速定位历史处理记录',
  },
])

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
      title="我的投诉"
      description="查看投诉处理进度、处理结果和关联订单信息。"
    >
      <div class="table-toolbar">
        <span class="table-caption">共 {{ complaintStore.myComplaintsPage.total }} 条投诉记录，建议优先查看待处理项。</span>
        <div class="page-actions">
          <el-button
            type="danger"
            plain
            @click="router.push('/complaints/create')"
          >
            发起新投诉
          </el-button>
        </div>
      </div>

      <div
        v-if="complaintStore.myComplaintsPage.list.length"
        class="table-stack"
      >
        <div class="desktop-table">
          <el-table
            v-loading="complaintStore.myComplaintsLoading"
            :data="complaintStore.myComplaintsPage.list"
            stripe
          >
            <el-table-column
              prop="complaintNo"
              label="投诉单号"
            />
            <el-table-column label="投诉类型">
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
            <el-table-column label="发起时间">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default="{ row }">
                <div class="row-action-group">
                  <el-button
                    link
                    type="primary"
                    @click="router.push(`/complaints/${row.complaintId}`)"
                  >
                    查看详情
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="mobile-record-list">
          <article
            v-for="row in complaintStore.myComplaintsPage.list"
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
              <li><span>发起时间</span><strong>{{ formatDateTime(row.createdAt) }}</strong></li>
            </ul>
            <div class="page-actions">
              <el-button
                type="primary"
                plain
                @click="router.push(`/complaints/${row.complaintId}`)"
              >
                查看详情
              </el-button>
            </div>
          </article>
        </div>

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
    </PageSection>
  </div>
</template>
