<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import {
  ElTable,
  ElTableColumn,
  ElButton,
  ElInput,
  ElInputNumber,
  ElDialog,
  ElForm,
  ElFormItem,
  ElMessage,
  ElMessageBox
} from 'element-plus'

interface SecurityLevel {
  id: string
  code: string
  name: string
  scope: string
  restriction: string
  sort: number
  color: string
}

interface FieldBinding {
  id: string
  modelName: string
  fieldCode: string
  fieldName: string
  levelCode: string
  bindBy: string
  bindTime: string
}

const levels = ref<SecurityLevel[]>([])
const bindings = ref<FieldBinding[]>([])
const search = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive<SecurityLevel>({
  id: '', code: '', name: '',
  scope: '', restriction: '',
  sort: 1, color: '#10b981'
})

const buildLevels = (): SecurityLevel[] => [
  {
    id: 'L1', code: 'PUBLIC', name: '公开',
    scope: '所有内外部用户',
    restriction: '可对外发布、无访问限制',
    sort: 1, color: '#10b981'
  },
  {
    id: 'L2', code: 'INTERNAL', name: '内部',
    scope: '集团全员',
    restriction: '禁止外发，仅内部系统调用',
    sort: 2, color: '#06b6d4'
  },
  {
    id: 'L3', code: 'SECRET', name: '秘密',
    scope: '部门内 + 授权人员',
    restriction: '需脱敏展示，写操作需审批',
    sort: 3, color: '#d97706'
  },
  {
    id: 'L4', code: 'CONFIDENTIAL', name: '机密',
    scope: '指定授权人员（白名单）',
    restriction: '全程审计、字段加密、双人复核',
    sort: 4, color: '#dc2626'
  }
]

const buildBindings = (): FieldBinding[] => [
  { id: 'B1', modelName: '客户主数据', fieldCode: 'idCard', fieldName: '身份证号', levelCode: 'CONFIDENTIAL', bindBy: '管理员', bindTime: '2026-04-12 10:21' },
  { id: 'B2', modelName: '客户主数据', fieldCode: 'phone', fieldName: '手机号', levelCode: 'SECRET', bindBy: '管理员', bindTime: '2026-04-12 10:22' },
  { id: 'B3', modelName: '客户主数据', fieldCode: 'name', fieldName: '客户名称', levelCode: 'INTERNAL', bindBy: '管理员', bindTime: '2026-04-12 10:23' },
  { id: 'B4', modelName: '员工主数据', fieldCode: 'salary', fieldName: '薪资', levelCode: 'CONFIDENTIAL', bindBy: 'HR管理员', bindTime: '2026-04-15 14:01' },
  { id: 'B5', modelName: '员工主数据', fieldCode: 'bankCard', fieldName: '银行卡号', levelCode: 'CONFIDENTIAL', bindBy: 'HR管理员', bindTime: '2026-04-15 14:02' },
  { id: 'B6', modelName: '供应商主数据', fieldCode: 'taxNo', fieldName: '税号', levelCode: 'SECRET', bindBy: '采购管理员', bindTime: '2026-05-01 09:33' },
  { id: 'B7', modelName: '供应商主数据', fieldCode: 'bankAccount', fieldName: '收款账号', levelCode: 'CONFIDENTIAL', bindBy: '财务管理员', bindTime: '2026-05-01 09:35' },
  { id: 'B8', modelName: '产品主数据', fieldCode: 'cost', fieldName: '成本价', levelCode: 'SECRET', bindBy: '管理员', bindTime: '2026-05-08 11:00' },
  { id: 'B9', modelName: '产品主数据', fieldCode: 'spec', fieldName: '产品规格', levelCode: 'INTERNAL', bindBy: '管理员', bindTime: '2026-05-08 11:01' },
  { id: 'B10', modelName: '产品主数据', fieldCode: 'name', fieldName: '产品名称', levelCode: 'PUBLIC', bindBy: '管理员', bindTime: '2026-05-08 11:02' }
]

