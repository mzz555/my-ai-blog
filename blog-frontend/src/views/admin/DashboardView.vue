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

const statCards = ref([
  { label: '文章总数', value: 0 },
  { label: '评论总数', value: 0 },
  { label: '用户总数', value: 0 },
])

onMounted(async () => {
  try {
    const res = await getOverview()
    const data = res.data
    statCards.value = [
      { label: '文章总数', value: data.totalArticles },
      { label: '评论总数', value: data.totalComments },
      { label: '用户总数', value: data.totalUsers },
    ]
  } catch {}
})
</script>
