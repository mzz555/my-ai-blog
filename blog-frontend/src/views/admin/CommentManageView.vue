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

      <el-table-column type="index" label="#" width="52" align="center" />

      <el-table-column label="所属文章" width="152">
        <template #default="{ row }">
          <a
            v-if="row.articleTitle"
            :href="`/posts/${row.articleSlug || row.articleId}`"
            target="_blank"
            class="article-chip"
          >
            <span class="article-chip-icon">📄</span>
            <span class="article-chip-text">{{ row.articleTitle }}</span>
          </a>
          <span v-else class="no-data">—</span>
        </template>
      </el-table-column>

      <el-table-column label="评论者" width="110">
        <template #default="{ row }">
          <div class="user-cell">
            <div class="mini-avatar">{{ (row.nickname || '?').charAt(0).toUpperCase() }}</div>
            <span class="nickname">{{ row.nickname }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="内容" min-width="220">
        <template #default="{ row }">
          <div class="content-cell">
            <p v-if="row.parentNickname" class="reply-hint">
              回复 <span class="reply-name">@{{ row.parentNickname }}</span>
            </p>
            <p :class="['comment-text', { expanded: expandedIds.has(row.id) }]">{{ row.content }}</p>
            <button v-if="row.content?.length > 60" class="expand-btn" @click="toggleExpand(row.id)">
              {{ expandedIds.has(row.id) ? '收起' : '展开' }}
            </button>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="根评论" width="90" align="center">
        <template #default="{ row }">
          <span v-if="!row.parentId" class="root-badge root-badge--root">✓ 根评论</span>
          <span v-else class="root-badge root-badge--reply">↩ 回复</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <span :class="['status-badge', `status-badge--${row.status?.toLowerCase()}`]">
            {{ statusText(row.status) }}
          </span>
        </template>
      </el-table-column>

      <el-table-column label="时间" width="100">
        <template #default="{ row }">
          <span class="date-text">{{ formatDate(row.createdAt) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="136" fixed="right">
        <template #default="{ row }">
          <div class="act-group">
            <!-- 查看详情 -->
            <button class="icon-btn icon-btn--view" title="查看详情" @click="openDetail(row)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z"/></svg>
            </button>
            <!-- 通过 -->
            <button
              v-if="row.status !== 'APPROVED'"
              class="icon-btn icon-btn--approve"
              title="通过"
              @click="changeStatus(row.id, 'APPROVED')"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
            </button>
            <!-- 拒绝 -->
            <button
              v-if="row.status !== 'REJECTED'"
              class="icon-btn icon-btn--reject"
              title="拒绝"
              @click="changeStatus(row.id, 'REJECTED')"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
            <!-- 删除 -->
            <el-popconfirm title="确认删除该评论？" @confirm="handleDelete(row.id)">
              <template #reference>
                <button class="icon-btn icon-btn--del" title="删除">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>
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

    <!-- 评论详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="评论详情"
      width="480px"
      :close-on-click-modal="true"
      class="detail-dialog"
    >
      <div v-if="detailRow" class="detail-body">
        <!-- 评论者 -->
        <div class="detail-user">
          <div class="detail-avatar">{{ (detailRow.nickname || '?').charAt(0).toUpperCase() }}</div>
          <div class="detail-user-info">
            <span class="detail-username">{{ detailRow.nickname }}</span>
            <span class="detail-time">{{ formatDate(detailRow.createdAt) }}</span>
          </div>
          <span :class="['status-badge', `status-badge--${detailRow.status?.toLowerCase()}`]" style="margin-left:auto">
            {{ statusText(detailRow.status) }}
          </span>
        </div>

        <!-- 所属文章 -->
        <div class="detail-row">
          <span class="detail-label">所属文章</span>
          <a
            v-if="detailRow.articleTitle"
            :href="`/posts/${detailRow.articleSlug || detailRow.articleId}`"
            target="_blank"
            class="detail-article-link"
          >{{ detailRow.articleTitle }} →</a>
          <span v-else class="no-data">—</span>
        </div>

        <!-- 类型 -->
        <div class="detail-row">
          <span class="detail-label">评论类型</span>
          <span v-if="!detailRow.parentId" class="root-badge root-badge--root">✓ 根评论</span>
          <span v-else class="root-badge root-badge--reply">↩ 回复 @{{ detailRow.parentNickname }}</span>
        </div>

        <!-- 正文 -->
        <div class="detail-content-wrap">
          <span class="detail-label">评论内容</span>
          <p class="detail-content">{{ detailRow.content }}</p>
        </div>

        <!-- 快捷操作 -->
        <div class="detail-actions">
          <button
            v-if="detailRow.status !== 'APPROVED'"
            class="da-btn da-btn--approve"
            @click="quickChange(detailRow.id, 'APPROVED')"
          >✓ 通过</button>
          <button
            v-if="detailRow.status !== 'REJECTED'"
            class="da-btn da-btn--reject"
            @click="quickChange(detailRow.id, 'REJECTED')"
          >✕ 拒绝</button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminComments, updateCommentStatus, deleteComment } from '@/api/comment'
import { formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'

const comments = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)
const statusFilter = ref('')
const pendingCount = ref(0)
const expandedIds = ref(new Set())
const detailVisible = ref(false)
const detailRow = ref(null)

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

function switchTab(val) { statusFilter.value = val; load(1) }

function toggleExpand(id) {
  const next = new Set(expandedIds.value)
  next.has(id) ? next.delete(id) : next.add(id)
  expandedIds.value = next
}

function openDetail(row) { detailRow.value = row; detailVisible.value = true }

async function changeStatus(id, status) {
  try {
    await updateCommentStatus(id, status)
    ElMessage.success('已更新')
    load(page.value)
    loadPendingCount()
  } catch {
  }
}

async function quickChange(id, status) {
  await changeStatus(id, status)
  detailRow.value = comments.value.find(c => c.id === id) || detailRow.value
}

async function handleDelete(id) {
  try {
    await deleteComment(id)
    ElMessage.success('已删除')
    load(page.value)
    loadPendingCount()
  } catch {
  }
}

const statusText = (s) => ({ PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }[s] || s)

onMounted(() => { load(); loadPendingCount() })
</script>

<style scoped>
.page-wrap { display: flex; flex-direction: column; gap: 20px; }
.page-head { display: flex; justify-content: space-between; align-items: flex-start; }
.page-title { margin: 0 0 4px; font-size: 22px; font-weight: 700; color: var(--color-text-primary); }
.page-sub { margin: 0; font-size: 13px; color: var(--color-text-tertiary); }

.status-tabs {
  display: flex; gap: 4px;
  background: var(--color-surface); border: 1px solid var(--color-border);
  border-radius: var(--radius-md); padding: 4px; width: fit-content;
}
.tab-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 6px 16px; border: none; border-radius: var(--radius-sm);
  font-size: 13px; font-weight: 500; color: var(--color-text-secondary);
  background: transparent; cursor: pointer; transition: all var(--transition-fast);
}
.tab-btn:hover { color: var(--color-text-primary); }
.tab-btn.active {
  background: var(--color-bg); color: var(--color-text-primary);
  font-weight: 600; box-shadow: 0 1px 3px rgba(0,0,0,.15);
}
.tab-badge {
  min-width: 18px; height: 18px; display: inline-flex; align-items: center;
  justify-content: center; background: #E8A838; color: #0C0C10;
  font-size: 10px; font-weight: 700; border-radius: var(--radius-full); padding: 0 5px;
}

.data-table { border-radius: var(--radius-lg); overflow: hidden; }

.article-chip {
  display: inline-flex; align-items: center; gap: 4px;
  max-width: 128px; padding: 3px 8px; border-radius: 6px;
  border: 1px solid var(--color-border); background: var(--color-surface);
  color: var(--color-text-secondary); font-size: 11px; text-decoration: none;
  transition: border-color var(--transition-fast), color var(--transition-fast); overflow: hidden;
}
.article-chip:hover { border-color: var(--color-accent); color: var(--color-accent); }
.article-chip-icon { flex-shrink: 0; }
.article-chip-text { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.no-data { font-size: 13px; color: var(--color-text-tertiary); }

.user-cell { display: flex; align-items: center; gap: 8px; }
.mini-avatar {
  width: 26px; height: 26px; border-radius: 50%;
  background: #E8A838; color: #0C0C10;
  font-size: 11px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.nickname { font-size: 13px; font-weight: 500; color: var(--color-text-primary); }

.content-cell { display: flex; flex-direction: column; gap: 3px; }
.reply-hint { margin: 0; font-size: 11px; color: var(--color-text-tertiary); }
.reply-name { color: #E8A838; font-weight: 600; }
.comment-text {
  margin: 0; font-size: 13px; line-height: 1.6; color: var(--color-text-secondary);
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.comment-text.expanded { display: block; overflow: visible; }
.expand-btn {
  background: none; border: none; font-size: 11px;
  color: #E8A838; cursor: pointer; padding: 0; width: fit-content;
}

.root-badge {
  display: inline-flex; align-items: center;
  font-size: 11px; font-weight: 600;
  padding: 3px 8px; border-radius: 20px; white-space: nowrap;
}
.root-badge--root { background: rgba(99,102,241,.12); color: #818CF8; border: 1px solid rgba(99,102,241,.25); }
.root-badge--reply { background: rgba(20,184,166,.10); color: #2DD4BF; border: 1px solid rgba(20,184,166,.25); }

.status-badge {
  display: inline-flex; align-items: center;
  font-size: 11px; font-weight: 600;
  padding: 2px 8px; border-radius: var(--radius-full);
}
.status-badge--pending  { background: rgba(232,168,56,.12); color: #E8A838; border: 1px solid rgba(232,168,56,.25); }
.status-badge--approved { background: rgba(52,199,89,.12);  color: #34C759; border: 1px solid rgba(52,199,89,.25); }
.status-badge--rejected { background: rgba(255,77,79,.1);   color: #ff4d4f; border: 1px solid rgba(255,77,79,.2);  }

.date-text { font-size: 12px; color: var(--color-text-tertiary); line-height: 1.7; }

/* 操作列 — 图标按钮横排 */
.act-group { display: flex; gap: 6px; align-items: center; }
.icon-btn {
  width: 30px; height: 30px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  border: 1px solid; cursor: pointer;
  transition: all var(--transition-fast); flex-shrink: 0;
}
.icon-btn svg { width: 14px; height: 14px; }
.icon-btn:hover { transform: translateY(-1px); box-shadow: 0 3px 8px rgba(0,0,0,.25); }

.icon-btn--view    { background: var(--color-surface); border-color: var(--color-border); color: var(--color-text-secondary); }
.icon-btn--view:hover { border-color: var(--color-accent); color: var(--color-accent); }

.icon-btn--approve { background: rgba(34,197,94,.10); border-color: rgba(34,197,94,.3); color: #22C55E; }
.icon-btn--approve:hover { background: rgba(34,197,94,.2); }

.icon-btn--reject  { background: transparent; border-color: rgba(239,68,68,.35); color: #EF4444; }
.icon-btn--reject:hover { background: rgba(239,68,68,.08); }

.icon-btn--del     { background: rgba(239,68,68,.08); border-color: rgba(239,68,68,.25); color: #EF4444; }
.icon-btn--del:hover { background: rgba(239,68,68,.18); }

.pagination-wrap {
  display: flex; justify-content: flex-end; align-items: center;
  padding: 12px 0 4px; border-top: 1px solid var(--color-border);
}

/* 详情弹窗 */
.detail-body { display: flex; flex-direction: column; gap: 20px; }

.detail-user {
  display: flex; align-items: center; gap: 12px;
  padding-bottom: 16px; border-bottom: 1px solid var(--color-border);
}
.detail-avatar {
  width: 40px; height: 40px; border-radius: 50%;
  background: #E8A838; color: #0C0C10;
  font-size: 16px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.detail-user-info { display: flex; flex-direction: column; gap: 3px; }
.detail-username { font-size: 15px; font-weight: 600; color: var(--color-text-primary); }
.detail-time { font-size: 12px; color: var(--color-text-tertiary); }

.detail-row {
  display: flex; align-items: center; gap: 12px;
}
.detail-label {
  font-size: 12px; color: var(--color-text-tertiary);
  width: 64px; flex-shrink: 0;
}
.detail-article-link {
  font-size: 13px; color: var(--color-accent);
  text-decoration: none; transition: opacity var(--transition-fast);
}
.detail-article-link:hover { opacity: 0.8; }

.detail-content-wrap { display: flex; flex-direction: column; gap: 8px; }
.detail-content {
  margin: 0;
  padding: 14px 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px; line-height: 1.75;
  color: var(--color-text-primary);
  white-space: pre-wrap; word-break: break-word;
}

.detail-actions {
  display: flex; gap: 10px;
  padding-top: 4px; border-top: 1px solid var(--color-border);
}
.da-btn {
  flex: 1; height: 36px; border-radius: var(--radius-md);
  font-size: 13px; font-weight: 600; cursor: pointer;
  border: 1px solid; transition: opacity var(--transition-fast);
}
.da-btn:hover { opacity: 0.8; }
.da-btn--approve { background: rgba(34,197,94,.12); border-color: rgba(34,197,94,.3); color: #22C55E; }
.da-btn--reject  { background: rgba(239,68,68,.08); border-color: rgba(239,68,68,.3); color: #EF4444; }

:deep(.el-table__row) { height: 60px; }
</style>
