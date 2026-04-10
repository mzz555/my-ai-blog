<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2>分类管理</h2>
      <el-button type="primary" @click="openDialog()">新建分类</el-button>
    </div>
    <el-table :data="categories" stripe>
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="slug" label="Slug" />
      <el-table-column prop="description" label="描述" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button text size="small" @click="openDialog(row)">编辑</el-button>
          <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
            <template #reference><el-button text size="small" type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑分类' : '新建分类'" width="400px">
      <el-form :model="form" label-width="60px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="Slug"><el-input v-model="form.slug" placeholder="留空自动生成" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/api/category'
import { ElMessage } from 'element-plus'

const categories = ref([])
const dialogVisible = ref(false)
const editId = ref(null)
const form = reactive({ name: '', slug: '', description: '' })

async function load() {
  const res = await getCategories()
  categories.value = res.data
}

function openDialog(row = null) {
  editId.value = row?.id || null
  Object.assign(form, { name: row?.name || '', slug: row?.slug || '', description: row?.description || '' })
  dialogVisible.value = true
}

async function handleSave() {
  if (editId.value) await updateCategory(editId.value, form)
  else await createCategory(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function handleDelete(id) {
  await deleteCategory(id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
