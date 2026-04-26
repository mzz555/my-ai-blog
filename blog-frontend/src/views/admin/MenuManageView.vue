<template>
  <div class="mm-page">
    <div class="page-head">
      <h2 class="page-title">菜单管理</h2>
      <button class="btn-add" @click="openCreate(null)">
        <span class="btn-icon">+</span> 新建菜单
      </button>
    </div>

    <el-table
      :data="menus"
      v-loading="loading"
      border
      row-key="id"
      default-expand-all
      :tree-props="{ children: 'children' }"
      class="mm-table"
    >
      <el-table-column prop="name" label="菜单名称" min-width="160" />
      <el-table-column prop="path" label="路径" min-width="160">
        <template #default="{ row }">
          <span class="path-code">{{ row.path || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="icon" label="图标" width="120">
        <template #default="{ row }">
          <span class="icon-text">{{ row.icon || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" align="center" />
      <el-table-column label="显示" width="80" align="center">
        <template #default="{ row }">
          <span :class="['vis-dot', row.visible ? 'vis-on' : 'vis-off']">
            {{ row.visible ? '是' : '否' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <div class="action-btns">
            <button class="icon-btn add-child-btn" @click="openCreate(row.id)" title="新增子菜单">
              <Plus />
            </button>
            <button class="icon-btn edit-btn" @click="openEdit(row)" title="编辑">
              <Edit />
            </button>
            <el-popconfirm
              title="确认删除该菜单？子菜单将一并删除。"
              confirm-button-text="删除"
              cancel-button-text="取消"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <button class="icon-btn del-btn" title="删除"><Delete /></button>
              </template>
            </el-popconfirm>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="editId ? '编辑菜单' : '新建菜单'"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="form-body">
        <div class="form-row">
          <div class="form-field">
            <label class="form-label">菜单名称 <span class="required">*</span></label>
            <input v-model="form.name" class="form-input" placeholder="如：文章管理" />
          </div>
          <div class="form-field">
            <label class="form-label">路径</label>
            <input v-model="form.path" class="form-input" placeholder="如：/admin/articles" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-field">
            <label class="form-label">图标</label>
            <input v-model="form.icon" class="form-input" placeholder="如：Document" />
          </div>
          <div class="form-field">
            <label class="form-label">排序号</label>
            <input v-model.number="form.sort" class="form-input" type="number" placeholder="数字越小越靠前" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-field">
            <label class="form-label">父级菜单</label>
            <select v-model="form.parentId" class="form-select">
              <option :value="null">顶级菜单</option>
              <option v-for="m in flatMenus" :key="m.id" :value="m.id">{{ m.name }}</option>
            </select>
          </div>
          <div class="form-field vis-field">
            <label class="form-label">是否显示</label>
            <el-switch v-model="form.visible" active-color="var(--color-primary)" />
          </div>
        </div>
      </div>
      <template #footer>
        <button class="btn-cancel" @click="dialogVisible = false">取消</button>
        <button class="btn-save" :disabled="saving" @click="handleSave">
          {{ saving ? '保存中…' : '保存' }}
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getMenus, createMenu, updateMenu, deleteMenu } from '@/api/menu'

const menus = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editId = ref(null)

const form = reactive({ name: '', path: '', icon: '', sort: 0, visible: true, parentId: null })

const flatMenus = computed(() => {
  const result = []
  const flatten = (list) => list.forEach(m => { result.push(m); if (m.children?.length) flatten(m.children) })
  flatten(menus.value)
  return result
})

async function loadMenus() {
  loading.value = true
  try {
    const res = await getMenus()
    menus.value = res.data || []
  } catch {
    ElMessage.error('加载菜单失败，请刷新重试')
  } finally {
    loading.value = false
  }
}

function openCreate(parentId) {
  editId.value = null
  form.name = ''
  form.path = ''
  form.icon = ''
  form.sort = 0
  form.visible = true
  form.parentId = parentId
  dialogVisible.value = true
}

function openEdit(row) {
  editId.value = row.id
  form.name = row.name
  form.path = row.path || ''
  form.icon = row.icon || ''
  form.sort = row.sort || 0
  form.visible = row.visible !== false
  form.parentId = row.parentId || null
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入菜单名称')
    return
  }
  saving.value = true
  try {
    const payload = { ...form, name: form.name.trim() }
    if (editId.value) {
      await updateMenu(editId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createMenu(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadMenus()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteMenu(id)
    ElMessage.success('删除成功')
    loadMenus()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '删除失败')
  }
}

onMounted(loadMenus)
</script>

<style scoped>
.mm-page { display: flex; flex-direction: column; gap: 20px; }

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}
.page-title { margin: 0 0 4px; font-size: 22px; font-weight: 700; color: var(--color-text-primary); }

.btn-add {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 18px;
  background: var(--color-primary);
  color: #0C0C10;
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background var(--transition-fast);
}
.btn-add:hover { background: #F5BC50; }
.btn-icon { font-size: 18px; line-height: 1; }

.path-code { font-family: monospace; font-size: 13px; color: var(--color-primary); }
.icon-text { font-size: 13px; color: var(--color-text-secondary); }

.vis-dot {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}
.vis-on { background: rgba(34,197,94,.12); color: #22C55E; }
.vis-off { background: rgba(110,110,130,.12); color: var(--color-text-muted); }

.action-btns { display: flex; gap: 6px; justify-content: center; }
.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background var(--transition-fast);
}
.icon-btn svg { width: 14px; height: 14px; }
.add-child-btn { background: rgba(99,102,241,.12); color: #818CF8; }
.add-child-btn:hover { background: rgba(99,102,241,.24); }
.edit-btn { background: rgba(232,168,56,.15); color: var(--color-primary); }
.edit-btn:hover { background: rgba(232,168,56,.28); }
.del-btn { background: rgba(239,68,68,.12); color: #EF4444; }
.del-btn:hover { background: rgba(239,68,68,.24); }

.form-body { display: flex; flex-direction: column; gap: var(--space-4); }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4); }
.form-field { display: flex; flex-direction: column; gap: 6px; }
.vis-field { justify-content: flex-start; padding-top: 4px; }
.form-label { font-size: 13px; color: var(--color-text-secondary); }
.required { color: #EF4444; }
.form-input {
  padding: 10px 14px;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-primary);
  font-size: 14px;
  outline: none;
  transition: border-color var(--transition-fast);
}
.form-input:focus { border-color: var(--color-primary); }
.form-select {
  padding: 10px 14px;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-primary);
  font-size: 14px;
  outline: none;
  cursor: pointer;
}
.form-select:focus { border-color: var(--color-primary); }

.btn-cancel, .btn-save {
  padding: 9px 24px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background var(--transition-fast);
  margin-left: 10px;
}
.btn-cancel { background: var(--color-bg); color: var(--color-text-secondary); border: 1px solid var(--color-border); }
.btn-cancel:hover { border-color: var(--color-primary); color: var(--color-primary); }
.btn-save { background: var(--color-primary); color: #0C0C10; border: none; font-weight: 600; }
.btn-save:hover:not(:disabled) { background: #F5BC50; }
.btn-save:disabled { opacity: 0.6; cursor: not-allowed; }

:deep(.el-table__row) { height: 56px; }
</style>
