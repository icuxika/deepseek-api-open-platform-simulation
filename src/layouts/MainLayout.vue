<script setup lang="ts">
import { computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NLayout, NLayoutSider, NLayoutHeader, NLayoutContent, NMenu, NIcon, NDropdown, NAvatar, NSpace, NButton } from 'naive-ui'
import {
  HomeOutline,
  KeyOutline,
  WalletOutline,
  ReceiptOutline,
  PersonOutline,
  MenuOutline,
  DocumentTextOutline,
  PricetagOutline
} from '@vicons/ionicons5'
import { useUserStore, useAppStore } from '@/stores'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const activeKey = computed(() => {
  const path = route.path
  if (path.startsWith('/dashboard')) return 'dashboard'
  if (path.startsWith('/api-keys')) return 'api-keys'
  if (path.startsWith('/docs')) return 'docs'
  if (path.startsWith('/pricing')) return 'pricing'
  if (path.startsWith('/recharge')) return 'recharge'
  if (path.startsWith('/billing')) return 'billing'
  if (path.startsWith('/profile')) return 'profile'
  return 'dashboard'
})

const menuOptions = [
  {
    label: '仪表板',
    key: 'dashboard',
    icon: () => h(NIcon, null, { default: () => h(HomeOutline) })
  },
  {
    label: 'API Keys',
    key: 'api-keys',
    icon: () => h(NIcon, null, { default: () => h(KeyOutline) })
  },
  {
    label: '接口文档',
    key: 'docs',
    icon: () => h(NIcon, null, { default: () => h(DocumentTextOutline) })
  },
  {
    label: '产品定价',
    key: 'pricing',
    icon: () => h(NIcon, null, { default: () => h(PricetagOutline) })
  },
  {
    label: '充值',
    key: 'recharge',
    icon: () => h(NIcon, null, { default: () => h(WalletOutline) })
  },
  {
    label: '账单',
    key: 'billing',
    icon: () => h(NIcon, null, { default: () => h(ReceiptOutline) })
  },
  {
    label: '个人设置',
    key: 'profile',
    icon: () => h(NIcon, null, { default: () => h(PersonOutline) })
  }
]

const userOptions = [
  {
    label: '个人设置',
    key: 'profile'
  },
  {
    type: 'divider',
    key: 'd1'
  },
  {
    label: '退出登录',
    key: 'logout'
  }
]

function handleMenuSelect(key: string) {
  router.push('/' + key)
}

async function handleUserSelect(key: string) {
  if (key === 'logout') {
    await userStore.logout()
    router.push('/login')
  } else if (key === 'profile') {
    router.push('/profile')
  }
}

function handleLogoClick() {
  router.push('/dashboard')
}
</script>

<template>
  <n-layout has-sider style="height: 100vh">
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :collapsed="appStore.collapsed"
      show-trigger
      @collapse="appStore.setCollapsed(true)"
      @expand="appStore.setCollapsed(false)"
      :native-scrollbar="false"
      inverted
    >
      <div class="logo" @click="handleLogoClick">
        <span v-if="!appStore.collapsed">DeepSeek API</span>
        <span v-else>DS</span>
      </div>
      <n-menu
        inverted
        :collapsed="appStore.collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        :value="activeKey"
        @update:value="handleMenuSelect"
      />
    </n-layout-sider>
    <n-layout style="display: flex; flex-direction: column;">
      <n-layout-header 
        bordered 
        position="absolute"
        style="height: 60px; padding: 0 24px; display: flex; align-items: center; justify-content: space-between; position: sticky; top: 0; z-index: 100;">
        <div style="display: flex; align-items: center; gap: 16px;">
          <n-button text @click="appStore.toggleCollapsed">
            <template #icon>
              <n-icon :component="MenuOutline" />
            </template>
          </n-button>
          <span style="font-size: 16px; font-weight: 500;">{{ route.meta.title || '仪表板' }}</span>
        </div>
        <div style="display: flex; align-items: center; gap: 16px;">
          <div v-if="userStore.user" style="display: flex; align-items: center; gap: 8px;">
            <span style="color: #63e2b7;">余额: ¥{{ userStore.user.balance.toFixed(2) }}</span>
          </div>
          <n-dropdown :options="userOptions" @select="handleUserSelect">
            <n-space align="center" style="cursor: pointer;">
              <n-avatar round size="small">
                {{ userStore.user?.username?.charAt(0).toUpperCase() || 'U' }}
              </n-avatar>
              <span>{{ userStore.user?.username || '用户' }}</span>
            </n-space>
          </n-dropdown>
        </div>
      </n-layout-header>
      <n-layout-content :native-scrollbar="false" style="padding: 24px; flex: 1; overflow: auto;">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<style scoped>
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
</style>
