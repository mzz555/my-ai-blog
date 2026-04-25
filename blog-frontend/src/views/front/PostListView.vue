<template>
  <div class="post-list-page">
    <!-- Page Header -->
    <section class="page-hero">
      <div class="page-hero-inner">
        <span class="page-label">ARTICLES</span>
        <h1 class="page-title">所有文章</h1>
        <p class="page-sub">探索 Spring Boot、Vue 3、云原生与工程实践</p>
      </div>
    </section>

    <!-- 分类筛选 -->
    <div class="filter-bar">
      <div class="filter-inner">
        <button
          :class="['filter-btn', { active: !selectedCategory }]"
          @click="selectCategory(null)"
        >全部</button>
        <button
          v-for="cat in categories"
          :key="cat.id"
          :class="['filter-btn', { active: selectedCategory === cat.slug }]"
          @click="selectCategory(cat.slug)"
        >{{ cat.name }}</button>
      </div>
    </div>

    <!-- Article List -->
    <div class="list-wrap">
      <div class="list-inner">
        <el-skeleton v-if="loading" :rows="6" animated />
        <template v-else>
          <div class="article-grid">
            <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
          </div>
          <el-empty v-if="!articles.length" description="暂无文章" />
          <div v-if="total > pageSize" class="pagination-wrap">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="total"
              :page-size="pageSize"
              :current-page="page"
              @current-change="(p) => loadArticles(p)"
            />
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getArticles } from '@/api/article'
import { getCategories } from '@/api/category'
import ArticleCard from '@/components/front/ArticleCard.vue'

const articles = ref([])
const categories = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)
const selectedCategory = ref(null)

async function loadArticles(p = 1) {
  loading.value = true
  page.value = p
  try {
    const params = { page: p, size: pageSize }
    if (selectedCategory.value) params.categorySlug = selectedCategory.value
    const res = await getArticles(params)
    articles.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function selectCategory(slug) {
  selectedCategory.value = slug
  loadArticles(1)
}

onMounted(async () => {
  loadArticles()
  const res = await getCategories()
  categories.value = res.data || []
})
</script>

<style scoped>
.post-list-page { display: flex; flex-direction: column; }

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
  letter-spacing: -0.5px;
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
  padding: 0 0 64px;
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  padding: 40px 64px;
}
@media (max-width: 1024px) {
  .article-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .article-grid { grid-template-columns: 1fr; padding: 24px 20px; }
}

.pagination-wrap {
  margin-top: 0;
  padding: 0 64px 0;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination .el-pager li.is-active) {
  background: #E8A838;
  color: #000;
  border-radius: 6px;
}
:deep(.el-pagination .el-pager li) {
  background: #13131E;
  color: #9CA3AF;
  border-radius: 6px;
}
:deep(.el-pagination button) {
  background: #13131E;
  color: #9CA3AF;
}

.filter-bar {
  background: #13131E;
  border-bottom: 1px solid #1C1C2C;
}
.filter-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 16px 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.filter-btn {
  height: 32px;
  padding: 0 16px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 13px;
  background: #1E2030;
  color: #D1D5DB;
  transition: background 0.2s, color 0.2s;
}
.filter-btn:hover:not(.active) { background: #2A2A40; }
.filter-btn.active {
  background: #E8A838;
  color: #000;
  font-weight: 600;
}
</style>
