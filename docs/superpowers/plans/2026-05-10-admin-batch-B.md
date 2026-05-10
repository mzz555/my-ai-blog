# 后台 B 批 · 整体视觉与列表抽象 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 用 1 天时间把后台暗/亮主题统一到 Soft Modern 设计语言，新增 AdminPageCard + DataTable 两个组件，5 个 list view 接入新骨架。

**Architecture:** 设计 token 先行（修 tokens.css 让 sidebar 与 main 同色、header 透明 blur），然后改 AdminLayout/AdminSidebar 应用新 token，再新建两个抽象组件，最后 5 个 view 渐进接入。Article 删除流程顺手统一为 ElMessageBox.confirm；Category/Tag 是卡片网格而非 table，仅用 AdminPageCard 包外壳；其他用 popconfirm 的视图保留 popconfirm。

**Tech Stack:** Vue 3.4 / Element Plus 2.6 / 现有 tokens.css 双主题 / Vite 5

**Spec 来源：** `docs/superpowers/specs/2026-05-10-admin-batch-B-design.md`

**前置：** 当前分支 `feat/admin-batch-B-ui-polish`，A 批 12 commits 已包含。

---

## 文件结构总览

| 文件 | 操作 | 责任 |
|------|------|------|
| `blog-frontend/src/styles/tokens.css` | 改 | sidebar 与 main 同色、header transparent、新增 card-surface/card-border/radius-card/shadow-card-elevate |
| `blog-frontend/src/layouts/AdminLayout.vue` | 改 | header backdrop-blur，main bg 同色 |
| `blog-frontend/src/components/admin/AdminSidebar.vue` | 改 | 跟随 token 自动更新（仅微调 active 态高光） |
| `blog-frontend/src/components/admin/AdminPageCard.vue` | 新建 | 通用页面卡片壳 |
| `blog-frontend/src/components/admin/DataTable.vue` | 新建 | 通用表格 + 分页封装 |
| `blog-frontend/src/views/admin/ArticleListView.vue` | 改 | 接入 AdminPageCard + DataTable + ElMessageBox |
| `blog-frontend/src/views/admin/CommentManageView.vue` | 改 | 接入 AdminPageCard + DataTable，保留 status-tabs/popconfirm/详情 dialog |
| `blog-frontend/src/views/admin/UserManageView.vue` | 改 | 接入 AdminPageCard + DataTable |
| `blog-frontend/src/views/admin/CategoryManageView.vue` | 改 | 接入 AdminPageCard（外壳），内部保留卡片网格 |
| `blog-frontend/src/views/admin/TagManageView.vue` | 改 | 接入 AdminPageCard（外壳），内部保留标签云 |

---

## Task 1: tokens.css 设计 token 调整

**Files:**
- Modify: `blog-frontend/src/styles/tokens.css`

**Why:** 当前 sidebar 与 main 不同色（断层来源），header 是半透明纯色块（不够柔和）。改 token 一处生效全局。

- [ ] **Step 1.1: 修改 light theme 的 sidebar-bg 与 header-bg**

打开 `blog-frontend/src/styles/tokens.css`，找到 `:root {` 块（约第 4 行起）。

把：
```css
  --color-header-bg:          rgba(255, 255, 255, 0.95);
  --color-header-border:      #E5E7EB;
  --color-sidebar-bg:         #FFFFFF;
```
改为：
```css
  --color-header-bg:          transparent;
  --color-header-blur:        10px;
  --color-header-border:      transparent;
  --color-sidebar-bg:         #F7F7F2;
```

- [ ] **Step 1.2: 在 light theme 末尾新增 card token**

在 `:root` 块的 `--prose-max-width: 780px;` 这一行（约第 75 行）之前，追加：
```css

  /* ── Soft Modern card (B 批新增) ── */
  --color-card-surface:       #FFFFFF;
  --color-card-border:        rgba(0,0,0,.06);
  --radius-card:              14px;
  --shadow-card-elevate:      0 8px 32px rgba(0,0,0,.06);
```

- [ ] **Step 1.3: 修改 dark theme 的 sidebar-bg 与 header-bg**

找到 `html[data-theme='dark'] {` 块（约第 79 行起）。

把：
```css
  --color-header-bg:          rgba(12, 12, 16, 0.95);
  --color-header-border:      #1C1C2C;
  --color-sidebar-bg:         #111119;
```
改为：
```css
  --color-header-bg:          transparent;
  --color-header-blur:        10px;
  --color-header-border:      transparent;
  --color-sidebar-bg:         #0C0C10;
```

- [ ] **Step 1.4: 在 dark theme 末尾新增 card token**

在 dark theme 块内，找到现有 `--shadow-card-hover:0 8px 32px rgba(0,0,0,.6);` 这一行（约第 162 行）之后，追加：
```css

  /* ── Soft Modern card (B 批新增) ── */
  --color-card-surface:       rgba(255,255,255,.025);
  --color-card-border:        rgba(255,255,255,.06);
  --radius-card:              14px;
  --shadow-card-elevate:      0 8px 32px rgba(0,0,0,.25);
```

