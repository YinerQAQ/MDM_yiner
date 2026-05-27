import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouteLocationNormalized } from 'vue-router'

export interface TabItem {
  name: string
  path: string
  title: string
  closable: boolean
}

export const useTabsStore = defineStore('tabs', () => {
  const tabs = ref<TabItem[]>([
    { name: 'Dashboard', path: '/', title: '首页', closable: false }
  ])
  const activeTab = ref('/')

  const addTab = (route: RouteLocationNormalized) => {
    const name = route.name as string
    if (!name) return

    const titleMap: Record<string, string> = {
      Dashboard: '首页',
      DataModels: '数据模型管理',
      CodeRules: '编码规则管理',
      MainData: '数据查询',
      DataApply: '数据申请',
      DataReview: '数据审核',
      DataArchive: '数据归档',
      DataDistribute: '数据分发',
      DistributeMonitor: '分发监控',
      Workflows: '流程管理',
      Orgs: '单位管理',
      Users: '用户管理',
      Roles: '角色管理',
      Groups: '用户组管理',
      Menus: '菜单配置',
      Dict: '数据字典',
      SysParams: '系统参数',
      SystemSettings: '系统设置',
      AuditLog: '审计日志',
      DataExchange: '数据交换'
    }

    const exists = tabs.value.find(t => t.name === name)
    if (!exists) {
      tabs.value.push({
        name,
        path: route.path,
        title: titleMap[name] || name,
        closable: name !== 'Dashboard'
      })
    }
    activeTab.value = route.path
  }

  const removeTab = (path: string) => {
    const index = tabs.value.findIndex(t => t.path === path)
    if (index === -1) return null

    const tab = tabs.value[index]
    if (!tab.closable) return null

    tabs.value.splice(index, 1)

    // If we removed the active tab, switch to the nearest one
    if (activeTab.value === path) {
      const newIndex = Math.min(index, tabs.value.length - 1)
      return tabs.value[newIndex]?.path || '/'
    }
    return null
  }

  return {
    tabs,
    activeTab,
    addTab,
    removeTab
  }
})
