<template>
  <div class="cs-wrap">
    <h3 class="cs-title">{{ comments.length }} 条评论</h3>

    <!-- 发表评论表单 -->
    <div class="cs-form-card">
      <div class="cs-form-head">
        <div class="cs-avatar cs-avatar--me">{{ meInitial }}</div>
        <span class="cs-form-label">发表评论</span>
      </div>
      <div v-if="!isLoggedIn" class="cs-anon-row">
        <input v-model="form.nickname" class="cs-input" placeholder="昵称（必填）" />
        <input v-model="form.email" class="cs-input" placeholder="邮箱（可选）" />
      </div>
      <textarea
        v-model="form.content"
        class="cs-textarea"
        rows="4"
        placeholder="写下你的想法..."
      ></textarea>
      <div class="cs-form-footer">
        <button class="cs-btn cs-btn--primary" :disabled="submitting" @click="submitComment">
          <span v-if="submitting" class="cs-spin"></span>
          {{ submitting ? '提交中…' : '发表评论' }}
        </button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div v-if="comments.length" class="cs-list">
      <div v-for="comment in comments" :key="comment.id" class="cs-item">
        <div class="cs-avatar">{{ initial(comment.nickname) }}</div>
        <div class="cs-body">
          <div class="cs-meta">
            <span class="cs-name">{{ comment.nickname }}</span>
            <span class="cs-time">{{ fromNow(comment.createdAt) }}</span>
          </div>
          <p class="cs-text">{{ comment.content }}</p>

          <!-- 嵌套回复 -->
          <div v-if="comment.children?.length" class="cs-replies">
            <div v-for="child in comment.children" :key="child.id" class="cs-reply-item">
              <div class="cs-avatar cs-avatar--sm">{{ initial(child.nickname) }}</div>
              <div class="cs-body">
                <div class="cs-meta">
                  <span class="cs-name">{{ child.nickname }}</span>
                  <span class="cs-time">{{ fromNow(child.createdAt) }}</span>
                </div>
                <p class="cs-text">{{ child.content }}</p>
              </div>
            </div>
          </div>

          <!-- 回复按钮 / 回复表单 -->
          <button v-if="replyingTo !== comment.id" class="cs-reply-btn" @click="startReply(comment.id)">
            回复
          </button>
          <div v-else class="cs-inline-form">
            <div v-if="!isLoggedIn" class="cs-anon-row">
              <input v-model="replyForm.nickname" class="cs-input cs-input--sm" placeholder="昵称（必填）" />
              <input v-model="replyForm.email" class="cs-input cs-input--sm" placeholder="邮箱（可选）" />
            </div>
            <textarea
              v-model="replyForm.content"
              class="cs-textarea cs-textarea--sm"
              rows="3"
              placeholder="回复内容…"
            ></textarea>
            <div class="cs-inline-actions">
              <button class="cs-btn cs-btn--primary cs-btn--sm" :disabled="replySubmitting" @click="submitReply">
                {{ replySubmitting ? '提交中…' : '发表回复' }}
              </button>
              <button class="cs-btn cs-btn--ghost cs-btn--sm" @click="cancelReply">取消</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="cs-empty">
      <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
        <circle cx="24" cy="24" r="22" stroke="var(--color-border)" stroke-width="1.5"/>
        <path d="M14 20h20M14 28h12" stroke="var(--color-border)" stroke-width="1.5" stroke-linecap="round"/>
      </svg>
      <p>还没有评论，来发表第一条吧！</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getComments, createComment } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import { fromNow } from '@/utils/format'
import { ElMessage } from 'element-plus'

const props = defineProps({ articleId: Number })
const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)
const meInitial = computed(() => userStore.user?.nickname?.charAt(0).toUpperCase() || '?')

const comments = ref([])
const submitting = ref(false)
const form = ref({ content: '', nickname: '', email: '' })
const replyingTo = ref(null)
const replyForm = ref({ content: '', nickname: '', email: '' })
const replySubmitting = ref(false)

function initial(name) {
  return (name || '?').charAt(0).toUpperCase()
}

async function loadComments() {
  const res = await getComments(props.articleId)
  comments.value = res.data
}

async function submitComment() {
  if (!form.value.content.trim()) return ElMessage.warning('请输入评论内容')
  if (!isLoggedIn.value && !form.value.nickname.trim()) return ElMessage.warning('请输入昵称')
  submitting.value = true
  try {
    await createComment(props.articleId, form.value)
    ElMessage.success('评论已提交，待审核后显示')
    form.value = { content: '', nickname: '', email: '' }
  } finally {
    submitting.value = false
  }
}

