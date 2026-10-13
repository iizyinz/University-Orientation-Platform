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
      path: '/volunteer',
      component: () => import('@/views/volunteer/VolunteerLayout.vue'),
      meta: { requiresAuth: true, userType: [4, 5] },
      children: [
        {
          path: '',
          component: () => import('@/views/volunteer/VolunteerShell.vue'),
        },
      ],
    },
    {
      path: '/volunteer/uniform',
      component: () => import('@/views/volunteer/UniformLayout.vue'),
      meta: { requiresAuth: true, userType: 5 },
      children: [
        {
          path: '',
          component: () => import('@/views/volunteer/UniformShell.vue'),
        },
      ],
    },
  ],
})

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

    // 角色校验 - 根据路由 meta 判断
    const requiredType = meta.userType
    // 如果路由指定了角色类型，校验用户类型
    const allowedTypes = Array.isArray(requiredType) ? requiredType : [requiredType]
    if (requiredType && !allowedTypes.includes(userType)) {
      // 跳转到用户对应角色首页
      if (userType === 1) {
        return '/student'
      } else if (userType === 2) {
        return '/counselor'
      } else if (userType === 3) {
        return '/admin'
      } else if (userType === 4 || userType === 5) {
        return '/volunteer'
      } else {
        return '/login'
      }
    }
  }

  return true
})

export default router