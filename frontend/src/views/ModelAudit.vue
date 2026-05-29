<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
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

interface AuditItem {
  id: string
  modelCode: string
  modelName: string
  modelType: string
  submitter: string
  submitTime: string
  version: number
  status: '待审核' | '已通过' | '已驳回'
  description: string
  remark?: string
}

const search = ref('')
const statusFilter = ref<string>('')
const items = ref<AuditItem[]>([])

const detailVisible = ref(false)
const rejectVisible = ref(false)
const currentItem = ref<AuditItem | null>(null)
const rejectRemark = ref('')

const buildMock = (): AuditItem[] => [
  {
    id: 'AUD-001',
    modelCode: 'MD_PRODUCT',
    modelName: '产品主数据',
    modelType: '普通',
    submitter: '张工',
    submitTime: '2026-05-26 14:32:10',
    version: 3,
    status: '待审核',
    description: '新增材料属性字段，扩展规格参数'
  },
  {
    id: 'AUD-002',
    modelCode: 'MD_CUSTOMER',
    modelName: '客户主数据',
    modelType: '普通',
    submitter: '李工',
    submitTime: '2026-05-25 09:21:44',
    version: 2,
    status: '待审核',
    description: '增加客户等级与黑名单标识字段'
  },
  {
    id: 'AUD-003',
    modelCode: 'MD_SUPPLIER',
    modelName: '供应商主数据',
    modelType: '普通',
    submitter: '王工',
    submitTime: '2026-05-22 16:05:09',
    version: 5,
    status: '已通过',
    description: '银行账号字段加密策略调整',
    remark: '与财务确认一致，通过'
  },
  {
    id: 'AUD-004',
    modelCode: 'MD_MATERIAL',
    modelName: '物料分类模型',
    modelType: '类别',
    submitter: '陈工',
    submitTime: '2026-05-20 11:48:33',
    version: 1,
    status: '已驳回',
    description: '新建物料三级分类层级',
    remark: '编码规则与现行规范不一致，请重新拟定'
  },
  {
    id: 'AUD-005',
    modelCode: 'MD_ORG',
    modelName: '组织主数据',
    modelType: '普通',
    submitter: '赵工',
    submitTime: '2026-05-28 10:11:02',
    version: 4,
    status: '待审核',
    description: '调整组织上下级引用关系'
  }
]

const loadData = () => {
  items.value = buildMock()
}

const filteredItems = computed(() => {
  return items.value.filter(it => {
    if (statusFilter.value && it.status !== statusFilter.value) return false
    if (search.value) {
      const kw = search.value.toLowerCase()
      if (
        !it.modelName.toLowerCase().includes(kw) &&
        !it.modelCode.toLowerCase().includes(kw)
      ) return false
    }
    return true
  })
})

const counts = computed(() => ({
  pending: items.value.filter(i => i.status === '待审核').length,
  approved: items.value.filter(i => i.status === '已通过').length,
  rejected: items.value.filter(i => i.status === '已驳回').length
}))

const statusClass = (s: string) => {
  if (s === '已通过') return 'status-approved'
  if (s === '待审核') return 'status-pending'
  if (s === '已驳回') return 'status-rejected'
  return 'status-draft'
}

const handleApprove = async (row: AuditItem) => {
  try {
    await ElMessageBox.confirm(`确认通过模型【${row.modelName}】v${row.version} 的审核？`, '审核通过', {
      type: 'success'
    })
    row.status = '已通过'
    row.remark = '审核通过'
    ElMessage.success('已通过审核')
  } catch {
    /* cancelled */
  }
}

const openReject = (row: AuditItem) => {
  currentItem.value = row
  rejectRemark.value = ''
  rejectVisible.value = true
}

const submitReject = () => {
  if (!rejectRemark.value.trim()) {
    ElMessage.error('请填写驳回理由')
    return
  }
  if (currentItem.value) {
    currentItem.value.status = '已驳回'
    currentItem.value.remark = rejectRemark.value
    ElMessage.success('已驳回')
  }
  rejectVisible.value = false
}

const openDetail = (row: AuditItem) => {
  currentItem.value = row
  detailVisible.value = true
}

