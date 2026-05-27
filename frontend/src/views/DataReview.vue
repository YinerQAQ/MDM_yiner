<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllModels } from '../api/models'
import { getMainDataByModelId, approveData, rejectData, getMainDataById } from '../api/mainData'
import type { MdmDataModel, MdmMainData } from '../api/types'
import StatusTag from '../components/StatusTag.vue'
import DataDiff from '../components/DataDiff.vue'

const models = ref<MdmDataModel[]>([])
const mainDataList = ref<MdmMainData[]>([])
const activeTab = ref('all')

const reviewDialogVisible = ref(false)
const reviewData = ref<MdmMainData | null>(null)
const reviewOpinion = ref('')
const selectedIds = ref<string[]>([])

const loadModels = async () => {
  try {
    const res = await getAllModels()
    models.value = res.data.data
  } catch {
    // ignore
  }
}

const loadMainData = async () => {
  try {
    const allData: MdmMainData[] = []
    for (const model of models.value) {
      try {
        const res = await getMainDataByModelId(model.id)
        const list = (res.data.data || []).map((d: MdmMainData) => ({
          ...d,
          _modelName: model.modelName
        }))
        allData.push(...list)
      } catch {
        // ignore
      }
    }
    mainDataList.value = allData
  } catch {
    ElMessage.error('加载数据失败')
  }
}

const filteredData = computed(() => {
  if (activeTab.value === 'all') {
    return mainDataList.value.filter(d => d.dataStatus === '审核中')
  }
  if (activeTab.value === 'mine') {
    return mainDataList.value.filter(d => d.dataStatus === '审核中')
  }
  if (activeTab.value === 'done') {
    return mainDataList.value.filter(d => d.dataStatus === '审核通过' || d.dataStatus === '审核拒绝')
  }
  return mainDataList.value
})

const openReviewDialog = async (row: any) => {
  try {
    const res = await getMainDataById(row.id)
    reviewData.value = res.data.data
    reviewOpinion.value = ''
    reviewDialogVisible.value = true
  } catch {
    ElMessage.error('加载详情失败')
  }
}

const handleApprove = async (id?: string) => {
  const ids = id ? [id] : selectedIds.value
  if (ids.length === 0) {
    ElMessage.warning('请选择要审核的数据')
    return
  }
  try {
    for (const itemId of ids) {
      await approveData(itemId)
    }
    ElMessage.success('审核通过')
    reviewDialogVisible.value = false
    selectedIds.value = []
    loadMainData()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleReject = async (id?: string) => {
  const ids = id ? [id] : selectedIds.value
  if (ids.length === 0) {
    ElMessage.warning('请选择要拒绝的数据')
    return
  }
  try {
    for (const itemId of ids) {
      await rejectData(itemId)
    }
    ElMessage.success('已拒绝')
    reviewDialogVisible.value = false
    selectedIds.value = []
    loadMainData()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleSelectionChange = (rows: any[]) => {
  selectedIds.value = rows.map(r => r.id)
}

const getModelName = (modelId: string) => {
  const m = models.value.find(x => x.id === modelId)
  return m ? m.modelName : modelId
}

const formatJsonData = (json?: string) => {
  if (!json) return {}
  try {
    return JSON.parse(json)
  } catch {
    return {}
  }
}

onMounted(async () => {
  await loadModels()
  await loadMainData()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">数据审核</h2>
        <p class="page-desc">审核主数据新增与变更申请</p>
      </div>
      <div v-if="activeTab !== 'done'" style="display: flex; gap: 8px;">
        <el-button type="success" @click="handleApprove()">批量通过</el-button>
        <el-button type="danger" @click="handleReject()">批量拒绝</el-button>
      </div>
    </div>

    <div class="table-card">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="全部待审" name="all">
          <el-table :data="filteredData" stripe @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="code" label="数据编码" />
            <el-table-column label="模型">
              <template #default="scope">{{ getModelName(scope.row.modelId) }}</template>
            </el-table-column>
            <el-table-column prop="createdByName" label="申请人" />
            <el-table-column prop="createTime" label="申请时间" />
            <el-table-column prop="dataStatus" label="状态">
              <template #default="scope"><StatusTag :status="scope.row.dataStatus" /></template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button type="success" size="small" @click="handleApprove(scope.row.id)">通过</el-button>
                <el-button type="danger" size="small" @click="handleReject(scope.row.id)">拒绝</el-button>
                <el-button type="primary" size="small" link @click="openReviewDialog(scope.row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="个人待审" name="mine">
          <el-table :data="filteredData" stripe @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="code" label="数据编码" />
            <el-table-column label="模型">
              <template #default="scope">{{ getModelName(scope.row.modelId) }}</template>
            </el-table-column>
            <el-table-column prop="createdByName" label="申请人" />
            <el-table-column prop="createTime" label="申请时间" />
            <el-table-column prop="dataStatus" label="状态">
              <template #default="scope"><StatusTag :status="scope.row.dataStatus" /></template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button type="success" size="small" @click="handleApprove(scope.row.id)">通过</el-button>
                <el-button type="danger" size="small" @click="handleReject(scope.row.id)">拒绝</el-button>
                <el-button type="primary" size="small" link @click="openReviewDialog(scope.row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="已审核" name="done">
          <el-table :data="filteredData" stripe>
            <el-table-column prop="code" label="数据编码" />
            <el-table-column label="模型">
              <template #default="scope">{{ getModelName(scope.row.modelId) }}</template>
            </el-table-column>
            <el-table-column prop="createdByName" label="申请人" />
            <el-table-column prop="createTime" label="申请时间" />
            <el-table-column prop="dataStatus" label="状态">
              <template #default="scope"><StatusTag :status="scope.row.dataStatus" /></template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 审核详情Dialog -->
    <el-dialog title="审核详情" v-model="reviewDialogVisible" width="600px">
      <div v-if="reviewData">
        <div style="margin-bottom: 16px;">
          <span style="color: var(--text-secondary);">数据编码：</span>
          <span style="color: var(--text-bright); font-weight: 500;">{{ reviewData.code }}</span>
        </div>
        <div style="margin-bottom: 16px;">
          <span style="color: var(--text-secondary);">所属模型：</span>
          <span style="color: var(--text-bright);">{{ getModelName(reviewData.modelId) }}</span>
        </div>
        <div style="margin-bottom: 16px;">
          <span style="color: var(--text-secondary);">申请人：</span>
          <span style="color: var(--text-bright);">{{ reviewData.createdByName }}</span>
        </div>
        <div style="margin-bottom: 16px;">
          <span style="color: var(--text-secondary);">数据内容：</span>
          <div style="margin-top: 8px; padding: 12px; background: var(--bg-card); border-radius: var(--radius-sm);">
            <pre style="color: var(--text-primary); font-size: 13px; white-space: pre-wrap; word-break: break-all;">{{ JSON.stringify(formatJsonData(reviewData.jsonData), null, 2) }}</pre>
          </div>
        </div>
        <el-form>
          <el-form-item label="审核意见">
            <el-input v-model="reviewOpinion" type="textarea" :rows="3" placeholder="请输入审核意见（可选）" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleReject(reviewData?.id)">拒绝</el-button>
        <el-button type="success" @click="handleApprove(reviewData?.id)">通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
</style>
