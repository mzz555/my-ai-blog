<template>
  <div class="archive-page">
    <section class="page-hero">
      <div class="page-hero-inner">
        <span class="page-label">ARCHIVE</span>
        <h1 class="page-title">归档</h1>
        <p class="page-sub">共 {{ articles.length }} 篇文章，记录每一步成长</p>
      </div>
    </section>

    <div class="archive-wrap">
      <div class="archive-inner">
        <el-skeleton v-if="loading" :rows="8" animated />

        <template v-else>
          <div v-for="(months, year) in grouped" :key="year" class="year-group">
            <div class="year-header">
              <span class="year-num">{{ year }}</span>
              <span class="year-count">{{ yearCount(year) }} 篇</span>
            </div>

            <div v-for="(posts, month) in months" :key="month" class="month-group">
              <div class="month-label">{{ month }} 月</div>
              <div class="post-list">
                <div v-for="article in posts" :key="article.id" class="post-item">
                  <div class="post-dot"></div>
                  <span class="post-date">{{ formatDate(article.publishedAt).slice(5) }}</span>
                  <router-link :to="`/posts/${article.slug}`" class="post-title">
                    {{ article.title }}
                  </router-link>
                  <span v-if="article.categoryName" class="post-cat">{{ article.categoryName }}</span>
                </div>
              </div>
            </div>
          </div>

          <el-empty v-if="!articles.length" description="暂无文章" />
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useHead } from '@vueuse/head'
import { getArticles } from '@/api/article'
import { formatDate } from '@/utils/format'
import dayjs from 'dayjs'

const blogName = import.meta.env.VITE_BLOG_NAME || 'DevLog.'

useHead({
  title: `归档 | ${blogName}`,
  meta: [
    { name: 'description', content: '按时间浏览所有文章归档。' },
    { property: 'og:title', content: `归档 | ${blogName}` },
    { property: 'og:type', content: 'website' },
  ],
})

const articles = ref([])
const loading = ref(false)

const grouped = computed(() => {
  const g = {}
  articles.value.forEach(a => {
    const d = dayjs(a.publishedAt)
    const y = d.year()
    const m = d.month() + 1
    if (!g[y]) g[y] = {}
    if (!g[y][m]) g[y][m] = []
    g[y][m].push(a)
  })
  const sorted = {}
  Object.keys(g).sort((a, b) => b - a).forEach(y => {
    sorted[y] = {}
    Object.keys(g[y]).sort((a, b) => b - a).forEach(m => {
      sorted[y][m] = g[y][m]
    })
  })
  return sorted
})

function yearCount(year) {
  return Object.values(grouped.value[year] || {}).reduce((s, arr) => s + arr.length, 0)
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getArticles({ page: 1, size: 200 })
    articles.value = res.data.list
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.archive-page { display: flex; flex-direction: column; }

.page-hero {
  background: var(--color-bg);
  border-bottom: 1px solid var(--color-border);
}

.page-hero-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 56px 64px 48px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.page-label {
  font-size: 12px;
  font-weight: 600;
  color: #E8A838;
  letter-spacing: 2px;
}

.page-title {
  margin: 0;
  font-size: 36px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.page-sub {
  margin: 0;
  font-size: 15px;
  color: var(--color-text-secondary);
}

.archive-wrap { background: var(--color-bg); }

.archive-inner {
  max-width: 860px;
  margin: 0 auto;
  padding: 40px 64px 80px;
}

.year-group { margin-bottom: 48px; }

.year-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--color-border);
}

.year-num {
  font-size: 32px;
  font-weight: 800;
  color: #E8A838;
  letter-spacing: -1px;
  line-height: 1;
}

.year-count {
  font-size: 13px;
  color: var(--color-text-tertiary);
}

.month-group { margin-bottom: 20px; }

.month-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-tertiary);
  letter-spacing: 1px;
  margin-bottom: 6px;
  padding-left: 20px;
}

.post-list { display: flex; flex-direction: column; gap: 2px; }

.post-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 7px 12px 7px 0;
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}
.post-item:hover { background: var(--color-surface); }

.post-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-border);
  flex-shrink: 0;
  margin-left: 4px;
  transition: background var(--transition-fast);
}
.post-item:hover .post-dot { background: #E8A838; }

.post-date {
  font-size: 12px;
  color: var(--color-text-tertiary);
  flex-shrink: 0;
  width: 40px;
  font-variant-numeric: tabular-nums;
}

.post-title {
  flex: 1;
  font-size: 14px;
  color: var(--color-text-primary);
  text-decoration: none;
  transition: color var(--transition-fast);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.post-title:hover { color: #E8A838; }

.post-cat {
  flex-shrink: 0;
  font-size: 11px;
  font-weight: 600;
  color: #E8A838;
  background: rgba(232,168,56,.12);
  border: 1px solid rgba(232,168,56,.25);
  padding: 2px 8px;
  border-radius: var(--radius-full);
}
</style>
