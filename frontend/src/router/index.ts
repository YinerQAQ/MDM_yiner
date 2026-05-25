import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../views/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: 'data-models',
        name: 'DataModels',
        component: () => import('../views/DataModels.vue')
      },
      {
        path: 'main-data/:modelId?',
        name: 'MainData',
        component: () => import('../views/MainData.vue')
      },
      {
        path: 'workflows',
        name: 'Workflows',
        component: () => import('../views/Workflows.vue')
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/Users.vue')
      },
      {
        path: 'orgs',
        name: 'Orgs',
        component: () => import('../views/Orgs.vue')
      },
      {
        path: 'data-exchange',
        name: 'DataExchange',
        component: () => import('../views/DataExchange.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  if (to.path !== '/login' && !authStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router