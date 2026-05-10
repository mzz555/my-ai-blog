# 后台 B 批 · 整体视觉与列表抽象 设计

**日期**：2026-05-10
**范围**：后台暗/亮主题统一视觉语言 + 5 个 list 页面骨架抽象
**预估工作量**：1 天

---

## 1. 背景

A 批落地后用户反馈两条体验问题：

1. Sidebar / Header / Main 三段之间存在颜色"断层"，视觉不连贯
2. 5 个 list 页（文章/评论/分类/标签/用户）各自重复实现"页头 + 筛选 + 表格 + 分页 + 删除弹窗"，复用度低

原计划的 B 批"批量操作"推延到下一批。本批改为整体视觉优化 + 列表骨架抽象，定调后续后台所有页面的设计语言。

---

## 2. 决策记录

| # | 决策 | 选项 |
|---|------|------|
| D1 | 设计方向 | **Soft Modern**（柔和现代）—— 大圆角、毛玻璃、柔光阴影、暗色为主 |
| D2 | 卡片策略 | **单张大卡片**（Notion 风）—— 页头/筛选/表格/分页全部包在一张卡片内 |
| D3 | 列表抽象粒度 | **L2 · 壳 + DataTable** —— `<AdminPageCard>` + `<DataTable>`，filter 作 slot 保留灵活 |
| D4 | Sidebar/Main 接缝 | **同色 + 1px hairline border-right** —— 取消颜色断层 |
| D5 | Header/Main 接缝 | **透明 + backdrop-blur** —— 取消硬色块，滚动时半透明覆盖 |
| D6 | 删除弹窗 | **统一 `ElMessageBox.confirm`** —— 删除每页自写的 `<el-dialog>` 删除确认 |
| D7 | 接入范围 | 5 个核心 list view：Article / Comment / Category / Tag / User；Role/Menu 因结构特殊，本批不动 |

---

## 3. 视觉语言（Soft Modern）

### 3.1 设计 token 调整 (`blog-frontend/src/styles/tokens.css`)

**新增/调整暗色 token：**

```css
html[data-theme='dark'] {
  /* 基础底色：sidebar 与 main 同色 */
  --color-bg-primary:   #0a0a10;
  --color-bg-secondary: #14141c;
  --color-bg-tertiary:  #0a0a10;

  /* Sidebar：与 main 同色，仅 1px hairline */
  --color-sidebar-bg:     #0a0a10;
  --color-sidebar-border: rgba(255,255,255,.05);

  /* Header：透明，靠 blur 制造层级 */
  --color-header-bg:      transparent;
  --color-header-blur:    10px;

  /* 卡片表面 */
  --color-card-surface:   rgba(255,255,255,.025);
  --color-card-border:    rgba(255,255,255,.06);

  /* 圆角 */
  --radius-card: 14px;
  --radius-md:   8px;
  --radius-sm:   6px;

  /* 阴影 */
  --shadow-card:    0 8px 32px rgba(0,0,0,.25);
  --shadow-elevate: 0 4px 12px rgba(232,168,56,.25);  /* 主操作按钮 */
}
```

**亮色对应（同结构）：**

```css
html[data-theme='light'] {
  --color-bg-primary:   #FAFAF7;
  --color-bg-secondary: #FFFFFF;
  --color-bg-tertiary:  #FAFAF7;

  --color-sidebar-bg:     #FAFAF7;
  --color-sidebar-border: rgba(0,0,0,.06);

  --color-header-bg:    transparent;
  --color-header-blur:  10px;

  --color-card-surface: #FFFFFF;
  --color-card-border:  rgba(0,0,0,.06);

  --shadow-card:    0 8px 32px rgba(0,0,0,.06);
  --shadow-elevate: 0 4px 12px rgba(232,168,56,.18);
}
```

### 3.2 字体策略

**保留现有项目字体方案**，本批不引入新字体（YAGNI）。当前已用 system-ui 栈在中文环境下解析为微软雅黑/苹方/华文等，足够清晰。如未来需要 brand-aligned 字体（HarmonyOS Sans / Inter），单独一批引入。

---

## 4. AdminLayout 改动

