<template>
  <article class="article-card" @click="$router.push(`/posts/${article.slug}`)">
    <div v-if="article.coverImage" class="cover">
      <img :src="article.coverImage" :alt="article.title" loading="lazy" />
    </div>
    <div class="body">
      <div class="meta-top">
        <span v-if="article.isTop" class="badge badge-top">置顶</span>
        <span v-if="article.categoryName" class="badge badge-cat">{{ article.categoryName }}</span>
      </div>
      <h2 class="title">{{ article.title }}</h2>
      <p class="summary">{{ article.summary || '暂无摘要' }}</p>
      <div class="meta-bottom">
        <span class="meta-item">{{ formatDate(article.publishedAt) }}</span>
        <span class="meta-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="meta-icon">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" /><circle cx="12" cy="12" r="3" />
          </svg>
          {{ article.viewCount }}
        </span>
        <span v-for="tag in article.tagNames?.slice(0,3)" :key="tag" class="tag-link"
          @click.stop="$router.push(`/tag/${tag}`)">
          #{{ tag }}
        </span>
      </div>
    </div>
  </article>
</template>

<script setup>
import { formatDate } from '@/utils/format'
defineProps({ article: Object })
</script>

<style scoped>
.article-card {
  background: #13131E;
  border: 1px solid #1C1C2C;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  transition: border-color 0.2s, transform var(--transition-base);
}
.article-card:hover {
  border-color: rgba(232, 168, 56, 0.4);
  transform: translateY(-2px);
}

.cover {
  width: 100%;
  height: 140px;
  overflow: hidden;
  background: #1C1C2E;
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}
.article-card:hover .cover img { transform: scale(1.04); }

.body {
  padding: 20px 20px 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-top { display: flex; gap: 6px; flex-wrap: wrap; }

.badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 3px;
}
.badge-top { background: #1C1C2E; color: #E8A838; }
.badge-cat { background: #1C1C2E; color: #8A8A9E; }

.title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #F0F0F8;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color var(--transition-fast);
}
.article-card:hover .title { color: #E8A838; }

.summary {
  margin: 0;
  font-size: 13px;
  color: #8A8A9E;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta-bottom { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6E6E82;
}

.meta-icon { width: 12px; height: 12px; }

.tag-link {
  font-size: 11px;
  color: #6E6E82;
  cursor: pointer;
  transition: color var(--transition-fast);
}
.tag-link:hover { color: #E8A838; }
</style>
