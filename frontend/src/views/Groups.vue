<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getGroups, createGroup, updateGroup, deleteGroup,
  getGroupOrgs, assignGroupOrgs, getGroupUsers, addGroupUsers, removeGroupUser
} from '../api/groups'
import type { BaseGroup, OrgPerm } from '../api/groups'
import { getAllOrgs } from '../api/orgs'
import type { BaseOrg } from '../api/types'
import { getAllUsers } from '../api/users'
import type { BaseUser } from '../api/types'

const groups = ref<BaseGroup[]>([])
const orgs = ref<BaseOrg[]>([])
const allUsers = ref<BaseUser[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const activeTab = ref('basic')
const selectedGroup = ref<BaseGroup | null>(null)

const form = ref<BaseGroup>({
  id: '',
  groupCode: '',
  groupName: '',
  orgId: '',
  status: '启用'
})

const groupUsers = ref<BaseUser[]>([])
const orgTreeRef = ref()
const orgTree = ref<BaseOrg[]>([])
const checkedOrgIds = ref<string[]>([])
const addUserDialogVisible = ref(false)
const selectedUserIds = ref<string[]>([])

const cascadeFlags = ref<Record<string, string>>({})

const rules: FormRules = {
  groupCode: [{ required: true, message: '请输入用户组编码', trigger: 'blur' }],
  groupName: [{ required: true, message: '请输入用户组名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const loadGroups = async () => {
  try {
    const res = await getGroups()
    groups.value = res.data.data
  } catch {
    ElMessage.error('加载用户组列表失败')
  }
}

const loadOrgs = async () => {
  try {
    const res = await getAllOrgs()
    orgs.value = res.data.data
    buildOrgTree(orgs.value)
  } catch {
    ElMessage.error('加载单位列表失败')
  }
}

const buildOrgTree = (list: BaseOrg[]) => {
  const map = new Map<string, BaseOrg & { children?: BaseOrg[] }>()
  const roots: (BaseOrg & { children?: BaseOrg[] })[] = []
  list.forEach(item => map.set(item.id, { ...item, children: [] }))
  map.forEach(item => {
    if (!item.parentId || item.parentId === '0') {
      roots.push(item)
    } else {
      const parent = map.get(item.parentId)
      if (parent) {
        if (!parent.children) parent.children = []
        parent.children.push(item)
      } else {
        roots.push(item)
      }
    }
  })
  orgTree.value = roots
}

const loadAllUsers = async () => {
  try {
    const res = await getAllUsers()
    allUsers.value = res.data.data
  } catch {
    ElMessage.error('加载用户列表失败')
  }
}

const openDialog = (group?: BaseGroup) => {
  if (group) {
    isEdit.value = true
    form.value = { ...group }
  } else {
    isEdit.value = false
    form.value = { id: '', groupCode: '', groupName: '', orgId: '', status: '启用' }
  }
  dialogVisible.value = true
}

const saveGroup = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await updateGroup(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createGroup(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadGroups()
  } catch {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm('确定删除该用户组？', '提示', { type: 'warning' })
    await deleteGroup(id)
    ElMessage.success('删除成功')
    if (selectedGroup.value?.id === id) {
      selectedGroup.value = null
    }
    loadGroups()
  } catch {
    // cancelled
  }
}

const selectGroup = async (group: BaseGroup) => {
  selectedGroup.value = group
  activeTab.value = 'basic'
  await loadGroupDetail(group.id)
}

const loadGroupDetail = async (groupId: string) => {
  if (activeTab.value === 'users') {
    await loadGroupUsers(groupId)
  } else if (activeTab.value === 'orgs') {
    await loadGroupOrgs(groupId)
  }
}

watch(activeTab, () => {
  if (selectedGroup.value) {
    loadGroupDetail(selectedGroup.value.id)
  }
})

const loadGroupUsers = async (groupId: string) => {
  try {
    const res = await getGroupUsers(groupId)
    groupUsers.value = res.data.data
  } catch {
    ElMessage.error('加载组用户失败')
  }
}

const loadGroupOrgs = async (groupId: string) => {
  try {
    const res = await getGroupOrgs(groupId)
    checkedOrgIds.value = res.data.data
    cascadeFlags.value = {}
    res.data.data.forEach(orgId => {
      cascadeFlags.value[orgId] = '0'
    })
  } catch {
    ElMessage.error('加载单位权限失败')
  }
}

const openAddUserDialog = () => {
  selectedUserIds.value = []
  addUserDialogVisible.value = true
}

const addUsers = async () => {
  if (!selectedGroup.value || selectedUserIds.value.length === 0) return
  try {
    await addGroupUsers(selectedGroup.value.id, selectedUserIds.value)
    ElMessage.success('添加用户成功')
    addUserDialogVisible.value = false
    loadGroupUsers(selectedGroup.value.id)
  } catch {
    ElMessage.error('添加用户失败')
  }
}

const removeUser = async (userId: string) => {
  if (!selectedGroup.value) return
  try {
    await ElMessageBox.confirm('确定移除该用户？', '提示', { type: 'warning' })
    await removeGroupUser(selectedGroup.value.id, userId)
    ElMessage.success('移除成功')
    loadGroupUsers(selectedGroup.value.id)
  } catch {
    // cancelled
  }
}

const saveOrgPermissions = async () => {
  if (!selectedGroup.value) return
  try {
    const checkedKeys = orgTreeRef.value?.getCheckedKeys(false) as string[]
    const perms: OrgPerm[] = checkedKeys.map((orgId: string) => ({
      orgId,
      cascadeFlag: cascadeFlags.value[orgId] || '0'
    }))
    await assignGroupOrgs(selectedGroup.value.id, perms)
    ElMessage.success('单位权限保存成功')
  } catch {
    ElMessage.error('保存失败')
  }
}

const getOrgName = (orgId: string) => {
  return orgs.value.find(o => o.id === orgId)?.orgName || orgId
}

const isUserInGroup = (userId: string) => {
  return groupUsers.value.some(u => u.id === userId)
}

onMounted(() => {
  loadGroups()
  loadOrgs()
  loadAllUsers()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">用户组管理</h2>
        <p class="page-desc">管理用户组与组成员关系</p>
      </div>
      <el-button type="primary" @click="openDialog()">新增用户组</el-button>
    </div>

    <div style="display: flex; gap: 20px; height: calc(100% - 90px);">
      <!-- 左侧：用户组列表 -->
      <div class="table-card" style="width: 400px; flex-shrink: 0; overflow: auto;">
        <el-table :data="groups" stripe highlight-current-row @current-change="selectGroup">
          <el-table-column prop="groupCode" label="用户组编码" />
          <el-table-column prop="groupName" label="名称" />
          <el-table-column label="状态" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.status === '启用' ? 'success' : 'danger'" size="small">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button type="primary" size="small" link @click.stop="openDialog(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" link @click.stop="handleDelete(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 右侧：详情 -->
      <div v-if="selectedGroup" class="card-glow" style="flex: 1; padding: 20px; overflow: auto;">
        <h3 style="margin-bottom: 16px; color: var(--text-bright);">{{ selectedGroup.groupName }}</h3>
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="用户组编码">{{ selectedGroup.groupCode }}</el-descriptions-item>
              <el-descriptions-item label="用户组名称">{{ selectedGroup.groupName }}</el-descriptions-item>
              <el-descriptions-item label="所属单位">{{ getOrgName(selectedGroup.orgId) }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="selectedGroup.status === '启用' ? 'success' : 'danger'" size="small">
                  {{ selectedGroup.status }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <el-tab-pane label="所属用户" name="users">
            <div style="margin-bottom: 12px;">
              <el-button type="primary" size="small" @click="openAddUserDialog">添加用户</el-button>
            </div>
            <el-table :data="groupUsers" stripe>
              <el-table-column prop="id" label="用户编码" />
              <el-table-column prop="username" label="用户名" />
              <el-table-column prop="nickname" label="昵称" />
              <el-table-column prop="orgName" label="所属单位" />
              <el-table-column label="操作" width="80">
                <template #default="scope">
                  <el-button type="danger" size="small" link @click="removeUser(scope.row.id)">移除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="单位权限" name="orgs">
            <el-tree
              ref="orgTreeRef"
              :data="orgTree"
              :props="{ label: 'orgName', children: 'children' }"
              node-key="id"
              :default-checked-keys="checkedOrgIds"
              show-checkbox
              default-expand-all
            />
            <div style="margin-top: 16px;">
              <el-button type="primary" @click="saveOrgPermissions">保存权限</el-button>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      <div v-else class="card-glow" style="flex: 1; padding: 60px; text-align: center;">
        <p style="color: var(--text-secondary);">请从左侧选择一个用户组查看详情</p>
      </div>
    </div>

    <!-- 新增/编辑Dialog -->
    <el-dialog :title="isEdit ? '编辑用户组' : '新增用户组'" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="用户组编码" prop="groupCode">
          <el-input v-model="form.groupCode" :disabled="isEdit" placeholder="请输入用户组编码" />
        </el-form-item>
        <el-form-item label="用户组名称" prop="groupName">
          <el-input v-model="form.groupName" placeholder="请输入用户组名称" />
        </el-form-item>
        <el-form-item label="所属单位">
          <el-select v-model="form.orgId" placeholder="请选择单位" clearable style="width: 100%">
            <el-option v-for="org in orgs" :key="org.id" :label="org.orgName" :value="org.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="启用" value="启用" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveGroup">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加用户Dialog -->
    <el-dialog title="添加用户" v-model="addUserDialogVisible" width="500px">
      <el-checkbox-group v-model="selectedUserIds">
        <el-checkbox
          v-for="user in allUsers.filter(u => !isUserInGroup(u.id))"
          :key="user.id"
          :label="user.id"
          style="display: block; margin-bottom: 8px;"
        >
          {{ user.username }} ({{ user.nickname }})
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="addUserDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addUsers" :disabled="selectedUserIds.length === 0">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
</style>
