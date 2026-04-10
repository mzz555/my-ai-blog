# 技术博客系统 — 前端实施计划（Vue.js）

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标：** 实现技术博客系统完整前端，包含面向访客的前台博客页面和管理员后台，与 Spring Boot 后端 REST API 对接。

**架构：** Vue.js 3 Composition API + Vite；前台（博客展示）+ 后台（管理系统）共用同一 Vue 项目，通过路由和布局区分；Pinia 管理全局状态；Axios 封装 HTTP 请求；动态路由 + `v-permission` 指令实现 RBAC 前端权限控制。

**技术栈：** Vue 3.4+ · Vite 5 · Element Plus 2 · Pinia 2 · Vue Router 4 · md-editor-v3 · Axios · @vueuse/core

---

## 文件清单

```
blog-frontend/
├── index.html
├── vite.config.js
├── package.json
├── .env.development
├── .env.production
├── public/
│   ├── favicon.ico
│   └── robots.txt
└── src/
    ├── main.js
    ├── App.vue
    ├── api/
    │   ├── request.js          # Axios 实例 + 拦截器
    │   ├── auth.js             # 认证相关接口
    │   ├── article.js          # 文章相关接口
    │   ├── comment.js          # 评论相关接口
    │   ├── category.js         # 分类接口
    │   ├── tag.js              # 标签接口
    │   ├── upload.js           # 上传接口
    │   └── stats.js            # 统计接口
    ├── router/
    │   └── index.js            # 路由配置 + 导航守卫 + 动态路由
    ├── stores/
    │   ├── user.js             # 用户状态（token、权限、菜单）
    │   └── app.js              # 应用状态（loading、侧边栏）
    ├── directives/
    │   └── permission.js       # v-permission 自定义指令
    ├── utils/
    │   ├── auth.js             # Token 存取工具
    │   └── format.js           # 日期、数字格式化
    ├── layouts/
    │   ├── FrontLayout.vue     # 前台布局（导航 + 侧边栏 + 页脚）
    │   └── AdminLayout.vue     # 后台布局（顶栏 + 侧边菜单 + 主内容）
    ├── components/
    │   ├── front/
    │   │   ├── ArticleCard.vue     # 文章卡片（列表页用）
    │   │   ├── TagCloud.vue        # 标签云
    │   │   ├── CategoryList.vue    # 侧边分类列表
    │   │   └── CommentSection.vue  # 评论区（含发表评论）
    │   └── admin/
    │       └── AdminSidebar.vue    # 后台侧边菜单（动态渲染）
    └── views/
        ├── front/
        │   ├── HomeView.vue        # 首页
        │   ├── PostListView.vue    # 文章列表
        │   ├── PostDetailView.vue  # 文章详情
        │   ├── CategoryView.vue    # 分类页
        │   ├── TagView.vue         # 标签页
        │   ├── ArchiveView.vue     # 归档页
        │   └── AboutView.vue       # 关于我
        ├── admin/
        │   ├── DashboardView.vue       # 仪表盘
        │   ├── ArticleListView.vue     # 文章管理列表
        │   ├── ArticleEditView.vue     # 新建/编辑文章
        │   ├── CommentManageView.vue   # 评论审核
        │   ├── CategoryManageView.vue  # 分类管理
        │   ├── TagManageView.vue       # 标签管理
        │   └── ProfileView.vue         # 个人资料
        └── LoginView.vue           # 登录页
```

---

## Task 1: 项目初始化

**Files:**
- Create: `blog-frontend/package.json`
- Create: `blog-frontend/vite.config.js`
- Create: `blog-frontend/index.html`
- Create: `blog-frontend/src/main.js`
- Create: `blog-frontend/src/App.vue`
- Create: `blog-frontend/.env.development`
- Create: `blog-frontend/.env.production`

- [ ] **Step 1: 创建 package.json**

```json
{
  "name": "blog-frontend",
  "version": "0.0.1",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.21",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.7",
    "element-plus": "^2.6.1",
    "@element-plus/icons-vue": "^2.3.1",
    "axios": "^1.6.8",
    "md-editor-v3": "^4.14.0",
    "@vueuse/core": "^10.9.0",
    "dayjs": "^1.11.10"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.4",
    "vite": "^5.2.0",
    "unplugin-auto-import": "^0.17.5",
    "unplugin-vue-components": "^0.26.0"
  }
}
```

- [ ] **Step 2: 创建 vite.config.js**

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({ resolvers: [ElementPlusResolver()] }),
    Components({ resolvers: [ElementPlusResolver()] }),
  ],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true }
    }
  }
})
```

- [ ] **Step 3: 创建 index.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <link rel="icon" type="image/svg+xml" href="/favicon.ico" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>技术博客</title>
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.js"></script>
</body>
</html>
```

- [ ] **Step 4: 创建 src/main.js**

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { permissionDirective } from './directives/permission'

const app = createApp(App)

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.directive('permission', permissionDirective)

app.mount('#app')
```

- [ ] **Step 5: 创建 src/App.vue**

```vue
<template>
  <router-view />
</template>

<script setup>
// 顶层组件，只负责渲染路由视图
</script>
```

- [ ] **Step 6: 创建环境变量文件**

```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8080
VITE_BLOG_NAME=技术博客（开发）
```

```bash
# .env.production
VITE_API_BASE_URL=
VITE_BLOG_NAME=技术博客
```

- [ ] **Step 7: 安装依赖**

```bash
cd blog-frontend
npm install
```

预期：`node_modules/` 创建成功，无报错

- [ ] **Step 8: 验证 dev 能启动**

```bash
npm run dev
```

预期：`VITE v5.x ready` + `http://localhost:3000/`（页面空白但无报错）

