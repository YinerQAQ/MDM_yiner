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
      // 数据标准
      {
        path: 'data-models',
        name: 'DataModels',
        component: () => import('../views/DataModels.vue')
      },
      {
        path: 'code-rules',
        name: 'CodeRules',
        component: () => import('../views/CodeRules.vue')
      },
      // 数据业务
      {
        path: 'main-data/:modelId?',
        name: 'MainData',
        component: () => import('../views/MainData.vue')
      },
      {
        path: 'data-apply',
        name: 'DataApply',
        component: () => import('../views/DataApply.vue')
      },
      {
        path: 'data-review',
        name: 'DataReview',
        component: () => import('../views/DataReview.vue')
      },
      {
        path: 'data-archive',
        name: 'DataArchive',
        component: () => import('../views/DataArchive.vue')
      },
      // 数据交换
      {
        path: 'data-distribute',
        name: 'DataDistribute',
        component: () => import('../views/DataDistribute.vue')
      },
      {
        path: 'distribute-monitor',
        name: 'DistributeMonitor',
        component: () => import('../views/DistributeMonitor.vue')
      },
      // 管理中心
      {
        path: 'workflows',
        name: 'Workflows',
        component: () => import('../views/Workflows.vue')
      },
      {
        path: 'workflow-designer/:id?',
        name: 'WorkflowDesigner',
        component: () => import('../views/WorkflowDesigner.vue')
      },
      {
        path: 'orgs',
        name: 'Orgs',
        component: () => import('../views/Orgs.vue')
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/Users.vue')
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('../views/Roles.vue')
      },
      {
        path: 'groups',
        name: 'Groups',
        component: () => import('../views/Groups.vue')
      },
      {
        path: 'menus',
        name: 'Menus',
        component: () => import('../views/Menus.vue')
      },
      {
        path: 'dict',
        name: 'Dict',
        component: () => import('../views/Dict.vue')
      },
      {
        path: 'sys-params',
        name: 'SysParams',
        component: () => import('../views/SysParams.vue')
      },
      {
        path: 'system-settings',
        name: 'SystemSettings',
        component: () => import('../views/SystemSettings.vue')
      },
      {
        path: 'audit-log',
        name: 'AuditLog',
        component: () => import('../views/AuditLog.vue')
      },
      // 保留旧路由兼容
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
  if (to.path !== '/login' && !authStore.isLoggedIn()) {
    next('/login')
  } else {
    next()
  }
})

export default router
