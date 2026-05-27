<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  ElTable,
  ElTableColumn,
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage
} from 'element-plus'

import type { MdmWorkflow } from '../api/types'
import {
  getAllWorkflows,
  createWorkflow,
  updateWorkflow,
  deleteWorkflow,
  activateWorkflow,
  deactivateWorkflow
} from '../api/workflows'

const router = useRouter()

const workflows = ref<MdmWorkflow[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref<MdmWorkflow>({
  id: '',
  workflowCode: '',
  workflowName: '',
  orgId: '',
  status: '草稿',
  definition: '',
  createBy: '',
  createTime: ''
})

const loadWorkflows = async () => {
  try {
    const response = await getAllWorkflows()
    workflows.value = response.data.data
  } catch (error) {
    ElMessage.error('加载工作流失败')
  }
}

const openDialog = (workflow?: MdmWorkflow) => {
  if (workflow) {
    isEdit.value = true
    form.value = { ...workflow }
  } else {
    isEdit.value = false
    form.value = {
      id: '',
      workflowCode: '',
      workflowName: '',
      orgId: '',
      status: '草稿',
      definition: '',
      createBy: '',
      createTime: ''
    }
  }
  dialogVisible.value = true
}

const saveWorkflow = async () => {
  try {
    if (!form.value.workflowCode || !form.value.workflowName) {
      ElMessage.error('请填写必填字段')
      return
    }

    if (isEdit.value) {
      await updateWorkflow(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      form.value.id = `FLOW_${Date.now()}`
      await createWorkflow(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadWorkflows()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (id: string) => {
  try {
    await deleteWorkflow(id)
    ElMessage.success('删除成功')
    loadWorkflows()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleActivate = async (id: string) => {
  try {
    await activateWorkflow(id)
    ElMessage.success('已启用')
    loadWorkflows()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDeactivate = async (id: string) => {
  try {
    await deactivateWorkflow(id)
    ElMessage.success('已停用')
    loadWorkflows()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const getStatusClass = (status: string): string => {
  switch (status) {
    case '启用': return 'status-active'
    case '停用': return 'status-inactive'
    case '草稿': return 'status-draft'
    default: return ''
  }
}

onMounted(() => {
  loadWorkflows()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">流程管理</h2>
        <p class="page-desc">管理审核流程与审批链路配置</p>
      </div>
      <ElButton type="primary" @click="openDialog()">新建流程</ElButton>
    </div>

    <div class="table-card">
      <ElTable :data="workflows" stripe>
        <ElTableColumn prop="workflowCode" label="流程编码" />
        <ElTableColumn prop="workflowName" label="流程名称" />
        <ElTableColumn prop="orgId" label="所属单位" />
        <ElTableColumn prop="status" label="状态">
          <template #default="scope">
            <span :class="getStatusClass(scope.row.status)">{{ scope.row.status }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="createTime" label="创建时间" />
        <ElTableColumn label="操作" width="300">
          <template #default="scope">
            <ElButton type="primary" size="small" @click="router.push(`/workflow-designer/${scope.row.id}`)">设计</ElButton>
            <ElButton v-if="scope.row.status !== '启用'" type="primary" size="small" @click="openDialog(scope.row)">编辑</ElButton>
            <ElButton v-if="scope.row.status === '草稿'" type="danger" size="small" @click="handleDelete(scope.row.id)">删除</ElButton>
            <ElButton v-if="scope.row.status !== '启用'" type="success" size="small" @click="handleActivate(scope.row.id)">启用</ElButton>
            <ElButton v-if="scope.row.status === '启用'" type="warning" size="small" @click="handleDeactivate(scope.row.id)">停用</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </div>

    <ElDialog title="流程信息" v-model="dialogVisible" width="500px">
      <ElForm :model="form" label-width="100px">
        <ElFormItem label="流程编码" required>
          <ElInput v-model="form.workflowCode" :disabled="isEdit" />
        </ElFormItem>
        <ElFormItem label="流程名称" required>
          <ElInput v-model="form.workflowName" />
        </ElFormItem>
        <ElFormItem label="所属单位">
          <ElInput v-model="form.orgId" />
        </ElFormItem>
        <ElFormItem label="流程定义">
          <ElInput v-model="form.definition" type="textarea" :rows="5" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="saveWorkflow">保存</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped lang="scss">
</style>
