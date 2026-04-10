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
