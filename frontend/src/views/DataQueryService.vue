<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import {
  ElTable,
  ElTableColumn,
  ElButton,
  ElInput,
  ElSelect,
  ElOption,
  ElDialog,
  ElForm,
  ElFormItem,
  ElTag,
  ElDescriptions,
  ElDescriptionsItem,
  ElMessage,
  ElMessageBox
} from 'element-plus'

interface QueryService {
  id: string
  name: string
  code: string
  modelName: string
  apiPath: string
  method: 'GET' | 'POST'
  authType: 'Token' | '证书' | '无'
  status: '启用' | '停用'
  callCount: number
  avgRt: number
  description: string
  createTime: string
}

interface CallLog {
  id: string
  serviceId: string
  serviceName: string
  caller: string
  callTime: string
  rt: number
  status: 200 | 401 | 500
}

const search = ref('')
const list = ref<QueryService[]>([])
const callLogs = ref<CallLog[]>([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive<QueryService>({
  id: '', name: '', code: '', modelName: '',
  apiPath: '', method: 'GET',
  authType: 'Token', status: '停用',
  callCount: 0, avgRt: 0, description: '', createTime: ''
})

const logVisible = ref(false)
const docVisible = ref(false)
const currentService = ref<QueryService | null>(null)

const buildMock = (): QueryService[] => [
  {
    id: 'QS-001', name: '产品查询服务', code: 'qs_product',
    modelName: '产品主数据', apiPath: '/api/v1/query/product', method: 'GET',
    authType: 'Token', status: '启用',
    callCount: 124853, avgRt: 78,
    description: '按编码 / 类别查询产品主数据', createTime: '2025-12-10 10:32:01'
  },
  {
    id: 'QS-002', name: '客户信息查询', code: 'qs_customer',
    modelName: '客户主数据', apiPath: '/api/v1/query/customer', method: 'GET',
    authType: 'Token', status: '启用',
    callCount: 88112, avgRt: 65,
    description: '提供给营销系统的客户基础信息查询', createTime: '2026-01-08 14:50:21'
  },
  {
    id: 'QS-003', name: '供应商批量查询', code: 'qs_supplier_batch',
    modelName: '供应商主数据', apiPath: '/api/v1/query/supplier/batch', method: 'POST',
    authType: '证书', status: '启用',
    callCount: 9203, avgRt: 142,
    description: '采购系统批量获取已认证供应商', createTime: '2026-02-15 09:20:11'
  },
  {
    id: 'QS-004', name: '组织树查询', code: 'qs_org_tree',
    modelName: '组织主数据', apiPath: '/api/v1/query/org/tree', method: 'GET',
    authType: 'Token', status: '启用',
    callCount: 50321, avgRt: 92,
    description: '获取组织层级树结构', createTime: '2026-03-01 16:10:45'
  },
  {
    id: 'QS-005', name: '物料字典查询', code: 'qs_material_dict',
    modelName: '物料主数据', apiPath: '/api/v1/query/material/dict', method: 'GET',
    authType: '无', status: '停用',
    callCount: 1280, avgRt: 35,
    description: '内网公开的物料字典服务', createTime: '2026-04-22 11:11:11'
  }
]

const buildMockCalls = (svc: QueryService): CallLog[] => [
  { id: 'C01', serviceId: svc.id, serviceName: svc.name, caller: 'ERP系统', callTime: '2026-05-29 09:21:22', rt: 78, status: 200 },
  { id: 'C02', serviceId: svc.id, serviceName: svc.name, caller: 'CRM系统', callTime: '2026-05-29 09:18:14', rt: 65, status: 200 },
  { id: 'C03', serviceId: svc.id, serviceName: svc.name, caller: 'BI报表', callTime: '2026-05-29 09:11:00', rt: 121, status: 200 },
  { id: 'C04', serviceId: svc.id, serviceName: svc.name, caller: '未知系统', callTime: '2026-05-29 08:55:43', rt: 12, status: 401 },
  { id: 'C05', serviceId: svc.id, serviceName: svc.name, caller: '门店POS', callTime: '2026-05-29 08:42:11', rt: 88, status: 200 },
  { id: 'C06', serviceId: svc.id, serviceName: svc.name, caller: '采购系统', callTime: '2026-05-29 08:30:00', rt: 530, status: 500 }
]

const loadAll = () => { list.value = buildMock() }

const filtered = computed(() =>
  list.value.filter(it =>
    !search.value ||
    it.name.includes(search.value) ||
    it.code.includes(search.value) ||
    it.apiPath.includes(search.value)
  )
)

const openDialog = (row?: QueryService) => {
  if (row) {
    isEdit.value = true
    Object.assign(form, row)
  } else {
    isEdit.value = false
    Object.assign(form, {
      id: '', name: '', code: '', modelName: '',
      apiPath: '', method: 'GET',
      authType: 'Token', status: '停用',
      callCount: 0, avgRt: 0, description: '',
      createTime: new Date().toISOString().replace('T', ' ').slice(0, 19)
    })
  }
  dialogVisible.value = true
}

const save = () => {
  if (!form.name || !form.code || !form.apiPath) {
    ElMessage.error('请填写名称、编码、接口路径')
    return
  }
  if (isEdit.value) {
    const idx = list.value.findIndex(i => i.id === form.id)
    if (idx > -1) list.value[idx] = { ...form }
    ElMessage.success('已更新')
  } else {
    list.value.unshift({ ...form, id: `QS-${Date.now().toString().slice(-4)}` })
    ElMessage.success('已创建')
  }
  dialogVisible.value = false
}

const toggleStatus = (row: QueryService) => {
  row.status = row.status === '启用' ? '停用' : '启用'
  ElMessage.success(`已${row.status}`)
}

const remove = async (row: QueryService) => {
  try {
    await ElMessageBox.confirm(`确认删除查询服务【${row.name}】？`, '提示', { type: 'warning' })
    list.value = list.value.filter(i => i.id !== row.id)
    ElMessage.success('已删除')
  } catch { /* cancelled */ }
}

const openLog = (row: QueryService) => {
  currentService.value = row
  callLogs.value = buildMockCalls(row)
  logVisible.value = true
}

const openDoc = (row: QueryService) => {
  currentService.value = row
  docVisible.value = true
}

const callStatusClass = (s: number) => {
  if (s === 200) return 'status-active'
  if (s === 401) return 'status-pending'
  return 'status-inactive'
}

const apiDoc = computed(() => {
  if (!currentService.value) return ''
  const s = currentService.value
  return `# ${s.name}

- 接口路径: ${s.apiPath}
- 请求方式: ${s.method}
- 认证方式: ${s.authType}
- 绑定模型: ${s.modelName}

## 请求示例
\`\`\`bash
curl -X ${s.method} "https://mdm.example.com${s.apiPath}?code=XXX" \\
  -H "Authorization: Bearer <token>"
\`\`\`

## 返回示例
\`\`\`json
{
  "code": 200,
  "data": [],
  "message": "ok"
}
\`\`\`
`
})

onMounted(loadAll)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">数据查询服务</h2>
        <p class="page-desc">对外提供主数据查询能力，统一鉴权与监控</p>
      </div>
      <ElButton type="primary" @click="openDialog()">新建服务</ElButton>
    </div>

    <div class="search-card">
      <ElInput v-model="search" placeholder="搜索服务名称 / 编码 / 路径" clearable style="width:340px;" />
    </div>

    <div class="table-card">
      <ElTable :data="filtered" stripe>
        <ElTableColumn prop="name" label="服务名称" min-width="160" />
        <ElTableColumn prop="code" label="编码" width="160" />
        <ElTableColumn prop="modelName" label="绑定模型" width="140" />
        <ElTableColumn label="接口路径" min-width="220">
          <template #default="scope">
            <span style="font-family:var(--font-mono);font-size:12px;color:var(--color-primary)">
              {{ scope.row.method }} {{ scope.row.apiPath }}
            </span>
          </template>
        </ElTableColumn>
        <ElTableColumn label="认证" width="90">
          <template #default="scope">
            <ElTag size="small" :type="scope.row.authType === '证书' ? 'warning' : (scope.row.authType === '无' ? 'info' : 'success')">
              {{ scope.row.authType }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="状态" width="80">
          <template #default="scope">
            <span :class="scope.row.status === '启用' ? 'status-active' : 'status-inactive'">
              {{ scope.row.status }}
            </span>
          </template>
        </ElTableColumn>
        <ElTableColumn label="调用统计" width="180">
          <template #default="scope">
            <div style="font-size:12px;">
              累计 <span style="color:var(--color-primary)">{{ scope.row.callCount.toLocaleString() }}</span> 次
            </div>
            <div style="font-size:12px;color:var(--text-muted);margin-top:2px;">
              平均 RT {{ scope.row.avgRt }}ms
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="320" fixed="right">
          <template #default="scope">
            <ElButton link size="small" type="primary" @click="openDialog(scope.row)">编辑</ElButton>
            <ElButton
              link size="small"
              :type="scope.row.status === '启用' ? 'warning' : 'success'"
              @click="toggleStatus(scope.row)"
            >{{ scope.row.status === '启用' ? '停用' : '启用' }}</ElButton>
            <ElButton link size="small" type="primary" @click="openLog(scope.row)">调用日志</ElButton>
            <ElButton link size="small" type="primary" @click="openDoc(scope.row)">生成文档</ElButton>
            <ElButton link size="small" type="danger" @click="remove(scope.row)">删除</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </div>

    <!-- 新增/编辑 -->
    <ElDialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑查询服务' : '新建查询服务'"
      width="560px"
    >
      <ElForm :model="form" label-width="100px">
        <ElFormItem label="服务名称" required>
          <ElInput v-model="form.name" />
        </ElFormItem>
        <ElFormItem label="服务编码" required>
          <ElInput v-model="form.code" :disabled="isEdit" />
        </ElFormItem>
        <ElFormItem label="绑定模型">
          <ElInput v-model="form.modelName" placeholder="如 产品主数据" />
        </ElFormItem>
        <ElFormItem label="请求方式">
          <ElSelect v-model="form.method" style="width:100%">
            <ElOption label="GET" value="GET" />
            <ElOption label="POST" value="POST" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="接口路径" required>
          <ElInput v-model="form.apiPath" placeholder="/api/v1/query/xxx" />
        </ElFormItem>
        <ElFormItem label="认证方式">
          <ElSelect v-model="form.authType" style="width:100%">
            <ElOption label="Token" value="Token" />
            <ElOption label="证书" value="证书" />
            <ElOption label="无" value="无" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="描述">
          <ElInput v-model="form.description" type="textarea" :rows="3" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="save">保存</ElButton>
      </template>
    </ElDialog>

    <!-- 调用日志 -->
    <ElDialog
      v-model="logVisible"
      :title="`调用日志 - ${currentService?.name || ''}`"
      width="780px"
    >
      <ElTable :data="callLogs" stripe>
        <ElTableColumn prop="callTime" label="时间" width="170" />
        <ElTableColumn prop="caller" label="调用方" width="140" />
        <ElTableColumn prop="rt" label="耗时(ms)" width="110">
          <template #default="scope">
            <span :style="{ color: scope.row.rt > 200 ? 'var(--color-danger)' : 'var(--text-primary)' }">
              {{ scope.row.rt }}
            </span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="status" label="状态" width="120">
          <template #default="scope">
            <span :class="callStatusClass(scope.row.status)">{{ scope.row.status }}</span>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElDialog>

    <!-- 接口文档 -->
    <ElDialog
      v-model="docVisible"
      :title="`接口文档 - ${currentService?.name || ''}`"
      width="720px"
    >
      <ElDescriptions v-if="currentService" :column="2" border style="margin-bottom:16px;">
        <ElDescriptionsItem label="服务名称">{{ currentService.name }}</ElDescriptionsItem>
        <ElDescriptionsItem label="编码">{{ currentService.code }}</ElDescriptionsItem>
        <ElDescriptionsItem label="路径">{{ currentService.method }} {{ currentService.apiPath }}</ElDescriptionsItem>
        <ElDescriptionsItem label="认证">{{ currentService.authType }}</ElDescriptionsItem>
      </ElDescriptions>
      <pre class="api-doc-pre">{{ apiDoc }}</pre>
    </ElDialog>
  </div>
</template>

<style scoped lang="scss">
.api-doc-pre {
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  padding: 16px;
  color: var(--text-primary);
  font-family: var(--font-mono);
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 400px;
  overflow: auto;
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
