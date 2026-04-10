<template>
  <div>
    <h2 style="margin-bottom:24px">归档</h2>
    <div v-for="(group, year) in grouped" :key="year" style="margin-bottom:32px">
      <h3 style="color:#409eff;border-bottom:1px solid #eee;padding-bottom:8px">{{ year }} 年</h3>
      <div v-for="article in group" :key="article.id"
        style="display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #f5f5f5">
        <router-link :to="`/posts/${article.slug}`" style="color:#303133;text-decoration:none;flex:1">
          {{ article.title }}
        </router-link>
        <span style="color:#bbb;font-size:13px;flex-shrink:0;margin-left:16px">
          {{ formatDate(article.publishedAt) }}
        </span>
      </div>
    </div>
    <el-empty v-if="!Object.keys(grouped).length" description="暂无文章" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getArticles } from '@/api/article'
import { formatDate } from '@/utils/format'
import dayjs from 'dayjs'

const articles = ref([])
const grouped = computed(() => {
  const g = {}
  articles.value.forEach(a => {
    const y = dayjs(a.publishedAt).year()
    if (!g[y]) g[y] = []
    g[y].push(a)
  })
  return Object.fromEntries(Object.entries(g).sort((a, b) => b[0] - a[0]))
})

onMounted(async () => {
  const res = await getArticles({ page: 1, size: 200 })
  articles.value = res.data.list
})
</script>
