# 作者体验功能 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为文章编辑页添加 localStorage 草稿自动保存、字数/阅读时间统计、封面 16:9 裁剪弹窗，并将前台详情页从 MdEditor 换成 MdPreview 只读渲染。

**Architecture:** 新建 `useArticleDraft` / `useWordCount` 两个 composable 处理纯逻辑，新建 `CoverCropDialog.vue` 封装裁剪弹窗，`ArticleEditView` 组合三者；`PostDetailView` 单独修改 import 和组件名。

**Tech Stack:** Vue 3.4 / md-editor-v3 / cropperjs / localStorage / Element Plus

---

## 现状说明（无需重新实现）

- `blog-frontend/src/views/admin/ArticleEditView.vue` — 现有编辑器，166 行，使用 `MdEditor`、`el-upload` 直传封面，无草稿/字数功能
- `blog-frontend/src/views/front/PostDetailView.vue` — 第 46 行 `<MdEditor v-model="article.content" previewOnly />`，第 140 行 `import { MdEditor } from 'md-editor-v3'`
- `blog-frontend/src/api/upload.js` — `uploadImage(file)` 接受 File 或 Blob，返回 `res.data`（URL 字符串）

---

## 文件变更总览

### 新建
- `blog-frontend/src/composables/useWordCount.js`
- `blog-frontend/src/composables/useArticleDraft.js`
- `blog-frontend/src/components/admin/CoverCropDialog.vue`

### 修改
- `blog-frontend/package.json`（添加 cropperjs）
- `blog-frontend/src/views/admin/ArticleEditView.vue`
- `blog-frontend/src/views/front/PostDetailView.vue`

---

## Task 1：安装 cropperjs 依赖

**Files:**
- Modify: `blog-frontend/package.json`

- [ ] **Step 1: 安装依赖**

```bash
cd blog-frontend
npm install cropperjs
```

期望输出：`added 1 package`，无 error。

- [ ] **Step 2: 验证**

打开 `blog-frontend/package.json`，确认 `"dependencies"` 里有 `"cropperjs"` 条目。

- [ ] **Step 3: 提交**

```bash
git add blog-frontend/package.json blog-frontend/package-lock.json
git commit -m "chore: 安装 cropperjs 依赖"
```

---

## Task 2：useWordCount composable

**Files:**
- Create: `blog-frontend/src/composables/useWordCount.js`

- [ ] **Step 1: 创建文件**

新建 `blog-frontend/src/composables/useWordCount.js`：

```js
import { computed } from 'vue'

export function useWordCount(content) {
  const wordCount = computed(() => {
    const md = content.value || ''
    const plain = md
      .replace(/```[\s\S]*?```/g, '')
      .replace(/`[^`]+`/g, '')
      .replace(/!?\[.*?\]\(.*?\)/g, '')
      .replace(/#{1,6}\s/g, '')
      .replace(/[*_~>]/g, '')
    const zh = (plain.match(/[一-龥]/g) || []).length
    const en = (plain.match(/\b[a-zA-Z]+\b/g) || []).length
    return zh + en
  })

  const readingMinutes = computed(() => Math.ceil(wordCount.value / 300))

  return { wordCount, readingMinutes }
}
```

- [ ] **Step 2: 在浏览器控制台验证正则逻辑**

打开任意页面的 DevTools Console，粘贴：

```js
const md = '# 标题\n这是一段**中文**内容，包含 hello world 英文。\n```\nconst x = 1\n```'
const plain = md
  .replace(/```[\s\S]*?```/g, '')
  .replace(/`[^`]+`/g, '')
  .replace(/!?\[.*?\]\(.*?\)/g, '')
  .replace(/#{1,6}\s/g, '')
  .replace(/[*_~>]/g, '')
console.log('zh:', (plain.match(/[一-龥]/g) || []).length)  // 期望 12
console.log('en:', (plain.match(/\b[a-zA-Z]+\b/g) || []).length)    // 期望 2
```

- [ ] **Step 3: 提交**

```bash
git add blog-frontend/src/composables/useWordCount.js
git commit -m "feat: 新增 useWordCount composable"
```

---

## Task 3：useArticleDraft composable

**Files:**
- Create: `blog-frontend/src/composables/useArticleDraft.js`

- [ ] **Step 1: 创建文件**

新建 `blog-frontend/src/composables/useArticleDraft.js`：

```js
import { watch, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'

const DRAFT_KEY = 'article_draft'

export function useArticleDraft(form, isEdit) {
  if (isEdit.value) return

  let timer = null

  watch(
    form,
    (val) => {
      clearTimeout(timer)
      timer = setTimeout(() => {
        localStorage.setItem(DRAFT_KEY, JSON.stringify({
          ...val,
          savedAt: new Date().toISOString(),
        }))
      }, 300)
    },
    { deep: true }
  )

  onMounted(async () => {
    const raw = localStorage.getItem(DRAFT_KEY)
    if (!raw) return
    try {
      const draft = JSON.parse(raw)
      const time = new Date(draft.savedAt).toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
      })
      await ElMessageBox.confirm(
        `检测到未保存的草稿（${time}），是否恢复？`,
        '恢复草稿',
        { confirmButtonText: '恢复', cancelButtonText: '忽略', type: 'info' }
      )
      const { savedAt, ...data } = draft
      Object.assign(form, data)
    } catch {
      // 用户点取消或 JSON 解析失败，清除草稿
      localStorage.removeItem(DRAFT_KEY)
    }
  })
}

export function clearDraft() {
  localStorage.removeItem(DRAFT_KEY)
}
```