- [ ] **Step 1.5: 验证 vite build**

```powershell
cd blog-frontend
npx vite build
```
预期：`✓ built in ...`，无 CSS 解析错误。

- [ ] **Step 1.6: 提交**

```bash
git add blog-frontend/src/styles/tokens.css
git commit -m "feat(frontend): tokens.css 加 card 系列 token，sidebar/header 同色"
```

---

## Task 2: AdminLayout 应用新 token

**Files:**
- Modify: `blog-frontend/src/layouts/AdminLayout.vue`

- [ ] **Step 2.1: 修改 .admin-header 样式**

打开 `blog-frontend/src/layouts/AdminLayout.vue`，找到 `<style scoped>` 中的 `.admin-header` 块（约第 128 行）。

把：
```css
.admin-header {
  height: var(--header-height);
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-5);
  flex-shrink: 0;
  transition: background var(--transition-base), border-color var(--transition-base);
}
```
改为：
```css
.admin-header {
  height: var(--header-height);
  background: var(--color-header-bg);
  backdrop-filter: blur(var(--color-header-blur));
  -webkit-backdrop-filter: blur(var(--color-header-blur));
  border-bottom: 1px solid var(--color-header-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-5);
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 10;
  transition: background var(--transition-base), border-color var(--transition-base);
}
```

- [ ] **Step 2.2: 修改 .admin-layout 与 .admin-main 背景色，统一为 --color-bg**

找到 `.admin-layout` 块（约第 104 行）：

把：
```css
.admin-layout {
  display: flex;
  height: 100vh;
  background: var(--color-bg-tertiary);
  overflow: hidden;
  transition: background var(--transition-base);
}
```
改为：
```css
.admin-layout {
  display: flex;
  height: 100vh;
  background: var(--color-bg);
  overflow: hidden;
  transition: background var(--transition-base);
}
```

找到 `.admin-main` 块（约第 261 行）：

把：
```css
.admin-main {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-6);
  background: var(--color-bg-tertiary);
  transition: background var(--transition-base);
}
```
改为：
```css
.admin-main {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-5) var(--space-6) var(--space-6);
  background: var(--color-bg);
  transition: background var(--transition-base);
}
```

- [ ] **Step 2.3: 验证 vite build**

```powershell
cd blog-frontend
npx vite build
```
预期：BUILD SUCCESS。

- [ ] **Step 2.4: 提交**

```bash
git add blog-frontend/src/layouts/AdminLayout.vue
git commit -m "feat(frontend): AdminLayout header 透明毛玻璃 + main 同色"
```

---

## Task 3: AdminSidebar 微调

**Files:**
- Modify: `blog-frontend/src/components/admin/AdminSidebar.vue`

**Why:** Sidebar 大部分由 token 控制，此 task 仅小幅微调让 sidebar/main 同色后视觉更柔和。

- [ ] **Step 3.1: .sidebar-wrap 增强 hairline border 视觉**

打开 `blog-frontend/src/components/admin/AdminSidebar.vue`，找到 `.sidebar-wrap` 块（约第 49 行）。

把：
```css
.sidebar-wrap {
  height: 100%;
  background: var(--color-sidebar-bg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-right: 1px solid var(--color-sidebar-border);
  transition: background var(--transition-base), border-color var(--transition-base);
}
```
改为：
```css
.sidebar-wrap {
  height: 100%;
  background: var(--color-sidebar-bg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-right: 1px solid var(--color-card-border);
  transition: background var(--transition-base), border-color var(--transition-base);
}
```

把 `.sidebar-logo` 块的 `border-bottom: 1px solid var(--color-sidebar-border);`（约第 64 行）改为：
```css
  border-bottom: 1px solid var(--color-card-border);
```

- [ ] **Step 3.2: 验证 vite build + 启 dev server 看效果**

```powershell
cd blog-frontend
npx vite build
```
预期：BUILD SUCCESS。

启动 dev：
```powershell
cd blog-frontend
npm run dev
```
浏览器访问 `http://localhost:5173/admin`，确认：
- Sidebar 与 main 区颜色一致，仅 1px hairline border 区隔
- Header 透明，可看到下方 main 隐约透出
- 暗/亮主题切换都正常

(dev server 可保留运行，后续 task 会复用)

- [ ] **Step 3.3: 提交**

```bash
git add blog-frontend/src/components/admin/AdminSidebar.vue
git commit -m "feat(frontend): AdminSidebar border 改 card-border，与新 token 一致"
```

---

## Task 4: 新建 AdminPageCard 组件

**Files:**
- Create: `blog-frontend/src/components/admin/AdminPageCard.vue`

- [ ] **Step 4.1: 创建组件文件**

