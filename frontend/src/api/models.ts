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

// 模型属性管理
export const getModelAttributes = async (modelId: string) => {
  return instance.get<ApiResponse<any[]>>(`/data-models/${modelId}/attributes`)
}

export const addModelAttribute = async (modelId: string, data: any) => {
  return instance.post<ApiResponse<any>>(`/data-models/${modelId}/attributes`, data)
}

export const updateModelAttribute = async (modelId: string, attrId: string, data: any) => {
  return instance.put<ApiResponse<any>>(`/data-models/${modelId}/attributes/${attrId}`, data)
}

export const deleteModelAttribute = async (modelId: string, attrId: string) => {
  return instance.delete<ApiResponse<void>>(`/data-models/${modelId}/attributes/${attrId}`)
}

// 模型生命周期
export const publishModel = async (id: string) => {
  return instance.post<ApiResponse<void>>(`/data-models/${id}/publish`)
}

export const changeModel = async (id: string) => {
  return instance.post<ApiResponse<void>>(`/data-models/${id}/change`)
}

// 约束管理
export const getModelConstraints = async (modelId: string) => {
  return instance.get<ApiResponse<any[]>>(`/data-models/${modelId}/constraints`)
}

export const createConstraint = async (modelId: string, data: any) => {
  return instance.post<ApiResponse<any>>(`/data-models/${modelId}/constraints`, data)
}

export const updateConstraint = async (modelId: string, id: string, data: any) => {
  return instance.put<ApiResponse<any>>(`/data-models/${modelId}/constraints/${id}`, data)
}

export const deleteConstraint = async (modelId: string, id: string) => {
  return instance.delete<ApiResponse<void>>(`/data-models/${modelId}/constraints/${id}`)
}