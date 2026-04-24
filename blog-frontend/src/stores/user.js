import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getMe, login as loginApi } from '@/api/auth'
import { getToken, setToken, setRefreshToken, clearAuth } from '@/utils/auth'
import { resetAdminRoutes } from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)
  const permissions = computed(() => userInfo.value?.permissions || [])
  const menus = computed(() => userInfo.value?.menus || [])

  async function login(credentials) {
    const res = await loginApi(credentials)
    token.value = res.data.accessToken
    setToken(res.data.accessToken)
    setRefreshToken(res.data.refreshToken)
  }

  async function fetchUserInfo() {
    const res = await getMe()
    userInfo.value = res.data
    return res.data
  }

  function hasPermission(code) {
    return permissions.value.includes(code)
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    clearAuth()
    resetAdminRoutes()
  }

  return { token, userInfo, isLoggedIn, permissions, menus, login, fetchUserInfo, hasPermission, logout }
})
