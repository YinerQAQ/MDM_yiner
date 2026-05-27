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

// 流程设计
export const getWorkflowDesign = async (id: string) => {
  return instance.get<ApiResponse<any>>(`/workflows/${id}/design`)
}

export const saveWorkflowDesign = async (id: string, data: any) => {
  return instance.post<ApiResponse<void>>(`/workflows/${id}/design`, data)
}

export const bindWorkflow = async (id: string, data: any) => {
  return instance.post<ApiResponse<void>>(`/workflows/${id}/bind`, data)
}

// 审核任务
export const getMyTasks = async (params?: any) => {
  return instance.get<ApiResponse<any[]>>('/workflows/tasks/mine', { params })
}

export const approveTask = async (taskId: string, data?: any) => {
  return instance.post<ApiResponse<void>>(`/workflows/tasks/${taskId}/approve`, data)
}

export const rejectTask = async (taskId: string, data?: any) => {
  return instance.post<ApiResponse<void>>(`/workflows/tasks/${taskId}/reject`, data)
}

export const transferTask = async (taskId: string, data: any) => {
  return instance.post<ApiResponse<void>>(`/workflows/tasks/${taskId}/transfer`, data)
}

export const claimTask = async (taskId: string) => {
  return instance.post<ApiResponse<void>>(`/workflows/tasks/${taskId}/claim`)
}