<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllModels } from '../api/models'
import { getMainDataByModelId, getArchiveList, archiveApply, archiveApprove, archiveReject, getArchiveData } from '../api/mainData'
import type { MdmDataModel } from '../api/types'
import StatusTag from '../components/StatusTag.vue'

const activeTab = ref('apply')
const models = ref<MdmDataModel[]>([])
const selectedModelId = ref('')
const dataList = ref<any[]>([])
const archiveList = ref<any[]>([])
const archivedDataList = ref<any[]>([])

// 归档申请
const selectedDataIds = ref<string[]>([])
const archiveReason = ref('')

// 归档查询
const searchKeyword = ref('')
const searchModelId = ref('')

const loadModels = async () => {
  try {
    const res = await getAllModels()
    models.value = res.data.data
  } catch {
    // ignore
  }
}

const loadDataList = async () => {
  if (!selectedModelId.value) {
    dataList.value = []
    return
  }
  try {
    const res = await getMainDataByModelId(selectedModelId.value)
    dataList.value = (res.data.data || []).filter((d: any) => d.dataStatus === '审核通过')
  } catch {
    dataList.value = []
  }
}

const loadArchiveList = async () => {
  try {
    const res = await getArchiveList()
    archiveList.value = res.data.data || []
  } catch {
    archiveList.value = []
  }
}

const loadArchivedData = async () => {
  try {
    const params: any = {}
    if (searchModelId.value) params.modelId = searchModelId.value
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await getArchiveData(params)
    archivedDataList.value = res.data.data || []
  } catch {
    archivedDataList.value = []
  }
}

const handleSelectionChange = (rows: any[]) => {
  selectedDataIds.value = rows.map(r => r.id)
}

const submitArchiveApply = async () => {
  if (selectedDataIds.value.length === 0) {
    ElMessage.warning('请选择要归档的数据')
    return
  }
  if (!archiveReason.value.trim()) {
    ElMessage.warning('请输入归档原因')
    return
  }
  try {
    await archiveApply({
      dataIds: selectedDataIds.value,
      reason: archiveReason.value
    })
    ElMessage.success('归档申请已提交')
    selectedDataIds.value = []
    archiveReason.value = ''
    loadDataList()
    loadArchiveList()
  } catch {
    ElMessage.error('提交失败')
  }
}

const handleArchiveApprove = async (applyId: string) => {
  try {
    await archiveApprove(applyId)
    ElMessage.success('已通过')
    loadArchiveList()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleArchiveReject = async (applyId: string) => {
  try {
    const reason = ''
    await archiveReject(applyId, { reason })
    ElMessage.success('已拒绝')
    loadArchiveList()
  } catch {
    ElMessage.error('操作失败')
  }
}

const getModelName = (modelId: string) => {
  const m = models.value.find(x => x.id === modelId)
  return m ? m.modelName : modelId
}

onMounted(async () => {
  await loadModels()
  await loadArchiveList()
  await loadArchivedData()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">数据归档</h2>
        <p class="page-desc">管理主数据归档与历史记录</p>
      </div>
    </div>

    <div class="table-card">
      <el-tabs v-model="activeTab" type="card">
        <!-- 归档申请 -->
        <el-tab-pane label="归档申请" name="apply">
          <div style="margin-bottom: 16px;">
            <span style="color: var(--text-secondary); margin-right: 8px;">选择模型：</span>
            <el-select v-model="selectedModelId" @change="loadDataList" style="width: 240px;" placeholder="请选择模型" clearable>
              <el-option v-for="m in models" :key="m.id" :label="m.modelName" :value="m.id" />
            </el-select>
          </div>
          <el-table :data="dataList" stripe @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="code" label="数据编码" />
            <el-table-column label="模型">
              <template #default="scope">{{ getModelName(scope.row.modelId) }}</template>
            </el-table-column>
            <el-table-column prop="createdByName" label="创建人" />
            <el-table-column prop="createTime" label="创建时间" />
          </el-table>
          <div v-if="selectedDataIds.length > 0" style="margin-top: 16px; padding: 16px; background: var(--bg-card); border-radius: var(--radius-sm);">
            <el-form label-width="100px">
              <el-form-item label="归档原因" required>
                <el-input v-model="archiveReason" type="textarea" :rows="3" placeholder="请输入归档原因" />
              </el-form-item>
            </el-form>
            <div style="text-align: right;">
              <el-button type="primary" @click="submitArchiveApply">提交归档申请</el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- 归档确认 -->
        <el-tab-pane label="归档确认" name="confirm">
          <el-table :data="archiveList.filter(a => a.status === '待确认')" stripe>
            <el-table-column prop="applyCode" label="申请单号" />
            <el-table-column prop="reason" label="归档原因" show-overflow-tooltip />
            <el-table-column prop="applyTime" label="申请时间" />
            <el-table-column prop="applicantName" label="申请人" />
            <el-table-column prop="status" label="状态">
              <template #default="scope"><StatusTag :status="scope.row.status" /></template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button type="success" size="small" @click="handleArchiveApprove(scope.row.id)">通过</el-button>
                <el-button type="danger" size="small" @click="handleArchiveReject(scope.row.id)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 归档查询 -->
        <el-tab-pane label="归档查询" name="query">
          <div style="display: flex; gap: 12px; margin-bottom: 16px;">
            <el-select v-model="searchModelId" style="width: 200px;" placeholder="选择模型" clearable>
              <el-option v-for="m in models" :key="m.id" :label="m.modelName" :value="m.id" />
            </el-select>
            <el-input v-model="searchKeyword" placeholder="搜索编码/内容" clearable style="width: 240px;" />
            <el-button type="primary" @click="loadArchivedData">查询</el-button>
          </div>
          <el-table :data="archivedDataList" stripe>
            <el-table-column prop="code" label="数据编码" />
            <el-table-column label="模型">
              <template #default="scope">{{ getModelName(scope.row.modelId) }}</template>
            </el-table-column>
            <el-table-column prop="archiveTime" label="归档时间" />
            <el-table-column prop="archivedByName" label="归档人" />
            <el-table-column prop="archiveReason" label="归档原因" show-overflow-tooltip />
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button type="primary" size="small" link @click="ElMessage.info(JSON.stringify(scope.row.jsonData || '{}'))">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped lang="scss">
</style>
