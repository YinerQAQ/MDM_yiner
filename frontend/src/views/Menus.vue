<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getAllMenus, createMenu, updateMenu, deleteMenu } from '../api/menus'
import type { BaseMenu } from '../api/menus'

const menuTree = ref<BaseMenu[]>([])
const selectedMenu = ref<BaseMenu | null>(null)
const formRef = ref<FormInstance>()
const isEdit = ref(false)

const form = ref<BaseMenu>({
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
  children: []
})

const rules: FormRules = {
  menuCode: [{ required: true, message: '请输入菜单编码', trigger: 'blur' }],
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const loadMenus = async () => {
  try {
    const res = await getAllMenus()
    menuTree.value = res.data.data
  } catch {
    ElMessage.error('加载菜单树失败')
  }
}

const handleNodeClick = (node: BaseMenu) => {
  selectedMenu.value = node
  isEdit.value = true
  form.value = {
    id: node.id,
    menuCode: node.menuCode,
    menuName: node.menuName,
    parentId: node.parentId,
    menuType: node.menuType,
    path: node.path,
    component: node.component,
    icon: node.icon,
    sortOrder: node.sortOrder,
    status: node.status,
    visible: node.visible,
    children: []
  }
}

const addRootMenu = () => {
  selectedMenu.value = null
  isEdit.value = false
  form.value = {
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
    children: []
  }
}

const addChildMenu = () => {
  if (!selectedMenu.value) {
    ElMessage.warning('请先选择父菜单')
    return
  }
  isEdit.value = false
  form.value = {
    id: '',
    menuCode: '',
    menuName: '',
    parentId: selectedMenu.value.id,
    menuType: '菜单',
    path: '',
    component: '',
    icon: '',
    sortOrder: 0,
    status: '启用',
    visible: '是',
    children: []
  }
}

const saveMenu = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await updateMenu(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createMenu(form.value)
      ElMessage.success('创建成功')
    }
    loadMenus()
  } catch {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async () => {
  if (!selectedMenu.value) return
  try {
    await ElMessageBox.confirm('确定删除该菜单？子菜单将一并删除。', '提示', { type: 'warning' })
    await deleteMenu(selectedMenu.value.id)
    ElMessage.success('删除成功')
    selectedMenu.value = null
    loadMenus()
  } catch {
    // cancelled
  }
}

onMounted(() => {
  loadMenus()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">菜单配置</h2>
        <p class="page-desc">管理系统菜单与导航结构</p>
      </div>
      <div>
        <el-button type="primary" @click="addRootMenu">新增根菜单</el-button>
        <el-button type="success" @click="addChildMenu" :disabled="!selectedMenu">新增子菜单</el-button>
      </div>
    </div>

    <div style="display: flex; gap: 20px; height: calc(100% - 90px);">
      <!-- 左侧：菜单树 -->
      <div class="table-card" style="width: 320px; flex-shrink: 0; overflow: auto;">
        <el-tree
          :data="menuTree"
          :props="{ label: 'menuName', children: 'children' }"
          node-key="id"
          highlight-current
          default-expand-all
          @node-click="handleNodeClick"
        >
          <template #default="{ data }">
            <span style="display: flex; align-items: center; gap: 6px;">
              <el-icon v-if="data.icon" :size="14"><component :is="data.icon" /></el-icon>
              <span>{{ data.menuName }}</span>
              <el-tag v-if="data.menuType" size="small" type="info" style="margin-left: 4px;">{{ data.menuType }}</el-tag>
            </span>
          </template>
        </el-tree>
      </div>

      <!-- 右侧：编辑表单 -->
      <div class="card-glow" style="flex: 1; padding: 20px; overflow: auto;">
        <div v-if="selectedMenu || form.id !== undefined">
          <h3 style="margin-bottom: 20px; color: var(--text-bright);">
            {{ isEdit ? '编辑菜单' : '新增菜单' }}
          </h3>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 600px;">
            <el-form-item label="菜单编码" prop="menuCode">
              <el-input v-model="form.menuCode" :disabled="isEdit" placeholder="请输入菜单编码" />
            </el-form-item>
            <el-form-item label="菜单名称" prop="menuName">
              <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
            </el-form-item>
            <el-form-item label="菜单类型" prop="menuType">
              <el-select v-model="form.menuType" placeholder="请选择类型" style="width: 100%">
                <el-option label="目录" value="目录" />
                <el-option label="菜单" value="菜单" />
                <el-option label="按钮" value="按钮" />
              </el-select>
            </el-form-item>
            <el-form-item label="路由路径">
              <el-input v-model="form.path" placeholder="如 /users" />
            </el-form-item>
            <el-form-item label="组件路径">
              <el-input v-model="form.component" placeholder="如 views/Users.vue" />
            </el-form-item>
            <el-form-item label="图标">
              <el-input v-model="form.icon" placeholder="图标名称" />
            </el-form-item>
            <el-form-item label="排序号">
              <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width: 100%" />
            </el-form-item>
            <el-form-item label="是否可见">
              <el-select v-model="form.visible" style="width: 100%">
                <el-option label="是" value="是" />
                <el-option label="否" value="否" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="启用" value="启用" />
                <el-option label="停用" value="停用" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveMenu">保存</el-button>
              <el-button v-if="isEdit" type="danger" @click="handleDelete">删除</el-button>
            </el-form-item>
          </el-form>
        </div>
        <div v-else style="text-align: center; padding: 60px; color: var(--text-secondary);">
          <p>请从左侧选择一个菜单进行编辑，或点击"新增根菜单"创建新菜单</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
</style>
