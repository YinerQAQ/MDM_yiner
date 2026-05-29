<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCodeRules, createCodeRule, updateCodeRule, deleteCodeRule,
  getSchemes, createScheme, updateScheme, deleteScheme,
  getSegments, createSegment, updateSegment, deleteSegment, generateCode
} from '../api/codeRules'

const rules = ref<any[]>([])
const selectedRule = ref<any>(null)
const schemes = ref<any[]>([])
const selectedScheme = ref<any>(null)
const segments = ref<any[]>([])

// 规则Dialog
const ruleDialogVisible = ref(false)
const isEditRule = ref(false)
const ruleForm = ref({ id: '', ruleCode: '', ruleName: '', status: '启用', description: '' })

// 方案Dialog
const schemeDialogVisible = ref(false)
const isEditScheme = ref(false)
const schemeForm = ref({ id: '', schemeName: '', preCondition: '', priority: 0 })

// 段Dialog
const segmentDialogVisible = ref(false)
const isEditSegment = ref(false)
const segmentForm = ref({ id: '', segmentType: '常量', segmentName: '', format: '', value: '', sortOrder: 0 })

// 测试生成
const testDialogVisible = ref(false)
const testContext = ref('{}')
const generatedCode = ref('')

const loadRules = async () => {
  try {
    const res = await getCodeRules()
    rules.value = res.data.data || []
  } catch {
    ElMessage.error('加载编码规则失败')
  }
}

const selectRule = async (rule: any) => {
  selectedRule.value = rule
  selectedScheme.value = null
  segments.value = []
  try {
    const res = await getSchemes(rule.id)
    schemes.value = res.data.data || []
  } catch {
    schemes.value = []
  }
}

const selectScheme = async (scheme: any) => {
  selectedScheme.value = scheme
  try {
    const res = await getSegments(scheme.id)
    segments.value = res.data.data || []
  } catch {
    segments.value = []
  }
}

// 规则CRUD
const openRuleDialog = (rule?: any) => {
  if (rule) {
    isEditRule.value = true
    ruleForm.value = { ...rule }
  } else {
    isEditRule.value = false
    ruleForm.value = { id: '', ruleCode: '', ruleName: '', status: '启用', description: '' }
  }
  ruleDialogVisible.value = true
}

const saveRule = async () => {
  try {
    if (isEditRule.value) {
      await updateCodeRule(ruleForm.value.id, ruleForm.value)
      ElMessage.success('更新成功')
    } else {
      await createCodeRule(ruleForm.value)
      ElMessage.success('创建成功')
    }
    ruleDialogVisible.value = false
    loadRules()
  } catch {
    ElMessage.error('保存失败')
  }
}

const handleDeleteRule = async (rule: any) => {
  try {
    await ElMessageBox.confirm(`确定删除规则"${rule.ruleName}"？`, '提示', { type: 'warning' })
    await deleteCodeRule(rule.id)
    ElMessage.success('删除成功')
    if (selectedRule.value?.id === rule.id) {
      selectedRule.value = null
      schemes.value = []
      selectedScheme.value = null
      segments.value = []
    }
    loadRules()
  } catch {
    // cancelled
  }
}

// 方案CRUD
const openSchemeDialog = (scheme?: any) => {
  if (!selectedRule.value) return
  if (scheme) {
    isEditScheme.value = true
    schemeForm.value = { ...scheme }
  } else {
    isEditScheme.value = false
    schemeForm.value = { id: '', schemeName: '', preCondition: '', priority: 0 }
  }
  schemeDialogVisible.value = true
}

const saveScheme = async () => {
  if (!selectedRule.value) return
  try {
    if (isEditScheme.value) {
      await updateScheme(schemeForm.value.id, schemeForm.value)
      ElMessage.success('更新成功')
    } else {
      await createScheme(selectedRule.value.id, schemeForm.value)
      ElMessage.success('创建成功')
    }
    schemeDialogVisible.value = false
    selectRule(selectedRule.value)
  } catch {
    ElMessage.error('保存失败')
  }
}

