<script setup>
import { computed, nextTick, onMounted, onBeforeUnmount, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { useAdminStore } from '../../stores/admin'
import { formatComplaintType } from '../../utils/format'

const adminStore = useAdminStore()

const period = reactive({ value: 'month' })
const loading = ref(false)
const trendCanvas = ref(null)
const distributionCanvas = ref(null)
let trendChart = null
let distributionChart = null

const stats = computed(() => [
  { label: '订单总数', value: adminStore.metrics.orders || 0 },
  { label: '用户总数', value: adminStore.metrics.users || 0 },
  { label: '投诉总数', value: adminStore.metrics.complaints || 0 },
  { label: '完成率', value: computeCompletionRate() },
])

function computeCompletionRate() {
  const orders = adminStore.dashboardOverview.recentOrders || []
  if (!orders.length) return '--'
  const completed = orders.filter(o => o.status === 'COMPLETED').length
  return Math.round((completed / orders.length) * 100) + '%'
}

const complaintStats = computed(() => {
  const complaints = adminStore.dashboardOverview.recentComplaints || []
  const typeMap = {}
  complaints.forEach(c => {
    const t = formatComplaintType(c.type || 'OTHER')
    typeMap[t] = (typeMap[t] || 0) + 1
  })
  const total = complaints.length || 1
  return Object.entries(typeMap).map(([typeLabel, count]) => ({
    typeLabel,
    count,
    percent: Math.round((count / total) * 100) + '%',
  }))
})

function getOrderTrendData() {
  const orders = adminStore.dashboardOverview.recentOrders || []
  const dateMap = {}
  orders.forEach(o => {
    const date = (o.createdAt || '').slice(0, 10) || '未知'
    dateMap[date] = (dateMap[date] || 0) + 1
  })
  const sorted = Object.entries(dateMap).sort((a, b) => a[0].localeCompare(b[0]))
  return {
    labels: sorted.map(([d]) => d),
    values: sorted.map(([, c]) => c),
  }
}

function getOrderTypeData() {
  const orders = adminStore.dashboardOverview.recentOrders || []
  const typeMap = {}
  orders.forEach(o => {
    const t = o.productName || '其他'
    typeMap[t] = (typeMap[t] || 0) + 1
  })
  return {
    labels: Object.keys(typeMap),
    values: Object.values(typeMap),
  }
}

async function loadChartJs() {
  if (window.Chart) return window.Chart
  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = 'https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.umd.min.js'
    script.onload = () => resolve(window.Chart)
    script.onerror = reject
    document.head.appendChild(script)
  })
}

function renderTrendChart() {
  if (!trendCanvas.value) return
  const data = getOrderTrendData()
  if (!data.labels.length) {
    data.labels = ['暂无数据']
    data.values = [0]
  }

  if (trendChart) trendChart.destroy()

  const colors = {
    line: '#d87b4d',
    fill: 'rgba(216, 123, 77, 0.1)',
    grid: '#f0ebe4',
    text: '#6b7c70',
  }

  trendChart = new window.Chart(trendCanvas.value, {
    type: 'line',
    data: {
      labels: data.labels,
      datasets: [{
        label: '订单数量',
        data: data.values,
        borderColor: colors.line,
        backgroundColor: colors.fill,
        fill: true,
        tension: 0.4,
        pointBackgroundColor: colors.line,
        pointRadius: 5,
        pointHoverRadius: 7,
      }],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
      },
      scales: {
        x: {
          grid: { color: colors.grid },
          ticks: { color: colors.text },
        },
        y: {
          beginAtZero: true,
          grid: { color: colors.grid },
          ticks: { color: colors.text, stepSize: 1 },
        },
      },
    },
  })
}

function renderDistributionChart() {
  if (!distributionCanvas.value) return
  const data = getOrderTypeData()
  if (!data.labels.length) {
    data.labels = ['暂无数据']
    data.values = [1]
  }

  if (distributionChart) distributionChart.destroy()

  const palette = ['#d87b4d', '#3b7a57', '#2c3e50', '#f0ebe4', '#6b7c70', '#b9852f', '#4a90d9']
  const bgColors = data.labels.map((_, i) => palette[i % palette.length])

  distributionChart = new window.Chart(distributionCanvas.value, {
    type: 'doughnut',
    data: {
      labels: data.labels,
      datasets: [{
        data: data.values,
        backgroundColor: bgColors,
        borderColor: '#ffffff',
        borderWidth: 2,
      }],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom',
          labels: { color: '#6b7c70', padding: 16, usePointStyle: true },
        },
      },
    },
  })
}