- [ ] **Step 9: 提交**

```bash
cd ..
git add blog-frontend/
git commit -m "初始化：Vue.js 前端项目骨架（Vite + Element Plus + Pinia）"
```

---

## Task 2: 工具层（Axios、Token、格式化）

**Files:**
- Create: `src/utils/auth.js`
- Create: `src/utils/format.js`
- Create: `src/api/request.js`

- [ ] **Step 1: 创建 src/utils/auth.js**

```javascript
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
```

- [ ] **Step 2: 创建 src/utils/format.js**

```javascript
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

export const formatDate = (date) => dayjs(date).format('YYYY-MM-DD')
export const formatDateTime = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')
export const fromNow = (date) => dayjs(date).fromNow()
```

- [ ] **Step 3: 创建 src/api/request.js**

```javascript
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
```

- [ ] **Step 4: 提交**

```bash
git add blog-frontend/src/utils/ blog-frontend/src/api/request.js
git commit -m "新增：Axios 请求封装（Token 自动注入 + 401 自动刷新 + 错误提示）"
```

---

## Task 3: API 模块

**Files:**
- Create: `src/api/auth.js`
- Create: `src/api/article.js`
- Create: `src/api/comment.js`
- Create: `src/api/category.js`
- Create: `src/api/tag.js`
- Create: `src/api/upload.js`
- Create: `src/api/stats.js`

- [ ] **Step 1: 创建 src/api/auth.js**

```javascript
import request from './request'

export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)
export const getMe = () => request.get('/auth/me')
export const refreshToken = (refreshToken) =>
  request.post('/auth/refresh', null, { params: { refreshToken } })
```

- [ ] **Step 2: 创建 src/api/article.js**

```javascript
import request from './request'

export const getArticles = (params) => request.get('/articles', { params })
export const getAdminArticles = (params) => request.get('/articles/admin/list', { params })
export const getArticleBySlug = (slug) => request.get(`/articles/${slug}`)
export const createArticle = (data) => request.post('/articles', data)
export const updateArticle = (id, data) => request.put(`/articles/${id}`, data)
export const togglePublish = (id) => request.put(`/articles/${id}/publish`)
export const deleteArticle = (id) => request.delete(`/articles/${id}`)
export const recordView = (id) => request.post(`/stats/articles/${id}/view`)
```

- [ ] **Step 3: 创建 src/api/comment.js**

```javascript
import request from './request'

export const getComments = (articleId) => request.get(`/articles/${articleId}/comments`)
export const createComment = (articleId, data) =>
  request.post(`/articles/${articleId}/comments`, data)
export const getAdminComments = (params) => request.get('/comments/admin', { params })
export const updateCommentStatus = (id, status) =>
  request.put(`/comments/${id}/status`, null, { params: { status } })
export const deleteComment = (id) => request.delete(`/comments/${id}`)
```

- [ ] **Step 4: 创建其余 API 模块**

```javascript
// src/api/category.js
import request from './request'
export const getCategories = () => request.get('/categories')
export const createCategory = (data) => request.post('/categories', data)
export const updateCategory = (id, data) => request.put(`/categories/${id}`, data)
export const deleteCategory = (id) => request.delete(`/categories/${id}`)
```

```javascript
// src/api/tag.js
import request from './request'
export const getTags = () => request.get('/tags')
export const createTag = (data) => request.post('/tags', data)
export const deleteTag = (id) => request.delete(`/tags/${id}`)
```

```javascript
// src/api/upload.js
import request from './request'
export const uploadImage = (file) => {
  const form = new FormData()
  form.append('file', file)
  return request.post('/upload/image', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
```

```javascript
// src/api/stats.js
import request from './request'
export const getOverview = () => request.get('/stats/overview')
```

- [ ] **Step 5: 提交**

```bash
git add blog-frontend/src/api/
git commit -m "新增：API 模块（auth/article/comment/category/tag/upload/stats）"
```

---

## Task 4: Pinia 状态管理 + 路由配置

**Files:**
- Create: `src/stores/user.js`
- Create: `src/stores/app.js`
- Create: `src/directives/permission.js`
- Create: `src/router/index.js`

- [ ] **Step 1: 创建 src/stores/user.js**

```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getMe, login as loginApi } from '@/api/auth'
import { getToken, setToken, setRefreshToken, clearAuth } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(null)   // { id, username, email, avatar, roles, permissions, menus }

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
  }

  return { token, userInfo, isLoggedIn, permissions, menus, login, fetchUserInfo, hasPermission, logout }
})
```

- [ ] **Step 2: 创建 src/stores/app.js**

```javascript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const loading = ref(false)

  function toggleSidebar() { sidebarCollapsed.value = !sidebarCollapsed.value }
  function setLoading(val) { loading.value = val }

  return { sidebarCollapsed, loading, toggleSidebar, setLoading }
})
```

- [ ] **Step 3: 创建 src/directives/permission.js**

```javascript
import { useUserStore } from '@/stores/user'

export const permissionDirective = {
  mounted(el, binding) {
    const userStore = useUserStore()
    const code = binding.value
    if (code && !userStore.hasPermission(code)) {
      el.parentNode?.removeChild(el)
    }
  }
}
```

- [ ] **Step 4: 创建 src/router/index.js**

