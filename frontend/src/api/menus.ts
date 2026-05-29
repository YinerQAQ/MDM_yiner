import request from './axios'
import type { ApiResponse } from './types'

export interface BaseMenu {
  id: string
  menuCode: string
  menuName: string
  parentId: string
  menuType: string
  path: string
  component: string
  icon: string
  sortOrder: number
  status: string
  visible: string
  perms?: string
  children?: BaseMenu[]
}

export interface MenuSortItem {
  id: string
  sortOrder: number
}

export const getMenuTree = async () => {
  return request.get<ApiResponse<BaseMenu[]>>('/menus/tree')
}

export const getAllMenus = async () => {
  return request.get<ApiResponse<BaseMenu[]>>('/menus/all')
}

export const createMenu = async (menu: BaseMenu) => {
  return request.post<ApiResponse<void>>('/menus', menu)
}

export const updateMenu = async (id: string, menu: BaseMenu) => {
  return request.put<ApiResponse<void>>(`/menus/${id}`, menu)
}

export const deleteMenu = async (id: string) => {
  return request.delete<ApiResponse<void>>(`/menus/${id}`)
}

export const updateMenuSort = async (sortList: MenuSortItem[]) => {
  return request.put<ApiResponse<void>>('/menus/sort', sortList)
}
