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
      <el-form-item label="封面图">
        <div style="display:flex;align-items:center;gap:12px">
          <img v-if="form.coverImage" :src="form.coverImage"
            style="width:120px;height:70px;object-fit:cover;border-radius:4px;border:1px solid #2A2A3C" />
          <el-upload :before-upload="handleCoverUpload" :show-file-list="false" accept="image/*">
            <el-button :loading="coverUploading" size="small">
              {{ form.coverImage ? '更换封面' : '上传封面' }}
            </el-button>
          </el-upload>
          <el-button v-if="form.coverImage" size="small" @click="form.coverImage = ''">移除</el-button>
        </div>
      </el-form-item>
      <el-form-item label="Slug">
        <el-input v-model="form.slug" placeholder="URL 路径（自动生成，可手动修改）" />
      </el-form-item>
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { createArticle, updateArticle, getArticleById } from '@/api/article'
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
const coverUploading = ref(false)

const form = reactive({
  title: '', slug: '', summary: '', content: '', coverImage: '',
  status: 'DRAFT', categoryId: null, tagNames: [], isTop: false, allowComment: true,
})

watch(() => form.title, (val) => {
  if (!isEdit.value && !form.slug) {
    form.slug = val.toLowerCase()
      .replace(/\s+/g, '-')
      .replace(/[^\w\-]/g, '')
      .slice(0, 100)
  }
})

async function handleUpload(files, callback) {
  const results = await Promise.all(files.map(async f => {
    const res = await uploadImage(f)
    return res.data
  }))
  callback(results)
}

async function handleCoverUpload(file) {
  coverUploading.value = true
  try {
    const res = await uploadImage(file.raw || file)
    form.coverImage = res.data
  } catch {
    ElMessage.error('封面上传失败')
  } finally {
    coverUploading.value = false
  }
  return false
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
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const [catRes, tagRes] = await Promise.all([getCategories(), getTags()])
  categories.value = catRes.data
  tags.value = tagRes.data

  if (isEdit.value) {
    try {
      const res = await getArticleById(route.params.id)
      const a = res.data
      Object.assign(form, {
        title: a.title,
        slug: a.slug,
        summary: a.summary || '',
        content: a.content || '',
        coverImage: a.coverImage || '',
        status: a.status || 'DRAFT',
        categoryId: a.categoryId || null,
        tagNames: a.tagNames || [],
        isTop: a.isTop || false,
        allowComment: a.allowComment !== false,
      })
    } catch {
      ElMessage.error('加载文章失败')
    }
  }
})
</script>
