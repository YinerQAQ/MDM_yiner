<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAuditLogs, getLoginLogs } from '../api/system'
import type { SysAuditLog, SysLoginLog, PageResult } from '../api/system'

const activeTab = ref('audit')

// 操作日志
const auditLogs = ref<SysAuditLog[]>([])
const auditTotal = ref(0)
const auditPage = ref(1)
const auditSize = ref(10)
const auditQuery = reactive({
  username: '',
  operation: '',
  startTime: '',
  endTime: ''
})

// 登录日志
const loginLogs = ref<SysLoginLog[]>([])
const loginTotal = ref(0)
const loginPage = ref(1)
const loginSize = ref(10)
const loginQuery = reactive({
  username: '',
  status: '',
  startTime: '',
  endTime: ''
})

const loadAuditLogs = async () => {
  try {
    const params: Record<string, string | number> = {
      page: auditPage.value,
      size: auditSize.value
    }
    if (auditQuery.username) params.username = auditQuery.username
    if (auditQuery.operation) params.operation = auditQuery.operation
    if (auditQuery.startTime) params.startTime = auditQuery.startTime
    if (auditQuery.endTime) params.endTime = auditQuery.endTime
    const res = await getAuditLogs(params)
    const page = res.data.data as PageResult<SysAuditLog>
    auditLogs.value = page.records
    auditTotal.value = page.total
  } catch {
    ElMessage.error('加载操作日志失败')
  }
}

const loadLoginLogs = async () => {
  try {
    const params: Record<string, string | number> = {
      page: loginPage.value,
      size: loginSize.value
    }
    if (loginQuery.username) params.username = loginQuery.username
    if (loginQuery.status) params.status = loginQuery.status
    if (loginQuery.startTime) params.startTime = loginQuery.startTime
    if (loginQuery.endTime) params.endTime = loginQuery.endTime
    const res = await getLoginLogs(params)
    const page = res.data.data as PageResult<SysLoginLog>
    loginLogs.value = page.records
    loginTotal.value = page.total
  } catch {
    ElMessage.error('加载登录日志失败')
  }
}

const searchAuditLogs = () => {
  auditPage.value = 1
  loadAuditLogs()
}

const searchLoginLogs = () => {
  loginPage.value = 1
  loadLoginLogs()
}

const resetAuditQuery = () => {
  auditQuery.username = ''
  auditQuery.operation = ''
  auditQuery.startTime = ''
  auditQuery.endTime = ''
  searchAuditLogs()
}

const resetLoginQuery = () => {
  loginQuery.username = ''
  loginQuery.status = ''
  loginQuery.startTime = ''
  loginQuery.endTime = ''
  searchLoginLogs()
}

const handleAuditPageChange = (page: number) => {
  auditPage.value = page
  loadAuditLogs()
}

const handleLoginPageChange = (page: number) => {
  loginPage.value = page
  loadLoginLogs()
}

onMounted(() => {
  loadAuditLogs()
  loadLoginLogs()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">审计日志</h2>
        <p class="page-desc">查看系统操作审计日志记录</p>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <!-- 操作日志Tab -->
      <el-tab-pane label="操作日志" name="audit">
        <div class="search-card">
          <el-form :inline="true" :model="auditQuery">
            <el-form-item label="用户名">
              <el-input v-model="auditQuery.username" placeholder="输入用户名" clearable style="width: 160px" />
            </el-form-item>
            <el-form-item label="操作类型">
              <el-input v-model="auditQuery.operation" placeholder="输入操作类型" clearable style="width: 160px" />
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="auditQuery.startTime"
                type="datetime"
                placeholder="开始时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 180px"
              />
              <span style="margin: 0 8px; color: var(--text-secondary);">至</span>
              <el-date-picker
                v-model="auditQuery.endTime"
                type="datetime"
                placeholder="结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 180px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchAuditLogs">搜索</el-button>
              <el-button @click="resetAuditQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="table-card">
          <el-table :data="auditLogs" stripe>
            <el-table-column prop="username" label="用户" width="120" />
            <el-table-column prop="operation" label="操作" width="140" />
            <el-table-column prop="method" label="方法" min-width="200" show-overflow-tooltip />
            <el-table-column prop="ip" label="IP" width="140" />
            <el-table-column prop="duration" label="耗时(ms)" width="100" />
            <el-table-column prop="result" label="结果" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.result === '成功' ? 'success' : 'danger'" size="small">
                  {{ scope.row.result }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="180" />
          </el-table>
          <div style="margin-top: 16px; display: flex; justify-content: flex-end;">
            <el-pagination
              :current-page="auditPage"
              :page-size="auditSize"
              :total="auditTotal"
              layout="total, prev, pager, next"
              @current-change="handleAuditPageChange"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 登录日志Tab -->
      <el-tab-pane label="登录日志" name="login">
        <div class="search-card">
          <el-form :inline="true" :model="loginQuery">
            <el-form-item label="用户名">
              <el-input v-model="loginQuery.username" placeholder="输入用户名" clearable style="width: 160px" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="loginQuery.status" placeholder="选择状态" clearable style="width: 120px">
                <el-option label="成功" value="成功" />
                <el-option label="失败" value="失败" />
              </el-select>
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="loginQuery.startTime"
                type="datetime"
                placeholder="开始时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 180px"
              />
              <span style="margin: 0 8px; color: var(--text-secondary);">至</span>
              <el-date-picker
                v-model="loginQuery.endTime"
                type="datetime"
                placeholder="结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 180px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchLoginLogs">搜索</el-button>
              <el-button @click="resetLoginQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="table-card">
          <el-table :data="loginLogs" stripe>
            <el-table-column prop="username" label="用户" width="120" />
            <el-table-column prop="ip" label="IP" width="140" />
            <el-table-column prop="location" label="地点" width="120" />
            <el-table-column prop="browser" label="浏览器" width="120" />
            <el-table-column prop="os" label="操作系统" width="120" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.status === '成功' ? 'success' : 'danger'" size="small">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="message" label="失败原因" min-width="160" show-overflow-tooltip />
            <el-table-column prop="createTime" label="时间" width="180" />
          </el-table>
          <div style="margin-top: 16px; display: flex; justify-content: flex-end;">
            <el-pagination
              :current-page="loginPage"
              :page-size="loginSize"
              :total="loginTotal"
              layout="total, prev, pager, next"
              @current-change="handleLoginPageChange"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped lang="scss">
</style>
