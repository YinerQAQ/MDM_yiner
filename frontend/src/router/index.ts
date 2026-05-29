import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import type { MenuItem } from '../api/permission'

// 静态路由：无需权限的页面
const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../views/Layout.vue')
  }
]

// 异步路由表：所有需要权限控制的业务页面
export const asyncRoutes: RouteRecordRaw[] = [
  { path: '', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
  { path: 'data-models', name: 'DataModels', component: () => import('../views/DataModels.vue') },
  { path: 'code-rules', name: 'CodeRules', component: () => import('../views/CodeRules.vue') },
  { path: 'main-data', name: 'MainData', component: () => import('../views/MainData.vue') },
  { path: 'data-apply', name: 'DataApply', component: () => import('../views/DataApply.vue') },
  { path: 'data-review', name: 'DataReview', component: () => import('../views/DataReview.vue') },
  { path: 'data-archive', name: 'DataArchive', component: () => import('../views/DataArchive.vue') },
  { path: 'data-distribute', name: 'DataDistribute', component: () => import('../views/DataDistribute.vue') },
  { path: 'distribute-monitor', name: 'DistributeMonitor', component: () => import('../views/DistributeMonitor.vue') },
  { path: 'workflows', name: 'Workflows', component: () => import('../views/Workflows.vue') },
  { path: 'workflow-designer/:id?', name: 'WorkflowDesigner', component: () => import('../views/WorkflowDesigner.vue') },
  { path: 'orgs', name: 'Orgs', component: () => import('../views/Orgs.vue') },
  { path: 'users', name: 'Users', component: () => import('../views/Users.vue') },
  { path: 'roles', name: 'Roles', component: () => import('../views/Roles.vue') },
  { path: 'groups', name: 'Groups', component: () => import('../views/Groups.vue') },
  { path: 'menus', name: 'Menus', component: () => import('../views/Menus.vue') },
  { path: 'dict', name: 'Dict', component: () => import('../views/Dict.vue') },
  { path: 'sys-params', name: 'SysParams', component: () => import('../views/SysParams.vue') },
  { path: 'system-settings', name: 'SystemSettings', component: () => import('../views/SystemSettings.vue') },
  { path: 'audit-log', name: 'AuditLog', component: () => import('../views/AuditLog.vue') },
  { path: 'data-exchange', name: 'DataExchange', component: () => import('../views/DataExchange.vue') },
  { path: 'model-audit', name: 'ModelAudit', component: () => import('../views/ModelAudit.vue') },
  { path: 'category-config', name: 'CategoryConfig', component: () => import('../views/CategoryConfig.vue') },
  { path: 'data-receive', name: 'DataReceive', component: () => import('../views/DataReceive.vue') },
  { path: 'data-query-service', name: 'DataQueryService', component: () => import('../views/DataQueryService.vue') },
  { path: 'security-level', name: 'SecurityLevel', component: () => import('../views/SecurityLevel.vue') },
  { path: 'system-monitor', name: 'SystemMonitor', component: () => import('../views/SystemMonitor.vue') },
  // 个人中心相关路由（所有登录用户可用，不受菜单权限控制）
  { path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue'), meta: { title: '个人信息' } },
  { path: 'change-password', name: 'ChangePassword', component: () => import('../views/ChangePassword.vue'), meta: { title: '修改密码' } }
]

// 任何登录用户都应可访问的"基础路由"：首页 + 个人中心
const ALWAYS_AVAILABLE_ROUTE_NAMES = ['Dashboard', 'Profile', 'ChangePassword']

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

let isRoutesLoaded = false

// 暴露给 auth store 在 logout 时调用，避免上一会话的路由残留到下一会话
export const resetDynamicRoutes = (): void => {
  for (const route of asyncRoutes) {
    const name = route.name as string
    if (name && router.hasRoute(name)) {
      router.removeRoute(name)
    }
  }
  isRoutesLoaded = false
}

// 将菜单 path 标准化为 asyncRoutes 中使用的相对路径形式（去除前导 /）
const getRoutePath = (menuPath: string): string => {
  if (!menuPath || menuPath === '/') return ''
  return menuPath.replace(/^\/+/, '')
}

const getBaseRoutePath = (routePath: string): string => {
  // 剥离动态参数部分，如 'main-data/:modelId?' -> 'main-data'
  return routePath.split('/:')[0]
}

const findRouteByMenu = (menu: MenuItem): RouteRecordRaw | undefined => {
  if (!menu || !menu.path) return undefined
  const menuPath = getRoutePath(menu.path)
  // 后端可能用 'dashboard' 或 '/' 表示首页，统一映射到 Dashboard 路由（path=''）
  if (menuPath === '' || menuPath === 'dashboard') {
    return asyncRoutes.find(r => r.path === '')
  }
  return asyncRoutes.find(r => getBaseRoutePath(r.path) === menuPath)
}

const registerRouteUnderLayout = (route: RouteRecordRaw): boolean => {
  const name = route.name as string
  if (!name) return false
  if (router.hasRoute(name)) return false
  router.addRoute('Layout', route)
  return true
}

const addRoutesByMenus = (menus: MenuItem[]): number => {
  let registered = 0
  const traverse = (items: MenuItem[]) => {
    for (const menu of items) {
      const route = findRouteByMenu(menu)
      if (route && registerRouteUnderLayout(route)) {
        registered++
      }
      if (menu.children && menu.children.length > 0) {
        traverse(menu.children)
      }
    }
  }
  traverse(menus)
  return registered
}

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()

  if (to.path === '/login') {
    next()
    return
  }

  if (!authStore.isLoggedIn()) {
    // 未登录或 token 过期，确保下一次登录时路由会重新按权限注册
    resetDynamicRoutes()
    next('/login')
    return
  }

  if (!isRoutesLoaded) {
    await authStore.fetchPermissions()
    const menus = authStore.menuTree
    let registered = 0
    if (menus && menus.length > 0) {
      registered = addRoutesByMenus(menus)
    }
    // 兜底 1：API 完全没返回菜单（向后兼容，仅在无任何菜单时启用）
    if (registered === 0 && (!menus || menus.length === 0)) {
      asyncRoutes.forEach(route => registerRouteUnderLayout(route))
    }
    // 兜底 2：基础路由（Dashboard / 个人中心）始终注册，保证非 admin 用户登录后至少能进入首页
    ALWAYS_AVAILABLE_ROUTE_NAMES.forEach(name => {
      const route = asyncRoutes.find(r => r.name === name)
      if (route) registerRouteUnderLayout(route)
    })
    isRoutesLoaded = true
    next({ ...to, replace: true })
    return
  }

  next()
})

export default router