const loadAll = () => {
  levels.value = buildLevels()
  bindings.value = buildBindings()
}

const filteredBindings = computed(() =>
  bindings.value.filter(b =>
    !search.value ||
    b.modelName.includes(search.value) ||
    b.fieldName.includes(search.value) ||
    b.fieldCode.includes(search.value)
  )
)

const stats = computed(() =>
  levels.value.map(l => ({
    ...l,
    count: bindings.value.filter(b => b.levelCode === l.code).length
  }))
)

const findLevel = (code: string) => levels.value.find(l => l.code === code)

const openDialog = (row?: SecurityLevel) => {
  if (row) {
    isEdit.value = true
    Object.assign(form, row)
  } else {
    isEdit.value = false
    Object.assign(form, {
      id: '', code: '', name: '',
      scope: '', restriction: '',
      sort: levels.value.length + 1,
      color: '#06b6d4'
    })
  }
  dialogVisible.value = true
}

const save = () => {
  if (!form.code || !form.name) {
    ElMessage.error('请填写编码和名称')
    return
  }
  if (isEdit.value) {
    const idx = levels.value.findIndex(l => l.id === form.id)
    if (idx > -1) levels.value[idx] = { ...form }
    ElMessage.success('已更新')
  } else {
    levels.value.push({ ...form, id: `L${Date.now().toString().slice(-4)}` })
    levels.value.sort((a, b) => a.sort - b.sort)
    ElMessage.success('已新增')
  }
  dialogVisible.value = false
}

