<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import { useAdminStore } from '../../stores/admin'
import { formatUserStatus } from '../../utils/format'
import { normalizePageFilters } from '../../utils/page'

const router = useRouter()
const adminStore = useAdminStore()

const filters = reactive({
  keyword: '',
  page: 1,
  pageSize: 10,
  status: '',
})

const summaryText = computed(() => {
  if (!adminStore.usersPage.list.length) {
    return '当前筛选条件下没有用户数据。'
  }
  return ''
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

const toggleStatus = async (row) => {
  if (adminStore.submitting) return

  try {
    if (row.status === 'NORMAL') {
      const { value } = await ElMessageBox.prompt('请输入封禁原因', '封禁用户', {
        cancelButtonText: '取消',
        confirmButtonText: '确认封禁',
        inputPlaceholder: '例如：恶意投诉、骚扰他人',
        inputValidator: (inputValue) => {
          if (!inputValue?.trim()) return '请输入封禁原因'
          if (inputValue.trim().length > 255) return '封禁原因长度不能超过 255 个字符'
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
    if (error === 'cancel' || error?.message === 'cancel') return
    ElMessage.error(error.message || '操作失败，请稍后重试')
  }
}

const handlePageChange = async (page) => {
  normalizeFilters()
  filters.page = page
  await loadUsers()
}

onMounted(loadUsers)
</script>

<template>
  <div class="stack-page">
    <div class="section">
      <div class="section-header">
        <h3>用户列表</h3>
      </div>

      <div class="toolbar">
        <input
          v-model="filters.keyword"
          type="text"
          placeholder="搜索用户名或手机号"
          @keyup.enter="submitFilters"
        >
        <select v-model="filters.status">
          <option value="">
            全部状态
          </option>
          <option value="NORMAL">
            正常
          </option>
          <option value="BANNED">
            封禁
          </option>
        </select>
        <button
          class="btn btn-primary"
          :disabled="adminStore.usersLoading"
          @click="submitFilters"
        >
          查询
        </button>
      </div>

      <div v-if="summaryText">
        <p class="muted-text">
          {{ summaryText }}
        </p>
      </div>

      <table
        v-if="adminStore.usersPage.list.length"
        class="table"
      >
        <thead>
          <tr>
            <th>用户ID</th>
            <th>昵称</th>
            <th>手机号</th>
            <th>学号</th>
            <th>信用分</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="row in adminStore.usersPage.list"
            :key="row.userId"
          >
            <td>{{ row.userId }}</td>
            <td>{{ row.nickname }}</td>
            <td>{{ row.phone }}</td>
            <td>{{ row.studentNo || '--' }}</td>
            <td>{{ row.creditScore }}</td>
            <td>
              <span
                class="tag"
                :class="row.status === 'NORMAL' ? 'tag-success' : 'tag-danger'"
              >
                {{ formatUserStatus(row.status) }}
              </span>
            </td>
            <td>
              <div class="row-action-group">
                <a
                  href="#"
                  class="table-link"
                  @click.prevent="router.push(`/admin/users/${row.userId}`)"
                >查看详情</a>
                <button
                  class="btn btn-danger btn-sm"
                  :disabled="adminStore.submitting"
                  @click="toggleStatus(row)"
                >
                  {{ row.status === 'NORMAL' ? '封禁' : '解封' }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <p
        v-else-if="!adminStore.usersLoading"
        class="muted-text"
      >
        暂无用户数据。
      </p>

      <div
        v-if="adminStore.usersPage.pages > 1"
        class="pagination"
      >
        <button
          :disabled="adminStore.usersPage.page <= 1"
          @click="handlePageChange(adminStore.usersPage.page - 1)"
        >
          上一页
        </button>
        <button
          v-for="p in adminStore.usersPage.pages"
          :key="p"
          :class="{ active: p === adminStore.usersPage.page }"
          @click="handlePageChange(p)"
        >
          {{ p }}
        </button>
        <button
          :disabled="adminStore.usersPage.page >= adminStore.usersPage.pages"
          @click="handlePageChange(adminStore.usersPage.page + 1)"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>
