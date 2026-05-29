<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  ElTabs, ElTabPane, ElTable, ElTableColumn, ElButton, ElDialog,
  ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElIcon,
  ElMessage, ElTag, ElCard, ElTooltip
} from 'element-plus'
import {
  Download, Refresh, VideoPlay, VideoPause,
  Warning, CircleCheck, CircleClose,
  TrendCharts, DataLine
} from '@element-plus/icons-vue'

// ==================== 数据分发 Tab ====================
interface DistInterface {
  id: string
  name: string
  modelCode: string
  syncType: string
  status: string
}

const interfaces = ref<DistInterface[]>([
  { id: '1', name: '用户数据分发', modelCode: 'USER_MODEL', syncType: '即时', status: '启用' },
  { id: '2', name: '组织数据分发', modelCode: 'ORG_MODEL', syncType: '定时', status: '启用' },
  { id: '3', name: '产品数据分发', modelCode: 'PRODUCT_MODEL', syncType: '手动', status: '停用' }
])

const dialogVisible = ref(false)
const form = ref({
  id: '',
  name: '',
  modelCode: '',
  syncType: '即时',
  status: '启用'
})

const openDialog = () => {
  form.value = { id: '', name: '', modelCode: '', syncType: '即时', status: '启用' }
  dialogVisible.value = true
}

const saveInterface = () => {
  if (!form.value.name || !form.value.modelCode) {
    ElMessage.error('请填写必填字段')
    return
  }
  if (form.value.id) {
    const index = interfaces.value.findIndex(i => i.id === form.value.id)
    if (index !== -1) interfaces.value[index] = { ...form.value }
    ElMessage.success('更新成功')
  } else {
    form.value.id = `INTERFACE_${Date.now()}`
    interfaces.value.push({ ...form.value })
    ElMessage.success('创建成功')
  }
  dialogVisible.value = false
}

const handleDelete = (id: string) => {
  interfaces.value = interfaces.value.filter(i => i.id !== id)
  ElMessage.success('删除成功')
}

const getDistStatusClass = (status: string): string => {
  return status === '启用' ? 'status-active' : 'status-inactive'
}

// ==================== 数据接收 Tab ====================
interface ReceiveRecord {
  id: string
  sourceSystem: string
  modelCode: string
  modelName: string
  receiveTime: string
  dataCount: number
  successCount: number
  failCount: number
  status: '成功' | '失败' | '处理中'
  errorMsgs: string[]
}

const receiveRecords = ref<ReceiveRecord[]>([
  { id: 'R001', sourceSystem: 'ERP系统', modelCode: 'MATERIAL', modelName: '物料主数据', receiveTime: '2024-01-15 14:30:22', dataCount: 256, successCount: 253, failCount: 3, status: '成功', errorMsgs: ['第12行：物料编码格式错误', '第45行：必填字段为空', '第78行：数据类型不匹配'] },
  { id: 'R002', sourceSystem: 'CRM系统', modelCode: 'CUSTOMER', modelName: '客户主数据', receiveTime: '2024-01-15 13:15:08', dataCount: 128, successCount: 128, failCount: 0, status: '成功', errorMsgs: [] },
  { id: 'R003', sourceSystem: 'SRM系统', modelCode: 'SUPPLIER', modelName: '供应商数据', receiveTime: '2024-01-15 11:20:45', dataCount: 64, successCount: 0, failCount: 64, status: '失败', errorMsgs: ['连接超时：目标系统未响应', '数据格式校验失败：不符合V2.0标准'] },
  { id: 'R004', sourceSystem: 'ERP系统', modelCode: 'ORG', modelName: '组织数据', receiveTime: '2024-01-15 10:05:33', dataCount: 32, successCount: 30, failCount: 2, status: '成功', errorMsgs: ['第5行：上级组织不存在', '第18行：组织编码重复'] },
  { id: 'R005', sourceSystem: 'HR系统', modelCode: 'EMPLOYEE', modelName: '人员数据', receiveTime: '2024-01-14 17:45:12', dataCount: 512, successCount: 0, failCount: 0, status: '处理中', errorMsgs: [] },
  { id: 'R006', sourceSystem: 'MES系统', modelCode: 'PRODUCT', modelName: '产品数据', receiveTime: '2024-01-14 16:30:00', dataCount: 96, successCount: 95, failCount: 1, status: '成功', errorMsgs: ['第33行：产品分类编码无效'] },
  { id: 'R007', sourceSystem: 'WMS系统', modelCode: 'WAREHOUSE', modelName: '仓库数据', receiveTime: '2024-01-14 14:22:18', dataCount: 16, successCount: 16, failCount: 0, status: '成功', errorMsgs: [] },
  { id: 'R008', sourceSystem: 'CRM系统', modelCode: 'CUSTOMER', modelName: '客户主数据', receiveTime: '2024-01-14 09:10:55', dataCount: 200, successCount: 198, failCount: 2, status: '成功', errorMsgs: ['第102行：客户等级值超范围', '第155行：联系方式格式错误'] }
])

