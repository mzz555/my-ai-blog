<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2>文章管理</h2>
      <el-button type="primary" @click="$router.push('/admin/articles/new')">写新文章</el-button>
    </div>
    <el-table :data="articles" v-loading="loading" stripe>
      <el-table-column prop="title" label="标题" min-width="200">
        <template #default="{ row }">
          <router-link :to="`/admin/articles/${row.id}/edit`" style="color:#409eff">{{ row.title }}</router-link>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="viewCount" label="阅读量" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'" size="small">
            {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="130">
        <template #default="{ row }">{{ row.publishedAt ? formatDate(row.publishedAt) : '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button text size="small" @click="$router.push(`/admin/articles/${row.id}/edit`)">编辑</el-button>
          <el-button text size="small" @click="handlePublish(row)">
            {{ row.status === 'PUBLISHED' ? '撤回' : '发布' }}
          </el-button>
          <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button text size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <div style="margin-top:16px;text-align:right">
      <el-pagination background layout="total,prev,pager,next" :total="total"
        :page-size="pageSize" :current-page="page" @current-change="loadArticles" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminArticles, togglePublish, deleteArticle } from '@/api/article'
import { formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'

const articles = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)

async function loadArticles(p = 1) {
  loading.value = true
  page.value = p
  try {
    const res = await getAdminArticles({ page: p, size: pageSize })
    articles.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
}

async function handlePublish(row) {
  await togglePublish(row.id)
  ElMessage.success(row.status === 'PUBLISHED' ? '已撤回' : '已发布')
  loadArticles(page.value)
}

async function handleDelete(id) {
  await deleteArticle(id)
  ElMessage.success('已删除')
  loadArticles(page.value)
}

onMounted(() => loadArticles())
</script>
