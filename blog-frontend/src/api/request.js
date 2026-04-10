import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, setToken, getRefreshToken, clearAuth } from '@/utils/auth'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器：自动附加 Token
request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 是否正在刷新中（防重复刷新）
let isRefreshing = false
let refreshQueue = []

// 响应拦截器：统一错误处理 + 自动刷新 Token
request.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const { response, config } = error
    if (!response) {
      ElMessage.error('网络连接失败')
      return Promise.reject(error)
    }

    if (response.status === 401 && !config._retry) {
      if (isRefreshing) {
        return new Promise((resolve) => {
          refreshQueue.push(() => resolve(request(config)))
        })
      }
      config._retry = true
      isRefreshing = true
      const refreshToken = getRefreshToken()
      if (refreshToken) {
        try {
          const res = await axios.post('/api/auth/refresh', null, {
            params: { refreshToken },
          })
          const newToken = res.data.data.accessToken
          setToken(newToken)
          refreshQueue.forEach((cb) => cb())
          refreshQueue = []
          return request(config)
        } catch {
          clearAuth()
          router.push('/login')
        } finally {
          isRefreshing = false
        }
      } else {
        clearAuth()
        router.push('/login')
      }
    }

    const msg = response.data?.message || '请求失败'
    if (response.status !== 401) ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
