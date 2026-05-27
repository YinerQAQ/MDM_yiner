import request from './axios'
import type { ApiResponse, BaseUser } from './types'

export interface BaseRole {
  id: string
  roleCode: string
  roleName: string
  orgId: string
  status: string
}

export const getRoles = async () => {
  return request.get<ApiResponse<BaseRole[]>>('/roles')
}

export const createRole = async (role: BaseRole) => {
  return request.post<ApiResponse<void>>('/roles', role)
}

export const updateRole = async (id: string, role: BaseRole) => {
  return request.put<ApiResponse<void>>(`/roles/${id}`, role)
}

export const deleteRole = async (id: string) => {
  return request.delete<ApiResponse<void>>(`/roles/${id}`)
}

export const getRoleMenus = async (id: string) => {
  return request.get<ApiResponse<string[]>>(`/roles/${id}/menus`)
}

export const assignRoleMenus = async (id: string, menuIds: string[]) => {
  return request.put<ApiResponse<void>>(`/roles/${id}/menus`, menuIds)
}

export const getRoleUsers = async (id: string) => {
  return request.get<ApiResponse<BaseUser[]>>(`/roles/${id}/users`)
}
