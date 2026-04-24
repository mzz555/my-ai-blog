import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const loading = ref(false)
  const darkMode = ref(localStorage.getItem('theme') === 'dark')

  function toggleSidebar() { sidebarCollapsed.value = !sidebarCollapsed.value }
  function setLoading(val) { loading.value = val }
  function toggleDarkMode() { darkMode.value = !darkMode.value }

  watch(darkMode, (val) => {
    localStorage.setItem('theme', val ? 'dark' : 'light')
    document.documentElement.setAttribute('data-theme', val ? 'dark' : 'light')
  }, { immediate: true })

  return { sidebarCollapsed, loading, darkMode, toggleSidebar, setLoading, toggleDarkMode }
})
