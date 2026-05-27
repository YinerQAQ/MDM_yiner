import instance from './axios'
import type { ApiResponse } from './types'

// 分发接口管理
export const getDistByModel = async (modelId: string) => {
  return instance.get<ApiResponse<any[]>>(`/esb/distribute/model/${modelId}`)
}

export const getDistById = async (id: string) => {
  return instance.get<ApiResponse<any>>(`/esb/distribute/${id}`)
}

export const createDist = async (data: any) => {
  return instance.post<ApiResponse<any>>('/esb/distribute', data)
}

export const updateDist = async (id: string, data: any) => {
  return instance.put<ApiResponse<any>>(`/esb/distribute/${id}`, data)
}

export const deleteDist = async (id: string) => {
  return instance.delete<ApiResponse<void>>(`/esb/distribute/${id}`)
}

export const enableDist = async (id: string) => {
  return instance.put<ApiResponse<void>>(`/esb/distribute/${id}/enable`)
}

export const disableDist = async (id: string) => {
  return instance.put<ApiResponse<void>>(`/esb/distribute/${id}/disable`)
}

// 分发内容
export const getDistContent = async (id: string) => {
  return instance.get<ApiResponse<any>>(`/esb/distribute/${id}/content`)
}

export const saveDistContent = async (id: string, data: any) => {
  return instance.put<ApiResponse<void>>(`/esb/distribute/${id}/content`, data)
}

// 执行
export const executeDist = async (id: string, dataIds: string[]) => {
  return instance.post<ApiResponse<any>>(`/esb/distribute/${id}/execute`, dataIds)
}

// 监控
export const getDistData = async (distId: string, params?: any) => {
  return instance.get<ApiResponse<any[]>>(`/esb/monitor/${distId}/data`, { params })
}

export const getDistRecords = async (distId: string, params?: any) => {
  return instance.get<ApiResponse<any[]>>(`/esb/monitor/${distId}/records`, { params })
}

// 信息系统
export const getSystems = async () => {
  return instance.get<ApiResponse<any[]>>('/esb/systems')
}

export const createSystem = async (data: any) => {
  return instance.post<ApiResponse<any>>('/esb/systems', data)
}

export const updateSystem = async (id: string, data: any) => {
  return instance.put<ApiResponse<any>>(`/esb/systems/${id}`, data)
}

export const deleteSystem = async (id: string) => {
  return instance.delete<ApiResponse<void>>(`/esb/systems/${id}`)
}