**说明：**
- `isEdit.value` 为 true（编辑已有文章）时直接 return，不注册任何副作用
- watch deep 监听整个 form reactive 对象，300ms debounce 防止频繁写入
- `ElMessageBox.confirm` 点"取消"会 reject，进入 catch → 删除草稿
- `clearDraft` 单独导出，供 ArticleEditView 在成功创建后调用

- [ ] **Step 2: 提交**

```bash
git add blog-frontend/src/composables/useArticleDraft.js
git commit -m "feat: 新增 useArticleDraft composable（localStorage 草稿自动保存）"
```

---

## Task 4：CoverCropDialog 组件

**Files:**
- Create: `blog-frontend/src/components/admin/CoverCropDialog.vue`

- [ ] **Step 1: 创建文件**

新建 `blog-frontend/src/components/admin/CoverCropDialog.vue`：

```vue
<template>
  <el-dialog
    :model-value="visible"
    title="裁剪封面（16:9）"
    width="600px"
    :close-on-click-modal="false"
    @closed="onClosed"
    @update:model-value="$emit('update:visible', $event)"
  >
    <div class="crop-wrap">
      <img ref="imgRef" :src="previewUrl" alt="封面预览" />
    </div>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="uploading" @click="handleConfirm">
        确认裁剪
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, onUnmounted } from 'vue'
import Cropper from 'cropperjs'
import 'cropperjs/dist/cropper.css'
import { uploadImage } from '@/api/upload'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: Boolean,
  file: { type: File, default: null },
})
const emit = defineEmits(['update:visible', 'done'])

const imgRef = ref(null)
const previewUrl = ref('')
const uploading = ref(false)
let cropper = null

watch(
  () => props.visible,
  (val) => {
    if (val && props.file) {
      previewUrl.value = URL.createObjectURL(props.file)
      setTimeout(() => {
        if (!imgRef.value) return
        cropper = new Cropper(imgRef.value, {
          aspectRatio: 16 / 9,
          viewMode: 1,
        })
      }, 50)
    } else {
      destroyCropper()
    }
  }
)

function destroyCropper() {
  if (cropper) { cropper.destroy(); cropper = null }
  if (previewUrl.value) { URL.revokeObjectURL(previewUrl.value); previewUrl.value = '' }
}

function onClosed() {
  destroyCropper()
}

async function handleConfirm() {
  if (!cropper) return
  uploading.value = true
  try {
    const blob = await new Promise((resolve) =>
      cropper.getCroppedCanvas().toBlob(resolve, 'image/jpeg', 0.9)
    )
    const res = await uploadImage(blob)
    emit('done', res.data)
    emit('update:visible', false)
  } catch {
    ElMessage.error('上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

onUnmounted(destroyCropper)
</script>

<style scoped>
.crop-wrap {
  height: 338px;
  background: #0C0C10;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.crop-wrap img { max-height: 338px; display: block; }
</style>
```

**说明：**
- `watch(visible)` 变为 true 时用 `URL.createObjectURL` 生成预览 URL，`setTimeout 50ms` 等待 DOM 渲染后初始化 cropperjs
- `handleConfirm` 调用 `getCroppedCanvas().toBlob()` 得到 Blob，传给 `uploadImage`（upload.js 中 `FormData.append('file', blob)` 兼容 Blob 类型）
- `onClosed` + `onUnmounted` 双重保障销毁 cropper 和释放 objectURL

- [ ] **Step 2: 提交**

```bash
git add blog-frontend/src/components/admin/CoverCropDialog.vue
git commit -m "feat: 新增 CoverCropDialog 封面裁剪弹窗（cropperjs 16:9）"
```

---

## Task 5：修改 ArticleEditView.vue

**Files:**
- Modify: `blog-frontend/src/views/admin/ArticleEditView.vue`

- [ ] **Step 1: 替换 `<script setup>` 全部内容**

将 `<script setup>` ... `</script>` 之间的所有内容替换为：

```js
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { createArticle, updateArticle, getArticleById } from '@/api/article'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import { uploadImage } from '@/api/upload'
import { ElMessage } from 'element-plus'
import { useArticleDraft, clearDraft } from '@/composables/useArticleDraft'
import { useWordCount } from '@/composables/useWordCount'
import CoverCropDialog from '@/components/admin/CoverCropDialog.vue'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const saving = ref(false)
const categories = ref([])
const tags = ref([])
const cropDialogVisible = ref(false)
const cropFile = ref(null)

const form = reactive({
  title: '', slug: '', summary: '', content: '', coverImage: '',
  status: 'DRAFT', categoryId: null, tagNames: [], isTop: false, allowComment: true,
})

const contentRef = computed(() => form.content)
const { wordCount, readingMinutes } = useWordCount(contentRef)

useArticleDraft(form, isEdit)

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

function handleCoverUpload(file) {
  cropFile.value = file.raw || file
  cropDialogVisible.value = true
  return false
}

function onCropDone(url) {
  form.coverImage = url
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
      clearDraft()
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
```

