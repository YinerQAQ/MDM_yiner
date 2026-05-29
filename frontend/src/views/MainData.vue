<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
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
  ElTabs,
  ElTabPane,
  ElMessage,
  ElDrawer,
  ElIcon,
  ElTooltip
} from 'element-plus'
import type { ElTable as ElTableInstance } from 'element-plus'
import {
  Search, Refresh, Edit, Delete, Check, Close,
  Upload, Download, EditPen, Timer, View,
  ArrowRight, Document, Collection, Position
} from '@element-plus/icons-vue'

import type { MdmMainData, MdmDataModel, MdmModelAttribute } from '../api/types'
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
  createVersion,
  batchEditMainData,
  getModelAttributes,
  getVersionHistory
} from '../api/mainData'
import ImportDialog from '../components/ImportDialog.vue'
import ExportDialog from '../components/ExportDialog.vue'

const models = ref<MdmDataModel[]>([])
const selectedModelId = ref('')
const mainDataList = ref<MdmMainData[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const activeTab = ref('all')

// 搜索面板
const searchExpanded = ref(true)
const searchForm = ref({
  keyword: '',
  status: '',
  dateRange: [] as string[]
})

// 导入/导出/批量编辑
const importDialogVisible = ref(false)
const exportDialogVisible = ref(false)
const batchEditDialogVisible = ref(false)
const selectedRows = ref<MdmMainData[]>([])
const batchEditFields = ref<Record<string, string>>({})
const batchEditLoading = ref(false)
const currentModelAttributes = ref<MdmModelAttribute[]>([])
const mainTableRef = ref<InstanceType<typeof ElTableInstance> | null>(null)

// loading 状态
const loadingModels = ref(false)
const loadingData = ref(false)

// 详情抽屉
const drawerVisible = ref(false)
const drawerData = ref<MdmMainData | null>(null)
const versionHistory = ref<any[]>([])

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
  loadingModels.value = true
  try {
    const response = await getAllModels()
    const allModels = response.data.data || []
    // 优先显示已发布模型，如果没有则显示全部（兜底）
    const published = allModels.filter(m => m.status === '已发布')
    models.value = published.length > 0 ? published : allModels
    if (models.value.length > 0) {
      selectedModelId.value = models.value[0].id
      await loadMainData()
    }
  } catch (error) {
    models.value = []
    ElMessage.error('加载模型失败，请检查网络或后端服务')
  } finally {
    loadingModels.value = false
  }
}

const loadMainData = async () => {
  if (!selectedModelId.value) return
  loadingData.value = true
  try {
    const response = await getMainDataByModelId(selectedModelId.value)
    mainDataList.value = response.data.data || []
  } catch (error) {
    mainDataList.value = []
    ElMessage.error('加载主数据失败')
  } finally {
    loadingData.value = false
  }
}

// 统计数据
const statusCounts = computed(() => {
  const list = mainDataList.value
  return {
    total: list.length,
    draft: list.filter(d => d.dataStatus === '暂存').length,
    pending: list.filter(d => d.dataStatus === '审核中').length,
    approved: list.filter(d => d.dataStatus === '审核通过').length,
    archived: list.filter(d => d.dataStatus === '已归档').length
  }
})

// 搜索过滤
const filteredData = computed(() => {
  let result = mainDataList.value

  // 标签过滤
  if (activeTab.value !== 'all') {
    const statusMap: Record<string, string> = {
      draft: '暂存',
      pending: '审核中',
      approved: '审核通过',
      archived: '已归档'
    }
    result = result.filter(d => d.dataStatus === statusMap[activeTab.value])
  }

  // 关键词过滤
  if (searchForm.value.keyword) {
    const kw = searchForm.value.keyword.toLowerCase()
    result = result.filter(d =>
      d.code?.toLowerCase().includes(kw) ||
      d.createdByName?.toLowerCase().includes(kw)
    )
  }

  // 状态过滤
  if (searchForm.value.status) {
    result = result.filter(d => d.dataStatus === searchForm.value.status)
  }

  return result
})

const handleSearch = () => {
  // computed 会自动触发
}

