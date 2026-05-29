<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDistByModel, getDistData, getDistRecords, executeDist } from '../api/esb'
import { getAllModels } from '../api/models'
import type { MdmDataModel } from '../api/types'

interface DistInterface {
  id: string
  name: string
  code: string
  modelId: string
  syncType: string
  status: string
}

interface DistDataItem {
  id: string
  dataCode: string
  modelName: string
  status: string
  retryCount: number
  lastSyncTime: string
  errorMsg: string
}

interface DistRecord {
  id: string
  requestId: string
  requestPayload: string
  responsePayload: string
  status: string
  duration: number
  createTime: string
}

const models = ref<MdmDataModel[]>([])
const distInterfaces = ref<DistInterface[]>([])
const selectedDistId = ref('')
const activeTab = ref('pending')

const pendingData = ref<DistDataItem[]>([])
const syncedData = ref<DistDataItem[]>([])
const failedData = ref<DistDataItem[]>([])
const records = ref<DistRecord[]>([])
const loading = ref(false)
const recordsLoading = ref(false)

// 加载模型
const loadModels = async () => {
  try {
    const res = await getAllModels()
    models.value = res.data.data
  } catch {
    ElMessage.error('加载模型列表失败')
  }
}

// 加载所有模型的分发接口
const loadDistInterfaces = async () => {
  const allDists: DistInterface[] = []
  for (const model of models.value) {
    try {
      const res = await getDistByModel(model.id)
      if (res.data.data) {
        allDists.push(...res.data.data)
      }
    } catch {
      // ignore
    }
  }
  distInterfaces.value = allDists
}

// 加载分发数据
const loadDistData = async () => {
  if (!selectedDistId.value) {
    pendingData.value = []
    syncedData.value = []
    failedData.value = []
    return
  }
  loading.value = true
  try {
    const res = await getDistData(selectedDistId.value)
    const data: DistDataItem[] = res.data.data || []
    pendingData.value = data.filter(d => d.status === '待同步')
    syncedData.value = data.filter(d => d.status === '已同步')
    failedData.value = data.filter(d => d.status === '失败')
  } catch {
    pendingData.value = []
    syncedData.value = []
    failedData.value = []
  } finally {
    loading.value = false
  }
}

// 加载分发记录
const loadRecords = async () => {
  if (!selectedDistId.value) {
    records.value = []
    return
  }
  recordsLoading.value = true
  try {
    const res = await getDistRecords(selectedDistId.value)
    records.value = res.data.data || []
  } catch {
    records.value = []
  } finally {
    recordsLoading.value = false
  }
}

watch(selectedDistId, () => {
  loadDistData()
  loadRecords()
})

watch(activeTab, () => {
  if (activeTab.value === 'records') {
    loadRecords()
  }
})

// 重新分发
const handleResend = async (item: DistDataItem) => {
  try {
    await ElMessageBox.confirm('确定重新分发该数据？', '提示', { type: 'info' })
    await executeDist(selectedDistId.value, [item.dataCode])
    ElMessage.success('已触发重新分发')
    loadDistData()
  } catch {
    // cancelled
  }
}

const getStatusClass = (status: string) => {
  switch (status) {
    case '已同步': return 'status-active'
    case '待同步': return 'status-pending'
    case '失败': return 'status-rejected'
    default: return 'status-draft'
  }
}

const getRecordStatusClass = (status: string) => {
  switch (status) {
    case '成功': return 'status-active'
    case '失败': return 'status-rejected'
    case '处理中': return 'status-pending'
    default: return 'status-draft'
  }
}

