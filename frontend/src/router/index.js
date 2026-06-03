import { ElMessage } from 'element-plus'
import { createRouter, createWebHistory } from 'vue-router'

import AdminLayout from '../layout/AdminLayout.vue'
import UserLayout from '../layout/UserLayout.vue'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/',
    redirect: '/orders',
  },
  {
    path: '/',
    children: [
      {
        path: '/login',
        component: () => import('../views/auth/LoginView.vue'),
        meta: { guestOnly: true, title: '用户登录' },
      },
      {
        path: '/register',
        component: () => import('../views/auth/RegisterView.vue'),
        meta: { guestOnly: true, title: '用户注册' },
      },
      {
        path: '/admin/login',
        component: () => import('../views/admin/AdminLoginView.vue'),
        meta: { guestOnly: true, title: '管理员登录' },
      },
    ],
  },
  {
    path: '/',
    component: UserLayout,
    children: [
      {
        path: '/orders',
        component: () => import('../views/order/OrderListView.vue'),
        meta: { requiresAuth: true, role: 'USER', title: '拼单大厅' },
      },
      {
        path: '/orders/create',
        component: () => import('../views/order/CreateOrderView.vue'),
        meta: { requiresAuth: true, requiresVerified: true, role: 'USER', title: '发起拼单' },
      },
      {
        path: '/orders/:orderId',
        component: () => import('../views/order/OrderDetailView.vue'),
        meta: { requiresAuth: true, role: 'USER', title: '拼单详情' },
      },
      {
        path: '/my-orders',
        component: () => import('../views/order/MyOrdersView.vue'),
        meta: { requiresAuth: true, role: 'USER', title: '我的拼单' },
      },
      {
        path: '/verify-student',
        component: () => import('../views/auth/VerifyStudentView.vue'),
        meta: { requiresAuth: true, role: 'USER', title: '实名认证' },
      },
      {
        path: '/profile',
        component: () => import('../views/user/ProfileView.vue'),
        meta: { requiresAuth: true, role: 'USER', title: '个人资料' },
      },
      {
        path: '/credit',
        component: () => import('../views/user/CreditView.vue'),
        meta: { requiresAuth: true, role: 'USER', title: '信用分' },
      },
      {
        path: '/complaints',
        component: () => import('../views/complaint/MyComplaintView.vue'),
        meta: { requiresAuth: true, role: 'USER', title: '我的投诉' },
      },
      {
        path: '/complaints/create',
        component: () => import('../views/complaint/ComplaintCreateView.vue'),
        meta: { requiresAuth: true, role: 'USER', title: '发起投诉' },
      },
      {
        path: '/complaints/:complaintId',
        component: () => import('../views/complaint/ComplaintDetailView.vue'),
        meta: { requiresAuth: true, role: 'USER', title: '投诉详情' },
      },
    ],
  },
  {
    path: '/',
    component: AdminLayout,
    children: [
      {
        path: '/admin/dashboard',
        component: () => import('../views/admin/AdminDashBoardView.vue'),
        meta: { requiresAuth: true, role: 'ADMIN', title: '管理台' },
      },
      {
        path: '/admin/users',
        component: () => import('../views/admin/AdminUserView.vue'),
        meta: { requiresAuth: true, role: 'ADMIN', title: '用户管理' },
      },
      {
        path: '/admin/users/:userId',
        component: () => import('../views/admin/AdminUserDetailView.vue'),
        meta: { requiresAuth: true, role: 'ADMIN', title: '用户详情' },
      },
      {
        path: '/admin/orders',
        component: () => import('../views/admin/AdminOrderView.vue'),
        meta: { requiresAuth: true, role: 'ADMIN', title: '订单管理' },
      },
      {
        path: '/admin/orders/:orderId',
        component: () => import('../views/admin/AdminOrderDetailView.vue'),
        meta: { requiresAuth: true, role: 'ADMIN', title: '订单详情' },
      },
      {
        path: '/admin/complaints',
        component: () => import('../views/admin/AdminComplaintView.vue'),
        meta: { requiresAuth: true, role: 'ADMIN', title: '投诉管理' },
      },
      {
        path: '/admin/complaints/:complaintId',
        component: () => import('../views/admin/AdminComplaintDetailView.vue'),
        meta: { requiresAuth: true, role: 'ADMIN', title: '投诉详情' },
      },
      {
        path: '/admin/records/capital',
        component: () => import('../views/admin/AdminCapitalView.vue'),
        meta: { requiresAuth: true, role: 'ADMIN', title: '资金记录' },
      },
      {
        path: '/admin/records/logs',
        component: () => import('../views/admin/AdminLogView.vue'),
        meta: { requiresAuth: true, role: 'ADMIN', title: '操作日志' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/orders',
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  await userStore.initializeSession()

  if (to.meta.guestOnly && userStore.isLoggedIn) {
    return userStore.isAdmin ? '/admin/dashboard' : '/orders'
  }

  if (!to.meta.requiresAuth) {
    return true
  }

  if (!userStore.isLoggedIn) {
    return to.meta.role === 'ADMIN' ? '/admin/login' : '/login'
  }

  if (to.meta.role && userStore.session.role !== to.meta.role) {
    ElMessage.warning('当前账号无权访问该页面')
    return userStore.isAdmin ? '/admin/dashboard' : '/orders'
  }

  if (
    to.meta.requiresVerified &&
    userStore.isUser &&
    !userStore.session.isVerified &&
    to.path !== '/verify-student'
  ) {
    ElMessage.warning('请先完成实名认证')
    return '/verify-student'
  }

  return true
})

router.afterEach((to) => {
  const pageTitle = to.meta?.title ? `${to.meta.title} | CampusShareOrder` : 'CampusShareOrder'
  document.title = pageTitle
})

export default router
