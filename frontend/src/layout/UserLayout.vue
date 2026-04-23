<script setup>
import { computed, onBeforeUnmount, onMounted, watch } from 'vue'
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
const isDesktopCollapsed = computed(() => !appStore.isMobileViewport && appStore.sidebarCollapsed)
const navButtonText = computed(() => (appStore.isMobileViewport ? '打开导航' : '切换导航'))

const handleNavSelect = () => {
  appStore.closeMobileNav()
}

const handleLogout = async () => {
  appStore.closeMobileNav()
  await userStore.logoutCurrent()
  router.push('/login')
}

watch(
  () => route.fullPath,
  () => {
    appStore.closeMobileNav()
  },
)

onMounted(() => {
  appStore.initializeViewportTracking()
})

onBeforeUnmount(() => {
  appStore.releaseViewportTracking()
})
</script>

<template>
  <div
    class="app-shell"
    :class="{ 'is-collapsed': isDesktopCollapsed, 'is-mobile': appStore.isMobileViewport }"
  >
    <aside
      v-if="!appStore.isMobileViewport"
      class="app-sidebar"
      :class="{ collapsed: isDesktopCollapsed }"
    >
      <div class="brand-block">
        <div class="brand-mark">
          {{ isDesktopCollapsed ? '拼' : 'CSO' }}
        </div>
        <span class="brand-kicker">CampusShareOrder</span>
        <h1>校园拼单平台</h1>
      </div>

      <el-menu
        :default-active="activePath"
        :collapse="isDesktopCollapsed"
        :collapse-transition="false"
        router
        class="shell-menu"
      >
        <el-menu-item
          v-for="item in navItems"
          :key="item.path"
          :index="item.path"
        >
          {{ item.label }}
        </el-menu-item>
      </el-menu>
    </aside>

    <el-drawer
      v-model="appStore.mobileNavOpen"
      :with-header="false"
      direction="ltr"
      size="280px"
      class="mobile-nav-drawer"
    >
      <div class="app-sidebar mobile-drawer-sidebar">
        <div class="brand-block">
          <div class="brand-mark">
            CSO
          </div>
          <span class="brand-kicker">CampusShareOrder</span>
          <h1>校园拼单平台</h1>
        </div>

        <el-menu
          :default-active="activePath"
          router
          class="shell-menu"
          @select="handleNavSelect"
        >
          <el-menu-item
            v-for="item in navItems"
            :key="item.path"
            :index="item.path"
          >
            {{ item.label }}
          </el-menu-item>
        </el-menu>
      </div>
    </el-drawer>

    <div class="app-main">
      <header class="app-header">
        <div>
          <div class="mode-chip">
            模式：{{ appStore.apiMode }}
          </div>
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
          <el-button
            text
            class="mobile-nav-trigger"
            @click="appStore.toggleSidebar()"
          >
            {{ navButtonText }}
          </el-button>
          <el-button
            type="primary"
            plain
            @click="handleLogout"
          >
            退出登录
          </el-button>
        </div>
      </header>

      <main class="app-content">
        <router-view />
      </main>
    </div>
  </div>
</template>
