import instance from './axios'
import type { ApiResponse, MdmWorkflow } from './types'

export const createWorkflow = async (workflow: MdmWorkflow) => {
  return instance.post<ApiResponse<MdmWorkflow>>('/workflows', workflow)
}

export const getWorkflowById = async (id: string) => {
  return instance.get<ApiResponse<MdmWorkflow>>(`/workflows/${id}`)
}

export const getAllWorkflows = async () => {
  return instance.get<ApiResponse<MdmWorkflow[]>>('/workflows')
}

export const updateWorkflow = async (id: string, workflow: MdmWorkflow) => {
  return instance.put<ApiResponse<MdmWorkflow>>(`/workflows/${id}`, workflow)
}

export const deleteWorkflow = async (id: string) => {
  return instance.delete<ApiResponse<void>>(`/workflows/${id}`)
}

export const activateWorkflow = async (id: string) => {
  return instance.post<ApiResponse<void>>(`/workflows/${id}/activate`)
}

export const deactivateWorkflow = async (id: string) => {
  return instance.post<ApiResponse<void>>(`/workflows/${id}/deactivate`)
}