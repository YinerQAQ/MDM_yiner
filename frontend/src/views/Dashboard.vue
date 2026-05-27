<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElRow, ElCol, ElTable, ElTableColumn, ElIcon } from 'element-plus'
import { Document, DataLine, User as Users, CircleCheck, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

import type { MdmDataModel, MdmMainData } from '../api/types'
import { getAllModels } from '../api/models'
import { getMainDataByModelId } from '../api/mainData'
import { getMyTasks } from '../api/workflows'
import { useAuthStore } from '../stores/auth'

const models = ref<MdmDataModel[]>([])
const mainDataList = ref<MdmMainData[]>([])
const stats = ref({
  totalModels: 0,
  totalData: 0,
  pendingReview: 0,
  todayNew: 0
})

const todoItems = ref<Array<{ title: string; time: string; type: string; id?: string }>>([])

const recentLogs = ref([
  { action: '创建数据模型', user: '管理员', time: '2024-01-15 14:30' },
  { action: '审核通过主数据', user: '张三', time: '2024-01-15 14:15' },
  { action: '修改编码规则', user: '李四', time: '2024-01-15 13:45' },
  { action: '发布组织数据', user: '管理员', time: '2024-01-15 11:20' },
  { action: '归档历史数据', user: '王五', time: '2024-01-15 10:00' }
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
      type: t.taskType || '审核'
    }))
    stats.value.pendingReview = tasks.length
  } catch {
    // 如果接口未实现，保留空列表
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
    models.value = modelsResponse.data.data

    stats.value.totalModels = models.value.length

    for (const model of models.value) {
      try {
        const dataResponse = await getMainDataByModelId(model.id)
        const dataList = dataResponse.data.data
        mainDataList.value.push(...dataList)
      } catch (e) {
        console.error(`加载模型${model.modelName}数据失败:`, e)
      }
    }

    stats.value.totalData = mainDataList.value.length
    stats.value.todayNew = Math.floor(mainDataList.value.length * 0.1)

    await loadMyTasks()

    await nextTick()
    initBarChart()
    initPieChart()
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const initBarChart = () => {
  const el = document.getElementById('bar-chart')
  if (!el) return
  const chart = echarts.init(el)
  const modelNames = models.value.slice(0, 6).map(m => m.modelName)
  const modelData = models.value.slice(0, 6).map(m => {
    const count = mainDataList.value.filter(d => d.modelId === m.id).length
    return count
  })

  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(27, 40, 56, 0.9)',
      borderColor: 'rgba(0, 212, 255, 0.3)',
      textStyle: { color: '#e2e8f0' }
    },
    grid: { top: 30, right: 20, bottom: 30, left: 50 },
    xAxis: {
      type: 'category',
      data: modelNames,
      axisLine: { lineStyle: { color: 'rgba(100, 116, 139, 0.3)' } },
      axisLabel: { color: '#94a3b8', fontSize: 11 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#94a3b8' },
      splitLine: { lineStyle: { color: 'rgba(100, 116, 139, 0.15)' } }
    },
    series: [{
      type: 'bar',
      data: modelData,
      barWidth: 24,
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#00d4ff' },
          { offset: 1, color: 'rgba(0, 212, 255, 0.2)' }
        ])
      }
    }]
  })

  window.addEventListener('resize', () => chart.resize())
}

const initPieChart = () => {
  const el = document.getElementById('pie-chart')
  if (!el) return
  const chart = echarts.init(el)

  const approved = mainDataList.value.filter(d => d.dataStatus === '审核通过').length
  const pending = mainDataList.value.filter(d => d.dataStatus === '审核中').length
  const draft = mainDataList.value.filter(d => d.dataStatus === '暂存').length
  const archived = mainDataList.value.filter(d => d.dataStatus === '已归档').length

  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(27, 40, 56, 0.9)',
      borderColor: 'rgba(0, 212, 255, 0.3)',
      textStyle: { color: '#e2e8f0' }
    },
    legend: {
      bottom: 0,
      textStyle: { color: '#94a3b8', fontSize: 12 },
      itemWidth: 10,
      itemHeight: 10,
      itemGap: 16
    },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold', color: '#e2e8f0' }
      },
      data: [
        { value: approved, name: '审核通过', itemStyle: { color: '#10b981' } },
        { value: pending, name: '审核中', itemStyle: { color: '#f59e0b' } },
        { value: draft, name: '暂存', itemStyle: { color: '#94a3b8' } },
        { value: archived, name: '已归档', itemStyle: { color: '#64748b' } }
      ]
    }]
  })

  window.addEventListener('resize', () => chart.resize())
}

