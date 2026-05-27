<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getModelById, updateModel, getModelAttributes, addModelAttribute,
  updateModelAttribute, deleteModelAttribute, getModelConstraints,
  createConstraint, updateConstraint, deleteConstraint, publishModel, submitModelForReview
} from '../api/models'
import { getCodeRules } from '../api/codeRules'
import type { MdmDataModel } from '../api/types'

const route = useRoute()
const router = useRouter()
const modelId = ref(route.query.id as string || '')

const activeStep = ref(0)
const model = ref<MdmDataModel | null>(null)
const codeRules = ref<any[]>([])

// Step1 基本信息
const basicFormRef = ref<FormInstance>()
const basicForm = ref({
  modelCode: '',
  modelName: '',
  modelType: '普通',
  subjectDomain: '',
  entryMode: '手工录入',
  description: ''
})
const basicRules: FormRules = {
  modelCode: [{ required: true, message: '请输入模型编码', trigger: 'blur' }],
  modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  modelType: [{ required: true, message: '请选择模型类型', trigger: 'change' }]
}

// Step2 属性配置
const attributes = ref<any[]>([])
const attrDialogVisible = ref(false)
const isEditAttr = ref(false)
const attrForm = ref({
  id: '',
  attributeCode: '',
  attributeName: '',
  dataType: '文本',
  displayType: '输入框',
  isRequired: 0,
  maxLength: 50,
  defaultValue: '',
  sortOrder: 0
})

// Step3 约束规则
const constraints = ref<any[]>([])
const constraintDialogVisible = ref(false)
const isEditConstraint = ref(false)
const constraintForm = ref({
  id: '',
  constraintType: '唯一性',
  constraintName: '',
  severity: '错误',
  scope: '全局',
  configJson: '{}'
})

// Step4 编码绑定
const selectedCodeRuleId = ref('')

const loadModel = async () => {
  if (!modelId.value) return
  try {
    const res = await getModelById(modelId.value)
    model.value = res.data.data
    basicForm.value = {
      modelCode: model.value.modelCode || '',
      modelName: model.value.modelName || '',
      modelType: model.value.modelType || '普通',
      subjectDomain: (model.value as any).subjectDomain || '',
      entryMode: (model.value as any).entryMode || '手工录入',
      description: model.value.description || ''
    }
  } catch {
    ElMessage.error('加载模型失败')
  }
}

const loadAttributes = async () => {
  if (!modelId.value) return
  try {
    const res = await getModelAttributes(modelId.value)
    attributes.value = res.data.data || []
  } catch {
    ElMessage.error('加载属性失败')
  }
}

const loadConstraints = async () => {
  if (!modelId.value) return
  try {
    const res = await getModelConstraints(modelId.value)
    constraints.value = res.data.data || []
  } catch {
    ElMessage.error('加载约束失败')
  }
}

const loadCodeRules = async () => {
  try {
    const res = await getCodeRules()
    codeRules.value = res.data.data || []
  } catch {
    // ignore
  }
}

// 属性CRUD
const openAttrDialog = (attr?: any) => {
  if (attr) {
    isEditAttr.value = true
    attrForm.value = { ...attr }
  } else {
    isEditAttr.value = false
    attrForm.value = {
      id: '',
      attributeCode: '',
      attributeName: '',
      dataType: '文本',
      displayType: '输入框',
      isRequired: 0,
      maxLength: 50,
      defaultValue: '',
      sortOrder: attributes.value.length
    }
  }
  attrDialogVisible.value = true
}

const saveAttr = async () => {
  if (!modelId.value) return
  try {
    if (isEditAttr.value) {
      await updateModelAttribute(modelId.value, attrForm.value.id, attrForm.value)
      ElMessage.success('更新成功')
    } else {
      await addModelAttribute(modelId.value, attrForm.value)
      ElMessage.success('添加成功')
    }
    attrDialogVisible.value = false
    loadAttributes()
  } catch {
    ElMessage.error('保存失败')
  }
}

const handleDeleteAttr = async (attr: any) => {
  try {
    await ElMessageBox.confirm('确定删除该属性？', '提示', { type: 'warning' })
    await deleteModelAttribute(modelId.value, attr.id)
    ElMessage.success('删除成功')
    loadAttributes()
  } catch {
    // cancelled
  }
}

// 约束CRUD
const openConstraintDialog = (c?: any) => {
  if (c) {
    isEditConstraint.value = true
    constraintForm.value = { ...c }
  } else {
    isEditConstraint.value = false
    constraintForm.value = {
      id: '',
      constraintType: '唯一性',
      constraintName: '',
      severity: '错误',
      scope: '全局',
      configJson: '{}'
    }
  }
  constraintDialogVisible.value = true
}

