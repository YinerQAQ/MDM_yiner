<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElForm, ElFormItem, ElInput, ElButton, ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({
  username: '',
  password: ''
})

const loading = ref(false)

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.error('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    await authStore.login(form.value.username, form.value.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    ElMessage.error('登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (authStore.isLoggedIn()) {
    router.push('/')
  }
})
</script>

<template>
  <div class="login-container">
    <div class="login-form">
      <div class="logo">
        <h1>MDM主数据管理系统</h1>
        <p>Master Data Management System</p>
      </div>
      <ElForm :model="form" label-width="80px">
        <ElFormItem label="用户名">
          <ElInput v-model="form.username" placeholder="请输入用户名" />
        </ElFormItem>
        <ElFormItem label="密码">
          <ElInput v-model="form.password" type="password" placeholder="请输入密码" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleLogin" :loading="loading" style="width: 100%">
            登录
          </ElButton>
        </ElFormItem>
      </ElForm>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-form {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  width: 400px;
}

.logo {
  text-align: center;
  margin-bottom: 30px;
}

.logo h1 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
}

.logo p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style>