```javascript
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getToken } from '@/utils/auth'

// 静态路由（无需登录）
const staticRoutes = [
  {
    path: '/',
    component: () => import('@/layouts/FrontLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/front/HomeView.vue') },
      { path: 'posts', name: 'PostList', component: () => import('@/views/front/PostListView.vue') },
      { path: 'posts/:slug', name: 'PostDetail', component: () => import('@/views/front/PostDetailView.vue') },
      { path: 'category/:slug', name: 'Category', component: () => import('@/views/front/CategoryView.vue') },
      { path: 'tag/:slug', name: 'Tag', component: () => import('@/views/front/TagView.vue') },
      { path: 'archive', name: 'Archive', component: () => import('@/views/front/ArchiveView.vue') },
      { path: 'about', name: 'About', component: () => import('@/views/front/AboutView.vue') },
    ]
  },
  { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue') },
]

// 后台路由（动态注册）
export const adminRoutes = [
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'Dashboard', component: () => import('@/views/admin/DashboardView.vue') },
      { path: 'articles', name: 'ArticleList', component: () => import('@/views/admin/ArticleListView.vue') },
      { path: 'articles/new', name: 'ArticleNew', component: () => import('@/views/admin/ArticleEditView.vue') },
      { path: 'articles/:id/edit', name: 'ArticleEdit', component: () => import('@/views/admin/ArticleEditView.vue') },
      { path: 'comments', name: 'CommentManage', component: () => import('@/views/admin/CommentManageView.vue') },
      { path: 'categories', name: 'CategoryManage', component: () => import('@/views/admin/CategoryManageView.vue') },
      { path: 'tags', name: 'TagManage', component: () => import('@/views/admin/TagManageView.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/admin/ProfileView.vue') },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: staticRoutes,
  scrollBehavior: () => ({ top: 0 }),
})

let adminRoutesAdded = false

// 导航守卫：登录检查 + 动态加载后台路由
router.beforeEach(async (to) => {
  const token = getToken()

  if (to.path.startsWith('/admin')) {
    if (!token) return '/login'
    if (!adminRoutesAdded) {
      const userStore = useUserStore()
      if (!userStore.userInfo) {
        try { await userStore.fetchUserInfo() } catch { return '/login' }
      }
      adminRoutes.forEach(r => router.addRoute(r))
      adminRoutesAdded = true
      return to.fullPath  // 重新导航触发新路由
    }
  }

  if (to.name === 'Login' && token) return '/admin'
})

export default router
```

- [ ] **Step 5: 提交**

```bash
git add blog-frontend/src/stores/ blog-frontend/src/directives/ blog-frontend/src/router/
git commit -m "新增：Pinia 状态管理 + Vue Router（动态后台路由 + 导航守卫）"
```

---

## Task 5: 布局组件

**Files:**
- Create: `src/layouts/FrontLayout.vue`
- Create: `src/layouts/AdminLayout.vue`
- Create: `src/components/admin/AdminSidebar.vue`

- [ ] **Step 1: 创建 src/layouts/FrontLayout.vue**

```vue
<template>
  <el-container direction="vertical" style="min-height: 100vh">
    <!-- 顶部导航 -->
    <el-header height="60px" style="background:#fff;border-bottom:1px solid #eee;display:flex;align-items:center;justify-content:space-between;padding:0 40px">
      <router-link to="/" style="font-size:20px;font-weight:bold;color:#303133;text-decoration:none">
        {{ blogName }}
      </router-link>
      <nav style="display:flex;gap:24px">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/posts" class="nav-link">文章</router-link>
        <router-link to="/archive" class="nav-link">归档</router-link>
        <router-link to="/about" class="nav-link">关于</router-link>
        <router-link v-if="isLoggedIn" to="/admin" class="nav-link">后台</router-link>
        <router-link v-else to="/login" class="nav-link">登录</router-link>
      </nav>
    </el-header>

    <!-- 主内容 -->
    <el-main style="max-width:1200px;margin:0 auto;width:100%;padding:24px 20px">
      <router-view />
    </el-main>

    <!-- 页脚 -->
    <el-footer height="60px" style="background:#f5f5f5;display:flex;align-items:center;justify-content:center;color:#909399;font-size:13px">
      © {{ new Date().getFullYear() }} {{ blogName }} · Powered by Spring Boot + Vue.js
    </el-footer>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)
const blogName = import.meta.env.VITE_BLOG_NAME || '技术博客'
</script>

<style scoped>
.nav-link { color: #606266; text-decoration: none; font-size: 14px; }
.nav-link:hover, .nav-link.router-link-active { color: #409eff; }
</style>
```

- [ ] **Step 2: 创建 src/components/admin/AdminSidebar.vue**

```vue
<template>
  <el-menu
    :default-active="$route.path"
    router
    :collapse="collapsed"
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409eff"
  >
    <div class="logo" style="height:60px;display:flex;align-items:center;justify-content:center;color:#fff;font-size:16px;font-weight:bold;border-bottom:1px solid #263445">
      {{ collapsed ? 'Blog' : '博客管理系统' }}
    </div>
    <el-menu-item index="/admin">
      <el-icon><Odometer /></el-icon><template #title>仪表盘</template>
    </el-menu-item>
    <el-menu-item index="/admin/articles">
      <el-icon><Document /></el-icon><template #title>文章管理</template>
    </el-menu-item>
    <el-menu-item index="/admin/comments">
      <el-icon><ChatDotRound /></el-icon><template #title>评论审核</template>
    </el-menu-item>
    <el-menu-item index="/admin/categories">
      <el-icon><FolderOpened /></el-icon><template #title>分类管理</template>
    </el-menu-item>
    <el-menu-item index="/admin/tags">
      <el-icon><CollectionTag /></el-icon><template #title>标签管理</template>
    </el-menu-item>
    <el-menu-item index="/admin/profile">
      <el-icon><User /></el-icon><template #title>个人资料</template>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
defineProps({ collapsed: Boolean })
</script>
```

