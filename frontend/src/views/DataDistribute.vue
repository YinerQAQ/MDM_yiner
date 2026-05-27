<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getAllModels } from '../api/models'
import type { MdmDataModel } from '../api/types'
import {
  getDistByModel, createDist, updateDist, deleteDist,
  enableDist, disableDist, getDistContent, saveDistContent,
  executeDist, getSystems, createSystem
} from '../api/esb'

interface DistInterface {
  id: string
  name: string
  code: string
  modelId: string
  syncType: string
  dataType: string
  dataFormat: string
  targetUrl: string
  timeout: number
  retryCount: number
  status: string
  systemId?: string
}

interface SystemInfo {
  id: string
  systemCode: string
  systemName: string
  endpoint: string
  description: string
}

const models = ref<MdmDataModel[]>([])
const systems = ref<SystemInfo[]>([])
const selectedModelId = ref('')
const distList = ref<DistInterface[]>([])
const loading = ref(false)

// Dialog相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentStep = ref(0)
const formRef = ref<FormInstance>()
const form = ref<DistInterface>({
  id: '',
  name: '',
  code: '',
  modelId: '',
  syncType: '同步',
  dataType: '全量',
  dataFormat: 'JSON',
  targetUrl: '',
  timeout: 30,
  retryCount: 3,
  status: '停用',
  systemId: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入接口名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入接口编码', trigger: 'blur' }],
  syncType: [{ required: true, message: '请选择同步类型', trigger: 'change' }],
  dataFormat: [{ required: true, message: '请选择数据格式', trigger: 'change' }]
}

// 分发内容配置
const selectedAttrCodes = ref<string[]>([])

// 系统Dialog
const systemDialogVisible = ref(false)
const systemForm = ref<SystemInfo>({
  id: '',
  systemCode: '',
  systemName: '',
  endpoint: '',
  description: ''
})

// 执行Dialog
const execDialogVisible = ref(false)
const execDataIds = ref('')

const loadModels = async () => {
  try {
    const res = await getAllModels()
    models.value = res.data.data
  } catch {
    ElMessage.error('加载模型列表失败')
  }
}

const loadSystems = async () => {
  try {
    const res = await getSystems()
    systems.value = res.data.data || []
  } catch {
    // 可能后端还没实现，忽略
    systems.value = []
  }
}

const loadDistList = async () => {
  if (!selectedModelId.value) {
    distList.value = []
    return
  }
  loading.value = true
  try {
    const res = await getDistByModel(selectedModelId.value)
    distList.value = res.data.data || []
  } catch {
    distList.value = []
  } finally {
    loading.value = false
  }
}

watch(selectedModelId, () => {
  loadDistList()
})

const openDialog = (dist?: DistInterface) => {
  if (dist) {
    isEdit.value = true
    form.value = { ...dist }
    // 加载分发内容
    loadDistContent(dist.id)
  } else {
    isEdit.value = false
    form.value = {
      id: '',
      name: '',
      code: '',
      modelId: selectedModelId.value,
      syncType: '同步',
      dataType: '全量',
      dataFormat: 'JSON',
      targetUrl: '',
      timeout: 30,
      retryCount: 3,
      status: '停用',
      systemId: ''
    }
    selectedAttrCodes.value = []
  }
  currentStep.value = 0
  dialogVisible.value = true
}

const loadDistContent = async (id: string) => {
  try {
    const res = await getDistContent(id)
    const content = res.data.data
    if (content && content.attributes) {
      selectedAttrCodes.value = content.attributes
    } else {
      selectedAttrCodes.value = []
    }
  } catch {
    selectedAttrCodes.value = []
  }
}

const saveDist = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    currentStep.value = 0
    return
  }

  try {
    if (isEdit.value) {
      await updateDist(form.value.id, form.value)
      // 保存分发内容
      if (selectedAttrCodes.value.length > 0) {
        await saveDistContent(form.value.id, { attributes: selectedAttrCodes.value })
      }
      ElMessage.success('更新成功')
    } else {
      form.value.modelId = selectedModelId.value
      const res = await createDist(form.value)
      const newId = res.data.data?.id || res.data.data
      // 保存分发内容
      if (newId && selectedAttrCodes.value.length > 0) {
        await saveDistContent(newId, { attributes: selectedAttrCodes.value })
      }
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadDistList()
  } catch {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm('确定删除该分发接口？', '提示', { type: 'warning' })
    await deleteDist(id)
    ElMessage.success('删除成功')
    loadDistList()
  } catch {
    // cancelled
  }
}

const handleToggleStatus = async (dist: DistInterface) => {
  try {
    if (dist.status === '启用') {
      await disableDist(dist.id)
      ElMessage.success('已停用')
    } else {
      await enableDist(dist.id)
      ElMessage.success('已启用')
    }
    loadDistList()
  } catch {
    ElMessage.error('操作失败')
  }
}

