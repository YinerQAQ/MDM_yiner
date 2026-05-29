<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import {
  ElTree,
  ElButton,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElSelect,
  ElOption,
  ElEmpty,
  ElMessage,
  ElMessageBox,
  ElDialog
} from 'element-plus'
import type Node from 'element-plus/es/components/tree/src/model/node'

interface CategoryNode {
  id: string
  parentId: string | null
  code: string
  name: string
  description: string
  sort: number
  children?: CategoryNode[]
}

const treeRef = ref<InstanceType<typeof ElTree>>()
const tree = ref<CategoryNode[]>([])
const selected = ref<CategoryNode | null>(null)
const editForm = reactive<CategoryNode>({
  id: '',
  parentId: null,
  code: '',
  name: '',
  description: '',
  sort: 0
})

const addDialogVisible = ref(false)
const addForm = reactive<CategoryNode>({
  id: '',
  parentId: null,
  code: '',
  name: '',
  description: '',
  sort: 0
})

const buildMock = (): CategoryNode[] => [
  {
    id: 'C001',
    parentId: null,
    code: 'PRODUCT',
    name: '产品分类',
    description: '所有产品类型主分类',
    sort: 1,
    children: [
      {
        id: 'C001-01',
        parentId: 'C001',
        code: 'PROD_HARDWARE',
        name: '硬件产品',
        description: '服务器、存储、网络设备等',
        sort: 1,
        children: [
          { id: 'C001-01-01', parentId: 'C001-01', code: 'PROD_SERVER', name: '服务器', description: '机架/塔式/刀片', sort: 1 },
          { id: 'C001-01-02', parentId: 'C001-01', code: 'PROD_STORAGE', name: '存储设备', description: 'SAN/NAS', sort: 2 }
        ]
      },
      {
        id: 'C001-02',
        parentId: 'C001',
        code: 'PROD_SOFTWARE',
        name: '软件产品',
        description: '操作系统、中间件、应用',
        sort: 2,
        children: [
          { id: 'C001-02-01', parentId: 'C001-02', code: 'PROD_OS', name: '操作系统', description: 'Linux / Windows', sort: 1 },
          { id: 'C001-02-02', parentId: 'C001-02', code: 'PROD_MIDWARE', name: '中间件', description: '数据库 / 消息队列', sort: 2 }
        ]
      }
    ]
  },
  {
    id: 'C002',
    parentId: null,
    code: 'CUSTOMER',
    name: '客户分类',
    description: '按行业 / 规模划分客户',
    sort: 2,
    children: [
      { id: 'C002-01', parentId: 'C002', code: 'CUST_GOV', name: '政府客户', description: '政府机关、事业单位', sort: 1 },
      { id: 'C002-02', parentId: 'C002', code: 'CUST_FIN', name: '金融客户', description: '银行、证券、保险', sort: 2 },
      { id: 'C002-03', parentId: 'C002', code: 'CUST_ENTER', name: '企业客户', description: '中大型工商企业', sort: 3 }
    ]
  },
  {
    id: 'C003',
    parentId: null,
    code: 'SUPPLIER',
    name: '供应商分类',
    description: '按供应类型划分',
    sort: 3,
    children: [
      { id: 'C003-01', parentId: 'C003', code: 'SUP_RAW', name: '原材料供应商', description: '钢材、塑料等', sort: 1 },
      { id: 'C003-02', parentId: 'C003', code: 'SUP_SVC', name: '服务供应商', description: '物流、咨询等', sort: 2 }
    ]
  }
]

const loadTree = () => {
  tree.value = buildMock()
}

// 扁平所有节点 - 用于"上级分类"下拉
const flatten = (nodes: CategoryNode[], result: CategoryNode[] = []): CategoryNode[] => {
  for (const n of nodes) {
    result.push(n)
    if (n.children && n.children.length > 0) flatten(n.children, result)
  }
  return result
}
const allNodes = computed(() => flatten(tree.value))

const findNode = (id: string, list: CategoryNode[] = tree.value): CategoryNode | null => {
  for (const n of list) {
    if (n.id === id) return n
    if (n.children) {
      const r = findNode(id, n.children)
      if (r) return r
    }
  }
  return null
}

