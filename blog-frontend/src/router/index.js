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
      { path: 'search', name: 'Search', component: () => import('@/views/front/SearchView.vue') },
    ]
  },
  { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue') },
]

export const adminRoutes = [
  {
    path: '/admin',
    name: 'AdminRoot',
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
      { path: 'users', name: 'UserManage', component: () => import('@/views/admin/UserManageView.vue') },
      { path: 'roles', name: 'RoleManage', component: () => import('@/views/admin/RoleManageView.vue') },
      { path: 'menus', name: 'MenuManage', component: () => import('@/views/admin/MenuManageView.vue'), meta: { title: '菜单管理' } },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: staticRoutes,
  scrollBehavior: () => ({ top: 0 }),
})

// 检查 admin 路由是否已注册（替代模块级 flag，避免注销后角色污染）
const adminRouteRegistered = () => router.hasRoute('AdminRoot')

// 供 user store logout 调用：清除动态注册的 admin 路由
export function resetAdminRoutes() {
  if (adminRouteRegistered()) {
    router.removeRoute('AdminRoot')
  }
}

router.beforeEach(async (to) => {
  const token = getToken()

  if (to.path.startsWith('/admin')) {
    if (!token) return '/login'
    if (!adminRouteRegistered()) {
      const userStore = useUserStore()
      if (!userStore.userInfo) {
        try { await userStore.fetchUserInfo() } catch { return '/login' }
      }
      adminRoutes.forEach(r => router.addRoute(r))
      return to.fullPath
    }
  }

  if (to.name === 'Login' && token) return '/admin'
})

// 兜底 404 路由，必须在所有路由之后注册
router.addRoute({
  path: '/:pathMatch(.*)*',
  name: 'NotFound',
  component: () => import('@/views/NotFoundView.vue'),
})

export default router