const receiveFilter = ref({
  timeRange: [] as string[],
  sourceSystem: '',
  status: ''
})

const receiveDetailVisible = ref(false)
const receiveDetailData = ref<ReceiveRecord | null>(null)

const filteredReceiveRecords = computed(() => {
  let result = receiveRecords.value
  if (receiveFilter.value.sourceSystem) {
    result = result.filter(r => r.sourceSystem === receiveFilter.value.sourceSystem)
  }
  if (receiveFilter.value.status) {
    result = result.filter(r => r.status === receiveFilter.value.status)
  }
  return result
})

const receiveStats = computed(() => {
  const list = receiveRecords.value
  const today = list.length
  const successCount = list.filter(r => r.status === '成功').length
  const rate = today > 0 ? Math.round((successCount / today) * 100) : 0
  const totalData = list.reduce((sum, r) => sum + r.dataCount, 0)
  return { today, rate, totalData }
})

const openReceiveDetail = (row: ReceiveRecord) => {
  receiveDetailData.value = { ...row }
  receiveDetailVisible.value = true
}

const handleManualReceive = () => {
  ElMessage.success('已触发手动接收任务，请稍后查看接收记录')
}

const getReceiveStatusType = (status: string): 'success' | 'warning' | 'danger' | 'info' => {
  switch (status) {
    case '成功': return 'success'
    case '失败': return 'danger'
    case '处理中': return 'warning'
    default: return 'info'
  }
}

// ==================== 服务监控 Tab ====================
interface ServiceInfo {
  id: string
  name: string
  targetSystem: string
  status: '运行中' | '已停止' | '异常'
  lastExecTime: string
  lastResult: '成功' | '失败'
  avgDuration: number
  todayCalls: number
  successRate: number
}

const services = ref<ServiceInfo[]>([
  { id: 'S001', name: 'ERP物料数据同步', targetSystem: 'ERP系统', status: '运行中', lastExecTime: '2024-01-15 14:30:00', lastResult: '成功', avgDuration: 1250, todayCalls: 48, successRate: 98.5 },
  { id: 'S002', name: 'CRM客户数据推送', targetSystem: 'CRM系统', status: '运行中', lastExecTime: '2024-01-15 14:25:00', lastResult: '成功', avgDuration: 890, todayCalls: 36, successRate: 99.2 },
  { id: 'S003', name: 'SRM供应商同步', targetSystem: 'SRM系统', status: '异常', lastExecTime: '2024-01-15 11:20:00', lastResult: '失败', avgDuration: 3200, todayCalls: 12, successRate: 75.0 },
  { id: 'S004', name: 'HR人员数据同步', targetSystem: 'HR系统', status: '运行中', lastExecTime: '2024-01-15 13:50:00', lastResult: '成功', avgDuration: 560, todayCalls: 24, successRate: 99.8 },
  { id: 'S005', name: 'MES产品数据分发', targetSystem: 'MES系统', status: '已停止', lastExecTime: '2024-01-14 18:00:00', lastResult: '成功', avgDuration: 1100, todayCalls: 0, successRate: 97.3 },
  { id: 'S006', name: 'WMS仓库数据推送', targetSystem: 'WMS系统', status: '运行中', lastExecTime: '2024-01-15 14:10:00', lastResult: '成功', avgDuration: 430, todayCalls: 18, successRate: 100 }
])

