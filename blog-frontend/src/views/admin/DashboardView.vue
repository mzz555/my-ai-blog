<template>
  <div class="dashboard">
    <div class="page-head">
      <div>
        <h2 class="page-title">仪表盘</h2>
        <p class="page-sub">欢迎回来，{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</p>
      </div>
    </div>

    <!-- KPI 卡片 -->
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

    <!-- 图表区 -->
    <div class="chart-grid">
      <div class="chart-card">
        <div class="chart-header">
          <span class="chart-title">近 7 天评论趋势</span>
          <span class="chart-badge chart-badge--purple">评论数</span>
        </div>
        <div ref="commentChartEl" class="chart-body" />
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <span class="chart-title">近 7 天发布文章</span>
          <span class="chart-badge chart-badge--gold">篇数</span>
        </div>
        <div ref="articleChartEl" class="chart-body" />
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-section">
      <h3 class="section-title">快捷操作</h3>
      <div class="actions-row">
        <button class="action-btn primary" @click="$router.push('/admin/articles/new')">
          <el-icon><EditPen /></el-icon> 写新文章
        </button>
        <button class="action-btn" @click="$router.push('/admin/comments')">
          <el-icon><ChatDotRound /></el-icon> 查看评论
          <span v-if="pendingComments > 0" class="notif-badge">{{ pendingComments }}</span>
        </button>
        <button class="action-btn" @click="$router.push('/admin/articles')">
          <el-icon><Document /></el-icon> 文章管理
        </button>
        <button class="action-btn" @click="$router.push('/admin/categories')">
          <el-icon><FolderOpened /></el-icon> 分类管理
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { getOverview, getTrend } from '@/api/stats'
import * as echarts from 'echarts/core'
import { LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([LineChart, BarChart, GridComponent, TooltipComponent, CanvasRenderer])

const userStore = useUserStore()
const appStore = useAppStore()

const stats = ref({})
const trend = ref({ dates: [], comments: [], articles: [] })
const pendingComments = computed(() => stats.value.pendingComments || 0)

const commentChartEl = ref(null)
const articleChartEl = ref(null)
let commentChart = null
let articleChart = null

const isDark = computed(() => appStore.darkMode)

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

function buildCommentOption(dark) {
  const t = trend.value
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: dark ? '#1E1E30' : '#fff',
      borderColor: dark ? '#2A2A3C' : '#E5E7EB',
      textStyle: { color: dark ? '#F0F0F8' : '#111827', fontSize: 12 },
    },
    grid: { top: 16, right: 16, bottom: 24, left: 36, containLabel: false },
    xAxis: {
      type: 'category',
      data: t.dates,
      axisLine: { lineStyle: { color: dark ? '#2A2A3C' : '#E5E7EB' } },
      axisTick: { show: false },
      axisLabel: { color: dark ? '#6E6E82' : '#9CA3AF', fontSize: 11 },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: dark ? '#1C1C2C' : '#F3F4F6', type: 'dashed' } },
      axisLabel: { color: dark ? '#6E6E82' : '#9CA3AF', fontSize: 11 },
    },
    series: [{
      type: 'line',
      data: t.comments,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { color: '#818CF8', width: 2.5 },
      itemStyle: { color: '#818CF8', borderColor: dark ? '#13131E' : '#fff', borderWidth: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(129,140,248,.35)' },
          { offset: 1, color: 'rgba(129,140,248,.02)' },
        ]),
      },
    }],
  }
}

function buildArticleOption(dark) {
  const t = trend.value
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: dark ? '#1E1E30' : '#fff',
      borderColor: dark ? '#2A2A3C' : '#E5E7EB',
      textStyle: { color: dark ? '#F0F0F8' : '#111827', fontSize: 12 },
    },
    grid: { top: 16, right: 16, bottom: 24, left: 36, containLabel: false },
    xAxis: {
      type: 'category',
      data: t.dates,
      axisLine: { lineStyle: { color: dark ? '#2A2A3C' : '#E5E7EB' } },
      axisTick: { show: false },
      axisLabel: { color: dark ? '#6E6E82' : '#9CA3AF', fontSize: 11 },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: dark ? '#1C1C2C' : '#F3F4F6', type: 'dashed' } },
      axisLabel: { color: dark ? '#6E6E82' : '#9CA3AF', fontSize: 11 },
    },
    series: [{
      type: 'bar',
      data: t.articles,
      barMaxWidth: 32,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#F0B840' },
          { offset: 1, color: '#E8A838' },
        ]),
        borderRadius: [4, 4, 0, 0],
      },
    }],
  }
}

