<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useAppStore } from '../stores/app'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const navItems = computed(() => {
  const items = [
    { label: '拼单大厅', path: '/orders' },
    { label: '发起拼单', path: '/orders/create' },
    { label: '我的拼单', path: '/my-orders' },
    { label: '我的投诉', path: '/complaints' },
    { label: '个人资料', path: '/profile' },
    { label: '信用分', path: '/credit' },
  ]

  if (!userStore.session.isVerified) {
    items.splice(4, 0, { label: '实名认证', path: '/verify-student' })
  }

  return items
})

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
          <el-button
            v-if="userStore.isUser && !userStore.session.isVerified"
            type="warning"
            plain
            @click="router.push('/verify-student')"
          >
            去认证
          </el-button>
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
