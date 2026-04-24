<template>
  <div class="tag-page">
    <section class="page-hero">
      <div class="page-hero-inner">
        <nav class="breadcrumb">
          <router-link to="/" class="bc-link">首页</router-link>
          <span class="bc-sep">／</span>
          <router-link to="/tags" class="bc-link">标签</router-link>
          <span class="bc-sep">／</span>
          <span class="bc-current">{{ $route.params.slug }}</span>
        </nav>
        <span class="page-label">TAG</span>
        <h1 class="page-title">#{{ $route.params.slug }}</h1>
        <p class="page-sub">{{ total }} 篇相关文章</p>
      </div>
    </section>

    <div class="list-wrap">
      <div class="list-inner">
        <template v-if="articles.length">
          <div class="cards-grid">
            <ArticleCardGrid v-for="article in articles" :key="article.id" :article="article" />
          </div>
          <div v-if="total > pageSize" class="pagination-wrap">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="total"
              :page-size="pageSize"
              :current-page="page"
              @current-change="(p) => load(p)"
            />
          </div>
        </template>
        <el-empty v-else description="该标签暂无文章" />
        <div class="back-row">
          <router-link to="/tags" class="back-link">← 返回所有标签</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getArticles } from '@/api/article'
import ArticleCardGrid from '@/components/front/ArticleCardGrid.vue'

const route = useRoute()
const articles = ref([])
const page = ref(1)
const pageSize = 12
const total = ref(0)

async function load(p = 1) {
  page.value = p
  const res = await getArticles({ tagSlug: route.params.slug, page: p, size: pageSize })
  articles.value = res.data.list
  total.value = res.data.total
}

onMounted(() => load())
watch(() => route.params.slug, () => load())
</script>

<style scoped>
.tag-page { display: flex; flex-direction: column; }

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
  gap: 12px;
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

.list-wrap { background: var(--color-bg); }

.list-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 40px 64px 64px;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.pagination-wrap {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  margin-bottom: 4px;
}
.bc-link { color: var(--color-text-secondary); text-decoration: none; transition: color var(--transition-fast); }
.bc-link:hover { color: var(--color-primary); }
.bc-sep { color: var(--color-text-muted); }
.bc-current { color: var(--color-primary); font-weight: 500; }

.back-row { margin-top: 32px; }
.back-link {
  font-size: 14px;
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: color var(--transition-fast);
}
.back-link:hover { color: var(--color-primary); }
</style>
