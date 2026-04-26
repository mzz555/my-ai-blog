<template>
  <div class="page-wrap">
    <div class="page-head">
      <div>
        <h2 class="page-title">个人资料</h2>
        <p class="page-sub">管理你的账户信息</p>
      </div>
    </div>

    <div class="profile-card">
      <!-- 头像行 -->
      <div class="avatar-row">
        <div class="avatar-block">{{ userInitial }}</div>
        <div class="avatar-info">
          <div class="avatar-name">{{ form.username }}</div>
          <div class="avatar-sub">{{ form.email || '暂无邮箱' }}</div>
        </div>
      </div>

      <div class="divider" />

      <!-- 表单 -->
      <div class="form-body">
        <div class="form-item">
          <label class="form-label">用户名</label>
          <el-input :value="form.username" disabled />
          <p class="form-hint">用户名不可修改</p>
        </div>
        <div class="form-item">
          <label class="form-label">邮箱</label>
          <el-input :value="form.email" disabled />
        </div>
        <div class="form-item">
          <label class="form-label">个人简介</label>
          <el-input
            v-model="form.bio"
            type="textarea"
            :rows="4"
            placeholder="介绍一下自己..."
          />
        </div>
        <div class="form-actions">
          <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { updateProfile } from '@/api/auth'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const form = reactive({ username: '', email: '', bio: '' })
const saving = ref(false)

const userInitial = computed(() => (form.username || 'A').charAt(0).toUpperCase())

async function handleSave() {
  saving.value = true
  try {
    await updateProfile({ bio: form.bio })
    ElMessage.success('保存成功')
    await userStore.fetchUserInfo()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  const info = userStore.userInfo
  if (info) Object.assign(form, { username: info.username, email: info.email || '', bio: info.bio || '' })
})
</script>

<style scoped>
.page-wrap { display: flex; flex-direction: column; gap: 20px; max-width: 560px; }

.page-head { display: flex; flex-direction: column; gap: 4px; }
.page-title { margin: 0; font-size: 22px; font-weight: 700; color: var(--color-text-primary); }
.page-sub { margin: 0; font-size: 13px; color: var(--color-text-tertiary); }

.profile-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
}

.avatar-block {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #E8A838;
  color: #0C0C10;
  font-size: 22px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.avatar-sub {
  font-size: 13px;
  color: var(--color-text-tertiary);
  margin-top: 2px;
}

.divider {
  height: 1px;
  background: var(--color-border);
}

.form-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 24px;
}

.form-item { display: flex; flex-direction: column; gap: 6px; }

.form-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.form-hint {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.form-actions { display: flex; padding-top: 4px; }
</style>