新建 `blog-frontend/src/components/admin/AdminPageCard.vue`，内容：
```vue
<template>
  <div class="admin-page-card">
    <header class="apc-head">
      <div class="apc-title-block">
        <h2 class="apc-title">{{ title }}</h2>
        <p v-if="subtitle" class="apc-subtitle">{{ subtitle }}</p>
      </div>
      <div v-if="$slots.actions" class="apc-actions">
        <slot name="actions" />
      </div>
    </header>

    <div v-if="$slots.tabs" class="apc-tabs">
      <slot name="tabs" />
    </div>

    <div v-if="$slots.filter" class="apc-filter">
      <slot name="filter" />
    </div>

    <div class="apc-divider" />

    <div class="apc-body">
      <slot />
    </div>

    <footer v-if="$slots.footer" class="apc-footer">
      <slot name="footer" />
    </footer>
  </div>
</template>

<script setup>
defineProps({
  title:    { type: String, default: '' },
  subtitle: { type: String, default: '' },
})
</script>

<style scoped>
.admin-page-card {
  background: linear-gradient(180deg, var(--color-card-surface) 0%, transparent 100%);
  border: 1px solid var(--color-card-border);
  border-radius: var(--radius-card);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  box-shadow: var(--shadow-card-elevate);
  padding: 22px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition: background var(--transition-base), border-color var(--transition-base);
}

.apc-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
}

.apc-title-block { min-width: 0; }

.apc-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: -0.01em;
}

.apc-subtitle {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.apc-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.apc-tabs {
  display: flex;
  align-items: center;
  gap: 8px;
}

.apc-filter {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.apc-divider {
  height: 1px;
  background: var(--color-card-border);
  margin: 0 -22px;
}

.apc-body {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.apc-footer {
  padding-top: 12px;
  border-top: 1px solid var(--color-card-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
```

- [ ] **Step 4.2: 验证 vite build**

```powershell
cd blog-frontend
npx vite build
```
预期：BUILD SUCCESS。

- [ ] **Step 4.3: 提交**

```bash
git add blog-frontend/src/components/admin/AdminPageCard.vue
git commit -m "feat(frontend): 新增 AdminPageCard 通用页面卡片壳"
```

---

## Task 5: 新建 DataTable 组件

**Files:**
- Create: `blog-frontend/src/components/admin/DataTable.vue`

- [ ] **Step 5.1: 创建组件文件**

新建 `blog-frontend/src/components/admin/DataTable.vue`，内容：
```vue
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

    <div v-if="total > 0" class="dt-footer">
      <span class="dt-total">共 {{ total }} 条</span>
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

<script setup>
const props = defineProps({
  data:      { type: Array,   default: () => [] },
  loading:   { type: Boolean, default: false },
  total:     { type: Number,  default: 0 },
  page:      { type: Number,  default: 1 },
  pageSize:  { type: Number,  default: 10 },
  rowKey:    { type: String,  default: 'id' },
  emptyText: { type: String,  default: '暂无数据' },
})

const emit = defineEmits(['update:page', 'page-change'])

function handlePageChange(p) {
  emit('update:page', p)
  emit('page-change', p)
}
</script>

<style scoped>
.data-table-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.data-table {
  background: transparent;
}
.data-table :deep(.el-table__inner-wrapper)::before { background: transparent; }
.data-table :deep(.el-table__cell) {
  background: transparent !important;
  border-bottom: 1px solid var(--color-card-border) !important;
}
.data-table :deep(thead .el-table__cell) {
  background: transparent !important;
  font-weight: 600;
  color: var(--color-text-tertiary);
  font-size: 11px;
  letter-spacing: .04em;
  text-transform: uppercase;
}
.data-table :deep(.el-table__row:hover > td) {
  background: var(--color-card-border) !important;
}

.dt-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--color-card-border);
}

.dt-total {
  font-size: 12px;
  color: var(--color-text-tertiary);
}
</style>
```

- [ ] **Step 5.2: 验证 vite build**

```powershell
cd blog-frontend
npx vite build
```
预期：BUILD SUCCESS。

- [ ] **Step 5.3: 提交**

```bash
git add blog-frontend/src/components/admin/DataTable.vue
git commit -m "feat(frontend): 新增 DataTable 通用表格 + 分页封装"
```

---

## Task 6: ArticleListView 接入新骨架（试点）

**Files:**
- Modify: `blog-frontend/src/views/admin/ArticleListView.vue`

- [ ] **Step 6.1: script setup 加 import**

打开 `blog-frontend/src/views/admin/ArticleListView.vue`，找到 `<script setup>` 顶部的 import 块。

在末尾追加（保留现有 import 不变）：
```js
import AdminPageCard from '@/components/admin/AdminPageCard.vue'
import DataTable from '@/components/admin/DataTable.vue'
```

- [ ] **Step 6.2: 改写模板（用 AdminPageCard + DataTable）**