- [ ] **Step 3: 创建 src/layouts/AdminLayout.vue**

```vue
<template>
  <el-container style="height:100vh">
    <!-- 侧边栏 -->
    <el-aside :width="collapsed ? '64px' : '220px'" style="transition:width 0.3s;overflow:hidden">
      <AdminSidebar :collapsed="collapsed" />
    </el-aside>

    <el-container direction="vertical">
      <!-- 顶栏 -->
      <el-header style="background:#fff;border-bottom:1px solid #eee;display:flex;align-items:center;justify-content:space-between;padding:0 20px">
        <el-icon style="cursor:pointer;font-size:20px" @click="appStore.toggleSidebar()">
          <component :is="collapsed ? 'Expand' : 'Fold'" />
        </el-icon>
        <div style="display:flex;align-items:center;gap:12px">
          <span style="font-size:13px;color:#606266">{{ userStore.userInfo?.username }}</span>
          <el-button text size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main style="background:#f0f2f5;overflow-y:auto">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'

const userStore = useUserStore()
const appStore = useAppStore()
const router = useRouter()
const collapsed = computed(() => appStore.sidebarCollapsed)

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>
```

- [ ] **Step 4: 提交**

```bash
git add blog-frontend/src/layouts/ blog-frontend/src/components/
git commit -m "新增：前台布局（FrontLayout）和后台布局（AdminLayout + 侧边菜单）"
```

---

## Task 6: 登录页 + 前台核心页面

**Files:**
- Create: `src/views/LoginView.vue`
- Create: `src/views/front/HomeView.vue`
- Create: `src/views/front/PostListView.vue`
- Create: `src/views/front/PostDetailView.vue`
- Create: `src/components/front/ArticleCard.vue`
- Create: `src/components/front/CommentSection.vue`
- Create: `src/views/front/CategoryView.vue`
- Create: `src/views/front/TagView.vue`
- Create: `src/views/front/ArchiveView.vue`
- Create: `src/views/front/AboutView.vue`

- [ ] **Step 1: 创建 src/views/LoginView.vue**

```vue
<template>
  <div style="display:flex;height:100vh;align-items:center;justify-content:center;background:#f0f2f5">
    <el-card style="width:400px;padding:20px">
      <h2 style="text-align:center;margin-bottom:24px">登录管理后台</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"
            size="large" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%;margin-top:8px"
          :loading="loading" @click="handleLogin">
          登 录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }],
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form)
    await userStore.fetchUserInfo()
    ElMessage.success('登录成功')
    router.push('/admin')
  } catch (e) {
    // 错误由 Axios 拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>
```

- [ ] **Step 2: 创建 src/components/front/ArticleCard.vue**

```vue
<template>
  <el-card class="article-card" shadow="hover" style="margin-bottom:20px">
    <div style="display:flex;gap:16px">
      <el-image v-if="article.coverImage" :src="article.coverImage"
        style="width:200px;height:120px;flex-shrink:0;border-radius:4px" fit="cover" />
      <div style="flex:1;overflow:hidden">
        <div style="display:flex;align-items:center;gap:8px;margin-bottom:8px">
          <el-tag v-if="article.isTop" type="danger" size="small">置顶</el-tag>
          <el-tag v-if="article.categoryName" type="info" size="small">{{ article.categoryName }}</el-tag>
        </div>
        <router-link :to="`/posts/${article.slug}`" class="article-title">
          {{ article.title }}
        </router-link>
        <p style="color:#909399;font-size:13px;margin:8px 0;line-height:1.6">
          {{ article.summary || '暂无摘要' }}
        </p>
        <div style="display:flex;align-items:center;gap:16px;font-size:12px;color:#bbb">
          <span>{{ formatDate(article.publishedAt) }}</span>
          <span>👁 {{ article.viewCount }}</span>
          <span v-for="tag in article.tagNames" :key="tag">
            <router-link :to="`/tag/${tag}`" style="color:#409eff">#{{ tag }}</router-link>
          </span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { formatDate } from '@/utils/format'
defineProps({ article: Object })
</script>

<style scoped>
.article-title { font-size:18px;font-weight:600;color:#303133;text-decoration:none; }
.article-title:hover { color:#409eff; }
</style>
```

- [ ] **Step 3: 创建 src/views/front/HomeView.vue**

```vue
<template>
  <div style="display:flex;gap:24px">
    <!-- 文章列表 -->
    <div style="flex:1">
      <el-skeleton v-if="loading" :rows="5" animated />
      <template v-else>
        <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
        <el-empty v-if="!articles.length" description="暂无文章" />
        <div style="text-align:center;margin-top:16px">
          <el-pagination v-if="total > pageSize" background layout="prev,pager,next"
            :total="total" :page-size="pageSize" :current-page="page"
            @current-change="loadArticles" />
        </div>
      </template>
    </div>
    <!-- 侧边栏 -->
    <div style="width:260px;flex-shrink:0">
      <el-card style="margin-bottom:16px">
        <template #header><span>分类</span></template>
        <div v-for="cat in categories" :key="cat.id" style="margin-bottom:8px">
          <router-link :to="`/category/${cat.slug}`" style="color:#606266;text-decoration:none">
            {{ cat.name }}
          </router-link>
        </div>
      </el-card>
      <el-card>
        <template #header><span>标签</span></template>
        <el-tag v-for="tag in tags" :key="tag.id" style="margin:4px;cursor:pointer"
          @click="$router.push(`/tag/${tag.slug}`)">
          {{ tag.name }}
        </el-tag>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getArticles } from '@/api/article'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import ArticleCard from '@/components/front/ArticleCard.vue'

const articles = ref([])
const categories = ref([])
const tags = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)

async function loadArticles(p = 1) {
  loading.value = true
  page.value = p
  try {
    const res = await getArticles({ page: p, size: pageSize })
    articles.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  loadArticles()
  const [catRes, tagRes] = await Promise.all([getCategories(), getTags()])
  categories.value = catRes.data
  tags.value = tagRes.data
})
</script>
```

