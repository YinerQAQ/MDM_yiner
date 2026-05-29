<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import {
  ElTable,
  ElTableColumn,
  ElButton,
  ElInput,
  ElDialog,
  ElForm,
  ElFormItem,
  ElSelect,
  ElOption,
  ElTabs,
  ElTabPane,
  ElTag,
  ElMessage,
  ElMessageBox
} from 'element-plus'

interface ReceiveConfig {
  id: string
  name: string
  code: string
  sourceSystem: string
  modelName: string
  receiveType: '推送' | '拉取'
  schedule: string
  format: string
  status: '启用' | '停用'
  lastReceiveTime: string
  successCount: number
  failCount: number
}

interface ReceiveLog {
  id: string
  configId: string
  configName: string
  startTime: string
  duration: string
  count: number
  status: '成功' | '失败' | '部分成功'
  errorMsg?: string
}

const search = ref('')
const list = ref<ReceiveConfig[]>([])
const logs = ref<ReceiveLog[]>([])
const tab = ref('list')

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive<ReceiveConfig>({
  id: '',
  name: '',
  code: '',
  sourceSystem: '',
  modelName: '',
  receiveType: '推送',
  schedule: '',
  format: 'JSON',
  status: '停用',
  lastReceiveTime: '-',
  successCount: 0,
  failCount: 0
})

const buildMockList = (): ReceiveConfig[] => [
  {
    id: 'RCV-001', name: 'ERP产品主数据接收', code: 'rcv_erp_product',
    sourceSystem: 'ERP系统', modelName: '产品主数据',
    receiveType: '推送', schedule: '实时', format: 'JSON',
    status: '启用', lastReceiveTime: '2026-05-29 09:12:45',
    successCount: 1248, failCount: 3
  },
  {
    id: 'RCV-002', name: 'CRM客户信息同步', code: 'rcv_crm_customer',
    sourceSystem: 'CRM系统', modelName: '客户主数据',
    receiveType: '拉取', schedule: '每5分钟', format: 'JSON',
    status: '启用', lastReceiveTime: '2026-05-29 09:15:00',
    successCount: 892, failCount: 1
  },
  {
    id: 'RCV-003', name: 'SRM供应商对接', code: 'rcv_srm_supplier',
    sourceSystem: 'SRM系统', modelName: '供应商主数据',
    receiveType: '拉取', schedule: '每小时', format: 'XML',
    status: '启用', lastReceiveTime: '2026-05-29 08:00:00',
    successCount: 305, failCount: 0
  },
  {
    id: 'RCV-004', name: 'HR组织架构同步', code: 'rcv_hr_org',
    sourceSystem: 'HR系统', modelName: '组织主数据',
    receiveType: '推送', schedule: '实时', format: 'JSON',
    status: '停用', lastReceiveTime: '2026-05-15 17:43:22',
    successCount: 56, failCount: 12
  },
  {
    id: 'RCV-005', name: 'MES物料编码下发接收', code: 'rcv_mes_material',
    sourceSystem: 'MES系统', modelName: '物料主数据',
    receiveType: '拉取', schedule: '每天 02:00', format: 'CSV',
    status: '启用', lastReceiveTime: '2026-05-29 02:00:18',
    successCount: 2173, failCount: 5
  }
]

const buildMockLogs = (): ReceiveLog[] => [
  { id: 'L01', configId: 'RCV-001', configName: 'ERP产品主数据接收', startTime: '2026-05-29 09:12:45', duration: '0.42s', count: 18, status: '成功' },
  { id: 'L02', configId: 'RCV-002', configName: 'CRM客户信息同步', startTime: '2026-05-29 09:15:00', duration: '1.18s', count: 7, status: '成功' },
  { id: 'L03', configId: 'RCV-005', configName: 'MES物料编码下发接收', startTime: '2026-05-29 02:00:18', duration: '12.5s', count: 348, status: '部分成功', errorMsg: '5条记录因主键重复被忽略' },
  { id: 'L04', configId: 'RCV-001', configName: 'ERP产品主数据接收', startTime: '2026-05-29 08:55:11', duration: '0.31s', count: 4, status: '成功' },
  { id: 'L05', configId: 'RCV-003', configName: 'SRM供应商对接', startTime: '2026-05-29 08:00:00', duration: '2.21s', count: 12, status: '成功' },
  { id: 'L06', configId: 'RCV-004', configName: 'HR组织架构同步', startTime: '2026-05-15 17:43:22', duration: '-', count: 0, status: '失败', errorMsg: '连接超时：HR系统无响应' },
  { id: 'L07', configId: 'RCV-002', configName: 'CRM客户信息同步', startTime: '2026-05-29 09:10:00', duration: '1.05s', count: 6, status: '成功' },
  { id: 'L08', configId: 'RCV-005', configName: 'MES物料编码下发接收', startTime: '2026-05-28 02:00:11', duration: '11.8s', count: 412, status: '成功' }
]

