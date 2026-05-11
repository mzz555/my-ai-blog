<template>
  <AdminPageCard title="用户管理" :subtitle="`共 ${total} 位用户`">
    <template #actions>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon> 新建用户
      </el-button>
    </template>

    <template #filter>
      <el-input
        v-model="keyword"
        placeholder="搜索用户名 / 昵称"
        clearable
        class="search-input"
        @input="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </template>

    <DataTable
      :data="users"
      :loading="loading"
      :total="total"
      :page="page"
      :page-size="pageSize"
      @page-change="fetchUsers"
    >
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
    </DataTable>

    <el-dialog v-model="createVisible" title="新建用户" width="480px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" placeholder="3-50 位字母/数字/下划线/连字符" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="createForm.email" placeholder="user@example.com" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" show-password placeholder="6-50 位" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="createForm.nickname" placeholder="留空时使用用户名" />
        </el-form-item>
        <el-form-item label="分配角色">
          <el-checkbox-group v-model="createForm.roleIds" class="role-check-group">
            <el-checkbox v-for="r in allRoles" :key="r.id" :label="r.id">{{ r.name }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="createForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createVisible = false">取消</el-button>
          <el-button type="primary" :loading="createSaving" @click="handleCreate">创建</el-button>
        </div>
      </template>
    </el-dialog>

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
  </AdminPageCard>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUsers, updateUserStatus, updateUser, createUser } from '@/api/user'
import { getRoles } from '@/api/role'
import { ElMessage } from 'element-plus'
import { Search, Edit, Plus } from '@element-plus/icons-vue'
import AdminPageCard from '@/components/admin/AdminPageCard.vue'
import DataTable from '@/components/admin/DataTable.vue'

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

const createVisible = ref(false)
const createSaving = ref(false)
const createFormRef = ref(null)
const createForm = reactive({
  username: '',
  email: '',
  password: '',
  nickname: '',
  roleIds: [],
  status: 1,
})
const createRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名 3-50 位', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: '只能包含字母、数字、下划线、连字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码 6-50 位', trigger: 'blur' },
  ],
}

function openCreate() {
  createForm.username = ''
  createForm.email = ''
  createForm.password = ''
  createForm.nickname = ''
  createForm.roleIds = []
  createForm.status = 1
  createVisible.value = true
}

async function handleCreate() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  createSaving.value = true
  try {
    await createUser({
      username: createForm.username.trim(),
      email: createForm.email.trim(),
      password: createForm.password,
      nickname: createForm.nickname.trim() || null,
      roleIds: createForm.roleIds,
      status: createForm.status,
    })
    ElMessage.success('用户已创建')
    createVisible.value = false
    fetchUsers(1)
  } finally {
    createSaving.value = false
  }
}

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
.search-input { width: 220px; }

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

.form-body { display: flex; flex-direction: column; gap: 16px; }
.form-item { display: flex; flex-direction: column; gap: 6px; }
.form-label { font-size: 13px; font-weight: 500; color: var(--color-text-primary); }
.role-check-group { display: flex; flex-wrap: wrap; gap: 8px; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 10px; }

:deep(.data-table .el-table__row) { height: 60px; }
</style>
