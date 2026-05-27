import request from './axios'
import type { ApiResponse, BaseUser } from './types'

export interface BaseGroup {
  id: string
  groupCode: string
  groupName: string
  orgId: string
  status: string
}

export interface OrgPerm {
  orgId: string
  cascadeFlag: string
}

export const getGroups = async () => {
  return request.get<ApiResponse<BaseGroup[]>>('/groups')
}

export const createGroup = async (group: BaseGroup) => {
  return request.post<ApiResponse<void>>('/groups', group)
}

export const updateGroup = async (id: string, group: BaseGroup) => {
  return request.put<ApiResponse<void>>(`/groups/${id}`, group)
}

export const deleteGroup = async (id: string) => {
  return request.delete<ApiResponse<void>>(`/groups/${id}`)
}

export const getGroupOrgs = async (id: string) => {
  return request.get<ApiResponse<string[]>>(`/groups/${id}/orgs`)
}

export const assignGroupOrgs = async (id: string, orgPerms: OrgPerm[]) => {
  return request.put<ApiResponse<void>>(`/groups/${id}/orgs`, orgPerms)
}

export const getGroupUsers = async (id: string) => {
  return request.get<ApiResponse<BaseUser[]>>(`/groups/${id}/users`)
}

export const addGroupUsers = async (id: string, userIds: string[]) => {
  return request.post<ApiResponse<void>>(`/groups/${id}/users`, userIds)
}

export const removeGroupUser = async (id: string, userId: string) => {
  return request.delete<ApiResponse<void>>(`/groups/${id}/users/${userId}`)
}