onMounted(async () => {
  await loadModels()
  await loadDistInterfaces()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">分发监控</h2>
        <p class="page-desc">监控数据分发服务运行状态</p>
      </div>
    </div>

    <!-- 顶部选择分发接口 -->
    <div class="search-card">
      <div style="display: flex; align-items: center; gap: 16px;">
        <span style="color: var(--text-secondary); font-size: 14px; white-space: nowrap;">选择分发接口：</span>
        <el-select
          v-model="selectedDistId"
          placeholder="请选择分发接口"
          style="width: 360px;"
          clearable
        >
          <el-option
            v-for="d in distInterfaces"
            :key="d.id"
            :label="`${d.name} (${d.code})`"
            :value="d.id"
          />
        </el-select>
        <el-button :disabled="!selectedDistId" @click="loadDistData(); loadRecords()">
          刷新
        </el-button>
      </div>
    </div>

    <template v-if="selectedDistId">
      <!-- 统计卡片 -->
      <div class="monitor-stats">
        <div class="monitor-stat-card">
          <div class="stat-value pending">{{ pendingData.length }}</div>
          <div class="stat-label">待同步</div>
        </div>
        <div class="monitor-stat-card">
          <div class="stat-value synced">{{ syncedData.length }}</div>
          <div class="stat-label">已同步</div>
        </div>
        <div class="monitor-stat-card">
          <div class="stat-value failed">{{ failedData.length }}</div>
          <div class="stat-label">失败</div>
        </div>
      </div>

      <!-- 数据Tabs -->
      <div class="card-glow" style="padding: 20px; margin-top: 16px;">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="待同步" name="pending">
            <el-table :data="pendingData" stripe v-loading="loading" size="small">
              <el-table-column prop="dataCode" label="数据编码" min-width="120" />
              <el-table-column prop="modelName" label="模型" width="120" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="scope">
                  <span :class="getStatusClass(scope.row.status)">{{ scope.row.status }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="retryCount" label="重试次数" width="80" />
              <el-table-column prop="lastSyncTime" label="最后同步时间" width="180" />
              <el-table-column prop="errorMsg" label="错误信息" min-width="160" />
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="已同步" name="synced">
            <el-table :data="syncedData" stripe v-loading="loading" size="small">
              <el-table-column prop="dataCode" label="数据编码" min-width="120" />
              <el-table-column prop="modelName" label="模型" width="120" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="scope">
                  <span :class="getStatusClass(scope.row.status)">{{ scope.row.status }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="lastSyncTime" label="同步时间" width="180" />
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="失败" name="failed">
            <el-table :data="failedData" stripe v-loading="loading" size="small">
              <el-table-column prop="dataCode" label="数据编码" min-width="120" />
              <el-table-column prop="modelName" label="模型" width="120" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="scope">
                  <span :class="getStatusClass(scope.row.status)">{{ scope.row.status }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="retryCount" label="重试次数" width="80" />
              <el-table-column prop="lastSyncTime" label="最后同步时间" width="180" />
              <el-table-column prop="errorMsg" label="错误信息" min-width="160" />
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="scope">
                  <el-button type="primary" size="small" link @click="handleResend(scope.row)">重新分发</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="分发记录" name="records">
            <el-table :data="records" stripe v-loading="recordsLoading" size="small">
              <el-table-column prop="requestId" label="请求ID" width="160" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="scope">
                  <span :class="getRecordStatusClass(scope.row.status)">{{ scope.row.status }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="duration" label="耗时(ms)" width="100" />
              <el-table-column prop="createTime" label="时间" width="180" />
              <el-table-column label="请求报文" min-width="140">
                <template #default="scope">
                  <el-tooltip :content="scope.row.requestPayload" placement="top" :show-after="200">
                    <span class="text-ellipsis">{{ scope.row.requestPayload }}</span>
                  </el-tooltip>
                </template>
              </el-table-column>
              <el-table-column label="响应报文" min-width="140">
                <template #default="scope">
                  <el-tooltip :content="scope.row.responsePayload" placement="top" :show-after="200">
                    <span class="text-ellipsis">{{ scope.row.responsePayload }}</span>
                  </el-tooltip>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>

    <!-- 空状态 -->
    <div v-else class="card-glow" style="padding: 60px; text-align: center;">
      <p style="color: var(--text-secondary); font-size: 15px;">请选择一个分发接口查看监控数据</p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.monitor-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.monitor-stat-card {
  padding: 20px;
  text-align: center;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  backdrop-filter: blur(10px);
  transition: var(--transition-normal);

  &:hover {
    border-color: var(--border-glow);
    box-shadow: var(--shadow-glow);
  }

  .stat-value {
    font-size: 32px;
    font-weight: 700;
    line-height: 1.2;

    &.pending { color: #d97706; }
    &.synced { color: #10b981; }
    &.failed { color: #dc2626; }
  }

  .stat-label {
    font-size: 13px;
    color: var(--text-secondary);
    margin-top: 4px;
  }
}

.text-ellipsis {
  display: inline-block;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
  color: var(--text-secondary);
  cursor: pointer;
}
</style>