function startReply(commentId) {
  replyingTo.value = commentId
  replyForm.value = { content: '', nickname: '', email: '' }
}

function cancelReply() {
  replyingTo.value = null
}

async function submitReply() {
  if (!replyForm.value.content.trim()) return ElMessage.warning('请输入回复内容')
  if (!isLoggedIn.value && !replyForm.value.nickname.trim()) return ElMessage.warning('请输入昵称')
  replySubmitting.value = true
  try {
    await createComment(props.articleId, { ...replyForm.value, parentId: replyingTo.value })
    ElMessage.success('回复已提交，待审核后显示')
    replyingTo.value = null
    replyForm.value = { content: '', nickname: '', email: '' }
    await loadComments()
  } finally {
    replySubmitting.value = false
  }
}

onMounted(loadComments)
</script>

<style scoped>
.cs-wrap {
  margin-top: 48px;
  padding-top: 32px;
  border-top: 1px solid var(--color-border);
}

.cs-title {
  margin: 0 0 24px;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary);
}

/* 表单卡片 */
.cs-form-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 20px;
  margin-bottom: 32px;
}

.cs-form-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.cs-form-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-secondary);
}

/* 头像 */
.cs-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #E8A838;
  color: #0C0C10;
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.cs-avatar--me { background: rgba(232,168,56,.2); color: #E8A838; }
.cs-avatar--sm { width: 28px; height: 28px; font-size: 11px; }

/* 匿名字段行 */
.cs-anon-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 10px;
}

/* 输入框 */
.cs-input {
  width: 100%;
  box-sizing: border-box;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 8px 12px;
  font-size: 13px;
  color: var(--color-text-primary);
  outline: none;
  transition: border-color var(--transition-fast);
}
.cs-input:focus { border-color: #E8A838; }
.cs-input--sm { padding: 6px 10px; font-size: 12px; }

.cs-textarea {
  width: 100%;
  box-sizing: border-box;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 10px 12px;
  font-size: 14px;
  color: var(--color-text-primary);
  resize: vertical;
  outline: none;
  font-family: inherit;
  transition: border-color var(--transition-fast);
  line-height: 1.6;
}
.cs-textarea:focus { border-color: #E8A838; }
.cs-textarea--sm { font-size: 13px; padding: 8px 10px; }

.cs-form-footer { margin-top: 12px; display: flex; justify-content: flex-end; }

/* 按钮 */
.cs-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  padding: 8px 18px;
  transition: opacity var(--transition-fast), transform var(--transition-fast);
}
.cs-btn:disabled { opacity: .5; cursor: not-allowed; }
.cs-btn:not(:disabled):hover { opacity: .88; transform: translateY(-1px); }
.cs-btn--primary { background: #E8A838; color: #0C0C10; }
.cs-btn--ghost { background: transparent; color: var(--color-text-secondary); border: 1px solid var(--color-border); }
.cs-btn--sm { padding: 5px 12px; font-size: 12px; }

/* 旋转加载 */
.cs-spin {
  width: 12px;
  height: 12px;
  border: 2px solid rgba(12,12,16,.3);
  border-top-color: #0C0C10;
  border-radius: 50%;
  animation: spin .6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 评论列表 */
.cs-list { display: flex; flex-direction: column; gap: 24px; }

.cs-item {
  display: flex;
  gap: 14px;
}

.cs-body { flex: 1; min-width: 0; }

.cs-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.cs-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.cs-time {
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.cs-text {
  margin: 0 0 10px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--color-text-secondary);
  white-space: pre-wrap;
  word-break: break-word;
}

/* 嵌套回复区 */
.cs-replies {
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 12px 16px;
  margin-bottom: 10px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.cs-reply-item {
  display: flex;
  gap: 10px;
}

/* 回复按钮 */
.cs-reply-btn {
  background: none;
  border: none;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-tertiary);
  cursor: pointer;
  padding: 0;
  transition: color var(--transition-fast);
}
.cs-reply-btn:hover { color: #E8A838; }

/* 内联回复表单 */
.cs-inline-form {
  margin-top: 10px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 14px;
}

.cs-inline-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  justify-content: flex-end;
}

/* 空状态 */
.cs-empty {
  text-align: center;
  padding: 48px 0;
  color: var(--color-text-tertiary);
  font-size: 14px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
</style>
