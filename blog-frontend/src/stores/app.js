import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const loading = ref(false)
  const darkMode = ref(localStorage.getItem('theme') !== 'light')

  function toggleSidebar() { sidebarCollapsed.value = !sidebarCollapsed.value }
  function setLoading(val) { loading.value = val }
  function toggleDarkMode() {
    const el = document.documentElement
    el.classList.add('theme-switching')
    darkMode.value = !darkMode.value
    setTimeout(() => el.classList.remove('theme-switching'), 400)
  }

  watch(darkMode, (val) => {
    localStorage.setItem('theme', val ? 'dark' : 'light')
    document.documentElement.setAttribute('data-theme', val ? 'dark' : 'light')
  }, { immediate: true })

  return { sidebarCollapsed, loading, darkMode, toggleSidebar, setLoading, toggleDarkMode }
})