const resetSearch = () => {
  searchForm.value = { keyword: '', status: '', dateRange: [] }
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

// 详情抽屉
const openDrawer = async (row: MdmMainData) => {
  drawerData.value = { ...row }
  drawerVisible.value = true
  try {
    const res = await getVersionHistory(row.id)
    versionHistory.value = res.data.data || []
  } catch {
    versionHistory.value = []
  }
}

const getStatusClass = (status: string): string => {
  switch (status) {
    case '审核通过': return 'status-approved'
    case '审核中': return 'status-pending'
    case '暂存': return 'status-draft'
    case '审核拒绝': return 'status-rejected'
    case '已归档': return 'status-archived'
    default: return ''
  }
}

const getStatusIcon = (status: string) => {
  switch (status) {
    case '审核通过': return Check
    case '审核中': return Timer
    case '暂存': return Edit
    case '审核拒绝': return Close
    case '已归档': return Collection
    default: return Document
  }
}

// 表格多选
const handleSelectionChange = (selection: MdmMainData[]) => {
  selectedRows.value = selection
}

// 打开批量编辑对话框
const openBatchEdit = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要批量编辑的数据')
    return
  }
  try {
    const response = await getModelAttributes(selectedModelId.value)
    currentModelAttributes.value = response.data.data || []
  } catch (error) {
    ElMessage.error('加载属性列表失败')
    return
  }
  batchEditFields.value = {}
  batchEditDialogVisible.value = true
}

// 执行批量编辑
const handleBatchEdit = async () => {
  const fieldsWithValue = Object.fromEntries(
    Object.entries(batchEditFields.value).filter(([_, v]) => v !== '' && v !== undefined)
  )
  if (Object.keys(fieldsWithValue).length === 0) {
    ElMessage.warning('请至少填写一个要修改的字段')
    return
  }
  batchEditLoading.value = true
  try {
    const ids = selectedRows.value.map(r => r.id)
    await batchEditMainData(ids, fieldsWithValue)
    ElMessage.success('批量编辑成功')
    batchEditDialogVisible.value = false
    loadMainData()
  } catch (error) {
    ElMessage.error('批量编辑失败')
  } finally {
    batchEditLoading.value = false
  }
}

const formatJsonData = (json?: string) => {
  if (!json) return {}
  try {
    return JSON.parse(json)
  } catch {
    return {}
  }
}

onMounted(() => {
  console.log('[MainData] component mounted, loading models...')
  loadModels()
})
</script>

