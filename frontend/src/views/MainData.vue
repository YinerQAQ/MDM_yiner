<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  ElCard,
  ElTable,
  ElTableColumn,
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElSelect,
  ElOption,
  ElTabs,
  ElTabPane,
  ElMessage
} from 'element-plus'

import type { MdmMainData, MdmDataModel } from '../api/types'
import { getAllModels } from '../api/models'
import {
  getMainDataByModelId,
  createMainData,
  updateMainData,
  deleteMainData,
  submitMainDataForReview,
  approveMainData,
  rejectMainData,
  archiveMainData,
  createVersion
} from '../api/mainData'

const models = ref<MdmDataModel[]>([])
const selectedModelId = ref('')
const mainDataList = ref<MdmMainData[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref<MdmMainData>({
  id: '',
  modelId: '',
  code: '',
  dataStatus: '暂存',
  flowStatus: 'DRAFT',
  version: 1,
  jsonData: '{}',
  createdById: '',
  createdByName: '',
  createdByOrgId: '',
  submittedByOrgId: '',
  modifiedById: '',
  createTime: '',
  modifyTime: '',
  isModify: 0,
  securityLevel: ''
})

const loadModels = async () => {
  try {
    const response = await getAllModels()
    models.value = response.data.data.filter(m => m.status === '已发布')
    if (models.value.length > 0) {
      selectedModelId.value = models.value[0].id
      loadMainData()
    }
  } catch (error) {
    ElMessage.error('加载模型失败')
  }
}

const loadMainData = async () => {
  if (!selectedModelId.value) return
  try {
    const response = await getMainDataByModelId(selectedModelId.value)
    mainDataList.value = response.data.data
  } catch (error) {
    ElMessage.error('加载主数据失败')
  }
}

const openDialog = (data?: MdmMainData) => {
  if (data) {
    isEdit.value = true
    form.value = { ...data }
  } else {
    isEdit.value = false
    form.value = {
      id: '',
      modelId: selectedModelId.value,
      code: '',
      dataStatus: '暂存',
      flowStatus: 'DRAFT',
      version: 1,
      jsonData: '{}',
      createdById: '',
      createdByName: '',
      createdByOrgId: '',
      submittedByOrgId: '',
      modifiedById: '',
      createTime: '',
      modifyTime: '',
      isModify: 0,
      securityLevel: ''
    }
  }
  dialogVisible.value = true
}

const saveMainData = async () => {
  try {
    if (!form.value.code) {
      ElMessage.error('请填写数据编码')
      return
    }

    if (isEdit.value) {
      await updateMainData(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      form.value.id = `DATA_${Date.now()}`
      await createMainData(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadMainData()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (id: string) => {
  try {
    await deleteMainData(id)
    ElMessage.success('删除成功')
    loadMainData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleSubmit = async (id: string) => {
  try {
    await submitMainDataForReview(id)
    ElMessage.success('已提交审核')
    loadMainData()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const handleApprove = async (id: string) => {
  try {
    await approveMainData(id)
    ElMessage.success('审核通过')
    loadMainData()
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

const handleReject = async (id: string) => {
  try {
    await rejectMainData(id)
    ElMessage.success('已拒绝')
    loadMainData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleArchive = async (id: string) => {
  try {
    await archiveMainData(id)
    ElMessage.success('已归档')
    loadMainData()
  } catch (error) {
    ElMessage.error('归档失败')
  }
}

const handleCreateVersion = async (id: string) => {
  try {
    await createVersion(id)
    ElMessage.success('版本已更新')
    loadMainData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const getStatusClass = (status: string): string => {
  switch (status) {
    case '审核通过':
      return 'status-approved'
    case '审核中':
      return 'status-pending'
    case '暂存':
      return 'status-draft'
    case '审核拒绝':
      return 'status-rejected'
    case '已归档':
      return 'status-archived'
    default:
      return ''
  }
}

const filteredData = (status: string) => {
  if (status === 'all') return mainDataList.value
  return mainDataList.value.filter(d => d.dataStatus === status)
}

onMounted(() => {
  loadModels()
})
</script>

<template>
  <ElCard title="主数据管理" class="card">
    <div class="card-header">
      <ElSelect v-model="selectedModelId" @change="loadMainData" style="width: 200px; margin-right: 20px;">
        <ElOption v-for="model in models" :key="model.id" :label="model.modelName" :value="model.id" />
      </ElSelect>
      <ElButton type="primary" icon="Plus" @click="openDialog()" :disabled="!selectedModelId">
        新增数据
      </ElButton>
    </div>

    <ElTabs type="card">
      <ElTabPane label="全部" name="all">
        <ElTable :data="filteredData('all')" stripe>
          <ElTableColumn prop="code" label="数据编码" />
          <ElTableColumn prop="dataStatus" label="状态">
            <template #default="scope">
              <span :class="getStatusClass(scope.row.dataStatus)">
                {{ scope.row.dataStatus }}
              </span>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="version" label="版本" />
          <ElTableColumn prop="createdByName" label="创建人" />
          <ElTableColumn prop="createTime" label="创建时间" />
          <ElTableColumn label="操作">
            <template #default="scope">
              <ElButton v-if="scope.row.dataStatus === '暂存'" type="primary" size="small" icon="Edit" @click="openDialog(scope.row)">编辑</ElButton>
              <ElButton v-if="scope.row.dataStatus === '暂存'" type="success" size="small" icon="Check" @click="handleSubmit(scope.row.id)">提交审核</ElButton>
              <ElButton v-if="scope.row.dataStatus === '暂存'" type="danger" size="small" icon="Delete" @click="handleDelete(scope.row.id)">删除</ElButton>
              <ElButton v-if="scope.row.dataStatus === '审核中'" type="success" size="small" icon="Check" @click="handleApprove(scope.row.id)">审核通过</ElButton>
              <ElButton v-if="scope.row.dataStatus === '审核中'" type="danger" size="small" icon="X" @click="handleReject(scope.row.id)">拒绝</ElButton>
              <ElButton v-if="scope.row.dataStatus === '审核通过'" type="primary" size="small" icon="Refresh" @click="handleCreateVersion(scope.row.id)">发起变更</ElButton>
              <ElButton v-if="scope.row.dataStatus === '审核通过'" type="warning" size="small" icon="Archive" @click="handleArchive(scope.row.id)">归档</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </ElTabPane>
      <ElTabPane label="暂存" name="draft">
        <ElTable :data="filteredData('暂存')" stripe>
          <ElTableColumn prop="code" label="数据编码" />
          <ElTableColumn prop="createdByName" label="创建人" />
          <ElTableColumn prop="createTime" label="创建时间" />
          <ElTableColumn label="操作">
            <template #default="scope">
              <ElButton type="primary" size="small" icon="Edit" @click="openDialog(scope.row)">编辑</ElButton>
              <ElButton type="success" size="small" icon="Check" @click="handleSubmit(scope.row.id)">提交审核</ElButton>
              <ElButton type="danger" size="small" icon="Delete" @click="handleDelete(scope.row.id)">删除</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </ElTabPane>
      <ElTabPane label="审核中" name="pending">
        <ElTable :data="filteredData('审核中')" stripe>
          <ElTableColumn prop="code" label="数据编码" />
          <ElTableColumn prop="createdByName" label="创建人" />
          <ElTableColumn prop="createTime" label="创建时间" />
          <ElTableColumn label="操作">
            <template #default="scope">
              <ElButton type="success" size="small" icon="Check" @click="handleApprove(scope.row.id)">审核通过</ElButton>
              <ElButton type="danger" size="small" icon="X" @click="handleReject(scope.row.id)">拒绝</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </ElTabPane>
      <ElTabPane label="审核通过" name="approved">
        <ElTable :data="filteredData('审核通过')" stripe>
          <ElTableColumn prop="code" label="数据编码" />
          <ElTableColumn prop="version" label="版本" />
          <ElTableColumn prop="createdByName" label="创建人" />
          <ElTableColumn prop="createTime" label="创建时间" />
          <ElTableColumn label="操作">
            <template #default="scope">
              <ElButton type="primary" size="small" icon="Refresh" @click="handleCreateVersion(scope.row.id)">发起变更</ElButton>
              <ElButton type="warning" size="small" icon="Archive" @click="handleArchive(scope.row.id)">归档</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </ElTabPane>
      <ElTabPane label="已归档" name="archived">
        <ElTable :data="filteredData('已归档')" stripe>
          <ElTableColumn prop="code" label="数据编码" />
          <ElTableColumn prop="createdByName" label="创建人" />
          <ElTableColumn prop="createTime" label="创建时间" />
        </ElTable>
      </ElTabPane>
    </ElTabs>
  </ElCard>

  <ElDialog title="数据详情" v-model="dialogVisible" width="500px">
    <ElForm :model="form" label-width="100px">
      <ElFormItem label="数据编码" required>
        <ElInput v-model="form.code" />
      </ElFormItem>
      <ElFormItem label="版本">
        <ElInput v-model="form.version" type="number" />
      </ElFormItem>
      <ElFormItem label="数据内容">
        <ElInput v-model="form.jsonData" type="textarea" :rows="5" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" @click="saveMainData">保存</ElButton>
    </template>
  </ElDialog>
</template>

<style scoped>
.card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.status-approved {
  color: #67c23a;
  background: #e8f5e9;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-pending {
  color: #e6a23c;
  background: #fdf6ec;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-draft {
  color: #909399;
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-rejected {
  color: #f56c6c;
  background: #fef0f0;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-archived {
  color: #606266;
  background: #e4e7ed;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
</style>