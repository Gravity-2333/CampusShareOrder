<script setup>
import { computed, onBeforeUnmount, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

import { useAppStore } from '../stores/app'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const navItems = [
  { label: '仪表盘', path: '/admin/dashboard' },
  { label: '订单管理', path: '/admin/orders' },
  { label: '投诉管理', path: '/admin/complaints' },
  { label: '用户管理', path: '/admin/users' },
  { label: '数据报表', path: '/admin/reports' },
]

const activePath = computed(() => {
  if (route.path.startsWith('/admin/users/')) {
    return '/admin/users'
  }

  if (route.path.startsWith('/admin/orders/')) {
    return '/admin/orders'
  }

  if (route.path.startsWith('/admin/complaints/')) {
    return '/admin/complaints'
  }

  return route.path
})
const isDesktopCollapsed = computed(() => !appStore.isMobileViewport && appStore.sidebarCollapsed)
const navButtonText = computed(() => (appStore.isMobileViewport ? '打开导航' : '收起导航'))

const handleNavSelect = () => {
  appStore.closeMobileNav()
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出管理后台吗？', '退出确认', {
      cancelButtonText: '取消',
      confirmButtonText: '确定退出',
      type: 'warning',
    })
    appStore.closeMobileNav()
    await userStore.logoutCurrent()
    router.push('/admin/login')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      throw error
    }
  }
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
    class="app-shell app-admin-shell"
    :class="{ 'is-collapsed': isDesktopCollapsed, 'is-mobile': appStore.isMobileViewport }"
  >
    <aside
      v-if="!appStore.isMobileViewport"
      class="app-sidebar"
      :class="{ collapsed: isDesktopCollapsed }"
    >
      <div class="brand-block">
        <div class="brand-mark">
          {{ isDesktopCollapsed ? '管' : 'ADMIN' }}
        </div>
        <span class="brand-kicker">校园拼单后台</span>
        <h1>管理员后台</h1>
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
            ADMIN
          </div>
          <span class="brand-kicker">校园拼单后台</span>
          <h1>管理员后台</h1>
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
          <h2>{{ route.meta.title || '管理后台' }}</h2>
        </div>
        <div class="header-actions">
          <span class="welcome-text">管理员</span>
          <el-button
            text
            class="mobile-nav-trigger"
            @click="appStore.toggleSidebar()"
          >
            {{ navButtonText }}
          </el-button>
          <el-button
            class="btn-logout"
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
