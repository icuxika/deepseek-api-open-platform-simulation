<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NForm, NFormItem, NInput, NButton, NSpace, NDivider, useMessage } from 'naive-ui'
import type { FormRules } from 'naive-ui'
import { useUserStore } from '@/stores'
import { oauthApi } from '@/utils/api'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

const formRef = ref()
const loading = ref(false)
const oauthLoading = ref<string | null>(null)
const formValue = ref({
  email: '',
  username: '',
  password: '',
  confirmPassword: ''
})

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule, value) => {
        if (value !== formValue.value.password) {
          return new Error('两次输入的密码不一致')
        }
        return true
      },
      trigger: 'blur'
    }
  ]
}

async function handleRegister() {
  try {
    await formRef.value?.validate()
    loading.value = true
    
    const success = await userStore.register(
      formValue.value.email,
      formValue.value.username,
      formValue.value.password
    )
    if (success) {
      message.success('注册成功')
      router.push('/dashboard')
    } else {
      message.error('注册失败')
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

async function handleOAuthLogin(provider: 'gitee' | 'github') {
  try {
    oauthLoading.value = provider
    
    let response
    if (provider === 'gitee') {
      response = await oauthApi.getGiteeAuthUrl()
    } else {
      response = await oauthApi.getGithubAuthUrl()
    }
    
    window.location.href = response.url
  } catch (error) {
    console.error('OAuth login failed:', error)
    message.error('获取授权链接失败')
    oauthLoading.value = null
  }
}

function goToLogin() {
  router.push('/login')
}
</script>

<template>
  <div class="register-container">
    <div class="register-card">
      <div class="logo">
        <h1>DeepSeek API</h1>
        <p>创建您的账号</p>
      </div>
      <n-card :bordered="false" style="background: transparent;">
        <n-form ref="formRef" :model="formValue" :rules="rules">
          <n-form-item path="email" label="邮箱">
            <n-input v-model:value="formValue.email" placeholder="请输入邮箱" size="large" />
          </n-form-item>
          <n-form-item path="username" label="用户名">
            <n-input v-model:value="formValue.username" placeholder="请输入用户名" size="large" />
          </n-form-item>
          <n-form-item path="password" label="密码">
            <n-input
              v-model:value="formValue.password"
              type="password"
              placeholder="请输入密码"
              size="large"
            />
          </n-form-item>
          <n-form-item path="confirmPassword" label="确认密码">
            <n-input
              v-model:value="formValue.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              @keyup.enter="handleRegister"
            />
          </n-form-item>
          <n-space vertical :size="16">
            <n-button type="primary" block size="large" :loading="loading" @click="handleRegister">
              注册
            </n-button>
            <n-button text block @click="goToLogin">
              已有账号？立即登录
            </n-button>
          </n-space>
        </n-form>
        
        <n-divider style="margin: 24px 0;">第三方登录</n-divider>
        
        <n-space vertical :size="12">
          <n-button 
            block 
            size="large" 
            :loading="oauthLoading === 'gitee'"
            :disabled="oauthLoading !== null"
            @click="handleOAuthLogin('gitee')"
          >
            <template #icon>
              <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                <path d="M11.984 0A12 12 0 0 0 0 12a12 12 0 0 0 12 12 12 12 0 0 0 12-12A12 12 0 0 0 12 0a12 12 0 0 0-.016 0zm6.09 5.333c.328 0 .593.266.592.593v1.482a.594.594 0 0 1-.593.592H9.777c-.982 0-1.778.796-1.778 1.778v5.63c0 .327.266.592.593.592h5.63c.327 0 .593-.265.593-.593v-1.481a.593.593 0 0 0-.593-.593h-3.556a.593.593 0 0 1-.593-.593V9.778c0-.327.266-.593.593-.593h5.926c.327 0 .593.266.593.593v6.815a2.37 2.37 0 0 1-2.37 2.37H6.518a.593.593 0 0 1-.593-.593V9.778a4.444 4.444 0 0 1 4.444-4.445h7.705z"/>
              </svg>
            </template>
            码云登录
          </n-button>
          <n-button 
            block 
            size="large" 
            :loading="oauthLoading === 'github'"
            :disabled="oauthLoading !== null"
            @click="handleOAuthLogin('github')"
          >
            <template #icon>
              <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
              </svg>
            </template>
            GitHub 登录
          </n-button>
        </n-space>
      </n-card>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

.register-card {
  width: 400px;
  padding: 40px;
}

.logo {
  text-align: center;
  margin-bottom: 40px;
}

.logo h1 {
  font-size: 32px;
  font-weight: bold;
  color: #fff;
  margin: 0;
}

.logo p {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
  margin: 8px 0 0;
}
</style>
