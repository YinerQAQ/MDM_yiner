<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Folder, Document, Operation, Plus, Edit, Delete, CirclePlus } from '@element-plus/icons-vue'
import { getMenuTree, createMenu, updateMenu, deleteMenu } from '../api/menus'
import type { BaseMenu } from '../api/menus'

type MenuType = '目录' | '菜单' | '按钮'

const menuTree = ref<BaseMenu[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const emptyForm = (): BaseMenu => ({
  id: '',
  menuCode: '',
  menuName: '',
  parentId: '0',
  menuType: '目录',
  path: '',
  component: '',
  icon: '',
  sortOrder: 0,
  status: '启用',
  visible: '是',
  perms: '',
  children: []
})

const form = ref<BaseMenu>(emptyForm())

const rules: FormRules = {
  menuCode: [{ required: true, message: '请输入菜单编码', trigger: 'blur' }],
  menuName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

// ============ 数据加载 ============
const loadMenus = async () => {
  loading.value = true
  try {
    const res = await getMenuTree()
    menuTree.value = res.data.data || []
  } catch {
    ElMessage.error('加载菜单树失败')
  } finally {
    loading.value = false
  }
}

// ============ 类型样式 ============
const typeMeta = (t: string) => {
  switch (t) {
    case '目录':
      return { icon: Folder, cls: 'type-tag type-tag--dir', text: '目录' }
    case '菜单':
      return { icon: Document, cls: 'type-tag type-tag--menu', text: '菜单' }
    case '按钮':
      return { icon: Operation, cls: 'type-tag type-tag--btn', text: '按钮' }
    default:
      return { icon: Document, cls: 'type-tag type-tag--menu', text: t || '-' }
  }
}

// ============ 父级选项（树）============
// 目录的父级：仅根 + 目录
// 菜单的父级：仅目录
// 按钮的父级：仅菜单
const parentOptions = computed(() => {
  const filterTree = (nodes: BaseMenu[], allow: MenuType[]): BaseMenu[] => {
    const out: BaseMenu[] = []
    for (const n of nodes) {
      if (allow.includes(n.menuType as MenuType)) {
        const children = n.children ? filterTree(n.children, allow) : []
        out.push({ ...n, children })
      } else if (n.children?.length) {
        out.push(...filterTree(n.children, allow))
      }
    }
    return out
  }

  if (form.value.menuType === '目录') {
    return [{ id: '0', menuName: '根目录', menuType: '目录', children: filterTree(menuTree.value, ['目录']) } as unknown as BaseMenu]
  }
  if (form.value.menuType === '菜单') {
    return filterTree(menuTree.value, ['目录'])
  }
  if (form.value.menuType === '按钮') {
    return filterTree(menuTree.value, ['目录', '菜单']).map(n => keepMenuLeaves(n))
      .filter(n => n.menuType === '菜单' || (n.children && n.children.length))
  }
  return []
})

// 仅保留 菜单 节点（按钮的父级）
function keepMenuLeaves(node: BaseMenu): BaseMenu {
  const children = (node.children || []).map(keepMenuLeaves).filter(c => c.menuType === '菜单' || (c.children && c.children.length))
  return { ...node, children }
}

// ============ 操作 ============
const openCreateDialog = (parent?: BaseMenu) => {
  isEdit.value = false
  form.value = emptyForm()
  if (parent) {
    form.value.parentId = parent.id
    // 默认子级类型推断
    if (parent.menuType === '目录') form.value.menuType = '菜单'
    else if (parent.menuType === '菜单') form.value.menuType = '按钮'
    else form.value.menuType = '目录'
  } else {
    form.value.parentId = '0'
    form.value.menuType = '目录'
  }
  dialogVisible.value = true
}

const openEditDialog = (row: BaseMenu) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    menuCode: row.menuCode,
    menuName: row.menuName,
    parentId: row.parentId || '0',
    menuType: row.menuType,
    path: row.path || '',
    component: row.component || '',
    icon: row.icon || '',
    sortOrder: row.sortOrder ?? 0,
    status: row.status || '启用',
    visible: row.visible || '是',
    perms: row.perms || '',
    children: []
  }
  dialogVisible.value = true
}

const handleTypeChange = () => {
  // 切换类型时，清空不适用的字段
  if (form.value.menuType === '目录') {
    form.value.path = ''
    form.value.component = ''
    form.value.perms = ''
    if (!isEdit.value) form.value.parentId = '0'
  } else if (form.value.menuType === '菜单') {
    if (!isEdit.value && form.value.parentId === '0') form.value.parentId = ''
  } else if (form.value.menuType === '按钮') {
    form.value.path = ''
    form.value.component = ''
    form.value.icon = ''
    if (!isEdit.value && form.value.parentId === '0') form.value.parentId = ''
  }
}

