<script setup lang="ts">
import { ref } from 'vue'
import { NCard, NGrid, NGi, NButton, NSpace, NRadioGroup, NRadioButton, NInputNumber, NAlert, useMessage } from 'naive-ui'
import { useUserStore } from '@/stores'

const userStore = useUserStore()
const message = useMessage()

const selectedAmount = ref<number | null>(null)
const customAmount = ref<number | null>(null)
const paymentMethod = ref<'alipay' | 'wechat'>('alipay')
const loading = ref(false)

const presetAmounts = [10, 50, 100, 200, 500, 1000]

function selectAmount(amount: number) {
  selectedAmount.value = amount
  customAmount.value = null
}

function handleCustomAmount(value: number | null) {
  customAmount.value = value
  selectedAmount.value = null
}

function getFinalAmount(): number {
  if (customAmount.value) return customAmount.value
  if (selectedAmount.value) return selectedAmount.value
  return 0
}

async function handleRecharge() {
  const amount = getFinalAmount()
  if (amount <= 0) {
    message.error('请选择或输入充值金额')
    return
  }
  
  loading.value = true
  try {
    await userStore.recharge(amount, paymentMethod.value)
    message.success(`充值成功！已充值 ¥${amount}`)
    selectedAmount.value = null
    customAmount.value = null
  } catch (error) {
    message.error('充值失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="recharge">
    <n-card title="账户充值">
      <n-grid :cols="3" :x-gap="16" :y-gap="16">
        <n-gi v-for="amount in presetAmounts" :key="amount">
          <div
            class="amount-card"
            :class="{ active: selectedAmount === amount }"
            @click="selectAmount(amount)"
          >
            <div class="amount">¥{{ amount }}</div>
          </div>
        </n-gi>
        <n-gi>
          <div class="amount-card custom" :class="{ active: customAmount !== null }">
            <div class="label">自定义金额</div>
            <n-input-number
              v-model:value="customAmount"
              :min="1"
              :max="10000"
              placeholder="输入金额"
              @update:value="handleCustomAmount"
            />
          </div>
        </n-gi>
      </n-grid>

      <n-card title="支付方式" style="margin-top: 24px;" :bordered="false">
        <n-radio-group v-model:value="paymentMethod">
          <n-space>
            <n-radio-button value="alipay">
              支付宝
            </n-radio-button>
            <n-radio-button value="wechat">
              微信支付
            </n-radio-button>
          </n-space>
        </n-radio-group>
      </n-card>

      <n-alert type="info" style="margin-top: 24px;">
        当前账户余额: ¥{{ userStore.user?.balance?.toFixed(2) || '0.00' }}
      </n-alert>

      <div class="actions">
        <n-space align="center">
          <span>充值金额: <strong>¥{{ getFinalAmount() }}</strong></span>
          <n-button
            type="primary"
            size="large"
            :loading="loading"
            :disabled="getFinalAmount() <= 0"
            @click="handleRecharge"
          >
            立即充值
          </n-button>
        </n-space>
      </div>
    </n-card>
  </div>
</template>

<style scoped>
.recharge {
  max-width: 800px;
}

.amount-card {
  padding: 24px;
  border: 2px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.amount-card:hover {
  border-color: #63e2b7;
}

.amount-card.active {
  border-color: #63e2b7;
  background: rgba(99, 226, 183, 0.1);
}

.amount-card .amount {
  font-size: 24px;
  font-weight: bold;
}

.amount-card.custom {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.amount-card.custom .label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.actions {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: flex-end;
}
</style>
