<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { VueFlow, useVueFlow, Position, Handle } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import type { Node, Edge } from '@vue-flow/core'
import { ElMessage } from 'element-plus'
import { getWorkflowDesign, saveWorkflowDesign } from '../api/workflows'

import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'

const route = useRoute()
const router = useRouter()
const workflowId = computed(() => route.params.id as string || route.query.id as string)

const { onConnect, addEdges, onNodeClick, onPaneClick } = useVueFlow()

// 节点类型配置
const nodeTypes = [
  { type: 'start', label: '开始节点', icon: '▶', color: '#10b981' },
  { type: 'task', label: '审核节点', icon: '✎', color: '#00d4ff' },
  { type: 'exclusive', label: '排他网关', icon: '◇', color: '#f59e0b' },
  { type: 'parallel', label: '并行网关', icon: '◈', color: '#8b5cf6' },
  { type: 'end', label: '结束节点', icon: '■', color: '#ef4444' }
]

// 审核人类型选项
const assigneeTypeOptions = [
  { label: '指定用户', value: 'user' },
  { label: '指定角色', value: 'role' },
  { label: '指定组织', value: 'org' },
  { label: '发起人本人', value: 'initiator' },
  { label: '发起人上级', value: 'manager' }
]

interface NodeData {
  label: string
  nodeType: string
  assigneeType?: string
  assigneeValue?: string
  timeout?: number
  multiInstance?: boolean
  description?: string
}

const nodes = ref<Node[]>([])
const edges = ref<Edge[]>([])
const selectedNode = ref<Node | null>(null)
const selectedNodeData = ref<NodeData | null>(null)

let nodeIdCounter = 0
const getNextNodeId = () => `node_${++nodeIdCounter}`

// 初始默认节点
const initDefaultNodes = () => {
  nodes.value = [
    {
      id: 'node_start',
      type: 'default',
      position: { x: 300, y: 50 },
      data: { label: '开始', nodeType: 'start' } as NodeData
    },
    {
      id: 'node_end',
      type: 'default',
      position: { x: 300, y: 450 },
      data: { label: '结束', nodeType: 'end' } as NodeData
    }
  ]
  nodeIdCounter = 2
}

// 拖拽相关
const onDragStart = (nodeType: string) => {
  const config = nodeTypes.find(n => n.type === nodeType)
  if (config) {
    const dataTransfer = (event: DragEvent) => {
      event.dataTransfer?.setData('application/vueflow', JSON.stringify({
        type: nodeType,
        label: config.label,
        color: config.color
      }))
      event.dataTransfer!.effectAllowed = 'move'
    }
    // Store the handler
    ;(window as any).__dragHandler = dataTransfer
  }
}

const onDragOver = (event: DragEvent) => {
  event.preventDefault()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
}

const onDrop = (event: DragEvent) => {
  const data = event.dataTransfer?.getData('application/vueflow')
  if (!data) return

  const parsed = JSON.parse(data)

  // 计算落点位置（相对于画布）
  const position = {
    x: event.offsetX - 100,
    y: event.offsetY - 25
  }

  const newNode: Node = {
    id: getNextNodeId(),
    type: 'default',
    position,
    data: {
      label: parsed.label,
      nodeType: parsed.type
    } as NodeData
  }

  nodes.value = [...nodes.value, newNode]
}

// 节点连接
onConnect((params) => {
  addEdges([{
    ...params,
    animated: true,
    style: { stroke: 'var(--color-primary, #00d4ff)', strokeWidth: 2 }
  }])
})

// 节点点击
onNodeClick(({ node }) => {
  selectedNode.value = node
  selectedNodeData.value = { ...node.data } as NodeData
})

// 画布点击（取消选中）
onPaneClick(() => {
  selectedNode.value = null
  selectedNodeData.value = null
})

// 更新选中节点属性
const updateNodeProperty = (key: string, value: any) => {
  if (!selectedNode.value || !selectedNodeData.value) return
  ;(selectedNodeData.value as any)[key] = value
  selectedNode.value.data = { ...selectedNodeData.value }
}

// 返回
const goBack = () => {
  router.push('/workflows')
}

// 保存设计
const saveDesign = async () => {
  if (!workflowId.value) {
    ElMessage.error('缺少流程ID')
    return
  }

  const designData = {
    nodes: nodes.value.map(n => ({
      id: n.id,
      position: n.position,
      data: n.data
    })),
    edges: edges.value.map(e => ({
      id: e.id,
      source: e.source,
      target: e.target,
      sourceHandle: e.sourceHandle,
      targetHandle: e.targetHandle
    }))
  }

  try {
    await saveWorkflowDesign(workflowId.value, designData)
    ElMessage.success('设计保存成功')
  } catch {
    ElMessage.error('保存失败')
  }
}

