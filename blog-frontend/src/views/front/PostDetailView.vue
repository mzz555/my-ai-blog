<template>
  <div v-if="article" class="post-page">
    <!-- Hero Banner -->
    <section class="post-hero">
      <div class="post-hero-inner">
        <div class="breadcrumb">
          <router-link to="/" class="bc-link">首页</router-link>
          <span class="bc-sep">/</span>
          <router-link to="/posts" class="bc-link">文章</router-link>
          <span v-if="article.categoryName">
            <span class="bc-sep">/</span>
            <router-link :to="`/category/${article.categoryName}`" class="bc-link">{{ article.categoryName }}</router-link>
          </span>
        </div>

        <div class="hero-tags">
          <span v-if="article.categoryName" class="hero-cat">{{ article.categoryName }}</span>
          <span v-for="tag in article.tagNames?.slice(0,3)" :key="tag" class="hero-tag">#{{ tag }}</span>
        </div>

        <h1 class="hero-title">{{ article.title }}</h1>

        <div class="hero-meta">
          <span class="hero-meta-item">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/></svg>
            {{ article.viewCount }} 阅读
          </span>
          <span class="hero-meta-sep">·</span>
          <span class="hero-meta-item">{{ formatDateTime(article.publishedAt) }}</span>
          <span v-if="readingMinutes" class="hero-meta-sep">·</span>
          <span v-if="readingMinutes" class="hero-meta-item">约 {{ readingMinutes }} 分钟</span>
        </div>
      </div>
    </section>

    <!-- Body -->
    <div class="post-body-wrap">
      <div class="post-body-inner">
        <!-- Article Content -->
        <article class="post-content">
          <div v-if="article.coverImage" class="cover-wrap">
            <img :src="article.coverImage" :alt="article.title" class="cover-img" />
          </div>

          <div class="markdown-body">
            <MdPreview :modelValue="article.content" />
          </div>

          <!-- Footer: tags + like -->
          <div class="post-footer">
            <div v-if="article.tagNames?.length" class="tag-list">
              <router-link v-for="tag in article.tagNames" :key="tag"
                :to="`/tag/${tag}`" class="post-tag">
                #{{ tag }}
              </router-link>
            </div>

            <div class="like-area">
              <button :class="['like-btn', { liked }]" @click="handleLike">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                  <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                </svg>
                <span>{{ likeCount }} 喜欢</span>
              </button>
            </div>
          </div>

          <!-- 上一篇 / 下一篇 -->
          <nav v-if="neighbors.prev || neighbors.next" class="post-nav">
            <router-link
              v-if="neighbors.prev"
              :to="`/posts/${neighbors.prev.slug}`"
              class="nav-item nav-prev"
            >
              <span class="nav-dir">← 上一篇</span>
              <span class="nav-title">{{ neighbors.prev.title }}</span>
            </router-link>
            <span v-else class="nav-item nav-empty" />
            <router-link
              v-if="neighbors.next"
              :to="`/posts/${neighbors.next.slug}`"
              class="nav-item nav-next"
            >
              <span class="nav-dir">下一篇 →</span>
              <span class="nav-title">{{ neighbors.next.title }}</span>
            </router-link>
            <span v-else class="nav-item nav-empty" />
          </nav>

          <el-divider />
          <CommentSection :article-id="article.id" />
        </article>

        <!-- Sidebar -->
        <aside class="post-sidebar">
          <!-- TOC -->
          <div v-if="headings.length" class="sidebar-widget">
            <div class="widget-label">目录</div>
            <nav class="toc">
              <a v-for="h in headings" :key="h.id"
                :href="`#${h.id}`"
                :class="['toc-item', `toc-h${h.level}`, { active: activeId === h.id }]"
                @click.prevent="scrollTo(h.id)">
                {{ h.text }}
              </a>
            </nav>
          </div>

          <!-- Author Card -->
          <div class="sidebar-widget author-card">
            <div class="author-avatar">{{ authorChar }}</div>
            <div class="author-name">{{ authorName }}</div>
            <p class="author-bio">{{ authorBio }}</p>
            <div class="author-stats">
              <div class="author-stat">
                <span class="as-num">{{ article.viewCount }}</span>
                <span class="as-lab">阅读</span>
              </div>
              <div class="as-sep"></div>
              <div class="author-stat">
                <span class="as-num">{{ likeCount }}</span>
                <span class="as-lab">喜欢</span>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>

  <div v-else class="loading-wrap">
    <el-skeleton :rows="12" animated />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useHead } from '@vueuse/head'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { getArticleBySlug, recordView, likeArticle, getArticleNeighbors } from '@/api/article'
import { getMe } from '@/api/auth'
import { formatDateTime } from '@/utils/format'
import CommentSection from '@/components/front/CommentSection.vue'
import { useWordCount } from '@/composables/useWordCount'

