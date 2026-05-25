<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  ElCard,
  ElRow,
  ElCol,
  ElTable,
  ElTableColumn,
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElSelect,
  ElOption,
  ElTree,
  ElMessage
} from 'element-plus'

import type { BaseOrg } from '../api/types'
import {
  getAllOrgs,
  createOrg,
  updateOrg,
  deleteOrg,
  changeOrgStatus
} from '../api/orgs'

const orgs = ref<BaseOrg[]>([])
const treeData = ref<{ id: string; label: string; children?: { id: string; label: string }[] }[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref<BaseOrg>({
  id: '',
  orgName: '',
  parentId: '',
  status: '启用'
})

const loadOrgs = async () => {
  try {
    const response = await getAllOrgs()
    orgs.value = response.data.data
    buildTree()
  } catch (error) {
    ElMessage.error('加载单位失败')
  }
}

const buildTree = () => {
  const map: Record<string, { id: string; label: string; children: { id: string; label: string }[] }> = {}
  const root: typeof treeData.value = []

  orgs.value.forEach(org => {
    map[org.id] = { id: org.id, label: org.orgName, children: [] }
  })

  orgs.value.forEach(org => {
    if (org.parentId && map[org.parentId]) {
      map[org.parentId].children.push({ id: org.id, label: org.orgName })
    } else {
      root.push(map[org.id])
    }
  })

  treeData.value = root
}

const openDialog = (org?: BaseOrg) => {
  if (org) {
    isEdit.value = true
    form.value = { ...org }
  } else {
    isEdit.value = false
    form.value = {
      id: '',
      orgName: '',
      parentId: '',
      status: '启用'
    }
  }
  dialogVisible.value = true
}

const saveOrg = async () => {
  try {
    if (!form.value.id || !form.value.orgName) {
      ElMessage.error('请填写必填字段')
      return
    }

    if (isEdit.value) {
      await updateOrg(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createOrg(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadOrgs()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (id: string) => {
  try {
    await deleteOrg(id)
    ElMessage.success('删除成功')
    loadOrgs()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleStatusChange = async (id: string, status: string) => {
  try {
    await changeOrgStatus(id, status)
    ElMessage.success(`已${status === '启用' ? '启用' : '停用'}`)
    loadOrgs()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const getStatusClass = (status: string): string => {
  switch (status) {
    case '启用':
      return 'status-active'
    case '停用':
      return 'status-inactive'
    default:
      return ''
  }
}

const getParentName = (parentId: string): string => {
  const parent = orgs.value.find(o => o.id === parentId)
  return parent ? parent.orgName : '无'
}

onMounted(() => {
  loadOrgs()
})
</script>

<template>
  <ElRow :gutter="20">
    <ElCol :span="8">
      <ElCard title="单位树形结构">
        <ElTree :data="treeData" :props="{ label: 'label' }" />
      </ElCard>
    </ElCol>
    <ElCol :span="16">
      <ElCard title="单位列表" class="card">
        <div class="card-header">
          <ElButton type="primary" icon="Plus" @click="openDialog()">
            新建单位
          </ElButton>
        </div>
        <ElTable :data="orgs" stripe>
          <ElTableColumn prop="id" label="单位编码" />
          <ElTableColumn prop="orgName" label="单位名称" />
          <ElTableColumn prop="parentId" label="上级单位">
            <template #default="scope">
              {{ getParentName(scope.row.parentId) }}
            </template>
          </ElTableColumn>
          <ElTableColumn prop="status" label="状态">
            <template #default="scope">
              <span :class="getStatusClass(scope.row.status)">
                {{ scope.row.status }}
              </span>
            </template>
          </ElTableColumn>
          <ElTableColumn label="操作">
            <template #default="scope">
              <ElButton type="primary" size="small" icon="Edit" @click="openDialog(scope.row)">编辑</ElButton>
              <ElButton type="warning" size="small" icon="PowerOff" @click="handleStatusChange(scope.row.id, scope.row.status === '启用' ? '停用' : '启用')">
                {{ scope.row.status === '启用' ? '停用' : '启用' }}
              </ElButton>
              <ElButton type="danger" size="small" icon="Delete" @click="handleDelete(scope.row.id)">删除</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </ElCard>
    </ElCol>
  </ElRow>

  <ElDialog title="单位信息" v-model="dialogVisible" width="400px">
    <ElForm :model="form" label-width="80px">
      <ElFormItem label="单位编码" required>
        <ElInput v-model="form.id" :disabled="isEdit" />
      </ElFormItem>
      <ElFormItem label="单位名称" required>
        <ElInput v-model="form.orgName" />
      </ElFormItem>
      <ElFormItem label="上级单位">
        <ElSelect v-model="form.parentId">
          <ElOption label="无" value="" />
          <ElOption v-for="org in orgs" :key="org.id" :label="org.orgName" :value="org.id" />
        </ElSelect>
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" @click="saveOrg">保存</ElButton>
    </template>
  </ElDialog>
</template>

<style scoped>
.card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.status-active {
  color: #67c23a;
  background: #e8f5e9;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-inactive {
  color: #f56c6c;
  background: #fef0f0;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
</style>