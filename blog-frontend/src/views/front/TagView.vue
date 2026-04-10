<template>
  <div>
    <h2>标签：#{{ $route.params.slug }}</h2>
    <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
    <el-empty v-if="!articles.length" description="该标签暂无文章" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getArticles } from '@/api/article'
import ArticleCard from '@/components/front/ArticleCard.vue'

const route = useRoute()
const articles = ref([])

async function load() {
  const res = await getArticles({ tagSlug: route.params.slug, page: 1, size: 20 })
  articles.value = res.data.list
}

onMounted(load)
watch(() => route.params.slug, load)
</script>
