const API_BASE_URL = 'http://localhost:8080/api'

export async function apiRequest<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const token = localStorage.getItem('token')
  
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...options.headers,
  }
  
  if (token) {
    (headers as Record<string, string>)['Authorization'] = `Bearer ${token}`
  }
  
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  })
  
  if (!response.ok) {
    const error = await response.json().catch(() => ({ error: '请求失败' }))
    throw new Error(error.error || '请求失败')
  }
  
  const contentLength = response.headers.get('content-length')
  if (response.status === 204 || contentLength === '0') {
    return undefined as T
  }
  
  const text = await response.text()
  if (!text) {
    return undefined as T
  }
  
  return JSON.parse(text)
}

export const authApi = {
  register: (data: { email: string; username: string; password: string }) =>
    apiRequest<{ token: string; user: unknown }>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  
  login: (data: { email: string; password: string }) =>
    apiRequest<{ token: string; user: unknown }>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  
  logout: () =>
    apiRequest<void>('/auth/logout', {
      method: 'POST',
    }),
  
  getMe: () => apiRequest<unknown>('/auth/me'),
  
  updateProfile: (data: { username: string; email: string }) =>
    apiRequest<unknown>('/auth/profile', {
      method: 'PUT',
      body: JSON.stringify(data),
    }),
  
  changePassword: (data: { currentPassword: string; newPassword: string }) =>
    apiRequest<void>('/auth/password', {
      method: 'PUT',
      body: JSON.stringify(data),
    }),
}

export const apiKeyApi = {
  getAll: () => apiRequest<unknown[]>('/api-keys'),
  
  create: (data: { name: string }) =>
    apiRequest<unknown>('/api-keys', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  
  delete: (id: number) =>
    apiRequest<void>(`/api-keys/${id}`, {
      method: 'DELETE',
    }),
}

export const billingApi = {
  getUsageStats: () => apiRequest<unknown>('/billing/usage'),
  
  getRecords: () => apiRequest<unknown[]>('/billing/records'),
  
  recharge: (data: { amount: number; paymentMethod: string }) =>
    apiRequest<unknown>('/billing/recharge', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
}

export const userApi = {
  getProfile: () => apiRequest<unknown>('/user/profile'),
}

export interface OAuthBinding {
  provider: string
  providerUsername?: string
  avatarUrl?: string
  createdAt: string
}

export const oauthApi = {
  getGiteeAuthUrl: () => 
    apiRequest<{ url: string }>('/auth/oauth/gitee'),
  
  giteeCallback: (code: string, state?: string) => {
    let url = `/auth/oauth/gitee/callback?code=${code}`
    if (state) url += `&state=${state}`
    return apiRequest<{ token: string; user: unknown }>(url)
  },
  
  getGithubAuthUrl: () => 
    apiRequest<{ url: string }>('/auth/oauth/github'),
  
  githubCallback: (code: string, state?: string) => {
    let url = `/auth/oauth/github/callback?code=${code}`
    if (state) url += `&state=${state}`
    return apiRequest<{ token: string; user: unknown }>(url)
  },
  
  getBindings: () => 
    apiRequest<OAuthBinding[]>('/auth/oauth/bindings'),
  
  unbind: (provider: string) => 
    apiRequest<void>(`/auth/oauth/bindings/${provider}`, {
      method: 'DELETE',
    }),
}
