<template>
  <div v-if="article" class="post-page">
    <!-- Hero Banner -->
    <section class="post-hero">
      <div class="post-hero-inner">
        <!-- 眉毛行：分类 + 阅读时间 -->
        <div class="hero-eyebrow">
          <router-link v-if="article.categoryName" :to="`/category/${article.categoryName}`" class="hero-cat">
            {{ article.categoryName }}
          </router-link>
          <span v-if="readingMinutes" class="hero-eyebrow-time">约 {{ readingMinutes }} 分钟阅读</span>
        </div>

        <!-- 标题 -->
        <h1 class="hero-title">{{ article.title }}</h1>

        <!-- 底部：日期 · 阅读量 · 标签 -->
        <div class="hero-foot">
          <span class="hero-foot-item">{{ formatDateTime(article.publishedAt) }}</span>
          <span class="hero-foot-sep">·</span>
          <span class="hero-foot-item">
            <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="3"/><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
            </svg>
            {{ article.viewCount }} 阅读
          </span>
          <template v-if="article.tagNames?.length">
            <span class="hero-foot-sep">·</span>
            <div class="hero-tags">
              <span v-for="tag in article.tagNames.slice(0,4)" :key="tag" class="hero-tag">#{{ tag }}</span>
            </div>
          </template>
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
            <MdPreview :modelValue="article.content" :theme="appStore.darkMode ? 'dark' : 'light'" />
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
          <div v-if="headings.length" class="sidebar-widget toc-widget">
            <div class="widget-label">目录</div>
            <nav class="toc" ref="tocNav">
              <a v-for="h in headings" :key="h.id"
                :href="`#${h.id}`"
                :class="['toc-item', `toc-h${h.level}`, { active: activeId === h.id }]"
                @click.prevent="scrollTo(h.id)">
                {{ h.text }}
              </a>
            </nav>
          </div>

          <!-- Categories -->
          <div v-if="allCategories.length" class="sidebar-widget">
            <div class="widget-label">分类</div>
            <div class="sidebar-cats">
              <router-link
                v-for="cat in allCategories"
                :key="cat.id"
                :to="`/category/${cat.slug || cat.name}`"
                :class="['cat-link', { active: article.categoryName === cat.name }]"
              >
                <span class="cat-dot"></span>
                <span class="cat-name">{{ cat.name }}</span>
                <span v-if="cat.articleCount" class="cat-count">{{ cat.articleCount }}</span>
              </router-link>
            </div>
          </div>

          <!-- Tags -->
          <div v-if="allTags.length" class="sidebar-widget">
            <div class="widget-label">标签</div>
            <div class="sidebar-tags">
              <router-link
                v-for="tag in allTags"
                :key="tag.id"
                :to="`/tag/${tag.name}`"
                :class="['tag-chip', { active: article.tagNames?.includes(tag.name) }]"
              >
                #{{ tag.name }}
              </router-link>
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
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useHead } from '@vueuse/head'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { getArticleBySlug, recordView, likeArticle, getArticleNeighbors } from '@/api/article'
import { getMe } from '@/api/auth'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import { formatDateTime } from '@/utils/format'
import CommentSection from '@/components/front/CommentSection.vue'
import { useWordCount } from '@/composables/useWordCount'
import { useAppStore } from '@/stores/app'

const blogName = import.meta.env.VITE_BLOG_NAME || 'DevLog.'
const route = useRoute()
const appStore = useAppStore()
const article = ref(null)
const headings = ref([])
const activeId = ref('')
const liked = ref(false)
const likeCount = ref(0)
const userInfo = ref(null)
const neighbors = ref({ prev: null, next: null })
const allCategories = ref([])
const allTags = ref([])
const tocNav = ref(null)

watch(activeId, (id) => {
  if (!id || !tocNav.value) return
  nextTick(() => {
    const el = tocNav.value.querySelector('.toc-item.active')
    if (el) el.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
  })
})

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
    nextTick(setupScrollHighlight)
  })
}