const loadAll = () => {
  list.value = buildMockList()
  logs.value = buildMockLogs()
}

const filtered = computed(() =>
  list.value.filter(it =>
    !search.value ||
    it.name.includes(search.value) ||
    it.sourceSystem.includes(search.value) ||
    it.code.includes(search.value)
  )
)

const openDialog = (row?: ReceiveConfig) => {
  if (row) {
    isEdit.value = true
    Object.assign(form, row)
  } else {
    isEdit.value = false
    Object.assign(form, {
      id: '', name: '', code: '', sourceSystem: '', modelName: '',
      receiveType: '推送', schedule: '', format: 'JSON',
      status: '停用', lastReceiveTime: '-', successCount: 0, failCount: 0
    })
  }
  dialogVisible.value = true
}

const save = () => {
  if (!form.name || !form.code) {
    ElMessage.error('请填写名称和编码')
    return
  }
  if (isEdit.value) {
    const idx = list.value.findIndex(i => i.id === form.id)
    if (idx > -1) list.value[idx] = { ...form }
    ElMessage.success('已更新')
  } else {
    list.value.unshift({ ...form, id: `RCV-${Date.now().toString().slice(-4)}` })
    ElMessage.success('已创建')
  }
  dialogVisible.value = false
}

const toggleStatus = (row: ReceiveConfig) => {
  row.status = row.status === '启用' ? '停用' : '启用'
  ElMessage.success(`已${row.status}`)
}

const triggerReceive = async (row: ReceiveConfig) => {
  try {
    await ElMessageBox.confirm(`确认手动触发接收【${row.name}】？`, '手动触发', { type: 'info' })
    const fakeCount = Math.floor(Math.random() * 50) + 1
    row.successCount += fakeCount
    row.lastReceiveTime = new Date().toISOString().replace('T', ' ').slice(0, 19)
    logs.value.unshift({
      id: `L${Date.now().toString().slice(-6)}`,
      configId: row.id,
      configName: row.name,
      startTime: row.lastReceiveTime,
      duration: `${(Math.random() * 2).toFixed(2)}s`,
      count: fakeCount,
      status: '成功'
    })
    ElMessage.success(`触发成功，新增 ${fakeCount} 条`)
  } catch {
    /* cancelled */
  }
}

const remove = async (row: ReceiveConfig) => {
  try {
    await ElMessageBox.confirm(`确认删除接收配置【${row.name}】？`, '提示', { type: 'warning' })
    list.value = list.value.filter(i => i.id !== row.id)
    ElMessage.success('已删除')
  } catch { /* cancelled */ }
}

const logStatusClass = (s: string) => {
  if (s === '成功') return 'status-active'
  if (s === '失败') return 'status-inactive'
  return 'status-pending'
}