const handleDeleteScheme = async (scheme: any) => {
  try {
    await ElMessageBox.confirm('确定删除该方案？', '提示', { type: 'warning' })
    await deleteScheme(scheme.id)
    ElMessage.success('删除成功')
    if (selectedScheme.value?.id === scheme.id) {
      selectedScheme.value = null
      segments.value = []
    }
    selectRule(selectedRule.value)
  } catch {
    // cancelled
  }
}

// 段CRUD
const openSegmentDialog = (seg?: any) => {
  if (!selectedScheme.value) return
  if (seg) {
    isEditSegment.value = true
    segmentForm.value = { ...seg }
  } else {
    isEditSegment.value = false
    segmentForm.value = { id: '', segmentType: '常量', segmentName: '', format: '', value: '', sortOrder: segments.value.length }
  }
  segmentDialogVisible.value = true
}

const saveSegment = async () => {
  if (!selectedScheme.value) return
  try {
    if (isEditSegment.value) {
      await updateSegment(segmentForm.value.id, segmentForm.value)
      ElMessage.success('更新成功')
    } else {
      await createSegment(selectedScheme.value.id, segmentForm.value)
      ElMessage.success('创建成功')
    }
    segmentDialogVisible.value = false
    selectScheme(selectedScheme.value)
  } catch {
    ElMessage.error('保存失败')
  }
}

const handleDeleteSegment = async (seg: any) => {
  try {
    await ElMessageBox.confirm('确定删除该编码段？', '提示', { type: 'warning' })
    await deleteSegment(seg.id)
    ElMessage.success('删除成功')
    selectScheme(selectedScheme.value)
  } catch {
    // cancelled
  }
}

// 测试生成
const openTestDialog = () => {
  if (!selectedRule.value) {
    ElMessage.warning('请先选择编码规则')
    return
  }
  testContext.value = '{}'
  generatedCode.value = ''
  testDialogVisible.value = true
}

const doGenerate = async () => {
  if (!selectedRule.value) return
  try {
    let ctx: any = {}
    try {
      ctx = JSON.parse(testContext.value)
    } catch {
      ctx = {}
    }
    const res = await generateCode(selectedRule.value.id, ctx)
    generatedCode.value = res.data.data || ''
    ElMessage.success('生成成功')
  } catch {
    ElMessage.error('生成失败')
  }
}

