import instance from './axios'
import type { ApiResponse, MdmDataModel } from './types'

export const createModel = async (model: MdmDataModel) => {
  return instance.post<ApiResponse<MdmDataModel>>('/data-models', model)
}

export const getModelById = async (id: string) => {
  return instance.get<ApiResponse<MdmDataModel>>(`/data-models/${id}`)
}

export const getAllModels = async () => {
  return instance.get<ApiResponse<MdmDataModel[]>>('/data-models')
}

export const updateModel = async (id: string, model: MdmDataModel) => {
  return instance.put<ApiResponse<MdmDataModel>>(`/data-models/${id}`, model)
}

export const deleteModel = async (id: string) => {
  return instance.delete<ApiResponse<void>>(`/data-models/${id}`)
}

export const submitModelForReview = async (id: string) => {
  return instance.post<ApiResponse<void>>(`/data-models/${id}/submit`)
}

export const approveModel = async (id: string) => {
  return instance.post<ApiResponse<void>>(`/data-models/${id}/approve`)
}

export const rejectModel = async (id: string) => {
  return instance.post<ApiResponse<void>>(`/data-models/${id}/reject`)
}