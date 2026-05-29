<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import {
  ElTable,
  ElTableColumn,
  ElProgress,
  ElTag,
  ElButton,
  ElMessage,
  ElMessageBox
} from 'element-plus'

interface OnlineUser {
  id: string
  username: string
  realName: string
  ip: string
  loginTime: string
  device: string
  duration: string
}
interface LoginRecord {
  id: string
  username: string
  realName: string
  ip: string
  device: string
  loginTime: string
  status: '成功' | '失败'
}
interface ScheduleJob {
  id: string
  name: string
  cron: string
  lastRun: string
  duration: string
  nextRun: string
  status: '成功' | '失败' | '执行中'
}

const jvmHeapUsed = ref(0)
const jvmHeapMax = ref(2048)
const cpuLoad = ref(0)
const dbActive = ref(0)
const dbIdle = ref(0)
const dbMax = ref(50)
const diskUsed = ref(0)
const diskTotal = ref(500)
const uptime = ref('5天 14小时 22分钟')

const onlineUsers = ref<OnlineUser[]>([])
const loginRecords = ref<LoginRecord[]>([])
const scheduleJobs = ref<ScheduleJob[]>([])

let timer: ReturnType<typeof setInterval> | null = null

const buildOnline = (): OnlineUser[] => [
  { id: 'U1', username: 'admin', realName: '系统管理员', ip: '192.168.10.21', loginTime: '2026-05-29 08:30:11', device: 'Chrome / Windows', duration: '00:42:18' },
  { id: 'U2', username: 'liu.zhi', realName: '刘志', ip: '192.168.10.55', loginTime: '2026-05-29 08:55:03', device: 'Chrome / macOS', duration: '00:17:26' },
  { id: 'U3', username: 'wang.fang', realName: '王芳', ip: '10.20.5.18', loginTime: '2026-05-29 09:01:48', device: 'Edge / Windows', duration: '00:10:41' },
  { id: 'U4', username: 'zhao.lei', realName: '赵磊', ip: '172.16.4.99', loginTime: '2026-05-29 09:08:22', device: 'Firefox / Linux', duration: '00:04:07' }
]

const buildLogins = (): LoginRecord[] => [
  { id: 'R01', username: 'admin', realName: '系统管理员', ip: '192.168.10.21', device: 'Chrome / Windows', loginTime: '2026-05-29 08:30:11', status: '成功' },
  { id: 'R02', username: 'liu.zhi', realName: '刘志', ip: '192.168.10.55', device: 'Chrome / macOS', loginTime: '2026-05-29 08:55:03', status: '成功' },
  { id: 'R03', username: 'unknown', realName: '-', ip: '203.0.113.42', device: 'curl/8.0', loginTime: '2026-05-29 08:42:31', status: '失败' },
  { id: 'R04', username: 'wang.fang', realName: '王芳', ip: '10.20.5.18', device: 'Edge / Windows', loginTime: '2026-05-29 09:01:48', status: '成功' },
  { id: 'R05', username: 'zhao.lei', realName: '赵磊', ip: '172.16.4.99', device: 'Firefox / Linux', loginTime: '2026-05-29 09:08:22', status: '成功' },
  { id: 'R06', username: 'liu.zhi', realName: '刘志', ip: '192.168.10.55', device: 'Chrome / macOS', loginTime: '2026-05-28 17:55:11', status: '成功' },
  { id: 'R07', username: 'sun.qi', realName: '孙琪', ip: '192.168.10.81', device: 'Chrome / Windows', loginTime: '2026-05-28 16:21:09', status: '成功' },
  { id: 'R08', username: 'admin', realName: '系统管理员', ip: '192.168.10.21', device: 'Chrome / Windows', loginTime: '2026-05-28 09:00:02', status: '成功' },
  { id: 'R09', username: 'admin', realName: '系统管理员', ip: '203.0.113.7', device: 'curl/7.81', loginTime: '2026-05-28 03:11:42', status: '失败' },
  { id: 'R10', username: 'chen.bo', realName: '陈博', ip: '10.20.5.66', device: 'Chrome / Windows', loginTime: '2026-05-27 14:33:18', status: '成功' }
]

