<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getRoles, createRole, updateRole, deleteRole, getRoleMenus, assignRoleMenus } from '../api/roles'
import type { BaseRole } from '../api/roles'
import { getAllMenus } from '../api/menus'
import type { BaseMenu } from '../api/menus'
import { getAllOrgs } from '../api/orgs'

const roles = ref<BaseRole[]>([])
const orgs = ref<{ id: string; orgName: string }[]>([])
const dialogVisible = ref(false)
const permDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const menuTreeRef = ref()

const form = ref<BaseRole>({
  id: '',
  roleCode: '',
  roleName: '',
  orgId: '',
  status: '启用'
})

const menuTree = ref<BaseMenu[]>([])
const checkedMenuIds = ref<string[]>([])
const currentRoleId = ref('')

const rules: FormRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const loadRoles = async () => {
  try {
    const res = await getRoles()
    roles.value = res.data.data
  } catch {
    ElMessage.error('加载角色列表失败')
  }
}

const loadOrgs = async () => {
  try {
    const res = await getAllOrgs()
    orgs.value = res.data.data.map(o => ({ id: o.id, orgName: o.orgName }))
  } catch {
    ElMessage.error('加载单位列表失败')
  }
}

const openDialog = (role?: BaseRole) => {
  if (role) {
    isEdit.value = true
    form.value = { ...role }
  } else {
    isEdit.value = false
    form.value = { id: '', roleCode: '', roleName: '', orgId: '', status: '启用' }
  }
  dialogVisible.value = true
}

const saveRole = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await updateRole(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createRole(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadRoles()
  } catch {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm('确定删除该角色？', '提示', { type: 'warning' })
    await deleteRole(id)
    ElMessage.success('删除成功')
    loadRoles()
  } catch {
    // cancelled or failed
  }
}

const openPermDialog = async (role: BaseRole) => {
  currentRoleId.value = role.id
  try {
    const menuRes = await getAllMenus()
    menuTree.value = menuRes.data.data
    const permRes = await getRoleMenus(role.id)
    checkedMenuIds.value = permRes.data.data
    permDialogVisible.value = true
  } catch {
    ElMessage.error('加载权限数据失败')
  }
}

const savePermissions = async () => {
  try {
    const checkedKeys = menuTreeRef.value?.getCheckedKeys(false) as string[]
    const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() as string[]
    const allKeys = [...checkedKeys, ...halfCheckedKeys]
    await assignRoleMenus(currentRoleId.value, allKeys)
    ElMessage.success('权限配置成功')
    permDialogVisible.value = false
  } catch {
    ElMessage.error('权限配置失败')
  }
}

const getOrgName = (orgId: string) => {
  return orgs.value.find(o => o.id === orgId)?.orgName || orgId
}

onMounted(() => {
  loadRoles()
  loadOrgs()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">角色管理</h2>
        <p class="page-desc">管理系统角色与权限分配</p>
      </div>
      <el-button type="primary" @click="openDialog()">新增角色</el-button>
    </div>

    <div class="table-card">
      <el-table :data="roles" stripe>
        <el-table-column prop="roleCode" label="角色编码" />
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column label="使用单位">
          <template #default="scope">{{ getOrgName(scope.row.orgId) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === '启用' ? 'success' : 'danger'" size="small">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openDialog(scope.row)">编辑</el-button>
            <el-button type="success" size="small" @click="openPermDialog(scope.row)">权限配置</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑Dialog -->
    <el-dialog :title="isEdit ? '编辑角色' : '新增角色'" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="isEdit" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="使用单位">
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
        <el-button type="primary" @click="saveRole">保存</el-button>
      </template>
    </el-dialog>

    <!-- 权限配置Dialog -->
    <el-dialog title="权限配置" v-model="permDialogVisible" width="500px">
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        :props="{ label: 'menuName', children: 'children' }"
        node-key="id"
        :default-checked-keys="checkedMenuIds"
        show-checkbox
        default-expand-all
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePermissions">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
</style>
