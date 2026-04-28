<template>
  <div class="search-page">
    <!-- 空查询提示 -->
    <div v-if="!q.trim()" class="empty-query">
      <svg viewBox="0 0 48 48" width="56" height="56" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="22" cy="22" r="14"/><line x1="32" y1="32" x2="44" y2="44"/>
      </svg>
      <p>请输入关键词后搜索</p>
    </div>

    <template v-else>
      <h1 class="page-title">
        搜索：<span class="keyword">{{ q }}</span>
        <span v-if="!loading" class="count">（{{ total }} 条结果）</span>
      </h1>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="4" animated v-for="i in 3" :key="i" style="margin-bottom:24px" />
      </div>

      <template v-else-if="list.length">
        <article v-for="item in list" :key="item.id" class="result-card">
          <router-link :to="`/posts/${item.slug}`" class="result-title" v-html="highlight(item.title)" />
          <p class="result-summary" v-html="highlight(item.summary)" />
          <div class="result-meta">
            <span v-if="item.categoryName" class="cat">{{ item.categoryName }}</span>
            <span class="date">{{ formatDateTime(item.publishedAt) }}</span>
            <span class="views">{{ item.viewCount }} 阅读</span>
          </div>
        </article>

        <el-pagination
          v-if="total > pageSize"
          class="pagination"
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="page"
          @current-change="changePage"
        />
      </template>

      <div v-else class="empty">
        <el-empty description="没有找到相关文章" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchArticles } from '@/api/article'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const q = ref(route.query.q || '')
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 10
const loading = ref(false)

async function fetchResults() {
  if (!q.value.trim()) return
  loading.value = true
  try {
    const res = await searchArticles({ q: q.value.trim(), page: page.value, size: pageSize })
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function changePage(p) {
  router.push({ path: '/search', query: { q: q.value, page: p } })
}

function highlight(text) {
  if (!text || !q.value.trim()) return text || ''
  const escaped = q.value.trim().replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return text.replace(new RegExp(escaped, 'gi'), m => `<mark class="hl">${m}</mark>`)
}

watch(
  () => route.query,
  (newQuery) => {
    q.value = newQuery.q || ''
    page.value = parseInt(newQuery.page) || 1
    fetchResults()
  },
  { immediate: true }
)
</script>

<style scoped>
.search-page { max-width: 740px; margin: 0 auto; }

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: var(--space-8);
}
.keyword { color: var(--color-accent); }
.count { font-size: 14px; font-weight: 400; color: var(--color-text-tertiary); margin-left: var(--space-2); }

.result-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-5) var(--space-6);
  margin-bottom: var(--space-4);
  transition: box-shadow var(--transition-fast);
}
.result-card:hover { box-shadow: var(--shadow-md); }

.result-title {
  display: block;
  font-size: 17px;
  font-weight: 600;
  color: var(--color-text-primary);
  text-decoration: none;
  margin-bottom: var(--space-2);
  transition: color var(--transition-fast);
}
.result-title:hover { color: var(--color-accent); }

.result-summary {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.7;
  margin: 0 0 var(--space-3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-meta {
  display: flex;
  gap: var(--space-4);
  font-size: 12px;
  color: var(--color-text-tertiary);
  align-items: center;
}
.cat {
  background: var(--color-accent-light);
  color: var(--color-accent);
  border: 1px solid var(--color-accent-border);
  border-radius: var(--radius-full);
  padding: 1px 8px;
  font-weight: 600;
}

.pagination { margin-top: var(--space-8); justify-content: center; }
.loading { margin-top: var(--space-4); }

.empty-query {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 80px 0;
  color: var(--color-text-muted);
}
.empty-query svg { opacity: 0.4; }
.empty-query p { font-size: 15px; color: var(--color-text-secondary); }

:deep(.hl) {
  background: rgba(232,168,56,.25);
  color: var(--color-accent);
  border-radius: 2px;
  padding: 0 1px;
  font-style: normal;
}
</style>
