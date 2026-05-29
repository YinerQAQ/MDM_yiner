<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  Document, DataLine, User as Users, CircleCheck,
  TrendCharts, EditPen, Upload, DataAnalysis,
  Timer, Edit, Check,
  Operation, Collection
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

import type { MdmDataModel, MdmMainData } from '../api/types'
import { getAllModels } from '../api/models'
import { getMainDataByModelId } from '../api/mainData'
import { getMyTasks } from '../api/workflows'
import { useAuthStore } from '../stores/auth'

const router = useRouter()

const models = ref<MdmDataModel[]>([])
const mainDataList = ref<MdmMainData[]>([])
const stats = ref({
  totalModels: 0,
  totalData: 0,
  pendingReview: 0,
  todayNew: 0
})

// 动画计数器
const displayStats = ref({
  totalModels: 0,
  totalData: 0,
  pendingReview: 0,
  todayNew: 0
})

const animateCount = (key: keyof typeof displayStats.value, target: number) => {
  const duration = 1200
  const start = displayStats.value[key]
  const diff = target - start
  if (diff === 0) return
  const startTime = performance.now()
  const step = (now: number) => {
    const elapsed = now - startTime
    const progress = Math.min(elapsed / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    displayStats.value[key] = Math.round(start + diff * eased)
    if (progress < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

watch(() => stats.value.totalModels, v => animateCount('totalModels', v))
watch(() => stats.value.totalData, v => animateCount('totalData', v))
watch(() => stats.value.pendingReview, v => animateCount('pendingReview', v))
watch(() => stats.value.todayNew, v => animateCount('todayNew', v))

const todoItems = ref<Array<{ title: string; time: string; type: string; priority: string; id?: string }>>([])

const recentLogs = ref([
  { action: '创建数据模型', user: '管理员', time: '2024-01-15 14:30', icon: 'model' },
  { action: '审核通过主数据', user: '张三', time: '2024-01-15 14:15', icon: 'approve' },
  { action: '修改编码规则', user: '李四', time: '2024-01-15 13:45', icon: 'edit' },
  { action: '发布组织数据', user: '管理员', time: '2024-01-15 11:20', icon: 'publish' },
  { action: '归档历史数据', user: '王五', time: '2024-01-15 10:00', icon: 'archive' }
])

const loadMyTasks = async () => {
  try {
    const authStore = useAuthStore()
    const res = await getMyTasks({ userId: authStore.username || 'admin' })
    const tasks = res.data.data || []
    todoItems.value = tasks.slice(0, 5).map((t: any) => ({
      id: t.id,
      title: t.taskName || t.businessType || '审核任务',
      time: t.createTime ? formatTime(t.createTime) : '',
      type: t.taskType || '审核',
      priority: t.priority || 'normal'
    }))
    stats.value.pendingReview = tasks.length
  } catch {
    todoItems.value = []
  }
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  return `${Math.floor(hours / 24)}天前`
}

const loadData = async () => {
  try {
    const modelsResponse = await getAllModels()
    models.value = modelsResponse.data.data || []
    stats.value.totalModels = models.value.length

    for (const model of models.value) {
      try {
        const dataResponse = await getMainDataByModelId(model.id)
        const dataList = dataResponse.data.data || []
        mainDataList.value.push(...dataList)
      } catch (e) {
        console.error(`加载模型${model.modelName}数据失败:`, e)
      }
    }

    stats.value.totalData = mainDataList.value.length
    stats.value.todayNew = Math.floor(mainDataList.value.length * 0.1)

    await loadMyTasks()

    await nextTick()
    initTrendChart()
    initPieChart()
  } catch (error) {
    // API加载失败时使用mock数据确保页面不白屏
    models.value = []
    mainDataList.value = []
    stats.value = { totalModels: 0, totalData: 0, pendingReview: 0, todayNew: 0 }
    await nextTick()
    initTrendChart()
    initPieChart()
  }
}

let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const initTrendChart = () => {
  const el = document.getElementById('trend-chart')
  if (!el) return
  trendChart = echarts.init(el)

  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const newData = [12, 18, 15, 22, 28, 20, 25]
  const reviewData = [8, 14, 12, 18, 22, 16, 20]

  trendChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(27, 40, 56, 0.95)',
      borderColor: 'rgba(0, 212, 255, 0.3)',
      textStyle: { color: '#e2e8f0', fontSize: 13 },
      axisPointer: { lineStyle: { color: 'rgba(0, 212, 255, 0.3)' } }
    },
    legend: {
      top: 0,
      right: 0,
      textStyle: { color: '#94a3b8', fontSize: 12 },
      itemWidth: 16,
      itemHeight: 3,
      itemGap: 16
    },
    grid: { top: 40, right: 20, bottom: 30, left: 50 },
    xAxis: {
      type: 'category',
      data: days,
      axisLine: { lineStyle: { color: 'rgba(100, 116, 139, 0.3)' } },
      axisLabel: { color: '#94a3b8', fontSize: 12 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 12 },
      splitLine: { lineStyle: { color: 'rgba(100, 116, 139, 0.1)', type: 'dashed' } }
    },
    series: [
      {
        name: '新增数据',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2.5, color: '#00d4ff' },
        itemStyle: { color: '#00d4ff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 212, 255, 0.25)' },
            { offset: 1, color: 'rgba(0, 212, 255, 0.02)' }
          ])
        },
        data: newData
      },
      {
        name: '审核数据',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2.5, color: '#10b981' },
        itemStyle: { color: '#10b981' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16, 185, 129, 0.2)' },
            { offset: 1, color: 'rgba(16, 185, 129, 0.02)' }
          ])
        },
        data: reviewData
      }
    ]
  })
}

