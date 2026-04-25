<template>
  <div class="page-wrap">
    <!-- 页头 -->
    <div class="page-head">
      <div>
        <h2 class="page-title">文章管理</h2>
        <p class="page-sub">共 {{ total }} 篇文章</p>
      </div>
      <el-button type="primary" @click="$router.push('/admin/articles/new')">
        <el-icon><Plus /></el-icon> 写新文章
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
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

      <el-select v-model="statusFilter" placeholder="全部状态" clearable class="filter-select" @change="search">
        <el-option label="已发布" value="PUBLISHED" />
        <el-option label="草稿" value="DRAFT" />
      </el-select>

      <el-button @click="search">搜索</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="articles" v-loading="loading" class="article-table">
      <el-table-column prop="title" label="标题" min-width="240">
        <template #default="{ row }">
          <router-link :to="`/admin/articles/${row.id}/edit`" class="title-link">
            {{ row.title }}
          </router-link>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="100">
        <template #default="{ row }">
          <span class="cat-badge">{{ row.categoryName || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="阅读" width="80" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <span :class="['status-dot', row.status === 'PUBLISHED' ? 'status-dot--pub' : 'status-dot--draft']">
            {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="130">
        <template #default="{ row }">
          <span class="date-text">{{ row.publishedAt ? formatDate(row.publishedAt) : '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="190" fixed="right">
        <template #default="{ row }">
          <div class="act-group">
            <button class="act-btn act-btn--edit" @click="$router.push(`/admin/articles/${row.id}/edit`)">
              <el-icon><Edit /></el-icon> 编辑
            </button>
            <el-popconfirm title="确认删除该文章？" @confirm="handleDelete(row.id)">
              <template #reference>
                <button class="act-btn act-btn--del">
                  <el-icon><Delete /></el-icon> 删除
                </button>
              </template>
            </el-popconfirm>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="loadArticles"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminArticles, togglePublish, deleteArticle } from '@/api/article'
import { formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'
import { Plus, Search, Edit, Delete, View, Hide } from '@element-plus/icons-vue'

const articles = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)
const keyword = ref('')
const statusFilter = ref('')

async function loadArticles(p = 1) {
  loading.value = true
  page.value = p
  try {
    const params = { page: p, size: pageSize }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (statusFilter.value) params.status = statusFilter.value
    const res = await getAdminArticles(params)
    articles.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function search() {
  loadArticles(1)
}

async function handlePublish(row) {
  await togglePublish(row.id)
  ElMessage.success(row.status === 'PUBLISHED' ? '已撤回为草稿' : '已发布')
  loadArticles(page.value)
}

async function handleDelete(id) {
  await deleteArticle(id)
  ElMessage.success('已删除')
  loadArticles(page.value)
}

onMounted(() => loadArticles())
</script>

<style scoped>
.page-wrap {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.page-title {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.page-sub {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-tertiary);
}

.filter-bar {
  display: flex;
  gap: 10px;
  align-items: center;
}

.filter-input { max-width: 280px; }
.filter-select { width: 130px; }

.article-table { border-radius: var(--radius-lg); overflow: hidden; }

.title-link {
  color: var(--color-text-primary);
  text-decoration: none;
  font-weight: 500;
  transition: color var(--transition-fast);
}
.title-link:hover { color: #E8A838; }

.cat-badge {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.status-dot {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: var(--radius-full);
}
.status-dot--pub {
  background: #1A2010;
  color: #6FCF97;
  border: none;
}
.status-dot--draft {
  background: #1A1028;
  color: #9CA3AF;
  border: none;
}

.date-text {
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.act-group { display: flex; gap: 8px; align-items: center; }
.act-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 28px;
  padding: 0 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid;
  transition: opacity var(--transition-fast);
  white-space: nowrap;
}
.act-btn:hover { opacity: 0.8; }
.act-btn--edit { background: #1A1A28; border-color: #2A2A3C; color: #9CA3AF; }
.act-btn--del  { background: #2D1A1A; border-color: #5B2626; color: #EF4444; }

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
}

/* Pencil 表格深色覆盖 */
:deep(.el-table) {
  background: transparent !important;
  color: #F0F0F8;
}
:deep(.el-table__header-wrapper th) {
  background: #111119 !important;
  color: #6E6E82;
  font-weight: 500;
  height: 44px;
}
:deep(.el-table__row) {
  background: transparent !important;
  height: 60px;
}
:deep(.el-table__row:nth-child(even)) {
  background: #0D0D1A !important;
}
:deep(.el-table__row:hover td) {
  background: #16162A !important;
}
:deep(.el-table td) { border-bottom: 1px solid #1C1C2C !important; }
:deep(.el-table th) { border-bottom: 1px solid #1E1E2C !important; }
:deep(.el-button--primary) {
  background: #E8A838 !important;
  border-color: #E8A838 !important;
  color: #000 !important;
  font-weight: 600;
}
:deep(.el-button--primary:hover) { background: #F0B840 !important; }
:deep(.el-input__wrapper) {
  background: #1A1A28 !important;
  border-color: #2A2A3C !important;
  box-shadow: none !important;
}
:deep(.el-input__inner) { color: #F0F0F8 !important; }
:deep(.el-select .el-input__wrapper) {
  background: #1A1A28 !important;
  border-color: #2A2A3C !important;
}
</style>
