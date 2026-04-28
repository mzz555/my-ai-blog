<template>
  <div class="page-wrap">
    <div class="page-head">
      <div>
        <h2 class="page-title">用户管理</h2>
        <p class="page-sub">共 {{ total }} 位用户</p>
      </div>
      <el-input
        v-model="keyword"
        placeholder="搜索用户名 / 昵称"
        clearable
        class="search-input"
        @input="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </div>

    <el-table :data="users" v-loading="loading" class="data-table">
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column label="用户" min-width="160">
        <template #default="{ row }">
          <div class="user-cell">
            <div class="user-avatar">{{ (row.username || '?').charAt(0).toUpperCase() }}</div>
            <div>
              <div class="username">{{ row.username }}</div>
              <div class="nickname-sub">{{ row.nickname }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="180">
        <template #default="{ row }">
          <span class="email-text">{{ row.email || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="角色" min-width="160">
        <template #default="{ row }">
          <div class="role-tags">
            <span v-for="r in row.roles" :key="r.id" class="role-tag">{{ r.name }}</span>
            <span v-if="!row.roles?.length" class="no-role">无角色</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            @change="(val) => toggleStatus(row, val)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <div class="act-group">
            <button class="act-btn act-btn--edit" @click="openEdit(row)">
              <el-icon><Edit /></el-icon> 编辑
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
        @current-change="fetchUsers"
      />
    </div>

    <el-dialog v-model="editVisible" title="编辑用户" width="440px" destroy-on-close>
      <div class="form-body">
        <div class="form-item">
          <label class="form-label">用户名</label>
          <el-input :value="editForm.username" disabled />
        </div>
        <div class="form-item">
          <label class="form-label">昵称</label>
          <el-input v-model="editForm.nickname" placeholder="显示昵称" />
        </div>
        <div class="form-item">
          <label class="form-label">分配角色</label>
          <el-checkbox-group v-model="editForm.roleIds" class="role-check-group">
            <el-checkbox v-for="r in allRoles" :key="r.id" :label="r.id">{{ r.name }}</el-checkbox>
          </el-checkbox-group>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSaveUser">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUsers, updateUserStatus, updateUser } from '@/api/user'
import { getRoles } from '@/api/role'
import { ElMessage } from 'element-plus'
import { Search, Edit } from '@element-plus/icons-vue'

const users = ref([])
const allRoles = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 15
const loading = ref(false)
const keyword = ref('')
const editVisible = ref(false)
const saving = ref(false)
const editForm = reactive({ id: null, username: '', nickname: '', roleIds: [] })

let searchTimer = null
function handleSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { page.value = 1; fetchUsers(1) }, 400)
}

async function fetchUsers(p = page.value) {
  page.value = p
  loading.value = true
  try {
    const res = await getUsers({ page: p, size: pageSize, keyword: keyword.value })
    users.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function toggleStatus(row, enabled) {
  const newStatus = enabled ? 1 : 0
  try {
    await updateUserStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(enabled ? '已启用' : '已禁用')
  } catch {
  }
}

function openEdit(row) {
  editForm.id = row.id
  editForm.username = row.username
  editForm.nickname = row.nickname || ''
  editForm.roleIds = (row.roles || []).map(r => r.id)
  editVisible.value = true
}

async function handleSaveUser() {
  saving.value = true
  try {
    await updateUser(editForm.id, { nickname: editForm.nickname, roleIds: editForm.roleIds })
    ElMessage.success('保存成功')
    editVisible.value = false
    fetchUsers(page.value)
  } catch {
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  fetchUsers(1)
  const res = await getRoles()
  allRoles.value = res.data
})
</script>

<style scoped>
.page-wrap { display: flex; flex-direction: column; gap: 20px; }

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.page-title { margin: 0 0 4px; font-size: 22px; font-weight: 700; color: var(--color-text-primary); }
.page-sub { margin: 0; font-size: 13px; color: var(--color-text-tertiary); }
.search-input { width: 220px; }
.data-table { border-radius: var(--radius-lg); overflow: hidden; }

.user-cell { display: flex; align-items: center; gap: 10px; }

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #E8A838;
  color: #0C0C10;
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.username { font-size: 14px; font-weight: 500; color: var(--color-text-primary); }
.nickname-sub { font-size: 12px; color: var(--color-text-tertiary); margin-top: 1px; }
.email-text { font-size: 13px; color: var(--color-text-secondary); }

.role-tags { display: flex; flex-wrap: wrap; gap: 4px; }

.role-tag {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  background: rgba(232,168,56,.1);
  color: #E8A838;
  border: 1px solid rgba(232,168,56,.25);
  border-radius: var(--radius-full);
}

.no-role { font-size: 12px; color: var(--color-text-tertiary); }

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
.act-btn--edit { background: var(--color-bg-secondary); border-color: var(--color-border); color: var(--color-text-secondary); }

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 12px 0 4px;
  border-top: 1px solid var(--color-border);
}

.form-body { display: flex; flex-direction: column; gap: 16px; }
.form-item { display: flex; flex-direction: column; gap: 6px; }
.form-label { font-size: 13px; font-weight: 500; color: var(--color-text-primary); }
.role-check-group { display: flex; flex-wrap: wrap; gap: 8px; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 10px; }

:deep(.el-table__row) { height: 60px; }
</style>
