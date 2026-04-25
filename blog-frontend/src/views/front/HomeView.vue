<template>
  <div class="home">
    <!-- Hero -->
    <section v-if="featured" class="hero">
      <div class="hero-inner">
        <div class="hero-left">
          <div class="hero-tag-row">
            <span class="hero-badge">{{ featured.categoryName || '技术' }}</span>
            <span v-if="featured.isTop" class="hero-badge hero-badge-top">置顶</span>
          </div>
          <h1 class="hero-title">{{ featured.title }}</h1>
          <p class="hero-desc">{{ featured.summary }}</p>
          <div class="hero-meta">
            <span class="hero-meta-item">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/></svg>
              {{ featured.viewCount }} 阅读
            </span>
            <span class="hero-meta-item">{{ formatDate(featured.publishedAt) }}</span>
            <span v-for="tag in featured.tagNames?.slice(0,2)" :key="tag" class="hero-tag">#{{ tag }}</span>
          </div>
          <div class="hero-btns">
            <router-link :to="`/posts/${featured.slug}`" class="btn-primary">阅读全文</router-link>
            <router-link to="/posts" class="btn-ghost">浏览全部</router-link>
          </div>
        </div>
        <div class="hero-right">
          <div class="hero-cover-card">
            <img v-if="featured.coverImage" :src="featured.coverImage" :alt="featured.title" class="hero-cover-img" />
            <div v-else class="hero-cover-placeholder">
              <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
            </div>
          </div>
        </div>
      </div>
    </section>
    <div class="section-divider"></div>

    <!-- 最新文章 -->
    <section class="articles-section">
      <div class="section-inner">
        <div class="section-header">
          <h2 class="section-title">最新文章</h2>
          <router-link to="/posts" class="section-link">
            查看全部
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
          </router-link>
        </div>

        <div v-if="loading" class="cards-grid">
          <div v-for="i in 3" :key="i" class="card-skeleton"></div>
        </div>

        <template v-else-if="articles.length">
          <div class="cards-grid">
            <ArticleCardGrid v-for="article in articles.slice(0,3)" :key="article.id" :article="article" />
          </div>
          <div v-if="articles.length > 3" class="cards-grid cards-grid-2">
            <ArticleCardGrid v-for="article in articles.slice(3,5)" :key="article.id" :article="article" />
          </div>
        </template>
        <el-empty v-else description="暂无文章" />
      </div>
    </section>
    <div class="section-divider"></div>

    <!-- Newsletter -->
    <section class="newsletter-section">
      <div class="newsletter-inner">
        <h2 class="newsletter-title">订阅技术周报</h2>
        <p class="newsletter-sub">每周精选 5 篇深度技术文章，直达你的邮箱。不推广告，只分享干货。</p>
        <div class="email-row">
          <input v-model="email" class="email-input" type="email" placeholder="输入你的邮箱地址" />
          <button class="email-btn" @click="subscribeNewsletter">订阅</button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useHead } from '@vueuse/head'
import { ElMessage } from 'element-plus'
import { getArticles } from '@/api/article'
import { formatDate } from '@/utils/format'
import ArticleCardGrid from '@/components/front/ArticleCardGrid.vue'

const blogName = import.meta.env.VITE_BLOG_NAME || 'DevLog.'

useHead({
  title: `${blogName} | 首页`,
  meta: [
    { name: 'description', content: '记录技术成长，分享工程实践。专注 Spring Boot、Vue 3 与云原生。' },
    { property: 'og:title', content: `${blogName} | 首页` },
    { property: 'og:description', content: '记录技术成长，分享工程实践。专注 Spring Boot、Vue 3 与云原生。' },
    { property: 'og:type', content: 'website' },
  ],
})

const articles = ref([])
const loading = ref(false)
const email = ref('')

const featured = computed(() => articles.value[0] || null)

async function loadArticles() {
  loading.value = true
  try {
    const res = await getArticles({ page: 1, size: 10 })
    articles.value = res.data.list
  } finally {
    loading.value = false
  }
}

async function subscribeNewsletter() {
  if (!email.value.trim()) {
    ElMessage.warning('请输入邮箱地址')
    return
  }
  try {
    email.value = ''
    ElMessage.success('订阅成功！感谢您的关注')
  } catch {
    ElMessage.error('订阅失败，请稍后重试')
  }
}

onMounted(loadArticles)
</script>

<style scoped>
.home { display: flex; flex-direction: column; }

