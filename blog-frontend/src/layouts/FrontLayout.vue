<template>
  <el-container direction="vertical" style="min-height: 100vh">
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

    <el-main style="max-width:1200px;margin:0 auto;width:100%;padding:24px 20px">
      <router-view />
    </el-main>

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
