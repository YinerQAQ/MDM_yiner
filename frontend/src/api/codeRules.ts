import instance from './axios'
import type { ApiResponse } from './types'

export const getCodeRules = async () => {
  return instance.get<ApiResponse<any[]>>('/code-rules')
}

export const getCodeRule = async (id: string) => {
  return instance.get<ApiResponse<any>>(`/code-rules/${id}`)
}

export const createCodeRule = async (data: any) => {
  return instance.post<ApiResponse<any>>('/code-rules', data)
}

export const updateCodeRule = async (id: string, data: any) => {
  return instance.put<ApiResponse<any>>(`/code-rules/${id}`, data)
}

export const deleteCodeRule = async (id: string) => {
  return instance.delete<ApiResponse<void>>(`/code-rules/${id}`)
}

// 编码方案
export const getSchemes = async (ruleId: string) => {
  return instance.get<ApiResponse<any[]>>(`/code-rules/${ruleId}/schemes`)
}

export const createScheme = async (ruleId: string, data: any) => {
  return instance.post<ApiResponse<any>>(`/code-rules/${ruleId}/schemes`, data)
}

export const updateScheme = async (schemeId: string, data: any) => {
  return instance.put<ApiResponse<any>>(`/code-rules/schemes/${schemeId}`, data)
}

export const deleteScheme = async (schemeId: string) => {
  return instance.delete<ApiResponse<void>>(`/code-rules/schemes/${schemeId}`)
}

// 编码段
export const getSegments = async (schemeId: string) => {
  return instance.get<ApiResponse<any[]>>(`/code-rules/schemes/${schemeId}/segments`)
}

export const createSegment = async (schemeId: string, data: any) => {
  return instance.post<ApiResponse<any>>(`/code-rules/schemes/${schemeId}/segments`, data)
}

export const updateSegment = async (segmentId: string, data: any) => {
  return instance.put<ApiResponse<any>>(`/code-rules/segments/${segmentId}`, data)
}

export const deleteSegment = async (segmentId: string) => {
  return instance.delete<ApiResponse<void>>(`/code-rules/segments/${segmentId}`)
}

// 测试生成
export const generateCode = async (ruleId: string, data: any) => {
  return instance.post<ApiResponse<string>>(`/code-rules/${ruleId}/generate`, data)
}