const findParentList = (id: string, list: CategoryNode[] = tree.value): CategoryNode[] | null => {
  for (let i = 0; i < list.length; i++) {
    if (list[i].id === id) return list
    if (list[i].children) {
      const r = findParentList(id, list[i].children!)
      if (r) return r
    }
  }
  return null
}

const handleSelect = (data: CategoryNode) => {
  selected.value = data
  editForm.id = data.id
  editForm.parentId = data.parentId
  editForm.code = data.code
  editForm.name = data.name
  editForm.description = data.description
  editForm.sort = data.sort
}

const saveEdit = () => {
  if (!selected.value) return
  if (!editForm.code || !editForm.name) {
    ElMessage.error('编码和名称不能为空')
    return
  }
  // 上级变更：实际场景需重新挂载
  if (editForm.parentId !== selected.value.parentId) {
    const parentList = findParentList(selected.value.id)
    if (!parentList) return
    const idx = parentList.findIndex(n => n.id === selected.value!.id)
    if (idx > -1) {
      const node = parentList.splice(idx, 1)[0]
      Object.assign(node, { ...editForm })
      if (editForm.parentId == null) {
        tree.value.push(node)
      } else {
        const newParent = findNode(editForm.parentId)
        if (newParent) {
          newParent.children = newParent.children || []
          newParent.children.push(node)
        }
      }
    }
  } else {
    Object.assign(selected.value, {
      code: editForm.code,
      name: editForm.name,
      description: editForm.description,
      sort: editForm.sort
    })
  }
  ElMessage.success('保存成功')
}

const openAdd = (parent?: CategoryNode) => {
  Object.assign(addForm, {
    id: '',
    parentId: parent ? parent.id : null,
    code: '',
    name: '',
    description: '',
    sort: 1
  })
  addDialogVisible.value = true
}

const submitAdd = () => {
  if (!addForm.code || !addForm.name) {
    ElMessage.error('编码和名称不能为空')
    return
  }
  const newNode: CategoryNode = {
    id: `C${Date.now()}`,
    parentId: addForm.parentId,
    code: addForm.code,
    name: addForm.name,
    description: addForm.description,
    sort: addForm.sort,
    children: []
  }
  if (addForm.parentId == null) {
    tree.value.push(newNode)
  } else {
    const parent = findNode(addForm.parentId)
    if (parent) {
      parent.children = parent.children || []
      parent.children.push(newNode)
    }
  }
  ElMessage.success('新增成功')
  addDialogVisible.value = false
}

const removeNode = async (data: CategoryNode) => {
  try {
    await ElMessageBox.confirm(`确认删除分类【${data.name}】及其全部下级节点？`, '提示', {
      type: 'warning'
    })
    const parentList = findParentList(data.id)
    if (parentList) {
      const idx = parentList.findIndex(n => n.id === data.id)
      if (idx > -1) parentList.splice(idx, 1)
    }
    if (selected.value?.id === data.id) {
      selected.value = null
    }
    ElMessage.success('已删除')
  } catch {
    /* cancelled */
  }
}

const allowDrop = (_drag: Node, drop: Node, type: 'inner' | 'prev' | 'next'): boolean => {
  // 简单允许全部位置
  return type !== 'inner' || !!drop
}

const handleDragEnd = () => {
  ElMessage.success('已更新顺序')
}

const breadcrumb = computed(() => {
  if (!selected.value) return []
  const result: CategoryNode[] = []
  let cur: CategoryNode | null = selected.value
  while (cur) {
    result.unshift(cur)
    cur = cur.parentId ? findNode(cur.parentId) : null
  }
  return result
})