const buildJobs = (): ScheduleJob[] => [
  { id: 'J1', name: '主数据增量同步', cron: '0 */5 * * * ?', lastRun: '2026-05-29 09:15:00', duration: '1.2s', nextRun: '2026-05-29 09:20:00', status: '成功' },
  { id: 'J2', name: '审计日志归档', cron: '0 0 2 * * ?', lastRun: '2026-05-29 02:00:01', duration: '38.5s', nextRun: '2026-05-30 02:00:00', status: '成功' },
  { id: 'J3', name: '编码池预生成', cron: '0 0 0 * * ?', lastRun: '2026-05-29 00:00:08', duration: '4.7s', nextRun: '2026-05-30 00:00:00', status: '成功' },
  { id: 'J4', name: '失效Token清理', cron: '0 0 * * * ?', lastRun: '2026-05-29 09:00:01', duration: '0.8s', nextRun: '2026-05-29 10:00:00', status: '成功' },
  { id: 'J5', name: '分发失败重试', cron: '0 */10 * * * ?', lastRun: '2026-05-29 09:10:00', duration: '12.4s', nextRun: '2026-05-29 09:20:00', status: '执行中' },
  { id: 'J6', name: '冷数据归档', cron: '0 0 3 1 * ?', lastRun: '2026-05-01 03:00:11', duration: '3m 12s', nextRun: '2026-06-01 03:00:00', status: '失败' }
]

// 随机数生成（控制在合理范围）
const rand = (min: number, max: number) => Math.round(min + Math.random() * (max - min))

const refreshMetrics = () => {
  jvmHeapUsed.value = rand(620, 950)
  cpuLoad.value = rand(15, 65)
  dbActive.value = rand(4, 18)
  dbIdle.value = rand(8, 25)
  diskUsed.value = rand(180, 220)
}

const loadAll = () => {
  onlineUsers.value = buildOnline()
  loginRecords.value = buildLogins()
  scheduleJobs.value = buildJobs()
  refreshMetrics()
}

const heapPercent = computed(() => Math.round((jvmHeapUsed.value / jvmHeapMax.value) * 100))
const diskPercent = computed(() => Math.round((diskUsed.value / diskTotal.value) * 100))
const dbPercent = computed(() => Math.round(((dbActive.value + dbIdle.value) / dbMax.value) * 100))

const heapColor = computed(() => heapPercent.value > 80 ? '#dc2626' : heapPercent.value > 60 ? '#d97706' : '#10b981')
const cpuColor = computed(() => cpuLoad.value > 80 ? '#dc2626' : cpuLoad.value > 60 ? '#d97706' : '#10b981')
const diskColor = computed(() => diskPercent.value > 80 ? '#dc2626' : diskPercent.value > 60 ? '#d97706' : '#10b981')

const jobStatusType = (s: string): 'success' | 'warning' | 'danger' => {
  if (s === '成功') return 'success'
  if (s === '执行中') return 'warning'
  return 'danger'
}

const kickOff = async (u: OnlineUser) => {
  try {
    await ElMessageBox.confirm(`确认强制下线用户【${u.realName}】？`, '提示', { type: 'warning' })
    onlineUsers.value = onlineUsers.value.filter(x => x.id !== u.id)
    ElMessage.success('已下线')
  } catch { /* cancelled */ }
}

const triggerJob = (j: ScheduleJob) => {
  ElMessage.success(`任务【${j.name}】已触发执行`)
  j.status = '执行中'
  setTimeout(() => {
    j.status = '成功'
    j.lastRun = new Date().toISOString().replace('T', ' ').slice(0, 19)
    j.duration = `${(Math.random() * 5 + 0.3).toFixed(1)}s`
  }, 1500)
}