把整个 `<template>` 块（第 1 行至 `</template>`，约 174 行）替换为：
```vue
<template>
  <AdminPageCard
    title="文章管理"
    :subtitle="`共 ${total} 篇文章`"
  >
    <template #actions>
      <el-button type="primary" @click="$router.push('/admin/articles/new')">
        <el-icon><Plus /></el-icon> 写新文章
      </el-button>
    </template>

    <template #filter>
      <el-input
        v-model="keyword"
        placeholder="搜索文章标题…"
        clearable
        class="filter-input"
        @keyup.enter="search"
        @clear="search"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>

      <el-select v-model="categoryFilter" placeholder="全部分类" clearable class="filter-select" @change="search">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>

      <el-select v-model="tagFilter" placeholder="全部标签" clearable class="filter-select" @change="search">
        <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>

      <el-select v-model="statusFilter" placeholder="全部状态" clearable class="filter-select-sm" @change="search">
        <el-option label="已发布" value="PUBLISHED" />
        <el-option label="草稿" value="DRAFT" />
      </el-select>

      <el-button @click="search">搜索</el-button>
      <el-button v-if="hasFilter" @click="resetFilter">重置</el-button>
    </template>

    <DataTable
      :data="articles"
      :loading="loading"
      :total="total"
      :page="page"
      :page-size="pageSize"
      @page-change="loadArticles"
    >
      <el-table-column label="#" width="56" align="center">
        <template #default="{ $index }">
          <span class="seq-num">{{ (page - 1) * pageSize + $index + 1 }}</span>
        </template>
      </el-table-column>

      <el-table-column label="封面" width="88">
        <template #default="{ row }">
          <div class="cover-cell">
            <img v-if="row.coverImage" :src="row.coverImage" class="cover-thumb" />
            <div v-else class="cover-placeholder"><el-icon><Picture /></el-icon></div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="文章" min-width="240">
        <template #default="{ row }">
          <div class="title-block">
            <router-link :to="`/admin/articles/${row.id}/edit`" class="title-link">
              {{ row.title }}
            </router-link>
            <p v-if="row.summary" class="summary-text">{{ row.summary }}</p>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="分类" width="110">
        <template #default="{ row }">
          <span
            v-if="row.categoryName"
            class="cat-chip cat-chip--link"
            @click="jumpFilter('category', row.categoryId)"
          >{{ row.categoryName }}</span>
          <span v-else class="none-text">—</span>
        </template>
      </el-table-column>

      <el-table-column label="标签" min-width="130">
        <template #default="{ row }">
          <div class="tag-wrap">
            <span
              v-for="tag in (row.tagNames || []).slice(0, 3)"
              :key="tag"
              class="tag-chip"
            >#{{ tag }}</span>
            <span v-if="(row.tagNames || []).length > 3" class="tag-more">+{{ row.tagNames.length - 3 }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="86" align="center">
        <template #default="{ row }">
          <button
            class="status-dot"
            :class="[
              row.status === 'PUBLISHED' ? 'status-dot--pub' : 'status-dot--draft',
              { 'status-dot--clickable': canToggle }
            ]"
            :disabled="!canToggle || togglingId === row.id"
            :title="canToggle ? (row.status === 'PUBLISHED' ? '点击撤回为草稿' : '点击发布') : '无发布权限'"
            @click="handleToggle(row)"
          >
            {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
          </button>
        </template>
      </el-table-column>

      <el-table-column prop="viewCount" label="阅读" width="66" align="center">
        <template #default="{ row }">
          <span class="num-text">{{ row.viewCount ?? 0 }}</span>
        </template>
      </el-table-column>

      <el-table-column label="发布时间" width="108">
        <template #default="{ row }">
          <span class="date-text">{{ row.publishedAt ? formatDate(row.publishedAt) : '—' }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <div class="act-group">
            <button class="act-btn act-btn--edit" @click="$router.push(`/admin/articles/${row.id}/edit`)">
              <el-icon><Edit /></el-icon> 编辑
            </button>
            <button class="act-btn act-btn--del" @click="confirmDelete(row)">
              <el-icon><Delete /></el-icon> 删除
            </button>
          </div>
        </template>
      </el-table-column>
    </DataTable>
  </AdminPageCard>
</template>
```

注意改动要点：
- 整个 `<div class="page-wrap">` 被 `<AdminPageCard>` 替代
- `.page-head` / `.filter-bar` / `<el-pagination>` / 删除弹窗 4 段都消失
- `<el-table>` 改用 `<DataTable>` 包裹（columns 不变）
- 删除按钮 onclick 从 `openDelete(row)` 改为 `confirmDelete(row)`（下一步定义）

- [ ] **Step 6.3: script 改 delete 流程为 ElMessageBox.confirm**

在 `<script setup>` 块里：

把现有 import：
```js
import { ElMessage } from 'element-plus'
```
改为：
```js
import { ElMessage, ElMessageBox } from 'element-plus'
```

