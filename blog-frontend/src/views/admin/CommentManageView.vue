<template>
  <div>
    <h2 style="margin-bottom:20px">评论审核</h2>
    <el-table :data="comments" v-loading="loading" stripe>
      <el-table-column label="评论者" width="100">
        <template #default="{ row }">{{ row.nickname }}</template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="200" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="130">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" text size="small" type="success"
            @click="changeStatus(row.id, 'APPROVED')">通过</el-button>
          <el-button v-if="row.status !== 'REJECTED'" text size="small" type="warning"
            @click="changeStatus(row.id, 'REJECTED')">拒绝</el-button>
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
        :page-size="10" :current-page="page" @current-change="load" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminComments, updateCommentStatus, deleteComment } from '@/api/comment'
import { formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'

const comments = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)

async function load(p = 1) {
  loading.value = true
  page.value = p
  try {
    const res = await getAdminComments({ page: p, size: 10 })
    comments.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
}

async function changeStatus(id, status) {
  await updateCommentStatus(id, status)
  ElMessage.success('已更新')
  load(page.value)
}

async function handleDelete(id) {
  await deleteComment(id)
  ElMessage.success('已删除')
  load(page.value)
}

const statusTag = (s) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s])
const statusText = (s) => ({ PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }[s])

onMounted(() => load())
</script>