// 新建系统
const openSystemDialog = () => {
  systemForm.value = { id: '', systemCode: '', systemName: '', endpoint: '', description: '' }
  systemDialogVisible.value = true
}

const saveSystem = async () => {
  if (!systemForm.value.systemCode || !systemForm.value.systemName) {
    ElMessage.error('请填写必填字段')
    return
  }
  try {
    const res = await createSystem(systemForm.value)
    ElMessage.success('系统创建成功')
    systemDialogVisible.value = false
    loadSystems()
    // 自动选择新系统
    if (res.data.data?.id) {
      form.value.systemId = res.data.data.id
    }
  } catch {
    ElMessage.error('创建系统失败')
  }
}

// 执行分发
const openExecDialog = (dist: DistInterface) => {
  execDataIds.value = ''
  execDialogVisible.value = true
  ;(window as any).__currentDistId = dist.id
}

const handleExecute = async () => {
  const distId = (window as any).__currentDistId
  if (!distId) return
  const ids = execDataIds.value.split('\n').map(s => s.trim()).filter(Boolean)
  if (ids.length === 0) {
    ElMessage.error('请输入数据ID')
    return
  }
  try {
    await executeDist(distId, ids)
    ElMessage.success('分发执行成功')
    execDialogVisible.value = false
  } catch {
    ElMessage.error('分发执行失败')
  }
}

const getStatusClass = (status: string) => {
  return status === '启用' ? 'status-active' : 'status-inactive'
}

const currentModelName = computed(() => {
  if (!selectedModelId.value) return ''
  const m = models.value.find(m => m.id === selectedModelId.value)
  return m?.modelName || ''
})

