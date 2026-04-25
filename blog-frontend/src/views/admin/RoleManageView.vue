<template>
  <div class="page-wrap">
    <div class="page-head">
      <div>
        <h2 class="page-title">角色管理</h2>
        <p class="page-sub">共 {{ roles.length }} 个角色</p>
      </div>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon> 新建角色
      </el-button>
    </div>

    <el-table :data="roles" v-loading="loading" class="data-table">
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column prop="name" label="角色名" width="150" />
      <el-table-column prop="description" label="描述" min-width="160">
        <template #default="{ row }">
          <span class="desc-text">{{ row.description || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="权限" min-width="240">
        <template #default="{ row }">
          <div class="perm-tags">
            <span v-for="p in row.permissions" :key="p.id" class="perm-tag">{{ p.code }}</span>
            <span v-if="!row.permissions?.length" class="no-perm">无权限</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <div class="act-group">
            <button class="act-btn act-btn--edit" @click="openEdit(row)">
              <el-icon><Edit /></el-icon> 编辑
            </button>
            <el-popconfirm title="确认删除该角色？" @confirm="handleDelete(row.id)">
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

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑角色' : '新建角色'" width="520px" destroy-on-close>
      <div class="form-body">
        <div class="form-item">
          <label class="form-label">角色名 <span class="required">*</span></label>
          <el-input v-model="form.name" placeholder="如：编辑员" />
        </div>
        <div class="form-item">
          <label class="form-label">描述</label>
          <el-input v-model="form.description" placeholder="角色职责描述（可选）" />
        </div>
        <div class="form-item">
          <label class="form-label">权限分配</label>
          <el-checkbox-group v-model="form.permissionCodes" class="perm-check-group">
            <el-checkbox v-for="perm in allPermissions" :key="perm.code" :label="perm.code">{{ perm.label }}</el-checkbox>
          </el-checkbox-group>
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
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getRoles, createRole, updateRole, deleteRole } from '@/api/role'

const roles = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editId = ref(null)

const allPermissions = [
  { code: 'article:list',    label: '文章列表' },
  { code: 'article:create',  label: '文章创建' },
  { code: 'article:update',  label: '文章编辑' },
  { code: 'article:delete',  label: '文章删除' },
  { code: 'article:publish', label: '文章发布' },
  { code: 'comment:list',    label: '评论列表' },
  { code: 'comment:approve', label: '评论审核' },
  { code: 'comment:delete',  label: '评论删除' },
  { code: 'category:manage', label: '分类管理' },
  { code: 'tag:manage',      label: '标签管理' },
  { code: 'user:list',       label: '用户列表' },
  { code: 'role:manage',     label: '角色管理' },
  { code: 'menu:manage',     label: '菜单管理' },
]

const form = reactive({ name: '', description: '', permissionCodes: [] })

async function loadRoles() {
  loading.value = true
  try {
    const res = await getRoles()
    roles.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editId.value = null
  form.name = ''
  form.description = ''
  form.permissionCodes = []
  dialogVisible.value = true
}

function openEdit(row) {
  editId.value = row.id
  form.name = row.name
  form.description = row.description || ''
  form.permissionCodes = (row.permissions || []).map(p => p.code)
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.name.trim()) return ElMessage.warning('请输入角色名')
  saving.value = true
  try {
    const payload = { name: form.name.trim(), description: form.description.trim(), permissionCodes: form.permissionCodes }
    if (editId.value) await updateRole(editId.value, payload)
    else await createRole(payload)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadRoles()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteRole(id)
    ElMessage.success('已删除')
    loadRoles()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '删除失败')
  }
}

onMounted(loadRoles)
</script>

<style scoped>
.page-wrap { display: flex; flex-direction: column; gap: 20px; }

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.page-title { margin: 0 0 4px; font-size: 22px; font-weight: 700; color: var(--color-text-primary); }
.page-sub { margin: 0; font-size: 13px; color: var(--color-text-tertiary); }

.data-table { border-radius: var(--radius-lg); overflow: hidden; }

.desc-text { font-size: 13px; color: var(--color-text-secondary); }

.perm-tags { display: flex; flex-wrap: wrap; gap: 4px; }
.perm-tag {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  background: rgba(232,168,56,.1);
  color: #E8A838;
  border: 1px solid rgba(232,168,56,.25);
  border-radius: var(--radius-full);
}
.no-perm { font-size: 12px; color: var(--color-text-tertiary); }

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

.form-body { display: flex; flex-direction: column; gap: 16px; }
.form-item { display: flex; flex-direction: column; gap: 6px; }
.form-label { font-size: 13px; font-weight: 500; color: var(--color-text-primary); }
.required { color: #ff4d4f; }
.perm-check-group { display: flex; flex-wrap: wrap; gap: 8px; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 10px; }
</style>
