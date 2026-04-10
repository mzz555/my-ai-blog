<template>
  <div style="max-width:860px;margin:0 auto" v-if="article">
    <h1 style="font-size:28px;line-height:1.4;margin-bottom:12px">{{ article.title }}</h1>
    <div style="color:#909399;font-size:13px;display:flex;gap:16px;margin-bottom:24px">
      <span>{{ formatDateTime(article.publishedAt) }}</span>
      <span>👁 {{ article.viewCount }}</span>
      <span v-if="article.categoryName">📂 {{ article.categoryName }}</span>
    </div>
    <el-image v-if="article.coverImage" :src="article.coverImage"
      style="width:100%;max-height:400px;border-radius:8px;margin-bottom:24px" fit="cover" />

    <MdEditor v-model="article.content" previewOnly style="border:none" />

    <div style="margin-top:24px" v-if="article.tagNames?.length">
      <el-tag v-for="tag in article.tagNames" :key="tag" style="margin-right:8px">
        #{{ tag }}
      </el-tag>
    </div>

    <el-divider />
    <CommentSection :article-id="article.id" />
  </div>
  <el-skeleton v-else :rows="10" animated />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { getArticleBySlug, recordView } from '@/api/article'
import { formatDateTime } from '@/utils/format'
import CommentSection from '@/components/front/CommentSection.vue'

const route = useRoute()
const article = ref(null)

onMounted(async () => {
  const res = await getArticleBySlug(route.params.slug)
  article.value = res.data
  recordView(article.value.id)
})
</script>
