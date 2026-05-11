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
      <div v-for="(cat, i) in categories" :key="cat.id" class="cat-card">
        <div class="cat-card-top">
          <div class="cat-icon" :style="iconStyle(i)">{{ cat.name.charAt(0) }}</div>
          <div class="cat-meta">
            <span class="cat-name">{{ cat.name }}</span>
            <span
            class="cat-badge"
            :class="{ 'cat-badge--link': (cat.articleCount ?? 0) > 0 }"
            :style="badgeStyle(i)"
            @click="(cat.articleCount ?? 0) > 0 && router.push(`/admin/articles?categoryId=${cat.id}`)"
          >{{ cat.articleCount ?? 0 }}篇</span>
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
  </AdminPageCard>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/api/category'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import AdminPageCard from '@/components/admin/AdminPageCard.vue'

const router = useRouter()

const PALETTE = [
  { iconBg: 'rgba(124,58,237,.12)',  iconFg: '#7C3AED', badgeBg: 'rgba(168,85,247,.12)',  badgeFg: '#A855F7' },
  { iconBg: 'rgba(34,197,94,.12)',   iconFg: '#16A34A', badgeBg: 'rgba(34,197,94,.12)',   badgeFg: '#16A34A' },
  { iconBg: 'rgba(232,168,56,.12)',  iconFg: '#D97706', badgeBg: 'rgba(232,168,56,.12)',  badgeFg: '#D97706' },
  { iconBg: 'rgba(59,130,246,.12)',  iconFg: '#2563EB', badgeBg: 'rgba(96,165,250,.12)',  badgeFg: '#2563EB' },
  { iconBg: 'rgba(168,85,247,.12)',  iconFg: '#9333EA', badgeBg: 'rgba(192,132,252,.12)', badgeFg: '#9333EA' },
  { iconBg: 'rgba(245,158,11,.12)',  iconFg: '#D97706', badgeBg: 'rgba(251,191,36,.12)',  badgeFg: '#B45309' },
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
  } catch {
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteCategory(id)
    ElMessage.success('已删除')
    load()
  } catch {
  }
}

onMounted(load)
</script>

<style scoped>
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
.cat-badge { font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 10px; transition: opacity 0.15s; }
.cat-badge--link { cursor: pointer; }
.cat-badge--link:hover { opacity: 0.7; }

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
.act-btn--edit { background: var(--color-bg-secondary); border-color: var(--color-border); color: var(--color-text-secondary); }
.act-btn--del  { background: rgba(239,68,68,.08); border-color: rgba(239,68,68,.3); color: var(--color-danger); }

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

</style>
