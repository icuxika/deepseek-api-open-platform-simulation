<script setup lang="ts">
import { NCard, NGrid, NGi, NTag, NSpace, NRadioGroup, NRadioButton } from 'naive-ui'

const plans = [
  {
    name: 'DeepSeek Chat',
    model: 'deepseek-chat',
    description: '通用对话模型，适合日常对话和文本生成任务',
    inputPrice: 0.001,
    outputPrice: 0.002,
    unit: '每 1K tokens',
    features: ['支持长上下文', '多轮对话', '流式输出', '函数调用'],
    recommended: false
  },
  {
    name: 'DeepSeek Coder',
    model: 'deepseek-coder',
    description: '专为代码生成和理解优化的模型',
    inputPrice: 0.0015,
    outputPrice: 0.003,
    unit: '每 1K tokens',
    features: ['代码补全', '代码解释', 'Bug 修复', '多语言支持'],
    recommended: true
  },
  {
    name: 'DeepSeek Reasoner',
    model: 'deepseek-reasoner',
    description: '推理增强模型，适合复杂推理任务',
    inputPrice: 0.002,
    outputPrice: 0.004,
    unit: '每 1K tokens',
    features: ['复杂推理', '数学计算', '逻辑分析', '深度思考'],
    recommended: false
  }
]
</script>

<template>
  <div class="pricing-page">
    <n-card style="margin-bottom: 24px;">
      <n-space justify="center">
        <span>选择计费方式：</span>
        <n-radio-group default-value="tokens">
          <n-radio-button value="tokens">按 Tokens</n-radio-button>
          <n-radio-button value="requests">按请求次数</n-radio-button>
        </n-radio-group>
      </n-space>
    </n-card>

    <n-grid :cols="3" :x-gap="24" :y-gap="24">
      <n-gi v-for="plan in plans" :key="plan.model">
        <n-card class="pricing-card" :class="{ recommended: plan.recommended }">
          <div class="plan-header">
            <h3>{{ plan.name }}</h3>
            <n-tag v-if="plan.recommended" type="success" size="small">推荐</n-tag>
          </div>
          <p class="description">{{ plan.description }}</p>
          
          <div class="price-section">
            <div class="price-item">
              <span class="label">输入</span>
              <span class="price">¥{{ plan.inputPrice }}</span>
              <span class="unit">/ 1K tokens</span>
            </div>
            <div class="price-item">
              <span class="label">输出</span>
              <span class="price">¥{{ plan.outputPrice }}</span>
              <span class="unit">/ 1K tokens</span>
            </div>
          </div>

          <div class="features">
            <div v-for="feature in plan.features" :key="feature" class="feature-item">
              <span>✓</span>
              <span>{{ feature }}</span>
            </div>
          </div>
        </n-card>
      </n-gi>
    </n-grid>

    <n-card title="价格计算器" style="margin-top: 24px;">
      <div class="calculator">
        <p>示例计算：</p>
        <ul>
          <li>1,000,000 tokens 输入 + 500,000 tokens 输出 (DeepSeek Chat)</li>
          <li>费用 = 1000 × ¥0.001 + 500 × ¥0.002 = ¥1.00 + ¥1.00 = ¥2.00</li>
        </ul>
      </div>
    </n-card>

    <n-card title="常见问题" style="margin-top: 24px;">
      <div class="faq">
        <div class="faq-item">
          <h4>如何计算 Token 数量？</h4>
          <p>Token 是文本的最小单位。通常，1 个英文单词约等于 1 个 Token，1 个中文字符约等于 2 个 Token。</p>
        </div>
        <div class="faq-item">
          <h4>是否有最低消费？</h4>
          <p>没有最低消费限制，您可以按需充值，余额永久有效。</p>
        </div>
        <div class="faq-item">
          <h4>支持哪些支付方式？</h4>
          <p>目前支持支付宝和微信支付。</p>
        </div>
      </div>
    </n-card>
  </div>
</template>

<style scoped>
.pricing-page {
  max-width: 1200px;
}

.pricing-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.pricing-card.recommended {
  border: 2px solid #63e2b7;
}

.plan-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.plan-header h3 {
  margin: 0;
}

.description {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  margin-bottom: 24px;
}

.price-section {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
}

.price-item {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.price-item:last-child {
  margin-bottom: 0;
}

.price-item .label {
  width: 40px;
  color: rgba(255, 255, 255, 0.6);
}

.price-item .price {
  font-size: 24px;
  font-weight: bold;
  color: #63e2b7;
}

.price-item .unit {
  color: rgba(255, 255, 255, 0.4);
  font-size: 12px;
}

.features {
  flex: 1;
  margin-bottom: 24px;
}

.feature-item {
  display: flex;
  gap: 8px;
  padding: 8px 0;
  color: rgba(255, 255, 255, 0.8);
}

.feature-item span:first-child {
  color: #63e2b7;
}

.calculator {
  padding: 16px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
}

.calculator ul {
  margin: 8px 0 0;
  padding-left: 20px;
}

.calculator li {
  margin: 8px 0;
  color: rgba(255, 255, 255, 0.8);
}

.faq {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.faq-item h4 {
  margin: 0 0 8px;
}

.faq-item p {
  margin: 0;
  color: rgba(255, 255, 255, 0.6);
}
</style>
