<script setup lang="ts">
import { computed } from 'vue'
import { NCard, NGrid, NGi, NStatistic, NProgress, NIcon } from 'naive-ui'
import { ChatbubbleOutline, CalculatorOutline, FlashOutline, WalletOutline } from '@vicons/ionicons5'
import { useUserStore } from '@/stores'

const userStore = useUserStore()

const usagePercentage = computed(() => {
  const total = userStore.usageStats.totalTokens
  const limit = 5000000
  return Math.min((total / limit) * 100, 100)
})
</script>

<template>
  <div class="dashboard">
    <n-grid :cols="4" :x-gap="16" :y-gap="16">
      <n-gi>
        <n-card>
          <div class="stat-card">
            <n-icon size="32" color="#63e2b7">
              <WalletOutline />
            </n-icon>
            <n-statistic label="账户余额" :value="userStore.user?.balance || 0" :precision="2">
              <template #prefix>¥</template>
            </n-statistic>
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card>
          <div class="stat-card">
            <n-icon size="32" color="#70c0e8">
              <ChatbubbleOutline />
            </n-icon>
            <n-statistic label="总请求次数" :value="userStore.usageStats.requestCount" />
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card>
          <div class="stat-card">
            <n-icon size="32" color="#f2c97d">
              <CalculatorOutline />
            </n-icon>
            <n-statistic label="总Token数" :value="userStore.usageStats.totalTokens" />
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card>
          <div class="stat-card">
            <n-icon size="32" color="#e88080">
              <FlashOutline />
            </n-icon>
            <n-statistic label="API Keys" :value="userStore.apiKeys.length" />
          </div>
        </n-card>
      </n-gi>
    </n-grid>

    <n-grid :cols="2" :x-gap="16" :y-gap="16" style="margin-top: 16px;">
      <n-gi>
        <n-card title="用量统计">
          <div class="usage-stats">
            <div class="usage-item">
              <span>提示词 Tokens</span>
              <span>{{ userStore.usageStats.promptTokens.toLocaleString() }}</span>
            </div>
            <div class="usage-item">
              <span>补全 Tokens</span>
              <span>{{ userStore.usageStats.completionTokens.toLocaleString() }}</span>
            </div>
            <div class="usage-item">
              <span>总 Tokens</span>
              <span>{{ userStore.usageStats.totalTokens.toLocaleString() }}</span>
            </div>
            <div style="margin-top: 16px;">
              <span>本月使用进度</span>
              <n-progress
                type="line"
                :percentage="usagePercentage"
                :indicator-placement="'inside'"
                processing
                style="margin-top: 8px;"
              />
            </div>
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card title="快速开始">
          <div class="quick-start">
            <div class="code-block">
              <pre><code>curl https://api.deepseek.com/v1/chat/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -d '{
    "model": "deepseek-chat",
    "messages": [
      {"role": "user", "content": "Hello"}
    ]
  }'</code></pre>
            </div>
            <p style="color: rgba(255,255,255,0.6); margin-top: 12px;">
              将 YOUR_API_KEY 替换为您的 API Key 即可开始使用
            </p>
          </div>
        </n-card>
      </n-gi>
    </n-grid>
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 1400px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
}

.usage-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.usage-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.usage-item:last-child {
  border-bottom: none;
}

.quick-start {
  padding: 8px 0;
}

.code-block {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 8px;
  padding: 16px;
  overflow-x: auto;
}

.code-block pre {
  margin: 0;
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  line-height: 1.5;
}

.code-block code {
  color: #a6e22e;
}
</style>