function initCharts() {
  if (commentChartEl.value && !commentChart) {
    commentChart = echarts.init(commentChartEl.value)
  }
  if (articleChartEl.value && !articleChart) {
    articleChart = echarts.init(articleChartEl.value)
  }
  updateCharts()
}

function updateCharts() {
  const dark = isDark.value
  commentChart?.setOption(buildCommentOption(dark))
  articleChart?.setOption(buildArticleOption(dark))
}

watch(isDark, updateCharts)
watch(() => trend.value, updateCharts, { deep: true })

const resizeObserver = new ResizeObserver(() => {
  commentChart?.resize()
  articleChart?.resize()
})

onMounted(async () => {
  try {
    const [overviewRes, trendRes] = await Promise.all([getOverview(), getTrend()])
    stats.value = overviewRes.data
    trend.value = trendRes.data
  } catch {}
  initCharts()
  if (commentChartEl.value) resizeObserver.observe(commentChartEl.value.parentElement)
})

onUnmounted(() => {
  resizeObserver.disconnect()
  commentChart?.dispose()
  articleChart?.dispose()
})
</script>

<style scoped>
.dashboard { max-width: 1000px; display: flex; flex-direction: column; gap: 24px; }

.page-head { display: flex; flex-direction: column; gap: 4px; }
.page-title { margin: 0; font-size: 22px; font-weight: 700; color: var(--color-text-primary); }
.page-sub { margin: 0; font-size: 13px; color: var(--color-text-tertiary); }

/* KPI */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 14px;
}
.kpi-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 18px 20px;
  display: flex;
  align-items: flex-start;
  gap: 14px;
  position: relative;
  transition: box-shadow var(--transition-base), transform var(--transition-base);
}
.kpi-card:hover { box-shadow: var(--shadow-card-hover); transform: translateY(-2px); }
.kpi-icon-wrap {
  width: 42px; height: 42px; border-radius: var(--radius-md);
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.kpi-body { flex: 1; min-width: 0; }
.kpi-value { font-size: 26px; font-weight: 800; color: var(--color-text-primary); line-height: 1.2; letter-spacing: -1px; }
.kpi-label { font-size: 12px; color: var(--color-text-tertiary); margin-top: 2px; }
.kpi-sub { position: absolute; bottom: 12px; right: 14px; font-size: 11px; font-weight: 600; }

/* 图表 */
.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.chart-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 16px 18px 12px;
}
.chart-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.chart-title { font-size: 13px; font-weight: 600; color: var(--color-text-primary); }
.chart-badge {
  font-size: 10px; font-weight: 700;
  padding: 2px 7px; border-radius: var(--radius-full);
}
.chart-badge--purple { background: rgba(129,140,248,.12); color: #818CF8; border: 1px solid rgba(129,140,248,.25); }
.chart-badge--gold   { background: rgba(232,168,56,.12);  color: #E8A838; border: 1px solid rgba(232,168,56,.25); }
.chart-body { height: 160px; }

/* 快捷操作 */
.section-title { margin: 0 0 12px; font-size: 14px; font-weight: 700; color: var(--color-text-primary); }
.actions-row { display: flex; flex-wrap: wrap; gap: 10px; }
.action-btn {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 9px 18px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text-secondary);
  font-size: 13px; font-weight: 500; cursor: pointer; position: relative;
  transition: background var(--transition-fast), color var(--transition-fast), border-color var(--transition-fast);
}
.action-btn:hover { background: var(--color-bg-secondary); color: var(--color-text-primary); border-color: rgba(232,168,56,.4); }
.action-btn.primary { background: #E8A838; color: #0C0C10; border-color: #E8A838; font-weight: 600; }
.action-btn.primary:hover { background: #F5BC50; border-color: #F5BC50; }
.notif-badge {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 18px; height: 18px; padding: 0 5px;
  background: #E85A4F; color: #fff;
  border-radius: var(--radius-full); font-size: 11px; font-weight: 700;
}

@media (max-width: 680px) {
  .chart-grid { grid-template-columns: 1fr; }
}
</style>
