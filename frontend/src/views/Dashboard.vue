<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElCard, ElRow, ElCol, ElStatistic, ElTable, ElTableColumn } from 'element-plus'
import { Document, DataLine, User as Users, CircleCheck } from '@element-plus/icons-vue'

import type { MdmDataModel, MdmMainData } from '../api/types'
import { getAllModels } from '../api/models'
import { getMainDataByModelId } from '../api/mainData'

const models = ref<MdmDataModel[]>([])
const mainDataList = ref<MdmMainData[]>([])
const stats = ref({
  totalModels: 0,
  totalData: 0,
  pendingReview: 0,
  approvedData: 0
})

const loadData = async () => {
  try {
    const modelsResponse = await getAllModels()
    models.value = modelsResponse.data.data

    stats.value.totalModels = models.value.length

    for (const model of models.value) {
      try {
        const dataResponse = await getMainDataByModelId(model.id)
        const dataList = dataResponse.data.data
        mainDataList.value.push(...dataList)
      } catch (e) {
        console.error(`加载模型${model.modelName}数据失败:`, e)
      }
    }

    stats.value.totalData = mainDataList.value.length
    stats.value.pendingReview = mainDataList.value.filter(d => d.dataStatus === '审核中').length
    stats.value.approvedData = mainDataList.value.filter(d => d.dataStatus === '审核通过').length
  } catch (error) {
    console.error('加载数据失败:', error)
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
    default:
      return ''
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="dashboard">
    <ElRow :gutter="20">
      <ElCol :span="6">
        <ElCard class="stat-card">
          <div class="stat-icon models">
            <Document />
          </div>
          <ElStatistic title="数据模型数" :value="stats.totalModels" suffix="个" />
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard class="stat-card">
          <div class="stat-icon data">
            <DataLine />
          </div>
          <ElStatistic title="主数据总量" :value="stats.totalData" suffix="条" />
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard class="stat-card">
          <div class="stat-icon pending">
            <Users />
          </div>
          <ElStatistic title="待审核" :value="stats.pendingReview" suffix="条" />
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard class="stat-card">
          <div class="stat-icon approved">
            <CircleCheck />
          </div>
          <ElStatistic title="已审核通过" :value="stats.approvedData" suffix="条" />
        </ElCard>
      </ElCol>
    </ElRow>

    <ElRow :gutter="20" style="margin-top: 20px;">
      <ElCol :span="12">
        <ElCard title="最近数据模型">
          <ElTable :data="models.slice(0, 5)" stripe>
            <ElTableColumn prop="modelCode" label="模型编码" />
            <ElTableColumn prop="modelName" label="模型名称" />
            <ElTableColumn prop="status" label="状态">
              <template #default="scope">
                <span :class="scope.row.status === '已发布' ? 'status-approved' : 'status-pending'">
                  {{ scope.row.status }}
                </span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="version" label="版本" />
          </ElTable>
        </ElCard>
      </ElCol>
      <ElCol :span="12">
        <ElCard title="最近数据记录">
          <ElTable :data="mainDataList.slice(0, 5)" stripe>
            <ElTableColumn prop="code" label="数据编码" />
            <ElTableColumn prop="dataStatus" label="状态">
              <template #default="scope">
                <span :class="getStatusClass(scope.row.dataStatus)">
                  {{ scope.row.dataStatus }}
                </span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="createdByName" label="创建人" />
            <ElTableColumn prop="createTime" label="创建时间" width="180" />
          </ElTable>
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>


<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.stat-icon.models {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.data {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.pending {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.approved {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
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
</style>