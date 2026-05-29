<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCurrentUser, updateProfile } from '../api/profile'
import { useAuthStore } from '../stores/auth'
import {
  ElForm,
  ElFormItem,
  ElInput,
  ElButton,
  ElDescriptions,
  ElDescriptionsItem,
  ElMessage,
  ElIcon,
  ElTag
} from 'element-plus'
import { User, Edit, Check, Close } from '@element-plus/icons-vue'

interface UserInfo {
  id: string
  username: string
  nickname: string
  email: string
  phone: string
  sex: string
  orgName: string
  status: string
  securityLevel: string
  createTime: string
}

const loading = ref(false)
const editing = ref(false)
const userInfo = ref<UserInfo | null>(null)
const editForm = ref({ nickname: '', email: '', phone: '' })

const authStore = useAuthStore()

const fetchUser = async () => {
  loading.value = true
  try {
    const res = await getCurrentUser()
    userInfo.value = res.data.data
    editForm.value = {
      nickname: userInfo.value?.nickname || '',
      email: userInfo.value?.email || '',
      phone: userInfo.value?.phone || ''
    }
  } catch {
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const handleEdit = () => {
  editing.value = true
}

const handleCancel = () => {
  editing.value = false
  if (userInfo.value) {
    editForm.value = {
      nickname: userInfo.value.nickname || '',
      email: userInfo.value.email || '',
      phone: userInfo.value.phone || ''
    }
  }
}

const handleSave = async () => {
  try {
    await updateProfile(editForm.value)
    ElMessage.success('个人信息更新成功')
    editing.value = false
    // 更新本地store中的nickname
    if (editForm.value.nickname && editForm.value.nickname !== authStore.nickname) {
      authStore.nickname = editForm.value.nickname
    }
    await fetchUser()
  } catch {
    ElMessage.error('更新失败')
  }
}

onMounted(() => {
  fetchUser()
})
</script>

<template>
  <div class="profile-page">
    <div class="profile-card">
      <!-- 头像区域 -->
      <div class="profile-header">
        <div class="avatar-wrapper">
          <div class="avatar">
            <ElIcon :size="48"><User /></ElIcon>
          </div>
          <div class="user-brief">
            <h2 class="username">{{ userInfo?.username || '用户' }}</h2>
            <p class="nickname">{{ userInfo?.nickname || '未设置昵称' }}</p>
          </div>
        </div>
      </div>

      <!-- 信息区域 -->
      <div class="profile-body" v-loading="loading">
        <!-- 查看模式 -->
        <template v-if="!editing">
          <ElDescriptions :column="2" border>
            <ElDescriptionsItem label="用户名">{{ userInfo?.username || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="昵称">{{ userInfo?.nickname || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="邮箱">{{ userInfo?.email || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="手机">{{ userInfo?.phone || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="性别">{{ userInfo?.sex || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="所属单位">{{ userInfo?.orgName || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="状态">
              <ElTag :type="userInfo?.status === '启用' ? 'success' : 'danger'" size="small">
                {{ userInfo?.status || '-' }}
              </ElTag>
            </ElDescriptionsItem>
            <ElDescriptionsItem label="安全等级">{{ userInfo?.securityLevel || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="创建时间">{{ userInfo?.createTime || '-' }}</ElDescriptionsItem>
          </ElDescriptions>
          <div class="action-bar">
            <ElButton type="primary" @click="handleEdit">
              <ElIcon><Edit /></ElIcon>编辑信息
            </ElButton>
          </div>
        </template>

        <!-- 编辑模式 -->
        <template v-else>
          <ElForm label-width="80px" :model="editForm">
            <ElFormItem label="用户名">
              <ElInput :model-value="userInfo?.username" disabled />
            </ElFormItem>
            <ElFormItem label="昵称">
              <ElInput v-model="editForm.nickname" placeholder="请输入昵称" />
            </ElFormItem>
            <ElFormItem label="邮箱">
              <ElInput v-model="editForm.email" placeholder="请输入邮箱" />
            </ElFormItem>
            <ElFormItem label="手机">
              <ElInput v-model="editForm.phone" placeholder="请输入手机号" />
            </ElFormItem>
            <ElFormItem>
              <ElButton type="primary" @click="handleSave">
                <ElIcon><Check /></ElIcon>保存
              </ElButton>
              <ElButton @click="handleCancel">
                <ElIcon><Close /></ElIcon>取消
              </ElButton>
            </ElFormItem>
          </ElForm>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.profile-page {
  padding: 24px;
  min-height: 100%;
}

.profile-card {
  max-width: 800px;
  margin: 0 auto;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  backdrop-filter: blur(12px);
  box-shadow: var(--shadow-md);
}

.profile-header {
  padding: 32px 32px 24px;
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.1), rgba(99, 102, 241, 0.1));
  border-bottom: 1px solid var(--border-color);
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.3);
}

.user-brief {
  .username {
    font-size: 22px;
    font-weight: 700;
    color: var(--text-primary);
    margin: 0 0 4px;
  }

  .nickname {
    font-size: 14px;
    color: var(--text-secondary);
    margin: 0;
  }
}

.profile-body {
  padding: 24px 32px 32px;

  :deep(.el-descriptions) {
    --el-descriptions-table-border: 1px solid var(--border-color);

    .el-descriptions__label {
      background: rgba(0, 212, 255, 0.05);
      color: var(--text-secondary);
      min-width: 100px;
    }

    .el-descriptions__content {
      background: transparent;
      color: var(--text-primary);
    }

    .el-descriptions__cell {
      border-color: var(--border-color) !important;
    }
  }

  :deep(.el-form-item__label) {
    color: var(--text-secondary);
  }

  :deep(.el-input__inner) {
    color: var(--text-primary);
  }

  :deep(.el-input.is-disabled .el-input__inner) {
    color: var(--text-muted);
    -webkit-text-fill-color: var(--text-muted);
  }
}

.action-bar {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
