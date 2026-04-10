<template>
  <div style="display:flex;gap:24px">
    <div style="flex:1">
      <el-skeleton v-if="loading" :rows="5" animated />
      <template v-else>
        <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
        <el-empty v-if="!articles.length" description="暂无文章" />
        <div style="text-align:center;margin-top:16px">
          <el-pagination v-if="total > pageSize" background layout="prev,pager,next"
            :total="total" :page-size="pageSize" :current-page="page"
            @current-change="loadArticles" />
        </div>
      </template>
    </div>
    <div style="width:260px;flex-shrink:0">
      <el-card style="margin-bottom:16px">
        <template #header><span>分类</span></template>
        <div v-for="cat in categories" :key="cat.id" style="margin-bottom:8px">
          <router-link :to="`/category/${cat.slug}`" style="color:#606266;text-decoration:none">
            {{ cat.name }}
          </router-link>
        </div>
        <el-empty v-if="!categories.length" description="暂无分类" :image-size="40" />
      </el-card>
      <el-card>
        <template #header><span>标签</span></template>
        <el-tag v-for="tag in tags" :key="tag.id" style="margin:4px;cursor:pointer"
          @click="$router.push(`/tag/${tag.slug}`)">
          {{ tag.name }}
        </el-tag>
        <el-empty v-if="!tags.length" description="暂无标签" :image-size="40" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getArticles } from '@/api/article'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import ArticleCard from '@/components/front/ArticleCard.vue'

const articles = ref([])
const categories = ref([])
const tags = ref([])
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
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  loadArticles()
  const [catRes, tagRes] = await Promise.all([getCategories(), getTags()])
  categories.value = catRes.data
  tags.value = tagRes.data
})
</script>