删除现有 state（约第 201-203 行）：
```js
const deleteDialogVisible = ref(false)
const deleteTarget = ref(null)
const deleting = ref(false)
```

删除现有的 `function openDelete(row)` 和 `async function confirmDelete()`（约第 245-261 行整段），替换为：
```js
async function confirmDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除「${row.title}」？此操作不可撤销。`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger',
      }
    )
  } catch { return }
  try {
    await deleteArticle(row.id)
    ElMessage.success('文章已删除')
    loadArticles(page.value)
  } catch {
    /* request.js 全局拦截已弹错 */
  }
}
```

- [ ] **Step 6.4: 删掉 style 中已废弃的样式**

打开 `<style scoped>` 块，**删除**以下样式段（它们的 DOM 已不存在）：
- `.page-wrap`、`.page-head`、`.page-title`、`.page-sub` 块
- `.filter-bar`、`.filter-input`、`.filter-select`、`.filter-select-sm` 块
- `.pagination-wrap` 块
- `.delete-dialog`、`.delete-body`、`.delete-icon-wrap`、`.delete-icon`、`.delete-msg`、`.delete-title-preview`、`.delete-hint`、`.delete-footer` 全部块（删除弹窗样式）
- 文末的 `:deep(.el-table__row) { height: 68px; }` 改为 `:deep(.data-table .el-table__row) { height: 68px; }`（限定到 DataTable 内）
- `:deep(.el-dialog__header) { padding: 16px 20px; }`、`:deep(.el-dialog__body) { padding: 20px; }`、`:deep(.el-dialog__footer) { padding: 14px 20px; }` 三行删除（不再有自管理 dialog）

**保留**以下样式（仍被 DOM 使用）：
- `.article-table` 改名为 `.data-table` 不需要——DataTable 自带样式，删除 `.article-table` 块
- `.seq-num`、`.cover-cell`、`.cover-thumb`、`.cover-placeholder` 保留
- `.title-block`、`.title-link`、`.summary-text` 保留
- `.cat-chip`、`.cat-chip--link`、`.none-text` 保留
- `.tag-wrap`、`.tag-chip`、`.tag-more` 保留
- `.status-dot`、`.status-dot--pub`、`.status-dot--draft`、`.status-dot--clickable` 保留（A 批新加的）
- `.num-text`、`.date-text` 保留
- `.act-group`、`.act-btn`、`.act-btn--edit`、`.act-btn--del` 保留

新增 filter 选择器宽度（filter slot 内的 input/select 用）：
```css
.filter-input { width: 220px; }
.filter-select { width: 130px; }
.filter-select-sm { width: 110px; }
```

- [ ] **Step 6.5: 验证 vite build**

```powershell
cd blog-frontend
npx vite build
```
预期：BUILD SUCCESS。

- [ ] **Step 6.6: 浏览器手测（关键试点验证）**

刷新 `http://localhost:5173/admin/articles`，确认：
1. 整页包在一张柔光卡片内，sidebar/main 同色
2. 头部"文章管理 + 共 N 篇"显示
3. 右上有"+ 写新文章"按钮
4. 筛选行（搜索/分类/标签/状态/搜索/重置）在卡片内
5. 表格行 hover 有微妙背景 + 行高 68px
6. 分页"共 N 条 + 翻页器"
7. 点删除 → 弹 ElMessageBox 确认 → 取消无变化 / 确认删除并刷新
8. 状态徽章可点切换（A 批功能仍工作）
9. 切换暗/亮主题，视觉都正常

如有视觉走样（如 filter input 宽度异常、DataTable header 错位），先在 ArticleListView 内 patch，不去改 AdminPageCard/DataTable 通用组件。

- [ ] **Step 6.7: 提交**

```bash
git add blog-frontend/src/views/admin/ArticleListView.vue
git commit -m "feat(frontend): ArticleListView 接入 AdminPageCard + DataTable + ElMessageBox"
```

---

## Task 7: CommentManageView 接入

**Files:**
- Modify: `blog-frontend/src/views/admin/CommentManageView.vue`

**Why:** Comment 是真正的 table，可用 DataTable 替换。保留 status-tabs（接 #tabs slot）+ popconfirm（小动作仍合理）+ 详情 dialog。

- [ ] **Step 7.1: script setup 加 import**

在 `<script setup>` 顶部 import 块末尾追加：
```js
import AdminPageCard from '@/components/admin/AdminPageCard.vue'
import DataTable from '@/components/admin/DataTable.vue'
```

- [ ] **Step 7.2: 改写模板顶层骨架（保留所有内部内容不变）**

