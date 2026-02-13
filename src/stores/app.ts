import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const collapsed = ref(false)
  const theme = ref<'dark' | 'light'>('dark')

  function toggleCollapsed() {
    collapsed.value = !collapsed.value
  }

  function setCollapsed(value: boolean) {
    collapsed.value = value
  }

  function toggleTheme() {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
  }

  return {
    collapsed,
    theme,
    toggleCollapsed,
    setCollapsed,
    toggleTheme
  }
})