function scrollTo(id) {
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

let scrollCleanup = null
function setupScrollHighlight() {
  const onScroll = () => {
    if (!headings.value.length) return
    const threshold = window.innerHeight * 0.25
    let current = headings.value[0].id
    for (const h of headings.value) {
      const el = document.getElementById(h.id)
      if (el && el.getBoundingClientRect().top <= threshold) {
        current = h.id
      }
    }
    activeId.value = current
  }
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
  scrollCleanup = () => window.removeEventListener('scroll', onScroll)
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
    getCategories().then(r => { allCategories.value = r.data || [] }).catch(() => {})
    getTags().then(r => { allTags.value = r.data || [] }).catch(() => {})
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

onUnmounted(() => scrollCleanup?.())
</script>

<style scoped>
.post-page { display: flex; flex-direction: column; }

/* Hero */
.post-hero {
  background: linear-gradient(180deg, #111120 0%, #0C0C10 100%);
  border-bottom: 1px solid var(--color-border);
}
:root:not([data-theme='dark']) .post-hero {
  background: linear-gradient(170deg, rgba(232,168,56,0.06) 0%, var(--color-bg) 100%);
}

.post-hero-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 52px 64px 56px;
  display: flex;
  flex-direction: column;
}

/* Eyebrow: category chip + reading time */
.hero-eyebrow {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
}
.hero-cat {
  display: inline-flex;
  align-items: center;
  padding: 4px 14px;
  background: rgba(232,168,56,.14);
  border: 1px solid rgba(232,168,56,.32);
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  color: #E8A838;
  text-decoration: none;
  transition: background var(--transition-fast), border-color var(--transition-fast);
}
.hero-cat:hover { background: rgba(232,168,56,.22); border-color: rgba(232,168,56,.5); }
.hero-eyebrow-time {
  font-size: 13px;
  color: var(--color-text-tertiary);
}

/* Title */
.hero-title {
  margin: 0 0 28px;
  font-size: 44px;
  font-weight: 800;
  color: var(--color-text-primary);
  line-height: 1.2;
  letter-spacing: -0.8px;
  max-width: 860px;
}

/* Footer meta row */
.hero-foot {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  padding-top: 20px;
  border-top: 1px solid rgba(255,255,255,0.06);
}
:root:not([data-theme='dark']) .hero-foot {
  border-top-color: var(--color-border);
}
.hero-foot-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--color-text-tertiary);
}
.hero-foot-sep { font-size: 13px; color: var(--color-text-tertiary); opacity: 0.5; }
.hero-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.hero-tag { font-size: 12px; color: var(--color-text-tertiary); }

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

/* MD wrapper backgrounds */
.markdown-body :deep(#md-editor-v3),
.markdown-body :deep(.md-editor-preview-wrapper) {
  background: transparent !important;
}
.markdown-body :deep(.md-editor-preview) {
  background: transparent;
  color: var(--color-text-primary);
  font-size: 16px;
  line-height: 1.9;
}

/* Headings */
.markdown-body :deep(.md-editor-preview h1) {
  color: var(--color-text-primary);
  font-size: 28px;
  font-weight: 700;
  margin-top: 2.5rem;
  margin-bottom: 1rem;
  letter-spacing: -0.3px;
  line-height: 1.3;
}
.markdown-body :deep(.md-editor-preview h2) {
  color: var(--color-text-primary);
  font-size: 22px;
  font-weight: 700;
  margin-top: 2.25rem;
  margin-bottom: 0.75rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--color-border);
  letter-spacing: -0.2px;
  line-height: 1.35;
}
.markdown-body :deep(.md-editor-preview h3) {
  color: var(--color-text-primary);
  font-size: 18px;
  font-weight: 600;
  margin-top: 1.75rem;
  margin-bottom: 0.5rem;
  line-height: 1.4;
}
.markdown-body :deep(.md-editor-preview h4) {
  color: var(--color-text-primary);
  font-size: 16px;
  font-weight: 600;
  margin-top: 1.5rem;
  margin-bottom: 0.4rem;
}

/* Paragraph */
.markdown-body :deep(.md-editor-preview p) {
  color: var(--color-text-secondary);
  margin-bottom: 1.25rem;
  line-height: 1.9;
}

