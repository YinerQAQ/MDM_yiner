<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  ElDialog,
  ElUpload,
  ElButton,
  ElRadioGroup,
  ElRadioButton,
  ElTable,
  ElTableColumn,
  ElMessage,
  ElTag,
  type UploadFile
} from 'element-plus'
import { importMainData, downloadImportTemplate } from '../api/mainData'
import type { ImportResultResponse } from '../api/types'

const props = defineProps<{
  modelValue: boolean
  modelId: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const fileList = ref<UploadFile[]>([])
const importType = ref('NORMAL')
const loading = ref(false)
const importResult = ref<ImportResultResponse | null>(null)

const handleUploadChange = (_file: UploadFile, uploadFileList: UploadFile[]) => {
  fileList.value = uploadFileList.slice(-1)
}

const handleImport = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择要导入的Excel文件')
    return
  }
  if (!props.modelId) {
    ElMessage.warning('请先选择模型')
    return
  }

  loading.value = true
  importResult.value = null
  try {
    const formData = new FormData()
    formData.append('file', fileList.value[0].raw!)
    const response = await importMainData(props.modelId, formData, importType.value)
    importResult.value = response.data.data
    if (importResult.value && importResult.value.failCount === 0) {
      ElMessage.success(`导入成功，共导入 ${importResult.value.successCount} 条数据`)
      emit('success')
    }
  } catch (error) {
    ElMessage.error('导入失败')
  } finally {
    loading.value = false
  }
}

const handleDownloadTemplate = async () => {
  if (!props.modelId) {
    ElMessage.warning('请先选择模型')
    return
  }
  try {
    await downloadImportTemplate(props.modelId)
    ElMessage.success('模板下载成功')
  } catch (error) {
    ElMessage.error('模板下载失败')
  }
}

const handleClose = () => {
  fileList.value = []
  importResult.value = null
  visible.value = false
}
</script>

<template>
  <ElDialog title="数据导入" v-model="visible" width="650px" @close="handleClose">
    <div class="import-content">
      <div class="import-actions">
        <ElButton size="small" @click="handleDownloadTemplate">下载导入模板</ElButton>
      </div>

      <ElUpload
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :file-list="fileList"
        :on-change="handleUploadChange"
        drag
      >
        <div class="upload-tip">
          <p>将Excel文件拖到此处，或<em>点击上传</em></p>
          <p class="sub-tip">仅支持 .xlsx / .xls 格式</p>
        </div>
      </ElUpload>

      <div class="import-type">
        <span class="label">导入类型：</span>
        <ElRadioGroup v-model="importType">
          <ElRadioButton value="NORMAL">普通导入（需审核）</ElRadioButton>
          <ElRadioButton value="INIT">初始化导入（直接生效）</ElRadioButton>
        </ElRadioGroup>
      </div>

      <div v-if="importResult" class="import-result">
        <div class="result-summary">
          <ElTag type="success" size="large">成功: {{ importResult.successCount }}</ElTag>
          <ElTag type="danger" size="large">失败: {{ importResult.failCount }}</ElTag>
        </div>
        <div v-if="importResult.failures && importResult.failures.length > 0" class="failure-detail">
          <h4>失败明细：</h4>
          <ElTable :data="importResult.failures" stripe size="small" max-height="250">
            <ElTableColumn prop="rowIndex" label="行号" width="80" />
            <ElTableColumn prop="reason" label="失败原因" />
          </ElTable>
        </div>
      </div>
    </div>

    <template #footer>
      <ElButton @click="handleClose">取消</ElButton>
      <ElButton type="primary" :loading="loading" @click="handleImport">开始导入</ElButton>
    </template>
  </ElDialog>
</template>

<style scoped lang="scss">
.import-content {
  .import-actions {
    margin-bottom: 12px;
  }

  .upload-tip {
    text-align: center;
    color: #909399;
    p { margin: 4px 0; }
    em { color: #409eff; font-style: normal; }
    .sub-tip { font-size: 12px; }
  }

  .import-type {
    margin-top: 16px;
    display: flex;
    align-items: center;
    .label { margin-right: 8px; white-space: nowrap; }
  }

  .import-result {
    margin-top: 16px;
    .result-summary {
      display: flex;
      gap: 12px;
      margin-bottom: 12px;
    }
    .failure-detail {
      h4 { margin: 0 0 8px; color: #f56c6c; }
    }
  }
}
</style>