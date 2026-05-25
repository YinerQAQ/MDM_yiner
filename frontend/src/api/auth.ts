import instance from './axios'
import type { ApiResponse } from './types'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  username: string
  nickname: string
  orgId: string
  orgName: string
  roles: string[]
  permissions: string[]
}

export const login = async (request: LoginRequest) => {
  return instance.post<ApiResponse<LoginResponse>>('/auth/login', request)
}

export const logout = async () => {
  return instance.post('/auth/logout')
}