/* Lists */
.markdown-body :deep(.md-editor-preview ul),
.markdown-body :deep(.md-editor-preview ol) {
  margin: 0.5rem 0 1.25rem;
  padding-left: 1.75rem;
}
.markdown-body :deep(.md-editor-preview li) {
  color: var(--color-text-secondary);
  line-height: 1.8;
  margin-bottom: 0.35rem;
}
.markdown-body :deep(.md-editor-preview li::marker) {
  color: var(--color-accent);
}

/* Inline code */
.markdown-body :deep(.md-editor-preview code) {
  background: #1e1e2e;
  color: #E8A838;
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  font-family: var(--font-mono);
  font-size: 0.875em;
}

/* Code block: outer container (md-editor-v3 generates .md-editor-code) */
.markdown-body :deep(.md-editor-preview .md-editor-code) {
  border: 1px solid #2d2d3f !important;
  border-radius: var(--radius-md) !important;
  overflow: hidden;
  margin: 1.5rem 0;
  font-size: 14px;
}
/* Head toolbar — always dark, keep md-editor-v3's built-in dots + copy btn */
.markdown-body :deep(.md-editor-preview .md-editor-code .md-editor-code-head) {
  background: #252540 !important;
  border-bottom: 1px solid #2d2d3f;
  height: 36px;
}
/* Copy button color */
.markdown-body :deep(.md-editor-preview .md-editor-code .md-editor-copy-button) {
  color: rgba(200,200,220,0.5) !important;
  transition: color 0.18s;
}
.markdown-body :deep(.md-editor-preview .md-editor-code .md-editor-copy-button:hover) {
  color: #E8A838 !important;
}
/* Language label */
.markdown-body :deep(.md-editor-preview .md-editor-code .md-editor-code-lang) {
  color: rgba(232,168,56,0.5) !important;
  font-family: var(--font-mono);
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 1.5px;
}
/* Pre background */
.markdown-body :deep(.md-editor-preview .md-editor-code pre) {
  background: #1e1e2e !important;
  margin: 0 !important;
  tab-size: 2;
  -moz-tab-size: 2;
}
/* Code content */
.markdown-body :deep(.md-editor-preview .md-editor-code pre code) {
  background: #1e1e2e !important;
  color: #e6e6e6 !important;
  font-size: 14px !important;
  font-family: var(--font-mono) !important;
  line-height: 1.6 !important;
  padding: 1.25rem 1.5rem !important;
  white-space: pre;
  display: block;
  border-radius: 0 !important;
}
/* Row number alignment: match rn-wrapper top to padding-top of code */
.markdown-body :deep(.md-editor-preview .md-editor-code pre code span[rn-wrapper]) {
  top: 1.25rem;
}
/* When row numbers are on: .md-editor-scrn is added to .md-editor-preview (not to pre).
   Restore left padding so code text doesn't overlap the 3em-wide rn-wrapper. */
.markdown-body :deep(.md-editor-preview.md-editor-scrn .md-editor-code pre code) {
  padding-left: 4em !important;
}
/* Scrollbar */
.markdown-body :deep(.md-editor-preview .md-editor-code pre code::-webkit-scrollbar) { height: 5px; }
.markdown-body :deep(.md-editor-preview .md-editor-code pre code::-webkit-scrollbar-track) {
  background: rgba(255,255,255,0.04);
}
.markdown-body :deep(.md-editor-preview .md-editor-code pre code::-webkit-scrollbar-thumb) {
  background: rgba(232,168,56,0.25);
  border-radius: 3px;
}
.markdown-body :deep(.md-editor-preview .md-editor-code pre code::-webkit-scrollbar-thumb:hover) {
  background: rgba(232,168,56,0.45);
}

/* Links */
.markdown-body :deep(.md-editor-preview a) {
  color: var(--color-accent);
  text-decoration: underline;
  text-underline-offset: 3px;
  text-decoration-color: rgba(232,168,56,0.4);
  transition: text-decoration-color var(--transition-fast);
}
.markdown-body :deep(.md-editor-preview a:hover) {
  text-decoration-color: var(--color-accent);
}

/* Blockquote */
.markdown-body :deep(.md-editor-preview blockquote) {
  border-left: 3px solid #E8A838;
  background: rgba(232,168,56,.06);
  color: var(--color-text-secondary);
  margin: 1.5rem 0;
  padding: 0.875rem 1.25rem;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  font-style: italic;
}
.markdown-body :deep(.md-editor-preview blockquote p) {
  margin-bottom: 0;
  color: inherit;
}

