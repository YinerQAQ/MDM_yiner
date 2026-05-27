import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as apiLogin, logout as apiLogout } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref('')
  const nickname = ref('')
  const orgId = ref('')
  const orgName = ref('')
  const roles = ref<string[]>([])
  const permissions = ref<string[]>([])

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
    localStorage.removeItem('token')
  }

  const isLoggedIn = () => {
    return token.value !== '' && !isTokenExpired(token.value)
  }

  return {
    token,
    username,
    nickname,
    orgId,
    orgName,
    roles,
    permissions,
    login,
    logout,
    clearAuth,
    isLoggedIn
  }
})