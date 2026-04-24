<template>
  <div class="dashboard">
    <div class="page-head">
      <h2 class="page-title">仪表盘</h2>
      <p class="page-sub">欢迎回来，{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</p>
    </div>

    <div class="kpi-grid">
      <div v-for="card in kpiCards" :key="card.label" class="kpi-card">
        <div class="kpi-icon-wrap" :style="{ background: card.iconBg }">
          <el-icon :size="20" :style="{ color: card.iconColor }"><component :is="card.icon" /></el-icon>
        </div>
        <div class="kpi-body">
          <div class="kpi-value">{{ card.value }}</div>
          <div class="kpi-label">{{ card.label }}</div>
        </div>
        <div v-if="card.sub" class="kpi-sub" :style="{ color: card.subColor }">{{ card.sub }}</div>
      </div>
    </div>

    <div class="quick-section">
      <h3 class="section-title">快捷操作</h3>
      <div class="actions-row">
        <button class="action-btn primary" @click="$router.push('/admin/articles/new')">
          <el-icon><EditPen /></el-icon>
          写新文章
        </button>
        <button class="action-btn" @click="$router.push('/admin/comments')">
          <el-icon><ChatDotRound /></el-icon>
          查看评论
          <span v-if="pendingComments > 0" class="notif-badge">{{ pendingComments }}</span>
        </button>
        <button class="action-btn" @click="$router.push('/admin/articles')">
          <el-icon><Document /></el-icon>
          文章管理
        </button>
        <button class="action-btn" @click="$router.push('/admin/categories')">
          <el-icon><FolderOpened /></el-icon>
          分类管理
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getOverview } from '@/api/stats'

const userStore = useUserStore()
const stats = ref({})
const pendingComments = computed(() => stats.value.pendingComments || 0)

const kpiCards = computed(() => [
  {
    label: '文章总数', value: stats.value.totalArticles ?? '-',
    icon: 'Document', iconBg: 'rgba(232,168,56,.12)', iconColor: '#E8A838',
    sub: `已发布 ${stats.value.publishedArticles ?? 0}`, subColor: '#4CAF8E',
  },
  {
    label: '草稿箱', value: stats.value.draftArticles ?? '-',
    icon: 'EditPen', iconBg: 'rgba(124,111,232,.12)', iconColor: '#7C6FE8',
  },
  {
    label: '评论总数', value: stats.value.totalComments ?? '-',
    icon: 'ChatDotRound', iconBg: 'rgba(76,175,142,.12)', iconColor: '#4CAF8E',
    sub: pendingComments.value > 0 ? `待审核 ${pendingComments.value}` : null,
    subColor: '#E85A4F',
  },
  {
    label: '用户总数', value: stats.value.totalUsers ?? '-',
    icon: 'User', iconBg: 'rgba(74,158,232,.12)', iconColor: '#4A9EE8',
  },
])

onMounted(async () => {
  try {
    const res = await getOverview()
    stats.value = res.data
  } catch {}
})
</script>

<style scoped>
.dashboard { max-width: 960px; display: flex; flex-direction: column; gap: 32px; }

.page-head { display: flex; flex-direction: column; gap: 6px; }
.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text-primary);
}
.page-sub { margin: 0; font-size: 13px; color: var(--color-text-tertiary); }

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 16px;
}

.kpi-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  position: relative;
  transition: box-shadow var(--transition-base), transform var(--transition-base);
}
.kpi-card:hover {
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-2px);
}

.kpi-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.kpi-body { flex: 1; min-width: 0; }

.kpi-value {
  font-size: 28px;
  font-weight: 800;
  color: var(--color-text-primary);
  line-height: 1.2;
  letter-spacing: -1px;
}

.kpi-label {
  font-size: 13px;
  color: var(--color-text-tertiary);
  margin-top: 2px;
}

.kpi-sub {
  position: absolute;
  bottom: 14px;
  right: 16px;
  font-size: 11px;
  font-weight: 600;
}

.section-title {
  margin: 0 0 16px;
  font-size: 15px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.actions-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  position: relative;
  transition: background var(--transition-fast), color var(--transition-fast), border-color var(--transition-fast);
}
.action-btn:hover {
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
  border-color: rgba(232,168,56,.4);
}
.action-btn.primary {
  background: #E8A838;
  color: #0C0C10;
  border-color: #E8A838;
}
.action-btn.primary:hover { background: #F5BC50; border-color: #F5BC50; }

.notif-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #E85A4F;
  color: #fff;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 700;
}
</style>
