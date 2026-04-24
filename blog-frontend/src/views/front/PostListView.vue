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
          <div class="article-list">
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
  padding: 40px 64px 64px;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pagination-wrap {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

.filter-bar {
  background: var(--color-bg);
  border-bottom: 1px solid var(--color-border);
}
.filter-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 16px 64px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.filter-btn {
  padding: 6px 16px;
  border-radius: var(--radius-full);
  border: 1px solid var(--color-border);
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: border-color var(--transition-fast), color var(--transition-fast), background var(--transition-fast);
}
.filter-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.filter-btn.active {
  background: rgba(232,168,56,.12);
  border-color: var(--color-primary);
  color: var(--color-primary);
}
</style>
