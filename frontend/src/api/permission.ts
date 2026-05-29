import instance from './axios'

export interface MenuItem {
  id: string
  name: string
  title: string
  path: string
  icon?: string
  component?: string
  parentId?: string
  menuType?: string
  children?: MenuItem[]
}

export const getMenuTree = () => instance.get('/permissions/menus')

export const getPermissions = () => instance.get('/permissions/perms')