`blog-frontend/src/layouts/AdminLayout.vue`

### 4.1 Header

```css
.admin-header {
  background: var(--color-header-bg);          /* transparent */
  backdrop-filter: blur(var(--color-header-blur));
  -webkit-backdrop-filter: blur(var(--color-header-blur));
  border-bottom: none;                          /* 取消硬色块 */
  position: sticky;
  top: 0;
  z-index: 10;
}
```

### 4.2 Main

```css
.admin-main {
  background: var(--color-bg-tertiary);        /* 与 sidebar/layout 同色 */
  padding: var(--space-5) var(--space-6) var(--space-6);
}
```

### 4.3 Layout 容器

```css
.admin-layout {
  background: var(--color-bg-tertiary);
}
```

---

## 5. AdminSidebar 改动

`blog-frontend/src/components/admin/AdminSidebar.vue`

```css
.sidebar-wrap {
  background: var(--color-sidebar-bg);          /* 与 main 同 */
  border-right: 1px solid var(--color-sidebar-border);
}

.sidebar-logo {
  border-bottom: 1px solid var(--color-sidebar-border);
}

.nav-item.active {
  /* 在 token 调整后自动跟随，无需改动 */
}
```

---

## 6. 新组件 `<AdminPageCard>`

`blog-frontend/src/components/admin/AdminPageCard.vue`（约 80 行）

### 6.1 Props

| prop | 类型 | 说明 |
|------|------|------|
| `title` | String | 页面标题（H2） |
| `subtitle` | String | 标题下方副文本（如"共 42 篇"） |

### 6.2 Slots

| slot | 用途 |
|------|------|
| `actions` | 标题右侧操作按钮（如"+ 新文章"） |
| `filter` | 筛选栏（搜索框/下拉等） |
| default | 主内容（通常是 `<DataTable>`） |
| `footer` | 卡片底部（如分页器） |

### 6.3 模板骨架

```html
<template>
  <div class="admin-page-card">
    <!-- Header -->
    <div class="apc-head">
      <div class="apc-title-block">
        <h2 class="apc-title">{{ title }}</h2>
        <p v-if="subtitle" class="apc-subtitle">{{ subtitle }}</p>
      </div>
      <div v-if="$slots.actions" class="apc-actions">
        <slot name="actions" />
      </div>
    </div>

    <!-- Hairline divider -->
    <div v-if="$slots.filter || $slots.default" class="apc-divider" />

    <!-- Filter -->
    <div v-if="$slots.filter" class="apc-filter">
      <slot name="filter" />
    </div>

    <!-- Body -->
    <div class="apc-body">
      <slot />
    </div>

    <!-- Footer (pagination etc.) -->
    <div v-if="$slots.footer" class="apc-footer">
      <slot name="footer" />
    </div>
  </div>
</template>
```

### 6.4 关键样式

```css
.admin-page-card {
  background: linear-gradient(180deg, var(--color-card-surface) 0%, transparent 100%);
  border: 1px solid var(--color-card-border);
  border-radius: var(--radius-card);
  backdrop-filter: blur(8px);
  box-shadow: var(--shadow-card);
  padding: 22px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.apc-head { display: flex; justify-content: space-between; align-items: flex-end; }
.apc-title { margin: 0; font-size: 18px; font-weight: 700; color: var(--color-text-primary); letter-spacing: -.01em; }
.apc-subtitle { margin: 3px 0 0; font-size: 12px; color: var(--color-text-tertiary); }
.apc-divider { height: 1px; background: var(--color-card-border); margin: 0 -22px; }
.apc-filter { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.apc-footer { padding-top: 10px; border-top: 1px solid var(--color-card-border); display: flex; justify-content: space-between; align-items: center; }
```

---

## 7. 新组件 `<DataTable>`

`blog-frontend/src/components/admin/DataTable.vue`（约 100 行）

### 7.1 Props

| prop | 类型 | 说明 |
|------|------|------|
| `data` | Array | 表格行数据 |
| `loading` | Boolean | loading 态 |
| `total` | Number | 总条数（分页用） |
| `page` | Number | 当前页（v-model 支持） |
| `pageSize` | Number | 每页条数（默认 10） |
| `rowKey` | String | 行 key（默认 'id'） |
| `emptyText` | String | 空状态文案（默认"暂无数据"） |

