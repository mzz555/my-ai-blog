<template>
  <article class="grid-card" @click="$router.push(`/posts/${article.slug}`)">
    <div class="card-accent" :style="{ background: accentColor }"></div>
    <div class="card-body">
      <div class="card-top">
        <span v-if="article.categoryName" class="card-cat">{{ article.categoryName }}</span>
      </div>
      <h3 class="card-title">{{ article.title }}</h3>
      <p class="card-summary">{{ article.summary || '暂无摘要' }}</p>
      <div class="card-meta">
        <span class="meta-date">{{ formatDate(article.publishedAt) }}</span>
        <span class="meta-views">
          <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/></svg>
          {{ article.viewCount }}
        </span>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { formatDate } from '@/utils/format'

const props = defineProps({ article: Object })

const palette = ['#E8A838', '#4CAF8E', '#7C6FE8', '#E85A4F', '#4A9EE8', '#E87A38']
const accentColor = computed(() => {
  if (!props.article?.id) return palette[0]
  return palette[props.article.id % palette.length]
})
</script>

<style scoped>
.grid-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  transition: box-shadow var(--transition-base), transform var(--transition-base);
}
.grid-card:hover {
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-3px);
}

.card-accent { height: 4px; flex-shrink: 0; }

.card-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.card-top { display: flex; gap: 8px; }
.card-cat {
  font-size: 11px;
  font-weight: 600;
  color: #E8A838;
  background: rgba(232,168,56,.12);
  border: 1px solid rgba(232,168,56,.25);
  padding: 2px 8px;
  border-radius: var(--radius-full);
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color var(--transition-fast);
}
.grid-card:hover .card-title { color: #E8A838; }

.card-summary {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.7;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: var(--color-text-tertiary);
  padding-top: 8px;
  border-top: 1px solid var(--color-border);
}

.meta-views { display: flex; align-items: center; gap: 3px; }
</style>
