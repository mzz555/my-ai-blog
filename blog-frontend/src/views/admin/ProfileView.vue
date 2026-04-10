<template>
  <div style="max-width:500px">
    <h2 style="margin-bottom:20px">个人资料</h2>
    <el-card>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" disabled />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="form.bio" type="textarea" :rows="4" placeholder="介绍一下自己..." />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" disabled>保存（需后端 PATCH /api/users/profile 接口）</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const form = reactive({ username: '', email: '', bio: '' })

onMounted(() => {
  const info = userStore.userInfo
  if (info) Object.assign(form, { username: info.username, email: info.email, bio: info.bio || '' })
})
</script>
