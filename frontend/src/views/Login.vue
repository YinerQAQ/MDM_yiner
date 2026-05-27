<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElForm, ElFormItem, ElInput, ElButton, ElIcon, ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
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
  <div class="login-page">
    <!-- 动态背景 -->
    <div class="bg-grid">
      <div class="grid-line" v-for="i in 20" :key="'h' + i" :style="{ top: (i * 5) + '%' }"></div>
      <div class="grid-line vertical" v-for="i in 20" :key="'v' + i" :style="{ left: (i * 5) + '%' }"></div>
      <div class="particle" v-for="i in 6" :key="'p' + i" :style="{ '--delay': (i * 2) + 's', '--x': (i * 15 + 5) + '%', '--y': (i * 12 + 10) + '%' }"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- Logo -->
      <div class="login-logo">
        <span class="logo-mdm">MDM</span>
        <h2 class="logo-title">主数据管理系统</h2>
        <p class="logo-subtitle">Master Data Management System</p>
      </div>

      <!-- 表单 -->
      <ElForm :model="form" class="login-form" @submit.prevent="handleLogin">
        <ElFormItem>
          <ElInput
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            class="login-input"
          >
            <template #prefix>
              <ElIcon class="input-icon"><User /></ElIcon>
            </template>
          </ElInput>
        </ElFormItem>
        <ElFormItem>
          <ElInput
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            class="login-input"
            show-password
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <ElIcon class="input-icon"><Lock /></ElIcon>
            </template>
          </ElInput>
        </ElFormItem>
        <ElFormItem>
          <ElButton
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </ElButton>
        </ElFormItem>
      </ElForm>

      <!-- 底部版权 -->
      <div class="login-footer">
        迈克科技 &copy; 2024
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0d1b2a 0%, #1b2838 50%, #0d1b2a 100%);
  position: relative;
  overflow: hidden;
}

// 网格线背景
.bg-grid {
  position: absolute;
  inset: 0;
  opacity: 0.06;
  pointer-events: none;

  .grid-line {
    position: absolute;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, var(--color-primary), transparent);
    animation: gridPulse 4s ease-in-out infinite;

    &.vertical {
      top: 0;
      bottom: 0;
      left: auto;
      right: auto;
      width: 1px;
      height: 100%;
      background: linear-gradient(180deg, transparent, var(--color-primary), transparent);
    }
  }
}

@keyframes gridPulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

// 浮动光点
.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--color-primary);
  left: var(--x);
  top: var(--y);
  animation: float 8s ease-in-out infinite;
  animation-delay: var(--delay);
  box-shadow: 0 0 12px var(--color-primary), 0 0 30px rgba(0, 212, 255, 0.3);
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.4;
  }
  50% {
    transform: translateY(-30px) scale(1.5);
    opacity: 1;
  }
}

// 登录卡片
.login-card {
  width: 420px;
  padding: 48px 40px 32px;
  background: rgba(27, 40, 56, 0.7);
  backdrop-filter: blur(20px);
  border: 1px solid var(--border-glow);
  border-radius: 16px;
  box-shadow:
    0 0 40px rgba(0, 212, 255, 0.1),
    0 8px 32px rgba(0, 0, 0, 0.4);
  position: relative;
  z-index: 1;
  animation: cardFadeIn 0.6s ease;
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

// Logo
.login-logo {
  text-align: center;
  margin-bottom: 36px;

  .logo-mdm {
    font-size: 48px;
    font-weight: 800;
    letter-spacing: 6px;
    background: linear-gradient(135deg, #00d4ff, #0ea5e9, #6366f1);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    display: block;
    text-shadow: none;
    filter: drop-shadow(0 0 20px rgba(0, 212, 255, 0.3));
  }

  .logo-title {
    font-size: 18px;
    font-weight: 500;
    color: var(--text-primary);
    margin: 12px 0 4px;
    letter-spacing: 4px;
  }

  .logo-subtitle {
    font-size: 12px;
    color: var(--text-muted);
    letter-spacing: 2px;
  }
}

// 表单
.login-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  .login-input {
    :deep(.el-input__wrapper) {
      background: rgba(13, 27, 42, 0.6);
      border: 1px solid var(--border-color);
      border-radius: 8px;
      box-shadow: none !important;
      padding: 4px 12px;
      transition: var(--transition-normal);

      &.is-focus {
        border-color: var(--color-primary);
        box-shadow: 0 0 0 2px rgba(0, 212, 255, 0.15) !important;
      }
    }

    :deep(.el-input__inner) {
      color: var(--text-primary);
      &::placeholder {
        color: var(--text-muted);
      }
    }

    .input-icon {
      color: var(--text-muted);
      font-size: 16px;
    }
  }
}

// 登录按钮
.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  border-radius: 8px;
  background: linear-gradient(135deg, #00d4ff, #0ea5e9);
  border: none;
  color: #fff;
  transition: var(--transition-normal);

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(0, 212, 255, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}

// 底部
.login-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
  color: var(--text-muted);
  font-size: 12px;
  letter-spacing: 1px;
}
</style>
