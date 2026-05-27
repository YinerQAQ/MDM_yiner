import instance from './axios'
import type { ApiResponse, MdmMainData } from './types'

export const createMainData = async (mainData: MdmMainData) => {
  return instance.post<ApiResponse<MdmMainData>>('/main-data', mainData)
}

export const getMainDataById = async (id: string) => {
  return instance.get<ApiResponse<MdmMainData>>(`/main-data/${id}`)
}

export const getMainDataByModelId = async (modelId: string) => {
  return instance.get<ApiResponse<MdmMainData[]>>(`/main-data/model/${modelId}`)
}

export const updateMainData = async (id: string, mainData: MdmMainData) => {
  return instance.put<ApiResponse<MdmMainData>>(`/main-data/${id}`, mainData)
}

export const deleteMainData = async (id: string) => {
  return instance.delete<ApiResponse<void>>(`/main-data/${id}`)
}

export const submitMainDataForReview = async (id: string) => {
  return instance.post<ApiResponse<void>>(`/main-data/${id}/submit`)
}

export const approveMainData = async (id: string) => {
  return instance.post<ApiResponse<void>>(`/main-data/${id}/approve`)
}

export const rejectMainData = async (id: string) => {
  return instance.post<ApiResponse<void>>(`/main-data/${id}/reject`)
}

export const archiveMainData = async (id: string) => {
  return instance.post<ApiResponse<void>>(`/main-data/${id}/archive`)
}

export const createVersion = async (id: string) => {
  return instance.post<ApiResponse<void>>(`/main-data/${id}/version`)
}

// 数据生命周期
export const submitData = async (id: string, data?: any) => {
  return instance.post<ApiResponse<void>>(`/main-data/${id}/submit`, data)
}

export const approveData = async (id: string) => {
  return instance.post<ApiResponse<void>>(`/main-data/${id}/approve`)
}

export const rejectData = async (id: string) => {
  return instance.post<ApiResponse<void>>(`/main-data/${id}/reject`)
}

export const withdrawData = async (id: string) => {
  return instance.post<ApiResponse<void>>(`/main-data/${id}/withdraw`)
}

export const changeData = async (id: string) => {
  return instance.post<ApiResponse<void>>(`/main-data/${id}/change`)
}

export const getVersionHistory = async (id: string) => {
  return instance.get<ApiResponse<any[]>>(`/main-data/${id}/versions`)
}

// 归档管理
export const archiveApply = async (data: any) => {
  return instance.post<ApiResponse<any>>('/main-data/archive/apply', data)
}

export const archiveApprove = async (applyId: string) => {
  return instance.post<ApiResponse<void>>(`/main-data/archive/${applyId}/approve`)
}

export const archiveReject = async (applyId: string, data: any) => {
  return instance.post<ApiResponse<void>>(`/main-data/archive/${applyId}/reject`, data)
}

export const getArchiveList = async (params?: any) => {
  return instance.get<ApiResponse<any[]>>('/main-data/archive/list', { params })
}

export const getArchiveData = async (params?: any) => {
  return instance.get<ApiResponse<any[]>>('/main-data/archive/data', { params })
}