### 7.2 Emits

- `update:page` — 页码变化
- `page-change` — 同上的语义事件

### 7.3 Slots

- default — 透传给内层 `<el-table>`，每个使用方传若干 `<el-table-column>`

### 7.4 模板骨架

```html
<template>
  <div class="data-table-wrap">
    <el-table
      :data="data"
      v-loading="loading"
      :row-key="rowKey"
      class="data-table"
      :empty-text="emptyText"
    >
      <slot />
    </el-table>

    <div v-if="total > 0" class="data-table-pagination">
      <span class="pagination-total">共 {{ total }} 条</span>
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>
```

### 7.5 关键样式（去除 el-table 默认外框，靠 hairline 区分行）

```css
.data-table {
  background: transparent;
}
.data-table :deep(.el-table__inner-wrapper)::before { background: transparent; }
.data-table :deep(.el-table__cell) {
  background: transparent;
  border-bottom: 1px solid var(--color-card-border);
}
.data-table :deep(.el-table__row:hover > td) {
  background: rgba(255,255,255,.02);
}
.data-table-pagination {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--color-card-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination-total { font-size: 12px; color: var(--color-text-tertiary); }
```

---

## 8. 5 个 ListView 接入改造

每个 view 的改造模板（以 `ArticleListView` 为例）：

### 8.1 Before

```html
<div class="page-wrap">
  <div class="page-head">
    <div>...title + sub...</div>
    <el-button>...</el-button>
  </div>
  <div class="filter-bar">...</div>
  <el-table :data="articles" v-loading="loading" class="article-table">...</el-table>
  <div class="pagination-wrap"><el-pagination ... /></div>
  <el-dialog v-model="deleteDialogVisible" title="删除文章" ...>...</el-dialog>
</div>
```

### 8.2 After

```html
<AdminPageCard title="文章管理" :subtitle="`共 ${total} 篇文章`">
  <template #actions>
    <el-button type="primary" @click="$router.push('/admin/articles/new')">
      <el-icon><Plus /></el-icon> 写新文章
    </el-button>
  </template>

  <template #filter>
    <el-input v-model="keyword" placeholder="搜索文章标题…" class="filter-input" @keyup.enter="search">
      <template #prefix><el-icon><Search /></el-icon></template>
    </el-input>
    <el-select v-model="categoryFilter" placeholder="全部分类" clearable class="filter-select" @change="search">
      <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
    </el-select>
    <!-- ...其他 filter... -->
  </template>

  <DataTable
    :data="articles"
    :loading="loading"
    :total="total"
    :page="page"
    :page-size="pageSize"
    @page-change="loadArticles"
  >
    <el-table-column label="#" width="56">...</el-table-column>
    <!-- ...列定义不变... -->
  </DataTable>
</AdminPageCard>
```

### 8.3 删除流程统一

把每个 view 的 delete-dialog 整段删除，替换为：

```js
async function openDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除「${row.title}」？此操作不可撤销。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger' }
    )
  } catch { return }
  await deleteArticle(row.id)
  ElMessage.success('已删除')
  loadArticles(page.value)
}
```

每个 view 同样的改法。

### 8.4 5 个 ListView 改造清单

| view | 关键变化 |
|------|---------|
| `ArticleListView.vue` | 卡片化 + DataTable + 删除统一 |
| `CommentManageView.vue` | 卡片化 + DataTable；保留现有 approve/reject 行内按钮；详情抽屉保留 |
| `CategoryManageView.vue` | 卡片化 + DataTable + 删除统一 |
| `TagManageView.vue` | 卡片化 + DataTable + 删除统一 |
| `UserManageView.vue` | 卡片化 + DataTable；新建/编辑对话框保留（已有） |

`RoleManageView` / `MenuManageView` 因结构特殊（多 panel / 树形），本批不动。

---

## 9. 测试策略

### 9.1 自动化（前端无 vitest）

- 每次接入一个 ListView 后，跑 `npx vite build` 确保无编译错误