onMounted(loadTree)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">分类配置</h2>
        <p class="page-desc">维护各业务域的分类层级，支持拖拽排序</p>
      </div>
      <ElButton type="primary" @click="openAdd()">新增根分类</ElButton>
    </div>

    <div class="cat-layout">
      <!-- 左侧 树 -->
      <div class="card-glow cat-tree">
        <div class="cat-tree-head">
          <span>分类树</span>
        </div>
        <ElTree
          ref="treeRef"
          :data="tree"
          node-key="id"
          default-expand-all
          draggable
          :allow-drop="allowDrop"
          :expand-on-click-node="false"
          @node-click="handleSelect"
          @node-drag-end="handleDragEnd"
        >
          <template #default="{ data }">
            <div class="tree-row">
              <span class="tree-name">{{ data.name }}</span>
              <span class="tree-code">{{ data.code }}</span>
              <span class="tree-actions">
                <ElButton link size="small" type="primary" @click.stop="openAdd(data)">+ 子级</ElButton>
                <ElButton link size="small" type="danger" @click.stop="removeNode(data)">删除</ElButton>
              </span>
            </div>
          </template>
        </ElTree>
      </div>

      <!-- 右侧 编辑 -->
      <div class="card-glow cat-detail">
        <div v-if="selected" class="cat-detail-inner">
          <div class="cat-breadcrumb">
            <span v-for="(b, idx) in breadcrumb" :key="b.id">
              <span :class="{ active: idx === breadcrumb.length - 1 }">{{ b.name }}</span>
              <span v-if="idx < breadcrumb.length - 1" class="sep">/</span>
            </span>
          </div>
          <ElForm label-width="100px" :model="editForm">
            <ElFormItem label="分类编码" required>
              <ElInput v-model="editForm.code" />
            </ElFormItem>
            <ElFormItem label="分类名称" required>
              <ElInput v-model="editForm.name" />
            </ElFormItem>
            <ElFormItem label="上级分类">
              <ElSelect v-model="editForm.parentId as any" placeholder="作为根分类" clearable style="width:100%">
                <ElOption
                  v-for="n in allNodes.filter(n => n.id !== selected!.id)"
                  :key="n.id"
                  :label="n.name"
                  :value="n.id"
                />
              </ElSelect>
            </ElFormItem>
            <ElFormItem label="排序">
              <ElInputNumber v-model="editForm.sort" :min="0" :max="999" style="width:100%" />
            </ElFormItem>
            <ElFormItem label="描述">
              <ElInput v-model="editForm.description" type="textarea" :rows="3" />
            </ElFormItem>
            <ElFormItem>
              <ElButton type="primary" @click="saveEdit">保存修改</ElButton>
              <ElButton @click="handleSelect(selected)">还原</ElButton>
            </ElFormItem>
          </ElForm>
        </div>
        <ElEmpty v-else description="请在左侧选择一个分类节点" />
      </div>
    </div>

    <!-- 新增分类 Dialog -->
    <ElDialog v-model="addDialogVisible" title="新增分类" width="480px">
      <ElForm label-width="100px" :model="addForm">
        <ElFormItem label="上级分类">
          <ElSelect v-model="addForm.parentId as any" placeholder="作为根分类" clearable style="width:100%">
            <ElOption
              v-for="n in allNodes"
              :key="n.id"
              :label="n.name"
              :value="n.id"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="分类编码" required>
          <ElInput v-model="addForm.code" placeholder="如 PROD_NEW" />
        </ElFormItem>
        <ElFormItem label="分类名称" required>
          <ElInput v-model="addForm.name" />
        </ElFormItem>
        <ElFormItem label="排序">
          <ElInputNumber v-model="addForm.sort" :min="0" :max="999" style="width:100%" />
        </ElFormItem>
        <ElFormItem label="描述">
          <ElInput v-model="addForm.description" type="textarea" :rows="3" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="addDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="submitAdd">保存</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped lang="scss">
.cat-layout {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 20px;
  height: calc(100vh - 200px);
}
.cat-tree {
  padding: 16px;
  overflow: auto;
}
.cat-tree-head {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 10px;
  border-bottom: 1px dashed var(--border-color);
  padding-bottom: 8px;
}
.cat-detail {
  padding: 24px;
  overflow: auto;
}
.cat-detail-inner { max-width: 720px; }
.cat-breadcrumb {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 18px;
  .sep { margin: 0 6px; color: var(--text-muted); }
  .active { color: var(--color-primary); font-weight: 500; }
}
.tree-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  .tree-name { color: var(--text-primary); font-weight: 500; }
  .tree-code {
    color: var(--text-muted);
    font-size: 12px;
    font-family: var(--font-mono);
  }
  .tree-actions {
    margin-left: auto;
    opacity: 0;
    transition: opacity 0.15s ease;
  }
  &:hover .tree-actions { opacity: 1; }
}
:deep(.el-tree) {
  background: transparent;
  color: var(--text-primary);
}
:deep(.el-tree-node__content:hover) {
  background: rgba(0, 212, 255, 0.06);
}
:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: rgba(0, 212, 255, 0.12);
}
</style>
