import instance from './axios'
import type { ApiResponse, MdmMainData, MdmModelAttribute, ImportResultResponse } from './types'

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

// 数据导入
export const importMainData = async (modelId: string, formData: FormData, importType: string = 'NORMAL') => {
  return instance.post<ApiResponse<ImportResultResponse>>(
    `/main-data/import?modelId=${modelId}&importType=${importType}`,
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  )
}

// 数据导出
export const exportMainData = async (modelId: string, fieldIds: string[] = [], format: string = 'EXCEL') => {
  const params: any = { modelId, format }
  if (fieldIds.length > 0) {
    params.fieldIds = fieldIds.join(',')
  }
  return instance.get('/main-data/export', {
    params,
    responseType: 'blob'
  }).then((response) => {
    const blob = new Blob([response.data as any])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    const ext = format === 'CSV' ? '.csv' : '.xlsx'
    link.download = `export_data${ext}`
    link.click()
    window.URL.revokeObjectURL(url)
    return response
  })
}

// 下载导入模板
export const downloadImportTemplate = async (modelId: string) => {
  return instance.get('/main-data/import-template', {
    params: { modelId },
    responseType: 'blob'
  }).then((response) => {
    const blob = new Blob([response.data as any])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'import_template.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    return response
  })
}

// 批量编辑
export const batchEditMainData = async (ids: string[], fields: Record<string, any>) => {
  return instance.put<ApiResponse<void>>('/main-data/batch-edit', { ids, fields })
}

// 获取模型属性列表
export const getModelAttributes = async (modelId: string) => {
  return instance.get<ApiResponse<MdmModelAttribute[]>>(`/data-models/${modelId}/attributes`)
}