<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    label-width="120px"
    class="dynamic-form"
  >
    <el-form-item
      v-for="attr in attributes"
      :key="attr.id"
      :label="attr.attributeName"
      :prop="attr.attributeCode"
    >
      <!-- 文本 -->
      <el-input
        v-if="attr.dataType === '文本'"
        v-model="formData[attr.attributeCode]"
        :placeholder="`请输入${attr.attributeName}`"
        :maxlength="attr.maxLength"
        clearable
      />
      <!-- 大文本 -->
      <el-input
        v-else-if="attr.dataType === '大文本'"
        v-model="formData[attr.attributeCode]"
        type="textarea"
        :rows="3"
        :placeholder="`请输入${attr.attributeName}`"
        :maxlength="attr.maxLength"
      />
      <!-- 整数 -->
      <el-input-number
        v-else-if="attr.dataType === '整数'"
        v-model="formData[attr.attributeCode]"
        :placeholder="`请输入${attr.attributeName}`"
        style="width: 100%"
        :controls="false"
      />
      <!-- 小数 -->
      <el-input-number
        v-else-if="attr.dataType === '小数'"
        v-model="formData[attr.attributeCode]"
        :placeholder="`请输入${attr.attributeName}`"
        style="width: 100%"
        :precision="2"
        :controls="false"
      />
      <!-- 日期 -->
      <el-date-picker
        v-else-if="attr.dataType === '日期'"
        v-model="formData[attr.attributeCode]"
        type="date"
        :placeholder="`请选择${attr.attributeName}`"
        style="width: 100%"
        value-format="YYYY-MM-DD"
      />
      <!-- 下拉 -->
      <el-select
        v-else-if="attr.dataType === '下拉'"
        v-model="formData[attr.attributeCode]"
        :placeholder="`请选择${attr.attributeName}`"
        style="width: 100%"
        clearable
      >
        <el-option
          v-for="opt in parseOptions(attr.defaultValue)"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <!-- 多选 -->
      <el-select
        v-else-if="attr.dataType === '多选'"
        v-model="formData[attr.attributeCode]"
        multiple
        :placeholder="`请选择${attr.attributeName}`"
        style="width: 100%"
        clearable
      >
        <el-option
          v-for="opt in parseOptions(attr.defaultValue)"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <!-- 默认 -->
      <el-input
        v-else
        v-model="formData[attr.attributeCode]"
        :placeholder="`请输入${attr.attributeName}`"
      />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

interface Attribute {
  id: string
  attributeCode: string
  attributeName: string
  dataType: string
  isRequired: number
  maxLength?: number
  defaultValue?: string
  displayType?: string
  sortOrder?: number
}

const props = defineProps<{
  attributes: Attribute[]
  modelValue: Record<string, any>
}>()

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, any>]
}>()

const formRef = ref<FormInstance>()

const formData = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formRules = computed<FormRules>(() => {
  const rules: FormRules = {}
  props.attributes.forEach((attr) => {
    if (attr.isRequired === 1) {
      rules[attr.attributeCode] = [
        { required: true, message: `请输入${attr.attributeName}`, trigger: 'blur' }
      ]
    }
  })
  return rules
})

const parseOptions = (val?: string) => {
  if (!val) return []
  try {
    return JSON.parse(val)
  } catch {
    return val.split(',').map((v) => ({ label: v.trim(), value: v.trim() }))
  }
}

watch(() => props.attributes, (newAttrs) => {
  const newData = { ...formData.value }
  newAttrs.forEach((attr) => {
    if (newData[attr.attributeCode] === undefined) {
      newData[attr.attributeCode] = attr.defaultValue || ''
    }
  })
  emit('update:modelValue', newData)
}, { immediate: true })

const validate = async () => {
  if (!formRef.value) return false
  try {
    await formRef.value.validate()
    return true
  } catch {
    return false
  }
}

defineExpose({ validate })
</script>

<style scoped lang="scss">
.dynamic-form {
  .el-form-item {
    margin-bottom: 18px;
  }
}
</style>
