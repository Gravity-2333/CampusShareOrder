<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useUserStore } from '../../stores/user'
import { validatePassword, requireValue, validationSummary } from '../../utils/validate'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  password: '',
  username: '',
})
const loading = ref(false)
const adminFocus = [
  { label: '订单治理', value: '查看异常流转与取消原因' },
  { label: '投诉处理', value: '先看详情再填写处理结果' },
  { label: '后台记录', value: '追踪日志与资金变化' },
]

const handleSubmit = async () => {
  const errorMessage = validationSummary([
    requireValue(form.username, '请输入管理员账号'),
    validatePassword(form.password, '管理员密码'),
  ])

  if (errorMessage) {
    ElMessage.warning(errorMessage)
    return
  }

  loading.value = true

  try {
    await userStore.loginAdmin(form)
    ElMessage.success('管理员登录成功')
    router.push('/admin/dashboard')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page auth-page-compact admin-auth-page">
    <section class="auth-hero auth-hero-compact admin-auth-hero">
      <p class="section-kicker">
        管理入口
      </p>
      <h1>先进入后台治理视角，再处理订单、投诉与用户风险。</h1>
      <p>管理员登录与普通用户分离，后台重点承接订单治理、投诉处理和平台记录查看。</p>

      <div class="auth-metric-grid">
        <article
          v-for="item in adminFocus"
          :key="item.label"
          class="auth-metric-card auth-metric-card-admin"
        >
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </article>
      </div>
    </section>

    <el-card
      class="auth-card"
      shadow="hover"
    >
      <template #header>
        <div class="card-header-row">
          <div>
            <p class="section-kicker">
              管理入口
            </p>
            <h2>管理员登录</h2>
          </div>
          <el-button
            link
            @click="router.push('/login')"
          >
            用户入口
          </el-button>
        </div>
      </template>

      <div class="auth-card-summary">
        <strong>进入平台管理端</strong>
        <p>登录后进入后台概览，集中处理用户、订单、投诉与平台记录。</p>
      </div>

      <el-form
        label-position="top"
        :model="form"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="用户名">
          <el-input
            v-model="form.username"
            placeholder="请输入管理员账号"
          />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            show-password
            placeholder="请输入管理员密码"
          />
        </el-form-item>
        <div class="page-actions">
          <el-button
            type="primary"
            :loading="loading"
            @click="handleSubmit"
          >
            登录管理端
          </el-button>
        </div>
      </el-form>

      <div class="auth-card-footer">
        <span>这是独立的后台登录入口</span>
        <button
          type="button"
          class="auth-inline-link"
          @click="router.push('/login')"
        >
          切换到用户登录
        </button>
      </div>
    </el-card>
  </div>
</template>
