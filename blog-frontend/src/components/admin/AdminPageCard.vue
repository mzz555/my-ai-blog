<template>
  <div class="admin-page-card">
    <header class="apc-head">
      <div class="apc-title-block">
        <h2 class="apc-title">{{ title }}</h2>
        <p v-if="subtitle" class="apc-subtitle">{{ subtitle }}</p>
      </div>
      <div v-if="$slots.actions" class="apc-actions">
        <slot name="actions" />
      </div>
    </header>

    <div v-if="$slots.tabs" class="apc-tabs">
      <slot name="tabs" />
    </div>

    <div v-if="$slots.filter" class="apc-filter">
      <slot name="filter" />
    </div>

    <div class="apc-divider" />

    <div class="apc-body">
      <slot />
    </div>

    <footer v-if="$slots.footer" class="apc-footer">
      <slot name="footer" />
    </footer>
  </div>
</template>

<script setup>
defineProps({
  title:    { type: String, default: '' },
  subtitle: { type: String, default: '' },
})
</script>

<style scoped>
.admin-page-card {
  background: linear-gradient(180deg, var(--color-card-surface) 0%, transparent 100%);
  border: 1px solid var(--color-card-border);
  border-radius: var(--radius-card);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  box-shadow: var(--shadow-card-elevate);
  padding: 22px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition: background var(--transition-base), border-color var(--transition-base);
}

.apc-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
}

.apc-title-block { min-width: 0; }

.apc-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: -0.01em;
}

.apc-subtitle {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.apc-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.apc-tabs {
  display: flex;
  align-items: center;
  gap: 8px;
}

.apc-filter {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.apc-divider {
  height: 1px;
  background: var(--color-card-border);
  margin: 0 -22px;
}

.apc-body {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.apc-footer {
  padding-top: 12px;
  border-top: 1px solid var(--color-card-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
