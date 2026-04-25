# 作者体验功能设计文档

**日期：** 2026-04-25
**范围：** Plan B — 作者体验提升
**Tech Stack：** Vue 3.4 / md-editor-v3 / cropperjs / localStorage

---

## 目标

在文章编辑页为作者提供三项体验改善：

1. **草稿自动保存** — 新文章编辑中防丢失
2. **字数/阅读时间统计** — 编辑器下方实时显示
3. **封面 16:9 裁剪预览** — 上传前裁剪，统一展示比例
4. **前台详情页只读渲染** — PostDetailView 换用 MdPreview，移除编辑工具栏

---

## 架构概览

```
blog-frontend/
  src/
    composables/
      useArticleDraft.js       ← 新建：localStorage 草稿逻辑
      useWordCount.js          ← 新建：字数/阅读时间计算
    components/admin/
      CoverCropDialog.vue      ← 新建：封面裁剪弹窗
    views/admin/
      ArticleEditView.vue      ← 修改：组合上述三个单元
    views/front/
      PostDetailView.vue       ← 修改：MdEditor → MdPreview
```

---

## 功能一：草稿自动保存（useArticleDraft）

### 适用范围

**仅对新文章**（`isEdit === false`）生效。已有 ID 的文章编辑依赖手动保存按钮，不介入 localStorage。

### 存储结构

```js
localStorage['article_draft'] = JSON.stringify({
  title, slug, summary, content, coverImage,
  categoryId, tagNames, status, isTop, allowComment,
  savedAt  // ISO 时间字符串，用于恢复提示
})
```

### 行为规格

| 事件 | 行为 |
|------|------|
| form 任意字段变化 | watch + 300ms debounce → 写入 localStorage |
| 页面挂载（新文章） | 检测草稿存在 → ElMessageBox.confirm "检测到未保存的草稿（HH:mm），是否恢复？" |
| 用户点"恢复" | Object.assign(form, draft)，关闭弹窗 |
| 用户点"取消" | removeItem('article_draft')，忽略草稿 |
| createArticle 成功 | removeItem('article_draft') |

### Composable 签名

```js
// useArticleDraft(form, isEdit)
// 内部自行注册 watch + onMounted，无返回值
export function useArticleDraft(form, isEdit) { ... }
```

---

## 功能二：字数/阅读时间统计（useWordCount）

### 计算逻辑

```js
function countWords(markdown) {
  const plain = markdown
    .replace(/```[\s\S]*?```/g, '')   // 代码块
    .replace(/`[^`]+`/g, '')           // 行内代码
    .replace(/!?\[.*?\]\(.*?\)/g, '')  // 链接/图片
    .replace(/#{1,6}\s/g, '')          // 标题符号
    .replace(/[*_~>]/g, '')            // 粗体/斜体/引用
  const zh = (plain.match(/[一-龥]/g) || []).length
  const en = (plain.match(/\b[a-zA-Z]+\b/g) || []).length
  return zh + en
}
```

- 阅读时间：`Math.ceil(wordCount / 300)`（分钟）
- 字数为 0 时不渲染状态栏

### Composable 签名

```js
// useWordCount(content: Ref<string>)
// 返回 { wordCount: ComputedRef<number>, readingMinutes: ComputedRef<number> }
export function useWordCount(content) { ... }
```

### 显示位置与样式

编辑器 `<MdEditor>` 下方，保存按钮行上方，独立一行：

```
1,234 字  ·  约 5 分钟阅读
```

```css
.word-count-bar {
  font-size: 12px;
  color: #6E6E82;
  padding: 4px 0 8px;
}
```

---

## 功能三：封面 16:9 裁剪弹窗（CoverCropDialog.vue）

### 依赖

```bash
npm install cropperjs
```

### 交互流程

1. 用户点"上传封面" → `el-upload` 的 `before-upload` 拦截 File，**不直接上传**
2. 将 File 传给 `CoverCropDialog`（`URL.createObjectURL`），打开弹窗
3. 弹窗内 `<img>` + cropperjs 初始化：
   - `aspectRatio: 16 / 9`
   - `viewMode: 1`（裁剪框不超出图片）
4. 用户拖拽调整裁剪区域 → 点"确认裁剪"
5. `cropper.getCroppedCanvas().toBlob(blob => uploadImage(blob))`
6. 上传成功 → emit `done(url)` → `form.coverImage = url`
7. 关闭弹窗，`URL.revokeObjectURL` 释放内存

### Props / Emits

```ts
props: {
  visible: Boolean,   // v-model:visible
  file: File          // 用户选择的原始 File
}
emits: ['update:visible', 'done']  // done(url: string)
```

### 弹窗规格

- `el-dialog` width: `600px`
- 预览区高度：`338px`（= 600 × 9/16，精确 16:9）
- 底部：**确认裁剪**（primary，上传中 loading）+ **取消**
- cropperjs 在 `onMounted` / watch visible 后初始化，`onUnmounted` 时 `cropper.destroy()`

---

## 功能四：PostDetailView 换用 MdPreview

**变更点：**

```js
// 修改前
import { MdEditor } from 'md-editor-v3'

// 修改后
import { MdPreview } from 'md-editor-v3'
```

```html
<!-- 修改前 -->
<MdEditor v-model="article.content" ... />

<!-- 修改后 -->
<MdPreview :modelValue="article.content" ... />
```

- `MdPreview` 渲染输出的 HTML 结构与 `MdEditor` 一致，现有 TOC（DOM 查询 heading + IntersectionObserver）不受影响
- 移除工具栏、编辑光标，前台读者不再看到编辑器 UI

---

## 文件变更清单

### 新建

- `blog-frontend/src/composables/useArticleDraft.js`
- `blog-frontend/src/composables/useWordCount.js`
- `blog-frontend/src/components/admin/CoverCropDialog.vue`

### 修改

- `blog-frontend/package.json`（添加 cropperjs 依赖）
- `blog-frontend/src/views/admin/ArticleEditView.vue`（组合三个新单元）
- `blog-frontend/src/views/front/PostDetailView.vue`（MdEditor → MdPreview）

---

## 验证方式

1. **草稿恢复：** 新建文章，输入标题和内容，关闭标签页，重新打开编辑页，确认弹出恢复提示且内容还原
2. **草稿清除：** 成功保存后刷新，确认不再出现恢复提示
3. **字数统计：** 输入中英文混合内容，确认字数和阅读时间实时更新；清空内容后状态栏消失
4. **封面裁剪：** 上传一张图片，确认弹出裁剪弹窗；拖拽后确认，封面显示裁剪后的 16:9 图片
5. **前台只读：** 访问任意文章详情页，确认无编辑工具栏显示，TOC 高亮滚动正常
