<script setup lang="ts">
import { ref } from 'vue'
import { ElTabs, ElTabPane, ElTable, ElTableColumn, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElIcon, ElMessage } from 'element-plus'
import { Tools } from '@element-plus/icons-vue'

interface DistInterface {
  id: string
  name: string
  modelCode: string
  syncType: string
  status: string
}

const interfaces = ref<DistInterface[]>([
  { id: '1', name: '用户数据分发', modelCode: 'USER_MODEL', syncType: '即时', status: '启用' },
  { id: '2', name: '组织数据分发', modelCode: 'ORG_MODEL', syncType: '定时', status: '启用' },
  { id: '3', name: '产品数据分发', modelCode: 'PRODUCT_MODEL', syncType: '手动', status: '停用' }
])

const dialogVisible = ref(false)
const form = ref({
  id: '',
  name: '',
  modelCode: '',
  syncType: '即时',
  status: '启用'
})

const openDialog = () => {
  form.value = {
    id: '',
    name: '',
    modelCode: '',
    syncType: '即时',
    status: '启用'
  }
  dialogVisible.value = true
}

const saveInterface = () => {
  if (!form.value.name || !form.value.modelCode) {
    ElMessage.error('请填写必填字段')
    return
  }

  if (form.value.id) {
    const index = interfaces.value.findIndex(i => i.id === form.value.id)
    if (index !== -1) {
      interfaces.value[index] = { ...form.value }
    }
    ElMessage.success('更新成功')
  } else {
    form.value.id = `INTERFACE_${Date.now()}`
    interfaces.value.push({ ...form.value })
    ElMessage.success('创建成功')
  }
  dialogVisible.value = false
}

const handleDelete = (id: string) => {
  interfaces.value = interfaces.value.filter(i => i.id !== id)
  ElMessage.success('删除成功')
}

const getStatusClass = (status: string): string => {
  return status === '启用' ? 'status-active' : 'status-inactive'
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">数据交换</h2>
        <p class="page-desc">管理数据分发接口与服务监控</p>
      </div>
    </div>

    <div class="table-card">
      <ElTabs type="card">
        <ElTabPane label="数据分发" name="dist">
          <div style="display: flex; justify-content: flex-end; margin-bottom: 16px;">
            <ElButton type="primary" @click="openDialog()">新建分发接口</ElButton>
          </div>
          <ElTable :data="interfaces" stripe>
            <ElTableColumn prop="name" label="接口名称" />
            <ElTableColumn prop="modelCode" label="关联模型" />
            <ElTableColumn prop="syncType" label="同步类型">
              <template #default="scope">
                <span v-if="scope.row.syncType === '即时'">即时同步</span>
                <span v-else-if="scope.row.syncType === '定时'">定时同步</span>
                <span v-else>手动同步</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="status" label="状态">
              <template #default="scope">
                <span :class="getStatusClass(scope.row.status)">{{ scope.row.status }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn label="操作" width="160">
              <template #default="scope">
                <ElButton type="primary" size="small">编辑</ElButton>
                <ElButton type="danger" size="small" @click="handleDelete(scope.row.id)">删除</ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElTabPane>
        <ElTabPane label="数据接收" name="receive">
          <div class="empty-content">
            <ElIcon :size="48" color="var(--text-muted)"><Tools /></ElIcon>
            <p style="margin-top: 16px; color: var(--text-secondary);">数据接收功能开发中...</p>
          </div>
        </ElTabPane>
        <ElTabPane label="服务监控" name="monitor">
          <div class="empty-content">
            <ElIcon :size="48" color="var(--text-muted)"><Tools /></ElIcon>
            <p style="margin-top: 16px; color: var(--text-secondary);">服务监控功能开发中...</p>
          </div>
        </ElTabPane>
      </ElTabs>
    </div>

    <ElDialog title="分发接口配置" v-model="dialogVisible" width="500px">
      <ElForm :model="form" label-width="100px">
        <ElFormItem label="接口名称" required>
          <ElInput v-model="form.name" />
        </ElFormItem>
        <ElFormItem label="关联模型" required>
          <ElInput v-model="form.modelCode" />
        </ElFormItem>
        <ElFormItem label="同步类型">
          <ElSelect v-model="form.syncType">
            <ElOption label="即时同步" value="即时" />
            <ElOption label="定时同步" value="定时" />
            <ElOption label="手动同步" value="手动" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="form.status">
            <ElOption label="启用" value="启用" />
            <ElOption label="停用" value="停用" />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="saveInterface">保存</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped lang="scss">
.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
}
</style>
