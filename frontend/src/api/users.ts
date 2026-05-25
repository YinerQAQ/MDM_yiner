import instance from './axios'
import type { ApiResponse, BaseUser } from './types'

export interface UserCreateRequest {
  id: string
  username: string
  password?: string
  nickname: string
  sex: string
  orgId: string
  email: string
  phone: string
  securityLevel: string
}

export const createUser = async (request: UserCreateRequest) => {
  return instance.post<ApiResponse<BaseUser>>('/users', request)
}

export const getUserById = async (id: string) => {
  return instance.get<ApiResponse<BaseUser>>(`/users/${id}`)
}

export const getAllUsers = async () => {
  return instance.get<ApiResponse<BaseUser[]>>('/users')
}

export const updateUser = async (id: string, request: UserCreateRequest) => {
  return instance.put<ApiResponse<BaseUser>>(`/users/${id}`, request)
}

export const deleteUser = async (id: string) => {
  return instance.delete<ApiResponse<void>>(`/users/${id}`)
}

export const changeUserStatus = async (id: string, status: string) => {
  return instance.put<ApiResponse<void>>(`/users/${id}/status`, undefined, {
    params: { status }
  })
}

export const resetPassword = async (id: string, password: string) => {
  return instance.put<ApiResponse<void>>(`/users/${id}/reset-password`, undefined, {
    params: { password }
  })
}