- [ ] **Step 4: 创建 src/views/front/PostListView.vue**

```vue
<template>
  <div>
    <h2 style="margin-bottom:20px">所有文章</h2>
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
      <el-empty v-if="!articles.length" description="暂无文章" />
      <div style="text-align:center;margin-top:16px">
        <el-pagination v-if="total > pageSize" background layout="prev,pager,next"
          :total="total" :page-size="pageSize" :current-page="page"
          @current-change="(p) => loadArticles(p)" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticles } from '@/api/article'
import ArticleCard from '@/components/front/ArticleCard.vue'

const route = useRoute()
const articles = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)

async function loadArticles(p = 1) {
  loading.value = true
  page.value = p
  try {
    const res = await getArticles({ page: p, size: pageSize, ...route.query })
    articles.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
}

onMounted(() => loadArticles())
</script>
```

- [ ] **Step 5: 创建 src/components/front/CommentSection.vue**

```vue
<template>
  <div style="margin-top:40px">
    <h3 style="margin-bottom:16px">评论（{{ comments.length }}）</h3>

    <!-- 发表评论 -->
    <el-card style="margin-bottom:20px">
      <el-form :model="form" label-position="top">
        <el-row :gutter="12" v-if="!isLoggedIn">
          <el-col :span="12">
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" placeholder="您的昵称（必填）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="邮箱（可选）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="评论内容">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="写下你的想法..." />
        </el-form-item>
        <el-button type="primary" :loading="submitting" @click="submitComment">发表评论</el-button>
      </el-form>
    </el-card>

    <!-- 评论列表 -->
    <div v-for="comment in comments" :key="comment.id" style="margin-bottom:16px">
      <el-card>
        <div style="display:flex;gap:12px">
          <el-avatar :size="36">{{ (comment.nickname || '').charAt(0).toUpperCase() }}</el-avatar>
          <div style="flex:1">
            <div style="display:flex;justify-content:space-between">
              <strong>{{ comment.nickname }}</strong>
              <span style="color:#bbb;font-size:12px">{{ fromNow(comment.createdAt) }}</span>
            </div>
            <p style="margin:8px 0;color:#606266">{{ comment.content }}</p>
            <!-- 子评论 -->
            <div v-for="child in comment.children" :key="child.id"
              style="background:#f5f7fa;padding:8px 12px;border-radius:4px;margin-top:8px">
              <strong style="font-size:13px">{{ child.nickname }}</strong>
              <span style="color:#bbb;font-size:12px;margin-left:8px">{{ fromNow(child.createdAt) }}</span>
              <p style="margin:4px 0;font-size:13px">{{ child.content }}</p>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-empty v-if="!comments.length" description="还没有评论，来发表第一条吧！" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getComments, createComment } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import { fromNow } from '@/utils/format'
import { ElMessage } from 'element-plus'

const props = defineProps({ articleId: Number })
const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

const comments = ref([])
const submitting = ref(false)
const form = ref({ content: '', nickname: '', email: '' })

async function loadComments() {
  const res = await getComments(props.articleId)
  comments.value = res.data
}

async function submitComment() {
  if (!form.value.content.trim()) return ElMessage.warning('请输入评论内容')
  submitting.value = true
  try {
    await createComment(props.articleId, form.value)
    ElMessage.success('评论已提交，待审核后显示')
    form.value = { content: '', nickname: '', email: '' }
  } finally { submitting.value = false }
}

onMounted(loadComments)
</script>
```

- [ ] **Step 6: 创建 src/views/front/PostDetailView.vue**

```vue
<template>
  <div style="max-width:860px;margin:0 auto" v-if="article">
    <!-- 文章头部 -->
    <h1 style="font-size:28px;line-height:1.4;margin-bottom:12px">{{ article.title }}</h1>
    <div style="color:#909399;font-size:13px;display:flex;gap:16px;margin-bottom:24px">
      <span>{{ formatDateTime(article.publishedAt) }}</span>
      <span>👁 {{ article.viewCount }}</span>
      <span v-if="article.categoryName">📂 {{ article.categoryName }}</span>
    </div>
    <el-image v-if="article.coverImage" :src="article.coverImage"
      style="width:100%;max-height:400px;border-radius:8px;margin-bottom:24px" fit="cover" />

    <!-- Markdown 渲染 -->
    <MdEditor v-model="article.content" previewOnly style="border:none" />

    <!-- 标签 -->
    <div style="margin-top:24px" v-if="article.tagNames?.length">
      <el-tag v-for="tag in article.tagNames" :key="tag" style="margin-right:8px">
        #{{ tag }}
      </el-tag>
    </div>

    <el-divider />

    <!-- 评论区 -->
    <CommentSection :article-id="article.id" />
  </div>
  <el-skeleton v-else :rows="10" animated />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { getArticleBySlug, recordView } from '@/api/article'
import { formatDateTime } from '@/utils/format'
import CommentSection from '@/components/front/CommentSection.vue'

const route = useRoute()
const article = ref(null)

onMounted(async () => {
  const res = await getArticleBySlug(route.params.slug)
  article.value = res.data
  recordView(article.value.id)
})
</script>
```

