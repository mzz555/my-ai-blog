<template>
  <div>
    <h2>分类：{{ $route.params.slug }}</h2>
    <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
    <el-empty v-if="!articles.length" description="该分类暂无文章" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getCategories } from '@/api/category'
import { getArticles } from '@/api/article'
import ArticleCard from '@/components/front/ArticleCard.vue'

const route = useRoute()
const articles = ref([])

async function load() {
  const catRes = await getCategories()
  const cat = catRes.data.find(c => c.slug === route.params.slug)
  if (!cat) return
  const res = await getArticles({ categoryId: cat.id, page: 1, size: 20 })
  articles.value = res.data.list
}

onMounted(load)
watch(() => route.params.slug, load)
</script>
