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
      <div v-for="(cat, i) in categories" :key="cat.id" class="cat-card">
        <div class="cat-card-top">
          <div class="cat-icon" :style="iconStyle(i)">{{ cat.name.charAt(0) }}</div>
          <div class="cat-meta">
            <span class="cat-name">{{ cat.name }}</span>
            <span class="cat-badge" :style="badgeStyle(i)">{{ cat.articleCount ?? 0 }}篇</span>
          </div>
        </div>
        <p class="cat-desc">{{ cat.description || cat.slug || '—' }}</p>
        <div class="cat-footer">
          <button class="act-btn act-btn--edit" @click="openDialog(cat)">
            <el-icon><Edit /></el-icon> 编辑
          </button>
          <el-popconfirm :title="`确认删除分类「${cat.name}」？`" @confirm="handleDelete(cat.id)">
            <template #reference>
              <button class="act-btn act-btn--del">
                <el-icon><Delete /></el-icon> 删除
              </button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && !categories.length" description="暂无分类" />

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑分类' : '新建分类'" width="460px" destroy-on-close>
      <div class="form-body">
        <div class="form-item">
          <label class="form-label">分类名称 <span class="required">*</span></label>
          <el-input v-model="form.name" placeholder="例：前端开发" />
        </div>
        <div class="form-item">
          <label class="form-label">Slug</label>
          <el-input v-model="form.slug" placeholder="留空自动生成" />
          <p class="form-hint">
            预览路径：<code class="hint-code">/category/{{ form.slug || slugPreview || '…' }}</code>
          </p>
        </div>
        <div class="form-item">
          <label class="form-label">描述</label>
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="分类简介（可选）" />
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/api/category'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

const PALETTE = [
  { iconBg: '#1A0B2E', iconFg: '#7C3AED', badgeBg: '#2E1A40', badgeFg: '#A855F7' },
  { iconBg: '#0A2A14', iconFg: '#22C55E', badgeBg: '#14532D', badgeFg: '#4ADE80' },
  { iconBg: '#231E0F', iconFg: '#E8A838', badgeBg: '#2B2210', badgeFg: '#E8A838' },
  { iconBg: '#0D1A33', iconFg: '#3B82F6', badgeBg: '#1E3A5F', badgeFg: '#60A5FA' },
  { iconBg: '#1A0D2E', iconFg: '#A855F7', badgeBg: '#2E1A40', badgeFg: '#C084FC' },
  { iconBg: '#1C1A0A', iconFg: '#F59E0B', badgeBg: '#2B2210', badgeFg: '#FBBF24' },
]
function iconStyle(i) { const p = PALETTE[i % PALETTE.length]; return { background: p.iconBg, color: p.iconFg } }
function badgeStyle(i) { const p = PALETTE[i % PALETTE.length]; return { background: p.badgeBg, color: p.badgeFg } }

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editId = ref(null)
const form = reactive({ name: '', slug: '', description: '' })

const slugPreview = computed(() =>
  form.name.toLowerCase().replace(/\s+/g, '-').replace(/[^\w-]/g, '')
)

async function load() {
  loading.value = true
  try {
    const res = await getCategories()
    categories.value = res.data
  } finally {
    loading.value = false
  }
}

function openDialog(row = null) {
  editId.value = row?.id || null
  Object.assign(form, {
    name: row?.name || '',
    slug: row?.slug || '',
    description: row?.description || ''
  })
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.name.trim()) return ElMessage.warning('请输入分类名称')
  saving.value = true
  try {
    const payload = {
      name: form.name.trim(),
      slug: form.slug.trim() || slugPreview.value,
      description: form.description.trim()
    }
    if (editId.value) await updateCategory(editId.value, payload)
    else await createCategory(payload)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  await deleteCategory(id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.page-wrap { display: flex; flex-direction: column; gap: 20px; }
.page-head { display: flex; justify-content: space-between; align-items: flex-start; }
.page-title { margin: 0 0 4px; font-size: 22px; font-weight: 700; color: var(--color-text-primary); }
.page-sub { margin: 0; font-size: 13px; color: var(--color-text-tertiary); }

/* Route tabs */
.route-tabs { display: flex; border-bottom: 1px solid var(--color-border); }
.rtab {
  padding: 10px 20px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-tertiary);
  text-decoration: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: color var(--transition-fast), border-color var(--transition-fast);
}
.rtab:hover { color: var(--color-text-primary); }
.rtab--active { color: #E8A838; border-bottom-color: #E8A838; }

/* Card grid */
.cat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  min-height: 80px;
}

.cat-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  transition: box-shadow var(--transition-fast), border-color var(--transition-fast);
}
.cat-card:hover { border-color: rgba(232,168,56,.3); box-shadow: var(--shadow-card-hover); }

.cat-card-top { display: flex; align-items: center; gap: 10px; }

.cat-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 700;
  flex-shrink: 0;
}

.cat-meta { display: flex; align-items: center; gap: 8px; flex: 1; }
.cat-name { font-size: 14px; font-weight: 600; color: var(--color-text-primary); }
.cat-badge { font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 10px; }

.cat-desc { margin: 0; font-size: 12px; color: var(--color-text-tertiary); line-height: 1.5; flex: 1; }

.cat-footer { display: flex; gap: 8px; justify-content: flex-end; margin-top: auto; }

/* Action buttons */
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
}
.act-btn:hover { opacity: 0.8; }
.act-btn--edit { background: #1A1A28; border-color: #2A2A3C; color: #9CA3AF; }
.act-btn--del  { background: #2D1A1A; border-color: #5B2626; color: #EF4444; }

/* Dialog */
.form-body { display: flex; flex-direction: column; gap: 16px; }
.form-item { display: flex; flex-direction: column; gap: 6px; }
.form-label { font-size: 13px; font-weight: 500; color: var(--color-text-primary); }
.required { color: #ff4d4f; }
.form-hint { margin: 4px 0 0; font-size: 12px; color: var(--color-text-tertiary); }
.hint-code {
  font-family: var(--font-mono);
  font-size: 12px;
  color: #E8A838;
  background: rgba(232,168,56,.08);
  border-radius: var(--radius-sm);
  padding: 1px 4px;
}
.dialog-footer { display: flex; justify-content: flex-end; gap: 10px; }

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
</style>
