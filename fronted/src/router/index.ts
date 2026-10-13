import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
    },
    {
      path: '/admin',
      component: () => import('@/views/admin/AdminLayout.vue'),
      meta: { requiresAuth: true, userType: 3 },
      children: [
        {
          path: '',
          component: () => import('@/views/admin/AdminDashboard.vue'),
        },
      ],
    },
    {
      path: '/admin/dashboard-screen',
      component: () => import('@/views/admin/AdminDashboardScreen.vue'),
      meta: { requiresAuth: true, userType: 3 },
    },
    {
      path: '/counselor',
      component: () => import('@/views/counselor/CounselorLayout.vue'),
      meta: { requiresAuth: true, userType: 2 },
      children: [
        {
          path: '',
          component: () => import('@/views/counselor/CounselorShell.vue'),
        },
      ],
    },
    {
      path: '/student',
      component: () => import('@/views/student/StudentLayout.vue'),
      meta: { requiresAuth: true, userType: 1 },
      children: [
        {
          path: '',
          component: () => import('@/views/student/StudentShell.vue'),
        },
      ],
    },
    {
      path: '/student-web',
      component: () => import('@/views/student/StudentWebLayout.vue'),
      meta: { requiresAuth: true, userType: 1 },
      children: [
        {
          path: '',
          component: () => import('@/views/student/StudentWebShell.vue'),
        },
      ],
    },
  ],
})

const MOBILE_APP_URL = 'https://ggqhapp.iizyinz.cn'

// 全局路由守卫：校验用户角色
router.beforeEach((to, from) => {
  const meta = to.meta as any

  // 登录页直接放行
  if (to.path === '/login') {
    return true
  }

  // 需要认证的页面
  if (meta.requiresAuth) {
    const userStore = useUserStore()
    const token = userStore.token
    const userType = userStore.userInfo?.userType

    // 未登录跳转登录页
    if (!token) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }

    // 志愿者（4、5）跳转手机端
    if (userType === 4 || userType === 5) {
      window.location.href = MOBILE_APP_URL
      return
    }

    // 角色校验
    const requiredType = meta.userType
    if (requiredType) {
      const allowedTypes = Array.isArray(requiredType) ? requiredType : [requiredType]
      if (!allowedTypes.includes(userType)) {
        const roleRedirectMap: Record<number, string> = {
          1: '/student-web',
          2: '/counselor',
          3: '/admin',
        }
        return roleRedirectMap[userType] || '/login'
      }
    }
  }

  return true
})

export default router
