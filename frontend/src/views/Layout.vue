<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import {
  ElContainer,
  ElHeader,
  ElAside,
  ElMain,
  ElMenu,
  ElMenuItem,
  ElButton,
  ElDropdown,
  ElDropdownMenu,
  ElDropdownItem,
  ElMessage
} from 'element-plus'
import {
  User
} from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const activeMenu = ref('Dashboard')

const menuItems = [
  { name: 'Dashboard', label: '系统首页' },
  { name: 'DataModels', label: '数据标准' },
  { name: 'MainData', label: '数据业务' },
  { name: 'Workflows', label: '流程管理' },
  { name: 'Users', label: '用户管理' },
  { name: 'Orgs', label: '单位管理' },
  { name: 'DataExchange', label: '数据交换' }
]

const handleMenuClick = (name: string) => {
  activeMenu.value = name
  router.push({ name })
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
  <ElContainer class="layout-container">
    <ElHeader class="header">
      <div class="logo">MDM主数据管理系统</div>
      <div class="header-right">
        <ElDropdown>
          <ElButton type="text">
            <User class="user-icon" />
            <span>{{ authStore.nickname }}</span>
          </ElButton>
          <template #dropdown>
            <ElDropdownMenu>
              <ElDropdownItem @click="handleLogout">
                退出登录
              </ElDropdownItem>
            </ElDropdownMenu>
          </template>
        </ElDropdown>
      </div>
    </ElHeader>
    <ElContainer>
      <ElAside class="sidebar">
        <ElMenu mode="vertical" :default-active="activeMenu">
          <ElMenuItem
            v-for="item in menuItems"
            :key="item.name"
            :index="item.name"
            @click="handleMenuClick(item.name)"
          >
            <span>{{ item.label }}</span>
          </ElMenuItem>
        </ElMenu>
      </ElAside>
      <ElMain class="main-content">
        <router-view />
      </ElMain>
    </ElContainer>
  </ElContainer>
</template>

<style scoped>
.layout-container {
  height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.logo {
  font-size: 20px;
  font-weight: bold;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-icon {
  margin-right: 8px;
}

.sidebar {
  width: 200px;
  background: #2f4050;
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item) {
  color: #a7b1c2;
  height: 48px;
  line-height: 48px;
}

:deep(.el-menu-item:hover) {
  background: #1f2d3d;
}

:deep(.el-menu-item.is-active) {
  background: #1ab394;
  color: white;
}

.main-content {
  padding: 20px;
  background: #f3f3f4;
  overflow-y: auto;
}
</style>