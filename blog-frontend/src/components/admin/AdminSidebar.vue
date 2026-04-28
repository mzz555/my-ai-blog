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
  background: var(--color-sidebar-bg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-right: 1px solid var(--color-sidebar-border);
  transition: background var(--transition-base), border-color var(--transition-base);
}

/* Logo 区域 — 与 nav-item 左边缘对齐 */
.sidebar-logo {
  height: 56px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--color-sidebar-border);
  flex-shrink: 0;
  padding: 0 10px 0 14px;
  transition: border-color var(--transition-base);
}

.logo-text {
  font-size: 16px;
  font-weight: 800;
  color: var(--color-sidebar-logo);
  white-space: nowrap;
  letter-spacing: -0.3px;
  transition: color var(--transition-base);
}
.logo-dot { color: #E8A838; }

/* 折叠时居中显示单字母 */
.logo-only {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 800;
  color: #E8A838;
}

.sidebar-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 8px;
  overflow-y: auto;
  overflow-x: hidden;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0;
  height: 38px;
  padding: 0 6px;
  border-radius: 6px;
  color: var(--color-sidebar-text);
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  transition: background var(--transition-fast), color var(--transition-fast);
}
.nav-item:hover:not(.active) {
  background: var(--color-sidebar-hover-bg);
  color: var(--color-text-primary);
}
.nav-item.active {
  background: var(--color-sidebar-active-bg);
  color: var(--color-sidebar-active);
  font-weight: 600;
}

/* icon 固定宽度，保证折叠/展开时文字不抖动 */
.nav-item :deep(.el-icon) {
  width: 32px;
  flex-shrink: 0;
  display: flex;
  justify-content: center;
}

.nav-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
