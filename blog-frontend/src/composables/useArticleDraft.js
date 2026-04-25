import { watch, onMounted, onUnmounted } from 'vue'
import { ElMessageBox } from 'element-plus'

const DRAFT_KEY = 'article_draft'

export function useArticleDraft(form, isEdit) {
  if (isEdit.value) return

  let timer = null

  watch(
    form,
    (val) => {
      clearTimeout(timer)
      timer = setTimeout(() => {
        localStorage.setItem(DRAFT_KEY, JSON.stringify({
          ...val,
          savedAt: new Date().toISOString(),
        }))
      }, 300)
    },
    { deep: true }
  )

  onMounted(async () => {
    const raw = localStorage.getItem(DRAFT_KEY)
    if (!raw) return
    let draft
    try {
      draft = JSON.parse(raw)
    } catch {
      localStorage.removeItem(DRAFT_KEY)
      return
    }
    const time = draft.savedAt
      ? new Date(draft.savedAt).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      : '未知时间'
    try {
      await ElMessageBox.confirm(
        `检测到未保存的草稿（${time}），是否恢复？`,
        '恢复草稿',
        { confirmButtonText: '恢复', cancelButtonText: '忽略', type: 'info' }
      )
      const { savedAt, ...data } = draft
      Object.assign(form, data)
    } catch {
      // 用户点取消，保留草稿供下次访问
    }
  })

  onUnmounted(() => clearTimeout(timer))
}

export function clearDraft() {
  localStorage.removeItem(DRAFT_KEY)
}
