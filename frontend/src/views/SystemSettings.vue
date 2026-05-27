<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { QuestionFilled, Lock, Monitor, Setting } from '@element-plus/icons-vue'
import { getSysParams, updateParam } from '../api/system'
import type { SysParam } from '../api/system'

interface SettingGroup {
  title: string
  icon: any
  params: SysParam[]
}

const allParams = ref<SysParam[]>([])
const settingGroups = ref<SettingGroup[]>([])
const loading = ref(false)

const groupConfig = [
  { title: '安全设置', icon: Lock, keys: ['login.fail.max.count', 'login.lock.duration', 'password.min.length'] },
  { title: '显示设置', icon: Monitor, keys: ['watermark.enabled', 'quick.query.default.expand'] },
  { title: '其他设置', icon: Setting, keys: [] }
]

const loadParams = async () => {
  loading.value = true
  try {
    const res = await getSysParams()
    allParams.value = res.data.data
    buildGroups()
  } catch {
    ElMessage.error('加载系统参数失败')
  } finally {
    loading.value = false
  }
}

const buildGroups = () => {
  const grouped: SettingGroup[] = []
  const assignedKeys = new Set<string>()

  for (const config of groupConfig) {
    const groupParams = allParams.value.filter(p => config.keys.includes(p.paramKey))
    grouped.push({
      title: config.title,
      icon: config.icon,
      params: groupParams
    })
    config.keys.forEach(k => assignedKeys.add(k))
  }

  // 未匹配的参数放入"其他设置"
  const others = allParams.value.filter(p => !assignedKeys.has(p.paramKey))
  const otherGroup = grouped.find(g => g.title === '其他设置')
  if (otherGroup) {
    otherGroup.params = others
  }

  settingGroups.value = grouped.filter(g => g.params.length > 0)
}

const isBooleanParam = (value: string) => value === 'true' || value === 'false'

const isNumericParam = (param: SysParam) => {
  if (isNaN(Number(param.paramValue))) return false
  return param.paramKey.includes('count') || param.paramKey.includes('length') || param.paramKey.includes('duration')
}

const getNumericValue = (param: SysParam) => Number(param.paramValue) || 0

const setNumericValue = (param: SysParam, val: number | undefined) => {
  param.paramValue = String(val ?? 0)
}

const saveGroup = async (group: SettingGroup) => {
  try {
    for (const param of group.params) {
      await updateParam(param.id, param)
    }
    ElMessage.success(`${group.title}保存成功`)
  } catch {
    ElMessage.error('保存失败')
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
        <h2 class="page-title">系统设置</h2>
        <p class="page-desc">配置系统全局设置与偏好</p>
      </div>
    </div>

    <div v-loading="loading" style="display: flex; flex-direction: column; gap: 20px;">
      <div v-for="group in settingGroups" :key="group.title" class="card-glow" style="padding: 24px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
          <h3 style="color: var(--text-bright); font-size: 16px; display: flex; align-items: center; gap: 8px;">
            <el-icon :size="18"><component :is="group.icon" /></el-icon>
            {{ group.title }}
          </h3>
          <el-button type="primary" size="small" @click="saveGroup(group)">保存</el-button>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px;">
          <div
            v-for="param in group.params"
            :key="param.id"
            style="display: flex; flex-direction: column; gap: 6px;"
          >
            <label style="font-size: 13px; color: var(--text-secondary);">
              {{ param.paramName }}
              <el-tooltip v-if="param.description" :content="param.description" placement="top">
                <el-icon :size="12" style="margin-left: 4px; cursor: help;"><QuestionFilled /></el-icon>
              </el-tooltip>
            </label>

            <!-- 布尔参数用Switch -->
            <el-switch
              v-if="isBooleanParam(param.paramValue)"
              v-model="param.paramValue"
              active-value="true"
              inactive-value="false"
            />
            <!-- 数值参数用InputNumber -->
            <el-input-number
              v-else-if="isNumericParam(param)"
              :model-value="getNumericValue(param)"
              @update:model-value="(val: number | undefined) => setNumericValue(param, val)"
              :min="0"
              controls-position="right"
              style="width: 100%"
            />
            <!-- 其他用普通Input -->
            <el-input
              v-else
              v-model="param.paramValue"
              size="default"
            />
          </div>
        </div>
      </div>
    </div>

    <div v-if="!loading && settingGroups.length === 0" class="card-glow" style="padding: 60px; text-align: center;">
      <p style="color: var(--text-secondary);">暂无系统设置项</p>
    </div>
  </div>
</template>

<style scoped lang="scss">
</style>
