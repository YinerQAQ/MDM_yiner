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

import type { MdmDataModel } from '../api/types'
import {
  getAllModels,
  createModel,
  updateModel,
  deleteModel,
  submitModelForReview,
  approveModel,
  rejectModel
} from '../api/models'

const models = ref<MdmDataModel[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref<MdmDataModel>({
  id: '',
  modelCode: '',
  modelName: '',
  modelType: '普通',
  status: '编制中',
  version: 1,
  description: '',
  createBy: '',
  createTime: '',
  updateTime: ''
})

const loadModels = async () => {
  try {
    const response = await getAllModels()
    models.value = response.data.data || []
  } catch (error) {
    models.value = []
    ElMessage.error('加载数据模型失败')
  }
}

const openDialog = (model?: MdmDataModel) => {
  if (model) {
    isEdit.value = true
    form.value = { ...model }
  } else {
    isEdit.value = false
    form.value = {
      id: '',
      modelCode: '',
      modelName: '',
      modelType: '普通',
      status: '编制中',
      version: 1,
      description: '',
      createBy: '',
      createTime: '',
      updateTime: ''
    }
  }
  dialogVisible.value = true
}

const saveModel = async () => {
  try {
    if (!form.value.modelCode || !form.value.modelName) {
      ElMessage.error('请填写必填字段')
      return
    }

    if (isEdit.value) {
      await updateModel(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      form.value.id = `MODEL_${Date.now()}`
      await createModel(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadModels()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (id: string) => {
  try {
    await deleteModel(id)
    ElMessage.success('删除成功')
    loadModels()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleSubmit = async (id: string) => {
  try {
    await submitModelForReview(id)
    ElMessage.success('已提交审核')
    loadModels()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const handleApprove = async (id: string) => {
  try {
    await approveModel(id)
    ElMessage.success('审核通过')
    loadModels()
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

const handleReject = async (id: string) => {
  try {
    await rejectModel(id)
    ElMessage.success('已拒绝')
    loadModels()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const getStatusClass = (status: string): string => {
  switch (status) {
    case '已发布': return 'status-approved'
    case '审核中': return 'status-pending'
    case '编制中': return 'status-draft'
    default: return ''
  }
}

const getActions = (status: string): string[] => {
  switch (status) {
    case '编制中': return ['edit', 'submit', 'delete']
    case '审核中': return ['approve', 'reject']
    case '已发布': return ['edit']
    default: return []
  }
}

onMounted(() => {
  loadModels()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">数据模型管理</h2>
        <p class="page-desc">定义和管理主数据模型结构</p>
      </div>
      <ElButton type="primary" @click="openDialog()">新建模型</ElButton>
    </div>

    <div class="table-card">
      <ElTable :data="models" stripe>
        <ElTableColumn prop="modelCode" label="模型编码" />
        <ElTableColumn prop="modelName" label="模型名称" />
        <ElTableColumn prop="modelType" label="模型类型">
          <template #default="scope">
            <span v-if="scope.row.modelType === '普通'">普通模型</span>
            <span v-else-if="scope.row.modelType === '类别'">类别模型</span>
            <span v-else>引用分类模型</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="status" label="状态">
          <template #default="scope">
            <span :class="getStatusClass(scope.row.status)">
              {{ scope.row.status }}
            </span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="version" label="版本" width="80" />
        <ElTableColumn prop="description" label="描述" />
        <ElTableColumn label="操作" width="300">
          <template #default="scope">
            <ElButton
              v-if="getActions(scope.row.status).includes('edit')"
              type="primary"
              size="small"
              @click="openDialog(scope.row)"
            >编辑</ElButton>
            <ElButton
              v-if="getActions(scope.row.status).includes('submit')"
              type="success"
              size="small"
              @click="handleSubmit(scope.row.id)"
            >提交审核</ElButton>
            <ElButton
              v-if="getActions(scope.row.status).includes('approve')"
              type="success"
              size="small"
              @click="handleApprove(scope.row.id)"
            >审核通过</ElButton>
            <ElButton
              v-if="getActions(scope.row.status).includes('reject')"
              type="danger"
              size="small"
              @click="handleReject(scope.row.id)"
            >拒绝</ElButton>
            <ElButton
              v-if="getActions(scope.row.status).includes('delete')"
              type="danger"
              size="small"
              @click="handleDelete(scope.row.id)"
            >删除</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </div>

    <ElDialog title="模型信息" v-model="dialogVisible" width="500px">
      <ElForm :model="form" label-width="100px">
        <ElFormItem label="模型编码" required>
          <ElInput v-model="form.modelCode" :disabled="isEdit" />
        </ElFormItem>
        <ElFormItem label="模型名称" required>
          <ElInput v-model="form.modelName" />
        </ElFormItem>
        <ElFormItem label="模型类型">
          <ElSelect v-model="form.modelType">
            <ElOption label="普通模型" value="普通" />
            <ElOption label="类别模型" value="类别" />
            <ElOption label="引用分类模型" value="引用分类" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="描述">
          <ElInput v-model="form.description" type="textarea" :rows="3" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="saveModel">保存</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped lang="scss">
.status-approved {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: var(--color-success);
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.25);
}

.status-pending {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: var(--color-warning);
  background: rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.25);
}

.status-draft {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
  background: rgba(148, 163, 184, 0.1);
  border: 1px solid rgba(148, 163, 184, 0.25);
}

:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(0, 212, 255, 0.03);
  --el-table-row-hover-bg-color: rgba(0, 212, 255, 0.06);
  --el-table-border-color: var(--border-color);
  --el-table-text-color: var(--text-primary);
  --el-table-header-text-color: var(--text-secondary);
}
</style>