const saveConstraint = async () => {
  if (!modelId.value) return
  try {
    if (isEditConstraint.value) {
      await updateConstraint(modelId.value, constraintForm.value.id, constraintForm.value)
      ElMessage.success('更新成功')
    } else {
      await createConstraint(modelId.value, constraintForm.value)
      ElMessage.success('添加成功')
    }
    constraintDialogVisible.value = false
    loadConstraints()
  } catch {
    ElMessage.error('保存失败')
  }
}

const handleDeleteConstraint = async (c: any) => {
  try {
    await ElMessageBox.confirm('确定删除该约束？', '提示', { type: 'warning' })
    await deleteConstraint(modelId.value, c.id)
    ElMessage.success('删除成功')
    loadConstraints()
  } catch {
    // cancelled
  }
}

// 步骤控制
const nextStep = async () => {
  if (activeStep.value === 0) {
    if (!basicFormRef.value) return
    await basicFormRef.value.validate()
  }
  if (activeStep.value < 3) activeStep.value++
}

const prevStep = () => {
  if (activeStep.value > 0) activeStep.value--
}

const saveModel = async () => {
  if (!modelId.value) return
  try {
    await updateModel(modelId.value, { ...basicForm.value })
    ElMessage.success('保存成功')
    loadModel()
  } catch {
    ElMessage.error('保存失败')
  }
}

const submitForReview = async () => {
  if (!modelId.value) return
  try {
    await submitModelForReview(modelId.value)
    ElMessage.success('已提交审核')
    router.push('/data-models')
  } catch {
    ElMessage.error('提交失败')
  }
}

const publish = async () => {
  if (!modelId.value) return
  try {
    await publishModel(modelId.value)
    ElMessage.success('已发布')
    router.push('/data-models')
  } catch {
    ElMessage.error('发布失败')
  }
}