const remove = async (row: SecurityLevel) => {
  const used = bindings.value.some(b => b.levelCode === row.code)
  if (used) {
    ElMessage.warning('当前密级已被字段绑定，无法删除')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除密级【${row.name}】？`, '提示', { type: 'warning' })
    levels.value = levels.value.filter(l => l.id !== row.id)
    ElMessage.success('已删除')
  } catch { /* cancelled */ }
}

const removeBinding = (row: FieldBinding) => {
  bindings.value = bindings.value.filter(b => b.id !== row.id)
  ElMessage.success('已解绑')
}

onMounted(loadAll)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">密级配置</h2>
        <p class="page-desc">维护数据密级等级，并跟踪模型字段的密级绑定情况</p>
      </div>
      <ElButton type="primary" @click="openDialog()">新增密级</ElButton>
    </div>

    <!-- 密级总览卡 -->
    <div class="sec-grid">
      <div
        v-for="lv in stats"
        :key="lv.id"
        class="sec-card"
        :style="{ '--sec-color': lv.color }"
      >
        <div class="sec-card-head">
          <span class="sec-name">{{ lv.name }}</span>
          <span class="sec-code">{{ lv.code }}</span>
        </div>
        <div class="sec-count">{{ lv.count }} <span>个字段</span></div>
        <div class="sec-restrict">{{ lv.restriction }}</div>
      </div>
    </div>

    <!-- 密级列表 -->
    <div class="table-card" style="margin-bottom:20px;">
      <div class="card-title">密级定义</div>
      <ElTable :data="levels" stripe>
        <ElTableColumn label="级别" width="110">
          <template #default="scope">
            <span class="level-pill" :style="{ '--sec-color': scope.row.color }">
              {{ scope.row.name }}
            </span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="code" label="编码" width="160">
          <template #default="scope">
            <span style="font-family:var(--font-mono);font-size:12px;">{{ scope.row.code }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="scope" label="可见范围" min-width="200" />
        <ElTableColumn prop="restriction" label="访问限制描述" min-width="260" />
        <ElTableColumn prop="sort" label="排序" width="80" />
        <ElTableColumn label="操作" width="160" fixed="right">
          <template #default="scope">
            <ElButton link size="small" type="primary" @click="openDialog(scope.row)">编辑</ElButton>
            <ElButton link size="small" type="danger" @click="remove(scope.row)">删除</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </div>

    <!-- 字段绑定 -->
    <div class="table-card">
      <div class="card-title-row">
        <div class="card-title">字段密级绑定</div>
        <ElInput
          v-model="search"
          placeholder="搜索模型 / 字段"
          clearable
          style="width:280px;"
        />
      </div>
      <ElTable :data="filteredBindings" stripe>
        <ElTableColumn prop="modelName" label="数据模型" width="160" />
        <ElTableColumn prop="fieldCode" label="字段编码" width="140">
          <template #default="scope">
            <span style="font-family:var(--font-mono);font-size:12px;color:var(--color-primary)">
              {{ scope.row.fieldCode }}
            </span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="fieldName" label="字段名称" width="140" />
        <ElTableColumn label="密级" width="120">
          <template #default="scope">
            <span class="level-pill" :style="{ '--sec-color': findLevel(scope.row.levelCode)?.color || '#94a3b8' }">
              {{ findLevel(scope.row.levelCode)?.name || scope.row.levelCode }}
            </span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="bindBy" label="绑定人" width="120" />
        <ElTableColumn prop="bindTime" label="绑定时间" width="160" />
        <ElTableColumn label="操作" width="100" fixed="right">
          <template #default="scope">
            <ElButton link size="small" type="danger" @click="removeBinding(scope.row)">解绑</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </div>

    <!-- 新增/编辑 -->
    <ElDialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑密级' : '新增密级'"
      width="520px"
    >
      <ElForm :model="form" label-width="100px">
        <ElFormItem label="级别名称" required>
          <ElInput v-model="form.name" />
        </ElFormItem>
        <ElFormItem label="级别编码" required>
          <ElInput v-model="form.code" :disabled="isEdit" placeholder="如 SECRET" />
        </ElFormItem>
        <ElFormItem label="可见范围">
          <ElInput v-model="form.scope" placeholder="如 部门内 + 授权人员" />
        </ElFormItem>
        <ElFormItem label="访问限制">
          <ElInput v-model="form.restriction" type="textarea" :rows="3" />
        </ElFormItem>
        <ElFormItem label="排序">
          <ElInputNumber v-model="form.sort" :min="0" :max="99" style="width:100%" />
        </ElFormItem>
        <ElFormItem label="标识颜色">
          <ElInput v-model="form.color" placeholder="#10b981" />
          <div style="margin-top:6px;">
            <span class="level-pill" :style="{ '--sec-color': form.color }">{{ form.name || '示例' }}</span>
          </div>
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
.sec-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}
.sec-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 16px 18px;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
  transition: var(--transition-normal);
  &::before {
    content: '';
    position: absolute;
    top: 0; left: 0; bottom: 0;
    width: 4px;
    background: var(--sec-color);
  }
  &:hover {
    border-color: var(--sec-color);
    box-shadow: 0 0 16px color-mix(in srgb, var(--sec-color) 25%, transparent);
    transform: translateY(-2px);
  }
}
.sec-card-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 8px;
  .sec-name {
    color: var(--sec-color);
    font-size: 16px;
    font-weight: 600;
  }
  .sec-code {
    color: var(--text-muted);
    font-size: 11px;
    font-family: var(--font-mono);
  }
}
.sec-count {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-bright);
  margin-bottom: 6px;
  span { font-size: 12px; color: var(--text-muted); margin-left: 4px; font-weight: 400; }
}
.sec-restrict {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
}
.level-pill {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
  color: var(--sec-color);
  background: color-mix(in srgb, var(--sec-color) 12%, transparent);
  border: 1px solid color-mix(in srgb, var(--sec-color) 35%, transparent);
}
.card-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 12px;
  border-left: 3px solid var(--color-primary);
  padding-left: 8px;
}
.card-title-row {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 12px;
  .card-title { margin-bottom: 0; }
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
