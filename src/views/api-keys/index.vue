<script setup lang="ts">
import { ref, h } from 'vue'
import { NCard, NButton, NDataTable, NSpace, NTag, NModal, NInput, NFormItem, NForm, useMessage, NIcon } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { CopyOutline, TrashOutline, AddOutline, EyeOutline, EyeOffOutline } from '@vicons/ionicons5'
import { useUserStore } from '@/stores'
import type { ApiKey } from '@/types'

const userStore = useUserStore()
const message = useMessage()

const showModal = ref(false)
const newKeyName = ref('')
const visibleKeys = ref<Set<number>>(new Set())
const loading = ref(false)

const columns: DataTableColumns<ApiKey> = [
  {
    title: '名称',
    key: 'name'
  },
  {
    title: 'API Key',
    key: 'key',
    render(row) {
      const isVisible = visibleKeys.value.has(row.id)
      const displayKey = isVisible ? row.key : row.key.substring(0, 10) + '...' + row.key.substring(row.key.length - 4)
      return h('div', { style: { display: 'flex', alignItems: 'center', gap: '8px' } }, [
        h('span', { style: { fontFamily: 'monospace' } }, displayKey),
        h(NButton, {
          text: true,
          size: 'small',
          onClick: () => toggleKeyVisibility(row.id)
        }, {
          icon: () => h(NIcon, null, { default: () => h(isVisible ? EyeOffOutline : EyeOutline) })
        })
      ])
    }
  },
  {
    title: '状态',
    key: 'status',
    render(row) {
      return h(NTag, {
        type: row.status === 'active' ? 'success' : 'default'
      }, { default: () => row.status === 'active' ? '活跃' : '已禁用' })
    }
  },
  {
    title: '创建时间',
    key: 'createdAt'
  },
  {
    title: '最后使用',
    key: 'lastUsedAt',
    render(row) {
      return row.lastUsedAt || '-'
    }
  },
  {
    title: '操作',
    key: 'actions',
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, {
            text: true,
            type: 'primary',
            onClick: () => copyKey(row.key)
          }, {
            icon: () => h(NIcon, null, { default: () => h(CopyOutline) }),
            default: () => '复制'
          }),
          h(NButton, {
            text: true,
            type: 'error',
            onClick: () => deleteKey(row.id)
          }, {
            icon: () => h(NIcon, null, { default: () => h(TrashOutline) }),
            default: () => '删除'
          })
        ]
      })
    }
  }
]

function toggleKeyVisibility(id: number) {
  if (visibleKeys.value.has(id)) {
    visibleKeys.value.delete(id)
  } else {
    visibleKeys.value.add(id)
  }
}

function copyKey(key: string) {
  navigator.clipboard.writeText(key)
  message.success('API Key 已复制到剪贴板')
}

async function deleteKey(id: number) {
  await userStore.deleteApiKey(id)
  message.success('API Key 已删除')
}

async function handleCreateKey() {
  if (!newKeyName.value.trim()) {
    message.error('请输入 Key 名称')
    return
  }
  
  loading.value = true
  try {
    const result = await userStore.createApiKey(newKeyName.value.trim())
    if (result) {
      message.success('API Key 创建成功')
      showModal.value = false
      newKeyName.value = ''
    } else {
      message.error('创建失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="api-keys">
    <n-card title="API Keys 管理">
      <template #header-extra>
        <n-button type="primary" @click="showModal = true">
          <template #icon>
            <n-icon :component="AddOutline" />
          </template>
          创建新 Key
        </n-button>
      </template>
      <n-data-table
        :columns="columns"
        :data="userStore.apiKeys"
        :bordered="false"
      />
    </n-card>

    <n-modal v-model:show="showModal" preset="dialog" title="创建新的 API Key">
      <n-form @submit.prevent="handleCreateKey">
        <n-form-item label="Key 名称">
          <n-input
            v-model:value="newKeyName"
            placeholder="请输入 Key 名称，如：Production Key"
            @keyup.enter="handleCreateKey"
          />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space>
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="loading" @click="handleCreateKey">创建</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
.api-keys {
  max-width: 1200px;
}
</style>
