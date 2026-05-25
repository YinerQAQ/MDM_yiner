import instance from './axios'
import type { ApiResponse, BaseOrg } from './types'

export const createOrg = async (org: BaseOrg) => {
  return instance.post<ApiResponse<BaseOrg>>('/orgs', org)
}

export const getOrgById = async (id: string) => {
  return instance.get<ApiResponse<BaseOrg>>(`/orgs/${id}`)
}

export const getAllOrgs = async () => {
  return instance.get<ApiResponse<BaseOrg[]>>('/orgs')
}

export const updateOrg = async (id: string, org: BaseOrg) => {
  return instance.put<ApiResponse<BaseOrg>>(`/orgs/${id}`, org)
}

export const deleteOrg = async (id: string) => {
  return instance.delete<ApiResponse<void>>(`/orgs/${id}`)
}

export const changeOrgStatus = async (id: string, status: string) => {
  return instance.put<ApiResponse<void>>(`/orgs/${id}/status`, undefined, {
    params: { status }
  })
}

export const getOrgTree = async (parentId: string) => {
  return instance.get<ApiResponse<BaseOrg[]>>(`/orgs/tree/${parentId}`)
}