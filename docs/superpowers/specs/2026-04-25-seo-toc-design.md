# SEO Meta Tags + 文章目录（TOC）设计文档

**日期：** 2026-04-25  
**范围：** Plan A — 读者体验提升（计划 B 为作者体验，独立实现）  
**Tech Stack：** Vue 3.4 / @vueuse/head / md-editor-v3

---

## 目标

1. 为文章详情页及主要前台页面注入正确的 `<title>` / `<meta description>` / Open Graph 标签，提升搜索引擎可见性和社交分享效果。
2. 在文章详情页右侧提供自动生成的目录（TOC），支持锚点跳转和滚动高亮。

---

## 架构概览

```
blog-frontend/
  src/
    main.js                          ← 注册 createHead()
    components/front/
      ArticleToc.vue                 ← 新建：TOC 组件
    views/front/
      PostDetailView.vue             ← 修改：添加 useHead + 布局改为正文+TOC
      PostListView.vue               ← 修改：添加 useHead
      HomeView.vue                   ← 修改：添加 useHead
      ArchiveView.vue                ← 修改：添加 useHead
      CategoryView.vue               ← 修改：添加 useHead
      TagView.vue                    ← 修改：添加 useHead
```

---

## 功能一：SEO Meta Tags

### 依赖安装

```bash
npm install @vueuse/head
```

### main.js 配置

```js
import { createHead } from '@vueuse/head'
const head = createHead()
app.use(head)
```

### 各页面 useHead 规格

#### PostDetailView（最高优先级）

```js
const blogName = import.meta.env.VITE_BLOG_NAME || 'DevLog.'

useHead({
  title: () => article.value ? `${article.value.title} | ${blogName}` : blogName,
  meta: [
    {
      name: 'description',
      content: () => {
        if (!article.value) return ''
        return article.value.summary ||
          (article.value.content || '').replace(/[#*`>\[\]]/g, '').slice(0, 150)
      }
    },
    { property: 'og:title',       content: () => article.value?.title || '' },
    { property: 'og:description', content: () => article.value?.summary || '' },
    { property: 'og:type',        content: 'article' },
    { property: 'og:image',       content: () => article.value?.coverImage || '' },
  ],
})
```

> **注意：** 所有 meta 均使用响应式 getter，随 `article` 加载自动更新。`og:image` 无封面时返回空字符串，主流 og 解析器会忽略空 image 值。

#### 其他页面（固定或半动态）

| 页面 | title | description |
|------|-------|-------------|
| HomeView | `${blogName} \| 首页` | `记录技术成长，分享工程实践` |
| PostListView | `文章列表 \| ${blogName}` | `探索 Spring Boot、Vue 3、云原生与工程实践` |
| ArchiveView | `归档 \| ${blogName}` | — |
| CategoryView | `${categoryName} \| ${blogName}` | — |
| TagView | `#${tagName} \| ${blogName}` | — |

---

## 功能二：文章目录（TOC）

### ArticleToc.vue 组件

**Props：**
```ts
content: String  // 文章 markdown 原文
```

**解析规则：**
- 正则：`/^#{2,3}\s+(.+)/gm`（匹配行首 `##` 和 `###`，排除代码块内的注释）
- h1 不纳入 TOC（文章本身已是标题）
- h4/h5/h6 不纳入 TOC
- 锚点生成：`text.toLowerCase().replace(/\s+/g, '-').replace(/[^\w一-龥-]/g, '')` — 与 md-editor-v3 自动生成的 heading ID 对齐（实现后需实测验证，若不一致则微调正则）

**渲染规则：**
- `toc.length < 2` 时：组件返回空（`v-if`），不占空间
- h2 → 正常缩进，h3 → 增加 `padding-left: 12px`
- `activeAnchor` ref：通过 `IntersectionObserver` 追踪当前可见 heading

**IntersectionObserver 策略：**
- `rootMargin: '-20% 0px -70% 0px'`（视口上方 20% 到下方 70% 区间内认为 active）
- 降级：若浏览器不支持 `IntersectionObserver`，TOC 仍渲染，点击跳转正常，仅无高亮

**交互：**
- 点击 TOC 项：`document.getElementById(anchor)?.scrollIntoView({ behavior: 'smooth' })`
- 激活项：amber 色（`#E8A838`），左侧 2px 竖线
- 非激活：`#6E6E82`，hover 变 `#F0F0F8`

### PostDetailView 布局变更

**现有结构：**
```
.post-body-inner（max-width: 800px, 单列）
  └── article.post-content
```

**变更后：**
```
.post-body-wrap
  └── .post-body-inner（max-width: 1100px, display: flex, gap: 48px）
       ├── article.post-content（flex: 1, min-width: 0）
       └── aside.toc-sidebar（width: 220px, flex-shrink: 0）
            └── <ArticleToc :content="article.content" />
```

**响应式：**
```css
@media (max-width: 1200px) {
  .toc-sidebar { display: none; }
}
```

**TOC sticky 定位：**
```css
.toc-sidebar {
  position: sticky;
  top: 96px;            /* 导航栏 72px + 24px 间距 */
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}
```

---

## 不在本计划内

- Canonical URL（SPA 路由已唯一）
- h4/h5/h6 heading
- 其他页面的 og:image
- 服务端渲染（SSR / Nuxt）
- 移动端 TOC 抽屉（屏宽 < 1200px 直接隐藏）

---

## 验证方式

1. **SEO：** 浏览器 DevTools → Elements → `<head>` 确认 `<title>` 和各 `<meta>` 正确注入，切换不同文章页确认动态更新
2. **OG 预览：** 在 [opengraph.xyz](https://www.opengraph.xyz) 输入文章 URL 验证社交分享卡片
3. **TOC 出现：** 发布含 3+ 个 `##` 标题的文章，确认右侧 TOC 渲染正确
4. **TOC 高亮：** 滚动文章，确认 active 条目随视口变化更新
5. **TOC 隐藏：** 缩小浏览器窗口至 1200px 以下，确认 TOC 消失
6. **无标题文章：** 发布无 `##` 标题文章，确认 TOC 不出现

---

## 文件变更清单

### 新建
- `blog-frontend/src/components/front/ArticleToc.vue`

### 修改
- `blog-frontend/package.json`（添加 @vueuse/head 依赖）
- `blog-frontend/src/main.js`（注册 createHead）
- `blog-frontend/src/views/front/PostDetailView.vue`（useHead + 布局 + ArticleToc）
- `blog-frontend/src/views/front/PostListView.vue`（useHead）
- `blog-frontend/src/views/front/HomeView.vue`（useHead）
- `blog-frontend/src/views/front/ArchiveView.vue`（useHead）
- `blog-frontend/src/views/front/CategoryView.vue`（useHead）
- `blog-frontend/src/views/front/TagView.vue`（useHead）