onMounted(loadAll)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">数据接收</h2>
        <p class="page-desc">配置和管理来自各业务系统的主数据接收接口</p>
      </div>
      <ElButton type="primary" @click="openDialog()">新增接收配置</ElButton>
    </div>

    <ElTabs v-model="tab" class="rcv-tabs">
      <ElTabPane label="接收配置" name="list">
        <div class="search-card">
          <div style="display:flex;gap:16px;align-items:center;flex-wrap:wrap;">
            <ElInput v-model="search" placeholder="搜索接口名称 / 编码 / 来源系统" clearable style="width:320px;" />
          </div>
        </div>

        <div class="table-card">
          <ElTable :data="filtered" stripe>
            <ElTableColumn prop="name" label="接口名称" min-width="180" />
            <ElTableColumn prop="code" label="编码" width="180" />
            <ElTableColumn prop="sourceSystem" label="来源系统" width="120" />
            <ElTableColumn prop="modelName" label="数据模型" width="140" />
            <ElTableColumn prop="receiveType" label="接收方式" width="100">
              <template #default="scope">
                <ElTag size="small" :type="scope.row.receiveType === '推送' ? 'success' : 'warning'">
                  {{ scope.row.receiveType }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="schedule" label="调度" width="110" />
            <ElTableColumn prop="format" label="格式" width="80" />
            <ElTableColumn prop="status" label="状态" width="80">
              <template #default="scope">
                <span :class="scope.row.status === '启用' ? 'status-active' : 'status-inactive'">
                  {{ scope.row.status }}
                </span>
              </template>
            </ElTableColumn>
            <ElTableColumn label="最近接收 / 统计" width="220">
              <template #default="scope">
                <div style="font-size:12px;color:var(--text-secondary);">
                  {{ scope.row.lastReceiveTime }}
                </div>
                <div style="font-size:12px;color:var(--text-muted);margin-top:2px;">
                  成功 <span style="color:var(--color-success)">{{ scope.row.successCount }}</span>
                  / 失败 <span style="color:var(--color-danger)">{{ scope.row.failCount }}</span>
                </div>
              </template>
            </ElTableColumn>
            <ElTableColumn label="操作" width="280" fixed="right">
              <template #default="scope">
                <ElButton link size="small" type="primary" @click="openDialog(scope.row)">编辑</ElButton>
                <ElButton
                  link size="small"
                  :type="scope.row.status === '启用' ? 'warning' : 'success'"
                  @click="toggleStatus(scope.row)"
                >{{ scope.row.status === '启用' ? '停用' : '启用' }}</ElButton>
                <ElButton link size="small" type="primary" @click="triggerReceive(scope.row)">手动触发</ElButton>
                <ElButton link size="small" type="danger" @click="remove(scope.row)">删除</ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
        </div>
      </ElTabPane>

      <ElTabPane label="接收日志" name="logs">
        <div class="table-card">
          <ElTable :data="logs" stripe>
            <ElTableColumn prop="startTime" label="开始时间" width="170" />
            <ElTableColumn prop="configName" label="接口" min-width="180" />
            <ElTableColumn prop="duration" label="耗时" width="100" />
            <ElTableColumn prop="count" label="记录数" width="100" />
            <ElTableColumn prop="status" label="状态" width="110">
              <template #default="scope">
                <span :class="logStatusClass(scope.row.status)">{{ scope.row.status }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="errorMsg" label="备注 / 错误" min-width="240">
              <template #default="scope">
                <span style="color:var(--text-muted)">{{ scope.row.errorMsg || '-' }}</span>
              </template>
            </ElTableColumn>
          </ElTable>
        </div>
      </ElTabPane>
    </ElTabs>

    <!-- 新增/编辑 -->
    <ElDialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑接收配置' : '新增接收配置'"
      width="560px"
    >
      <ElForm :model="form" label-width="100px">
        <ElFormItem label="接口名称" required>
          <ElInput v-model="form.name" />
        </ElFormItem>
        <ElFormItem label="接口编码" required>
          <ElInput v-model="form.code" :disabled="isEdit" />
        </ElFormItem>
        <ElFormItem label="来源系统">
          <ElInput v-model="form.sourceSystem" placeholder="如 ERP / CRM / MES" />
        </ElFormItem>
        <ElFormItem label="数据模型">
          <ElInput v-model="form.modelName" placeholder="绑定的数据模型" />
        </ElFormItem>
        <ElFormItem label="接收方式">
          <ElSelect v-model="form.receiveType" style="width:100%">
            <ElOption label="推送（被动）" value="推送" />
            <ElOption label="拉取（主动）" value="拉取" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="调度策略">
          <ElInput v-model="form.schedule" placeholder="如 实时 / 每5分钟 / 每天 02:00" />
        </ElFormItem>
        <ElFormItem label="数据格式">
          <ElSelect v-model="form.format" style="width:100%">
            <ElOption label="JSON" value="JSON" />
            <ElOption label="XML" value="XML" />
            <ElOption label="CSV" value="CSV" />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="save">保存</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped lang="scss">
.rcv-tabs :deep(.el-tabs__item) {
  color: var(--text-secondary);
  &.is-active { color: var(--color-primary); }
}
.rcv-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--color-primary);
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
