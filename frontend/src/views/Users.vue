<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  ElTable,
  ElTableColumn,
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElSelect,
  ElOption,
  ElMessage
} from 'element-plus'

import type { BaseUser } from '../api/types'
import type { UserCreateRequest } from '../api/users'
import {
  getAllUsers,
  createUser,
  updateUser,
  deleteUser,
  changeUserStatus,
  resetPassword
} from '../api/users'
import { getAllOrgs } from '../api/orgs'

const users = ref<BaseUser[]>([])
const orgs = ref<{ id: string; orgName: string }[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref<UserCreateRequest>({
  id: '',
  username: '',
  password: '',
  nickname: '',
  sex: '',
  orgId: '',
  email: '',
  phone: '',
  securityLevel: ''
})

const loadUsers = async () => {
  try {
    const response = await getAllUsers()
    users.value = response.data.data
  } catch (error) {
    ElMessage.error('加载用户失败')
  }
}

const loadOrgs = async () => {
  try {
    const response = await getAllOrgs()
    orgs.value = response.data.data.map(org => ({ id: org.id, orgName: org.orgName }))
  } catch (error) {
    ElMessage.error('加载单位失败')
  }
}

const openDialog = (user?: BaseUser) => {
  if (user) {
    isEdit.value = true
    form.value = {
      id: user.id,
      username: user.username,
      password: '',
      nickname: user.nickname,
      sex: user.sex,
      orgId: user.orgId,
      email: user.email,
      phone: user.phone,
      securityLevel: user.securityLevel
    }
  } else {
    isEdit.value = false
    form.value = {
      id: '',
      username: '',
      password: '',
      nickname: '',
      sex: '',
      orgId: '',
      email: '',
      phone: '',
      securityLevel: ''
    }
  }
  dialogVisible.value = true
}

const saveUser = async () => {
  try {
    if (!form.value.id || !form.value.username) {
      ElMessage.error('请填写必填字段')
      return
    }

    if (isEdit.value) {
      await updateUser(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      if (!form.value.password) {
        ElMessage.error('请设置密码')
        return
      }
      await createUser(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (id: string) => {
  try {
    await deleteUser(id)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleStatusChange = async (id: string, status: string) => {
  try {
    await changeUserStatus(id, status)
    ElMessage.success(`已${status === '启用' ? '启用' : '停用'}`)
    loadUsers()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleResetPassword = async (id: string) => {
  try {
    await resetPassword(id, '123456')
    ElMessage.success('密码已重置为123456')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const getStatusClass = (status: string): string => {
  switch (status) {
    case '启用': return 'status-active'
    case '停用': return 'status-inactive'
    case '锁定': return 'status-locked'
    default: return ''
  }
}

onMounted(() => {
  loadUsers()
  loadOrgs()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">用户管理</h2>
        <p class="page-desc">管理系统用户账号、权限与状态</p>
      </div>
      <ElButton v-auth="'btn:user:add'" type="primary" @click="openDialog()">新建用户</ElButton>
    </div>

    <div class="table-card">
      <ElTable :data="users" stripe>
        <ElTableColumn prop="id" label="用户编码" />
        <ElTableColumn prop="username" label="用户名" />
        <ElTableColumn prop="nickname" label="昵称" />
        <ElTableColumn prop="orgName" label="所属单位" />
        <ElTableColumn prop="email" label="邮箱" />
        <ElTableColumn prop="phone" label="手机" />
        <ElTableColumn prop="status" label="状态">
          <template #default="scope">
            <span :class="getStatusClass(scope.row.status)">
              {{ scope.row.status }}
            </span>
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="280">
          <template #default="scope">
            <ElButton v-auth="'btn:user:edit'" type="primary" size="small" @click="openDialog(scope.row)">编辑</ElButton>
            <ElButton type="warning" size="small" @click="handleStatusChange(scope.row.id, scope.row.status === '启用' ? '停用' : '启用')">
              {{ scope.row.status === '启用' ? '停用' : '启用' }}
            </ElButton>
            <ElButton size="small" @click="handleResetPassword(scope.row.id)">重置密码</ElButton>
            <ElButton v-auth="'btn:user:delete'" type="danger" size="small" @click="handleDelete(scope.row.id)">删除</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </div>

    <ElDialog title="用户信息" v-model="dialogVisible" width="500px">
      <ElForm :model="form" label-width="100px">
        <ElFormItem label="用户编码" required>
          <ElInput v-model="form.id" :disabled="isEdit" />
        </ElFormItem>
        <ElFormItem label="用户名" required>
          <ElInput v-model="form.username" />
        </ElFormItem>
        <ElFormItem :label="isEdit ? '新密码' : '密码'" :required="!isEdit">
          <ElInput v-model="form.password" type="password" placeholder="不填则保持不变" />
        </ElFormItem>
        <ElFormItem label="昵称">
          <ElInput v-model="form.nickname" />
        </ElFormItem>
        <ElFormItem label="性别">
          <ElSelect v-model="form.sex">
            <ElOption label="男" value="男" />
            <ElOption label="女" value="女" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="所属单位">
          <ElSelect v-model="form.orgId">
            <ElOption v-for="org in orgs" :key="org.id" :label="org.orgName" :value="org.id" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="邮箱">
          <ElInput v-model="form.email" />
        </ElFormItem>
        <ElFormItem label="手机">
          <ElInput v-model="form.phone" />
        </ElFormItem>
        <ElFormItem label="密级">
          <ElInput v-model="form.securityLevel" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="saveUser">保存</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped lang="scss">
</style>