const resetFilter = () => {
  search.value = ''
  statusFilter.value = ''
}

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">模型审核</h2>
        <p class="page-desc">对提交的数据模型进行审核、通过或驳回</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div class="stat-card stat-card--pending">
        <div class="stat-label">待审核</div>
        <div class="stat-value">{{ counts.pending }}</div>
      </div>
      <div class="stat-card stat-card--approved">
        <div class="stat-label">已通过</div>
        <div class="stat-value">{{ counts.approved }}</div>
      </div>
      <div class="stat-card stat-card--rejected">
        <div class="stat-label">已驳回</div>
        <div class="stat-value">{{ counts.rejected }}</div>
      </div>
      <div class="stat-card stat-card--total">
        <div class="stat-label">合计</div>
        <div class="stat-value">{{ items.length }}</div>
      </div>
    </div>

    <!-- 筛选 -->
    <div class="search-card">
      <div style="display:flex; gap:16px; flex-wrap:wrap; align-items:center;">
        <ElInput
          v-model="search"
          placeholder="搜索模型名称 / 编码"
          clearable
          style="width:280px;"
        />
        <ElSelect v-model="statusFilter" placeholder="审核状态" clearable style="width:160px;">
          <ElOption label="待审核" value="待审核" />
          <ElOption label="已通过" value="已通过" />
          <ElOption label="已驳回" value="已驳回" />
        </ElSelect>
        <ElButton @click="resetFilter">重置</ElButton>
      </div>
    </div>

    <div class="table-card">
      <ElTable :data="filteredItems" stripe>
        <ElTableColumn prop="modelCode" label="模型编码" width="140" />
        <ElTableColumn prop="modelName" label="模型名称" min-width="160" />
        <ElTableColumn prop="modelType" label="类型" width="90">
          <template #default="scope">
            <ElTag size="small" :type="scope.row.modelType === '类别' ? 'warning' : 'info'">
              {{ scope.row.modelType }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="version" label="版本" width="80">
          <template #default="scope">v{{ scope.row.version }}</template>
        </ElTableColumn>
        <ElTableColumn prop="submitter" label="提交人" width="100" />
        <ElTableColumn prop="submitTime" label="提交时间" width="170" />
        <ElTableColumn prop="status" label="审核状态" width="110">
          <template #default="scope">
            <span :class="statusClass(scope.row.status)">{{ scope.row.status }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="240" fixed="right">
          <template #default="scope">
            <ElButton size="small" link type="primary" @click="openDetail(scope.row)">详情</ElButton>
            <ElButton
              v-if="scope.row.status === '待审核'"
              size="small"
              link
              type="success"
              @click="handleApprove(scope.row)"
            >通过</ElButton>
            <ElButton
              v-if="scope.row.status === '待审核'"
              size="small"
              link
              type="danger"
              @click="openReject(scope.row)"
            >驳回</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </div>

    <!-- 详情 -->
    <ElDialog v-model="detailVisible" title="模型审核详情" width="640px">
      <ElDescriptions v-if="currentItem" :column="2" border>
        <ElDescriptionsItem label="模型编码">{{ currentItem.modelCode }}</ElDescriptionsItem>
        <ElDescriptionsItem label="模型名称">{{ currentItem.modelName }}</ElDescriptionsItem>
        <ElDescriptionsItem label="版本">v{{ currentItem.version }}</ElDescriptionsItem>
        <ElDescriptionsItem label="类型">{{ currentItem.modelType }}</ElDescriptionsItem>
        <ElDescriptionsItem label="提交人">{{ currentItem.submitter }}</ElDescriptionsItem>
        <ElDescriptionsItem label="提交时间">{{ currentItem.submitTime }}</ElDescriptionsItem>
        <ElDescriptionsItem label="状态" :span="2">
          <span :class="statusClass(currentItem.status)">{{ currentItem.status }}</span>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="变更说明" :span="2">{{ currentItem.description }}</ElDescriptionsItem>
        <ElDescriptionsItem label="审核备注" :span="2">{{ currentItem.remark || '-' }}</ElDescriptionsItem>
      </ElDescriptions>
    </ElDialog>

    <!-- 驳回对话框 -->
    <ElDialog v-model="rejectVisible" title="驳回审核" width="480px">
      <ElForm label-width="80px">
        <ElFormItem label="驳回理由" required>
          <ElInput
            v-model="rejectRemark"
            type="textarea"
            :rows="4"
            placeholder="请输入驳回理由（必填）"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="rejectVisible = false">取消</ElButton>
        <ElButton type="danger" @click="submitReject">确认驳回</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped lang="scss">
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 18px 20px;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
  transition: var(--transition-normal);
  &:hover {
    border-color: var(--border-glow);
    box-shadow: var(--shadow-glow);
    transform: translateY(-2px);
  }
  &::before {
    content: '';
    position: absolute;
    inset: 0 auto 0 0;
    width: 4px;
  }
  .stat-label {
    color: var(--text-secondary);
    font-size: 13px;
  }
  .stat-value {
    font-size: 28px;
    font-weight: 600;
    margin-top: 6px;
    color: var(--text-bright);
  }
  &--pending::before { background: var(--color-warning); }
  &--approved::before { background: var(--color-success); }
  &--rejected::before { background: var(--color-danger); }
  &--total::before { background: var(--color-primary); }
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
