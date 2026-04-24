<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useUserStore } from '../../stores/user'
import { firstValidationError, validatePassword, validatePhone } from '../../utils/validate'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  password: '',
  phone: '',
})
const loading = ref(false)
const heroMetrics = [
  { label: '多人拼单', value: '一键发起和加入' },
  { label: '流程透明', value: '支付、凭证、收货清晰可查' },
  { label: '异常可追踪', value: '投诉与信用记录闭环' },
]

const handleSubmit = async () => {
  const errorMessage = firstValidationError([
    validatePhone(form.phone),
    validatePassword(form.password),
  ])

  if (errorMessage) {
    ElMessage.warning(errorMessage)
    return
  }

  loading.value = true

  try {
    await userStore.loginUser(form)
    ElMessage.success('登录成功')
    router.push('/orders')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <section class="auth-hero">
      <p class="section-kicker">
        Campus Share Order
      </p>
      <h1>让校园拼单更省心、更透明。</h1>
      <p>
        和同学一起凑单、分摊配送费、跟踪凭证与收货状态，把每一次拼单都放在清楚的流程里。
      </p>

      <div class="auth-metric-grid">
        <article
          v-for="item in heroMetrics"
          :key="item.label"
          class="auth-metric-card"
        >
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </article>
      </div>

      <div class="auth-side-note">
        <strong>从发起到完成，全程有迹可循</strong>
        <p>实名认证后即可发起拼单，成员加入、支付、上传凭证、确认送达和投诉处理都会被记录。</p>
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
              用户入口
            </p>
            <h2>用户登录</h2>
          </div>
          <el-button
            link
            @click="router.push('/admin/login')"
          >
            管理员入口
          </el-button>
        </div>
      </template>

      <div class="auth-card-summary">
        <strong>欢迎回来</strong>
        <p>登录后进入拼单大厅，查看正在招募的拼单，也可以管理自己的参与记录。</p>
      </div>

      <el-form
        label-position="top"
        :model="form"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="手机号">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
          />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            show-password
            placeholder="请输入密码"
          />
        </el-form-item>
        <div class="page-actions">
          <el-button
            type="primary"
            :loading="loading"
            @click="handleSubmit"
          >
            登录
          </el-button>
          <el-button @click="router.push('/register')">
            去注册
          </el-button>
        </div>
      </el-form>

      <div class="auth-card-footer">
        <span>还没有账号？注册后即可完善资料</span>
        <button
          type="button"
          class="auth-inline-link"
          @click="router.push('/admin/login')"
        >
          切换到管理员登录
        </button>
      </div>
    </el-card>
  </div>
</template>
