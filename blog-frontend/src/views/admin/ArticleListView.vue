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
      <!-- 封面 -->
      <el-table-column label="封面" width="88">
        <template #default="{ row }">
          <div class="cover-cell">
            <img v-if="row.coverImage" :src="row.coverImage" class="cover-thumb" />
            <div v-else class="cover-placeholder"><el-icon><Picture /></el-icon></div>
          </div>
        </template>
      </el-table-column>

      <!-- 标题 + 摘要 -->
      <el-table-column label="文章" min-width="280">
        <template #default="{ row }">
          <div class="title-block">
            <router-link :to="`/admin/articles/${row.id}/edit`" class="title-link">
              {{ row.title }}
            </router-link>
            <p v-if="row.summary" class="summary-text">{{ row.summary }}</p>
            <div class="meta-row">
              <span v-if="row.categoryName" class="cat-chip">{{ row.categoryName }}</span>
              <span v-for="tag in (row.tagNames || []).slice(0,2)" :key="tag" class="tag-chip">#{{ tag }}</span>
            </div>
          </div>
        </template>
      </el-table-column>

      <!-- 状态 -->
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <span :class="['status-dot', row.status === 'PUBLISHED' ? 'status-dot--pub' : 'status-dot--draft']">
            {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
          </span>
        </template>
      </el-table-column>

      <!-- 阅读量 -->
      <el-table-column prop="viewCount" label="阅读" width="70" align="center">
        <template #default="{ row }">
          <span class="num-text">{{ row.viewCount ?? 0 }}</span>
        </template>
      </el-table-column>

      <!-- 发布时间 -->
      <el-table-column label="发布时间" width="112">
        <template #default="{ row }">
          <span class="date-text">{{ row.publishedAt ? formatDate(row.publishedAt) : '—' }}</span>
        </template>
      </el-table-column>

      <!-- 操作 -->
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <div class="act-group">
            <button class="act-btn act-btn--edit" @click="$router.push(`/admin/articles/${row.id}/edit`)">
              <el-icon><Edit /></el-icon> 编辑
            </button>
            <button class="act-btn act-btn--del" @click="openDelete(row)">
              <el-icon><Delete /></el-icon> 删除
            </button>
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

    <!-- 删除确认弹窗 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="删除文章"
      width="420px"
      :close-on-click-modal="false"
      class="delete-dialog"
    >
      <div class="delete-body">
        <div class="delete-icon-wrap">
          <el-icon class="delete-icon"><Warning /></el-icon>
        </div>
        <p class="delete-msg">确定要删除文章</p>
        <p class="delete-title-preview">「{{ deleteTarget?.title }}」</p>
        <p class="delete-hint">此操作不可撤销，文章将被永久删除。</p>
      </div>
      <template #footer>
        <div class="delete-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" :loading="deleting" @click="confirmDelete">确认删除</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminArticles, deleteArticle } from '@/api/article'
import { formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'
import { Plus, Search, Edit, Delete, Picture, Warning } from '@element-plus/icons-vue'

const articles = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)
const keyword = ref('')
const statusFilter = ref('')

const deleteDialogVisible = ref(false)
const deleteTarget = ref(null)
const deleting = ref(false)

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

function search() { loadArticles(1) }

function openDelete(row) {
  deleteTarget.value = row
  deleteDialogVisible.value = true
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await deleteArticle(deleteTarget.value.id)
    ElMessage.success('文章已删除')
    deleteDialogVisible.value = false
    loadArticles(page.value)
  } catch {
    ElMessage.error('删除失败，请重试')
  } finally {
    deleting.value = false
  }
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

/* 封面 */
.cover-cell { width: 64px; height: 42px; }
.cover-thumb {
  width: 64px;
  height: 42px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #1C1C2C;
  display: block;
}
.cover-placeholder {
  width: 64px;
  height: 42px;
  border-radius: 4px;
  background: #1C1C2E;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3A3A5C;
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
.meta-row { display: flex; gap: 6px; flex-wrap: wrap; align-items: center; }
.cat-chip {
  font-size: 11px;
  padding: 1px 7px;
  border-radius: 3px;
  background: #1C1C2E;
  color: #9CA3AF;
}
.tag-chip {
  font-size: 11px;
  color: #4A4A6A;
}

/* 状态 */
.status-dot {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 20px;
}
.status-dot--pub { background: #1A2010; color: #6FCF97; }
.status-dot--draft { background: #1A1028; color: #9CA3AF; }

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

.pagination-wrap { display: flex; justify-content: flex-end; }

/* 删除弹窗内容 */
.delete-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px 0 8px;
  text-align: center;
}
.delete-icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: #2D1A1A;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}
.delete-icon { font-size: 26px; color: #EF4444; }
.delete-msg { margin: 0; font-size: 15px; color: var(--color-text-primary); font-weight: 500; }
.delete-title-preview {
  margin: 0;
  font-size: 14px;
  color: #E8A838;
  font-weight: 600;
  max-width: 320px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.delete-hint { margin: 0; font-size: 12px; color: var(--color-text-tertiary); }
.delete-footer { display: flex; justify-content: flex-end; gap: 10px; }

/* El-Plus 覆盖：依赖全局 tokens.css，此处只补充布局相关 */
:deep(.el-table__row) { height: 68px; }
:deep(.el-dialog__header) { padding: 16px 20px; }
:deep(.el-dialog__body) { padding: 20px; }
:deep(.el-dialog__footer) { padding: 14px 20px; }
</style>