把当前 `<template>` 中：
```vue
<template>
  <div class="page-wrap">
    <div class="page-head">
      <div>
        <h2 class="page-title">评论审核</h2>
        <p class="page-sub">共 {{ total }} 条评论</p>
      </div>
    </div>

    <div class="status-tabs">
      <button
        v-for="tab in statusTabs"
        :key="tab.value"
        :class="['tab-btn', { active: statusFilter === tab.value }]"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
        <span v-if="tab.value === 'PENDING' && pendingCount > 0" class="tab-badge">{{ pendingCount }}</span>
      </button>
    </div>

    <el-table :data="comments" v-loading="loading" class="data-table">
```
改为：
```vue
<template>
  <AdminPageCard title="评论审核" :subtitle="`共 ${total} 条评论`">
    <template #tabs>
      <div class="status-tabs">
        <button
          v-for="tab in statusTabs"
          :key="tab.value"
          :class="['tab-btn', { active: statusFilter === tab.value }]"
          @click="switchTab(tab.value)"
        >
          {{ tab.label }}
          <span v-if="tab.value === 'PENDING' && pendingCount > 0" class="tab-badge">{{ pendingCount }}</span>
        </button>
      </div>
    </template>

    <DataTable
      :data="comments"
      :loading="loading"
      :total="total"
      :page="page"
      :page-size="pageSize"
      @page-change="load"
    >
```

把 `</el-table>` 之后的：
```vue
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="load"
      />
    </div>

    <!-- 评论详情弹窗 -->
    <el-dialog
```
改为：
```vue
    </DataTable>

    <!-- 评论详情弹窗 -->
    <el-dialog
```

最后把模板末尾的 `</el-dialog>` 后面的 `</div>`（即 `.page-wrap` 闭合）改为：
```vue
  </AdminPageCard>
</template>
```

注意：所有 `<el-table-column>` 不动；详情 dialog 不动；popconfirm 不动。

- [ ] **Step 7.3: 删 style 中已废弃样式**

在 `<style scoped>` 中**删除**：
- `.page-wrap`、`.page-head`、`.page-title`、`.page-sub` 块
- `.data-table { border-radius: var(--radius-lg); overflow: hidden; }` 这一行（被 DataTable 内部接管）
- `.pagination-wrap` 块

**保留**所有其他样式（status-tabs / table cell / detail-dialog 等），且把 `:deep(.el-table__row) { height: 60px; }` 改为 `:deep(.data-table .el-table__row) { height: 60px; }`。

- [ ] **Step 7.4: 验证 vite build**

```powershell
cd blog-frontend
npx vite build
```
预期：BUILD SUCCESS。

- [ ] **Step 7.5: 浏览器手测**

访问 `/admin/comments`：
1. 卡片骨架 + status-tabs 在卡片内
2. table 显示数据 + hover 行
3. 分页正常
4. 通过/拒绝/删除/查看详情都工作
5. 暗/亮主题切换无走样

- [ ] **Step 7.6: 提交**

```bash
git add blog-frontend/src/views/admin/CommentManageView.vue
git commit -m "feat(frontend): CommentManageView 接入 AdminPageCard + DataTable"
```

---

## Task 8: UserManageView 接入

**Files:**
- Modify: `blog-frontend/src/views/admin/UserManageView.vue`

- [ ] **Step 8.1: script setup 加 import**

在 import 块末尾追加：
```js
import AdminPageCard from '@/components/admin/AdminPageCard.vue'
import DataTable from '@/components/admin/DataTable.vue'
```

- [ ] **Step 8.2: 改写模板顶层骨架**

把当前模板顶部：
```vue
<template>
  <div class="page-wrap">
    <div class="page-head">
      <div>
        <h2 class="page-title">用户管理</h2>
        <p class="page-sub">共 {{ total }} 位用户</p>
      </div>
      <div class="head-right">
        <el-input
          v-model="keyword"
          placeholder="搜索用户名 / 昵称"
          clearable
          class="search-input"
          @input="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon> 新建用户
        </el-button>
      </div>
    </div>

    <el-table :data="users" v-loading="loading" class="data-table">
```
改为：
```vue
<template>
  <AdminPageCard title="用户管理" :subtitle="`共 ${total} 位用户`">
    <template #actions>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon> 新建用户
      </el-button>
    </template>

    <template #filter>
      <el-input
        v-model="keyword"
        placeholder="搜索用户名 / 昵称"
        clearable
        class="search-input"
        @input="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </template>

    <DataTable
      :data="users"
      :loading="loading"
      :total="total"
      :page="page"
      :page-size="pageSize"
      @page-change="fetchUsers"
    >
```

把 `</el-table>` 之后的：
```vue
    </el-table>

    <div class="pagination-wrap">
      <el-pagination ... />
    </div>

    <el-dialog v-model="createVisible" title="新建用户" ...>
```
改为：
```vue
    </DataTable>

    <el-dialog v-model="createVisible" title="新建用户" ...>
```

最后把模板末尾两个 `</el-dialog>` 之后的 `</div>`（即 `.page-wrap` 闭合）改为：
```vue
  </AdminPageCard>
</template>
```

- [ ] **Step 8.3: 删 style 中已废弃样式**

