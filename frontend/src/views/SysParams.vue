<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getSysParams, updateParam } from '../api/system'
import type { SysParam } from '../api/system'

const params = ref<SysParam[]>([])
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = ref<SysParam>({
  id: '',
  paramKey: '',
  paramValue: '',
  paramName: '',
  paramType: '',
  description: '',
  status: '启用'
})

const rules: FormRules = {
  paramValue: [{ required: true, message: '请输入参数值', trigger: 'blur' }]
}

const loadParams = async () => {
  try {
    const res = await getSysParams()
    params.value = res.data.data
  } catch {
    ElMessage.error('加载系统参数失败')
  }
}

const openDialog = (param: SysParam) => {
  form.value = { ...param }
  dialogVisible.value = true
}

const saveParam = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    await updateParam(form.value.id, form.value)
    ElMessage.success('更新成功')
    dialogVisible.value = false
    loadParams()
  } catch {
    ElMessage.error('更新失败')
  }
}

const getParamTypeTag = (type: string) => {
  switch (type) {
    case '安全': return 'danger'
    case '显示': return 'primary'
    case '系统': return 'warning'
    default: return 'info'
  }
}

onMounted(() => {
  loadParams()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">系统参数</h2>
        <p class="page-desc">管理系统运行参数与配置项</p>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="params" stripe>
        <el-table-column prop="paramName" label="参数名称" min-width="160" />
        <el-table-column prop="paramKey" label="参数Key" min-width="180">
          <template #default="scope">
            <code style="color: var(--color-primary, #00d4ff); font-size: 13px;">{{ scope.row.paramKey }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="paramValue" label="参数值" min-width="140" />
        <el-table-column prop="paramType" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="getParamTypeTag(scope.row.paramType)" size="small">
              {{ scope.row.paramType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openDialog(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 编辑Dialog -->
    <el-dialog title="编辑参数" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="参数名称">
          <el-input v-model="form.paramName" disabled />
        </el-form-item>
        <el-form-item label="参数Key">
          <el-input v-model="form.paramKey" disabled />
        </el-form-item>
        <el-form-item label="参数值" prop="paramValue">
          <el-input v-model="form.paramValue" placeholder="请输入参数值" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveParam">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
</style>
