<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useAppStore } from '../stores/app'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const navItems = [
  { label: '管理台', path: '/admin/dashboard' },
  { label: '用户管理', path: '/admin/users' },
  { label: '订单管理', path: '/admin/orders' },
  { label: '投诉管理', path: '/admin/complaints' },
  { label: '资金记录', path: '/admin/records/capital' },
  { label: '操作日志', path: '/admin/records/logs' },
]

const activePath = computed(() => route.path)

const handleLogout = async () => {
  await userStore.logoutCurrent()
  router.push('/admin/login')
}
</script>

<template>
  <div class="app-shell admin-shell">
    <aside class="app-sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
      <div class="brand-block">
        <span class="brand-kicker">Admin Console</span>
        <h1>平台管理端</h1>
      </div>
      <el-menu :default-active="activePath" router class="shell-menu">
        <el-menu-item v-for="item in navItems" :key="item.path" :index="item.path">
          {{ item.label }}
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="app-main">
      <header class="app-header">
        <div>
          <div class="mode-chip">模式：{{ appStore.apiMode }}</div>
          <h2>{{ route.meta.title || '管理后台' }}</h2>
        </div>
        <div class="header-actions">
          <span class="welcome-text">{{ userStore.displayName }}</span>
          <el-button text @click="appStore.toggleSidebar()">切换导航</el-button>
          <el-button type="danger" plain @click="handleLogout">退出管理</el-button>
        </div>
      </header>

      <main class="app-content">
        <router-view />
      </main>
    </div>
  </div>
</template>
