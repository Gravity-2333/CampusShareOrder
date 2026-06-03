<script setup>
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useAdminStore } from '../../stores/admin'
import { formatComplaintStatus, formatComplaintType } from '../../utils/format'

const router = useRouter()
const adminStore = useAdminStore()

const filters = reactive({
  keyword: '',
  page: 1,
  pageSize: 10,
  status: '',
})

const loadComplaints = async () => {
  try {
    const page = await adminStore.loadComplaints(filters)
    filters.page = page.page
    filters.pageSize = page.pageSize
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleSearch = async () => {
  filters.page = 1
  await loadComplaints()
}

const handlePageChange = async (page) => {
  filters.page = page
  await loadComplaints()
}

onMounted(loadComplaints)
</script>

<template>
  <div class="stack-page">
    <div class="section">
      <div class="section-header">
        <h3>投诉列表</h3>
      </div>

      <div class="toolbar">
        <input
          v-model="filters.keyword"
          type="text"
          placeholder="搜索投诉单号或订单号"
          @keyup.enter="handleSearch"
        >
        <select v-model="filters.status">
          <option value="">
            全部状态
          </option>
          <option value="PENDING">
            待处理
          </option>
          <option value="PROCESSING">
            处理中
          </option>
          <option value="RESOLVED">
            已处理
          </option>
        </select>
        <button
          class="btn btn-primary"
          :disabled="adminStore.complaintsLoading"
          @click="handleSearch"
        >
          查询
        </button>
      </div>

      <table
        v-if="adminStore.complaintsPage.list.length"
        class="table"
      >
        <thead>
          <tr>
            <th>投诉单号</th>
            <th>关联订单</th>
            <th>投诉人</th>
            <th>类型</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="row in adminStore.complaintsPage.list"
            :key="row.complaintId"
          >
            <td>{{ row.complaintNo }}</td>
            <td>{{ row.orderNo || '--' }}</td>
            <td>{{ row.complainantNickname || '--' }}</td>
            <td>{{ formatComplaintType(row.type) }}</td>
            <td>
              <span
                class="tag"
                :class="`tag-${row.status === 'PENDING' ? 'warning' : 'success'}`"
              >
                {{ formatComplaintStatus(row.status) }}
              </span>
            </td>
            <td>
              <a
                href="#"
                class="table-link"
                @click.prevent="router.push(`/admin/complaints/${row.complaintId}`)"
              >{{ row.status === 'PENDING' ? '处理' : '查看' }}</a>
            </td>
          </tr>
        </tbody>
      </table>
      <p
        v-else-if="!adminStore.complaintsLoading"
        class="muted-text"
      >
        暂无投诉数据。
      </p>

      <div
        v-if="adminStore.complaintsPage.pages > 1"
        class="pagination"
      >
        <button
          :disabled="adminStore.complaintsPage.page <= 1"
          @click="handlePageChange(adminStore.complaintsPage.page - 1)"
        >
          上一页
        </button>
        <button
          v-for="p in adminStore.complaintsPage.pages"
          :key="p"
          :class="{ active: p === adminStore.complaintsPage.page }"
          @click="handlePageChange(p)"
        >
          {{ p }}
        </button>
        <button
          :disabled="adminStore.complaintsPage.page >= adminStore.complaintsPage.pages"
          @click="handlePageChange(adminStore.complaintsPage.page + 1)"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>