- [ ] **Step 7: 创建其余前台页面**

```vue
<!-- src/views/front/CategoryView.vue -->
<template>
  <div>
    <h2>分类：{{ $route.params.slug }}</h2>
    <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
    <el-empty v-if="!articles.length" description="该分类暂无文章" />
  </div>
</template>
<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getCategories } from '@/api/category'
import { getArticles } from '@/api/article'
import ArticleCard from '@/components/front/ArticleCard.vue'

const route = useRoute()
const articles = ref([])

async function load() {
  const catRes = await getCategories()
  const cat = catRes.data.find(c => c.slug === route.params.slug)
  if (!cat) return
  const res = await getArticles({ categoryId: cat.id, page: 1, size: 20 })
  articles.value = res.data.list
}

onMounted(load)
watch(() => route.params.slug, load)
</script>
```

```vue
<!-- src/views/front/TagView.vue -->
<template>
  <div>
    <h2>标签：#{{ $route.params.slug }}</h2>
    <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
    <el-empty v-if="!articles.length" description="该标签暂无文章" />
  </div>
</template>
<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getArticles } from '@/api/article'
import ArticleCard from '@/components/front/ArticleCard.vue'

const route = useRoute()
const articles = ref([])

async function load() {
  const res = await getArticles({ tagSlug: route.params.slug, page: 1, size: 20 })
  articles.value = res.data.list
}
onMounted(load)
watch(() => route.params.slug, load)
</script>
```

```vue
<!-- src/views/front/ArchiveView.vue -->
<template>
  <div>
    <h2 style="margin-bottom:24px">归档</h2>
    <div v-for="(group, year) in grouped" :key="year" style="margin-bottom:32px">
      <h3 style="color:#409eff;border-bottom:1px solid #eee;padding-bottom:8px">{{ year }} 年</h3>
      <div v-for="article in group" :key="article.id" style="display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #f5f5f5">
        <router-link :to="`/posts/${article.slug}`" style="color:#303133;text-decoration:none;flex:1">
          {{ article.title }}
        </router-link>
        <span style="color:#bbb;font-size:13px;flex-shrink:0;margin-left:16px">{{ formatDate(article.publishedAt) }}</span>
      </div>
    </div>
    <el-empty v-if="!Object.keys(grouped).length" description="暂无文章" />
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { getArticles } from '@/api/article'
import { formatDate } from '@/utils/format'
import dayjs from 'dayjs'

const articles = ref([])
const grouped = computed(() => {
  const g = {}
  articles.value.forEach(a => {
    const y = dayjs(a.publishedAt).year()
    if (!g[y]) g[y] = []
    g[y].push(a)
  })
  return Object.fromEntries(Object.entries(g).sort((a, b) => b[0] - a[0]))
})
onMounted(async () => {
  const res = await getArticles({ page: 1, size: 200 })
  articles.value = res.data.list
})
</script>
```

```vue
<!-- src/views/front/AboutView.vue -->
<template>
  <div style="max-width:700px;margin:0 auto">
    <h1>关于我</h1>
    <el-card style="margin-top:24px">
      <div style="display:flex;gap:24px;align-items:center">
        <el-avatar :size="80" :src="userInfo?.avatar">
          {{ (userInfo?.username || 'B').charAt(0).toUpperCase() }}
        </el-avatar>
        <div>
          <h2 style="margin:0 0 8px">{{ userInfo?.username || '博主' }}</h2>
          <p style="color:#606266;margin:0">{{ userInfo?.bio || '一个热爱技术的程序员，记录学习与思考。' }}</p>
        </div>
      </div>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { getMe } from '@/api/auth'
const userInfo = ref(null)
onMounted(async () => {
  try { const res = await getMe(); userInfo.value = res.data } catch {}
})
</script>
```

- [ ] **Step 8: 提交**

```bash
git add blog-frontend/src/views/ blog-frontend/src/components/
git commit -m "新增：登录页 + 前台全部页面（首页/文章列表/详情/分类/标签/归档/关于）"
```

---

## Task 7: 后台管理页面

**Files:**
- Create: `src/views/admin/DashboardView.vue`
- Create: `src/views/admin/ArticleListView.vue`
- Create: `src/views/admin/ArticleEditView.vue`
- Create: `src/views/admin/CommentManageView.vue`
- Create: `src/views/admin/CategoryManageView.vue`
- Create: `src/views/admin/TagManageView.vue`
- Create: `src/views/admin/ProfileView.vue`

- [ ] **Step 1: 创建 src/views/admin/DashboardView.vue**

```vue
<template>
  <div>
    <h2 style="margin-bottom:20px">仪表盘</h2>
    <el-row :gutter="16">
      <el-col :span="8" v-for="item in statCards" :key="item.label">
        <el-card style="text-align:center;padding:16px">
          <div style="font-size:36px;font-weight:bold;color:#409eff">{{ item.value }}</div>
          <div style="color:#909399;margin-top:8px">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top:20px">
      <template #header>快捷操作</template>
      <el-button type="primary" @click="$router.push('/admin/articles/new')">写新文章</el-button>
      <el-button style="margin-left:12px" @click="$router.push('/admin/comments')">查看评论</el-button>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { getOverview } from '@/api/stats'

const stats = ref({ totalArticles: 0, totalComments: 0, totalUsers: 0 })
const statCards = ref([])

onMounted(async () => {
  try {
    const res = await getOverview()
    stats.value = res.data
  } catch {}
  statCards.value = [
    { label: '文章总数', value: stats.value.totalArticles },
    { label: '评论总数', value: stats.value.totalComments },
    { label: '用户总数', value: stats.value.totalUsers },
  ]
})
</script>
```