const blogName = import.meta.env.VITE_BLOG_NAME || 'DevLog.'
const route = useRoute()
const article = ref(null)
const headings = ref([])
const activeId = ref('')
const liked = ref(false)
const likeCount = ref(0)
const userInfo = ref(null)
const neighbors = ref({ prev: null, next: null })

const authorName = computed(() => userInfo.value?.nickname || userInfo.value?.username || '博主')
const authorChar = computed(() => authorName.value.charAt(0))
const authorBio = computed(() => userInfo.value?.bio || '专注于 Spring Boot、Vue 3 与云原生架构，记录技术成长历程。')
const articleContent = computed(() => article.value?.content || '')
const { readingMinutes } = useWordCount(articleContent)

function parseHeadings() {
  nextTick(() => {
    const els = document.querySelectorAll('.md-editor-preview h1, .md-editor-preview h2, .md-editor-preview h3')
    headings.value = Array.from(els).map((el, i) => {
      if (!el.id) el.id = `heading-${i}`
      return { id: el.id, text: el.textContent, level: parseInt(el.tagName[1]) }
    })
    nextTick(setupObserver)
  })
}

function scrollTo(id) {
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

let observer = null
function setupObserver() {
  observer = new IntersectionObserver(
    (entries) => {
      const visible = entries.find(e => e.isIntersecting)
      if (visible) activeId.value = visible.target.id
    },
    { rootMargin: '-20% 0px -70% 0px' }
  )
  headings.value.forEach(h => {
    const el = document.getElementById(h.id)
    if (el) observer.observe(el)
  })
}

onMounted(async () => {
  try {
    const [articleRes, meRes] = await Promise.allSettled([
      getArticleBySlug(route.params.slug),
      getMe(),
    ])
    if (articleRes.status === 'fulfilled') {
      article.value = articleRes.value.data
      useHead({
        title: `${article.value.title} | ${blogName}`,
        meta: [
          { name: 'description', content: article.value.summary || article.value.title },
          { property: 'og:title', content: article.value.title },
          { property: 'og:description', content: article.value.summary || article.value.title },
          { property: 'og:image', content: article.value.coverImage || '' },
          { property: 'og:type', content: 'article' },
        ],
      })
      recordView(article.value.id)
      likeCount.value = article.value.likeCount ?? 0
      liked.value = !!localStorage.getItem(`liked_article_${article.value.id}`)
      parseHeadings()
      getArticleNeighbors(route.params.slug).then(r => { neighbors.value = r.data || { prev: null, next: null } }).catch(() => {})
    }
    if (meRes.status === 'fulfilled') {
      userInfo.value = meRes.value.data
    }
  } catch {}
})

async function handleLike() {
  if (liked.value) return
  try {
    const res = await likeArticle(article.value.id)
    likeCount.value = res.data
    liked.value = true
    localStorage.setItem(`liked_article_${article.value.id}`, '1')
  } catch {}
}

onUnmounted(() => observer?.disconnect())
</script>

<style scoped>
.post-page { display: flex; flex-direction: column; }

/* Hero */
.post-hero {
  background: linear-gradient(180deg, #111120 0%, #0C0C10 100%);
  border-bottom: 1px solid var(--color-border);
}
:root:not([data-theme='dark']) .post-hero {
  background: linear-gradient(180deg, #1A1A2E 0%, #16162A 100%);
}

.post-hero-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 48px 64px 52px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.breadcrumb { display: flex; align-items: center; gap: 8px; }
.bc-link { font-size: 13px; color: #6E6E82; text-decoration: none; transition: color var(--transition-fast); }
.bc-link:hover { color: #E8A838; }
.bc-sep { font-size: 13px; color: #3A3A5C; }

.hero-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.hero-cat {
  padding: 4px 12px;
  background: rgba(232,168,56,.15);
  border: 1px solid rgba(232,168,56,.3);
  border-radius: var(--radius-full);
  font-size: 12px; font-weight: 600; color: #E8A838;
}
.hero-tag { font-size: 13px; color: #6E6E82; }

.hero-title {
  margin: 0;
  font-size: 40px;
  font-weight: 700;
  color: #F0F0F8;
  line-height: 1.25;
  letter-spacing: -0.5px;
  max-width: 860px;
}

.hero-meta { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.hero-meta-item { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #6E6E82; }
.hero-meta-sep { color: #3A3A5C; font-size: 13px; }

/* Body */
.post-body-wrap { background: var(--color-bg); }

.post-body-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 48px 64px;
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

/* Article content */
.post-content {
  flex: 1;
  min-width: 0;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 40px;
}

.cover-wrap { margin: 0 -40px 32px; overflow: hidden; }
.cover-img { width: 100%; max-height: 420px; object-fit: cover; }

.markdown-body :deep(.md-editor-preview) {
  background: transparent;
  color: var(--color-text-primary);
  font-size: 16px;
  line-height: 1.85;
}
.markdown-body :deep(.md-editor-preview h1),
.markdown-body :deep(.md-editor-preview h2),
.markdown-body :deep(.md-editor-preview h3),
.markdown-body :deep(.md-editor-preview h4) {
  color: var(--color-text-primary);
  font-weight: 700;
  margin-top: 2rem;
}
.markdown-body :deep(.md-editor-preview p) {
  color: var(--color-text-secondary);
  margin-bottom: 1rem;
}
.markdown-body :deep(.md-editor-preview code) {
  background: var(--color-bg-secondary);
  color: #E8A838;
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  font-family: var(--font-mono);
  font-size: 0.875em;
}
.markdown-body :deep(.md-editor-preview pre) {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}
.markdown-body :deep(.md-editor-preview blockquote) {
  border-left: 3px solid #E8A838;
  background: rgba(232,168,56,.05);
  color: var(--color-text-secondary);
  margin: 1rem 0;
  padding: 0.75rem 1.25rem;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
}

.post-footer { margin-top: 32px; display: flex; flex-direction: column; gap: 20px; }

.tag-list { display: flex; flex-wrap: wrap; gap: 8px; }
.post-tag {
  padding: 4px 12px;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  font-size: 13px;
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: background var(--transition-fast), color var(--transition-fast), border-color var(--transition-fast);
}
.post-tag:hover { background: rgba(232,168,56,.1); color: #E8A838; border-color: rgba(232,168,56,.3); }

.like-area { display: flex; justify-content: center; }
.like-btn {
  display: inline-flex; align-items: center; gap: 10px;
  padding: 12px 32px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-full);
  background: transparent;
  color: var(--color-text-tertiary);
  font-size: 15px;
  cursor: pointer;
  transition: all var(--transition-fast);
}
.like-btn:hover { border-color: #E85A4F; color: #E85A4F; }
.like-btn.liked { border-color: #E85A4F; color: #E85A4F; background: rgba(232,90,79,.06); cursor: default; }

/* Sidebar */
.post-sidebar {
  width: 280px;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--header-height) + 24px);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-widget {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 20px;
}

.widget-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 14px;
}

.toc { display: flex; flex-direction: column; gap: 2px; }
.toc-item {
  display: block;
  font-size: 13px;
  color: var(--color-text-secondary);
  text-decoration: none;
  padding: 5px 8px;
  border-radius: var(--radius-sm);
  border-left: 2px solid transparent;
  line-height: 1.5;
  transition: color var(--transition-fast), background var(--transition-fast), border-color var(--transition-fast);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.toc-item:hover { color: #E8A838; background: rgba(232,168,56,.08); }
.toc-item.active { color: #E8A838; border-left-color: #E8A838; background: rgba(232,168,56,.08); font-weight: 600; }
.toc-h2 { padding-left: 16px; }
.toc-h3 { padding-left: 28px; font-size: 12px; }

/* Author Card */
.author-card { display: flex; flex-direction: column; align-items: center; gap: 12px; text-align: center; }
.author-avatar {
  width: 60px; height: 60px; border-radius: 50%;
  background: #E8A838; display: flex; align-items: center; justify-content: center;
  font-size: 22px; font-weight: 700; color: #0C0C10;
}
.author-name { font-size: 16px; font-weight: 700; color: var(--color-text-primary); }
.author-bio { margin: 0; font-size: 12px; color: var(--color-text-tertiary); line-height: 1.6; }
.author-stats { display: flex; align-items: center; width: 100%; border-top: 1px solid var(--color-border); padding-top: 12px; justify-content: center; }
.author-stat { display: flex; flex-direction: column; align-items: center; gap: 2px; flex: 1; }
.as-num { font-size: 18px; font-weight: 700; color: #E8A838; }
.as-lab { font-size: 11px; color: var(--color-text-tertiary); }
.as-sep { width: 1px; height: 32px; background: var(--color-border); }

.loading-wrap {
  max-width: 740px;
  margin: 40px auto;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 40px;
}

/* Prev / Next Navigation */
.post-nav {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin: 32px 0 8px;
}
.nav-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px 20px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  text-decoration: none;
  transition: border-color var(--transition-fast), background var(--transition-fast);
}
.nav-item:hover { border-color: var(--color-primary); background: rgba(232,168,56,.06); }
.nav-empty { pointer-events: none; background: transparent; border-color: transparent; }
.nav-prev { align-items: flex-start; }
.nav-next { align-items: flex-end; text-align: right; }
.nav-dir { font-size: 12px; color: var(--color-primary); font-weight: 600; letter-spacing: 0.5px; }
.nav-title {
  font-size: 14px;
  color: var(--color-text-primary);
  font-weight: 500;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
