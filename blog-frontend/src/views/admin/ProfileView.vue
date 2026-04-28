<template>
  <div class="page-wrap">
    <div class="page-head">
      <div>
        <h2 class="page-title">个人资料</h2>
        <p class="page-sub">管理你的账户信息与头像</p>
      </div>
    </div>

    <div class="profile-card">
      <!-- 顶部：头像 + 基本信息 -->
      <div class="profile-top">
        <div class="avatar-zone" title="点击更换头像" @click="triggerUpload">
          <img v-if="form.avatar" :src="form.avatar" class="avatar-img" alt="头像" />
          <div v-else class="avatar-initial">{{ userInitial }}</div>
          <div class="avatar-overlay">
            <svg class="camera-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
              <circle cx="12" cy="13" r="4"/>
            </svg>
          </div>
          <div v-if="avatarUploading" class="avatar-loading">
            <span class="spinner" />
          </div>
          <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleAvatarChange" />
        </div>

        <div class="profile-meta">
          <div class="profile-name">{{ form.username }}</div>
          <div class="profile-email">{{ form.email || '暂无邮箱' }}</div>
          <div v-if="roleLabel" class="role-badge">{{ roleLabel }}</div>
        </div>
      </div>

      <div class="divider" />

      <!-- 表单 -->
      <div class="form-body">
        <div class="form-row">
          <div class="form-item">
            <label class="form-label">用户名</label>
            <input :value="form.username" disabled class="form-input" />
            <p class="form-hint">用户名不可修改</p>
          </div>
          <div class="form-item">
            <label class="form-label">邮箱</label>
            <input :value="form.email || '暂未绑定'" disabled class="form-input" />
          </div>
        </div>

        <div class="form-item">
          <label class="form-label">个人简介</label>
          <textarea
            v-model="form.bio"
            class="form-textarea"
            rows="4"
            placeholder="介绍一下自己..."
          />
        </div>

        <div class="form-actions">
          <button
            class="btn-save"
            :disabled="saving || avatarUploading"
            @click="handleSave"
          >
            {{ saving ? '保存中…' : '保存修改' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { updateProfile } from '@/api/auth'
import { uploadImage } from '@/api/upload'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const form = reactive({ username: '', email: '', bio: '', avatar: '' })
const saving = ref(false)
const avatarUploading = ref(false)
const fileInput = ref(null)

const userInitial = computed(() => (form.username || 'A').charAt(0).toUpperCase())
const roleLabel = computed(() => {
  const roles = userStore.userInfo?.roles || []
  if (roles.includes('ADMIN')) return '管理员'
  if (roles.includes('USER')) return '普通用户'
  return roles[0] || ''
})

function triggerUpload() {
  fileInput.value?.click()
}

async function handleAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  avatarUploading.value = true
  try {
    const res = await uploadImage(file)
    form.avatar = res.data
  } catch {
    ElMessage.error('头像上传失败，请重试')
  } finally {
    avatarUploading.value = false
    e.target.value = ''
  }
}

async function handleSave() {
  saving.value = true
  try {
    await updateProfile({ bio: form.bio, avatar: form.avatar || null })
    ElMessage.success('保存成功')
    await userStore.fetchUserInfo()
  } catch {
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  const info = userStore.userInfo
  if (info) Object.assign(form, {
    username: info.username || '',
    email: info.email || '',
    bio: info.bio || '',
    avatar: info.avatar || '',
  })
})
</script>

<style scoped>
.page-wrap { display: flex; flex-direction: column; gap: 20px; max-width: 640px; }

.page-head { display: flex; flex-direction: column; gap: 4px; }
.page-title { margin: 0; font-size: 22px; font-weight: 700; color: var(--color-text-primary); }
.page-sub { margin: 0; font-size: 13px; color: var(--color-text-tertiary); }

/* 卡片 */
.profile-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

/* 顶部区域 */
.profile-top {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 28px 28px 24px;
}

/* 头像 */
.avatar-zone {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  flex-shrink: 0;
  cursor: pointer;
  overflow: hidden;
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
  display: block;
}
.avatar-initial {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: #E8A838;
  color: #0C0C10;
  font-size: 28px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  user-select: none;
}
.avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}
.avatar-zone:hover .avatar-overlay { opacity: 1; }
.camera-icon {
  width: 24px;
  height: 24px;
  color: #fff;
}
.avatar-loading {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
}
.spinner {
  display: inline-block;
  width: 22px;
  height: 22px;
  border: 3px solid rgba(255,255,255,.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 基本信息 */
.profile-meta { display: flex; flex-direction: column; gap: 4px; }
.profile-name { font-size: 18px; font-weight: 700; color: var(--color-text-primary); }
.profile-email { font-size: 13px; color: var(--color-text-tertiary); }
.role-badge {
  display: inline-flex;
  align-items: center;
  margin-top: 4px;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 600;
  background: rgba(232,168,56,.12);
  color: #E8A838;
  border: 1px solid rgba(232,168,56,.25);
  width: fit-content;
}

.divider { height: 1px; background: var(--color-border); }

/* 表单 */
.form-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 24px 28px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-item { display: flex; flex-direction: column; gap: 6px; }

.form-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.form-hint {
  margin: 0;
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.form-input {
  height: 38px;
  padding: 0 12px;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-primary);
  font-size: 14px;
  outline: none;
  transition: border-color var(--transition-fast);
  width: 100%;
  box-sizing: border-box;
}
.form-input:disabled {
  background: var(--color-bg-secondary);
  color: var(--color-text-tertiary);
  cursor: not-allowed;
}

.form-textarea {
  padding: 10px 12px;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-primary);
  font-size: 14px;
  outline: none;
  resize: vertical;
  transition: border-color var(--transition-fast);
  width: 100%;
  box-sizing: border-box;
  font-family: inherit;
  line-height: 1.6;
}
.form-textarea:focus { border-color: var(--color-accent); }

.form-actions { display: flex; justify-content: flex-end; padding-top: 4px; }

.btn-save {
  padding: 9px 28px;
  background: var(--color-accent);
  color: #0C0C10;
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background var(--transition-fast);
}
.btn-save:hover:not(:disabled) { background: #F5BC50; }
.btn-save:disabled { opacity: 0.6; cursor: not-allowed; }

@media (max-width: 480px) {
  .profile-top { padding: 20px; }
  .form-body { padding: 20px; }
  .form-row { grid-template-columns: 1fr; }
}
</style>
