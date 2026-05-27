import request from './axios'
import type { ApiResponse } from './types'

export interface BaseDict {
  id: string
  dictCode: string
  dictName: string
  description: string
  status: string
  createTime?: string
  updateTime?: string
}

export interface BaseDictItem {
  id: string
  dictId: string
  dictCode: string
  itemValue: string
  itemLabel: string
  sortOrder: number
  status: string
  createTime?: string
  updateTime?: string
}

export const getDicts = async () => {
  return request.get<ApiResponse<BaseDict[]>>('/dicts')
}

export const getDictItems = async (dictCode: string) => {
  return request.get<ApiResponse<BaseDictItem[]>>(`/dicts/${dictCode}/items`)
}

export const createDict = async (dict: BaseDict) => {
  return request.post<ApiResponse<void>>('/dicts', dict)
}

export const updateDict = async (id: string, dict: BaseDict) => {
  return request.put<ApiResponse<void>>(`/dicts/${id}`, dict)
}

export const deleteDict = async (id: string) => {
  return request.delete<ApiResponse<void>>(`/dicts/${id}`)
}

export const createDictItem = async (dictId: string, item: BaseDictItem) => {
  return request.post<ApiResponse<void>>(`/dicts/${dictId}/items`, item)
}

export const updateDictItem = async (id: string, item: BaseDictItem) => {
  return request.put<ApiResponse<void>>(`/dicts/items/${id}`, item)
}

export const deleteDictItem = async (id: string) => {
  return request.delete<ApiResponse<void>>(`/dicts/items/${id}`)
}
