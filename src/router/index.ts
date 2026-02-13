import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/oauth/callback/:provider',
    name: 'OAuthCallback',
    component: () => import('@/views/oauth/callback.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表板' }
      },
      {
        path: 'api-keys',
        name: 'ApiKeys',
        component: () => import('@/views/api-keys/index.vue'),
        meta: { title: 'API Keys' }
      },
      {
        path: 'docs',
        name: 'Docs',
        component: () => import('@/views/docs/index.vue'),
        meta: { title: '接口文档' }
      },
      {
        path: 'pricing',
        name: 'Pricing',
        component: () => import('@/views/pricing/index.vue'),
        meta: { title: '产品定价' }
      },
      {
        path: 'recharge',
        name: 'Recharge',
        component: () => import('@/views/recharge/index.vue'),
        meta: { title: '充值' }
      },
      {
        path: 'billing',
        name: 'Billing',
        component: () => import('@/views/billing/index.vue'),
        meta: { title: '账单' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人设置' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  
  if (userStore.token && !userStore.user) {
    await userStore.fetchCurrentUser()
  }
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if ((to.name === 'Login' || to.name === 'Register') && userStore.isLoggedIn) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
