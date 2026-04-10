<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2>标签管理</h2>
      <el-button type="primary" @click="dialogVisible = true">新建标签</el-button>
    </div>
    <div style="display:flex;flex-wrap:wrap;gap:8px">
      <el-tag v-for="tag in tags" :key="tag.id" closable @close="handleDelete(tag.id)" size="large">
        {{ tag.name }}
      </el-tag>
      <el-empty v-if="!tags.length" description="暂无标签" :image-size="60" />
    </div>
    <el-dialog v-model="dialogVisible" title="新建标签" width="360px">
      <el-input v-model="form.name" placeholder="标签名称" />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getTags, createTag, deleteTag } from '@/api/tag'
import { ElMessage } from 'element-plus'

const tags = ref([])
const dialogVisible = ref(false)
const form = reactive({ name: '' })

async function load() { const res = await getTags(); tags.value = res.data }

async function handleCreate() {
  await createTag(form)
  ElMessage.success('创建成功')
  dialogVisible.value = false
  form.name = ''
  load()
}

async function handleDelete(id) {
  await deleteTag(id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
