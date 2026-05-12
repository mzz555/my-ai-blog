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

    <BulkActionBar :count="selectedRows.length" @cancel="clearSelection">
      <button class="bba-action bba-action--del" @click="handleBatchDelete">
        <el-icon><Delete /></el-icon> 批量删除
      </button>
    </BulkActionBar>

    <DataTable
      ref="dataTableRef"
      selectable
      :data="articles"
      :loading="loading"
      :total="total"
      :page="page"
      :page-size="pageSize"
      @page-change="loadArticles"
      @selection-change="handleSelectionChange"
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

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAdminArticles, deleteArticle, togglePublish, batchDeleteArticles } from '@/api/article'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import { formatDate } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, Picture } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import AdminPageCard from '@/components/admin/AdminPageCard.vue'
import DataTable from '@/components/admin/DataTable.vue'
import BulkActionBar from '@/components/admin/BulkActionBar.vue'

const route = useRoute()
const router = useRouter()

const articles = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)
const keyword = ref('')
const categoryFilter = ref(null)
const tagFilter = ref(null)
const statusFilter = ref('')
const categories = ref([])
const tags = ref([])

const userStore = useUserStore()
const canToggle = computed(() => userStore.hasPermission('article:publish'))
const togglingId = ref(null)

const dataTableRef = ref(null)
const selectedRows = ref([])

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function clearSelection() {
  dataTableRef.value?.clearSelection()
}

async function handleBatchDelete() {
  const count = selectedRows.value.length
  if (count === 0) return
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${count} 篇文章？此操作不可撤销。`,
      '批量删除确认',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger',
      }
    )
  } catch { return }
  try {
    const ids = selectedRows.value.map(r => r.id)
    await batchDeleteArticles({ ids })
    ElMessage.success(`已删除 ${count} 篇文章`)
    selectedRows.value = []
    loadArticles(page.value)
  } catch {
    /* request.js 全局拦截弹错 */
  }
}

const hasFilter = computed(() =>
  keyword.value || categoryFilter.value || tagFilter.value || statusFilter.value
)

async function loadArticles(p = 1) {
  loading.value = true
  page.value = p
  try {
    const params = { page: p, size: pageSize }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (statusFilter.value) params.status = statusFilter.value
    if (categoryFilter.value) params.categoryId = categoryFilter.value
    if (tagFilter.value) params.tagId = tagFilter.value
    const res = await getAdminArticles(params)
    articles.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function search() { loadArticles(1) }

function resetFilter() {
  keyword.value = ''
  categoryFilter.value = null
  tagFilter.value = null
  statusFilter.value = ''
  router.replace({ query: {} })
  loadArticles(1)
}

function jumpFilter(type, id) {
  if (type === 'category') {
    categoryFilter.value = id
    tagFilter.value = null
  }
  loadArticles(1)
}

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

async function handleToggle(row) {
  if (!canToggle.value) return
  if (row.status === 'PUBLISHED') {
    try {
      await ElMessageBox.confirm(
        `确认将文章「${row.title}」撤回为草稿？前台将不再可见。`,
        '撤回文章',
        { type: 'warning', confirmButtonText: '确认撤回', cancelButtonText: '取消' }
      )
    } catch {
      return
    }
  }
  togglingId.value = row.id
  try {
    await togglePublish(row.id)
    row.status = row.status === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED'
    if (row.status === 'PUBLISHED' && !row.publishedAt) {
      row.publishedAt = new Date().toISOString()
    }
    ElMessage.success(row.status === 'PUBLISHED' ? '已发布' : '已撤回为草稿')
  } finally {
    togglingId.value = null
  }
}

onMounted(async () => {
  const [catRes, tagRes] = await Promise.all([getCategories(), getTags()])
  categories.value = catRes.data
  tags.value = tagRes.data

  // 支持从分类/标签管理页跳转带参数
  if (route.query.categoryId) categoryFilter.value = Number(route.query.categoryId)
  if (route.query.tagId) tagFilter.value = Number(route.query.tagId)

  loadArticles(1)
})
</script>

<style scoped>
.filter-input { width: 220px; }
.filter-select { width: 130px; }
.filter-select-sm { width: 110px; }

/* 序号 */
.seq-num { font-size: 12px; color: var(--color-text-tertiary); font-variant-numeric: tabular-nums; }

/* 封面 */
.cover-cell { width: 64px; height: 42px; }
.cover-thumb {
  width: 64px;
  height: 42px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid var(--color-border);
  display: block;
}
.cover-placeholder {
  width: 64px;
  height: 42px;
  border-radius: 4px;
  background: var(--color-bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-tertiary);
  font-size: 18px;
}

/* 标题块 */
.title-block { display: flex; flex-direction: column; gap: 4px; padding: 4px 0; }
.title-link {
  color: var(--color-text-primary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.3;
  transition: color 0.15s;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.title-link:hover { color: #E8A838; }
.summary-text {
  margin: 0;
  font-size: 12px;
  color: var(--color-text-tertiary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 分类 */
.cat-chip {
  display: inline-block;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--color-bg-secondary);
  color: var(--color-text-secondary);
  border: 1px solid var(--color-border);
  white-space: nowrap;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cat-chip--link {
  cursor: pointer;
  transition: color 0.15s, border-color 0.15s;
}
.cat-chip--link:hover {
  color: #E8A838;
  border-color: rgba(232,168,56,.4);
}
.none-text { font-size: 12px; color: var(--color-text-tertiary); }

/* 标签 */
.tag-wrap { display: flex; gap: 4px; flex-wrap: wrap; align-items: center; }
.tag-chip { font-size: 11px; color: var(--color-text-tertiary); }
.tag-more { font-size: 11px; color: var(--color-text-tertiary); }

/* 状态 */
.status-dot {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 20px;
}
.status-dot--pub   { background: rgba(34,197,94,.12);  color: #22C55E; border: 1px solid rgba(34,197,94,.25); }
.status-dot--draft { background: rgba(107,114,128,.1); color: var(--color-text-secondary); border: 1px solid var(--color-border); }

/* 行内状态切换的可点态 */
.status-dot {
  font-family: inherit;
  cursor: default;
  transition: opacity .15s, transform .15s;
}
.status-dot--clickable { cursor: pointer; }
.status-dot--clickable:hover { opacity: .8; transform: scale(1.04); }
.status-dot--clickable:disabled { cursor: wait; opacity: .6; transform: none; }

.num-text { font-size: 13px; color: var(--color-text-secondary); }
.date-text { font-size: 12px; color: var(--color-text-tertiary); }

/* 操作按钮 */
.act-group { display: flex; gap: 6px; align-items: center; }
.act-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 28px;
  padding: 0 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid;
  transition: opacity 0.15s;
  white-space: nowrap;
}
.act-btn:hover { opacity: 0.75; }
.act-btn--edit { background: var(--color-bg-secondary); border-color: var(--color-border); color: var(--color-text-secondary); }
.act-btn--del  { background: rgba(239,68,68,.08); border-color: rgba(239,68,68,.3); color: var(--color-danger); }

:deep(.data-table .el-table__row) { height: 68px; }
</style>
