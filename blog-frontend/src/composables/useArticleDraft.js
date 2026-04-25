import { watch, onMounted } from 'vue'
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
    try {
      const draft = JSON.parse(raw)
      const time = new Date(draft.savedAt).toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
      })
      await ElMessageBox.confirm(
        `检测到未保存的草稿（${time}），是否恢复？`,
        '恢复草稿',
        { confirmButtonText: '恢复', cancelButtonText: '忽略', type: 'info' }
      )
      const { savedAt, ...data } = draft
      Object.assign(form, data)
    } catch {
      // 用户点取消或 JSON 解析失败，清除草稿
      localStorage.removeItem(DRAFT_KEY)
    }
  })
}

export function clearDraft() {
  localStorage.removeItem(DRAFT_KEY)
}