onMounted(() => {
  loadAll()
  timer = setInterval(refreshMetrics, 5000)
})
onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">系统监控</h2>
        <p class="page-desc">实时观测 JVM、数据库连接池、CPU、磁盘、用户与定时任务状态</p>
      </div>
      <div class="uptime-pill">
        <span class="dot" /> 系统运行 {{ uptime }}
      </div>
    </div>

    <!-- 核心指标 -->
    <div class="metric-grid">
      <div class="metric-card">
        <div class="metric-head">
          <span class="metric-title">JVM 堆内存</span>
          <span class="metric-val">{{ jvmHeapUsed }} / {{ jvmHeapMax }} MB</span>
        </div>
        <ElProgress
          :percentage="heapPercent"
          :color="heapColor"
          :stroke-width="14"
          :show-text="true"
        />
        <div class="metric-foot">
          <span>已用 {{ heapPercent }}%</span>
          <span>峰值阈值 80%</span>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-head">
          <span class="metric-title">CPU 负载</span>
          <span class="metric-val">{{ cpuLoad }}%</span>
        </div>
        <ElProgress
          :percentage="cpuLoad"
          :color="cpuColor"
          :stroke-width="14"
          :show-text="true"
        />
        <div class="metric-foot">
          <span>核心数 8</span>
          <span>1分钟均值</span>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-head">
          <span class="metric-title">磁盘使用</span>
          <span class="metric-val">{{ diskUsed }} / {{ diskTotal }} GB</span>
        </div>
        <ElProgress
          :percentage="diskPercent"
          :color="diskColor"
          :stroke-width="14"
          :show-text="true"
        />
        <div class="metric-foot">
          <span>挂载点 /opt/mdm</span>
          <span>{{ diskPercent }}%</span>
        </div>
      </div>

      <div class="metric-card metric-card--db">
        <div class="metric-head">
          <span class="metric-title">数据库连接池</span>
          <span class="metric-val">{{ dbActive + dbIdle }} / {{ dbMax }}</span>
        </div>
        <div class="db-bars">
          <div class="db-bar">
            <span>活跃</span>
            <div class="bar"><div class="bar-inner active" :style="{ width: (dbActive / dbMax * 100) + '%' }" /></div>
            <strong>{{ dbActive }}</strong>
          </div>
          <div class="db-bar">
            <span>空闲</span>
            <div class="bar"><div class="bar-inner idle" :style="{ width: (dbIdle / dbMax * 100) + '%' }" /></div>
            <strong>{{ dbIdle }}</strong>
          </div>
          <div class="db-bar">
            <span>容量</span>
            <div class="bar"><div class="bar-inner full" :style="{ width: dbPercent + '%' }" /></div>
            <strong>{{ dbPercent }}%</strong>
          </div>
        </div>
      </div>
    </div>

    <!-- 在线用户 + 登录记录 -->
    <div class="row-grid">
      <div class="card-glow panel">
        <div class="panel-head">
          <span>在线用户</span>
          <span class="panel-count">{{ onlineUsers.length }} 人在线</span>
        </div>
        <ElTable :data="onlineUsers" stripe size="small">
          <ElTableColumn prop="realName" label="姓名" width="100" />
          <ElTableColumn prop="username" label="账号" width="110" />
          <ElTableColumn prop="ip" label="IP" width="130" />
          <ElTableColumn prop="device" label="设备" min-width="160" />
          <ElTableColumn prop="duration" label="在线时长" width="100" />
          <ElTableColumn label="操作" width="90" fixed="right">
            <template #default="scope">
              <ElButton link size="small" type="danger" @click="kickOff(scope.row)">下线</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>

      <div class="card-glow panel">
        <div class="panel-head">
          <span>最近登录记录</span>
          <span class="panel-count">最近 10 条</span>
        </div>
        <ElTable :data="loginRecords" stripe size="small">
          <ElTableColumn prop="loginTime" label="时间" width="160" />
          <ElTableColumn prop="realName" label="用户" width="110" />
          <ElTableColumn prop="ip" label="IP" width="130" />
          <ElTableColumn prop="device" label="设备" min-width="160" />
          <ElTableColumn label="结果" width="80">
            <template #default="scope">
              <span :class="scope.row.status === '成功' ? 'status-active' : 'status-inactive'">
                {{ scope.row.status }}
              </span>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>
    </div>

    <!-- 定时任务 -->
    <div class="card-glow panel" style="margin-top:20px;">
      <div class="panel-head">
        <span>定时任务执行状态</span>
        <span class="panel-count">{{ scheduleJobs.length }} 个任务</span>
      </div>
      <ElTable :data="scheduleJobs" stripe size="small">
        <ElTableColumn prop="name" label="任务名称" width="180" />
        <ElTableColumn prop="cron" label="Cron 表达式" width="180">
          <template #default="scope">
            <span style="font-family:var(--font-mono);font-size:12px;color:var(--color-primary)">{{ scope.row.cron }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="lastRun" label="上次执行" width="170" />
        <ElTableColumn prop="duration" label="耗时" width="100" />
        <ElTableColumn prop="nextRun" label="下次执行" width="170" />
        <ElTableColumn prop="status" label="状态" width="100">
          <template #default="scope">
            <ElTag size="small" :type="jobStatusType(scope.row.status)">
              {{ scope.row.status }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="100" fixed="right">
          <template #default="scope">
            <ElButton link size="small" type="primary" @click="triggerJob(scope.row)">立即执行</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </div>
  </div>
</template>

<style scoped lang="scss">
.uptime-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 20px;
  border: 1px solid rgba(16, 185, 129, 0.35);
  color: var(--color-success);
  background: rgba(16, 185, 129, 0.08);
  font-size: 12px;
  .dot {
    width: 8px; height: 8px; border-radius: 50%;
    background: var(--color-success);
    box-shadow: 0 0 10px var(--color-success);
    animation: pulse 1.6s ease-in-out infinite;
  }
}
@keyframes pulse {
  0%, 100% { opacity: 0.6; transform: scale(0.9); }
  50% { opacity: 1; transform: scale(1.1); }
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}
.metric-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 18px 20px;
  backdrop-filter: blur(10px);
  transition: var(--transition-normal);
  &:hover {
    border-color: var(--border-glow);
    box-shadow: var(--shadow-glow);
  }
}
.metric-head {
  display: flex; justify-content: space-between; align-items: baseline;
  margin-bottom: 12px;
  .metric-title { color: var(--text-secondary); font-size: 13px; }
  .metric-val { color: var(--text-bright); font-size: 14px; font-weight: 600; font-family: var(--font-mono); }
}
.metric-foot {
  display: flex; justify-content: space-between;
  margin-top: 10px;
  font-size: 11px; color: var(--text-muted);
}

