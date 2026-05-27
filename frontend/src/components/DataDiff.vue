<template>
  <div class="data-diff">
    <div v-if="!diffs || diffs.length === 0" class="diff-empty">
      无差异数据
    </div>
    <div v-for="diff in diffs" :key="diff.fieldName" class="diff-row">
      <span class="field-name">{{ diff.fieldName }}</span>
      <span class="old-value">{{ diff.oldValue || '-' }}</span>
      <span class="arrow">→</span>
      <span class="new-value">{{ diff.newValue || '-' }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
interface DiffItem {
  fieldName: string
  oldValue?: string
  newValue?: string
}

defineProps<{
  diffs: DiffItem[]
}>()
</script>

<style scoped lang="scss">
.data-diff {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.diff-empty {
  text-align: center;
  color: var(--text-secondary);
  padding: 20px;
  font-size: 13px;
}
.diff-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  .field-name {
    width: 120px;
    font-weight: 500;
    color: var(--text-bright);
    flex-shrink: 0;
  }
  .old-value {
    flex: 1;
    color: #ef4444;
    background: rgba(239, 68, 68, 0.05);
    padding: 4px 8px;
    border-radius: 4px;
    text-decoration: line-through;
  }
  .arrow {
    color: var(--color-primary);
    font-weight: bold;
  }
  .new-value {
    flex: 1;
    color: #10b981;
    background: rgba(16, 185, 129, 0.05);
    padding: 4px 8px;
    border-radius: 4px;
  }
}
</style>