onMounted(() => {
  loadModels()
  loadSystems()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">数据分发</h2>
        <p class="page-desc">配置和管理数据分发接口</p>
      </div>
      <el-button type="primary" :disabled="!selectedModelId" @click="openDialog()">新建分发接口</el-button>
    </div>

    <!-- 顶部选择模型 -->
    <div class="search-card">
      <div style="display: flex; align-items: center; gap: 16px;">
        <span style="color: var(--text-secondary); font-size: 14px; white-space: nowrap;">选择模型：</span>
        <el-select
          v-model="selectedModelId"
          placeholder="请选择数据模型"
          style="width: 320px;"
          clearable
        >
          <el-option
            v-for="m in models"
            :key="m.id"
            :label="m.modelName"
            :value="m.id"
          />
        </el-select>
        <span v-if="currentModelName" style="color: var(--text-muted); font-size: 13px;">
          已选择：{{ currentModelName }}
        </span>
      </div>
    </div>

    <!-- 接口列表 -->
    <div class="table-card" v-if="selectedModelId">
      <el-table :data="distList" stripe v-loading="loading">
        <el-table-column prop="name" label="接口名称" min-width="120" />
        <el-table-column prop="code" label="接口编码" width="140" />
        <el-table-column prop="syncType" label="类型" width="80">
          <template #default="scope">
            <span :class="scope.row.syncType === '同步' ? 'status-approved' : 'status-pending'">
              {{ scope.row.syncType }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="dataType" label="同步方式" width="90" />
        <el-table-column prop="dataFormat" label="数据格式" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <span :class="getStatusClass(scope.row.status)">{{ scope.row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" link @click="openDialog(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(scope.row.id)">删除</el-button>
            <el-button
              :type="scope.row.status === '启用' ? 'warning' : 'success'"
              size="small"
              link
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === '启用' ? '停用' : '启用' }}
            </el-button>
            <el-button type="primary" size="small" link @click="openExecDialog(scope.row)">执行</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 空状态 -->
    <div v-else class="card-glow" style="padding: 60px; text-align: center;">
      <p style="color: var(--text-secondary); font-size: 15px;">请先选择一个数据模型查看分发接口</p>
    </div>

    <!-- 新增/编辑Dialog - 步骤表单 -->
    <el-dialog
      :title="isEdit ? '编辑分发接口' : '新建分发接口'"
      v-model="dialogVisible"
      width="640px"
      :close-on-click-modal="false"
    >
      <el-steps :active="currentStep" finish-status="success" simple style="margin-bottom: 20px;">
        <el-step title="基本配置" />
        <el-step title="分发内容" />
        <el-step title="目标系统" />
      </el-steps>

      <!-- Step1: 基本配置 -->
      <div v-show="currentStep === 0">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="接口名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入接口名称" />
          </el-form-item>
          <el-form-item label="接口编码" prop="code">
            <el-input v-model="form.code" placeholder="请输入接口编码" :disabled="isEdit" />
          </el-form-item>
          <el-form-item label="同步类型" prop="syncType">
            <el-select v-model="form.syncType" style="width: 100%">
              <el-option label="同步" value="同步" />
              <el-option label="异步" value="异步" />
            </el-select>
          </el-form-item>
          <el-form-item label="同步方式">
            <el-select v-model="form.dataType" style="width: 100%">
              <el-option label="全量" value="全量" />
              <el-option label="增量" value="增量" />
            </el-select>
          </el-form-item>
          <el-form-item label="数据格式" prop="dataFormat">
            <el-select v-model="form.dataFormat" style="width: 100%">
              <el-option label="JSON" value="JSON" />
              <el-option label="XML" value="XML" />
              <el-option label="CSV" value="CSV" />
            </el-select>
          </el-form-item>
          <el-form-item label="目标URL">
            <el-input v-model="form.targetUrl" placeholder="请输入目标URL" />
          </el-form-item>
          <el-form-item label="超时(秒)">
            <el-input-number v-model="form.timeout" :min="5" :max="300" style="width: 100%" />
          </el-form-item>
          <el-form-item label="重试次数">
            <el-input-number v-model="form.retryCount" :min="0" :max="10" style="width: 100%" />
          </el-form-item>
        </el-form>
      </div>

      <!-- Step2: 分发内容 -->
      <div v-show="currentStep === 1">
        <div style="margin-bottom: 12px; color: var(--text-secondary); font-size: 13px;">
          选择参与分发的模型属性字段：
        </div>
        <div v-if="selectedAttrCodes.length > 0" style="margin-bottom: 12px;">
          <el-tag
            v-for="code in selectedAttrCodes"
            :key="code"
            closable
            size="small"
            style="margin: 2px 4px;"
            @close="selectedAttrCodes = selectedAttrCodes.filter(c => c !== code)"
          >
            {{ code }}
          </el-tag>
        </div>
        <div class="attr-input-area">
          <el-input
            placeholder="输入属性编码后回车添加"
            size="small"
            @keyup.enter="(e: any) => {
              const val = e.target?.value?.trim()
              if (val && !selectedAttrCodes.includes(val)) {
                selectedAttrCodes.push(val)
              }
              if (e.target) e.target.value = ''
            }"
          />
        </div>
        <div style="margin-top: 12px; color: var(--text-muted); font-size: 12px;">
          提示：输入属性编码后按回车添加，如 code、name、status
        </div>
      </div>

      <!-- Step3: 目标系统 -->
      <div v-show="currentStep === 2">
        <el-form :model="form" label-width="100px">
          <el-form-item label="目标系统">
            <el-select v-model="form.systemId" placeholder="选择目标系统" style="width: 100%" clearable>
              <el-option
                v-for="sys in systems"
                :key="sys.id"
                :label="sys.systemName"
                :value="sys.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <div style="text-align: center; margin: 12px 0;">
          <el-button type="primary" size="small" link @click="openSystemDialog">新建信息系统</el-button>
        </div>
        <div v-if="systems.length === 0" style="text-align: center; color: var(--text-muted); font-size: 13px; padding: 20px;">
          暂无信息系统，请先创建
        </div>
      </div>

      <template #footer>
        <el-button v-if="currentStep > 0" @click="currentStep--">上一步</el-button>
        <el-button v-if="currentStep < 2" type="primary" @click="currentStep++">下一步</el-button>
        <el-button v-if="currentStep === 2" type="primary" @click="saveDist">保存</el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 新建系统Dialog -->
    <el-dialog title="新建信息系统" v-model="systemDialogVisible" width="480px">
      <el-form :model="systemForm" label-width="100px">
        <el-form-item label="系统编码" required>
          <el-input v-model="systemForm.systemCode" placeholder="请输入系统编码" />
        </el-form-item>
        <el-form-item label="系统名称" required>
          <el-input v-model="systemForm.systemName" placeholder="请输入系统名称" />
        </el-form-item>
        <el-form-item label="接口地址">
          <el-input v-model="systemForm.endpoint" placeholder="请输入接口地址" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="systemForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="systemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSystem">保存</el-button>
      </template>
    </el-dialog>

    <!-- 执行分发Dialog -->
    <el-dialog title="手动执行分发" v-model="execDialogVisible" width="480px">
      <div style="margin-bottom: 12px; color: var(--text-secondary); font-size: 13px;">
        输入需要分发的数据ID，每行一个：
      </div>
      <el-input
        v-model="execDataIds"
        type="textarea"
        :rows="6"
        placeholder="请输入数据ID，每行一个"
      />
      <template #footer>
        <el-button @click="execDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleExecute">执行分发</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.attr-input-area {
  max-width: 400px;
}
</style>
