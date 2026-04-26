<template>
  <div class="page-wrap">
    <div class="page-head">
      <div>
        <h2 class="page-title">评论审核</h2>
        <p class="page-sub">共 {{ total }} 条评论</p>
      </div>
    </div>

    <!-- 状态 Tab 筛选 -->
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
      <el-table-column label="评论者" width="110">
        <template #default="{ row }">
          <div class="user-cell">
            <div class="mini-avatar">{{ (row.nickname || '?').charAt(0).toUpperCase() }}</div>
            <span class="nickname">{{ row.nickname }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="内容" min-width="240">
        <template #default="{ row }">
          <div class="content-cell">
            <p :class="['comment-text', { expanded: expandedIds.has(row.id) }]">{{ row.content }}</p>
            <button v-if="row.content?.length > 60" class="expand-btn" @click="toggleExpand(row.id)">
              {{ expandedIds.has(row.id) ? '收起' : '展开' }}
            </button>
            <a v-if="row.articleId" :href="`/posts/${row.articleSlug || row.articleId}`"
              target="_blank" class="article-link">
              {{ row.articleTitle || '查看文章' }} →
            </a>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <span :class="['status-badge', `status-badge--${row.status?.toLowerCase()}`]">
            {{ statusText(row.status) }}
          </span>
        </template>
      </el-table-column>

      <el-table-column label="时间" width="120">
        <template #default="{ row }">
          <span class="date-text">{{ formatDate(row.createdAt) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <div class="act-group">
            <button v-if="row.status !== 'APPROVED'" class="act-btn act-btn--approve" @click="changeStatus(row.id, 'APPROVED')">
              <el-icon><Check /></el-icon> 通过
            </button>
            <button v-if="row.status !== 'REJECTED'" class="act-btn act-btn--reject" @click="changeStatus(row.id, 'REJECTED')">
              <el-icon><Close /></el-icon> 拒绝
            </button>
            <el-popconfirm title="确认删除该评论？" @confirm="handleDelete(row.id)">
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
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminComments, updateCommentStatus, deleteComment } from '@/api/comment'
import { formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'
import { Check, Close, Delete, RefreshRight } from '@element-plus/icons-vue'

const comments = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)
const statusFilter = ref('')
const pendingCount = ref(0)
const expandedIds = ref(new Set())

const statusTabs = [
  { label: '全部', value: '' },
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' }
]

async function load(p = 1) {
  loading.value = true
  page.value = p
  try {
    const params = { page: p, size: pageSize }
    if (statusFilter.value) params.status = statusFilter.value
    const res = await getAdminComments(params)
    comments.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadPendingCount() {
  try {
    const res = await getAdminComments({ page: 1, size: 1, status: 'PENDING' })
    pendingCount.value = res.data.total
  } catch {}
}

function switchTab(val) {
  statusFilter.value = val
  load(1)
}

function toggleExpand(id) {
  const next = new Set(expandedIds.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  expandedIds.value = next
}

async function changeStatus(id, status) {
  await updateCommentStatus(id, status)
  ElMessage.success('已更新')
  load(page.value)
  loadPendingCount()
}

async function handleDelete(id) {
  await deleteComment(id)
  ElMessage.success('已删除')
  load(page.value)
  loadPendingCount()
}

const statusText = (s) => ({ PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }[s] || s)

onMounted(() => {
  load()
  loadPendingCount()
})
</script>

<style scoped>
.page-wrap { display: flex; flex-direction: column; gap: 20px; }

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

.status-tabs {
  display: flex;
  gap: 4px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 4px;
  width: fit-content;
}

.tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
  background: transparent;
  cursor: pointer;
  transition: all var(--transition-fast);
}
.tab-btn:hover { color: var(--color-text-primary); }
.tab-btn.active {
  background: var(--color-bg);
  color: var(--color-text-primary);
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(0,0,0,.15);
}

.tab-badge {
  min-width: 18px;
  height: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #E8A838;
  color: #0C0C10;
  font-size: 10px;
  font-weight: 700;
  border-radius: var(--radius-full);
  padding: 0 5px;
}

.data-table { border-radius: var(--radius-lg); overflow: hidden; }

.user-cell { display: flex; align-items: center; gap: 8px; }

.mini-avatar {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #E8A838;
  color: #0C0C10;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.nickname {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.content-cell { display: flex; flex-direction: column; gap: 4px; }

.comment-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.comment-text.expanded {
  display: block;
  overflow: visible;
}

.expand-btn {
  background: none;
  border: none;
  font-size: 11px;
  color: #E8A838;
  cursor: pointer;
  padding: 0;
  width: fit-content;
}

.article-link {
  font-size: 11px;
  color: var(--color-text-tertiary);
  text-decoration: none;
  transition: color var(--transition-fast);
}
.article-link:hover { color: #E8A838; }

.status-badge {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: var(--radius-full);
}
.status-badge--pending {
  background: rgba(232,168,56,.12);
  color: #E8A838;
  border: 1px solid rgba(232,168,56,.25);
}
.status-badge--approved {
  background: rgba(52,199,89,.12);
  color: #34C759;
  border: 1px solid rgba(52,199,89,.25);
}
.status-badge--rejected {
  background: rgba(255,77,79,.1);
  color: #ff4d4f;
  border: 1px solid rgba(255,77,79,.2);
}

.date-text { font-size: 12px; color: var(--color-text-tertiary); }

.act-group { display: flex; gap: 6px; align-items: center; flex-wrap: wrap; }
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
  transition: opacity var(--transition-fast);
  white-space: nowrap;
}
.act-btn:hover { opacity: 0.8; }
.act-btn--approve { background: rgba(34,197,94,.12); border-color: rgba(34,197,94,.3); color: var(--color-success); }
.act-btn--reject  { background: transparent; border-color: rgba(239,68,68,.4); color: var(--color-danger); }
.act-btn--del     { background: rgba(239,68,68,.08); border-color: rgba(239,68,68,.3); color: var(--color-danger); }

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 12px 0 4px;
  border-top: 1px solid var(--color-border);
}

:deep(.el-table__row) { height: 60px; }
</style>