**删除**：
- `.page-wrap`、`.page-head`、`.page-title`、`.page-sub` 块
- `.data-table { border-radius: var(--radius-lg); overflow: hidden; }`
- `.pagination-wrap` 块
- `.head-right { display: flex; align-items: center; gap: 12px; }`（已不存在）

**修改**：
- 把 `:deep(.el-table__row) { height: 60px; }` 改为 `:deep(.data-table .el-table__row) { height: 60px; }`

- [ ] **Step 8.4: 验证 vite build + 浏览器手测**

```powershell
cd blog-frontend
npx vite build
```

访问 `/admin/users`：
1. 卡片骨架 + 右上"新建用户"按钮
2. filter 区有搜索框
3. table 数据 + hover 行
4. 分页 + 状态 switch + 编辑/新建对话框都工作

- [ ] **Step 8.5: 提交**

```bash
git add blog-frontend/src/views/admin/UserManageView.vue
git commit -m "feat(frontend): UserManageView 接入 AdminPageCard + DataTable"
```

---

## Task 9: CategoryManageView 接入（外壳，保留卡片网格）

**Files:**
- Modify: `blog-frontend/src/views/admin/CategoryManageView.vue`

**Why:** Category 内容是 3 列卡片网格，不适合改 table。仅用 AdminPageCard 包外壳，内部 cat-grid 不变。

- [ ] **Step 9.1: script setup 加 import**

在 `<script setup>` 顶部 import 块末尾追加：
```js
import AdminPageCard from '@/components/admin/AdminPageCard.vue'
```

- [ ] **Step 9.2: 改写模板顶层骨架**

把当前模板顶部：
```vue
<template>
  <div class="page-wrap">
    <div class="page-head">
      <div>
        <h2 class="page-title">分类管理</h2>
        <p class="page-sub">共 {{ categories.length }} 个分类</p>
      </div>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> 新建分类
      </el-button>
    </div>

    <!-- 路由标签 -->
    <div class="route-tabs">
      <router-link to="/admin/categories" class="rtab rtab--active">分类管理</router-link>
      <router-link to="/admin/tags" class="rtab">标签管理</router-link>
    </div>

    <!-- 卡片网格 -->
    <div v-loading="loading" class="cat-grid">
```
改为：
```vue
<template>
  <AdminPageCard title="分类管理" :subtitle="`共 ${categories.length} 个分类`">
    <template #actions>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> 新建分类
      </el-button>
    </template>

    <template #tabs>
      <div class="route-tabs">
        <router-link to="/admin/categories" class="rtab rtab--active">分类管理</router-link>
        <router-link to="/admin/tags" class="rtab">标签管理</router-link>
      </div>
    </template>

    <div v-loading="loading" class="cat-grid">
```

把模板末尾：
```vue
    <el-empty v-if="!loading && !categories.length" description="暂无分类" />

    <el-dialog ...>
```
改为：
```vue
    <el-empty v-if="!loading && !categories.length" description="暂无分类" />

    <el-dialog ...>
```
（这块不变；只是不再被 `.page-wrap` 包裹）

最后把模板末尾 `</el-dialog>` 之后的 `</div>`（`.page-wrap` 闭合）改为：
```vue
  </AdminPageCard>
</template>
```

- [ ] **Step 9.3: 删 style 中已废弃样式**

**删除**：
- `.page-wrap`、`.page-head`、`.page-title`、`.page-sub` 块

保留所有其他样式（route-tabs / cat-grid / cat-card 等）。

- [ ] **Step 9.4: 验证 vite build + 浏览器手测**

```powershell
cd blog-frontend
npx vite build
```

访问 `/admin/categories`：
1. 卡片外壳 + 顶部 tabs 在卡片内
2. 内部 3 列卡片网格保留
3. 编辑/删除/popconfirm 都工作
4. 暗/亮主题正常

- [ ] **Step 9.5: 提交**

```bash
git add blog-frontend/src/views/admin/CategoryManageView.vue
git commit -m "feat(frontend): CategoryManageView 接入 AdminPageCard 外壳"
```

---

## Task 10: TagManageView 接入（外壳，保留标签云）

**Files:**
- Modify: `blog-frontend/src/views/admin/TagManageView.vue`

**Why:** 同 Category，标签云不适合改 table。

- [ ] **Step 10.1: script setup 加 import**

在 `<script setup>` 顶部 import 块末尾追加：
```js
import AdminPageCard from '@/components/admin/AdminPageCard.vue'
```

- [ ] **Step 10.2: 改写模板顶层骨架**

