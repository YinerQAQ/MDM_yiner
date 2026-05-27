<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useTabsStore } from '../stores/tabs'
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
  Management,
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
  Files,
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

const activeMenu = computed(() => {
  const pathMap: Record<string, string> = {
    '/': 'Dashboard',
    '/data-models': 'DataModels',
    '/code-rules': 'CodeRules',
    '/main-data': 'MainData',
    '/data-apply': 'DataApply',
    '/data-review': 'DataReview',
    '/data-archive': 'DataArchive',
    '/data-distribute': 'DataDistribute',
    '/distribute-monitor': 'DistributeMonitor',
    '/workflows': 'Workflows',
    '/orgs': 'Orgs',
    '/users': 'Users',
    '/roles': 'Roles',
    '/groups': 'Groups',
    '/menus': 'Menus',
    '/dict': 'Dict',
    '/sys-params': 'SysParams',
    '/system-settings': 'SystemSettings',
    '/audit-log': 'AuditLog',
    '/data-exchange': 'DataExchange'
  }
  return pathMap[route.path] || 'Dashboard'
})

// Breadcrumb
const breadcrumbs = computed(() => {
  const crumbs: { title: string; path?: string }[] = [{ title: '首页', path: '/' }]
  const menuMap: Record<string, { parent: string; title: string }> = {
    DataModels: { parent: '数据标准', title: '数据模型管理' },
    CodeRules: { parent: '数据标准', title: '编码规则管理' },
    MainData: { parent: '数据业务', title: '数据查询' },
    DataApply: { parent: '数据业务', title: '数据申请' },
    DataReview: { parent: '数据业务', title: '数据审核' },
    DataArchive: { parent: '数据业务', title: '数据归档' },
    DataDistribute: { parent: '数据交换', title: '数据分发' },
    DistributeMonitor: { parent: '数据交换', title: '分发监控' },
    Workflows: { parent: '管理中心', title: '流程管理' },
    Orgs: { parent: '管理中心', title: '单位管理' },
    Users: { parent: '管理中心', title: '用户管理' },
    Roles: { parent: '管理中心', title: '角色管理' },
    Groups: { parent: '管理中心', title: '用户组管理' },
    Menus: { parent: '管理中心', title: '菜单配置' },
    Dict: { parent: '管理中心', title: '数据字典' },
    SysParams: { parent: '管理中心', title: '系统参数' },
    SystemSettings: { parent: '管理中心', title: '系统设置' },
    AuditLog: { parent: '管理中心', title: '审计日志' },
    DataExchange: { parent: '数据交换', title: '数据交换' }
  }
  const info = menuMap[activeMenu.value]
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
          <ElMenuItem index="Dashboard" @click="handleMenuClick('/')">
            <ElIcon><HomeFilled /></ElIcon>
            <template #title>首页</template>
          </ElMenuItem>

          <ElSubMenu index="data-standard">
            <template #title>
              <ElIcon><DataBoard /></ElIcon>
              <span>数据标准</span>
            </template>
            <ElMenuItem index="DataModels" @click="handleMenuClick('/data-models')">
              <ElIcon><DataAnalysis /></ElIcon>
              <template #title>数据模型管理</template>
            </ElMenuItem>
            <ElMenuItem index="CodeRules" @click="handleMenuClick('/code-rules')">
              <ElIcon><Guide /></ElIcon>
              <template #title>编码规则管理</template>
            </ElMenuItem>
          </ElSubMenu>

          <ElSubMenu index="data-business">
            <template #title>
              <ElIcon><Document /></ElIcon>
              <span>数据业务</span>
            </template>
            <ElMenuItem index="MainData" @click="handleMenuClick('/main-data')">
              <ElIcon><Search /></ElIcon>
              <template #title>数据查询</template>
            </ElMenuItem>
            <ElMenuItem index="DataApply" @click="handleMenuClick('/data-apply')">
              <ElIcon><EditPen /></ElIcon>
              <template #title>数据申请</template>
            </ElMenuItem>
            <ElMenuItem index="DataReview" @click="handleMenuClick('/data-review')">
              <ElIcon><CircleCheck /></ElIcon>
              <template #title>数据审核</template>
            </ElMenuItem>
            <ElMenuItem index="DataArchive" @click="handleMenuClick('/data-archive')">
              <ElIcon><Box /></ElIcon>
              <template #title>数据归档</template>
            </ElMenuItem>
          </ElSubMenu>

          <ElSubMenu index="data-exchange">
            <template #title>
              <ElIcon><Connection /></ElIcon>
              <span>数据交换</span>
            </template>
            <ElMenuItem index="DataDistribute" @click="handleMenuClick('/data-distribute')">
              <ElIcon><DataLine /></ElIcon>
              <template #title>数据分发</template>
            </ElMenuItem>
            <ElMenuItem index="DistributeMonitor" @click="handleMenuClick('/distribute-monitor')">
              <ElIcon><Monitor /></ElIcon>
              <template #title>服务监控</template>
            </ElMenuItem>
          </ElSubMenu>

          <ElSubMenu index="admin-center">
            <template #title>
              <ElIcon><Setting /></ElIcon>
              <span>管理中心</span>
            </template>
            <ElMenuItem index="Workflows" @click="handleMenuClick('/workflows')">
              <ElIcon><Operation /></ElIcon>
              <template #title>流程管理</template>
            </ElMenuItem>
            <ElMenuItem index="Orgs" @click="handleMenuClick('/orgs')">
              <ElIcon><OfficeBuilding /></ElIcon>
              <template #title>单位管理</template>
            </ElMenuItem>
            <ElMenuItem index="Users" @click="handleMenuClick('/users')">
              <ElIcon><User /></ElIcon>
              <template #title>用户管理</template>
            </ElMenuItem>
            <ElMenuItem index="Roles" @click="handleMenuClick('/roles')">
              <ElIcon><Lock /></ElIcon>
              <template #title>角色管理</template>
            </ElMenuItem>
            <ElMenuItem index="Groups" @click="handleMenuClick('/groups')">
              <ElIcon><UserFilled /></ElIcon>
              <template #title>用户组管理</template>
            </ElMenuItem>
            <ElMenuItem index="Menus" @click="handleMenuClick('/menus')">
              <ElIcon><Menu /></ElIcon>
              <template #title>菜单配置</template>
            </ElMenuItem>
            <ElMenuItem index="Dict" @click="handleMenuClick('/dict')">
              <ElIcon><Collection /></ElIcon>
              <template #title>数据字典</template>
            </ElMenuItem>
            <ElMenuItem index="SysParams" @click="handleMenuClick('/sys-params')">
              <ElIcon><Tools /></ElIcon>
              <template #title>系统参数</template>
            </ElMenuItem>
            <ElMenuItem index="SystemSettings" @click="handleMenuClick('/system-settings')">
              <ElIcon><Setting /></ElIcon>
              <template #title>系统设置</template>
            </ElMenuItem>
            <ElMenuItem index="AuditLog" @click="handleMenuClick('/audit-log')">
              <ElIcon><List /></ElIcon>
              <template #title>审计日志</template>
            </ElMenuItem>
          </ElSubMenu>
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
          <div class="search-box">
            <ElIcon class="search-icon"><Search /></ElIcon>
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索功能..."
              class="search-input"
            />
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
                <ElDropdownItem>
                  <ElIcon><User /></ElIcon>个人信息
                </ElDropdownItem>
                <ElDropdownItem>
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

  .search-icon {
    position: absolute;
    left: 10px;
    color: var(--text-muted);
    font-size: 14px;
  }

  .search-input {
    background: rgba(13, 27, 42, 0.6);
    border: 1px solid var(--border-color);
    border-radius: 20px;
    padding: 6px 16px 6px 34px;
    color: var(--text-primary);
    font-size: 13px;
    width: 200px;
    outline: none;
    transition: var(--transition-normal);

    &::placeholder {
      color: var(--text-muted);
    }

    &:focus {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 2px rgba(0, 212, 255, 0.1);
      width: 260px;
    }
  }
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