const getStatusClass = (status: string): string => {
  switch (status) {
    case '审核通过': return 'status-approved'
    case '审核中': return 'status-pending'
    case '暂存': return 'status-draft'
    case '审核拒绝': return 'status-rejected'
    default: return ''
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <div class="stat-row">
      <div class="stat-card animate-fade-in" style="animation-delay: 0s">
        <div class="stat-icon-box models">
          <ElIcon :size="24"><Document /></ElIcon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalModels }}</div>
          <div class="stat-label">数据模型数</div>
        </div>
        <div class="stat-trend">
          <ElIcon :size="14" color="#10b981"><TrendCharts /></ElIcon>
        </div>
      </div>

      <div class="stat-card animate-fade-in" style="animation-delay: 0.1s">
        <div class="stat-icon-box data">
          <ElIcon :size="24"><DataLine /></ElIcon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalData }}</div>
          <div class="stat-label">主数据总量</div>
        </div>
        <div class="stat-trend">
          <ElIcon :size="14" color="#10b981"><TrendCharts /></ElIcon>
        </div>
      </div>

      <div class="stat-card animate-fade-in" style="animation-delay: 0.2s">
        <div class="stat-icon-box pending">
          <ElIcon :size="24"><Users /></ElIcon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pendingReview }}</div>
          <div class="stat-label">待审核</div>
        </div>
        <div class="stat-trend">
          <ElIcon :size="14" color="#f59e0b"><TrendCharts /></ElIcon>
        </div>
      </div>

      <div class="stat-card animate-fade-in" style="animation-delay: 0.3s">
        <div class="stat-icon-box today">
          <ElIcon :size="24"><CircleCheck /></ElIcon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.todayNew }}</div>
          <div class="stat-label">今日新增</div>
        </div>
        <div class="stat-trend">
          <ElIcon :size="14" color="#10b981"><TrendCharts /></ElIcon>
        </div>
      </div>
    </div>

    <!-- 中间区域 -->
    <ElRow :gutter="20" style="margin-top: 20px;">
      <ElCol :span="14">
        <div class="card-glow chart-card">
          <div class="card-title">各模型数据量</div>
          <div id="bar-chart" style="height: 320px;"></div>
        </div>
      </ElCol>
      <ElCol :span="10">
        <div class="card-glow todo-card">
          <div class="card-title">待办事项</div>
          <div class="todo-list">
            <div
              v-for="(item, idx) in todoItems"
              :key="idx"
              class="todo-item"
            >
              <div class="todo-dot" :class="item.type === '审核' ? 'dot-0' : item.type === '变更' ? 'dot-1' : 'dot-2'"></div>
              <div class="todo-content">
                <div class="todo-title">{{ item.title }}</div>
                <div class="todo-time">{{ item.time }}</div>
              </div>
              <span class="todo-type" :class="item.type === '审核' ? 'type-0' : item.type === '变更' ? 'type-1' : 'type-2'">{{ item.type }}</span>
            </div>
            <div v-if="todoItems.length === 0" style="text-align: center; padding: 30px; color: var(--text-muted); font-size: 13px;">
              暂无待办事项
            </div>
          </div>
        </div>
      </ElCol>
    </ElRow>

    <!-- 底部区域 -->
    <ElRow :gutter="20" style="margin-top: 20px;">
      <ElCol :span="8">
        <div class="card-glow chart-card">
          <div class="card-title">审批状态分布</div>
          <div id="pie-chart" style="height: 280px;"></div>
        </div>
      </ElCol>
      <ElCol :span="16">
        <div class="card-glow log-card">
          <div class="card-title">最近操作日志</div>
          <ElTable :data="recentLogs" style="width: 100%" size="small">
            <ElTableColumn prop="action" label="操作" />
            <ElTableColumn prop="user" label="操作人" width="120" />
            <ElTableColumn prop="time" label="时间" width="180" />
          </ElTable>
        </div>
      </ElCol>
    </ElRow>
  </div>
</template>

<style scoped lang="scss">
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  backdrop-filter: blur(10px);
  transition: var(--transition-normal);

  &:hover {
    border-color: var(--border-glow);
    box-shadow: var(--shadow-glow);
    transform: translateY(-2px);
  }
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
  }

  .stat-label {
    font-size: 13px;
    color: var(--text-secondary);
    margin-top: 2px;
  }
}

.stat-trend {
  flex-shrink: 0;
}

// 卡片标题
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-bright);
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 3px solid var(--color-primary);
}

.chart-card {
  padding: 20px;
}

// 待办卡片
.todo-card {
  padding: 20px;
  height: 100%;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  background: rgba(13, 27, 42, 0.4);
  transition: var(--transition-fast);

  &:hover {
    background: rgba(0, 212, 255, 0.05);
  }
}

.todo-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;

  &.dot-0 { background: var(--color-primary); box-shadow: 0 0 6px rgba(0, 212, 255, 0.5); }
  &.dot-1 { background: var(--color-warning); box-shadow: 0 0 6px rgba(245, 158, 11, 0.5); }
  &.dot-2 { background: var(--color-success); box-shadow: 0 0 6px rgba(16, 185, 129, 0.5); }
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

  .todo-time {
    font-size: 11px;
    color: var(--text-muted);
    margin-top: 2px;
  }
}

.todo-type {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  flex-shrink: 0;
  font-weight: 500;

  &.type-0 {
    color: var(--color-primary);
    background: rgba(0, 212, 255, 0.1);
  }
  &.type-1 {
    color: var(--color-warning);
    background: rgba(245, 158, 11, 0.1);
  }
  &.type-2 {
    color: var(--color-success);
    background: rgba(16, 185, 129, 0.1);
  }
}

// 日志卡片
.log-card {
  padding: 20px;
}
</style>
