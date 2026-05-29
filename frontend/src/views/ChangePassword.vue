<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { changePassword } from '../api/profile'
import { useAuthStore } from '../stores/auth'
import {
  ElForm,
  ElFormItem,
  ElInput,
  ElButton,
  ElProgress,
  ElMessage,
  ElIcon
} from 'element-plus'
import { Lock, Check, Key } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码强度计算
const passwordStrength = computed(() => {
  const pwd = form.value.newPassword
  if (!pwd) return 0
  let score = 0
  if (pwd.length >= 6) score += 25
  if (pwd.length >= 10) score += 25
  if (/[A-Z]/.test(pwd) && /[a-z]/.test(pwd)) score += 25
  if (/[0-9]/.test(pwd) && /[^A-Za-z0-9]/.test(pwd)) score += 25
  return score
})

const strengthLabel = computed(() => {
  const s = passwordStrength.value
  if (s <= 25) return '弱'
  if (s <= 50) return '中'
  if (s <= 75) return '强'
  return '很强'
})

const strengthColor = computed(() => {
  const s = passwordStrength.value
  if (s <= 25) return '#ef4444'
  if (s <= 50) return '#f59e0b'
  if (s <= 75) return '#10b981'
  return '#00d4ff'
})

const rules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await changePassword({
      oldPassword: form.value.oldPassword,
      newPassword: form.value.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    // 清除登录态，跳转到登录页
    authStore.clearAuth()
    router.push('/login')
  } catch {
    // 错误已在拦截器中提示
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="change-password-page">
    <div class="password-card">
      <div class="card-header">
        <ElIcon :size="28" class="header-icon"><Key /></ElIcon>
        <h2>修改密码</h2>
      </div>

      <div class="card-body">
        <ElForm
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          style="max-width: 460px"
        >
          <ElFormItem label="原密码" prop="oldPassword">
            <ElInput
              v-model="form.oldPassword"
              type="password"
              placeholder="请输入原密码"
              show-password
              :prefix-icon="Lock"
            />
          </ElFormItem>

          <ElFormItem label="新密码" prop="newPassword">
            <ElInput
              v-model="form.newPassword"
              type="password"
              placeholder="请输入新密码（至少6位）"
              show-password
              :prefix-icon="Lock"
            />
          </ElFormItem>

          <!-- 密码强度指示器 -->
          <ElFormItem v-if="form.newPassword" label="密码强度">
            <div class="strength-wrapper">
              <ElProgress
                :percentage="passwordStrength"
                :color="strengthColor"
                :stroke-width="8"
                :show-text="false"
              />
              <span class="strength-text" :style="{ color: strengthColor }">{{ strengthLabel }}</span>
            </div>
          </ElFormItem>

          <ElFormItem label="确认密码" prop="confirmPassword">
            <ElInput
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
              :prefix-icon="Lock"
            />
          </ElFormItem>

          <ElFormItem>
            <ElButton type="primary" :loading="loading" @click="handleSubmit">
              <ElIcon><Check /></ElIcon>确认修改
            </ElButton>
          </ElFormItem>
        </ElForm>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.change-password-page {
  padding: 24px;
  min-height: 100%;
  display: flex;
  align-items: flex-start;
  justify-content: center;
}

.password-card {
  width: 100%;
  max-width: 600px;
  margin-top: 40px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  backdrop-filter: blur(12px);
  box-shadow: var(--shadow-md);
}

.card-header {
  padding: 24px 32px;
  display: flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.1), rgba(99, 102, 241, 0.1));
  border-bottom: 1px solid var(--border-color);

  .header-icon {
    color: var(--color-primary);
  }

  h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
  }
}

.card-body {
  padding: 32px;

  :deep(.el-form-item__label) {
    color: var(--text-secondary);
  }

  :deep(.el-input__inner) {
    color: var(--text-primary);
  }
}

.strength-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;

  :deep(.el-progress) {
    flex: 1;
  }

  .strength-text {
    font-size: 13px;
    font-weight: 600;
    min-width: 28px;
  }
}
</style>