onMounted(() => {
  loadModel()
  loadAttributes()
  loadConstraints()
  loadCodeRules()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">模型设计器</h2>
        <p class="page-desc">可视化设计数据模型结构与规则</p>
      </div>
      <el-button @click="router.push('/data-models')">返回列表</el-button>
    </div>

    <div class="table-card" style="padding: 20px;">
      <!-- 步骤条 -->
      <el-steps :active="activeStep" finish-status="success" simple style="margin-bottom: 24px;">
        <el-step title="基本信息" />
        <el-step title="属性配置" />
        <el-step title="约束规则" />
        <el-step title="编码绑定" />
      </el-steps>

      <!-- Step1 基本信息 -->
      <div v-if="activeStep === 0">
        <el-form ref="basicFormRef" :model="basicForm" :rules="basicRules" label-width="120px">
          <el-form-item label="模型编码" prop="modelCode">
            <el-input v-model="basicForm.modelCode" placeholder="请输入模型编码" />
          </el-form-item>
          <el-form-item label="模型名称" prop="modelName">
            <el-input v-model="basicForm.modelName" placeholder="请输入模型名称" />
          </el-form-item>
          <el-form-item label="模型类型" prop="modelType">
            <el-select v-model="basicForm.modelType" style="width: 100%">
              <el-option label="普通模型" value="普通" />
              <el-option label="类别模型" value="类别" />
              <el-option label="引用分类模型" value="引用分类" />
            </el-select>
          </el-form-item>
          <el-form-item label="主题域">
            <el-input v-model="basicForm.subjectDomain" placeholder="请输入主题域" />
          </el-form-item>
          <el-form-item label="录入方式">
            <el-select v-model="basicForm.entryMode" style="width: 100%">
              <el-option label="手工录入" value="手工录入" />
              <el-option label="导入" value="导入" />
              <el-option label="接口同步" value="接口同步" />
            </el-select>
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="basicForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
          </el-form-item>
        </el-form>
      </div>

      <!-- Step2 属性配置 -->
      <div v-if="activeStep === 1">
        <div style="margin-bottom: 12px; text-align: right;">
          <el-button type="primary" @click="openAttrDialog()">新增属性</el-button>
        </div>
        <el-table :data="attributes" stripe border>
          <el-table-column prop="attributeCode" label="属性编码" width="140" />
          <el-table-column prop="attributeName" label="属性名称" width="140" />
          <el-table-column prop="dataType" label="数据类型" width="100" />
          <el-table-column prop="displayType" label="展示方式" width="100" />
          <el-table-column prop="isRequired" label="必填" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.isRequired === 1 ? 'danger' : 'info'" size="small">
                {{ scope.row.isRequired === 1 ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="maxLength" label="长度" width="80" />
          <el-table-column prop="defaultValue" label="默认值" show-overflow-tooltip />
          <el-table-column label="操作" width="160">
            <template #default="scope">
              <el-button type="primary" size="small" link @click="openAttrDialog(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" link @click="handleDeleteAttr(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Step3 约束规则 -->
      <div v-if="activeStep === 2">
        <div style="margin-bottom: 12px; text-align: right;">
          <el-button type="primary" @click="openConstraintDialog()">新增约束</el-button>
        </div>
        <el-table :data="constraints" stripe border>
          <el-table-column prop="constraintName" label="约束名称" />
          <el-table-column prop="constraintType" label="约束类型" width="120" />
          <el-table-column prop="severity" label="严重度" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.severity === '错误' ? 'danger' : 'warning'" size="small">
                {{ scope.row.severity }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="scope" label="作用范围" width="100" />
          <el-table-column label="操作" width="160">
            <template #default="scope">
              <el-button type="primary" size="small" link @click="openConstraintDialog(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" link @click="handleDeleteConstraint(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Step4 编码绑定 -->
      <div v-if="activeStep === 3">
        <el-form label-width="120px">
          <el-form-item label="选择编码规则">
            <el-select v-model="selectedCodeRuleId" style="width: 100%" clearable placeholder="请选择编码规则">
              <el-option
                v-for="rule in codeRules"
                :key="rule.id"
                :label="rule.ruleName"
                :value="rule.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <div v-if="selectedCodeRuleId" style="margin-top: 16px; padding: 16px; background: var(--bg-card); border-radius: var(--radius-sm);">
          <p style="color: var(--text-secondary); font-size: 13px;">已绑定编码规则，保存后生效。</p>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div style="margin-top: 24px; display: flex; justify-content: center; gap: 12px;">
        <el-button v-if="activeStep > 0" @click="prevStep">上一步</el-button>
        <el-button v-if="activeStep < 3" type="primary" @click="nextStep">下一步</el-button>
        <el-button type="primary" plain @click="saveModel">保存</el-button>
        <el-button type="success" @click="submitForReview">提交审核</el-button>
      </div>
    </div>

    <!-- 属性Dialog -->
    <el-dialog :title="isEditAttr ? '编辑属性' : '新增属性'" v-model="attrDialogVisible" width="560px">
      <el-form :model="attrForm" label-width="100px">
        <el-form-item label="属性编码" required>
          <el-input v-model="attrForm.attributeCode" placeholder="请输入属性编码" />
        </el-form-item>
        <el-form-item label="属性名称" required>
          <el-input v-model="attrForm.attributeName" placeholder="请输入属性名称" />
        </el-form-item>
        <el-form-item label="数据类型">
          <el-select v-model="attrForm.dataType" style="width: 100%">
            <el-option label="文本" value="文本" />
            <el-option label="大文本" value="大文本" />
            <el-option label="整数" value="整数" />
            <el-option label="小数" value="小数" />
            <el-option label="日期" value="日期" />
            <el-option label="下拉" value="下拉" />
            <el-option label="多选" value="多选" />
          </el-select>
        </el-form-item>
        <el-form-item label="展示方式">
          <el-select v-model="attrForm.displayType" style="width: 100%">
            <el-option label="输入框" value="输入框" />
            <el-option label="文本域" value="文本域" />
            <el-option label="下拉选择" value="下拉选择" />
            <el-option label="日期选择" value="日期选择" />
            <el-option label="数字输入" value="数字输入" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否必填">
          <el-radio-group v-model="attrForm.isRequired">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="最大长度">
          <el-input-number v-model="attrForm.maxLength" :min="1" :max="4000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="默认值">
          <el-input v-model="attrForm.defaultValue" placeholder="请输入默认值，下拉/多选可填JSON选项" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="attrForm.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="attrDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAttr">保存</el-button>
      </template>
    </el-dialog>

    <!-- 约束Dialog -->
    <el-dialog :title="isEditConstraint ? '编辑约束' : '新增约束'" v-model="constraintDialogVisible" width="560px">
      <el-form :model="constraintForm" label-width="100px">
        <el-form-item label="约束类型">
          <el-select v-model="constraintForm.constraintType" style="width: 100%">
            <el-option label="唯一性" value="唯一性" />
            <el-option label="必填" value="必填" />
            <el-option label="格式" value="格式" />
            <el-option label="范围" value="范围" />
            <el-option label="关联" value="关联" />
          </el-select>
        </el-form-item>
        <el-form-item label="约束名称" required>
          <el-input v-model="constraintForm.constraintName" placeholder="请输入约束名称" />
        </el-form-item>
        <el-form-item label="严重度">
          <el-select v-model="constraintForm.severity" style="width: 100%">
            <el-option label="错误" value="错误" />
            <el-option label="警告" value="警告" />
          </el-select>
        </el-form-item>
        <el-form-item label="作用范围">
          <el-select v-model="constraintForm.scope" style="width: 100%">
            <el-option label="全局" value="全局" />
            <el-option label="组织内" value="组织内" />
            <el-option label="记录级" value="记录级" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置JSON">
          <el-input v-model="constraintForm.configJson" type="textarea" :rows="4" placeholder="请输入约束配置JSON" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="constraintDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveConstraint">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
</style>
