const TOKEN_KEY = 'blog_access_token'
const REFRESH_KEY = 'blog_refresh_token'

export const getToken = () => localStorage.getItem(TOKEN_KEY)
export const setToken = (token) => localStorage.setItem(TOKEN_KEY, token)
export const removeToken = () => localStorage.removeItem(TOKEN_KEY)

export const getRefreshToken = () => localStorage.getItem(REFRESH_KEY)
export const setRefreshToken = (token) => localStorage.setItem(REFRESH_KEY, token)
export const removeRefreshToken = () => localStorage.removeItem(REFRESH_KEY)

export const clearAuth = () => {
  removeToken()
  removeRefreshToken()
}