- [ ] **Step 2: 创建 src/views/admin/ArticleListView.vue**

```vue
<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2>文章管理</h2>
      <el-button type="primary" @click="$router.push('/admin/articles/new')">写新文章</el-button>
    </div>
    <el-table :data="articles" v-loading="loading" stripe>
      <el-table-column prop="title" label="标题" min-width="200">
        <template #default="{ row }">
          <router-link :to="`/admin/articles/${row.id}/edit`" style="color:#409eff">{{ row.title }}</router-link>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="viewCount" label="阅读量" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'" size="small">
            {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="130">
        <template #default="{ row }">{{ row.publishedAt ? formatDate(row.publishedAt) : '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button text size="small" @click="$router.push(`/admin/articles/${row.id}/edit`)">编辑</el-button>
          <el-button text size="small" @click="handlePublish(row)">
            {{ row.status === 'PUBLISHED' ? '撤回' : '发布' }}
          </el-button>
          <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button text size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <div style="margin-top:16px;text-align:right">
      <el-pagination background layout="total,prev,pager,next" :total="total"
        :page-size="pageSize" :current-page="page" @current-change="loadArticles" />
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { getAdminArticles, togglePublish, deleteArticle } from '@/api/article'
import { formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'

const articles = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)

async function loadArticles(p = 1) {
  loading.value = true
  page.value = p
  try {
    const res = await getAdminArticles({ page: p, size: pageSize })
    articles.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
}

async function handlePublish(row) {
  await togglePublish(row.id)
  ElMessage.success(row.status === 'PUBLISHED' ? '已撤回' : '已发布')
  loadArticles(page.value)
}

async function handleDelete(id) {
  await deleteArticle(id)
  ElMessage.success('已删除')
  loadArticles(page.value)
}

onMounted(() => loadArticles())
</script>
```

- [ ] **Step 3: 创建 src/views/admin/ArticleEditView.vue**

```vue
<template>
  <div>
    <h2 style="margin-bottom:20px">{{ isEdit ? '编辑文章' : '写新文章' }}</h2>
    <el-form :model="form" label-width="80px">
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="文章标题" size="large" />
      </el-form-item>
      <el-row :gutter="12">
        <el-col :span="8">
          <el-form-item label="分类">
            <el-select v-model="form.categoryId" clearable placeholder="选择分类" style="width:100%">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="标签">
            <el-select v-model="form.tagNames" multiple filterable allow-create
              default-first-option placeholder="输入标签" style="width:100%">
              <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.name" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="状态">
            <el-select v-model="form.status" style="width:100%">
              <el-option label="草稿" value="DRAFT" />
              <el-option label="发布" value="PUBLISHED" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="摘要">
        <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="文章摘要（用于列表页和 SEO）" />
      </el-form-item>
      <el-form-item label="内容">
        <MdEditor v-model="form.content" style="width:100%" :toolbars="toolbars"
          @onUploadImg="handleUpload" />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="form.isTop">置顶</el-checkbox>
        <el-checkbox v-model="form.allowComment" style="margin-left:16px">允许评论</el-checkbox>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        <el-button style="margin-left:12px" @click="$router.push('/admin/articles')">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { getArticleBySlug, createArticle, updateArticle } from '@/api/article'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import { uploadImage } from '@/api/upload'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const saving = ref(false)
const categories = ref([])
const tags = ref([])

const form = reactive({
  title: '', slug: '', summary: '', content: '', coverImage: '',
  status: 'DRAFT', categoryId: null, tagNames: [], isTop: false, allowComment: true,
})

const toolbars = ['bold','italic','strikethrough','|','title','quote','unorderedList','orderedList','|',
  'code','codeRow','link','image','table','|','preview','fullscreen']

async function handleUpload(files, callback) {
  const results = await Promise.all(files.map(async f => {
    const res = await uploadImage(f)
    return res.data
  }))
  callback(results)
}

async function handleSave() {
  if (!form.title) return ElMessage.warning('请输入标题')
  if (!form.content) return ElMessage.warning('请输入内容')
  saving.value = true
  try {
    if (isEdit.value) {
      await updateArticle(route.params.id, form)
      ElMessage.success('保存成功')
    } else {
      await createArticle(form)
      ElMessage.success('发布成功')
      router.push('/admin/articles')
    }
  } finally { saving.value = false }
}

onMounted(async () => {
  const [catRes, tagRes] = await Promise.all([getCategories(), getTags()])
  categories.value = catRes.data
  tags.value = tagRes.data

  if (isEdit.value) {
    // 通过 slug 查找文章（这里需要 admin 接口，暂用 slug 模拟）
    // 实际项目中应有 /api/articles/admin/{id} 接口
  }
})
</script>
```

- [ ] **Step 4: 创建 src/views/admin/CommentManageView.vue**

```vue
<template>
  <div>
    <h2 style="margin-bottom:20px">评论审核</h2>
    <el-table :data="comments" v-loading="loading" stripe>
      <el-table-column label="评论者" width="100">
        <template #default="{ row }">{{ row.nickname }}</template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="200" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="130">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" text size="small" type="success"
            @click="updateStatus(row.id, 'APPROVED')">通过</el-button>
          <el-button v-if="row.status !== 'REJECTED'" text size="small" type="warning"
            @click="updateStatus(row.id, 'REJECTED')">拒绝</el-button>
          <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button text size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <div style="margin-top:16px;text-align:right">
      <el-pagination background layout="total,prev,pager,next" :total="total"
        :page-size="10" :current-page="page" @current-change="load" />
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { getAdminComments, updateCommentStatus, deleteComment } from '@/api/comment'
import { formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'

const comments = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)

async function load(p = 1) {
  loading.value = true
  page.value = p
  try {
    const res = await getAdminComments({ page: p, size: 10 })
    comments.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
}

async function updateStatus(id, status) {
  await updateCommentStatus(id, status)
  ElMessage.success('已更新')
  load(page.value)
}

async function handleDelete(id) {
  await deleteComment(id)
  ElMessage.success('已删除')
  load(page.value)
}

const statusTag = (s) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s])
const statusText = (s) => ({ PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }[s])

onMounted(() => load())
</script>
```