const initPieChart = () => {
  const el = document.getElementById('pie-chart')
  if (!el) return
  pieChart = echarts.init(el)

  const modelData = models.value.slice(0, 6).map(m => {
    const count = mainDataList.value.filter(d => d.modelId === m.id).length
    return { value: count, name: m.modelName }
  })

  const colors = ['#00d4ff', '#6366f1', '#10b981', '#f59e0b', '#ef4444', '#06b6d4']

  pieChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(27, 40, 56, 0.95)',
      borderColor: 'rgba(0, 212, 255, 0.3)',
      textStyle: { color: '#e2e8f0' }
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { color: '#94a3b8', fontSize: 12 },
      itemWidth: 10,
      itemHeight: 10,
      itemGap: 12
    },
    series: [{
      type: 'pie',
      radius: ['50%', '75%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold', color: '#e2e8f0' },
        itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0, 212, 255, 0.3)' }
      },
      data: modelData.map((d, i) => ({
        ...d,
        itemStyle: { color: colors[i % colors.length] }
      }))
    }]
  })
}

const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
}

// 快捷操作
const quickActions = [
  { icon: EditPen, label: '新增数据', color: '#00d4ff', action: () => router.push('/main-data') },
  { icon: Check, label: '发起审核', color: '#10b981', action: () => router.push('/data-review') },
  { icon: Upload, label: '导入数据', color: '#6366f1', action: () => router.push('/main-data') },
  { icon: DataAnalysis, label: '查看报表', color: '#f59e0b', action: () => router.push('/data-exchange') },
  { icon: Operation, label: '流程管理', color: '#06b6d4', action: () => router.push('/workflows') },
  { icon: Collection, label: '数据模型', color: '#ef4444', action: () => router.push('/data-models') }
]

const getLogIcon = (type: string) => {
  switch (type) {
    case 'model': return Document
    case 'approve': return Check
    case 'edit': return Edit
    case 'publish': return DataAnalysis
    case 'archive': return Collection
    default: return Timer
  }
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
})
</script>