// 加载已有设计
const loadDesign = async () => {
  if (!workflowId.value) return
  try {
    const res = await getWorkflowDesign(workflowId.value)
    const data = res.data.data
    if (data && data.nodes && data.nodes.length > 0) {
      nodes.value = data.nodes.map((n: any) => ({
        id: n.id,
        type: 'default',
        position: n.position,
        data: n.data
      }))
      edges.value = (data.edges || []).map((e: any) => ({
        ...e,
        animated: true,
        style: { stroke: 'var(--color-primary, #00d4ff)', strokeWidth: 2 }
      }))
      // 更新计数器
      const maxId = Math.max(...nodes.value.map(n => {
        const num = parseInt(n.id.replace('node_', ''))
        return isNaN(num) ? 0 : num
      }), 0)
      nodeIdCounter = maxId
    } else {
      initDefaultNodes()
    }
  } catch {
    initDefaultNodes()
  }
}

onMounted(() => {
  if (workflowId.value) {
    loadDesign()
  } else {
    initDefaultNodes()
  }
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">流程设计器</h2>
        <p class="page-desc">拖拽节点设计审批流程</p>
      </div>
      <div style="display: flex; gap: 10px;">
        <el-button @click="goBack">返回</el-button>
        <el-button type="primary" @click="saveDesign">保存设计</el-button>
      </div>
    </div>

    <div class="designer-layout card-glow">
      <!-- 左侧节点面板 -->
      <div class="node-panel">
        <div class="panel-title">节点类型</div>
        <div
          v-for="nt in nodeTypes"
          :key="nt.type"
          class="node-item"
          draggable="true"
          @dragstart="onDragStart(nt.type)"
        >
          <span class="node-icon" :style="{ background: nt.color }">{{ nt.icon }}</span>
          <span class="node-label">{{ nt.label }}</span>
        </div>
        <div class="panel-divider"></div>
        <div class="panel-title">操作提示</div>
        <div class="panel-tip">拖拽节点至画布</div>
        <div class="panel-tip">连接节点端口创建连线</div>
        <div class="panel-tip">点击节点编辑属性</div>
      </div>

      <!-- 中间画布 -->
      <div class="flow-canvas" @drop="onDrop" @dragover="onDragOver">
        <VueFlow
          v-model:nodes="nodes"
          v-model:edges="edges"
          :default-edge-options="{ animated: true, style: { stroke: '#00d4ff', strokeWidth: 2 } }"
          :snap-to-grid="true"
          :snap-grid="[15, 15]"
          fit-view-on-init
          class="vue-flow-wrapper"
        >
          <Background :gap="20" :size="1" pattern-color="rgba(100, 116, 139, 0.15)" />
          <Controls position="bottom-right" />

          <!-- 自定义节点渲染 -->
          <template #node-default="nodeProps">
            <div class="custom-node" :class="'node-type-' + nodeProps.data.nodeType">
              <div class="node-header">
                <span class="node-type-icon">{{ nodeTypes.find(t => t.type === nodeProps.data.nodeType)?.icon || '●' }}</span>
                <span class="node-title">{{ nodeProps.data.label }}</span>
              </div>
              <div v-if="nodeProps.data.nodeType === 'task' && nodeProps.data.assigneeType" class="node-detail">
                {{ assigneeTypeOptions.find(a => a.value === nodeProps.data.assigneeType)?.label || '' }}
              </div>
              <Handle type="target" :position="Position.Top" />
              <Handle type="source" :position="Position.Bottom" />
            </div>
          </template>
        </VueFlow>
      </div>

      <!-- 右侧属性面板 -->
      <div class="property-panel" v-if="selectedNodeData">
        <div class="panel-title">节点属性</div>
        <div class="prop-group">
          <label class="prop-label">节点名称</label>
          <el-input
            :model-value="selectedNodeData.label"
            @update:model-value="(v: string) => updateNodeProperty('label', v)"
            placeholder="请输入节点名称"
            size="small"
          />
        </div>
        <div class="prop-group">
          <label class="prop-label">节点类型</label>
          <el-select
            :model-value="selectedNodeData.nodeType"
            @update:model-value="(v: string) => updateNodeProperty('nodeType', v)"
            size="small"
            style="width: 100%"
            disabled
          >
            <el-option v-for="nt in nodeTypes" :key="nt.type" :label="nt.label" :value="nt.type" />
          </el-select>
        </div>

        <template v-if="selectedNodeData.nodeType === 'task'">
          <div class="prop-group">
            <label class="prop-label">审核人类型</label>
            <el-select
              :model-value="selectedNodeData.assigneeType"
              @update:model-value="(v: string) => updateNodeProperty('assigneeType', v)"
              size="small"
              style="width: 100%"
              placeholder="请选择审核人类型"
            >
              <el-option v-for="opt in assigneeTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </div>
          <div class="prop-group" v-if="selectedNodeData.assigneeType && selectedNodeData.assigneeType !== 'initiator' && selectedNodeData.assigneeType !== 'manager'">
            <label class="prop-label">审核人/角色/组织</label>
            <el-input
              :model-value="selectedNodeData.assigneeValue"
              @update:model-value="(v: string) => updateNodeProperty('assigneeValue', v)"
              placeholder="请输入标识"
              size="small"
            />
          </div>
          <div class="prop-group">
            <label class="prop-label">超时时间(小时)</label>
            <el-input-number
              :model-value="selectedNodeData.timeout || 0"
              @update:model-value="(v: number) => updateNodeProperty('timeout', v)"
              :min="0"
              :max="720"
              size="small"
              style="width: 100%"
            />
          </div>
          <div class="prop-group">
            <label class="prop-label">会签模式</label>
            <el-switch
              :model-value="selectedNodeData.multiInstance || false"
              @update:model-value="(v: boolean) => updateNodeProperty('multiInstance', v)"
              active-text="会签"
              inactive-text="或签"
              size="small"
            />
          </div>
        </template>

        <div class="prop-group">
          <label class="prop-label">备注</label>
          <el-input
            :model-value="selectedNodeData.description"
            @update:model-value="(v: string) => updateNodeProperty('description', v)"
            type="textarea"
            :rows="3"
            placeholder="节点说明"
            size="small"
          />
        </div>
      </div>

      <!-- 右侧占位 -->
      <div class="property-panel property-panel-empty" v-else>
        <div class="panel-title">节点属性</div>
        <div class="empty-hint">
          <p>点击画布中的节点</p>
          <p>查看和编辑属性</p>
        </div>
      </div>
    </div>
  </div>
</template>



<style scoped lang="scss">
.designer-layout {
  display: flex;
  height: calc(100vh - 160px);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.node-panel {
  width: 200px;
  background: rgba(13, 27, 42, 0.8);
  border-right: 1px solid var(--border-color);
  padding: 16px 12px;
  flex-shrink: 0;
  overflow-y: auto;
}

.panel-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-bright);
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 3px solid var(--color-primary);
}

.node-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  margin-bottom: 6px;
  border-radius: 6px;
  background: rgba(0, 212, 255, 0.05);
  border: 1px solid var(--border-color);
  cursor: grab;
  transition: all 0.2s;

  &:hover {
    border-color: var(--border-glow);
    box-shadow: var(--shadow-glow);
    background: rgba(0, 212, 255, 0.1);
  }

  &:active {
    cursor: grabbing;
  }
}