### 9.2 手测清单

| # | 操作 | 预期 |
|---|------|------|
| 1 | 暗色主题进 `/admin/articles` | sidebar/main 同色无断层、header 半透明、卡片柔光阴影 |
| 2 | 切换浅色主题 | 同上结构、白底卡片、不破对比度 |
| 3 | 滚动文章列表 | header 维持半透明 + 卡片内 table 滚动 |
| 4 | 点删除 | 弹 ElMessageBox 确认 |
| 5 | 进 `/admin/comments` | 同样卡片骨架 |
| 6 | 进 `/admin/categories`、`/admin/tags`、`/admin/users` | 全部 5 个 list 视觉一致 |
| 7 | sidebar 折叠/展开 | 主区动画跟随、卡片宽度自适应 |
| 8 | A 批新加的"行内状态切换" | 在新卡片骨架内仍工作 |
| 9 | A 批新加的"封面裁剪"对话框 | 在新视觉下不破样式 |

### 9.3 后端

无后端改动，无新增测试。

---

## 10. 不在范围

- ❌ 列表批量操作（推到下批）
- ❌ Dashboard 改造（不是 list 类，独立设计批次）
- ❌ Role/Menu 视图改造（结构特殊，单独评估）
- ❌ 移动端响应式（当前后台仅桌面）
- ❌ 引入新字体（HarmonyOS Sans / Inter）
- ❌ 前台页面（仅后台）

---

## 11. 文件清单

### 新建
- `blog-frontend/src/components/admin/AdminPageCard.vue`
- `blog-frontend/src/components/admin/DataTable.vue`

### 改
- `blog-frontend/src/styles/tokens.css`（颜色/圆角/阴影 token）
- `blog-frontend/src/layouts/AdminLayout.vue`（header backdrop-blur + main bg）
- `blog-frontend/src/components/admin/AdminSidebar.vue`（取消颜色差）
- `blog-frontend/src/views/admin/ArticleListView.vue`
- `blog-frontend/src/views/admin/CommentManageView.vue`
- `blog-frontend/src/views/admin/CategoryManageView.vue`
- `blog-frontend/src/views/admin/TagManageView.vue`
- `blog-frontend/src/views/admin/UserManageView.vue`

---

## 12. 实施顺序

```
1. tokens.css + AdminLayout + AdminSidebar  （视觉接缝先解决，0 业务风险）
2. AdminPageCard 组件
3. DataTable 组件
4. ArticleListView 试点接入（最熟悉的 view，验证抽象边界）
5. 其余 4 个 ListView 滚动接入
6. 5 个 view 删除流程统一为 ElMessageBox.confirm
7. 暗/亮主题验证
8. 手测清单 9 项 + 提交
```

---

## 13. 风险

| 风险 | 缓解 |
|------|------|
| `backdrop-filter` 兼容性 | Chrome 76+ / Safari 9+ / Firefox 103+ 全支持，目标用户都覆盖；旧浏览器降级为半透明背景 |
| 现有 view 用了 `<el-dialog>` 删除确认，改成 `ElMessageBox.confirm` 后 confirm 按钮样式不同 | `confirmButtonClass: 'el-button--danger'` 已加；如不一致再补 CSS |
| `DataTable` 抽象边界把握不准（如 CommentManageView 的特殊行操作） | filter 用 slot 保留灵活；列定义全部走 default slot 透传给 el-table；不去强制约束列 |
| token 改动影响 Dashboard / Profile 等非 list 页面 | 这些页面本就该跟随设计语言更新，符合预期；如个别页面明显走样，本批 patch 修一下 |
| 滚动时 header backdrop-blur 性能 | 现代 GPU 合成，不影响日常；如个例慢可降级为半透明纯色 |

---

## 14. 完工标准

- [ ] 5 个 ListView 全部用 `<AdminPageCard>` + `<DataTable>` 重写
- [ ] 5 个 ListView 删除流程统一为 `ElMessageBox.confirm`
- [ ] Sidebar / Header 接缝消失
- [ ] 暗/亮主题切换无 UI 走样
- [ ] `npx vite build` 通过
- [ ] 手测清单 9 项全部 ✓