interface AlertRecord {
  id: string
  time: string
  serviceName: string
  level: '严重' | '警告' | '提示'
  message: string
}

const alertRecords = ref<AlertRecord[]>([
  { id: 'A001', time: '2024-01-15 14:32:10', serviceName: 'SRM供应商同步', level: '严重', message: '连接SRM系统超时，已连续3次失败' },
  { id: 'A002', time: '2024-01-15 13:15:45', serviceName: 'ERP物料数据同步', level: '警告', message: '响应时间超过阈值（3200ms > 2000ms）' },
  { id: 'A003', time: '2024-01-15 11:22:30', serviceName: 'SRM供应商同步', level: '严重', message: '数据格式校验失败，批次停止处理' },
  { id: 'A004', time: '2024-01-15 09:45:00', serviceName: 'CRM客户数据推送', level: '提示', message: '推送数据量较大，可能导致延迟' },
  { id: 'A005', time: '2024-01-14 22:10:15', serviceName: 'MES产品数据分发', level: '警告', message: '服务已停止，等待人工启动' },
  { id: 'A006', time: '2024-01-14 18:05:00', serviceName: 'HR人员数据同步', level: '提示', message: '增量同步完成，处理0条变更' }
])

const serviceStats = computed(() => {
  const running = services.value.filter(s => s.status === '运行中').length
  const stopped = services.value.filter(s => s.status === '已停止').length
  const error = services.value.filter(s => s.status === '异常').length
  const totalCalls = services.value.reduce((sum, s) => sum + s.todayCalls, 0)
  const avgRate = services.value.length > 0
    ? Math.round(services.value.reduce((sum, s) => sum + s.successRate, 0) / services.value.length * 10) / 10
    : 0
  const avgTime = services.value.length > 0
    ? Math.round(services.value.reduce((sum, s) => sum + s.avgDuration, 0) / services.value.length)
    : 0
  return { running, stopped, error, totalCalls, avgRate, avgTime }
})

const toggleService = (service: ServiceInfo) => {
  if (service.status === '运行中') {
    service.status = '已停止'
    service.todayCalls = 0
    ElMessage.success(`服务 [${service.name}] 已停止`)
  } else {
    service.status = '运行中'
    ElMessage.success(`服务 [${service.name}] 已启动`)
  }
}

const getServiceStatusType = (status: string): 'success' | 'warning' | 'danger' | 'info' => {
  switch (status) {
    case '运行中': return 'success'
    case '已停止': return 'info'
    case '异常': return 'danger'
    default: return 'info'
  }
}

const getResultType = (result: string): 'success' | 'danger' => {
  return result === '成功' ? 'success' : 'danger'
}

const getAlertLevelType = (level: string): 'success' | 'warning' | 'danger' | 'info' => {
  switch (level) {
    case '严重': return 'danger'
    case '警告': return 'warning'
    case '提示': return 'info'
    default: return 'info'
  }
}

