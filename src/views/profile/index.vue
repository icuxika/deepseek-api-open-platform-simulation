<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NCard, NForm, NFormItem, NInput, NButton, NSpace, NAvatar, NDivider, NTag, NPopconfirm, NSpin, useMessage } from 'naive-ui'
import type { FormRules } from 'naive-ui'
import { useUserStore } from '@/stores'
import { oauthApi } from '@/utils/api'
import type { OAuthBinding } from '@/utils/api'

const userStore = useUserStore()
const message = useMessage()

const formRef = ref()
const loading = ref(false)
const bindings = ref<OAuthBinding[]>([])
const formValue = ref({
  username: userStore.user?.username || '',
  email: userStore.user?.email || '',
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' }
  ],
  newPassword: [
    { min: 6, message: '密码长度至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    {
      validator: (_rule, value) => {
        if (value && value !== formValue.value.newPassword) {
          return new Error('两次输入的密码不一致')
        }
        return true
      },
      trigger: 'blur'
    }
  ]
}

const isBound = (provider: string) => {
  return bindings.value.some(b => b.provider === provider)
}

async function loadBindings() {
  try {
    loading.value = true
    bindings.value = await oauthApi.getBindings()
  } catch (error) {
    console.error('加载绑定信息失败:', error)
  } finally {
    loading.value = false
  }
}

function handleSaveProfile() {
  formRef.value?.validate((errors: boolean) => {
    if (!errors) {
      if (userStore.user) {
        userStore.user.username = formValue.value.username
        userStore.user.email = formValue.value.email
      }
      message.success('个人信息已更新')
    }
  })
}

function handleChangePassword() {
  if (!formValue.value.currentPassword) {
    message.error('请输入当前密码')
    return
  }
  if (!formValue.value.newPassword) {
    message.error('请输入新密码')
    return
  }
  if (formValue.value.newPassword !== formValue.value.confirmPassword) {
    message.error('两次输入的密码不一致')
    return
  }
  
  message.success('密码修改成功')
  formValue.value.currentPassword = ''
  formValue.value.newPassword = ''
  formValue.value.confirmPassword = ''
}

async function handleBind(provider: 'gitee' | 'github') {
  try {
    let response
    if (provider === 'gitee') {
      response = await oauthApi.getGiteeAuthUrl()
    } else {
      response = await oauthApi.getGithubAuthUrl()
    }
    window.location.href = response.url + '&state=bind'
  } catch (error) {
    console.error('获取授权链接失败:', error)
    message.error('获取授权链接失败')
  }
}

async function handleUnbind(provider: string) {
  try {
    await oauthApi.unbind(provider)
    message.success('已解除绑定')
    await loadBindings()
  } catch (error) {
    console.error('解除绑定失败:', error)
    message.error('解除绑定失败')
  }
}

onMounted(() => {
  loadBindings()
})
</script>

<template>
  <div class="profile">
    <n-card title="个人设置" style="max-width: 800px;">
      <div class="avatar-section">
        <n-avatar round :size="80" :src="userStore.user?.avatarUrl">
          {{ userStore.user?.username?.charAt(0).toUpperCase() || 'U' }}
        </n-avatar>
      </div>

      <n-form
        ref="formRef"
        :model="formValue"
        :rules="rules"
        label-placement="left"
        label-width="100"
        style="margin-top: 24px;"
      >
        <n-form-item path="username" label="用户名">
          <n-input v-model:value="formValue.username" placeholder="请输入用户名" />
        </n-form-item>
        <n-form-item path="email" label="邮箱">
          <n-input v-model:value="formValue.email" placeholder="请输入邮箱" />
        </n-form-item>
        <n-space>
          <n-button type="primary" @click="handleSaveProfile">保存信息</n-button>
        </n-space>
      </n-form>

      <n-divider />

      <n-card title="修改密码" :bordered="false">
        <n-form
          :model="formValue"
          :rules="rules"
          label-placement="left"
          label-width="100"
        >
          <n-form-item path="currentPassword" label="当前密码">
            <n-input
              v-model:value="formValue.currentPassword"
              type="password"
              placeholder="请输入当前密码"
            />
          </n-form-item>
          <n-form-item path="newPassword" label="新密码">
            <n-input
              v-model:value="formValue.newPassword"
              type="password"
              placeholder="请输入新密码"
            />
          </n-form-item>
          <n-form-item path="confirmPassword" label="确认密码">
            <n-input
              v-model:value="formValue.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
            />
          </n-form-item>
          <n-space>
            <n-button type="primary" @click="handleChangePassword">修改密码</n-button>
          </n-space>
        </n-form>
      </n-card>

      <n-divider />

      <n-card title="第三方账号绑定" :bordered="false">
        <n-spin :show="loading">
          <div class="oauth-bindings">
            <div class="oauth-item">
              <div class="oauth-info">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor" class="oauth-icon">
                  <path d="M11.984 0A12 12 0 0 0 0 12a12 12 0 0 0 12 12 12 12 0 0 0 12-12A12 12 0 0 0 12 0a12 12 0 0 0-.016 0zm6.09 5.333c.328 0 .593.266.592.593v1.482a.594.594 0 0 1-.593.592H9.777c-.982 0-1.778.796-1.778 1.778v5.63c0 .327.266.592.593.592h5.63c.327 0 .593-.265.593-.593v-1.481a.593.593 0 0 0-.593-.593h-3.556a.593.593 0 0 1-.593-.593V9.778c0-.327.266-.593.593-.593h5.926c.327 0 .593.266.593.593v6.815a2.37 2.37 0 0 1-2.37 2.37H6.518a.593.593 0 0 1-.593-.593V9.778a4.444 4.444 0 0 1 4.444-4.445h7.705z"/>
                </svg>
                <span class="oauth-name">码云</span>
                <n-tag v-if="isBound('GITEE')" type="success" size="small">已绑定</n-tag>
              </div>
              <div class="oauth-action">
                <n-button v-if="!isBound('GITEE')" size="small" @click="handleBind('gitee')">
                  绑定
                </n-button>
                <n-popconfirm v-else @positive-click="handleUnbind('GITEE')">
                  <template #trigger>
                    <n-button size="small" type="warning">解绑</n-button>
                  </template>
                  确定要解除码云账号绑定吗？
                </n-popconfirm>
              </div>
            </div>

            <div class="oauth-item">
              <div class="oauth-info">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor" class="oauth-icon">
                  <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
                </svg>
                <span class="oauth-name">GitHub</span>
                <n-tag v-if="isBound('GITHUB')" type="success" size="small">已绑定</n-tag>
              </div>
              <div class="oauth-action">
                <n-button v-if="!isBound('GITHUB')" size="small" @click="handleBind('github')">
                  绑定
                </n-button>
                <n-popconfirm v-else @positive-click="handleUnbind('GITHUB')">
                  <template #trigger>
                    <n-button size="small" type="warning">解绑</n-button>
                  </template>
                  确定要解除 GitHub 账号绑定吗？
                </n-popconfirm>
              </div>
            </div>
          </div>
        </n-spin>
      </n-card>
    </n-card>
  </div>
</template>

<style scoped>
.profile {
  max-width: 800px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0;
}

.oauth-bindings {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.oauth-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
}

.oauth-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.oauth-icon {
  color: rgba(255, 255, 255, 0.8);
}

.oauth-name {
  font-weight: 500;
}
</style>
