<template>
  <div class="admin-layout">
    <aside class="sidebar" :class="{ collapsed }">
      <AdminSidebar :collapsed="collapsed" />
    </aside>

    <div class="admin-body">
      <header class="admin-header">
        <div class="header-left">
          <button class="collapse-btn" @click="appStore.toggleSidebar()">
            <el-icon :size="18">
              <component :is="collapsed ? 'Expand' : 'Fold'" />
            </el-icon>
          </button>

          <!-- 面包屑 -->
          <nav class="breadcrumb">
            <template v-for="(crumb, i) in breadcrumbs" :key="crumb.path">
              <span v-if="i > 0" class="bc-sep">/</span>
              <router-link v-if="i < breadcrumbs.length - 1" :to="crumb.path" class="bc-link">
                {{ crumb.name }}
              </router-link>
              <span v-else class="bc-current">{{ crumb.name }}</span>
            </template>
          </nav>
        </div>

        <div class="header-right">
          <ThemeToggle />
          <el-dropdown trigger="click" placement="bottom-end">
            <div class="user-trigger">
              <div class="user-avatar">{{ userInitial }}</div>
              <span class="username">{{ userStore.userInfo?.username }}</span>
              <el-icon :size="12" class="chevron"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/admin/profile')">
                  <el-icon><User /></el-icon> 个人资料
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="admin-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'
import ThemeToggle from '@/components/common/ThemeToggle.vue'
import { ArrowDown, User, SwitchButton } from '@element-plus/icons-vue'

const userStore = useUserStore()
const appStore = useAppStore()
const router = useRouter()
const route = useRoute()
const collapsed = computed(() => appStore.sidebarCollapsed)

const userInitial = computed(() => {
  const name = userStore.userInfo?.username || ''
  return name.charAt(0).toUpperCase() || 'A'
})

const breadcrumbs = computed(() => {
  return route.matched
    .filter(r => r.meta?.title)
    .map(r => ({ name: r.meta.title, path: r.path }))
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  background: var(--color-bg-tertiary);
  overflow: hidden;
}

.sidebar {
  width: var(--sidebar-width);
  flex-shrink: 0;
  transition: width var(--transition-base);
  overflow: hidden;
}
.sidebar.collapsed { width: var(--sidebar-collapsed-width); }

.admin-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

.admin-header {
  height: var(--header-height);
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-5);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  flex-shrink: 0;
  transition: background var(--transition-fast), color var(--transition-fast);
}
.collapse-btn:hover {
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.bc-sep {
  color: var(--color-text-tertiary);
  font-size: 11px;
}

.bc-link {
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: color var(--transition-fast);
}
.bc-link:hover { color: #E8A838; }

.bc-current {
  color: var(--color-text-primary);
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 10px 5px 5px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--transition-fast);
}
.user-trigger:hover { background: var(--color-bg-secondary); }

.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #E8A838;
  color: #0C0C10;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.username {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-primary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chevron { color: var(--color-text-tertiary); }

.admin-main {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-6);
}
</style>
