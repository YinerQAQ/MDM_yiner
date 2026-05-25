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