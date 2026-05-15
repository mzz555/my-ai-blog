<template>
  <div class="layout">
    <header class="header">
      <div class="header-inner">
        <router-link to="/" class="brand">{{ blogName }}</router-link>
        <nav class="nav">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/posts" class="nav-link">文章</router-link>
          <router-link to="/archive" class="nav-link">归档</router-link>
          <router-link to="/about" class="nav-link">关于</router-link>
        </nav>
        <div class="nav-right">
          <button class="search-icon-btn" @click="toggleSearch" aria-label="搜索">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
          </button>
          <router-link v-if="isLoggedIn" to="/admin" class="nav-cta">后台</router-link>
          <router-link v-else to="/login" class="nav-cta">登录</router-link>
          <ThemeToggle />
        </div>
      </div>
      <div v-if="searchOpen" class="search-bar">
        <form class="search-form" @submit.prevent="doSearch">
          <input v-model="searchQuery" ref="searchInput" class="search-input" type="search" placeholder="搜索文章…" />
          <button type="submit" class="search-btn">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
          </button>
          <button type="button" class="search-close" @click="searchOpen = false">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </form>
      </div>
    </header>

    <main class="main-content">
      <router-view />
    </main>

    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-top">
          <div class="footer-brand">
            <router-link to="/" class="footer-logo">{{ blogName }}</router-link>
            <p class="footer-brand-desc">记录技术成长，分享工程实践。专注 Spring Boot、Vue 3 与云原生。</p>
          </div>
          <div class="footer-col">
            <div class="footer-col-title">导航</div>
            <router-link to="/" class="footer-link">首页</router-link>
            <router-link to="/posts" class="footer-link">文章</router-link>
            <router-link to="/archive" class="footer-link">归档</router-link>
            <router-link to="/about" class="footer-link">关于</router-link>
          </div>
          <div class="footer-col">
            <div class="footer-col-title">链接</div>
            <a href="#" class="footer-link">GitHub</a>
            <a href="#" class="footer-link">RSS</a>
            <router-link to="/search" class="footer-link">搜索</router-link>
          </div>
        </div>
        <div class="footer-divider"></div>
        <div class="footer-bottom">
          <span>© {{ new Date().getFullYear() }} {{ blogName }}. 保留所有权利</span>
          <span>用 ❤️ 和 Java 构建</span>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import ThemeToggle from '@/components/common/ThemeToggle.vue'

const router = useRouter()
const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)
const blogName = import.meta.env.VITE_BLOG_NAME || 'DevLog.'
const searchQuery = ref('')
const searchOpen = ref(false)
const searchInput = ref(null)

function toggleSearch() {
  searchOpen.value = !searchOpen.value
  if (searchOpen.value) {
    setTimeout(() => searchInput.value?.focus(), 50)
  }
}

function doSearch() {
  const q = searchQuery.value.trim()
  if (!q) return
  router.push({ path: '/search', query: { q } })
  searchQuery.value = ''
  searchOpen.value = false
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--color-bg);
  background-image: var(--bg-glow-1), var(--bg-glow-2);
  background-attachment: fixed;
  background-repeat: no-repeat;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 72px;
  background: var(--color-header-bg);
  border-bottom: 1px solid var(--color-header-border);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  transition: background var(--transition-base), border-color var(--transition-base);
}

.header-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  height: 100%;
  padding: 0 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
}

.brand {
  font-family: var(--font-display);
  font-variation-settings: "opsz" 100, "wght" 600, "SOFT" 50;
  font-size: 26px;
  font-weight: 600;
  color: var(--color-accent);
  text-decoration: none;
  letter-spacing: -0.015em;
  flex-shrink: 0;
  transition: opacity var(--transition-fast);
}
.brand:hover { opacity: 0.85; }

.nav {
  display: flex;
  align-items: center;
  gap: 32px;
  flex: 1;
  justify-content: center;
}

.nav-link {
  color: var(--color-text-secondary);
  text-decoration: none;
  font-size: 14px;
  font-weight: normal;
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  transition: color var(--transition-fast), background var(--transition-fast);
}
.nav-link:hover {
  color: #E8A838;
  background: var(--color-accent-light);
}
.nav-link.router-link-exact-active {
  color: #E8A838;
  font-weight: 600;
}

/* Nav right */
.nav-right { display: flex; align-items: center; gap: 20px; flex-shrink: 0; }

.search-icon-btn {
  display: flex;
  align-items: center;
  background: none;
  border: none;
  color: var(--color-text-tertiary);
  cursor: pointer;
  padding: 0;
  transition: color var(--transition-fast);
}
.search-icon-btn:hover { color: var(--color-accent); }

.nav-cta {
  display: inline-flex;
  align-items: center;
  padding: 7px 18px;
  background: #E8A838;
  color: #0C0C10;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: background var(--transition-fast);
}
.nav-cta:hover { background: #F5BC50; color: #0C0C10; }

/* Search bar */
.search-bar {
  border-top: 1px solid var(--color-border);
  background: var(--color-header-bg);
  padding: 10px 48px;
}
.search-form {
  max-width: 600px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  padding: 0 12px;
  transition: border-color var(--transition-fast);
}
.search-form:focus-within { border-color: var(--color-accent); }
.search-input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 8px 4px;
  font-size: 14px;
  color: var(--color-text-primary);
  outline: none;
}
.search-input::placeholder { color: var(--color-text-tertiary); }
.search-input::-webkit-search-cancel-button { display: none; }
.search-btn, .search-close {
  display: flex; align-items: center;
  background: none; border: none;
  color: var(--color-text-tertiary);
  cursor: pointer; padding: 4px;
  transition: color var(--transition-fast);
}
.search-btn:hover, .search-close:hover { color: var(--color-accent); }

.main-content { flex: 1; width: 100%; }

/* Footer */
.footer {
  background: var(--color-bg);
  border-top: 1px solid var(--color-border);
}
.footer-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 48px 64px 40px;
}
.footer-top {
  display: flex;
  justify-content: space-between;
  gap: 32px;
  margin-bottom: 40px;
}
.footer-brand { width: 280px; display: flex; flex-direction: column; gap: 14px; }
.footer-logo {
  font-family: var(--font-display);
  font-variation-settings: "opsz" 100, "wght" 600, "SOFT" 50;
  font-size: 22px;
  font-weight: 600;
  color: #E8A838;
  text-decoration: none;
  letter-spacing: -0.012em;
}
.footer-brand-desc { margin: 0; font-size: 13px; color: var(--color-text-tertiary); line-height: 1.7; }
.footer-col { display: flex; flex-direction: column; gap: 14px; width: 160px; }
.footer-col-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}
.footer-link {
  font-size: 13px;
  color: var(--color-text-tertiary);
  text-decoration: none;
  transition: color var(--transition-fast);
}
.footer-link:hover { color: #E8A838; }
.footer-divider { height: 1px; background: var(--color-border); margin-bottom: 24px; }
.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--color-text-muted);
}
</style>
