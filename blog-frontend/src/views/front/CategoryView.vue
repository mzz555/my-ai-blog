<template>
  <div class="category-page">
    <section class="page-hero">
      <div class="page-hero-inner">
        <span class="page-label">CATEGORY</span>
        <h1 class="page-title">{{ category?.name || $route.params.slug }}</h1>
        <p v-if="category?.description" class="page-sub">{{ category.description }}</p>
        <p class="page-count">{{ total }} 篇文章</p>
      </div>
    </section>

    <div class="list-wrap">
      <div class="list-inner">
        <div v-if="loading" class="article-list">
          <div v-for="i in 4" :key="i" class="skeleton-card">
            <el-skeleton :rows="3" animated />
          </div>
        </div>
        <template v-else>
          <div class="article-list">
            <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
          </div>
          <el-empty v-if="!articles.length" description="该分类暂无文章" />
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
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getCategories } from '@/api/category'
import { getArticles } from '@/api/article'
import ArticleCard from '@/components/front/ArticleCard.vue'

const route = useRoute()
const articles = ref([])
const category = ref(null)
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)

async function load(p = 1) {
  loading.value = true
  page.value = p
  try {
    const catRes = await getCategories()
    category.value = catRes.data.find(c => c.slug === route.params.slug) || null
    if (!category.value) { loading.value = false; return }
    const res = await getArticles({ categoryId: category.value.id, page: p, size: pageSize })
    articles.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

onMounted(() => load())
watch(() => route.params.slug, () => load())
</script>

<style scoped>
.category-page { display: flex; flex-direction: column; }

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
  letter-spacing: -0.5px;
}

.page-sub {
  margin: 0;
  font-size: 15px;
  color: var(--color-text-secondary);
}

.page-count {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-tertiary);
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

.skeleton-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 24px;
}

.pagination-wrap {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}
</style>
