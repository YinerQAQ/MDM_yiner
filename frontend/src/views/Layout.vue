<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useTabsStore } from '../stores/tabs'
import MenuTree from '../components/MenuTree.vue'
import type { MenuItem } from '../api/permission'
import * as Icons from '@element-plus/icons-vue'
import {
  ElMenu,
  ElMenuItem,
  ElSubMenu,
  ElDropdown,
  ElDropdownMenu,
  ElDropdownItem,
  ElBadge,
  ElIcon,
  ElBreadcrumb,
  ElBreadcrumbItem,
  ElMessage
} from 'element-plus'
import {
  HomeFilled,
  DataBoard,
  Document,
  Connection,
  Setting,
  Bell,
  Search,
  Fold,
  Expand,
  User,
  Lock,
  SwitchButton,
  OfficeBuilding,
  UserFilled,
  Avatar,
  Menu,
  Collection,
  Operation,
  Tools,
  List,
  DataLine,
  DataAnalysis,
  Monitor,
  Guide,
  CircleCheck,
  EditPen,
  Box
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const tabsStore = useTabsStore()

const isCollapsed = ref(false)
const searchKeyword = ref('')
const searchPanelVisible = ref(false)
const searchInputRef = ref<HTMLInputElement | null>(null)
const searchBoxRef = ref<HTMLElement | null>(null)
const activeResultIndex = ref(0)

interface SearchableMenu {
  name: string
  title: string
  path: string
  icon?: string
  parentTitle: string
}

// 备用静态菜单数据（当后端菜单为空时）
const fallbackMenus: SearchableMenu[] = [
  { name: 'Dashboard', title: '首页', path: '/', icon: 'HomeFilled', parentTitle: '' },
  { name: 'DataModels', title: '数据模型管理', path: '/data-models', icon: 'DataAnalysis', parentTitle: '数据标准' },
  { name: 'CodeRules', title: '编码规则管理', path: '/code-rules', icon: 'Guide', parentTitle: '数据标准' },
  { name: 'MainData', title: '数据查询', path: '/main-data', icon: 'Search', parentTitle: '数据业务' },
  { name: 'DataApply', title: '数据申请', path: '/data-apply', icon: 'EditPen', parentTitle: '数据业务' },
  { name: 'DataReview', title: '数据审核', path: '/data-review', icon: 'CircleCheck', parentTitle: '数据业务' },
  { name: 'DataArchive', title: '数据归档', path: '/data-archive', icon: 'Box', parentTitle: '数据业务' },
  { name: 'DataDistribute', title: '数据分发', path: '/data-distribute', icon: 'DataLine', parentTitle: '数据交换' },
  { name: 'DistributeMonitor', title: '服务监控', path: '/distribute-monitor', icon: 'Monitor', parentTitle: '数据交换' },
  { name: 'Workflows', title: '流程管理', path: '/workflows', icon: 'Operation', parentTitle: '管理中心' },
  { name: 'Orgs', title: '单位管理', path: '/orgs', icon: 'OfficeBuilding', parentTitle: '管理中心' },
  { name: 'Users', title: '用户管理', path: '/users', icon: 'User', parentTitle: '管理中心' },
  { name: 'Roles', title: '角色管理', path: '/roles', icon: 'Lock', parentTitle: '管理中心' },
  { name: 'Groups', title: '用户组管理', path: '/groups', icon: 'UserFilled', parentTitle: '管理中心' },
  { name: 'Menus', title: '菜单配置', path: '/menus', icon: 'Menu', parentTitle: '管理中心' },
  { name: 'Dict', title: '数据字典', path: '/dict', icon: 'Collection', parentTitle: '管理中心' },
  { name: 'SysParams', title: '系统参数', path: '/sys-params', icon: 'Tools', parentTitle: '管理中心' },
  { name: 'SystemSettings', title: '系统设置', path: '/system-settings', icon: 'Setting', parentTitle: '管理中心' },
  { name: 'AuditLog', title: '审计日志', path: '/audit-log', icon: 'List', parentTitle: '管理中心' }
]

// 扁平化菜单树，过滤按钮与无路径项
const flattenMenus = (items: MenuItem[], parentTitle = ''): SearchableMenu[] => {
  const result: SearchableMenu[] = []
  for (const item of items || []) {
    if (item.children && item.children.length > 0) {
      result.push(...flattenMenus(item.children, item.title || ''))
    } else {
      const isButton = item.menuType === '按钮' || item.menuType === 'BUTTON'
      if (!isButton && item.path && item.path !== '#') {
        result.push({
          name: item.name,
          title: item.title,
          path: item.path,
          icon: item.icon,
          parentTitle
        })
      }
    }
  }
  return result
}

const searchableMenus = computed<SearchableMenu[]>(() => {
  const dynamic = flattenMenus(authStore.menuTree || [])
  return dynamic.length > 0 ? dynamic : fallbackMenus
})

const filteredResults = computed<SearchableMenu[]>(() => {
  const kw = searchKeyword.value.trim().toLowerCase()
  if (!kw) return []
  return searchableMenus.value
    .filter(m => {
      const title = (m.title || '').toLowerCase()
      const parent = (m.parentTitle || '').toLowerCase()
      const path = (m.path || '').toLowerCase()
      return title.includes(kw) || parent.includes(kw) || path.includes(kw)
    })
    .slice(0, 10)
})

const getIconComponent = (name?: string) => {
  if (!name) return null
  return (Icons as Record<string, any>)[name] || null
}

// 高亮匹配文本
const highlightMatch = (text: string): string => {
  const kw = searchKeyword.value.trim()
  if (!kw || !text) return text
  const escaped = kw.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${escaped})`, 'gi')
  return text.replace(regex, '<mark class="search-hl">$1</mark>')
}

const openSearchPanel = () => {
  searchPanelVisible.value = true
  activeResultIndex.value = 0
}

const closeSearchPanel = () => {
  searchPanelVisible.value = false
}

const handleSearchInput = () => {
  searchPanelVisible.value = true
  activeResultIndex.value = 0
}

const handleSearchFocus = () => {
  if (searchKeyword.value.trim()) {
    openSearchPanel()
  }
}

const goToResult = (item: SearchableMenu) => {
  if (!item || !item.path) return
  router.push(item.path)
  searchKeyword.value = ''
  closeSearchPanel()
  searchInputRef.value?.blur()
}

const handleSearchKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape') {
    e.preventDefault()
    closeSearchPanel()
    searchInputRef.value?.blur()
    return
  }
  const results = filteredResults.value
  if (e.key === 'Enter') {
    e.preventDefault()
    if (results.length > 0) {
      const idx = Math.min(activeResultIndex.value, results.length - 1)
      goToResult(results[idx])
    }
    return
  }
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    if (results.length > 0) {
      activeResultIndex.value = (activeResultIndex.value + 1) % results.length
      searchPanelVisible.value = true
    }
    return
  }
  if (e.key === 'ArrowUp') {
    e.preventDefault()
    if (results.length > 0) {
      activeResultIndex.value =
        (activeResultIndex.value - 1 + results.length) % results.length
    }
    return
  }
}

// 全局快捷键：Ctrl+K 或 / 聚焦搜索框
const handleGlobalKeydown = (e: KeyboardEvent) => {
  const target = e.target as HTMLElement | null
  const isTyping =
    target &&
    (target.tagName === 'INPUT' ||
      target.tagName === 'TEXTAREA' ||
      target.isContentEditable)

  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
    e.preventDefault()
    nextTick(() => searchInputRef.value?.focus())
    return
  }
  if (e.key === '/' && !isTyping) {
    e.preventDefault()
    nextTick(() => searchInputRef.value?.focus())
  }
}

// 点击外部关闭面板
const handleDocClick = (e: MouseEvent) => {
  if (!searchPanelVisible.value) return
  const target = e.target as Node
  if (searchBoxRef.value && !searchBoxRef.value.contains(target)) {
    closeSearchPanel()
  }
}

onMounted(() => {
  document.addEventListener('mousedown', handleDocClick)
  document.addEventListener('keydown', handleGlobalKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleDocClick)
  document.removeEventListener('keydown', handleGlobalKeydown)
})

const activeMenu = computed(() => {
  return route.path || '/'
})

// Breadcrumb
const breadcrumbs = computed(() => {
  const crumbs: { title: string; path?: string }[] = [{ title: '首页', path: '/' }]
  const menuMap: Record<string, { parent: string; title: string }> = {
    '/data-models': { parent: '数据标准', title: '数据模型管理' },
    '/code-rules': { parent: '数据标准', title: '编码规则管理' },
    '/main-data': { parent: '数据业务', title: '数据查询' },
    '/data-apply': { parent: '数据业务', title: '数据申请' },
    '/data-review': { parent: '数据业务', title: '数据审核' },
    '/data-archive': { parent: '数据业务', title: '数据归档' },
    '/data-distribute': { parent: '数据交换', title: '数据分发' },
    '/distribute-monitor': { parent: '数据交换', title: '分发监控' },
    '/data-exchange': { parent: '数据交换', title: '数据交换' },
    '/workflows': { parent: '管理中心', title: '流程管理' },
    '/orgs': { parent: '管理中心', title: '单位管理' },
    '/users': { parent: '管理中心', title: '用户管理' },
    '/roles': { parent: '管理中心', title: '角色管理' },
    '/groups': { parent: '管理中心', title: '用户组管理' },
    '/menus': { parent: '管理中心', title: '菜单配置' },
    '/dict': { parent: '管理中心', title: '数据字典' },
    '/sys-params': { parent: '管理中心', title: '系统参数' },
    '/system-settings': { parent: '管理中心', title: '系统设置' },
    '/audit-log': { parent: '管理中心', title: '审计日志' },
    '/profile': { parent: '个人中心', title: '个人信息' },
    '/change-password': { parent: '个人中心', title: '修改密码' }
  }
  const info = menuMap[route.path]
  if (info) {
    crumbs.push({ title: info.parent })
    crumbs.push({ title: info.title })
  }
  return crumbs
})

// Watch route changes to add tabs
watch(() => route.path, () => {
  if (route.name) {
    tabsStore.addTab(route)
  }
}, { immediate: true })

const handleMenuClick = (path: string) => {
  router.push(path)
}

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

const handleTabClick = (path: string) => {
  router.push(path)
}

const handleTabRemove = (path: string) => {
  const nextPath = tabsStore.removeTab(path)
  if (nextPath !== null) {
    router.push(nextPath)
  }
}

const hasDynamicMenus = computed(() => {
  return authStore.menuTree && authStore.menuTree.length > 0
})

const handleLogout = async () => {
  try {
    await authStore.logout()
    ElMessage.success('登出成功')
    router.push('/login')
  } catch (error) {
    ElMessage.error('登出失败')
  }
}
</script>

<template>
  <div class="layout">
    <!-- 左侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-logo">
        <span class="logo-text">MDM</span>
        <span v-if="!isCollapsed" class="logo-sub">主数据管理</span>
      </div>

      <div class="sidebar-menu">
        <ElMenu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          :collapse-transition="false"
          router
        >
          <MenuTree v-if="hasDynamicMenus" :menus="authStore.menuTree" />
          <template v-else>
          <ElMenuItem index="/" @click="handleMenuClick('/')">
            <ElIcon><HomeFilled /></ElIcon>
            <template #title>首页</template>
          </ElMenuItem>

          <ElSubMenu index="/data-standard">
            <template #title>
              <ElIcon><DataBoard /></ElIcon>
              <span>数据标准</span>
            </template>
            <ElMenuItem index="/data-models" @click="handleMenuClick('/data-models')">
              <ElIcon><DataAnalysis /></ElIcon>
              <template #title>数据模型管理</template>
            </ElMenuItem>
            <ElMenuItem index="/code-rules" @click="handleMenuClick('/code-rules')">
              <ElIcon><Guide /></ElIcon>
              <template #title>编码规则管理</template>
            </ElMenuItem>
          </ElSubMenu>

          <ElSubMenu index="/data-business">
            <template #title>
              <ElIcon><Document /></ElIcon>
              <span>数据业务</span>
            </template>
            <ElMenuItem index="/main-data" @click="handleMenuClick('/main-data')">
              <ElIcon><Search /></ElIcon>
              <template #title>数据查询</template>
            </ElMenuItem>
            <ElMenuItem index="/data-apply" @click="handleMenuClick('/data-apply')">
              <ElIcon><EditPen /></ElIcon>
              <template #title>数据申请</template>
            </ElMenuItem>
            <ElMenuItem index="/data-review" @click="handleMenuClick('/data-review')">
              <ElIcon><CircleCheck /></ElIcon>
              <template #title>数据审核</template>
            </ElMenuItem>
            <ElMenuItem index="/data-archive" @click="handleMenuClick('/data-archive')">
              <ElIcon><Box /></ElIcon>
              <template #title>数据归档</template>
            </ElMenuItem>
          </ElSubMenu>

          <ElSubMenu index="/data-exchange">
            <template #title>
              <ElIcon><Connection /></ElIcon>
              <span>数据交换</span>
            </template>
            <ElMenuItem index="/data-distribute" @click="handleMenuClick('/data-distribute')">
              <ElIcon><DataLine /></ElIcon>
              <template #title>数据分发</template>
            </ElMenuItem>
            <ElMenuItem index="/distribute-monitor" @click="handleMenuClick('/distribute-monitor')">
              <ElIcon><Monitor /></ElIcon>
              <template #title>服务监控</template>
            </ElMenuItem>
          </ElSubMenu>

          <ElSubMenu index="/admin-center">
            <template #title>
              <ElIcon><Setting /></ElIcon>
              <span>管理中心</span>
            </template>
            <ElMenuItem index="/workflows" @click="handleMenuClick('/workflows')">
              <ElIcon><Operation /></ElIcon>
              <template #title>流程管理</template>
            </ElMenuItem>
            <ElMenuItem index="/orgs" @click="handleMenuClick('/orgs')">
              <ElIcon><OfficeBuilding /></ElIcon>
              <template #title>单位管理</template>
            </ElMenuItem>
            <ElMenuItem index="/users" @click="handleMenuClick('/users')">
              <ElIcon><User /></ElIcon>
              <template #title>用户管理</template>
            </ElMenuItem>
            <ElMenuItem index="/roles" @click="handleMenuClick('/roles')">
              <ElIcon><Lock /></ElIcon>
              <template #title>角色管理</template>
            </ElMenuItem>
            <ElMenuItem index="/groups" @click="handleMenuClick('/groups')">
              <ElIcon><UserFilled /></ElIcon>
              <template #title>用户组管理</template>
            </ElMenuItem>
            <ElMenuItem index="/menus" @click="handleMenuClick('/menus')">
              <ElIcon><Menu /></ElIcon>
              <template #title>菜单配置</template>
            </ElMenuItem>
            <ElMenuItem index="/dict" @click="handleMenuClick('/dict')">
              <ElIcon><Collection /></ElIcon>
              <template #title>数据字典</template>
            </ElMenuItem>
            <ElMenuItem index="/sys-params" @click="handleMenuClick('/sys-params')">
              <ElIcon><Tools /></ElIcon>
              <template #title>系统参数</template>
            </ElMenuItem>
            <ElMenuItem index="/system-settings" @click="handleMenuClick('/system-settings')">
              <ElIcon><Setting /></ElIcon>
              <template #title>系统设置</template>
            </ElMenuItem>
            <ElMenuItem index="/audit-log" @click="handleMenuClick('/audit-log')">
              <ElIcon><List /></ElIcon>
              <template #title>审计日志</template>
            </ElMenuItem>
          </ElSubMenu>
          </template>
        </ElMenu>
      </div>

      <div class="sidebar-footer" @click="toggleCollapse">
        <ElIcon :size="18">
          <Fold v-if="!isCollapsed" />
          <Expand v-else />
        </ElIcon>
      </div>
    </aside>

    <!-- 右侧区域 -->
    <div class="main-area">
      <!-- 顶栏 -->
      <header class="header">
        <div class="header-left">
          <ElBreadcrumb separator="/">
            <ElBreadcrumbItem
              v-for="(crumb, idx) in breadcrumbs"
              :key="idx"
              :to="crumb.path ? { path: crumb.path } : undefined"
            >
              {{ crumb.title }}
            </ElBreadcrumbItem>
          </ElBreadcrumb>
        </div>

        <div class="header-right">
          <div class="search-box" ref="searchBoxRef">
            <ElIcon class="search-icon"><Search /></ElIcon>
            <input
              ref="searchInputRef"
              v-model="searchKeyword"
              type="text"
              placeholder="搜索菜单功能 ( Ctrl+K )"
              class="search-input"
              @input="handleSearchInput"
              @focus="handleSearchFocus"
              @keydown="handleSearchKeydown"
            />
            <span
              v-if="searchKeyword"
              class="search-clear"
              @click="searchKeyword = ''; closeSearchPanel()"
            >&times;</span>

            <transition name="search-fade">
              <div
                v-if="searchPanelVisible && searchKeyword.trim()"
                class="search-panel"
              >
                <template v-if="filteredResults.length > 0">
                  <div class="search-panel-header">
                    <span>找到 {{ filteredResults.length }} 个匹配</span>
                    <span class="search-panel-tip">↑↓ 切换 · Enter 跳转 · Esc 关闭</span>
                  </div>
                  <ul class="search-result-list">
                    <li
                      v-for="(item, idx) in filteredResults"
                      :key="item.path + item.name"
                      class="search-result-item"
                      :class="{ active: idx === activeResultIndex }"
                      @mouseenter="activeResultIndex = idx"
                      @click="goToResult(item)"
                    >
                      <ElIcon class="result-icon" v-if="getIconComponent(item.icon)">
                        <component :is="getIconComponent(item.icon)" />
                      </ElIcon>
                      <ElIcon class="result-icon" v-else><Document /></ElIcon>
                      <span class="result-title" v-html="highlightMatch(item.title)" />
                      <span class="result-path" v-html="highlightMatch(item.path)" />
                      <span class="result-parent" v-if="item.parentTitle">{{ item.parentTitle }}</span>
                    </li>
                  </ul>
                </template>
                <div v-else class="search-empty">
                  <ElIcon><Search /></ElIcon>
                  <span>无匹配菜单</span>
                </div>
              </div>
            </transition>
          </div>

          <ElBadge :value="3" :max="99" class="notification-badge">
            <ElIcon :size="20" class="notification-icon"><Bell /></ElIcon>
          </ElBadge>

          <ElDropdown trigger="click">
            <div class="user-info">
              <div class="user-avatar">
                <ElIcon :size="16"><Avatar /></ElIcon>
              </div>
              <span class="user-name">{{ authStore.nickname || authStore.username || '用户' }}</span>
            </div>
            <template #dropdown>
              <ElDropdownMenu>
                <ElDropdownItem @click="router.push('/profile')">
                  <ElIcon><User /></ElIcon>个人信息
                </ElDropdownItem>
                <ElDropdownItem @click="router.push('/change-password')">
                  <ElIcon><Lock /></ElIcon>修改密码
                </ElDropdownItem>
                <ElDropdownItem divided @click="handleLogout">
                  <ElIcon><SwitchButton /></ElIcon>退出登录
                </ElDropdownItem>
              </ElDropdownMenu>
            </template>
          </ElDropdown>
        </div>
      </header>

      <!-- 标签页 -->
      <div class="tab-bar">
        <div
          v-for="tab in tabsStore.tabs"
          :key="tab.path"
          class="tab-item"
          :class="{ active: tabsStore.activeTab === tab.path }"
          @click="handleTabClick(tab.path)"
        >
          <span class="tab-title">{{ tab.title }}</span>
          <span
            v-if="tab.closable"
            class="tab-close"
            @click.stop="handleTabRemove(tab.path)"
          >&times;</span>
        </div>
      </div>

      <!-- 主内容 -->
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped lang="scss">
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, var(--bg-primary) 0%, var(--bg-secondary) 100%);
}

// 侧边栏
.sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-sidebar);
  backdrop-filter: blur(20px);
  border-right: 1px solid var(--border-color);
  transition: width var(--transition-normal);
  overflow: hidden;
  flex-shrink: 0;

  &.collapsed {
    width: var(--sidebar-collapsed-width);
  }
}

.sidebar-logo {
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
  padding: 0 16px;

  .logo-text {
    font-size: 24px;
    font-weight: 800;
    background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    letter-spacing: 2px;
    text-shadow: none;
  }

  .logo-sub {
    font-size: 12px;
    color: var(--text-secondary);
    white-space: nowrap;
  }
}

.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 0;

  :deep(.el-menu) {
    border-right: none;
  }

  :deep(.el-sub-menu .el-sub-menu__title) {
    height: 44px;
    line-height: 44px;
    margin: 2px 8px;
    border-radius: var(--radius-sm);

    .el-sub-menu__icon-arrow {
      color: var(--text-muted);
    }
  }

  :deep(.el-menu-item) {
    height: 40px;
    line-height: 40px;
    padding-left: 52px !important;
    margin: 1px 8px;
    border-radius: var(--radius-sm);
    font-size: 13px;
    position: relative;
  }

  :deep(.el-menu-item .el-icon) {
    font-size: 16px;
  }
}

.sidebar-footer {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-top: 1px solid var(--border-color);
  color: var(--text-muted);
  transition: var(--transition-fast);
  flex-shrink: 0;

  &:hover {
    color: var(--color-primary);
    background: rgba(0, 212, 255, 0.05);
  }
}

// 右侧区域
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

// 顶栏
.header {
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: var(--bg-header);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  // 提升搜索框栈上下文，确保下拉面板高于其他 header 元素
  z-index: 9998;

  .search-icon {
    position: absolute;
    left: 10px;
    color: var(--text-muted);
    font-size: 14px;
    z-index: 1;
    pointer-events: none;
  }

  .search-input {
    background: rgba(13, 27, 42, 0.6);
    border: 1px solid var(--border-color);
    border-radius: 20px;
    padding: 6px 30px 6px 34px;
    color: var(--text-primary);
    font-size: 13px;
    width: 220px;
    outline: none;
    transition: var(--transition-normal);

    &::placeholder {
      color: var(--text-muted);
    }

    &:focus {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 2px rgba(0, 168, 204, 0.18);
      width: 300px;
    }
  }

  .search-clear {
    position: absolute;
    right: 12px;
    top: 50%;
    transform: translateY(-50%);
    width: 16px;
    height: 16px;
    line-height: 14px;
    text-align: center;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.08);
    color: var(--text-muted);
    cursor: pointer;
    font-size: 14px;
    transition: var(--transition-fast);
    z-index: 2;

    &:hover {
      background: rgba(0, 168, 204, 0.25);
      color: var(--color-primary);
    }
  }
}

// 搜索结果下拉面板
.search-panel {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 360px;
  max-width: 480px;
  background: rgba(10, 22, 36, 0.92);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border: 1px solid rgba(0, 168, 204, 0.22);
  border-radius: 12px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.45),
    0 0 0 1px rgba(255, 255, 255, 0.02) inset;
  // 置顶：高于 Element Plus 默认 popper / overlay (3000+)
  z-index: 9999;
  overflow: hidden;
}

// 确保 header 自身建立栈上下文且层级足够高，防止被内容区或弹窗覆盖
.header {
  position: relative;
  z-index: 100;
}

.search-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 14px;
  font-size: 12px;
  color: var(--text-muted);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(0, 168, 204, 0.04);

  .search-panel-tip {
    color: rgba(255, 255, 255, 0.35);
    font-size: 11px;
    letter-spacing: 0.3px;
  }
}

.search-result-list {
  list-style: none;
  margin: 0;
  padding: 4px;
  max-height: 380px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 168, 204, 0.25);
    border-radius: 3px;
  }
}

.search-result-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-primary);
  transition: background var(--transition-fast), transform var(--transition-fast);

  .result-icon {
    color: var(--color-primary);
    font-size: 15px;
    flex-shrink: 0;
  }

  .result-title {
    color: var(--text-primary);
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 160px;
  }

  .result-path {
    flex: 1;
    color: var(--text-muted);
    font-size: 12px;
    font-family: 'JetBrains Mono', 'Consolas', monospace;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .result-parent {
    flex-shrink: 0;
    padding: 2px 8px;
    font-size: 11px;
    color: rgba(0, 168, 204, 0.85);
    background: rgba(0, 168, 204, 0.12);
    border: 1px solid rgba(0, 168, 204, 0.25);
    border-radius: 10px;
    white-space: nowrap;
  }

  &.active,
  &:hover {
    background: rgba(0, 168, 204, 0.14);
    transform: translateX(2px);

    .result-icon {
      color: var(--color-primary);
    }
  }
}

.search-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 28px 16px;
  color: var(--text-muted);
  font-size: 13px;

  .el-icon {
    font-size: 16px;
    opacity: 0.6;
  }
}

:deep(.search-hl) {
  background: transparent;
  color: var(--color-primary);
  font-weight: 600;
  padding: 0;
}

.search-fade-enter-active,
.search-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.search-fade-enter-from,
.search-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.notification-badge {
  cursor: pointer;

  .notification-icon {
    color: var(--text-secondary);
    transition: color var(--transition-fast);

    &:hover {
      color: var(--color-primary);
    }
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  transition: var(--transition-fast);

  &:hover {
    background: rgba(0, 212, 255, 0.08);
  }

  .user-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }

  .user-name {
    color: var(--text-primary);
    font-size: 14px;
    font-weight: 500;
  }
}

// 标签页
.tab-bar {
  display: flex;
  align-items: center;
  height: 36px;
  padding: 0 12px;
  background: rgba(13, 27, 42, 0.4);
  border-bottom: 1px solid var(--border-color);
  overflow-x: auto;
  flex-shrink: 0;

  &::-webkit-scrollbar {
    height: 0;
  }
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 12px;
  height: 28px;
  font-size: 12px;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: 4px;
  white-space: nowrap;
  transition: var(--transition-fast);
  border: 1px solid transparent;
  margin-right: 4px;
  flex-shrink: 0;

  &:hover {
    color: var(--text-primary);
    background: rgba(0, 212, 255, 0.05);
  }

  &.active {
    color: var(--color-primary);
    background: rgba(0, 212, 255, 0.1);
    border-color: rgba(0, 212, 255, 0.2);
  }

  .tab-close {
    font-size: 14px;
    line-height: 1;
    color: var(--text-muted);
    margin-left: 4px;

    &:hover {
      color: var(--color-danger);
    }
  }
}

// 主内容区
.content {
  flex: 1;
  overflow-y: auto;
  padding: 0;
  background: linear-gradient(135deg, var(--bg-primary) 0%, var(--bg-secondary) 100%);
}
</style>
