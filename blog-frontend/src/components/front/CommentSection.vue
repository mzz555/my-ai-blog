<template>
  <div style="margin-top:40px">
    <h3 style="margin-bottom:16px">评论（{{ comments.length }}）</h3>

    <el-card style="margin-bottom:20px">
      <el-form :model="form" label-position="top">
        <el-row :gutter="12" v-if="!isLoggedIn">
          <el-col :span="12">
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" placeholder="您的昵称（必填）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="邮箱（可选）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="评论内容">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="写下你的想法..." />
        </el-form-item>
        <el-button type="primary" :loading="submitting" @click="submitComment">发表评论</el-button>
      </el-form>
    </el-card>

    <div v-for="comment in comments" :key="comment.id" style="margin-bottom:16px">
      <el-card>
        <div style="display:flex;gap:12px">
          <el-avatar :size="36">{{ (comment.nickname || '').charAt(0).toUpperCase() }}</el-avatar>
          <div style="flex:1">
            <div style="display:flex;justify-content:space-between">
              <strong>{{ comment.nickname }}</strong>
              <span style="color:#bbb;font-size:12px">{{ fromNow(comment.createdAt) }}</span>
            </div>
            <p style="margin:8px 0;color:#606266">{{ comment.content }}</p>
            <div v-for="child in comment.children" :key="child.id"
              style="background:#f5f7fa;padding:8px 12px;border-radius:4px;margin-top:8px">
              <strong style="font-size:13px">{{ child.nickname }}</strong>
              <span style="color:#bbb;font-size:12px;margin-left:8px">{{ fromNow(child.createdAt) }}</span>
              <p style="margin:4px 0;font-size:13px">{{ child.content }}</p>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-empty v-if="!comments.length" description="还没有评论，来发表第一条吧！" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getComments, createComment } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import { fromNow } from '@/utils/format'
import { ElMessage } from 'element-plus'

const props = defineProps({ articleId: Number })
const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

const comments = ref([])
const submitting = ref(false)
const form = ref({ content: '', nickname: '', email: '' })

async function loadComments() {
  const res = await getComments(props.articleId)
  comments.value = res.data
}

async function submitComment() {
  if (!form.value.content.trim()) return ElMessage.warning('请输入评论内容')
  submitting.value = true
  try {
    await createComment(props.articleId, form.value)
    ElMessage.success('评论已提交，待审核后显示')
    form.value = { content: '', nickname: '', email: '' }
  } finally { submitting.value = false }
}

onMounted(loadComments)
</script>