const formatDuration = (ms: number): string => {
  if (ms < 1000) return `${ms}ms`
  return `${(ms / 1000).toFixed(1)}s`
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">数据交换</h2>
        <p class="page-desc">管理数据分发接口、数据接收与服务监控</p>
      </div>
    </div>

    <div class="table-card">
      <ElTabs type="card">
        <!-- ==================== 数据分发 Tab ==================== -->
        <ElTabPane label="数据分发" name="dist">
          <div style="display: flex; justify-content: flex-end; margin-bottom: 16px;">
            <ElButton type="primary" @click="openDialog()">新建分发接口</ElButton>
          </div>
          <ElTable :data="interfaces" stripe>
            <ElTableColumn prop="name" label="接口名称" />
            <ElTableColumn prop="modelCode" label="关联模型" />
            <ElTableColumn prop="syncType" label="同步类型">
              <template #default="scope">
                <span v-if="scope.row.syncType === '即时'">即时同步</span>
                <span v-else-if="scope.row.syncType === '定时'">定时同步</span>
                <span v-else>手动同步</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="status" label="状态">
              <template #default="scope">
                <span :class="getDistStatusClass(scope.row.status)">{{ scope.row.status }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn label="操作" width="160">
              <template #default="scope">
                <ElButton type="primary" size="small">编辑</ElButton>
                <ElButton type="danger" size="small" @click="handleDelete(scope.row.id)">删除</ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElTabPane>

        <!-- ==================== 数据接收 Tab ==================== -->
        <ElTabPane label="数据接收" name="receive">
          <!-- 统计卡片 -->
          <div class="stat-cards">
            <ElCard shadow="hover" class="stat-card-item">
              <div class="stat-card-inner">
                <div class="stat-icon-box receive">
                  <ElIcon :size="22"><Download /></ElIcon>
                </div>
                <div class="stat-detail">
                  <div class="stat-number">{{ receiveStats.today }}</div>
                  <div class="stat-desc">今日接收</div>
                </div>
              </div>
            </ElCard>
            <ElCard shadow="hover" class="stat-card-item">
              <div class="stat-card-inner">
                <div class="stat-icon-box rate">
                  <ElIcon :size="22"><TrendCharts /></ElIcon>
                </div>
                <div class="stat-detail">
                  <div class="stat-number">{{ receiveStats.rate }}%</div>
                  <div class="stat-desc">成功率</div>
                </div>
              </div>
            </ElCard>
            <ElCard shadow="hover" class="stat-card-item">
              <div class="stat-card-inner">
                <div class="stat-icon-box total">
                  <ElIcon :size="22"><DataLine /></ElIcon>
                </div>
                <div class="stat-detail">
                  <div class="stat-number">{{ receiveStats.totalData }}</div>
                  <div class="stat-desc">总接收量</div>
                </div>
              </div>
            </ElCard>
          </div>

          <!-- 筛选栏 -->
          <div class="filter-bar">
            <ElForm inline>
              <ElFormItem label="来源系统">
                <ElSelect v-model="receiveFilter.sourceSystem" placeholder="全部" clearable style="width: 160px;">
                  <ElOption label="ERP系统" value="ERP系统" />
                  <ElOption label="CRM系统" value="CRM系统" />
                  <ElOption label="SRM系统" value="SRM系统" />
                  <ElOption label="HR系统" value="HR系统" />
                  <ElOption label="MES系统" value="MES系统" />
                  <ElOption label="WMS系统" value="WMS系统" />
                </ElSelect>
              </ElFormItem>
              <ElFormItem label="状态">
                <ElSelect v-model="receiveFilter.status" placeholder="全部" clearable style="width: 120px;">
                  <ElOption label="成功" value="成功" />
                  <ElOption label="失败" value="失败" />
                  <ElOption label="处理中" value="处理中" />
                </ElSelect>
              </ElFormItem>
              <ElFormItem>
                <ElButton type="primary" @click="handleManualReceive">
                  <ElIcon style="margin-right: 4px;"><Refresh /></ElIcon>手动触发接收
                </ElButton>
              </ElFormItem>
            </ElForm>
          </div>

          <!-- 接收记录列表 -->
          <ElTable :data="filteredReceiveRecords" stripe row-class-name="data-table-row">
            <ElTableColumn prop="sourceSystem" label="来源系统" width="120" />
            <ElTableColumn prop="modelName" label="数据模型" min-width="140" />
            <ElTableColumn prop="receiveTime" label="接收时间" width="180" />
            <ElTableColumn prop="dataCount" label="数据量" width="100" align="center">
              <template #default="scope">
                <span class="cell-number">{{ scope.row.dataCount }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="status" label="状态" width="100" align="center">
              <template #default="scope">
                <ElTag :type="getReceiveStatusType(scope.row.status)" size="small" effect="dark">{{ scope.row.status }}</ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn label="成功/失败" width="120" align="center">
              <template #default="scope">
                <span class="success-count">{{ scope.row.successCount }}</span>
                <span style="color: var(--text-muted);"> / </span>
                <span :class="scope.row.failCount > 0 ? 'fail-count' : ''">{{ scope.row.failCount }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn label="操作" width="100" fixed="right">
              <template #default="scope">
                <ElButton type="primary" size="small" link @click="openReceiveDetail(scope.row)">详情</ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElTabPane>

        <!-- ==================== 服务监控 Tab ==================== -->
        <ElTabPane label="服务监控" name="monitor">
          <!-- 服务状态总览 -->
          <div class="stat-cards">
            <ElCard shadow="hover" class="stat-card-item">
              <div class="stat-card-inner">
                <div class="stat-icon-box running">
                  <ElIcon :size="22"><VideoPlay /></ElIcon>
                </div>
                <div class="stat-detail">
                  <div class="stat-number running-num">{{ serviceStats.running }}</div>
                  <div class="stat-desc">运行中</div>
                </div>
              </div>
            </ElCard>
            <ElCard shadow="hover" class="stat-card-item">
              <div class="stat-card-inner">
                <div class="stat-icon-box stopped">
                  <ElIcon :size="22"><VideoPause /></ElIcon>
                </div>
                <div class="stat-detail">
                  <div class="stat-number stopped-num">{{ serviceStats.stopped }}</div>
                  <div class="stat-desc">已停止</div>
                </div>
              </div>
            </ElCard>
            <ElCard shadow="hover" class="stat-card-item">
              <div class="stat-card-inner">
                <div class="stat-icon-box error">
                  <ElIcon :size="22"><Warning /></ElIcon>
                </div>
                <div class="stat-detail">
                  <div class="stat-number error-num">{{ serviceStats.error }}</div>
                  <div class="stat-desc">异常</div>
                </div>
              </div>
            </ElCard>
          </div>

          <!-- 调用统计 -->
          <div class="stat-cards" style="margin-top: 12px;">
            <ElCard shadow="hover" class="stat-card-item mini">
              <div class="mini-stat">
                <div class="mini-stat-value">{{ serviceStats.totalCalls }}</div>
                <div class="mini-stat-label">今日调用次数</div>
              </div>
            </ElCard>
            <ElCard shadow="hover" class="stat-card-item mini">
              <div class="mini-stat">
                <div class="mini-stat-value">{{ serviceStats.avgRate }}%</div>
                <div class="mini-stat-label">平均成功率</div>
              </div>
            </ElCard>
            <ElCard shadow="hover" class="stat-card-item mini">
              <div class="mini-stat">
                <div class="mini-stat-value">{{ formatDuration(serviceStats.avgTime) }}</div>
                <div class="mini-stat-label">平均响应时间</div>
              </div>
            </ElCard>
          </div>

          <!-- 服务列表 -->
          <div class="section-header">
            <span class="section-title">服务列表</span>
          </div>
          <ElTable :data="services" stripe row-class-name="data-table-row">
            <ElTableColumn prop="name" label="服务名称" min-width="180" />
            <ElTableColumn prop="targetSystem" label="目标系统" width="120" />
            <ElTableColumn prop="status" label="状态" width="100" align="center">
              <template #default="scope">
                <ElTag :type="getServiceStatusType(scope.row.status)" size="small" effect="dark">{{ scope.row.status }}</ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="lastExecTime" label="最近执行" width="180" />
            <ElTableColumn prop="lastResult" label="执行结果" width="100" align="center">
              <template #default="scope">
                <ElTag :type="getResultType(scope.row.lastResult)" size="small">{{ scope.row.lastResult }}</ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="avgDuration" label="平均耗时" width="110" align="center">
              <template #default="scope">
                <span class="cell-duration" :class="{ 'slow': scope.row.avgDuration > 2000 }">{{ formatDuration(scope.row.avgDuration) }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="todayCalls" label="今日调用" width="100" align="center">
              <template #default="scope">
                <span class="cell-number">{{ scope.row.todayCalls }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="successRate" label="成功率" width="100" align="center">
              <template #default="scope">
                <span class="cell-rate" :class="{ 'low-rate': scope.row.successRate < 90 }">{{ scope.row.successRate }}%</span>
              </template>
            </ElTableColumn>
            <ElTableColumn label="操作" width="120" fixed="right">
              <template #default="scope">
                <ElTooltip :content="scope.row.status === '运行中' ? '停止服务' : '启动服务'" placement="top">
                  <ElButton
                    :type="scope.row.status === '运行中' ? 'danger' : 'success'"
                    size="small"
                    circle
                    @click="toggleService(scope.row)"
                  >
                    <ElIcon><component :is="scope.row.status === '运行中' ? VideoPause : VideoPlay" /></ElIcon>
                  </ElButton>
                </ElTooltip>
              </template>
            </ElTableColumn>
          </ElTable>

          <!-- 异常告警 -->
          <div class="section-header" style="margin-top: 24px;">
            <span class="section-title">异常告警</span>
            <ElTag type="danger" size="small" effect="dark" style="margin-left: 8px;">{{ alertRecords.length }}</ElTag>
          </div>
          <ElTable :data="alertRecords" stripe size="small">
            <ElTableColumn prop="time" label="告警时间" width="180" />
            <ElTableColumn prop="serviceName" label="服务名称" min-width="180" />
            <ElTableColumn prop="level" label="级别" width="80" align="center">
              <template #default="scope">
                <ElTag :type="getAlertLevelType(scope.row.level)" size="small" effect="dark">{{ scope.row.level }}</ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="message" label="错误信息" min-width="300" />
          </ElTable>
        </ElTabPane>
      </ElTabs>
    </div>

    <!-- 分发接口配置对话框 -->
    <ElDialog title="分发接口配置" v-model="dialogVisible" width="500px">
      <ElForm :model="form" label-width="100px">
        <ElFormItem label="接口名称" required>
          <ElInput v-model="form.name" />
        </ElFormItem>
        <ElFormItem label="关联模型" required>
          <ElInput v-model="form.modelCode" />
        </ElFormItem>
        <ElFormItem label="同步类型">
          <ElSelect v-model="form.syncType">
            <ElOption label="即时同步" value="即时" />
            <ElOption label="定时同步" value="定时" />
            <ElOption label="手动同步" value="手动" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="form.status">
            <ElOption label="启用" value="启用" />
            <ElOption label="停用" value="停用" />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="saveInterface">保存</ElButton>
      </template>
    </ElDialog>

    <!-- 接收详情对话框 -->
    <ElDialog title="接收详情" v-model="receiveDetailVisible" width="600px">
      <div v-if="receiveDetailData" class="receive-detail">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">来源系统</span>
            <span class="detail-value">{{ receiveDetailData.sourceSystem }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">数据模型</span>
            <span class="detail-value">{{ receiveDetailData.modelName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">接收时间</span>
            <span class="detail-value">{{ receiveDetailData.receiveTime }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">状态</span>
            <ElTag :type="getReceiveStatusType(receiveDetailData.status)" size="small" effect="dark">{{ receiveDetailData.status }}</ElTag>
          </div>
        </div>

        <div class="detail-counts">
          <div class="count-item">
            <span class="count-label">数据条数</span>
            <span class="count-value">{{ receiveDetailData.dataCount }}</span>
          </div>
          <div class="count-item success">
            <span class="count-label">成功数</span>
            <span class="count-value">{{ receiveDetailData.successCount }}</span>
          </div>
          <div class="count-item fail">
            <span class="count-label">失败数</span>
            <span class="count-value">{{ receiveDetailData.failCount }}</span>
          </div>
        </div>

        <div v-if="receiveDetailData.errorMsgs.length > 0" class="error-list">
          <h4 class="error-title">错误信息列表</h4>
          <div class="error-items">
            <div v-for="(msg, idx) in receiveDetailData.errorMsgs" :key="idx" class="error-item">
              <ElIcon color="var(--color-danger)" :size="14"><CircleClose /></ElIcon>
              <span>{{ msg }}</span>
            </div>
          </div>
        </div>
        <div v-else class="no-errors">
          <ElIcon color="var(--color-success)" :size="20"><CircleCheck /></ElIcon>
          <span>无错误信息</span>
        </div>
      </div>
    </ElDialog>
  </div>
</template>

<style scoped lang="scss">
// 分发状态
.status-active {
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

.status-inactive {
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

// 统计卡片
.stat-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card-item {
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  transition: var(--transition-normal);

  :deep(.el-card__body) {
    padding: 16px 20px;
  }

  &:hover {
    border-color: var(--border-glow);
    box-shadow: var(--shadow-glow);
  }

  &.mini {
    :deep(.el-card__body) {
      padding: 12px 16px;
    }
  }
}

.stat-card-inner {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon-box {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;

  &.receive {
    background: linear-gradient(135deg, #00d4ff, #0ea5e9);
    box-shadow: 0 4px 12px rgba(0, 212, 255, 0.3);
  }
  &.rate {
    background: linear-gradient(135deg, #10b981, #34d399);
    box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
  }
  &.total {
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  }
  &.running {
    background: linear-gradient(135deg, #10b981, #34d399);
    box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
  }
  &.stopped {
    background: linear-gradient(135deg, #64748b, #94a3b8);
    box-shadow: 0 4px 12px rgba(100, 116, 139, 0.3);
  }
  &.error {
    background: linear-gradient(135deg, #ef4444, #f97316);
    box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
  }
}

.stat-detail {
  flex: 1;
  .stat-number {
    font-size: 24px;
    font-weight: 700;
    color: var(--text-bright);
    line-height: 1.2;
    font-variant-numeric: tabular-nums;

    &.running-num { color: var(--color-success); }
    &.stopped-num { color: var(--text-secondary); }
    &.error-num { color: var(--color-danger); }
  }
  .stat-desc {
    font-size: 12px;
    color: var(--text-secondary);
    margin-top: 2px;
  }
}

.mini-stat {
  text-align: center;
  .mini-stat-value {
    font-size: 20px;
    font-weight: 700;
    color: var(--color-primary);
    font-variant-numeric: tabular-nums;
  }
  .mini-stat-label {
    font-size: 12px;
    color: var(--text-secondary);
    margin-top: 2px;
  }
}

// 筛选栏
.filter-bar {
  margin-bottom: 16px;
  padding: 12px 16px;
  background: rgba(13, 27, 42, 0.5);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
}

// 区块标题
.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-bright);
    padding-left: 10px;
    border-left: 3px solid var(--color-primary);
  }
}

// 表格增强
:deep(.data-table-row) {
  cursor: default;
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

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background: rgba(13, 27, 42, 0.3);
}

.cell-number {
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  color: var(--text-bright);
}

.success-count {
  color: var(--color-success);
  font-weight: 500;
}

.fail-count {
  color: var(--color-danger);
  font-weight: 500;
}

.cell-duration {
  font-family: var(--font-mono);
  font-size: 13px;
  font-variant-numeric: tabular-nums;
  color: var(--text-primary);

  &.slow { color: var(--color-danger); }
}

.cell-rate {
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  color: var(--color-success);

  &.low-rate { color: var(--color-danger); }
}

// 接收详情
.receive-detail {
  .detail-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    margin-bottom: 20px;
  }

  .detail-item {
    display: flex;
    flex-direction: column;
    gap: 4px;
    .detail-label {
      font-size: 12px;
      color: var(--text-muted);
    }
    .detail-value {
      font-size: 13px;
      color: var(--text-primary);
    }
  }

  .detail-counts {
    display: flex;
    gap: 16px;
    margin-bottom: 20px;
  }

  .count-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16px;
    border-radius: var(--radius-sm);
    background: rgba(13, 27, 42, 0.5);
    border: 1px solid var(--border-color);

    .count-label {
      font-size: 12px;
      color: var(--text-secondary);
      margin-bottom: 6px;
    }
    .count-value {
      font-size: 22px;
      font-weight: 700;
      color: var(--text-bright);
      font-variant-numeric: tabular-nums;
    }

    &.success .count-value { color: var(--color-success); }
    &.fail .count-value { color: var(--color-danger); }
  }

  .error-list {
    .error-title {
      font-size: 14px;
      font-weight: 600;
      color: var(--color-danger);
      margin: 0 0 12px;
      padding-left: 10px;
      border-left: 3px solid var(--color-danger);
    }
    .error-items {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    .error-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 12px;
      border-radius: var(--radius-sm);
      background: rgba(239, 68, 68, 0.05);
      border: 1px solid rgba(239, 68, 68, 0.15);
      font-size: 13px;
      color: var(--text-primary);
    }
  }

  .no-errors {
    display: flex;
    align-items: center;
    gap: 8px;
    justify-content: center;
    padding: 20px;
    color: var(--color-success);
    font-size: 14px;
  }
}

// ElCard样式覆盖
:deep(.el-card) {
  background: var(--bg-card);
  border-color: var(--border-color);
}
</style>
