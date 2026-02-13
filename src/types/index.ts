export interface User {
  id: number
  email: string
  username: string
  avatarUrl?: string
  balance: number
  createdAt: string
}

export interface ApiKey {
  id: number
  name: string
  key: string
  createdAt: string
  lastUsedAt?: string
  status: 'active' | 'disabled'
}

export interface UsageStats {
  totalTokens: number
  promptTokens: number
  completionTokens: number
  requestCount: number
}

export interface BillingRecord {
  id: number
  type: 'recharge' | 'usage'
  amount: number
  balance: number
  description: string
  createdAt: string
}

export interface PricingPlan {
  id: string
  name: string
  model: string
  inputPrice: number
  outputPrice: number
  unit: string
}

export interface ApiEndpoint {
  method: 'GET' | 'POST' | 'PUT' | 'DELETE'
  path: string
  description: string
  params?: Record<string, unknown>
  response?: Record<string, unknown>
}
