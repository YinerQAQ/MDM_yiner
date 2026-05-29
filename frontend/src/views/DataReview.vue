<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllModels } from '../api/models'
import { getMainDataByModelId, approveData, rejectData, getMainDataById, withdrawData, getVersionHistory } from '../api/mainData'
import type { MdmDataModel, MdmMainData } from '../api/types'
import StatusTag from '../components/StatusTag.vue'
import DataDiff from '../components/DataDiff.vue'
import { transferTask } from '../api/workflows'

const models = ref<MdmDataModel[]>([])
const mainDataList = ref<MdmMainData[]>([])
const activeTab = ref('all')

const selectedData = ref<MdmMainData | null>(null)
const reviewOpinion = ref('')
const selectedIds = ref<string[]>([])
const versionHistory = ref<any[]>([])
const detailLoading = ref(false)
const transferDialogVisible = ref(false)
const transferUserId = ref('')

// 变更对比数据
const diffData = ref<Array<{ fieldName: string; oldValue?: string; newValue?: string }>>([])

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

const selectReviewItem = async (row: any) => {
  try {
    detailLoading.value = true
    const res = await getMainDataById(row.id)
    selectedData.value = res.data.data
    reviewOpinion.value = ''
    diffData.value = []

    // 加载版本历史并生成diff
    try {
      const vRes = await getVersionHistory(row.id)
      versionHistory.value = vRes.data.data || []
      // 如果有旧版本，生成变更对比
      if (versionHistory.value.length > 1) {
        const oldVersion = versionHistory.value[1]
        const newVersion = versionHistory.value[0]
        const oldJson = formatJsonData(oldVersion.jsonData)
        const newJson = formatJsonData(newVersion.jsonData)
        const allKeys = new Set([...Object.keys(oldJson), ...Object.keys(newJson)])
        const diffs: typeof diffData.value = []
        allKeys.forEach(key => {
          if (oldJson[key] !== newJson[key]) {
            diffs.push({
              fieldName: key,
              oldValue: String(oldJson[key] ?? '-'),
              newValue: String(newJson[key] ?? '-')
            })
          }
        })
        diffData.value = diffs
      }
    } catch {
      versionHistory.value = []
    }
  } catch {
    ElMessage.error('加载详情失败')
  } finally {
    detailLoading.value = false
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
    selectedData.value = null
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
    selectedData.value = null
    selectedIds.value = []
    loadMainData()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleWithdraw = async (id: string) => {
  try {
    await withdrawData(id)
    ElMessage.success('已退回')
    selectedData.value = null
    loadMainData()
  } catch {
    ElMessage.error('退回失败')
  }
}

const handleTransfer = async () => {
  if (!transferUserId.value) {
    ElMessage.warning('请输入转办人')
    return
  }
  try {
    if (selectedData.value) {
      await transferTask(selectedData.value.id, { assignee: transferUserId.value })
      ElMessage.success('已转办')
      transferDialogVisible.value = false
      selectedData.value = null
      loadMainData()
    }
  } catch {
    ElMessage.error('转办失败')
  }
}

const getModelName = (modelId: string) => {
  const m = models.value.find(x => x.id === modelId)
  return m ? m.modelName : modelId
}

const formatJsonData = (json?: string) => {
  if (!json) return {} as Record<string, any>
  try {
    return JSON.parse(json)
  } catch {
    return {} as Record<string, any>
  }
}

// 获取优先级（模拟）
const getPriority = (row: any) => {
  if (row.isModify === 1) return 'high'
  return 'normal'
}

onMounted(async () => {
  await loadModels()
  await loadMainData()
})
</script>

<template>
  <div class="page-container review-page">
    <!-- 顶部操作栏 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">数据审核</h2>
        <p class="page-desc">审核主数据新增与变更申请</p>
      </div>
      <div style="display: flex; gap: 8px;">
        <el-button v-auth="'btn:review:approve'" type="success" @click="handleApprove()" :disabled="selectedIds.length === 0">
          批量通过 ({{ selectedIds.length }})
        </el-button>
        <el-button v-auth="'btn:review:reject'" type="danger" @click="handleReject()" :disabled="selectedIds.length === 0">
          批量拒绝
        </el-button>
      </div>
    </div>

    <!-- 标签切换 -->
    <div class="tab-bar">
      <div
        v-for="tab in [
          { key: 'all', label: '全部待审' },
          { key: 'mine', label: '个人待审' },
          { key: 'done', label: '已审核' }
        ]"
        :key="tab.key"
        class="tab-item"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
        <span v-if="tab.key === 'all'" class="tab-count">{{ filteredData.length }}</span>
      </div>
    </div>

    <!-- 左右分栏布局 -->
    <div class="review-layout">
      <!-- 左侧：待审核列表 (40%) -->
      <div class="review-list-panel card-glow">
        <div class="panel-header">
          <span class="panel-title">待审核列表</span>
          <span class="panel-count">{{ filteredData.length }} 条</span>
        </div>
        <div class="review-cards">
          <div
            v-for="item in filteredData"
            :key="item.id"
            class="review-card"
            :class="{ selected: selectedData?.id === item.id }"
            @click="selectReviewItem(item)"
          >
            <div class="card-top">
              <div class="card-code">{{ item.code }}</div>
              <span class="card-priority" :class="getPriority(item) === 'high' ? 'priority-high' : 'priority-normal'">
                {{ getPriority(item) === 'high' ? '紧急' : '普通' }}
              </span>
            </div>
            <div class="card-middle">
              <span class="card-model">
                <span class="model-dot"></span>
                {{ getModelName(item.modelId) }}
              </span>
              <StatusTag :status="item.dataStatus" />
            </div>
            <div class="card-bottom">
              <span class="card-user">{{ item.createdByName || '-' }}</span>
              <span class="card-time">{{ item.createTime || '-' }}</span>
            </div>
          </div>
          <div v-if="filteredData.length === 0" class="empty-list">
            <span>暂无待审核数据</span>
          </div>
        </div>
      </div>

      <!-- 右侧：审核详情 (60%) -->
      <div class="review-detail-panel card-glow">
        <div v-if="!selectedData" class="empty-detail">
          <div class="empty-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M9 12h6m-3-3v6m-7 4h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
            </svg>
          </div>
          <p>请从左侧选择待审核数据</p>
        </div>

        <div v-else class="detail-content" v-loading="detailLoading">
          <!-- 数据信息卡片 -->
          <div class="detail-section">
            <h4 class="section-title">数据信息</h4>
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">数据编码</span>
                <span class="info-value code">{{ selectedData.code }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">所属模型</span>
                <span class="info-value">{{ getModelName(selectedData.modelId) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">申请人</span>
                <span class="info-value">{{ selectedData.createdByName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">版本</span>
                <span class="info-value version">V{{ selectedData.version }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">状态</span>
                <StatusTag :status="selectedData.dataStatus" />
              </div>
              <div class="info-item">
                <span class="info-label">申请时间</span>
                <span class="info-value">{{ selectedData.createTime || '-' }}</span>
              </div>
            </div>

            <!-- JSON 数据展示 -->
            <div class="json-data-section">
              <span class="info-label">数据内容</span>
              <div class="json-preview">
                <pre>{{ JSON.stringify(formatJsonData(selectedData.jsonData), null, 2) }}</pre>
              </div>
            </div>
          </div>

          <!-- 变更对比区 -->
          <div v-if="diffData.length > 0" class="detail-section">
            <h4 class="section-title">变更对比</h4>
            <DataDiff :diffs="diffData" />
          </div>

          <!-- 审核意见时间线 -->
          <div class="detail-section">
            <h4 class="section-title">审核记录</h4>
            <div v-if="versionHistory.length > 0" class="approval-timeline">
              <div v-for="(v, idx) in versionHistory" :key="idx" class="approval-item">
                <div class="approval-dot" :class="idx === 0 ? 'current' : ''"></div>
                <div v-if="idx < versionHistory.length - 1" class="approval-line"></div>
                <div class="approval-info">
                  <div class="approval-header">
                    <span class="approval-version">V{{ v.version || idx + 1 }}</span>
                    <span class="approval-status">{{ v.dataStatus || '-' }}</span>
                  </div>
                  <div class="approval-meta">
                    <span>{{ v.createdByName || '-' }}</span>
                    <span>{{ v.createTime || '-' }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="no-approval">暂无审核记录</div>
          </div>

          <!-- 审核意见输入 -->
          <div class="detail-section opinion-section">
            <h4 class="section-title">审核意见</h4>
            <el-input
              v-model="reviewOpinion"
              type="textarea"
              :rows="3"
              placeholder="请输入审核意见（可选）"
              resize="none"
            />
          </div>
        </div>

        <!-- 底部固定操作栏 -->
        <div v-if="selectedData" class="detail-actions">
          <el-button v-auth="'btn:review:approve'" type="success" size="large" @click="handleApprove(selectedData.id)">
            <el-icon style="margin-right: 4px;"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 13l4 4L19 7"/></svg></el-icon>
            通过
          </el-button>
          <el-button v-auth="'btn:review:reject'" type="danger" size="large" @click="handleReject(selectedData.id)">
            <el-icon style="margin-right: 4px;"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 18L18 6M6 6l12 12"/></svg></el-icon>
            拒绝
          </el-button>
          <el-button size="large" @click="transferDialogVisible = true">
            转办
          </el-button>
          <el-button size="large" @click="handleWithdraw(selectedData.id)">
            退回
          </el-button>
        </div>
      </div>
    </div>

    <!-- 转办对话框 -->
    <el-dialog title="转办" v-model="transferDialogVisible" width="400px">
      <el-form label-width="80px">
        <el-form-item label="转办人">
          <el-input v-model="transferUserId" placeholder="请输入转办人用户名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTransfer">确认转办</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.review-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - var(--header-height, 60px) - 40px);
  overflow: hidden;
}

// 标签栏
.tab-bar {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;
  padding: 4px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  width: fit-content;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  font-size: 13px;
  color: var(--text-secondary);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: var(--transition-fast);
  user-select: none;

  &:hover { color: var(--text-primary); background: rgba(0, 212, 255, 0.04); }
  &.active {
    color: var(--color-primary);
    background: rgba(0, 212, 255, 0.1);
    font-weight: 500;
  }
}

.tab-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  background: var(--color-primary);
  border-radius: 9px;
}

// 左右分栏
.review-layout {
  display: flex;
  gap: 20px;
  flex: 1;
  min-height: 0;
}

.review-list-panel {
  width: 40%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.review-detail-panel {
  width: 60%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-bright);
  padding-left: 12px;
  border-left: 3px solid var(--color-primary);
}

.panel-count {
  font-size: 12px;
  color: var(--text-muted);
}

// 审核卡片列表
.review-cards {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.review-card {
  padding: 14px 16px;
  border-radius: var(--radius-sm);
  background: rgba(13, 27, 42, 0.4);
  border: 1px solid transparent;
  cursor: pointer;
  transition: var(--transition-fast);

  &:hover {
    background: rgba(0, 212, 255, 0.04);
    border-color: var(--border-glow);
  }

  &.selected {
    background: rgba(0, 212, 255, 0.08);
    border-color: var(--color-primary);
    box-shadow: 0 0 8px rgba(0, 168, 204, 0.12);
  }
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.card-code {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary);
  font-family: var(--font-mono);
}

.card-priority {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;

  &.priority-high {
    color: var(--color-danger);
    background: rgba(239, 68, 68, 0.1);
    border: 1px solid rgba(239, 68, 68, 0.25);
  }
  &.priority-normal {
    color: var(--text-secondary);
    background: rgba(148, 163, 184, 0.1);
    border: 1px solid rgba(148, 163, 184, 0.2);
  }
}

.card-middle {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.card-model {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.model-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-accent);
}

.card-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-user {
  font-size: 12px;
  color: var(--text-muted);
}

.card-time {
  font-size: 11px;
  color: var(--text-muted);
}

.empty-list {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--text-muted);
  font-size: 13px;
}

// 空详情
.empty-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-muted);

  .empty-icon {
    color: var(--border-color);
  }

  p {
    font-size: 14px;
  }
}

// 详情内容
.detail-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.detail-section {
  margin-bottom: 24px;
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

// 数据信息网格
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: var(--text-muted);
}

.info-value {
  font-size: 13px;
  color: var(--text-primary);

  &.code {
    color: var(--color-primary);
    font-family: var(--font-mono);
    font-weight: 500;
  }
  &.version {
    display: inline-block;
    padding: 2px 8px;
    background: rgba(99, 102, 241, 0.1);
    border: 1px solid rgba(99, 102, 241, 0.2);
    color: var(--color-accent);
    border-radius: 4px;
    font-size: 12px;
    font-weight: 500;
    font-family: var(--font-mono);
    width: fit-content;
  }
}

.json-data-section {
  margin-top: 14px;
}

.json-preview {
  margin-top: 6px;
  background: rgba(13, 27, 42, 0.6);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  padding: 14px;
  max-height: 180px;
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

// 审核时间线
.approval-timeline {
  display: flex;
  flex-direction: column;
}

.approval-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  position: relative;
  padding-bottom: 16px;

  &:last-child { padding-bottom: 0; }
}

.approval-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--text-muted);
  border: 2px solid var(--bg-tertiary);
  flex-shrink: 0;
  margin-top: 5px;

  &.current {
    background: var(--color-primary);
    box-shadow: 0 0 6px rgba(0, 168, 204, 0.3);
  }
}

.approval-line {
  position: absolute;
  left: 4px;
  top: 15px;
  width: 2px;
  height: calc(100% - 15px);
  background: var(--border-color);
}

.approval-info {
  flex: 1;
}

.approval-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.approval-version {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary);
  font-family: var(--font-mono);
}

.approval-status {
  font-size: 12px;
  color: var(--text-secondary);
}

.approval-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--text-muted);
}

.no-approval {
  text-align: center;
  padding: 20px;
  color: var(--text-muted);
  font-size: 13px;
}

// 审核意见
.opinion-section {
  margin-bottom: 80px; // 为底部操作栏留空间
}

// 底部固定操作栏
.detail-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid var(--border-color);
  background: rgba(27, 40, 56, 0.95);
  backdrop-filter: blur(10px);
  flex-shrink: 0;

  .el-button--success {
    --el-button-bg-color: rgba(16, 185, 129, 0.15);
    --el-button-border-color: rgba(16, 185, 129, 0.4);
    --el-button-hover-bg-color: rgba(16, 185, 129, 0.25);
    --el-button-hover-border-color: rgba(16, 185, 129, 0.6);
  }

  .el-button--danger {
    --el-button-bg-color: rgba(239, 68, 68, 0.15);
    --el-button-border-color: rgba(239, 68, 68, 0.4);
    --el-button-hover-bg-color: rgba(239, 68, 68, 0.25);
    --el-button-hover-border-color: rgba(239, 68, 68, 0.6);
  }
}
</style>
