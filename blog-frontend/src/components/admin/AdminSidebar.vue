<template>
  <el-menu
    :default-active="$route.path"
    router
    :collapse="collapsed"
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409eff"
  >
    <div class="logo">
      {{ collapsed ? 'Blog' : '博客管理系统' }}
    </div>
    <el-menu-item index="/admin">
      <el-icon><Odometer /></el-icon><template #title>仪表盘</template>
    </el-menu-item>
    <el-menu-item index="/admin/articles">
      <el-icon><Document /></el-icon><template #title>文章管理</template>
    </el-menu-item>
    <el-menu-item index="/admin/comments">
      <el-icon><ChatDotRound /></el-icon><template #title>评论审核</template>
    </el-menu-item>
    <el-menu-item index="/admin/categories">
      <el-icon><FolderOpened /></el-icon><template #title>分类管理</template>
    </el-menu-item>
    <el-menu-item index="/admin/tags">
      <el-icon><CollectionTag /></el-icon><template #title>标签管理</template>
    </el-menu-item>
    <el-menu-item index="/admin/profile">
      <el-icon><User /></el-icon><template #title>个人资料</template>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
defineProps({ collapsed: Boolean })
</script>

<style scoped>
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #263445;
  overflow: hidden;
  white-space: nowrap;
}
</style>
