<template>
  <el-card class="article-card" shadow="hover" style="margin-bottom:20px">
    <div style="display:flex;gap:16px">
      <el-image v-if="article.coverImage" :src="article.coverImage"
        style="width:200px;height:120px;flex-shrink:0;border-radius:4px" fit="cover" />
      <div style="flex:1;overflow:hidden">
        <div style="display:flex;align-items:center;gap:8px;margin-bottom:8px">
          <el-tag v-if="article.isTop" type="danger" size="small">置顶</el-tag>
          <el-tag v-if="article.categoryName" type="info" size="small">{{ article.categoryName }}</el-tag>
        </div>
        <router-link :to="`/posts/${article.slug}`" class="article-title">
          {{ article.title }}
        </router-link>
        <p style="color:#909399;font-size:13px;margin:8px 0;line-height:1.6">
          {{ article.summary || '暂无摘要' }}
        </p>
        <div style="display:flex;align-items:center;gap:16px;font-size:12px;color:#bbb">
          <span>{{ formatDate(article.publishedAt) }}</span>
          <span>👁 {{ article.viewCount }}</span>
          <span v-for="tag in article.tagNames" :key="tag">
            <router-link :to="`/tag/${tag}`" style="color:#409eff">#{{ tag }}</router-link>
          </span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { formatDate } from '@/utils/format'
defineProps({ article: Object })
</script>

<style scoped>
.article-title { font-size:18px;font-weight:600;color:#303133;text-decoration:none;display:block; }
.article-title:hover { color:#409eff; }
</style>
