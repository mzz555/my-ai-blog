<template>
  <div class="sidebar-wrap">
    <div class="sidebar-logo">
      <span v-if="!collapsed" class="logo-text">DevLog<span class="logo-dot">.</span></span>
      <span v-else class="logo-only">D</span>
    </div>

    <nav class="sidebar-nav">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        :class="['nav-item', { active: isActive(item.path) }]"
        :title="collapsed ? item.label : ''"
      >
        <el-icon :size="18"><component :is="item.icon" /></el-icon>
        <span v-if="!collapsed" class="nav-label">{{ item.label }}</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'

defineProps({ collapsed: Boolean })

const route = useRoute()

const navItems = [
  { path: '/admin', icon: 'Odometer', label: '仪表盘' },
  { path: '/admin/articles', icon: 'Document', label: '文章管理' },
  { path: '/admin/comments', icon: 'ChatDotRound', label: '评论审核' },
  { path: '/admin/categories', icon: 'FolderOpened', label: '分类管理' },
  { path: '/admin/tags', icon: 'CollectionTag', label: '标签管理' },
  { path: '/admin/users', icon: 'UserFilled', label: '用户管理' },
  { path: '/admin/roles', icon: 'Lock', label: '角色管理' },
  { path: '/admin/menus', icon: 'Menu', label: '菜单管理' },
  { path: '/admin/profile', icon: 'User', label: '个人资料' },
]

function isActive(path) {
  if (path === '/admin') return route.path === '/admin'
  return route.path.startsWith(path)
}
</script>

<style scoped>
.sidebar-wrap {
  height: 100%;
  background: #0D0D18;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-right: 1px solid rgba(255,255,255,.06);
}

.sidebar-logo {
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255,255,255,.06);
  flex-shrink: 0;
  padding: 0 16px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #F0F0F8;
  white-space: nowrap;
}

.logo-dot { color: #E8A838; }

.logo-only {
  font-size: 18px;
  font-weight: 700;
  color: #E8A838;
}

.sidebar-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 12px 8px;
  overflow-y: auto;
  overflow-x: hidden;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--radius-md);
  border: 1px solid transparent;
  color: #6E6E82;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  transition: background var(--transition-fast), color var(--transition-fast), border-color var(--transition-fast);
}
.nav-item:hover {
  background: rgba(255,255,255,.06);
  color: #C0C0D0;
}
.nav-item.active {
  background: rgba(232,168,56,.12);
  color: #E8A838;
  border-color: rgba(232,168,56,.2);
}

.nav-label { flex: 1; }
</style>
