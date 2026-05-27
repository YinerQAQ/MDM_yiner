import request from './axios'
import type { ApiResponse } from './types'

export interface SysParam {
  id: string
  paramKey: string
  paramValue: string
  paramName: string
  paramType: string
  description: string
  status: string
  createTime?: string
  updateTime?: string
}

export interface SysAuditLog {
  id: number
  userId: string
  username: string
  operation: string
  method: string
  params: string
  ip: string
  result: string
  duration: number
  createTime: string
}

export interface SysLoginLog {
  id: number
  userId: string
  username: string
  ip: string
  location: string
  browser: string
  os: string
  status: string
  message: string
  createTime: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const getSysParams = async () => {
  return request.get<ApiResponse<SysParam[]>>('/sys-params')
}

export const getParamValue = async (key: string) => {
  return request.get<ApiResponse<string>>(`/sys-params/value/${key}`)
}

export const updateParam = async (id: string, param: SysParam) => {
  return request.put<ApiResponse<void>>(`/sys-params/${id}`, param)
}

export const getAuditLogs = async (params: {
  username?: string
  operation?: string
  startTime?: string
  endTime?: string
  page?: number
  size?: number
}) => {
  return request.get<ApiResponse<PageResult<SysAuditLog>>>('/audit-logs', { params })
}

export const getLoginLogs = async (params: {
  username?: string
  status?: string
  startTime?: string
  endTime?: string
  page?: number
  size?: number
}) => {
  return request.get<ApiResponse<PageResult<SysLoginLog>>>('/audit-logs/login-logs', { params })
}
