<script setup lang="ts">
import { h } from 'vue'
import { NCard, NDataTable, NTag, NSpace, NDatePicker, NButton } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { useUserStore } from '@/stores'
import type { BillingRecord } from '@/types'

const userStore = useUserStore()

const columns: DataTableColumns<BillingRecord> = [
  {
    title: '类型',
    key: 'type',
    render(row) {
      return h(NTag, {
        type: row.type === 'recharge' ? 'success' : 'warning'
      }, { default: () => row.type === 'recharge' ? '充值' : '消费' })
    }
  },
  {
    title: '金额',
    key: 'amount',
    render(row) {
      const color = row.amount > 0 ? '#63e2b7' : '#e88080'
      return h('span', { style: { color } }, (row.amount > 0 ? '+' : '') + row.amount.toFixed(2))
    }
  },
  {
    title: '余额',
    key: 'balance',
    render(row) {
      return '¥' + row.balance.toFixed(2)
    }
  },
  {
    title: '描述',
    key: 'description'
  },
  {
    title: '时间',
    key: 'createdAt'
  }
]
</script>

<template>
  <div class="billing">
    <n-card title="账单记录">
      <template #header-extra>
        <n-space>
          <n-date-picker type="daterange" clearable />
          <n-button>导出</n-button>
        </n-space>
      </template>
      <n-data-table
        :columns="columns"
        :data="userStore.billingRecords"
        :bordered="false"
      />
    </n-card>
  </div>
</template>

<style scoped>
.billing {
  max-width: 1200px;
}
</style>
