import { computed } from 'vue'

export function useWordCount(content) {
  const wordCount = computed(() => {
    const md = content.value || ''
    const plain = md
      .replace(/```[\s\S]*?```/g, '')
      .replace(/`[^`]+`/g, '')
      .replace(/!?\[.*?\]\(.*?\)/g, '')
      .replace(/#{1,6}\s/g, '')
      .replace(/[*_~>]/g, '')
    const zh = (plain.match(/[一-龥]/g) || []).length
    const en = (plain.match(/\b[a-zA-Z]+\b/g) || []).length
    return zh + en
  })

  const readingMinutes = computed(() => Math.ceil(wordCount.value / 300))

  return { wordCount, readingMinutes }
}
