<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NSpin, NResult, useMessage } from 'naive-ui'
import { useUserStore } from '@/stores'
import { oauthApi } from '@/utils/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const message = useMessage()

onMounted(async () => {
  const provider = route.params.provider as string
  const code = route.query.code as string
  const state = route.query.state as string | undefined

  if (!code) {
    router.push('/login')
    return
  }

  const isBindMode = state?.startsWith('bind:')

  try {
    let response
    
    if (provider === 'gitee') {
      response = await oauthApi.giteeCallback(code, state)
    } else if (provider === 'github') {
      response = await oauthApi.githubCallback(code, state)
    } else {
      throw new Error('不支持的登录方式')
    }

    userStore.setToken(response.token)
    userStore.setUser(response.user as any)
    await userStore.loadUserData()
    
    if (isBindMode) {
      message.success('账号绑定成功')
      router.push('/profile')
    } else {
      router.push('/dashboard')
    }
  } catch (error: any) {
    console.error('OAuth login failed:', error)
    if (isBindMode) {
      message.error(error.message || '绑定失败')
      router.push('/profile')
    } else {
      router.push('/login')
    }
  }
})
</script>

<template>
  <div class="callback-container">
    <n-spin size="large">
      <template #description>
        <n-result
          status="info"
          title="正在处理"
          description="请稍候，正在处理第三方登录..."
        />
      </template>
    </n-spin>
  </div>
</template>

<style scoped>
.callback-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}
</style>
