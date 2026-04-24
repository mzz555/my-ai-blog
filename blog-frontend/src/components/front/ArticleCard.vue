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
  display: flex;
  gap: 24px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 24px;
  cursor: pointer;
  transition: box-shadow var(--transition-base), transform var(--transition-base), border-color var(--transition-base);
}
.article-card:hover {
  box-shadow: var(--shadow-card-hover);
  border-color: rgba(232,168,56,.35);
  transform: translateY(-2px);
}

.cover {
  flex-shrink: 0;
  width: 200px;
  height: 130px;
  border-radius: var(--radius-md);
  overflow: hidden;
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}
.article-card:hover .cover img { transform: scale(1.04); }

.body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.meta-top { display: flex; gap: 8px; }

.badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 600;
}
.badge-top { background: rgba(239,68,68,.12); color: #ef4444; border: 1px solid rgba(239,68,68,.25); }
.badge-cat { background: rgba(232,168,56,.12); color: #E8A838; border: 1px solid rgba(232,168,56,.25); }

.title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary);
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
  color: var(--color-text-secondary);
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.meta-bottom {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.meta-icon { width: 13px; height: 13px; }

.tag-link {
  font-size: 12px;
  color: #E8A838;
  cursor: pointer;
  transition: color var(--transition-fast);
}
.tag-link:hover { color: #F5BC50; }
</style>
