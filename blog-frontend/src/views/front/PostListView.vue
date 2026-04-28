<template>
  <div class="post-list-page">
    <!-- Page Hero -->
    <section class="page-hero">
      <div class="page-hero-inner">
        <span class="page-label">ARTICLES</span>
        <h1 class="page-title">所有文章</h1>
        <p class="page-sub">探索 Spring Boot、Vue 3、云原生与工程实践</p>
      </div>
    </section>

    <!-- Main: 文章区 + 侧边栏 -->
    <div class="main-wrap">
      <div class="content-inner">

        <!-- 文章区 -->
        <div class="article-area">
          <el-skeleton v-if="loading" :rows="6" animated />
          <template v-else>
            <div class="article-grid">
              <ArticleCard
                v-for="(article, i) in articles"
                :key="article.id"
                :article="article"
                :style="{ animationDelay: i * 70 + 'ms' }"
              />
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

        <!-- 右侧侧边栏 -->
        <aside class="sidebar">

          <!-- 分类 -->
          <div class="sidebar-section">
            <div class="sidebar-title">CATEGORIES</div>
            <ul class="cat-list">
              <li
                :class="['cat-item', { active: selectedCategoryId === null }]"
                @click="selectCategory(null)"
              >
                <span class="cat-dot" />
                全部
              </li>
              <li
                v-for="cat in categories"
                :key="cat.id"
                :class="['cat-item', { active: selectedCategoryId === cat.id }]"
                @click="selectCategory(cat.id)"
              >
                <span class="cat-dot" />
                {{ cat.name }}
              </li>
            </ul>
          </div>

          <!-- 标签 -->
          <div class="sidebar-section" v-if="sortedTags.length">
            <div class="sidebar-title">TAGS</div>
            <div class="tag-cloud">
              <span
                v-for="tag in sortedTags"
                :key="tag.id"
                :class="['tag-pill', { active: selectedTagSlug === tag.slug }]"
                @click="selectTag(tag.slug)"
              >
                #{{ tag.name }}
              </span>
            </div>
          </div>

        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useHead } from '@vueuse/head'
import { getArticles } from '@/api/article'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import ArticleCard from '@/components/front/ArticleCard.vue'

const blogName = import.meta.env.VITE_BLOG_NAME || 'DevLog.'

useHead({
  title: `文章列表 | ${blogName}`,
  meta: [
    { name: 'description', content: '探索 Spring Boot、Vue 3、云原生与工程实践技术文章。' },
    { property: 'og:title', content: `文章列表 | ${blogName}` },
    { property: 'og:type', content: 'website' },
  ],
})

const articles = ref([])
const categories = ref([])
const tags = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)
const selectedCategoryId = ref(null)
const selectedTagSlug = ref(null)

const sortedTags = computed(() =>
  [...tags.value].sort((a, b) => a.name.localeCompare(b.name, 'zh'))
)

async function loadArticles(p = 1) {
  loading.value = true
  page.value = p
  try {
    const params = { page: p, size: pageSize }
    if (selectedCategoryId.value) params.categoryId = selectedCategoryId.value
    if (selectedTagSlug.value)    params.tagSlug    = selectedTagSlug.value
    const res = await getArticles(params)
    articles.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function selectCategory(id) {
  selectedCategoryId.value = selectedCategoryId.value === id ? null : id
  selectedTagSlug.value = null
  loadArticles(1)
}

function selectTag(slug) {
  selectedTagSlug.value = selectedTagSlug.value === slug ? null : slug
  selectedCategoryId.value = null
  loadArticles(1)
}

onMounted(async () => {
  const [, catRes, tagRes] = await Promise.all([
    loadArticles(),
    getCategories(),
    getTags(),
  ])
  categories.value = catRes.data || []
  tags.value = tagRes.data || []
})
</script>

<style scoped>
.post-list-page { display: flex; flex-direction: column; }

/* ── Hero ── */
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

/* ── 主体双栏 ── */
.main-wrap { background: var(--color-bg); flex: 1; }
.content-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 40px 64px 64px;
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

/* ── 文章区 ── */
.article-area { flex: 1; min-width: 0; }
.article-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}
.pagination-wrap { display: flex; justify-content: center; }

/* ── 侧边栏 ── */
.sidebar {
  width: 220px;
  flex-shrink: 0;
  border-left: 1px solid var(--color-border);
  padding-left: 32px;
  position: sticky;
  top: 80px;
}
.sidebar-section { margin-bottom: 28px; }
.sidebar-title {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 2px;
  color: var(--color-text-tertiary);
  margin-bottom: 12px;
}

/* 分类列表 */
.cat-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 2px; }
.cat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 6px;
  font-size: 13px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.cat-item:hover { background: var(--color-surface); color: var(--color-text-primary); }
.cat-item.active { color: var(--color-accent); }
.cat-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  border: 1.5px solid currentColor;
  flex-shrink: 0;
  transition: background 0.15s;
}
.cat-item.active .cat-dot { background: var(--color-accent); }

/* 标签 cloud */
.tag-cloud { display: flex; flex-wrap: wrap; gap: 8px; }
.tag-pill {
  padding: 3px 10px;
  border: 1px solid var(--color-border);
  border-radius: 20px;
  font-size: 12px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s;
}
.tag-pill:hover { border-color: var(--color-accent); color: var(--color-accent); }
.tag-pill.active { border-color: var(--color-accent); color: var(--color-accent); }

/* ── 响应式 ── */
@media (max-width: 1024px) {
  .article-grid { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .content-inner {
    flex-direction: column-reverse;
    padding: 24px 20px 48px;
    gap: 24px;
  }
  .sidebar {
    width: 100%;
    border-left: none;
    padding-left: 0;
    border-bottom: 1px solid var(--color-border);
    padding-bottom: 24px;
    position: static;
  }
  .page-hero-inner { padding: 40px 20px 32px; }
}
</style>