.node-icon {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}

.node-label {
  font-size: 13px;
  color: var(--text-primary);
}

.panel-divider {
  height: 1px;
  background: var(--border-color);
  margin: 16px 0;
}

.panel-tip {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 6px;
  padding-left: 4px;
}

.flow-canvas {
  flex: 1;
  background: #1a2332;
  position: relative;
}

.vue-flow-wrapper {
  width: 100%;
  height: 100%;
}

.property-panel {
  width: 260px;
  background: rgba(13, 27, 42, 0.8);
  border-left: 1px solid var(--border-color);
  padding: 16px;
  flex-shrink: 0;
  overflow-y: auto;
}

.property-panel-empty {
  display: flex;
  flex-direction: column;
}

.empty-hint {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 13px;
  gap: 4px;
}

.prop-group {
  margin-bottom: 14px;
}

.prop-label {
  display: block;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

// 自定义节点样式
.custom-node {
  min-width: 160px;
  padding: 8px 14px;
  border-radius: 8px;
  background: var(--bg-card);
  border: 2px solid var(--border-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  transition: all 0.2s;

  &:hover {
    border-color: var(--border-glow);
    box-shadow: var(--shadow-glow);
  }
}

.node-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-type-icon {
  font-size: 14px;
}

.node-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-bright);
}

.node-detail {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 4px;
  padding-left: 22px;
}

.node-type-start {
  border-color: #10b981;
  .node-type-icon { color: #10b981; }
}
.node-type-end {
  border-color: #ef4444;
  .node-type-icon { color: #ef4444; }
}
.node-type-task {
  border-color: #00d4ff;
  .node-type-icon { color: #00d4ff; }
}
.node-type-exclusive {
  border-color: #f59e0b;
  .node-type-icon { color: #f59e0b; }
}
.node-type-parallel {
  border-color: #8b5cf6;
  .node-type-icon { color: #8b5cf6; }
}
</style>
