import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, logout as apiLogout } from '@/api/auth'
import { getMenuTree, getPermissions, type MenuItem } from '@/api/permission'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref('')
  const nickname = ref('')
  const orgId = ref('')
  const orgName = ref('')
  const roles = ref<string[]>([])
  const permissions = ref<string[]>([])
  const menus = ref<MenuItem[]>([])

  // 检查 token 是否过期
  const isTokenExpired = (t: string): boolean => {
    if (!t) return true
    try {
      const payload = JSON.parse(atob(t.split('.')[1]))
      return payload.exp * 1000 < Date.now()
    } catch {
      return true
    }
  }

  // 初始化时如果 token 过期则清除
  if (token.value && isTokenExpired(token.value)) {
    token.value = ''
    localStorage.removeItem('token')
  }

  const login = async (loginUsername: string, loginPassword: string) => {
    const response = await apiLogin({ username: loginUsername, password: loginPassword })
    const data = response.data.data
    token.value = data.token
    username.value = data.username
    nickname.value = data.nickname
    orgId.value = data.orgId
    orgName.value = data.orgName
    roles.value = data.roles
    permissions.value = data.permissions
    localStorage.setItem('token', token.value)
    await fetchPermissions()
  }

  const logout = async () => {
    try {
      await apiLogout()
    } catch {
      // 忽略登出接口错误
    }
    clearAuth()
  }

  const clearAuth = () => {
    token.value = ''
    username.value = ''
    nickname.value = ''
    orgId.value = ''
    orgName.value = ''
    roles.value = []
    permissions.value = []
    menus.value = []
    localStorage.removeItem('token')
    // 异步导入 router 模块，避免与 router/index.ts 形成循环依赖
    import('@/router').then(({ resetDynamicRoutes }) => {
      try { resetDynamicRoutes() } catch { /* ignore */ }
    }).catch(() => { /* ignore */ })
  }

  const isLoggedIn = () => {
    return token.value !== '' && !isTokenExpired(token.value)
  }

  const hasPermission = (perm: string): boolean => {
    if (!perm) return true
    const perms = permissions.value || []
    if (perms.includes('*')) return true
    return perms.includes(perm)
  }

  const menuTree = computed<MenuItem[]>(() => {
    return menus.value || []
  })

  const fetchPermissions = async () => {
    try {
      const [menuRes, permRes] = await Promise.all([
        getMenuTree(),
        getPermissions()
      ])
      menus.value = menuRes.data.data || []
      permissions.value = permRes.data.data || []
    } catch {
      menus.value = []
      permissions.value = []
    }
  }

  return {
    token,
    username,
    nickname,
    orgId,
    orgName,
    roles,
    permissions,
    menus,
    login,
    logout,
    clearAuth,
    isLoggedIn,
    hasPermission,
    menuTree,
    fetchPermissions
  }
})