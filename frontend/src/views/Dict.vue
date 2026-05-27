<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getDicts, getDictItems, createDict, updateDict, deleteDict,
  createDictItem, updateDictItem, deleteDictItem
} from '../api/dict'
import type { BaseDict, BaseDictItem } from '../api/dict'

const dicts = ref<BaseDict[]>([])
const selectedDict = ref<BaseDict | null>(null)
const dictItems = ref<BaseDictItem[]>([])
const searchKeyword = ref('')

// 字典表单
const dictDialogVisible = ref(false)
const isEditDict = ref(false)
const dictFormRef = ref<FormInstance>()
const dictForm = ref<BaseDict>({
  id: '',
  dictCode: '',
  dictName: '',
  description: '',
  status: '启用'
})

const dictRules: FormRules = {
  dictCode: [{ required: true, message: '请输入字典编码', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 字典项表单
const itemDialogVisible = ref(false)
const isEditItem = ref(false)
const itemFormRef = ref<FormInstance>()
const itemForm = ref<BaseDictItem>({
  id: '',
  dictId: '',
  dictCode: '',
  itemValue: '',
  itemLabel: '',
  sortOrder: 0,
  status: '启用'
})

const itemRules: FormRules = {
  itemValue: [{ required: true, message: '请输入字典项值', trigger: 'blur' }],
  itemLabel: [{ required: true, message: '请输入字典项标签', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const loadDicts = async () => {
  try {
    const res = await getDicts()
    dicts.value = res.data.data
  } catch {
    ElMessage.error('加载字典列表失败')
  }
}

const loadDictItems = async (dictCode: string) => {
  try {
    const res = await getDictItems(dictCode)
    dictItems.value = res.data.data
  } catch {
    ElMessage.error('加载字典项失败')
  }
}

const selectDict = (dict: BaseDict) => {
  selectedDict.value = dict
  loadDictItems(dict.dictCode)
}

const filteredDicts = () => {
  if (!searchKeyword.value) return dicts.value
  return dicts.value.filter(d =>
    d.dictCode.includes(searchKeyword.value) ||
    d.dictName.includes(searchKeyword.value)
  )
}

// 字典CRUD
const openDictDialog = (dict?: BaseDict) => {
  if (dict) {
    isEditDict.value = true
    dictForm.value = { ...dict }
  } else {
    isEditDict.value = false
    dictForm.value = { id: '', dictCode: '', dictName: '', description: '', status: '启用' }
  }
  dictDialogVisible.value = true
}

const saveDict = async () => {
  if (!dictFormRef.value) return
  await dictFormRef.value.validate()
  try {
    if (isEditDict.value) {
      await updateDict(dictForm.value.id, dictForm.value)
      ElMessage.success('更新成功')
    } else {
      await createDict(dictForm.value)
      ElMessage.success('创建成功')
    }
    dictDialogVisible.value = false
    loadDicts()
  } catch {
    ElMessage.error('保存失败')
  }
}

const handleDeleteDict = async (dict: BaseDict) => {
  try {
    await ElMessageBox.confirm(`确定删除字典"${dict.dictName}"？`, '提示', { type: 'warning' })
    await deleteDict(dict.id)
    ElMessage.success('删除成功')
    if (selectedDict.value?.id === dict.id) {
      selectedDict.value = null
      dictItems.value = []
    }
    loadDicts()
  } catch {
    // cancelled
  }
}

// 字典项CRUD
const openItemDialog = (item?: BaseDictItem) => {
  if (!selectedDict.value) return
  if (item) {
    isEditItem.value = true
    itemForm.value = { ...item }
  } else {
    isEditItem.value = false
    itemForm.value = {
      id: '',
      dictId: selectedDict.value.id,
      dictCode: selectedDict.value.dictCode,
      itemValue: '',
      itemLabel: '',
      sortOrder: 0,
      status: '启用'
    }
  }
  itemDialogVisible.value = true
}

const saveDictItem = async () => {
  if (!itemFormRef.value) return
  await itemFormRef.value.validate()
  try {
    if (isEditItem.value) {
      await updateDictItem(itemForm.value.id, itemForm.value)
      ElMessage.success('更新成功')
    } else {
      await createDictItem(selectedDict.value!.id, itemForm.value)
      ElMessage.success('创建成功')
    }
    itemDialogVisible.value = false
    if (selectedDict.value) {
      loadDictItems(selectedDict.value.dictCode)
    }
  } catch {
    ElMessage.error('保存失败')
  }
}

const handleDeleteItem = async (item: BaseDictItem) => {
  try {
    await ElMessageBox.confirm('确定删除该字典项？', '提示', { type: 'warning' })
    await deleteDictItem(item.id)
    ElMessage.success('删除成功')
    if (selectedDict.value) {
      loadDictItems(selectedDict.value.dictCode)
    }
  } catch {
    // cancelled
  }
}

onMounted(() => {
  loadDicts()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">数据字典</h2>
        <p class="page-desc">管理系统数据字典与枚举值</p>
      </div>
      <el-button type="primary" @click="openDictDialog()">新增字典</el-button>
    </div>

    <div style="display: flex; gap: 20px; height: calc(100% - 90px);">
      <!-- 左侧：字典列表 -->
      <div class="table-card" style="width: 340px; flex-shrink: 0; display: flex; flex-direction: column;">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索字典编码/名称"
          clearable
          style="margin-bottom: 12px;"
        />
        <div style="flex: 1; overflow: auto;">
          <div
            v-for="dict in filteredDicts()"
            :key="dict.id"
            @click="selectDict(dict)"
            :class="['dict-item', { active: selectedDict?.id === dict.id }]"
          >
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <div>
                <div style="font-weight: 500; color: var(--text-bright);">{{ dict.dictName }}</div>
                <div style="font-size: 12px; color: var(--text-secondary);">{{ dict.dictCode }}</div>
              </div>
              <div>
                <el-button type="primary" size="small" link @click.stop="openDictDialog(dict)">编辑</el-button>
                <el-button type="danger" size="small" link @click.stop="handleDeleteDict(dict)">删除</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：字典项 -->
      <div class="card-glow" style="flex: 1; padding: 20px; overflow: auto;">
        <div v-if="selectedDict">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <h3 style="color: var(--text-bright);">{{ selectedDict.dictName }} - 字典项</h3>
            <el-button type="primary" size="small" @click="openItemDialog()">新增字典项</el-button>
          </div>
          <el-table :data="dictItems" stripe>
            <el-table-column prop="itemValue" label="值" />
            <el-table-column prop="itemLabel" label="标签" />
            <el-table-column prop="sortOrder" label="排序" width="80" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.status === '启用' ? 'success' : 'danger'" size="small">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140">
              <template #default="scope">
                <el-button type="primary" size="small" link @click="openItemDialog(scope.row)">编辑</el-button>
                <el-button type="danger" size="small" link @click="handleDeleteItem(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div v-else style="text-align: center; padding: 60px; color: var(--text-secondary);">
          <p>请从左侧选择一个字典查看详情</p>
        </div>
      </div>
    </div>

    <!-- 字典Dialog -->
    <el-dialog :title="isEditDict ? '编辑字典' : '新增字典'" v-model="dictDialogVisible" width="500px">
      <el-form ref="dictFormRef" :model="dictForm" :rules="dictRules" label-width="100px">
        <el-form-item label="字典编码" prop="dictCode">
          <el-input v-model="dictForm.dictCode" :disabled="isEditDict" placeholder="请输入字典编码" />
        </el-form-item>
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="dictForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="dictForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="dictForm.status" style="width: 100%">
            <el-option label="启用" value="启用" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dictDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDict">保存</el-button>
      </template>
    </el-dialog>

    <!-- 字典项Dialog -->
    <el-dialog :title="isEditItem ? '编辑字典项' : '新增字典项'" v-model="itemDialogVisible" width="500px">
      <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-width="100px">
        <el-form-item label="字典项值" prop="itemValue">
          <el-input v-model="itemForm.itemValue" placeholder="请输入字典项值" />
        </el-form-item>
        <el-form-item label="字典项标签" prop="itemLabel">
          <el-input v-model="itemForm.itemLabel" placeholder="请输入字典项标签" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="itemForm.sortOrder" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="itemForm.status" style="width: 100%">
            <el-option label="启用" value="启用" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDictItem">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.dict-item {
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;
  &:hover {
    background: rgba(0, 212, 255, 0.05);
  }
  &.active {
    background: rgba(0, 212, 255, 0.1);
    border-left: 3px solid var(--color-primary, #00d4ff);
  }
}
</style>
