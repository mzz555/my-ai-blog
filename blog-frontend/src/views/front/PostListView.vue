<template>
  <div>
    <h2 style="margin-bottom:20px">所有文章</h2>
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else>
      <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
      <el-empty v-if="!articles.length" description="暂无文章" />
      <div style="text-align:center;margin-top:16px">
        <el-pagination v-if="total > pageSize" background layout="prev,pager,next"
          :total="total" :page-size="pageSize" :current-page="page"
          @current-change="(p) => loadArticles(p)" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getArticles } from '@/api/article'
import ArticleCard from '@/components/front/ArticleCard.vue'

const articles = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)

async function loadArticles(p = 1) {
  loading.value = true
  page.value = p
  try {
    const res = await getArticles({ page: p, size: pageSize })
    articles.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
}

onMounted(() => loadArticles())
</script>
