# 文章列表页侧边筛选栏设计文档

**日期：** 2026-04-26
**范围：** PostListView 改造 — 右侧侧边栏（分类 + 标签过滤）
**Tech Stack：** Vue 3.4 / Element Plus / 现有后端 API

---

## 目标

将 `/posts` 文章列表页从「顶部分类按钮栏 + 全宽网格」改造为「左侧文章网格 + 右侧筛选侧边栏」，读者可按分类或标签过滤文章，两个维度互斥单选。

---

## 架构概览

**仅修改一个文件：**

```
blog-frontend/src/views/front/PostListView.vue   ← 布局 + 侧边栏 + API 修复
```

无需新建文件，无需改后端。

---

## 一、页面布局

### 结构

```
┌─ Page Hero（通栏）────────────────────────────────────┐
│  ARTICLES / 所有文章 / 探索 Spring Boot、Vue 3…        │
└───────────────────────────────────────────────────────┘
┌─── 文章区 flex-1 ──────────────────┬── 侧边栏 220px ──┐
│                                    │                  │
│  ┌──────────┐  ┌──────────┐        │  CATEGORIES      │
│  │ 文章卡片 │  │ 文章卡片 │        │  ● 全部          │
│  └──────────┘  └──────────┘        │  ○ 后端开发  32  │
│  ┌──────────┐  ┌──────────┐        │  ○ 前端工程  18  │
│  │ 文章卡片 │  │ 文章卡片 │        │  ○ 云原生    11  │
│  └──────────┘  └──────────┘        │                  │
│                                    │  TAGS            │
│  [ 分页 ]                          │  #Vue  #Java     │
│                                    │  #Spring …       │
└────────────────────────────────────┴──────────────────┘
```

### 宽度规格

| 区域 | 宽度 | 说明 |
|------|------|------|
| 侧边栏 | `220px` 固定 | 右侧，左边有分隔线 |
| 文章区 | `flex: 1` | 左侧，撑满剩余空间 |
| 文章网格 | **2 列** | 加了侧边栏后宽度缩小，3 列过挤 |

### 响应式

- `≤ 768px`：侧边栏移到文章列表上方，直接展开显示（无折叠交互）
- `> 768px`：标准双栏布局

### 移除

- 删除现有 `.filter-bar`（顶部分类 pill 按钮栏）及其全部 CSS

---

## 二、侧边栏内容与交互

### 分类区（CATEGORIES）

- 固定第一项「全部」（selectedCategory = null）
- 下方列出所有分类：`分类名 + 文章数`，右对齐数字
- 选中态：左侧彩色圆点（`var(--color-primary)`）+ 文字亮白
- 未选态：文字 `var(--color-text-secondary)`，数字 `var(--color-text-tertiary)`
- 点击已选分类 → 回到「全部」

### 标签区（TAGS）

- 所有标签以 pill 展示，按 `articleCount` 降序排列
- pill 样式：`border: 1px solid var(--color-border)`，`border-radius: 20px`
- 选中态：border + 文字 = `var(--color-primary)`
- 未选态：文字 `var(--color-text-secondary)`

### 互斥规则

| 操作 | 效果 |
|------|------|
| 点击分类 | 设置 selectedCategoryId，清空 selectedTagSlug |
| 点击标签 | 设置 selectedTagSlug，清空 selectedCategoryId，分类回到「全部」 |
| 点击已选项 | 清空该维度，回到「全部」 |

---

## 三、数据与 API

### Bug 修复（同步修复）

现有 PostListView 传 `categorySlug` 给后端，但后端接收的是 `categoryId`（Long），导致分类过滤实际失效。本次修复：

```js
// 修复前（无效）
params.categorySlug = selectedCategory.value

// 修复后
if (selectedCategoryId.value) params.categoryId = selectedCategoryId.value
if (selectedTagSlug.value)    params.tagSlug    = selectedTagSlug.value
```

### 页面挂载

```js
const [articlesRes, categoriesRes, tagsRes] = await Promise.all([
  getArticles({ page: 1, size: pageSize }),
  getCategories(),
  getTags(),
])
```

### 过滤触发

点击分类或标签后，重置到第 1 页并重新请求文章：

```js
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
```

### 标签数据

`getTags()` 已有，返回 `[{ id, name, slug, articleCount }]`，前端按 `articleCount` 降序排列后填入侧边栏。

---

## 四、响应式方案

```css
/* 默认：双栏 */
.content-wrap {
  display: flex;
  gap: 0;
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 40px 64px 64px;
}

.article-area { flex: 1; min-width: 0; }
.sidebar { width: 220px; flex-shrink: 0; }

/* ≤ 768px：单栏，侧边栏在上 */
@media (max-width: 768px) {
  .content-wrap { flex-direction: column-reverse; }
  .sidebar { width: 100%; border-left: none; border-bottom: 1px solid var(--color-border); }
}
```

---

## 验证方式

1. 进入 `/posts`，确认右侧侧边栏显示分类和标签
2. 点击分类 → 文章列表更新，标签选中态清空
3. 点击标签 → 文章列表更新，分类回到「全部」
4. 再次点击已选项 → 回到全部文章
5. 缩小窗口到手机尺寸，确认侧边栏在文章上方展示
6. 确认原顶部分类过滤栏不再显示
