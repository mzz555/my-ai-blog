<!-- blog-frontend/src/components/common/ErrorState.vue -->
<template>
  <div class="error-state">
    <div class="error-box">
      <span class="error-icon">⚠</span>
      <span class="error-msg">{{ message }}</span>
    </div>
    <button v-if="onRetry" class="retry-btn" @click="onRetry">重试</button>
  </div>
</template>

<script setup>
defineProps({
  message: { type: String, default: '加载失败，请刷新重试' },
  onRetry: { type: Function, default: null },
})
</script>

<style scoped>
.error-state { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 40px; }
.error-box {
  display: flex; align-items: center; gap: 8px;
  background: #2A1010; border: 1px solid #5C1010; border-radius: 6px;
  padding: 12px 20px; color: #F87171; font-size: 13px;
}
.error-icon { font-size: 16px; }
.retry-btn {
  background: #1A1A28; border: 1px solid #3A3A5C; color: #F0F0F8;
  padding: 6px 16px; border-radius: 6px; cursor: pointer; font-size: 13px;
}
.retry-btn:hover { border-color: #E8A838; }
</style>
