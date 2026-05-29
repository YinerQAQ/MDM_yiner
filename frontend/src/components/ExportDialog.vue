<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  ElDialog,
  ElCheckboxGroup,
  ElCheckbox,
  ElRadioGroup,
  ElRadioButton,
  ElButton,
  ElMessage
} from 'element-plus'
import { getModelAttributes, exportMainData } from '../api/mainData'
import type { MdmModelAttribute } from '../api/types'

const props = defineProps<{
  modelValue: boolean
  modelId: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const attributes = ref<MdmModelAttribute[]>([])
const selectedFields = ref<string[]>([])
const exportFormat = ref('EXCEL')
const loading = ref(false)
const selectAll = ref(false)

const loadAttributes = async () => {
  if (!props.modelId) return
  try {
    const response = await getModelAttributes(props.modelId)
    attributes.value = response.data.data || []
  } catch (error) {
    ElMessage.error('加载属性列表失败')
  }
}

const handleSelectAll = (val: any) => {
  selectedFields.value = val ? attributes.value.map(a => a.id) : []
}

const handleFieldChange = () => {
  selectAll.value = selectedFields.value.length === attributes.value.length
}

const handleExport = async () => {
  if (!props.modelId) {
    ElMessage.warning('请先选择模型')
    return
  }

  loading.value = true
  try {
    await exportMainData(props.modelId, selectedFields.value, exportFormat.value)
    ElMessage.success('导出成功')
    visible.value = false
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAttributes()
})
</script>

<template>
  <ElDialog title="数据导出" v-model="visible" width="500px">
    <div class="export-content">
      <div class="field-select">
        <div class="field-header">
          <span>选择导出字段：</span>
          <ElCheckbox v-model="selectAll" @change="handleSelectAll">全选</ElCheckbox>
        </div>
        <ElCheckboxGroup v-model="selectedFields" @change="handleFieldChange">
          <div class="field-grid">
            <ElCheckbox
              v-for="attr in attributes"
              :key="attr.id"
              :value="attr.id"
              :label="attr.attributeName"
            />
          </div>
        </ElCheckboxGroup>
        <p class="hint">不选择则导出全部字段</p>
      </div>

      <div class="format-select">
        <span class="label">导出格式：</span>
        <ElRadioGroup v-model="exportFormat">
          <ElRadioButton value="EXCEL">Excel</ElRadioButton>
          <ElRadioButton value="CSV">CSV</ElRadioButton>
        </ElRadioGroup>
      </div>
    </div>

    <template #footer>
      <ElButton @click="visible = false">取消</ElButton>
      <ElButton type="primary" :loading="loading" @click="handleExport">导出</ElButton>
    </template>
  </ElDialog>
</template>

<style scoped lang="scss">
.export-content {
  .field-select {
    .field-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
    }

    .field-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 4px 12px;
      max-height: 200px;
      overflow-y: auto;
      border: 1px solid #ebeef5;
      border-radius: 4px;
      padding: 8px 12px;
    }

    .hint {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }
  }

  .format-select {
    margin-top: 16px;
    display: flex;
    align-items: center;
    .label { margin-right: 8px; white-space: nowrap; }
  }
}
</style>