const saveMenu = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  if (form.value.menuType !== '目录' && (!form.value.parentId || form.value.parentId === '0')) {
    ElMessage.warning(form.value.menuType === '菜单' ? '请选择所属目录' : '请选择所属菜单')
    return
  }
  try {
    if (isEdit.value) {
      await updateMenu(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createMenu(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadMenus()
  } catch {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (row: BaseMenu) => {
  try {
    await ElMessageBox.confirm(
      `确定删除「${row.menuName}」？子节点将一并删除。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
    await deleteMenu(row.id)
    ElMessage.success('删除成功')
    loadMenus()
  } catch {
    /* 取消 */
  }
}

// 展开/折叠
const tableRef = ref()
const allExpanded = ref(true)
const toggleExpandAll = () => {
  allExpanded.value = !allExpanded.value
  const expandRows = (rows: BaseMenu[]) => {
    rows.forEach(r => {
      tableRef.value?.toggleRowExpansion(r, allExpanded.value)
      if (r.children?.length) expandRows(r.children)
    })
  }
  expandRows(menuTree.value)
}

onMounted(() => {
  loadMenus()
})
</script>

<template>
  <div class="page-container menu-config-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">菜单配置</h2>
        <p class="page-desc">管理系统菜单与导航结构 · 目录 / 菜单 / 按钮 三级层次</p>
      </div>
      <div class="header-actions">
        <el-button @click="toggleExpandAll">
          {{ allExpanded ? '全部折叠' : '全部展开' }}
        </el-button>
        <el-button type="primary" :icon="Plus" @click="openCreateDialog()">新增根目录</el-button>
      </div>
    </div>

    <!-- 类型图例 -->
    <div class="legend-bar">
      <span class="legend-item">
        <el-icon class="legend-icon legend-icon--dir"><Folder /></el-icon>
        <span class="legend-label">目录</span>
        <span class="legend-desc">第一层分组</span>
      </span>
      <span class="legend-divider" />
      <span class="legend-item">
        <el-icon class="legend-icon legend-icon--menu"><Document /></el-icon>
        <span class="legend-label">菜单</span>
        <span class="legend-desc">可访问页面</span>
      </span>
      <span class="legend-divider" />
      <span class="legend-item">
        <el-icon class="legend-icon legend-icon--btn"><Operation /></el-icon>
        <span class="legend-label">按钮</span>
        <span class="legend-desc">页面内权限点</span>
      </span>
    </div>

    <div class="table-card menu-table-card">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="menuTree"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
        :indent="22"
        stripe
        class="menu-tree-table"
      >
        <el-table-column label="名称" min-width="280">
          <template #default="{ row }">
            <div class="name-cell" :class="`name-cell--${row.menuType}`">
              <el-icon class="name-cell__icon">
                <component :is="typeMeta(row.menuType).icon" />
              </el-icon>
              <span class="name-cell__text">{{ row.menuName }}</span>
              <span v-if="row.menuCode" class="name-cell__code">{{ row.menuCode }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="110" align="center">
          <template #default="{ row }">
            <span :class="typeMeta(row.menuType).cls">{{ typeMeta(row.menuType).text }}</span>
          </template>
        </el-table-column>

        <el-table-column label="路由路径" min-width="180">
          <template #default="{ row }">
            <span v-if="row.menuType !== '按钮' && row.path" class="mono-text">{{ row.path }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>

        <el-table-column label="权限标识" min-width="200">
          <template #default="{ row }">
            <span v-if="row.perms" class="perm-text">{{ row.perms }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>

        <el-table-column label="排序" width="80" align="center" prop="sortOrder" />

        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <span :class="row.status === '启用' ? 'status-active' : 'status-inactive'">
              {{ row.status || '启用' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="240" align="right" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.menuType !== '按钮'"
              link
              type="primary"
              :icon="CirclePlus"
              @click="openCreateDialog(row)"
            >
              子级
            </el-button>
            <el-button link type="primary" :icon="Edit" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑菜单' : '新增菜单'"
      width="640px"
      :close-on-click-modal="false"
      class="menu-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="类型" prop="menuType">
          <el-radio-group v-model="form.menuType" :disabled="isEdit" @change="handleTypeChange">
            <el-radio-button value="目录">
              <el-icon><Folder /></el-icon>
              目录
            </el-radio-button>
            <el-radio-button value="菜单">
              <el-icon><Document /></el-icon>
              菜单
            </el-radio-button>
            <el-radio-button value="按钮">
              <el-icon><Operation /></el-icon>
              按钮
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 父级选择 -->
        <el-form-item
          v-if="form.menuType !== '目录'"
          :label="form.menuType === '菜单' ? '所属目录' : '所属菜单'"
        >
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'menuName', children: 'children', value: 'id' }"
            node-key="id"
            check-strictly
            default-expand-all
            placeholder="请选择父级"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="编码" prop="menuCode">
          <el-input v-model="form.menuCode" :disabled="isEdit" placeholder="如 MainData / BtnDataApprove" />
        </el-form-item>

        <el-form-item label="名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="显示名称" />
        </el-form-item>

        <!-- 目录/菜单：图标 -->
        <el-form-item v-if="form.menuType !== '按钮'" label="图标">
          <el-input v-model="form.icon" placeholder="Element Plus 图标名，如 House" />
        </el-form-item>

        <!-- 菜单：路径与组件 -->
        <template v-if="form.menuType === '菜单'">
          <el-form-item label="路由路径">
            <el-input v-model="form.path" placeholder="如 /main-data" />
          </el-form-item>
          <el-form-item label="组件路径">
            <el-input v-model="form.component" placeholder="如 views/MainData.vue" />
          </el-form-item>
        </template>

        <!-- 菜单/按钮：权限标识 -->
        <el-form-item v-if="form.menuType !== '目录'" label="权限标识">
          <el-input
            v-model="form.perms"
            :placeholder="form.menuType === '按钮' ? '如 btn:mainData:approve' : '如 menu:mainData:view'"
          />
        </el-form-item>

        <el-form-item label="排序号">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width: 200px" />
        </el-form-item>

        <el-form-item v-if="form.menuType !== '按钮'" label="是否可见">
          <el-radio-group v-model="form.visible">
            <el-radio value="是">显示</el-radio>
            <el-radio value="否">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="启用">启用</el-radio>
            <el-radio value="停用">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMenu">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.menu-config-page {
  .header-actions {
    display: flex;
    gap: 12px;
  }
}

/* 图例条 */
.legend-bar {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 18px;
  margin-bottom: 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  backdrop-filter: blur(10px);

  .legend-item {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
  }
  .legend-icon {
    font-size: 16px;
    &--dir { color: #60a5fa; }
    &--menu { color: #34d399; }
    &--btn { color: #fbbf24; }
  }
  .legend-label {
    color: var(--text-bright);
    font-weight: 500;
  }
  .legend-desc {
    color: var(--text-muted);
    font-size: 12px;
  }
  .legend-divider {
    width: 1px;
    height: 16px;
    background: var(--border-color);
  }
}

/* 表格容器 */
.menu-table-card {
  padding: 8px 0;
}

/* 名称单元格 */
.name-cell {
  display: inline-flex;
  align-items: center;
  gap: 10px;

  &__icon {
    font-size: 17px;
    flex-shrink: 0;
  }
  &__text {
    color: var(--text-primary);
    font-weight: 500;
  }
  &__code {
    color: var(--text-muted);
    font-size: 11px;
    font-family: var(--font-mono);
    padding: 1px 6px;
    border-radius: 4px;
    background: rgba(148, 163, 184, 0.08);
    border: 1px solid rgba(148, 163, 184, 0.15);
  }

  &--目录 {
    .name-cell__icon { color: #60a5fa; }
    .name-cell__text {
      color: var(--text-bright);
      font-weight: 600;
      letter-spacing: 0.3px;
    }
  }
  &--菜单 {
    .name-cell__icon { color: #34d399; }
  }
  &--按钮 {
    .name-cell__icon { color: #fbbf24; }
    .name-cell__text {
      color: var(--text-secondary);
      font-weight: 400;
    }
  }
}

/* 类型 tag —— 柔和配色 */
.type-tag {
  display: inline-block;
  min-width: 48px;
  text-align: center;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.5px;

  &--dir {
    color: #93c5fd;
    background: rgba(96, 165, 250, 0.12);
    border: 1px solid rgba(96, 165, 250, 0.3);
  }
  &--menu {
    color: #6ee7b7;
    background: rgba(52, 211, 153, 0.12);
    border: 1px solid rgba(52, 211, 153, 0.3);
  }
  &--btn {
    color: #fcd34d;
    background: rgba(251, 191, 36, 0.12);
    border: 1px solid rgba(251, 191, 36, 0.3);
  }
}

/* 文本样式 */
.mono-text {
  font-family: var(--font-mono);
  font-size: 12.5px;
  color: var(--text-secondary);
  padding: 1px 8px;
  border-radius: 4px;
  background: rgba(99, 102, 241, 0.08);
  border: 1px solid rgba(99, 102, 241, 0.15);
}
.perm-text {
  font-family: var(--font-mono);
  font-size: 12.5px;
  color: var(--color-info);
}
.text-muted {
  color: var(--text-muted);
  font-size: 13px;
}

/* 对话框中的类型 RadioButton */
.menu-dialog {
  :deep(.el-radio-button__inner) {
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }
}
</style>
