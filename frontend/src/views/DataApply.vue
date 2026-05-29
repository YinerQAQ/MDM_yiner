<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllModels, getModelAttributes } from '../api/models'
import {
  createMainData, updateMainData, deleteMainData, submitData, getMainDataByModelId
} from '../api/mainData'
import type { MdmDataModel } from '../api/types'
import DynamicForm from '../components/DynamicForm.vue'
import StatusTag from '../components/StatusTag.vue'

const models = ref<MdmDataModel[]>([])
const selectedModelId = ref('')
const attributes = ref<any[]>([])
const formData = ref<Record<string, any>>({})
const draftList = ref<any[]>([])

const isEdit = ref(false)
const editId = ref('')

const loadModels = async () => {
  try {
    const res = await getAllModels()
    models.value = res.data.data.filter((m: MdmDataModel) => m.status === '已发布')
  } catch {
    ElMessage.error('加载模型失败')
  }
}

const loadAttributes = async () => {
  if (!selectedModelId.value) {
    attributes.value = []
    return
  }
  try {
    const res = await getModelAttributes(selectedModelId.value)
    attributes.value = res.data.data || []
  } catch {
    attributes.value = []
  }
}

const loadDraftList = async () => {
  if (!selectedModelId.value) {
    draftList.value = []
    return
  }
  try {
    const res = await getMainDataByModelId(selectedModelId.value)
    draftList.value = (res.data.data || []).filter((d: any) => d.dataStatus === '暂存')
  } catch {
    draftList.value = []
  }
}

watch(selectedModelId, () => {
  loadAttributes()
  loadDraftList()
  resetForm()
})

const resetForm = () => {
  formData.value = {}
  isEdit.value = false
  editId.value = ''
}

const dynamicFormRef = ref<InstanceType<typeof DynamicForm>>()

const saveDraft = async () => {
  if (!selectedModelId.value) {
    ElMessage.warning('请选择数据模型')
    return
  }
  const valid = await dynamicFormRef.value?.validate()
  if (!valid) return

  try {
    const payload = {
      modelId: selectedModelId.value,
      jsonData: JSON.stringify(formData.value),
      dataStatus: '暂存',
      flowStatus: 'DRAFT'
    }
    if (isEdit.value && editId.value) {
      await updateMainData(editId.value, payload as any)
      ElMessage.success('更新成功')
    } else {
      await createMainData(payload as any)
      ElMessage.success('保存成功')
    }
    resetForm()
    loadDraftList()
  } catch {
    ElMessage.error('保存失败')
  }
}

const submitForReview = async () => {
  if (!selectedModelId.value) {
    ElMessage.warning('请选择数据模型')
    return
  }
  const valid = await dynamicFormRef.value?.validate()
  if (!valid) return

  try {
    let id = editId.value
    if (!id) {
      const payload = {
        modelId: selectedModelId.value,
        jsonData: JSON.stringify(formData.value),
        dataStatus: '暂存',
        flowStatus: 'DRAFT'
      }
      const res = await createMainData(payload as any)
      id = res.data.data?.id || ''
    } else {
      const payload = {
        modelId: selectedModelId.value,
        jsonData: JSON.stringify(formData.value),
        dataStatus: '暂存',
        flowStatus: 'DRAFT'
      }
      await updateMainData(id, payload as any)
    }
    if (id) {
      await submitData(id)
      ElMessage.success('提交审核成功')
      resetForm()
      loadDraftList()
    }
  } catch {
    ElMessage.error('提交失败')
  }
}

const editDraft = (row: any) => {
  isEdit.value = true
  editId.value = row.id
  try {
    formData.value = JSON.parse(row.jsonData || '{}')
  } catch {
    formData.value = {}
  }
}

const deleteDraft = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定删除该暂存数据？', '提示', { type: 'warning' })
    await deleteMainData(row.id)
    ElMessage.success('删除成功')
    loadDraftList()
    if (editId.value === row.id) resetForm()
  } catch {
    // cancelled
  }
}

const submitDraft = async (row: any) => {
  try {
    await submitData(row.id)
    ElMessage.success('提交审核成功')
    loadDraftList()
    if (editId.value === row.id) resetForm()
  } catch {
    ElMessage.error('提交失败')
  }
}

onMounted(() => {
  loadModels()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">数据申请</h2>
        <p class="page-desc">提交主数据新增或变更申请</p>
      </div>
    </div>

    <div style="display: flex; gap: 20px; height: calc(100% - 90px);">
      <!-- 左侧：动态表单 -->
      <div class="card-glow" style="flex: 1; padding: 20px; overflow: auto;">
        <div style="margin-bottom: 20px;">
          <span style="color: var(--text-secondary); margin-right: 8px;">选择模型：</span>
          <el-select v-model="selectedModelId" style="width: 280px;" placeholder="请选择数据模型" clearable>
            <el-option
              v-for="model in models"
              :key="model.id"
              :label="model.modelName"
              :value="model.id"
            />
          </el-select>
        </div>

        <div v-if="selectedModelId">
          <DynamicForm
            ref="dynamicFormRef"
            :attributes="attributes"
            v-model="formData"
          />
          <div style="margin-top: 24px; display: flex; justify-content: center; gap: 12px;">
            <el-button @click="resetForm">重置</el-button>
            <el-button type="primary" plain @click="saveDraft">保存（暂存）</el-button>
            <el-button type="success" @click="submitForReview">提交审核</el-button>
          </div>
        </div>
        <div v-else style="text-align: center; padding: 60px; color: var(--text-secondary);">
          <p>请先选择数据模型</p>
        </div>
      </div>

      <!-- 右侧：暂存列表 -->
      <div class="table-card" style="width: 360px; flex-shrink: 0; display: flex; flex-direction: column;">
        <h3 style="color: var(--text-bright); margin-bottom: 12px;">暂存数据</h3>
        <div style="flex: 1; overflow: auto;">
          <div v-if="draftList.length === 0" style="text-align: center; padding: 40px; color: var(--text-secondary);">
            暂无暂存数据
          </div>
          <div
            v-for="item in draftList"
            :key="item.id"
            :class="['draft-item', { active: editId === item.id }]"
            @click="editDraft(item)"
          >
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <div>
                <div style="font-weight: 500; color: var(--text-bright);">{{ item.code || '未生成编码' }}</div>
                <div style="font-size: 12px; color: var(--text-secondary); margin-top: 4px;">
                  {{ item.createTime }}
                </div>
              </div>
              <div>
                <StatusTag :status="item.dataStatus" />
              </div>
            </div>
            <div style="margin-top: 8px; display: flex; gap: 8px; justify-content: flex-end;">
              <el-button type="primary" size="small" link @click.stop="editDraft(item)">编辑</el-button>
              <el-button type="success" size="small" link @click.stop="submitDraft(item)">提交</el-button>
              <el-button type="danger" size="small" link @click.stop="deleteDraft(item)">删除</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.draft-item {
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 8px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  &:hover {
    border-color: var(--border-glow);
  }
  &.active {
    border-color: var(--color-primary);
    background: rgba(0, 212, 255, 0.05);
  }
}
</style>
