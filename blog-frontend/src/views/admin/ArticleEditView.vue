<template>
  <div>
    <h2 style="margin-bottom:20px">{{ isEdit ? '编辑文章' : '写新文章' }}</h2>
    <el-form :model="form" label-width="80px">
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="文章标题" size="large" />
      </el-form-item>
      <el-row :gutter="12">
        <el-col :span="8">
          <el-form-item label="分类">
            <el-select v-model="form.categoryId" clearable placeholder="选择分类" style="width:100%">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="标签">
            <el-select v-model="form.tagNames" multiple filterable allow-create
              default-first-option placeholder="输入标签" style="width:100%">
              <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.name" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="状态">
            <el-select v-model="form.status" style="width:100%">
              <el-option label="草稿" value="DRAFT" />
              <el-option label="发布" value="PUBLISHED" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="摘要">
        <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="文章摘要（SEO）" />
      </el-form-item>
      <el-form-item label="内容">
        <MdEditor v-model="form.content" style="width:100%" @onUploadImg="handleUpload" />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="form.isTop">置顶</el-checkbox>
        <el-checkbox v-model="form.allowComment" style="margin-left:16px">允许评论</el-checkbox>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        <el-button style="margin-left:12px" @click="$router.push('/admin/articles')">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { createArticle, updateArticle } from '@/api/article'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import { uploadImage } from '@/api/upload'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const saving = ref(false)
const categories = ref([])
const tags = ref([])

const form = reactive({
  title: '', slug: '', summary: '', content: '', coverImage: '',
  status: 'DRAFT', categoryId: null, tagNames: [], isTop: false, allowComment: true,
})

async function handleUpload(files, callback) {
  const results = await Promise.all(files.map(async f => {
    const res = await uploadImage(f)
    return res.data
  }))
  callback(results)
}

async function handleSave() {
  if (!form.title) return ElMessage.warning('请输入标题')
  if (!form.content) return ElMessage.warning('请输入内容')
  saving.value = true
  try {
    if (isEdit.value) {
      await updateArticle(route.params.id, form)
      ElMessage.success('保存成功')
    } else {
      await createArticle(form)
      ElMessage.success('发布成功')
      router.push('/admin/articles')
    }
  } finally { saving.value = false }
}

onMounted(async () => {
  const [catRes, tagRes] = await Promise.all([getCategories(), getTags()])
  categories.value = catRes.data
  tags.value = tagRes.data
})
</script>
