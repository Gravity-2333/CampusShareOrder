<script setup>
import { onBeforeUnmount, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useUserStore } from './stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const handleSessionExpired = (event) => {
  const message = event.detail?.message || '登录状态已失效，请重新登录'
  const loginPath = route.path.startsWith('/admin') ? '/admin/login' : '/login'

  userStore.clearSession()
  ElMessage.warning(message)

  if (route.path !== loginPath) {
    router.replace(loginPath)
  }
}

onMounted(() => {
  window.addEventListener('campus-session-expired', handleSessionExpired)
})

onBeforeUnmount(() => {
  window.removeEventListener('campus-session-expired', handleSessionExpired)
})
</script>

<template>
  <router-view />
</template>