async function initCharts() {
  try {
    await loadChartJs()
    await nextTick()
    renderTrendChart()
    renderDistributionChart()
  } catch {
    ElMessage.warning('图表库加载失败，请检查网络连接')
  }
}

async function loadReport() {
  loading.value = true
  try {
    await adminStore.loadDashboardOverview()
    await nextTick()
    initCharts()
    ElMessage.success('报表数据已刷新')
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadXlsx() {
  if (window.XLSX) return window.XLSX
  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = 'https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js'
    script.onload = () => resolve(window.XLSX)
    script.onerror = reject
    document.head.appendChild(script)
  })
}

async function exportExcel() {
  try {
    await loadXlsx()

    const wb = window.XLSX.utils.book_new()

    // Sheet 1: 概览
    const overviewData = [
      ['指标', '数值'],
      ...stats.value.map(s => [s.label, String(s.value)]),
    ]
    const overviewSheet = window.XLSX.utils.aoa_to_sheet(overviewData)
    window.XLSX.utils.book_append_sheet(wb, overviewSheet, '数据概览')

    // Sheet 2: 投诉统计
    const complaintData = [
      ['类型', '数量', '占比'],
      ...complaintStats.value.map(c => [c.typeLabel, c.count, c.percent]),
    ]
    const complaintSheet = window.XLSX.utils.aoa_to_sheet(complaintData)
    window.XLSX.utils.book_append_sheet(wb, complaintSheet, '投诉统计')

    // Sheet 3: 订单趋势
    const trend = getOrderTrendData()
    const trendData = [
      ['日期', '订单数'],
      ...trend.labels.map((l, i) => [l, trend.values[i]]),
    ]
    const trendSheet = window.XLSX.utils.aoa_to_sheet(trendData)
    window.XLSX.utils.book_append_sheet(wb, trendSheet, '订单趋势')

    // Sheet 4: 订单类型分布
    const dist = getOrderTypeData()
    const distData = [
      ['商品', '数量'],
      ...dist.labels.map((l, i) => [l, dist.values[i]]),
    ]
    const distSheet = window.XLSX.utils.aoa_to_sheet(distData)
    window.XLSX.utils.book_append_sheet(wb, distSheet, '订单类型分布')

    window.XLSX.writeFile(wb, `校园拼单平台-数据报表-${new Date().toISOString().slice(0, 10)}.xlsx`)
    ElMessage.success('报表已导出')
  } catch {
    ElMessage.warning('导出失败，请检查网络连接')
  }
}

onMounted(async () => {
  await adminStore.loadDashboardOverview()
  await nextTick()
  initCharts()
})

onBeforeUnmount(() => {
  if (trendChart) trendChart.destroy()
  if (distributionChart) distributionChart.destroy()
})
</script>

<template>
  <div class="stack-page">
    <div class="toolbar">
      <select v-model="period.value">
        <option value="week">
          本周
        </option>
        <option value="month">
          本月
        </option>
        <option value="quarter">
          本季度
        </option>
      </select>
      <button
        class="btn btn-primary"
        :disabled="loading"
        @click="loadReport"
      >
        生成报表
      </button>
      <button
        class="btn btn-secondary"
        @click="exportExcel"
      >
        导出Excel
      </button>
    </div>

    <div class="stats-grid">
      <div
        v-for="item in stats"
        :key="item.label"
        class="stat-card"
      >
        <div class="stat-label">
          {{ item.label }}
        </div>
        <div class="stat-value">
          {{ item.value }}
        </div>
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <h3>订单趋势</h3>
      </div>
      <div class="chart-container">
        <canvas ref="trendCanvas" />
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <h3>订单类型分布</h3>
      </div>
      <div class="chart-container">
        <canvas ref="distributionCanvas" />
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <h3>投诉统计</h3>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>类型</th>
            <th>数量</th>
            <th>占比</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="item in complaintStats"
            :key="item.typeLabel"
          >
            <td>{{ item.typeLabel }}</td>
            <td>{{ item.count }}</td>
            <td>{{ item.percent }}</td>
          </tr>
          <tr v-if="!complaintStats.length">
            <td
              colspan="3"
              style="text-align: center; color: #6b7c70;"
            >
              暂无投诉数据
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