onMounted(() => {
  loadRules()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">编码规则管理</h2>
        <p class="page-desc">定义和管理数据编码生成规则</p>
      </div>
      <el-button type="primary" @click="openRuleDialog()">新增规则</el-button>
    </div>

    <div style="display: flex; gap: 20px; height: calc(100% - 90px);">
      <!-- 左侧：规则列表 -->
      <div class="table-card" style="width: 380px; flex-shrink: 0; display: flex; flex-direction: column;">
        <div style="flex: 1; overflow: auto;">
          <el-table :data="rules" stripe highlight-current-row @current-change="selectRule">
            <el-table-column prop="ruleCode" label="编码" width="120" />
            <el-table-column prop="ruleName" label="名称" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.status === '启用' ? 'success' : 'info'" size="small">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="primary" size="small" link @click.stop="openRuleDialog(scope.row)">编辑</el-button>
                <el-button type="danger" size="small" link @click.stop="handleDeleteRule(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 右侧：详情 -->
      <div v-if="selectedRule" class="card-glow" style="flex: 1; padding: 20px; overflow: auto;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
          <h3 style="color: var(--text-bright);">{{ selectedRule.ruleName }} - 编码方案</h3>
          <div style="display: flex; gap: 8px;">
            <el-button type="primary" size="small" @click="openSchemeDialog()">新增方案</el-button>
            <el-button type="success" size="small" @click="openTestDialog">测试生成</el-button>
          </div>
        </div>

        <el-tabs type="card">
          <el-tab-pane label="编码方案">
            <el-table :data="schemes" stripe highlight-current-row @current-change="selectScheme">
              <el-table-column prop="schemeName" label="方案名称" />
              <el-table-column prop="preCondition" label="前置条件" show-overflow-tooltip />
              <el-table-column prop="priority" label="优先级" width="80" />
              <el-table-column label="操作" width="140">
                <template #default="scope">
                  <el-button type="primary" size="small" link @click.stop="openSchemeDialog(scope.row)">编辑</el-button>
                  <el-button type="danger" size="small" link @click.stop="handleDeleteScheme(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>

        <!-- 编码段 -->
        <div v-if="selectedScheme" style="margin-top: 20px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
            <h4 style="color: var(--text-bright);">{{ selectedScheme.schemeName }} - 编码段</h4>
            <el-button type="primary" size="small" @click="openSegmentDialog()">新增段</el-button>
          </div>
          <el-table :data="segments" stripe border>
            <el-table-column prop="segmentName" label="段名称" />
            <el-table-column prop="segmentType" label="段类型" width="100" />
            <el-table-column prop="format" label="格式" width="120" />
            <el-table-column prop="value" label="值/配置" show-overflow-tooltip />
            <el-table-column prop="sortOrder" label="排序" width="70" />
            <el-table-column label="操作" width="140">
              <template #default="scope">
                <el-button type="primary" size="small" link @click="openSegmentDialog(scope.row)">编辑</el-button>
                <el-button type="danger" size="small" link @click="handleDeleteSegment(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <div v-else class="card-glow" style="flex: 1; display: flex; align-items: center; justify-content: center;">
        <p style="color: var(--text-secondary);">请从左侧选择一个编码规则</p>
      </div>
    </div>

    <!-- 规则Dialog -->
    <el-dialog :title="isEditRule ? '编辑规则' : '新增规则'" v-model="ruleDialogVisible" width="500px">
      <el-form :model="ruleForm" label-width="100px">
        <el-form-item label="规则编码" required>
          <el-input v-model="ruleForm.ruleCode" placeholder="请输入规则编码" />
        </el-form-item>
        <el-form-item label="规则名称" required>
          <el-input v-model="ruleForm.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="ruleForm.status" style="width: 100%">
            <el-option label="启用" value="启用" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="ruleForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRule">保存</el-button>
      </template>
    </el-dialog>

    <!-- 方案Dialog -->
    <el-dialog :title="isEditScheme ? '编辑方案' : '新增方案'" v-model="schemeDialogVisible" width="500px">
      <el-form :model="schemeForm" label-width="100px">
        <el-form-item label="方案名称" required>
          <el-input v-model="schemeForm.schemeName" placeholder="请输入方案名称" />
        </el-form-item>
        <el-form-item label="前置条件">
          <el-input v-model="schemeForm.preCondition" placeholder="请输入前置条件表达式" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="schemeForm.priority" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="schemeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveScheme">保存</el-button>
      </template>
    </el-dialog>

    <!-- 段Dialog -->
    <el-dialog :title="isEditSegment ? '编辑段' : '新增段'" v-model="segmentDialogVisible" width="500px">
      <el-form :model="segmentForm" label-width="100px">
        <el-form-item label="段名称" required>
          <el-input v-model="segmentForm.segmentName" placeholder="请输入段名称" />
        </el-form-item>
        <el-form-item label="段类型">
          <el-select v-model="segmentForm.segmentType" style="width: 100%">
            <el-option label="常量" value="常量" />
            <el-option label="日期" value="日期" />
            <el-option label="流水号" value="流水号" />
            <el-option label="属性值" value="属性值" />
            <el-option label="随机码" value="随机码" />
          </el-select>
        </el-form-item>
        <el-form-item label="格式">
          <el-input v-model="segmentForm.format" placeholder="如：yyyyMMdd、0000" />
        </el-form-item>
        <el-form-item label="值/配置">
          <el-input v-model="segmentForm.value" placeholder="常量值或属性编码" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="segmentForm.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="segmentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSegment">保存</el-button>
      </template>
    </el-dialog>

    <!-- 测试生成Dialog -->
    <el-dialog title="测试编码生成" v-model="testDialogVisible" width="500px">
      <el-form label-width="100px">
        <el-form-item label="上下文参数">
          <el-input v-model="testContext" type="textarea" :rows="4" placeholder='{"attrCode":"value"}' />
        </el-form-item>
        <el-form-item label="生成结果" v-if="generatedCode">
          <div style="padding: 12px; background: var(--bg-card); border-radius: var(--radius-sm); color: var(--color-primary); font-family: monospace; font-size: 16px;">
            {{ generatedCode }}
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="testDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="doGenerate">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
</style>
