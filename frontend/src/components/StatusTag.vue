<template>
  <el-tag :type="tagType" size="small" :class="`status-tag--${tagType}`">
    {{ displayStatus }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  status: string
}>()

const tagType = computed(() => {
  switch (props.status) {
    case '暂存':
    case '编制中':
    case 'DRAFT':
      return 'info'
    case '审核中':
    case '待确认':
    case 'PENDING':
      return 'warning'
    case '审核通过':
    case '已发布':
    case '已通过':
    case 'APPROVED':
      return 'success'
    case '拒绝':
    case '审核拒绝':
    case 'REJECTED':
      return 'danger'
    case '已归档':
    case 'ARCHIVED':
      return 'info'
    default:
      return 'info'
  }
})

const displayStatus = computed(() => {
  return props.status || '-'
})
</script>