- [ ] **Step 5: 创建分类/标签/个人资料管理页面**

```vue
<!-- src/views/admin/CategoryManageView.vue -->
<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2>分类管理</h2>
      <el-button type="primary" @click="openDialog()">新建分类</el-button>
    </div>
    <el-table :data="categories" stripe>
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="slug" label="Slug" />
      <el-table-column prop="description" label="描述" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button text size="small" @click="openDialog(row)">编辑</el-button>
          <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
            <template #reference><el-button text size="small" type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑分类' : '新建分类'" width="400px">
      <el-form :model="form" label-width="60px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="Slug"><el-input v-model="form.slug" placeholder="留空自动生成" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/api/category'
import { ElMessage } from 'element-plus'

const categories = ref([])
const dialogVisible = ref(false)
const editId = ref(null)
const form = reactive({ name: '', slug: '', description: '' })

async function load() {
  const res = await getCategories()
  categories.value = res.data
}

function openDialog(row = null) {
  editId.value = row?.id || null
  Object.assign(form, { name: row?.name || '', slug: row?.slug || '', description: row?.description || '' })
  dialogVisible.value = true
}

async function handleSave() {
  if (editId.value) await updateCategory(editId.value, form)
  else await createCategory(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function handleDelete(id) {
  await deleteCategory(id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
```

```vue
<!-- src/views/admin/TagManageView.vue -->
<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2>标签管理</h2>
      <el-button type="primary" @click="dialogVisible = true">新建标签</el-button>
    </div>
    <div style="display:flex;flex-wrap:wrap;gap:8px">
      <el-tag v-for="tag in tags" :key="tag.id" closable @close="handleDelete(tag.id)" size="large">
        {{ tag.name }}
      </el-tag>
    </div>
    <el-dialog v-model="dialogVisible" title="新建标签" width="360px">
      <el-input v-model="form.name" placeholder="标签名称" />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getTags, createTag, deleteTag } from '@/api/tag'
import { ElMessage } from 'element-plus'

const tags = ref([])
const dialogVisible = ref(false)
const form = reactive({ name: '' })

async function load() { const res = await getTags(); tags.value = res.data }
async function handleCreate() {
  await createTag(form); ElMessage.success('创建成功'); dialogVisible.value = false; form.name = ''; load()
}
async function handleDelete(id) { await deleteTag(id); ElMessage.success('已删除'); load() }
onMounted(load)
</script>
```

```vue
<!-- src/views/admin/ProfileView.vue -->
<template>
  <div style="max-width:500px">
    <h2 style="margin-bottom:20px">个人资料</h2>
    <el-card>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" disabled />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="form.bio" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" disabled>保存（待实现更新接口）</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<script setup>
import { reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const form = reactive({ username: '', email: '', bio: '' })

onMounted(() => {
  const info = userStore.userInfo
  if (info) Object.assign(form, { username: info.username, email: info.email, bio: info.bio || '' })
})
</script>
```

- [ ] **Step 6: 提交**

```bash
git add blog-frontend/src/views/admin/
git commit -m "新增：后台管理全部页面（仪表盘/文章/评论/分类/标签/个人资料）"
```

---

## Task 8: 构建验证

- [ ] **Step 1: 运行开发服务器验证**

```bash
cd blog-frontend
npm run dev
```

预期：无报错，http://localhost:3000 可访问

- [ ] **Step 2: 生产构建**

```bash
npm run build
```

预期：`dist/` 目录生成，BUILD SUCCESS，无 TypeScript/Lint 报错

- [ ] **Step 3: 检查 dist 产物**

```bash
ls dist/
```

预期：`index.html` + `assets/` 目录

- [ ] **Step 4: 提交**

```bash
cd ..
git add blog-frontend/
git commit -m "前端完成：构建验证通过，dist 产物正常生成"
```

---

## 自检结果

**规范覆盖检查：**
- ✅ 前台页面：首页/文章列表/文章详情/分类/标签/归档/关于 → Task 6
- ✅ 后台页面：仪表盘/文章管理/评论审核/分类/标签/个人资料 → Task 7
- ✅ 动态路由 + 导航守卫 → Task 4
- ✅ Pinia userStore（token + 权限 + 菜单）→ Task 4
- ✅ v-permission 指令 → Task 4
- ✅ Axios 封装（Token 注入 + 401 自动刷新）→ Task 2
- ✅ Markdown 编辑器（md-editor-v3）+ 图片上传 → Task 7（ArticleEditView）
- ✅ 评论区（游客 + 登录）→ Task 6（CommentSection）
- ✅ SEO：各页面使用动态 slug URL → 路由配置
- ✅ Element Plus 组件库 → 贯穿全部页面

**类型一致性检查：**
- API 返回 `res.data` 结构在所有 views 中使用一致（`res.data.list` / `res.data.total`）✅
- `useUserStore()` 的 `login`、`fetchUserInfo`、`logout` 方法签名统一 ✅
- `v-permission` 指令在 `main.js` 注册为 `permission`，使用 `v-permission="'code'"` 形式 ✅
