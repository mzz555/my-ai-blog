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
      <div v-for="tag in tags" :key="tag.id" class="tag-card">
        <div class="tag-card-inner">
          <span class="tag-hash">#</span>
          <span class="tag-name">{{ tag.name }}</span>
          <span v-if="tag.articleCount != null" class="tag-count">{{ tag.articleCount }}</span>
        </div>
        <div class="tag-actions">
          <button class="tag-btn" @click="openDialog(tag)">
            <el-icon><Edit /></el-icon>
          </button>
          <el-popconfirm :title="`确认删除标签「${tag.name}」？`" @confirm="handleDelete(tag.id)">
            <template #reference>
              <button class="tag-btn tag-btn--danger">
                <el-icon><Delete /></el-icon>
              </button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </div>

    <el-empty v-else description="暂无标签" />

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑标签' : '新建标签'" width="360px" destroy-on-close>
      <div class="form-body">
        <div class="form-item">
          <label class="form-label">标签名称 <span class="required">*</span></label>
          <el-input v-model="form.name" placeholder="例：Vue3" @keyup.enter="handleSave" />
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
import { ref, reactive, onMounted } from 'vue'
import { getTags, createTag, updateTag, deleteTag } from '@/api/tag'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

const tags = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editId = ref(null)
const form = reactive({ name: '' })

async function load() {
  loading.value = true
  try {
    const res = await getTags()
    tags.value = res.data
  } finally {
    loading.value = false
  }
}

function openDialog(tag = null) {
  editId.value = tag?.id || null
  form.name = tag?.name || ''
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.name.trim()) return ElMessage.warning('请输入标签名称')
  saving.value = true
  try {
    if (editId.value) await updateTag(editId.value, { name: form.name.trim() })
    else await createTag({ name: form.name.trim() })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  await deleteTag(id)
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

.tag-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px 8px 14px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  transition: border-color var(--transition-fast);
}
.tag-card:hover { border-color: rgba(232,168,56,.4); }

.tag-card-inner {
  display: flex;
  align-items: center;
  gap: 4px;
}

.tag-hash {
  font-size: 13px;
  font-weight: 700;
  color: #E8A838;
}

.tag-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.tag-count {
  font-size: 11px;
  color: var(--color-text-tertiary);
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  padding: 1px 6px;
  margin-left: 4px;
}

.tag-actions {
  display: flex;
  gap: 4px;
  margin-left: 4px;
}

.tag-btn {
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-text-tertiary);
  cursor: pointer;
  transition: all var(--transition-fast);
  font-size: 12px;
}
.tag-btn:hover { background: var(--color-bg); color: var(--color-text-primary); }
.tag-btn--danger:hover { color: #ff4d4f; background: rgba(255,77,79,.08); }

.form-body { display: flex; flex-direction: column; gap: 16px; }
.form-item { display: flex; flex-direction: column; gap: 6px; }

.form-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.required { color: #ff4d4f; }

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