/* Hero */
.hero {
  width: 100%;
  background: linear-gradient(180deg, #111120 0%, #0C0C10 100%);
}
:root:not([data-theme='dark']) .hero {
  background: linear-gradient(135deg, #FFFDF5 0%, #F7F7F2 100%);
}

.hero-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  display: flex;
  min-height: 500px;
}

.hero-left {
  flex: 0 0 680px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 22px;
  padding: 60px 0 60px 64px;
}

.hero-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.hero-tag-row { display: flex; gap: 8px; }

.hero-badge {
  padding: 5px 12px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 600;
  background: rgba(232, 168, 56, 0.15);
  color: #E8A838;
  border: 1px solid rgba(232, 168, 56, 0.3);
}
.hero-badge-top {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.3);
}

.hero-title {
  margin: 0;
  font-size: 50px;
  font-weight: 700;
  line-height: 1.2;
  color: #F0F0F8;
  letter-spacing: -1px;
}
:root:not([data-theme='dark']) .hero-title { color: #111827; }

.hero-desc {
  margin: 0;
  font-size: 15px;
  line-height: 1.75;
  color: #8A8A9E;
  max-width: 560px;
}
:root:not([data-theme='dark']) .hero-desc { color: #6B7280; }

.hero-meta { display: flex; align-items: center; gap: 20px; flex-wrap: wrap; }
.hero-meta-item { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #6E6E82; }
.hero-tag { font-size: 13px; color: #E8A838; }

.hero-btns { display: flex; gap: 14px; }

.btn-primary {
  display: inline-flex;
  align-items: center;
  padding: 11px 24px;
  background: #E8A838;
  color: #0C0C10;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: background var(--transition-fast), transform var(--transition-fast);
}
.btn-primary:hover { background: #F5BC50; transform: translateY(-1px); }

.btn-ghost {
  display: inline-flex;
  align-items: center;
  padding: 11px 24px;
  border: 1px solid #3A3A5C;
  color: #8A8A9E;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: border-color var(--transition-fast), color var(--transition-fast);
}
.btn-ghost:hover { border-color: #E8A838; color: #E8A838; }
:root:not([data-theme='dark']) .btn-ghost { border-color: #E5E7EB; color: #6B7280; }
:root:not([data-theme='dark']) .btn-ghost:hover { border-color: #E8A838; color: #E8A838; }

.hero-cover-card {
  width: 100%;
  max-width: 420px;
  height: 350px;
  background: #16162A;
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
:root:not([data-theme='dark']) .hero-cover-card {
  background: #FFFFFF;
  box-shadow: 0 8px 32px rgba(0,0,0,.12);
}
.hero-cover-img { width: 100%; height: 100%; object-fit: cover; }
.hero-cover-placeholder svg { opacity: 0.25; color: #6E6E82; }

/* Divider */
.section-divider { height: 1px; background: var(--color-border); }

/* Articles */
.articles-section { width: 100%; }
.section-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 56px 64px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.section-header { display: flex; justify-content: space-between; align-items: center; }
.section-title { margin: 0; font-size: 24px; font-weight: 700; color: var(--color-text-primary); }

.section-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #E8A838;
  text-decoration: none;
  transition: gap var(--transition-fast);
}
.section-link:hover { gap: 8px; }

.cards-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}
.cards-grid-2 { grid-template-columns: repeat(2, 1fr); }

.card-skeleton {
  height: 280px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  animation: pulse 1.5s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* Newsletter */
.newsletter-section {
  width: 100%;
  background: linear-gradient(135deg, #111120 0%, #0C0C10 100%);
}
:root:not([data-theme='dark']) .newsletter-section {
  background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%);
}

.newsletter-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 56px 64px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.newsletter-title {
  margin: 0;
  font-size: 30px;
  font-weight: 700;
  color: #F0F0F8;
  text-align: center;
}
:root:not([data-theme='dark']) .newsletter-title { color: #111827; }

.newsletter-sub {
  margin: 0;
  font-size: 15px;
  color: #6E6E82;
  line-height: 1.7;
  text-align: center;
  max-width: 520px;
}
:root:not([data-theme='dark']) .newsletter-sub { color: #6B7280; }

.email-row {
  display: flex;
  border-radius: 8px;
  overflow: hidden;
  background: #16162A;
  border: 1px solid #1E1E2C;
  width: 520px;
}
:root:not([data-theme='dark']) .email-row {
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  box-shadow: 0 2px 12px rgba(0,0,0,.08);
}

.email-input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 14px 20px;
  font-size: 14px;
  color: #F0F0F8;
  outline: none;
}
:root:not([data-theme='dark']) .email-input { color: #111827; }
.email-input::placeholder { color: #6E6E82; }

.email-btn {
  padding: 14px 24px;
  background: #E8A838;
  color: #0C0C10;
  border: none;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border-radius: 0 8px 8px 0;
  transition: background var(--transition-fast);
}
.email-btn:hover { background: #F5BC50; }
</style>