**注意：** 删除了原有 `coverUploading` ref（裁剪弹窗内部自己管理 loading），其余逻辑不变。

- [ ] **Step 2: 修改封面图 el-form-item**

找到：
```html
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
```

替换为：
```html
<el-form-item label="封面图">
  <div style="display:flex;align-items:center;gap:12px">
    <img v-if="form.coverImage" :src="form.coverImage"
      style="width:120px;height:67px;object-fit:cover;border-radius:4px;border:1px solid #2A2A3C" />
    <el-upload :before-upload="handleCoverUpload" :show-file-list="false" accept="image/*">
      <el-button size="small">{{ form.coverImage ? '更换封面' : '上传封面' }}</el-button>
    </el-upload>
    <el-button v-if="form.coverImage" size="small" @click="form.coverImage = ''">移除</el-button>
  </div>
</el-form-item>

<CoverCropDialog v-model:visible="cropDialogVisible" :file="cropFile" @done="onCropDone" />
```

封面预览高度改为 `67px`（= 120 × 9/16，精确 16:9）。

- [ ] **Step 3: 在内容区和复选框行之间插入字数统计栏**

找到：
```html
      <el-form-item label="内容">
        <MdEditor v-model="form.content" style="width:100%" @onUploadImg="handleUpload" />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="form.isTop">置顶</el-checkbox>
```

在两个 `el-form-item` 之间插入：
```html
      <div v-if="wordCount > 0" class="word-count-bar">
        {{ wordCount.toLocaleString() }} 字 · 约 {{ readingMinutes }} 分钟阅读
      </div>
```

- [ ] **Step 4: 添加 .word-count-bar 样式**

在文件末尾添加（若已有 `<style>` 块则追加到块内，若没有则新建）：

```html
<style scoped>
.word-count-bar {
  font-size: 12px;
  color: #6E6E82;
  padding: 4px 0 8px;
  margin-left: 80px;
}
</style>
```

- [ ] **Step 5: 启动开发服务器验证三个功能**

```bash
cd blog-frontend && npm run dev
```

验证：
1. 访问 `/admin/articles/new`，输入内容，等待 300ms，DevTools → Application → Local Storage 确认 `article_draft` 存在
2. 刷新页面，弹出恢复草稿提示，点"恢复"内容还原
3. 编辑器输入文字，底部出现 `X 字 · 约 Y 分钟阅读`；清空后消失
4. 点"上传封面"，选择图片，弹出裁剪弹窗，拖拽后点"确认裁剪"，封面预览更新

- [ ] **Step 6: 提交**

```bash
git add blog-frontend/src/views/admin/ArticleEditView.vue
git commit -m "feat: ArticleEditView 集成草稿自动保存、字数统计、封面裁剪"
```

---

## Task 6：PostDetailView 换用 MdPreview

**Files:**
- Modify: `blog-frontend/src/views/front/PostDetailView.vue`

当前状态：第 140 行 `import { MdEditor } from 'md-editor-v3'`，第 46 行 `<MdEditor v-model="article.content" previewOnly />`。

- [ ] **Step 1: 替换 import（第 140 行）**

将：
```js
import { MdEditor } from 'md-editor-v3'
```
改为：
```js
import { MdPreview } from 'md-editor-v3'
```

- [ ] **Step 2: 替换模板中的组件（第 46 行）**

将：
```html
<MdEditor v-model="article.content" previewOnly />
```
改为：
```html
<MdPreview :modelValue="article.content" />
```

`v-model` 改为单向 `:modelValue`（MdPreview 是只读组件）；`previewOnly` 属性移除（MdPreview 无此 prop）。

- [ ] **Step 3: 验证**

访问一篇含多个 `##` 标题的文章详情页，确认：
1. 文章内容正常渲染
2. 右侧 TOC 正常显示，滚动时高亮更新
3. 无编辑工具栏出现

- [ ] **Step 4: 提交**

```bash
git add blog-frontend/src/views/front/PostDetailView.vue
git commit -m "fix: 前台文章详情页换用 MdPreview 只读渲染"
```

---

## 自检清单

完成全部 Task 后逐项验证：

- [ ] 新建文章页输入内容 → Local Storage 出现 `article_draft` 键
- [ ] 刷新新建页面 → 弹出恢复提示 → 点恢复内容还原
- [ ] 成功创建文章后刷新新建页面 → 不再弹恢复提示
- [ ] 编辑器输入内容 → 底部显示字数和阅读时间；内容清空 → 统计行消失
- [ ] 上传封面 → 裁剪弹窗打开 → 裁剪后封面预览为 16:9
- [ ] 编辑已有文章（有 ID）→ 不弹草稿恢复提示
- [ ] 前台文章详情页 → 无编辑工具栏 → TOC 高亮正常
