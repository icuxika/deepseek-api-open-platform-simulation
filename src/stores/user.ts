import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, ApiKey, UsageStats, BillingRecord } from '@/types'
import { authApi, apiKeyApi, billingApi } from '@/utils/api'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))
  const apiKeys = ref<ApiKey[]>([])
  const usageStats = ref<UsageStats>({
    totalTokens: 0,
    promptTokens: 0,
    completionTokens: 0,
    requestCount: 0
  })
  const billingRecords = ref<BillingRecord[]>([])

  const isLoggedIn = computed(() => !!token.value && !!user.value)

  function setToken(newToken: string | null) {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  function setUser(newUser: User | null) {
    user.value = newUser
  }

  async function login(email: string, password: string): Promise<boolean> {
    try {
      const response = await authApi.login({ email, password })
      setToken(response.token)
      const userData = response.user as User
      setUser(userData)
      await loadUserData()
      return true
    } catch (error) {
      console.error('Login failed:', error)
      return false
    }
  }

  async function register(email: string, username: string, password: string): Promise<boolean> {
    try {
      const response = await authApi.register({ email, username, password })
      setToken(response.token)
      const userData = response.user as User
      setUser(userData)
      return true
    } catch (error) {
      console.error('Register failed:', error)
      return false
    }
  }

  async function fetchCurrentUser(): Promise<boolean> {
    if (!token.value) return false
    try {
      const userData = await authApi.getMe()
      setUser(userData as User)
      await loadUserData()
      return true
    } catch (error) {
      console.error('Fetch user failed:', error)
      logout()
      return false
    }
  }

  async function loadUserData() {
    try {
      const [keys, stats, records] = await Promise.all([
        apiKeyApi.getAll(),
        billingApi.getUsageStats(),
        billingApi.getRecords()
      ])
      apiKeys.value = keys as ApiKey[]
      usageStats.value = stats as UsageStats
      billingRecords.value = records as BillingRecord[]
    } catch (error) {
      console.error('Load user data failed:', error)
    }
  }

  function logout() {
    setUser(null)
    setToken(null)
    apiKeys.value = []
    usageStats.value = {
      totalTokens: 0,
      promptTokens: 0,
      completionTokens: 0,
      requestCount: 0
    }
    billingRecords.value = []
  }

  async function updateBalance(amount: number) {
    if (user.value) {
      user.value.balance += amount
    }
  }

  async function createApiKey(name: string): Promise<ApiKey | null> {
    try {
      const newKey = await apiKeyApi.create({ name })
      const apiKey = newKey as ApiKey
      apiKeys.value.push(apiKey)
      return apiKey
    } catch (error) {
      console.error('Create API key failed:', error)
      return null
    }
  }

  async function deleteApiKey(id: number) {
    try {
      await apiKeyApi.delete(id)
      const index = apiKeys.value.findIndex(key => key.id === id)
      if (index > -1) {
        apiKeys.value.splice(index, 1)
      }
    } catch (error) {
      console.error('Delete API key failed:', error)
    }
  }

  async function recharge(amount: number, paymentMethod: string) {
    try {
      await billingApi.recharge({ amount, paymentMethod })
      const userData = await authApi.getMe()
      setUser(userData as User)
      await loadUserData()
    } catch (error) {
      console.error('Recharge failed:', error)
      throw error
    }
  }

  return {
    user,
    token,
    apiKeys,
    usageStats,
    billingRecords,
    isLoggedIn,
    setToken,
    setUser,
    login,
    register,
    logout,
    fetchCurrentUser,
    updateBalance,
    loadUserData,
    createApiKey,
    deleteApiKey,
    recharge
  }
})