<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">主数据管理</h2>
        <p class="page-desc">管理主数据的全生命周期</p>
      </div>
      <div style="display: flex; align-items: center; gap: 12px;">
        <ElSelect v-model="selectedModelId" @change="loadMainData" style="width: 200px;" placeholder="选择模型" :loading="loadingModels">
          <ElOption v-for="model in models" :key="model.id" :label="model.modelName" :value="model.id" />
        </ElSelect>
        <ElButton v-auth="'btn:mainData:add'" type="primary" @click="openDialog()" :disabled="!selectedModelId">
          <ElIcon style="margin-right: 4px;"><EditPen /></ElIcon>新增数据
        </ElButton>
        <ElButton type="success" @click="importDialogVisible = true" :disabled="!selectedModelId">
          <ElIcon style="margin-right: 4px;"><Upload /></ElIcon>导入
        </ElButton>
        <ElButton type="warning" @click="exportDialogVisible = true" :disabled="!selectedModelId">
          <ElIcon style="margin-right: 4px;"><Download /></ElIcon>导出
        </ElButton>
        <ElButton type="info" @click="openBatchEdit" :disabled="selectedRows.length === 0">
          <ElIcon style="margin-right: 4px;"><Edit /></ElIcon>批量编辑 ({{ selectedRows.length }})
        </ElButton>
      </div>
    </div>

    <!-- 高级搜索面板 -->
    <div class="search-card">
      <div class="search-header" @click="searchExpanded = !searchExpanded">
        <span class="search-title">
          <ElIcon><Search /></ElIcon>
          高级搜索
        </span>
        <ElIcon :class="{ 'expanded': searchExpanded }"><ArrowRight /></ElIcon>
      </div>
      <div v-show="searchExpanded" class="search-body">
        <ElForm inline>
          <ElFormItem label="关键词">
            <ElInput v-model="searchForm.keyword" placeholder="编码/创建人" clearable style="width: 200px;" @keyup.enter="handleSearch" />
          </ElFormItem>
          <ElFormItem label="状态">
            <ElSelect v-model="searchForm.status" placeholder="全部状态" clearable style="width: 150px;">
              <ElOption label="暂存" value="暂存" />
              <ElOption label="审核中" value="审核中" />
              <ElOption label="审核通过" value="审核通过" />
              <ElOption label="审核拒绝" value="审核拒绝" />
              <ElOption label="已归档" value="已归档" />
            </ElSelect>
          </ElFormItem>
          <ElFormItem>
            <ElButton type="primary" @click="handleSearch">
              <ElIcon style="margin-right: 4px;"><Search /></ElIcon>搜索
            </ElButton>
            <ElButton @click="resetSearch">
              <ElIcon style="margin-right: 4px;"><Refresh /></ElIcon>重置
            </ElButton>
          </ElFormItem>
        </ElForm>
      </div>
    </div>

    <!-- 统计条 -->
    <div class="status-bar">
      <div class="status-bar-item">
        <span class="status-bar-label">总数</span>
        <span class="status-bar-value total">{{ statusCounts.total }}</span>
      </div>
      <div class="status-bar-divider"></div>
      <div class="status-bar-item" @click="activeTab = 'draft'" :class="{ active: activeTab === 'draft' }">
        <span class="status-bar-dot draft"></span>
        <span class="status-bar-label">暂存</span>
        <span class="status-bar-value">{{ statusCounts.draft }}</span>
      </div>
      <div class="status-bar-item" @click="activeTab = 'pending'" :class="{ active: activeTab === 'pending' }">
        <span class="status-bar-dot pending"></span>
        <span class="status-bar-label">审核中</span>
        <span class="status-bar-value">{{ statusCounts.pending }}</span>
      </div>
      <div class="status-bar-item" @click="activeTab = 'approved'" :class="{ active: activeTab === 'approved' }">
        <span class="status-bar-dot approved"></span>
        <span class="status-bar-label">审核通过</span>
        <span class="status-bar-value">{{ statusCounts.approved }}</span>
      </div>
      <div class="status-bar-item" @click="activeTab = 'archived'" :class="{ active: activeTab === 'archived' }">
        <span class="status-bar-dot archived"></span>
        <span class="status-bar-label">已归档</span>
        <span class="status-bar-value">{{ statusCounts.archived }}</span>
      </div>
    </div>

    <!-- 无模型提示 -->
    <div v-if="!loadingModels && models.length === 0" class="table-card" style="text-align: center; padding: 60px 20px;">
      <ElIcon :size="48" style="color: var(--text-muted);"><Document /></ElIcon>
      <p style="margin-top: 16px; color: var(--text-secondary); font-size: 14px;">暂无可用数据模型</p>
      <p style="margin-top: 8px; color: var(--text-muted); font-size: 12px;">请先在「数据模型管理」中创建并发布模型</p>
    </div>

    <!-- 标签页+表格 -->
    <div v-else class="table-card">
      <ElTabs v-model="activeTab" type="card">
        <ElTabPane label="全部" name="all">
          <ElTable
            ref="mainTableRef"
            v-loading="loadingData"
            :data="filteredData"
            stripe
            @selection-change="handleSelectionChange"
            row-class-name="data-table-row"
            @row-click="(row: MdmMainData) => openDrawer(row)"
            style="cursor: pointer;"
          >
            <template #empty>
              <div style="padding: 40px 0;">
                <ElIcon :size="40" style="color: var(--text-muted);"><Document /></ElIcon>
                <p style="margin-top: 12px; color: var(--text-secondary);">暂无数据</p>
              </div>
            </template>
            <ElTableColumn type="selection" width="45" />
            <ElTableColumn prop="code" label="数据编码" min-width="140">
              <template #default="scope">
                <span class="cell-code">{{ scope.row.code }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="dataStatus" label="状态" width="130">
              <template #default="scope">
                <span class="enhanced-status" :class="getStatusClass(scope.row.dataStatus)">
                  <ElIcon :size="12"><component :is="getStatusIcon(scope.row.dataStatus)" /></ElIcon>
                  {{ scope.row.dataStatus }}
                </span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="version" label="版本" width="80">
              <template #default="scope">
                <span class="cell-version">V{{ scope.row.version }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="createdByName" label="创建人" width="120" />
            <ElTableColumn prop="createTime" label="创建时间" width="170" />
            <ElTableColumn label="操作" width="200" fixed="right">
              <template #default="scope">
                <div class="action-btns" @click.stop>
                  <ElTooltip content="编辑" placement="top">
                    <ElButton v-auth="'btn:mainData:edit'" v-if="scope.row.dataStatus === '暂存'" type="primary" size="small" circle @click="openDialog(scope.row)">
                      <ElIcon><Edit /></ElIcon>
                    </ElButton>
                  </ElTooltip>
                  <ElTooltip content="提交审核" placement="top">
                    <ElButton v-auth="'btn:mainData:submit'" v-if="scope.row.dataStatus === '暂存'" type="success" size="small" circle @click="handleSubmit(scope.row.id)">
                      <ElIcon><Position /></ElIcon>
                    </ElButton>
                  </ElTooltip>
                  <ElTooltip content="删除" placement="top">
                    <ElButton v-auth="'btn:mainData:delete'" v-if="scope.row.dataStatus === '暂存'" type="danger" size="small" circle @click="handleDelete(scope.row.id)">
                      <ElIcon><Delete /></ElIcon>
                    </ElButton>
                  </ElTooltip>
                  <ElTooltip content="通过" placement="top">
                    <ElButton v-if="scope.row.dataStatus === '审核中'" type="success" size="small" circle @click="handleApprove(scope.row.id)">
                      <ElIcon><Check /></ElIcon>
                    </ElButton>
                  </ElTooltip>
                  <ElTooltip content="拒绝" placement="top">
                    <ElButton v-if="scope.row.dataStatus === '审核中'" type="danger" size="small" circle @click="handleReject(scope.row.id)">
                      <ElIcon><Close /></ElIcon>
                    </ElButton>
                  </ElTooltip>
                  <ElTooltip content="发起变更" placement="top">
                    <ElButton v-if="scope.row.dataStatus === '审核通过'" type="primary" size="small" circle @click="handleCreateVersion(scope.row.id)">
                      <ElIcon><EditPen /></ElIcon>
                    </ElButton>
                  </ElTooltip>
                  <ElTooltip content="归档" placement="top">
                    <ElButton v-if="scope.row.dataStatus === '审核通过'" type="warning" size="small" circle @click="handleArchive(scope.row.id)">
                      <ElIcon><Collection /></ElIcon>
                    </ElButton>
                  </ElTooltip>
                  <ElTooltip content="查看详情" placement="top">
                    <ElButton type="info" size="small" circle @click="openDrawer(scope.row)">
                      <ElIcon><View /></ElIcon>
                    </ElButton>
                  </ElTooltip>
                </div>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElTabPane>
        <ElTabPane label="暂存" name="draft">
          <ElTable v-loading="loadingData" :data="filteredData" stripe row-class-name="data-table-row" @row-click="(row: MdmMainData) => openDrawer(row)" style="cursor: pointer;">
            <template #empty>
              <div style="padding: 40px 0;">
                <ElIcon :size="40" style="color: var(--text-muted);"><Document /></ElIcon>
                <p style="margin-top: 12px; color: var(--text-secondary);">暂无数据</p>
              </div>
            </template>
            <ElTableColumn type="selection" width="45" />
            <ElTableColumn prop="code" label="数据编码" min-width="140">
              <template #default="scope"><span class="cell-code">{{ scope.row.code }}</span></template>
            </ElTableColumn>
            <ElTableColumn prop="dataStatus" label="状态" width="130">
              <template #default="scope">
                <span class="enhanced-status" :class="getStatusClass(scope.row.dataStatus)">
                  <ElIcon :size="12"><component :is="getStatusIcon(scope.row.dataStatus)" /></ElIcon>
                  {{ scope.row.dataStatus }}
                </span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="createdByName" label="创建人" width="120" />
            <ElTableColumn prop="createTime" label="创建时间" width="170" />
            <ElTableColumn label="操作" width="160" fixed="right">
              <template #default="scope">
                <div class="action-btns" @click.stop>
                  <ElTooltip content="编辑" placement="top">
                    <ElButton v-auth="'btn:mainData:edit'" type="primary" size="small" circle @click="openDialog(scope.row)"><ElIcon><Edit /></ElIcon></ElButton>
                  </ElTooltip>
                  <ElTooltip content="提交审核" placement="top">
                    <ElButton v-auth="'btn:mainData:submit'" type="success" size="small" circle @click="handleSubmit(scope.row.id)"><ElIcon><Position /></ElIcon></ElButton>
                  </ElTooltip>
                  <ElTooltip content="删除" placement="top">
                    <ElButton v-auth="'btn:mainData:delete'" type="danger" size="small" circle @click="handleDelete(scope.row.id)"><ElIcon><Delete /></ElIcon></ElButton>
                  </ElTooltip>
                </div>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElTabPane>
        <ElTabPane label="审核中" name="pending">
          <ElTable v-loading="loadingData" :data="filteredData" stripe row-class-name="data-table-row" @row-click="(row: MdmMainData) => openDrawer(row)" style="cursor: pointer;">
            <template #empty>
              <div style="padding: 40px 0;">
                <ElIcon :size="40" style="color: var(--text-muted);"><Document /></ElIcon>
                <p style="margin-top: 12px; color: var(--text-secondary);">暂无数据</p>
              </div>
            </template>
            <ElTableColumn type="selection" width="45" />
            <ElTableColumn prop="code" label="数据编码" min-width="140">
              <template #default="scope"><span class="cell-code">{{ scope.row.code }}</span></template>
            </ElTableColumn>
            <ElTableColumn prop="dataStatus" label="状态" width="130">
              <template #default="scope">
                <span class="enhanced-status" :class="getStatusClass(scope.row.dataStatus)">
                  <ElIcon :size="12"><component :is="getStatusIcon(scope.row.dataStatus)" /></ElIcon>
                  {{ scope.row.dataStatus }}
                </span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="createdByName" label="创建人" width="120" />
            <ElTableColumn prop="createTime" label="创建时间" width="170" />
            <ElTableColumn label="操作" width="120" fixed="right">
              <template #default="scope">
                <div class="action-btns" @click.stop>
                  <ElTooltip content="通过" placement="top">
                    <ElButton type="success" size="small" circle @click="handleApprove(scope.row.id)"><ElIcon><Check /></ElIcon></ElButton>
                  </ElTooltip>
                  <ElTooltip content="拒绝" placement="top">
                    <ElButton type="danger" size="small" circle @click="handleReject(scope.row.id)"><ElIcon><Close /></ElIcon></ElButton>
                  </ElTooltip>
                </div>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElTabPane>
        <ElTabPane label="审核通过" name="approved">
          <ElTable v-loading="loadingData" :data="filteredData" stripe row-class-name="data-table-row" @row-click="(row: MdmMainData) => openDrawer(row)" style="cursor: pointer;">
            <template #empty>
              <div style="padding: 40px 0;">
                <ElIcon :size="40" style="color: var(--text-muted);"><Document /></ElIcon>
                <p style="margin-top: 12px; color: var(--text-secondary);">暂无数据</p>
              </div>
            </template>
            <ElTableColumn prop="code" label="数据编码" min-width="140">
              <template #default="scope"><span class="cell-code">{{ scope.row.code }}</span></template>
            </ElTableColumn>
            <ElTableColumn prop="dataStatus" label="状态" width="130">
              <template #default="scope">
                <span class="enhanced-status" :class="getStatusClass(scope.row.dataStatus)">
                  <ElIcon :size="12"><component :is="getStatusIcon(scope.row.dataStatus)" /></ElIcon>
                  {{ scope.row.dataStatus }}
                </span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="version" label="版本" width="80">
              <template #default="scope"><span class="cell-version">V{{ scope.row.version }}</span></template>
            </ElTableColumn>
            <ElTableColumn prop="createdByName" label="创建人" width="120" />
            <ElTableColumn prop="createTime" label="创建时间" width="170" />
            <ElTableColumn label="操作" width="120" fixed="right">
              <template #default="scope">
                <div class="action-btns" @click.stop>
                  <ElTooltip content="发起变更" placement="top">
                    <ElButton type="primary" size="small" circle @click="handleCreateVersion(scope.row.id)"><ElIcon><EditPen /></ElIcon></ElButton>
                  </ElTooltip>
                  <ElTooltip content="归档" placement="top">
                    <ElButton type="warning" size="small" circle @click="handleArchive(scope.row.id)"><ElIcon><Collection /></ElIcon></ElButton>
                  </ElTooltip>
                </div>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElTabPane>
        <ElTabPane label="已归档" name="archived">
          <ElTable v-loading="loadingData" :data="filteredData" stripe row-class-name="data-table-row" @row-click="(row: MdmMainData) => openDrawer(row)" style="cursor: pointer;">
            <template #empty>
              <div style="padding: 40px 0;">
                <ElIcon :size="40" style="color: var(--text-muted);"><Document /></ElIcon>
                <p style="margin-top: 12px; color: var(--text-secondary);">暂无数据</p>
              </div>
            </template>
            <ElTableColumn prop="code" label="数据编码" min-width="140">
              <template #default="scope"><span class="cell-code">{{ scope.row.code }}</span></template>
            </ElTableColumn>
            <ElTableColumn prop="dataStatus" label="状态" width="130">
              <template #default="scope">
                <span class="enhanced-status" :class="getStatusClass(scope.row.dataStatus)">
                  <ElIcon :size="12"><component :is="getStatusIcon(scope.row.dataStatus)" /></ElIcon>
                  {{ scope.row.dataStatus }}
                </span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="createdByName" label="创建人" width="120" />
            <ElTableColumn prop="createTime" label="创建时间" width="170" />
          </ElTable>
        </ElTabPane>
      </ElTabs>
    </div>

    <!-- 数据详情抽屉 -->
    <ElDrawer v-model="drawerVisible" title="数据详情" size="480px" :destroy-on-close="true">
      <div v-if="drawerData" class="drawer-content">
        <!-- 基本信息 -->
        <div class="detail-section">
          <h4 class="section-title">基本信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">数据编码</span>
              <span class="detail-value code">{{ drawerData.code }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">状态</span>
              <span class="enhanced-status" :class="getStatusClass(drawerData.dataStatus)">
                <ElIcon :size="12"><component :is="getStatusIcon(drawerData.dataStatus)" /></ElIcon>
                {{ drawerData.dataStatus }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">版本</span>
              <span class="detail-value">V{{ drawerData.version }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">创建人</span>
              <span class="detail-value">{{ drawerData.createdByName || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">创建时间</span>
              <span class="detail-value">{{ drawerData.createTime || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">修改时间</span>
              <span class="detail-value">{{ drawerData.modifyTime || '-' }}</span>
            </div>
          </div>
        </div>

        <!-- 数据内容 -->
        <div class="detail-section">
          <h4 class="section-title">数据内容</h4>
          <div class="json-preview">
            <pre>{{ JSON.stringify(formatJsonData(drawerData.jsonData), null, 2) }}</pre>
          </div>
        </div>

        <!-- 版本历史 -->
        <div class="detail-section">
          <h4 class="section-title">版本历史</h4>
          <div v-if="versionHistory.length > 0" class="version-timeline">
            <div v-for="(v, idx) in versionHistory" :key="idx" class="version-item">
              <div class="version-dot" :class="idx === 0 ? 'current' : ''"></div>
              <div v-if="idx < versionHistory.length - 1" class="version-line"></div>
              <div class="version-info">
                <span class="version-label">V{{ v.version || idx + 1 }}</span>
                <span class="version-time">{{ v.createTime || '-' }}</span>
              </div>
            </div>
          </div>
          <div v-else class="version-empty">暂无版本记录</div>
        </div>
      </div>
    </ElDrawer>

    <ElDialog title="数据详情" v-model="dialogVisible" width="500px">
      <ElForm :model="form" label-width="100px">
        <ElFormItem label="数据编码" required>
          <ElInput v-model="form.code" />
        </ElFormItem>
        <ElFormItem label="版本">
          <ElInput v-model="form.version" type="number" @input="(v: any) => form.version = Number(v)" />
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

    <!-- 批量编辑对话框 -->
    <ElDialog title="批量编辑" v-model="batchEditDialogVisible" width="550px">
      <p style="margin-bottom: 12px; color: var(--text-secondary);">已选择 {{ selectedRows.length }} 条数据，只修改下方填写的字段</p>
      <ElForm label-width="120px">
        <ElFormItem v-for="attr in currentModelAttributes" :key="attr.id" :label="attr.attributeName">
          <ElInput v-model="batchEditFields[attr.attributeCode]" :placeholder="attr.defaultValue || '不修改'" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="batchEditDialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="batchEditLoading" @click="handleBatchEdit">确认修改</ElButton>
      </template>
    </ElDialog>

    <!-- 导入对话框 -->
    <ImportDialog v-model="importDialogVisible" :model-id="selectedModelId" @success="loadMainData" />

    <!-- 导出对话框 -->
    <ExportDialog v-model="exportDialogVisible" :model-id="selectedModelId" />
  </div>
</template>

<style scoped lang="scss">
// 搜索面板
.search-card {
  margin-bottom: 16px;
}

.search-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
  padding-bottom: 0;

  .search-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    font-weight: 500;
    color: var(--text-primary);
  }

  .el-icon {
    transition: transform 0.3s ease;
    color: var(--text-muted);
    &.expanded { transform: rotate(90deg); }
  }
}

.search-body {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

// 统计条
.status-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 12px 20px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  margin-bottom: 16px;
}

.status-bar-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  transition: var(--transition-fast);

  &:hover, &.active {
    background: rgba(0, 212, 255, 0.04);
  }

  &.active {
    .status-bar-value { color: #00a8cc; }
  }
}

.status-bar-divider {
  width: 1px;
  height: 20px;
  background: var(--border-color);
}

.status-bar-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;

  &.draft { background: var(--text-secondary); }
  &.pending { background: var(--color-warning); }
  &.approved { background: var(--color-success); }
  &.archived { background: var(--text-muted); }
}

.status-bar-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.status-bar-value {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-bright);
  font-variant-numeric: tabular-nums;

  &.total { color: #00a8cc; }
}

// 表格增强
:deep(.data-table-row) {
  cursor: pointer;
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

.cell-code {
  color: var(--color-primary);
  font-weight: 500;
  font-family: var(--font-mono);
  font-size: 13px;
}

.cell-version {
  display: inline-block;
  padding: 2px 8px;
  background: rgba(99, 102, 241, 0.1);
  border: 1px solid rgba(99, 102, 241, 0.2);
  color: var(--color-accent);
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  font-family: var(--font-mono);
}

// 增强状态标签
.enhanced-status {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;

  &.status-approved {
    color: var(--color-success);
    background: rgba(16, 185, 129, 0.1);
    border: 1px solid rgba(16, 185, 129, 0.25);
  }
  &.status-pending {
    color: var(--color-warning);
    background: rgba(245, 158, 11, 0.1);
    border: 1px solid rgba(245, 158, 11, 0.25);
  }
  &.status-draft {
    color: var(--text-secondary);
    background: rgba(148, 163, 184, 0.1);
    border: 1px solid rgba(148, 163, 184, 0.25);
  }
  &.status-rejected {
    color: var(--color-danger);
    background: rgba(239, 68, 68, 0.1);
    border: 1px solid rgba(239, 68, 68, 0.25);
  }
  &.status-archived {
    color: var(--text-muted);
    background: rgba(100, 116, 139, 0.1);
    border: 1px solid rgba(100, 116, 139, 0.25);
  }
}

// 操作按钮组
.action-btns {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: nowrap;

  .el-button {
    margin: 0 !important;
  }
}

// 抽屉详情
.drawer-content {
  padding: 0 4px;
}

.detail-section {
  margin-bottom: 28px;

  &:last-child { margin-bottom: 0; }
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-bright);
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 3px solid var(--color-primary);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 12px;
  color: var(--text-muted);
}

.detail-value {
  font-size: 13px;
  color: var(--text-primary);

  &.code {
    color: var(--color-primary);
    font-family: var(--font-mono);
    font-weight: 500;
  }
}

.json-preview {
  background: rgba(13, 27, 42, 0.6);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  padding: 14px;
  max-height: 240px;
  overflow-y: auto;

  pre {
    color: var(--text-primary);
    font-size: 12px;
    font-family: var(--font-mono);
    white-space: pre-wrap;
    word-break: break-all;
    margin: 0;
  }
}

// 版本时间线
.version-timeline {
  display: flex;
  flex-direction: column;
}

.version-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  position: relative;
  padding-bottom: 16px;

  &:last-child { padding-bottom: 0; }
}

.version-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--text-muted);
  border: 2px solid var(--bg-tertiary);
  flex-shrink: 0;
  margin-top: 4px;

  &.current {
    background: var(--color-primary);
    box-shadow: 0 0 6px rgba(0, 168, 204, 0.3);
  }
}

.version-line {
  position: absolute;
  left: 4px;
  top: 14px;
  width: 2px;
  height: calc(100% - 14px);
  background: var(--border-color);
}

.version-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.version-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary);
  font-family: var(--font-mono);
}

.version-time {
  font-size: 12px;
  color: var(--text-muted);
}

.version-empty {
  text-align: center;
  padding: 20px;
  color: var(--text-muted);
  font-size: 13px;
}
</style>
