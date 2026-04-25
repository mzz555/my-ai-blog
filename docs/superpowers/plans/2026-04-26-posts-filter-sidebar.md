# 文章列表页右侧筛选栏 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `/posts` 文章列表页改造为「左侧文章网格（2列）+ 右侧筛选侧边栏」，支持按分类或标签单选过滤，同时修复现有 categorySlug → categoryId 的 API 参数错误。

**Architecture:** 仅修改 `PostListView.vue` 一个文件：删除顶部分类按钮栏，新增右侧 220px 侧边栏（分类列表 + 标签 pill），文章网格从 3 列改为 2 列，分类和标签互斥单选。无需新建文件，无需改后端。

**Tech Stack:** Vue 3.4 / Element Plus / `getCategories()` / `getTags()` 现有 API

**注意：** 后端 Tag 和 Category 实体均不含 `articleCount` 字段，侧边栏只显示名称不显示计数。分类按 `sortOrder` 升序显示，标签按字母升序显示。

---

## 现状说明

- `blog-frontend/src/views/front/PostListView.vue`（217 行）
  - 已有：顶部 `.filter-bar` 分类按钮栏，点击按 `categorySlug` 过滤（**但后端接收的是 `categoryId`，此处有 bug**）
  - 已有：3 列文章卡片网格（`ArticleCardGrid` 组件）
  - 已有：`getCategories()` 加载分类，`getArticles({ categorySlug })` 加载文章
  - **缺失**：标签过滤，`getTags()` 未调用

- 后端 `GET /api/articles` 参数：`categoryId`（Long）、`tagSlug`（String），两者均可选，互相独立
- `getTags()` → `GET /api/tags` → 返回 `[{ id, name, slug }]`
- `getCategories()` → `GET /api/categories` → 返回 `[{ id, name, slug, description, sortOrder }]`

---

## 文件变更

### 修改

- `blog-frontend/src/views/front/PostListView.vue`

---

## Task 1：改造 PostListView.vue

**Files:**
- Modify: `blog-frontend/src/views/front/PostListView.vue`

- [ ] **Step 1: 读取当前文件**

确认文件在 `blog-frontend/src/views/front/PostListView.vue`，阅读全文理解现有结构后再做修改。

- [ ] **Step 2: 替换 `<script setup>` 全部内容**

将 `<script setup>` 与 `</script>` 之间的所有内容替换为：

```js
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
```

说明：
- `selectedCategoryId`（Long）替代旧的 `selectedCategory`（slug 字符串），修复 API bug
- `sortedTags` computed 将标签按字母升序排列
- `onMounted` 并行发出三个请求，`loadArticles()` 的返回值丢弃（用 `[,`）因为它直接写入 `articles`

- [ ] **Step 3: 替换 `<template>` 全部内容**

将 `<template>` 与 `</template>` 之间的所有内容替换为：

```html
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
              v-for="article in articles"
              :key="article.id"
              :article="article"
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
```

- [ ] **Step 4: 替换 `<style scoped>` 全部内容**

将 `<style scoped>` 与 `</style>` 之间的所有内容替换为：

```css
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
.cat-item.active { color: var(--color-primary); }
.cat-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  border: 1.5px solid currentColor;
  flex-shrink: 0;
  transition: background 0.15s;
}
.cat-item.active .cat-dot { background: var(--color-primary); }

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
.tag-pill:hover { border-color: var(--color-primary); color: var(--color-primary); }
.tag-pill.active { border-color: var(--color-primary); color: var(--color-primary); }

/* ── 分页样式（保留原有） ── */
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
```

注意：
- 侧边栏 `position: sticky; top: 80px` 让右栏随页面滚动固定（80px 是导航栏高度）
- `@media (max-width: 768px)` 使用 `flex-direction: column-reverse` 让 sidebar（DOM 中排后）显示在上方
- `@media (max-width: 1024px)` 文章降为 1 列（侧边栏已占用宽度）

- [ ] **Step 5: 验证**

启动开发服务器：
```bash
cd blog-frontend && npm run dev
```

逐项验证：
1. 访问 `/posts`，确认右侧出现侧边栏，左侧文章 2 列网格
2. 点击分类「后端开发」→ 文章列表更新，分类高亮，标签无选中
3. 点击任意标签 pill → 文章列表更新，标签高亮，分类回到「全部」高亮
4. 再次点击同一分类/标签 → 恢复全部文章
5. 缩小窗口到 768px 以下，确认侧边栏在文章上方展示、无左边框
6. 确认旧的顶部分类按钮栏不再显示

- [ ] **Step 6: 提交**

```bash
git add blog-frontend/src/views/front/PostListView.vue
git commit -m "feat: 文章列表页新增右侧筛选侧边栏（分类+标签互斥过滤），修复 categorySlug→categoryId bug"
```

---

## 自检清单

完成后逐项验证：

- [ ] 右侧侧边栏正常显示，分类和标签列表有数据
- [ ] 点击分类过滤文章，分类高亮，标签选中状态清空
- [ ] 点击标签过滤文章，标签高亮，分类回到「全部」
- [ ] 再次点击已选项回到全部文章
- [ ] 旧顶部 filter-bar 不再显示
- [ ] 手机视图侧边栏在文章上方
- [ ] 翻页时过滤条件保持