把当前模板顶部：
```vue
<template>
  <div class="page-wrap">
    <div class="page-head">
      <div>
        <h2 class="page-title">标签管理</h2>
        <p class="page-sub">共 {{ tags.length }} 个标签</p>
      </div>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> 新建标签
      </el-button>
    </div>

    <!-- 路由标签 -->
    <div class="route-tabs">
      <router-link to="/admin/categories" class="rtab">分类管理</router-link>
      <router-link to="/admin/tags" class="rtab rtab--active">标签管理</router-link>
    </div>

    <div v-if="tags.length" class="tag-grid">
```
改为：
```vue
<template>
  <AdminPageCard title="标签管理" :subtitle="`共 ${tags.length} 个标签`">
    <template #actions>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> 新建标签
      </el-button>
    </template>

    <template #tabs>
      <div class="route-tabs">
        <router-link to="/admin/categories" class="rtab">分类管理</router-link>
        <router-link to="/admin/tags" class="rtab rtab--active">标签管理</router-link>
      </div>
    </template>

    <div v-if="tags.length" class="tag-grid">
```

把模板末尾：
```vue
    <el-empty v-else description="暂无标签" />

    <el-dialog ...>
```
保持不变。

把末尾 `</el-dialog>` 之后的 `</div>` 改为：
```vue
  </AdminPageCard>
</template>
```

- [ ] **Step 10.3: 删 style 中已废弃样式**

**删除**：
- `.page-wrap`、`.page-head`、`.page-title`、`.page-sub` 块

- [ ] **Step 10.4: 验证 vite build + 浏览器手测**

```powershell
cd blog-frontend
npx vite build
```

访问 `/admin/tags`：
1. 卡片外壳 + tabs 在内
2. 标签云保留
3. 创建/编辑/删除 popconfirm 都工作

- [ ] **Step 10.5: 提交**

```bash
git add blog-frontend/src/views/admin/TagManageView.vue
git commit -m "feat(frontend): TagManageView 接入 AdminPageCard 外壳"
```

---

## Task 11: 跨页面手测 + 视觉细节修补

**Files:** 视手测发现而定，可能 0-3 个文件

- [ ] **Step 11.1: 全量手测 9 项**

确保 dev server 在跑（`npm run dev`），逐项验证：

| # | 操作 | 预期 |
|---|------|------|
| 1 | 暗色主题进 `/admin/articles` | sidebar/main 同色，header 半透明，卡片柔光阴影 |
| 2 | 切换浅色 | 同结构、白底卡片、无对比度问题 |
| 3 | 滚动文章列表 | header 维持半透明覆盖 |
| 4 | 点删除文章 | ElMessageBox 弹出，确认/取消都正确 |
| 5 | `/admin/comments` | 卡片骨架 + status-tabs + popconfirm 删除 |
| 6 | `/admin/categories` | 卡片骨架 + route-tabs + 卡片网格保留 |
| 7 | `/admin/tags` | 卡片骨架 + route-tabs + 标签云保留 |
| 8 | `/admin/users` | 卡片骨架 + 搜索 + 新建/编辑对话框 |
| 9 | A 批新功能：封面裁剪 + 文章状态点击切换 | 在新视觉下都正常 |

- [ ] **Step 11.2: 视觉走样 patch（如有）**

常见问题与修法：

**问题 A**：DataTable 的 el-pagination 在亮色模式下样式与暗色不一致
- 位置：`blog-frontend/src/components/admin/DataTable.vue` 的 `<style scoped>`
- 修法：通过 `:deep(.el-pagination)` 覆盖

**问题 B**：filter slot 内 input/select 与 AdminPageCard 间距太挤
- 位置：使用方 view 的 `<style scoped>` 微调
- 修法：在 view 内加 `.filter-input { width: 220px; }` 等

**问题 C**：明色模式 sidebar 文字偏淡
- 位置：`blog-frontend/src/components/admin/AdminSidebar.vue` `.nav-item` 样式
- 修法：检查 `--color-sidebar-text` 在亮模式的对比度

如手测无走样跳过此步。

- [ ] **Step 11.3: 提交（如有 patch）**

```bash
git status
# 若 step 11.2 有改动
git add <changed-files>
git commit -m "fix(frontend): B 批 视觉细节修补"
```
若无改动则跳过。

---

## Task 12: 完工汇报

**Files:** 无新文件

- [ ] **Step 12.1: 跑前端全量 build 兜底**

```powershell
cd blog-frontend
npx vite build
```
预期：BUILD SUCCESS，无 warning。

- [ ] **Step 12.2: 检查 git 树**

```bash
git status
git log --oneline master..HEAD
```
预期：
- 无未提交改动（除 .idea/.claude 之类的本地文件）
- 列出 B 批所有 commits

- [ ] **Step 12.3: 汇报**

向用户报告：
- 完成的 8 个文件改动（含新建 2 个组件）
- 所有 5 个 list view 已接入新骨架
- 9 项手测项的通过情况
- 暗/亮主题验证情况
- 已知未触及但待办的项（spec §10 不在范围列表）

---

## 实施完成后的下一步

B 批完成后可考虑：
- 把 A 批 + B 批一起合并到 master
- 启动原本被推延的"批量操作"批次（spec 中名为 B 批，可改名 C 批）
- 为 Dashboard 视图加同样的卡片骨架（独立小批）