<template>
  <div class="page-container">
    <!-- 统计卡片行 -->
    <div class="stat-row">
      <div class="stat-card animate-fade-in" style="animation-delay: 0s">
        <div class="stat-card-bg bg-models"></div>
        <div class="stat-icon-box models">
          <el-icon :size="24"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ displayStats.totalModels }}</div>
          <div class="stat-label">数据模型数</div>
        </div>
        <div class="stat-trend up">
          <el-icon :size="14"><TrendCharts /></el-icon>
          <span>12%</span>
        </div>
        <div class="stat-shimmer"></div>
      </div>

      <div class="stat-card animate-fade-in" style="animation-delay: 0.1s">
        <div class="stat-card-bg bg-data"></div>
        <div class="stat-icon-box data">
          <el-icon :size="24"><DataLine /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ displayStats.totalData }}</div>
          <div class="stat-label">主数据总量</div>
        </div>
        <div class="stat-trend up">
          <el-icon :size="14"><TrendCharts /></el-icon>
          <span>8%</span>
        </div>
        <div class="stat-shimmer"></div>
      </div>

      <div class="stat-card animate-fade-in" style="animation-delay: 0.2s">
        <div class="stat-card-bg bg-pending"></div>
        <div class="stat-icon-box pending">
          <el-icon :size="24"><Users /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ displayStats.pendingReview }}</div>
          <div class="stat-label">待审核</div>
        </div>
        <div class="stat-trend warn">
          <el-icon :size="14"><TrendCharts /></el-icon>
          <span>+3</span>
        </div>
        <div class="stat-shimmer"></div>
      </div>

      <div class="stat-card animate-fade-in" style="animation-delay: 0.3s">
        <div class="stat-card-bg bg-today"></div>
        <div class="stat-icon-box today">
          <el-icon :size="24"><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ displayStats.todayNew }}</div>
          <div class="stat-label">今日新增</div>
        </div>
        <div class="stat-trend up">
          <el-icon :size="14"><TrendCharts /></el-icon>
          <span>15%</span>
        </div>
        <div class="stat-shimmer"></div>
      </div>
    </div>

    <!-- CSS Grid 24格布局 -->
    <div class="dashboard-grid">
      <!-- 数据趋势图 (8格) -->
      <div class="grid-item card-glow" style="grid-column: span 16;">
        <div class="card-header">
          <span class="card-title">7天数据趋势</span>
          <span class="card-subtitle">近一周新增与审核数据</span>
        </div>
        <div id="trend-chart" style="height: 320px;"></div>
      </div>

      <!-- 待办任务面板 (4格) -->
      <div class="grid-item card-glow" style="grid-column: span 8;">
        <div class="card-header">
          <span class="card-title">待办任务</span>
          <span class="card-badge">{{ todoItems.length }}</span>
        </div>
        <div class="todo-list">
          <div
            v-for="(item, idx) in todoItems"
            :key="idx"
            class="todo-item"
          >
            <div class="todo-left">
              <div class="todo-priority" :class="item.priority === 'high' ? 'priority-high' : item.priority === 'medium' ? 'priority-medium' : 'priority-normal'"></div>
              <div class="todo-content">
                <div class="todo-title">{{ item.title }}</div>
                <div class="todo-meta">
                  <span class="todo-time">{{ item.time }}</span>
                </div>
              </div>
            </div>
            <span class="todo-type" :class="item.type === '审核' ? 'type-review' : item.type === '变更' ? 'type-change' : 'type-other'">
              {{ item.type }}
            </span>
          </div>
          <div v-if="todoItems.length === 0" class="todo-empty">
            <el-icon :size="32" color="var(--text-muted)"><CircleCheck /></el-icon>
            <span>暂无待办事项</span>
          </div>
        </div>
      </div>

      <!-- 数据模型分布 (6格) -->
      <div class="grid-item card-glow" style="grid-column: span 12;">
        <div class="card-header">
          <span class="card-title">数据模型分布</span>
          <span class="card-subtitle">各模型数据占比</span>
        </div>
        <div id="pie-chart" style="height: 300px;"></div>
      </div>

      <!-- 最近操作时间线 (6格) -->
      <div class="grid-item card-glow" style="grid-column: span 12;">
        <div class="card-header">
          <span class="card-title">最近操作</span>
        </div>
        <div class="timeline">
          <div v-for="(log, idx) in recentLogs" :key="idx" class="timeline-item">
            <div class="timeline-dot" :class="idx === 0 ? 'dot-active' : ''">
              <el-icon :size="12"><component :is="getLogIcon(log.icon)" /></el-icon>
            </div>
            <div v-if="idx < recentLogs.length - 1" class="timeline-line"></div>
            <div class="timeline-content">
              <div class="timeline-action">{{ log.action }}</div>
              <div class="timeline-meta">
                <span class="timeline-user">{{ log.user }}</span>
                <span class="timeline-time">{{ log.time }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷操作入口 (12格) -->
      <div class="grid-item card-glow" style="grid-column: span 24;">
        <div class="card-header">
          <span class="card-title">快捷操作</span>
        </div>
        <div class="quick-actions">
          <div
            v-for="(action, idx) in quickActions"
            :key="idx"
            class="quick-action-btn"
            @click="action.action"
          >
            <div class="action-icon" :style="{ '--action-color': action.color }">
              <el-icon :size="22"><component :is="action.icon" /></el-icon>
            </div>
            <span class="action-label">{{ action.label }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
// 统计卡片
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 22px 24px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  backdrop-filter: blur(10px);
  transition: var(--transition-normal);
  overflow: hidden;

  &:hover {
    border-color: var(--border-glow);
    box-shadow: var(--shadow-glow);
    transform: translateY(-3px);
  }
}

.stat-card-bg {
  position: absolute;
  top: 0;
  right: 0;
  width: 120px;
  height: 100%;
  opacity: 0.06;
  pointer-events: none;

  &.bg-models { background: linear-gradient(135deg, #00d4ff, transparent); }
  &.bg-data { background: linear-gradient(135deg, #6366f1, transparent); }
  &.bg-pending { background: linear-gradient(135deg, #f59e0b, transparent); }
  &.bg-today { background: linear-gradient(135deg, #10b981, transparent); }
}

.stat-shimmer {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.3), transparent);
  animation: shimmer 3s infinite;
}

@keyframes shimmer {
  0% { left: -100%; }
  100% { left: 100%; }
}

.stat-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;

  &.models {
    background: linear-gradient(135deg, #00d4ff, #0ea5e9);
    box-shadow: 0 4px 12px rgba(0, 212, 255, 0.3);
  }
  &.data {
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  }
  &.pending {
    background: linear-gradient(135deg, #f59e0b, #f97316);
    box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
  }
  &.today {
    background: linear-gradient(135deg, #10b981, #34d399);
    box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
  }
}

.stat-info {
  flex: 1;
  .stat-value {
    font-size: 28px;
    font-weight: 700;
    color: var(--text-bright);
    line-height: 1.2;
    font-variant-numeric: tabular-nums;
  }
  .stat-label {
    font-size: 13px;
    color: var(--text-secondary);
    margin-top: 2px;
  }
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;

  &.up { color: var(--color-success); }
  &.warn { color: var(--color-warning); }
  &.down { color: var(--color-danger); }
}

// CSS Grid 24格布局
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(24, 1fr);
  gap: 20px;
}

.grid-item {
  padding: 20px;
}

// 卡片头部
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-bright);
  padding-left: 12px;
  border-left: 3px solid var(--color-primary);
}

.card-subtitle {
  font-size: 12px;
  color: var(--text-muted);
}

.card-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  background: var(--color-primary);
  border-radius: 10px;
}

// 待办面板
.todo-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 310px;
  overflow-y: auto;
}

.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-radius: var(--radius-sm);
  background: rgba(13, 27, 42, 0.5);
  border: 1px solid transparent;
  transition: var(--transition-fast);
  cursor: pointer;

  &:hover {
    background: rgba(0, 212, 255, 0.04);
    border-color: var(--border-glow);
  }
}

.todo-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.todo-priority {
  width: 3px;
  height: 32px;
  border-radius: 2px;
  flex-shrink: 0;

  &.priority-high { background: var(--color-danger); box-shadow: 0 0 6px rgba(239, 68, 68, 0.4); }
  &.priority-medium { background: var(--color-warning); box-shadow: 0 0 6px rgba(245, 158, 11, 0.4); }
  &.priority-low { background: var(--color-success); box-shadow: 0 0 6px rgba(16, 185, 129, 0.4); }
  &.priority-normal { background: var(--color-primary); box-shadow: 0 0 6px rgba(0, 212, 255, 0.4); }
}

.todo-content {
  flex: 1;
  min-width: 0;
  .todo-title {
    font-size: 13px;
    color: var(--text-primary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .todo-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 3px;
  }
  .todo-time {
    font-size: 11px;
    color: var(--text-muted);
  }
}

.todo-type {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  flex-shrink: 0;
  font-weight: 500;

  &.type-review {
    color: var(--color-primary);
    background: rgba(0, 212, 255, 0.1);
    border: 1px solid rgba(0, 212, 255, 0.2);
  }
  &.type-change {
    color: var(--color-warning);
    background: rgba(245, 158, 11, 0.1);
    border: 1px solid rgba(245, 158, 11, 0.2);
  }
  &.type-other {
    color: var(--color-success);
    background: rgba(16, 185, 129, 0.1);
    border: 1px solid rgba(16, 185, 129, 0.2);
  }
}

.todo-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 0;
  color: var(--text-muted);
  font-size: 13px;
}

// 时间线
.timeline {
  display: flex;
  flex-direction: column;
  gap: 0;
  max-height: 310px;
  overflow-y: auto;
  padding-right: 4px;
}

.timeline-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  position: relative;
  padding-bottom: 20px;

  &:last-child { padding-bottom: 0; }
}

.timeline-dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  border: 2px solid var(--border-color);
  color: var(--text-secondary);
  flex-shrink: 0;
  z-index: 1;
  transition: var(--transition-fast);

  &.dot-active {
    border-color: var(--color-primary);
    color: var(--color-primary);
    box-shadow: 0 0 10px rgba(0, 212, 255, 0.3);
  }
}

.timeline-line {
  position: absolute;
  left: 13px;
  top: 28px;
  width: 2px;
  height: calc(100% - 28px);
  background: var(--border-color);
}

.timeline-content {
  flex: 1;
  min-width: 0;
  .timeline-action {
    font-size: 13px;
    color: var(--text-primary);
    font-weight: 500;
  }
  .timeline-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-top: 4px;
  }
  .timeline-user {
    font-size: 12px;
    color: var(--color-primary);
  }
  .timeline-time {
    font-size: 12px;
    color: var(--text-muted);
  }
}

// 快捷操作
.quick-actions {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}

.quick-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px 12px;
  border-radius: var(--radius-md);
  background: rgba(13, 27, 42, 0.5);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: var(--transition-normal);

  &:hover {
    border-color: var(--action-color, var(--border-glow));
    box-shadow: 0 0 16px rgba(0, 212, 255, 0.1);
    transform: translateY(-2px);
    background: rgba(0, 212, 255, 0.03);
  }
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, var(--action-color), color-mix(in srgb, var(--action-color) 60%, #000));
  box-shadow: 0 4px 12px color-mix(in srgb, var(--action-color) 30%, transparent);
}

.action-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}
</style>
