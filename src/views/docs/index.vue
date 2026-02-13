<script setup lang="ts">
import { NCard, NTabs, NTabPane, NCode, NCollapse, NCollapseItem, NTag, NSpace } from 'naive-ui'

const endpoints = [
  {
    method: 'POST' as const,
    path: '/v1/chat/completions',
    description: '创建聊天补全',
    params: {
      model: { type: 'string', required: true, description: '模型名称，如 deepseek-chat' },
      messages: { type: 'array', required: true, description: '消息数组' },
      temperature: { type: 'number', required: false, description: '采样温度，0-2' },
      max_tokens: { type: 'number', required: false, description: '最大生成 token 数' },
      stream: { type: 'boolean', required: false, description: '是否流式输出' }
    }
  },
  {
    method: 'GET' as const,
    path: '/v1/models',
    description: '获取可用模型列表',
    params: {}
  }
]

const codeExample = `import openai

client = openai.OpenAI(
    api_key="YOUR_API_KEY",
    base_url="http://localhost:8080/v1"
)

response = client.chat.completions.create(
    model="deepseek-chat",
    messages=[
        {"role": "system", "content": "You are a helpful assistant."},
        {"role": "user", "content": "Hello"}
    ],
    stream=False
)

print(response.choices[0].message.content)`

const nodeExample = `const response = await fetch('http://localhost:8080/v1/chat/completions', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_API_KEY'
  },
  body: JSON.stringify({
    model: 'deepseek-chat',
    messages: [
      { role: 'user', content: 'Hello' }
    ]
  })
});

const data = await response.json();
console.log(data.choices[0].message.content);`

const curlExample = `curl -X POST http://localhost:8080/v1/chat/completions \\
  -H "Content-Type: application/json" \\
  -H "Authorization: Bearer YOUR_API_KEY" \\
  -d '{
    "model": "deepseek-chat",
    "messages": [{"role": "user", "content": "Hello"}]
  }'`
</script>

<template>
  <div class="docs-page">
    <n-card title="API 接口文档">
      <p class="intro">DeepSeek API 兼容 OpenAI API 格式，您可以轻松迁移现有应用</p>
      
      <n-tabs type="line">
        <n-tab-pane name="overview" tab="概述">
          <div class="section">
            <h3>基础 URL</h3>
            <n-code code="http://localhost:8080/v1" language="text" />
            
            <h3>认证方式</h3>
            <p>所有 API 请求都需要在 Header 中携带 API Key：</p>
            <n-code code="Authorization: Bearer YOUR_API_KEY" language="text" />
          </div>
        </n-tab-pane>

        <n-tab-pane name="endpoints" tab="接口列表">
          <n-collapse>
            <n-collapse-item
              v-for="endpoint in endpoints"
              :key="endpoint.path"
              :name="endpoint.path"
            >
              <template #header>
                <n-space align="center">
                  <n-tag :type="endpoint.method === 'GET' ? 'info' : 'success'" size="small">
                    {{ endpoint.method }}
                  </n-tag>
                  <code>{{ endpoint.path }}</code>
                  <span style="color: rgba(255,255,255,0.6);">{{ endpoint.description }}</span>
                </n-space>
              </template>
              <div v-if="Object.keys(endpoint.params).length > 0">
                <h4>参数</h4>
                <table class="params-table">
                  <thead>
                    <tr>
                      <th>参数名</th>
                      <th>类型</th>
                      <th>必填</th>
                      <th>说明</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(param, key) in endpoint.params" :key="key">
                      <td><code>{{ key }}</code></td>
                      <td>{{ param!.type }}</td>
                      <td>
                        <n-tag :type="param!.required ? 'error' : 'default'" size="small">
                          {{ param!.required ? '是' : '否' }}
                        </n-tag>
                      </td>
                      <td>{{ param!.description }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </n-collapse-item>
          </n-collapse>
        </n-tab-pane>

        <n-tab-pane name="curl" tab="cURL">
          <div class="code-section">
            <h3>示例请求</h3>
            <n-code :code="curlExample" language="bash" />
          </div>
        </n-tab-pane>

        <n-tab-pane name="python" tab="Python SDK">
          <div class="code-section">
            <h3>安装 SDK</h3>
            <n-code code="pip install openai" language="bash" />
            
            <h3>示例代码</h3>
            <n-code :code="codeExample" language="python" />
          </div>
        </n-tab-pane>

        <n-tab-pane name="nodejs" tab="Node.js">
          <div class="code-section">
            <h3>示例代码</h3>
            <n-code :code="nodeExample" language="javascript" />
          </div>
        </n-tab-pane>
      </n-tabs>
    </n-card>
  </div>
</template>

<style scoped>
.docs-page {
  max-width: 1200px;
}

.intro {
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 24px;
}

.section {
  padding: 16px 0;
}

.section h3 {
  margin: 24px 0 12px;
}

.code-section {
  padding: 16px 0;
}

.code-section h3 {
  margin: 24px 0 12px;
}

.params-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 12px;
}

.params-table th,
.params-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.params-table th {
  background: rgba(255, 255, 255, 0.05);
}
</style>
