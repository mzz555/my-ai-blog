<template>
  <div class="not-found">
    <div class="not-found__content">
      <h1 class="not-found__code">404</h1>
      <p class="not-found__title">页面不存在</p>
      <p class="not-found__desc">您访问的页面可能已被删除或链接有误。</p>
      <el-button type="primary" @click="router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f7fa;
}

.not-found__content {
  text-align: center;
}

.not-found__code {
  font-size: 120px;
  font-weight: 700;
  color: #409eff;
  margin: 0;
  line-height: 1;
}

.not-found__title {
  font-size: 24px;
  color: #303133;
  margin: 16px 0 8px;
}

.not-found__desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 32px;
}
</style>
