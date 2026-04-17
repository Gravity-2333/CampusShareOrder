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
  { label: '拼单大厅', path: '/orders' },
  { label: '发起拼单', path: '/orders/create' },
  { label: '我的拼单', path: '/my-orders' },
  { label: '我的投诉', path: '/complaints' },
  { label: '个人资料', path: '/profile' },
  { label: '信用分', path: '/credit' },
]

const activePath = computed(() => route.path)

const handleLogout = async () => {
  await userStore.logoutCurrent()
  router.push('/login')
}
</script>

<template>
  <div class="app-shell">
    <aside class="app-sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
      <div class="brand-block">
        <span class="brand-kicker">CampusShareOrder</span>
        <h1>校园拼单平台</h1>
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
          <h2>{{ route.meta.title || '校园拼单平台' }}</h2>
        </div>
        <div class="header-actions">
          <span class="welcome-text">{{ userStore.displayName }}</span>
          <el-button text @click="appStore.toggleSidebar()">切换导航</el-button>
          <el-button type="primary" plain @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <main class="app-content">
        <router-view />
      </main>
    </div>
  </div>
</template>
