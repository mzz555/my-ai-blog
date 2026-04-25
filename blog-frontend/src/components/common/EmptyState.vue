<!-- blog-frontend/src/components/common/EmptyState.vue -->
<template>
  <div class="empty-state">
    <div class="empty-icon">{{ icon }}</div>
    <div class="empty-title">{{ title }}</div>
    <div v-if="subtitle" class="empty-subtitle">{{ subtitle }}</div>
    <slot />
  </div>
</template>

<script setup>
defineProps({
  icon: { type: String, default: '📭' },
  title: { type: String, default: '暂无数据' },
  subtitle: { type: String, default: '' },
})
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 10px;
}
.empty-icon { font-size: 40px; }
.empty-title { font-size: 16px; font-weight: 600; color: #F0F0F8; }
.empty-subtitle { font-size: 13px; color: #8A8A9E; }
</style>
