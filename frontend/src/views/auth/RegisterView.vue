<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useUserStore } from '../../stores/user'
import {
  firstValidationError,
  validateNickname,
  validatePassword,
  validatePhone,
} from '../../utils/validate'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  nickname: '',
  password: '',
  phone: '',
})
const loading = ref(false)
const registerTips = [
  '注册后进入用户端登录流程',
  '实名认证完成后才能发起拼单',
  '管理员账号与普通用户账号完全分离',
]

const handleSubmit = async () => {
  const errorMessage = firstValidationError([
    validateNickname(form.nickname),
    validatePhone(form.phone),
    validatePassword(form.password),
  ])

  if (errorMessage) {
    ElMessage.warning(errorMessage)
    return
  }

  loading.value = true

  try {
    await userStore.registerUser(form)
    ElMessage.success('注册成功，请返回登录')
    router.push('/login')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page auth-page-compact">
    <section class="auth-hero auth-hero-compact">
      <p class="section-kicker">
        创建账号
      </p>
      <h1>先建好用户身份，再进入拼单与投诉流程。</h1>
      <p>注册后即可进入校园拼单，完成认证后可以发起拼单、加入订单并查看完整流程。</p>

      <div class="auth-side-note">
        <strong>注册后你会得到</strong>
        <ul class="auth-bullet-list">
          <li
            v-for="item in registerTips"
            :key="item"
          >
            {{ item }}
          </li>
        </ul>
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
              创建账号
            </p>
            <h2>创建用户账号</h2>
          </div>
          <el-button
            link
            @click="router.push('/login')"
          >
            返回登录
          </el-button>
        </div>
      </template>

      <el-alert
        v-if="userStore.session.role === ''"
        class="auth-alert"
        title="注册后将进入用户端登录流程，管理员账号与普通用户账号分离。"
        type="info"
        :closable="false"
      />

      <div class="auth-card-summary">
        <strong>快速创建普通用户账号</strong>
        <p>手机号会作为登录账号，请使用常用号码；昵称会展示给拼单成员。</p>
      </div>

      <el-form
        label-position="top"
        :model="form"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="昵称">
          <el-input
            v-model="form.nickname"
            placeholder="请输入昵称"
          />
        </el-form-item>
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
            立即注册
          </el-button>
        </div>
      </el-form>

      <div class="auth-card-footer">
        <span>已有账号可以直接返回登录</span>
        <button
          type="button"
          class="auth-inline-link"
          @click="router.push('/login')"
        >
          去登录
        </button>
      </div>
    </el-card>
  </div>
</template>
