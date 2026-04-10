<template>
  <el-container style="height:100vh">
    <el-aside :width="collapsed ? '64px' : '220px'" style="transition:width 0.3s;overflow:hidden">
      <AdminSidebar :collapsed="collapsed" />
    </el-aside>

    <el-container direction="vertical">
      <el-header style="background:#fff;border-bottom:1px solid #eee;display:flex;align-items:center;justify-content:space-between;padding:0 20px">
        <el-icon style="cursor:pointer;font-size:20px" @click="appStore.toggleSidebar()">
          <component :is="collapsed ? 'Expand' : 'Fold'" />
        </el-icon>
        <div style="display:flex;align-items:center;gap:12px">
          <span style="font-size:13px;color:#606266">{{ userStore.userInfo?.username }}</span>
          <el-button text size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

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