/* Images */
.markdown-body :deep(.md-editor-preview img) {
  max-width: 100%;
  border-radius: var(--radius-md);
  box-shadow: 0 4px 24px rgba(0,0,0,0.12);
  margin: 1.25rem auto;
  display: block;
}

/* HR */
.markdown-body :deep(.md-editor-preview hr) {
  border: none;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--color-border) 20%, var(--color-border) 80%, transparent);
  margin: 2rem 0;
}

/* Tables */
.markdown-body :deep(.md-editor-preview table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1.5rem 0;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  display: table;
}
.markdown-body :deep(.md-editor-preview th) {
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
  font-weight: 600;
  padding: 10px 16px;
  text-align: left;
  border-bottom: 1px solid var(--color-border);
  font-size: 13px;
}
.markdown-body :deep(.md-editor-preview td) {
  color: var(--color-text-secondary);
  padding: 10px 16px;
  border-bottom: 1px solid var(--color-border);
  font-size: 14px;
}
.markdown-body :deep(.md-editor-preview tr:last-child td) {
  border-bottom: none;
}
.markdown-body :deep(.md-editor-preview tr:hover td) {
  background: var(--color-bg-secondary);
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
  height: calc(100vh - var(--header-height) - 48px);
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow: hidden;
}

.sidebar-widget {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 18px 20px;
  flex-shrink: 0;
}

/* TOC widget: fills remaining space, TOC nav scrolls inside */
.toc-widget {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.widget-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.toc {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: var(--color-border) transparent;
}
.toc-item {
  display: block;
  font-size: 13px;
  color: var(--color-text-secondary);
  text-decoration: none;
  padding: 5px 8px;
  border-radius: var(--radius-sm);
  border-left: 2px solid transparent;
  line-height: 1.5;
  word-break: break-word;
  transition: color var(--transition-fast), background var(--transition-fast), border-color var(--transition-fast);
}
.toc-item:hover { color: #E8A838; background: rgba(232,168,56,.08); }
.toc-item.active {
  color: #E8A838;
  border-left-color: #E8A838;
  background: rgba(232,168,56,.10);
  font-weight: 600;
  border-left-width: 3px;
}
.toc-h2 { padding-left: 16px; }
.toc-h3 { padding-left: 28px; font-size: 12px; }

/* Categories sidebar */
.sidebar-cats { display: flex; flex-direction: column; gap: 2px; }
.cat-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 8px;
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: all var(--transition-fast);
}
.cat-link:hover { background: rgba(232,168,56,.08); color: #E8A838; }
.cat-link.active { color: #E8A838; background: rgba(232,168,56,.08); font-weight: 600; }
.cat-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: var(--color-border);
  flex-shrink: 0;
  transition: background var(--transition-fast);
}
.cat-link:hover .cat-dot, .cat-link.active .cat-dot { background: #E8A838; }
.cat-name { flex: 1; }
.cat-count {
  font-size: 11px;
  color: var(--color-text-tertiary);
  background: var(--color-bg-secondary);
  padding: 1px 6px;
  border-radius: var(--radius-full);
}

/* Tags sidebar */
.sidebar-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.tag-chip {
  padding: 3px 10px;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  font-size: 12px;
  color: var(--color-text-tertiary);
  text-decoration: none;
  transition: all var(--transition-fast);
}
.tag-chip:hover { background: rgba(232,168,56,.1); color: #E8A838; border-color: rgba(232,168,56,.3); }
.tag-chip.active { background: rgba(232,168,56,.1); color: #E8A838; border-color: rgba(232,168,56,.3); font-weight: 500; }


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
.nav-item:hover { border-color: var(--color-accent); background: rgba(232,168,56,.06); }
.nav-empty { pointer-events: none; background: transparent; border-color: transparent; }
.nav-prev { align-items: flex-start; }
.nav-next { align-items: flex-end; text-align: right; }
.nav-dir { font-size: 12px; color: var(--color-accent); font-weight: 600; letter-spacing: 0.5px; }
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