.db-bars {
  display: flex; flex-direction: column; gap: 10px;
}
.db-bar {
  display: grid;
  grid-template-columns: 40px 1fr 50px;
  align-items: center; gap: 10px;
  font-size: 12px; color: var(--text-secondary);
  strong { text-align: right; color: var(--text-bright); font-family: var(--font-mono); }
  .bar {
    height: 8px; border-radius: 4px;
    background: rgba(148, 163, 184, 0.15);
    overflow: hidden;
  }
  .bar-inner {
    height: 100%; border-radius: 4px;
    transition: width 0.5s ease;
    &.active { background: linear-gradient(90deg, #00d4ff, #0ea5e9); }
    &.idle { background: linear-gradient(90deg, #10b981, #059669); }
    &.full { background: linear-gradient(90deg, #d97706, #b45309); }
  }
}

.row-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.panel {
  padding: 16px 18px;
}
.panel-head {
  display: flex; align-items: center; justify-content: space-between;
  padding-bottom: 10px;
  margin-bottom: 10px;
  border-bottom: 1px dashed var(--border-color);
  font-size: 14px; font-weight: 500; color: var(--text-primary);
  &::before {
    content: '';
    display: inline-block;
    width: 3px; height: 14px;
    background: var(--color-primary);
    margin-right: 8px;
    vertical-align: middle;
  }
  .panel-count { color: var(--text-muted); font-size: 12px; font-weight: 400; }
}

:deep(.el-progress-bar__outer) {
  background-color: rgba(148, 163, 184, 0.15) !important;
}
:deep(.el-progress__text) {
  color: var(--text-secondary) !important;
}
:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(0, 212, 255, 0.03);
  --el-table-row-hover-bg-color: rgba(0, 212, 255, 0.06);
  --el-table-border-color: var(--border-color);
  --el-table-text-color: var(--text-primary);
  --el-table-header-text-color: var(--text-secondary);
}

@media (max-width: 1100px) {
  .row-grid { grid-template-columns: 1fr; }
}
</style>
