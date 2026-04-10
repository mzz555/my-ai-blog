import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getToken } from '@/utils/auth'

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
      return to.fullPath
    }
  }

  if (to.name === 'Login' && token) return '/admin'
})

export default router
