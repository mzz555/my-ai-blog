<!-- blog-frontend/src/components/common/SkeletonCard.vue -->
<template>
  <div class="skeleton-card">
    <div class="sk-image" />
    <div class="sk-body">
      <div class="sk-line sk-title" />
      <div class="sk-line sk-subtitle" />
      <div class="sk-line sk-meta" />
    </div>
  </div>
</template>

<style scoped>
@keyframes shimmer {
  0% { background-position: -400px 0; }
  100% { background-position: 400px 0; }
}
.skeleton-card {
  background: #13131E;
  border-radius: 8px;
  overflow: hidden;
}
.sk-image {
  height: 140px;
  background: linear-gradient(90deg, #1C1C2C 25%, #252545 50%, #1C1C2C 75%);
  background-size: 400px 100%;
  animation: shimmer 1.4s infinite linear;
}
.sk-body { padding: 16px; display: flex; flex-direction: column; gap: 10px; }
.sk-line {
  border-radius: 4px;
  background: linear-gradient(90deg, #1C1C2C 25%, #252545 50%, #1C1C2C 75%);
  background-size: 400px 100%;
  animation: shimmer 1.4s infinite linear;
}
.sk-title { height: 16px; width: 80%; }
.sk-subtitle { height: 12px; width: 60%; }
.sk-meta { height: 10px; width: 40%; }
</style>
