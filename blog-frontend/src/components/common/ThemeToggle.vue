<template>
  <button class="theme-toggle" @click="appStore.toggleDarkMode()"
    :title="appStore.darkMode ? '切换亮色模式' : '切换暗色模式'">
    <span class="icon-wrap" :class="{ 'is-dark': appStore.darkMode }">
      <svg class="icon-sun" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="5"/>
        <line x1="12" y1="1" x2="12" y2="3"/>
        <line x1="12" y1="21" x2="12" y2="23"/>
        <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/>
        <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/>
        <line x1="1" y1="12" x2="3" y2="12"/>
        <line x1="21" y1="12" x2="23" y2="12"/>
        <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/>
        <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
      </svg>
      <svg class="icon-moon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
      </svg>
    </span>
  </button>
</template>

<script setup>
import { useAppStore } from '@/stores/app'
const appStore = useAppStore()
</script>

<style scoped>
.theme-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  padding: 0;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  background: var(--color-surface);
  color: var(--color-text-secondary);
  cursor: pointer;
  flex-shrink: 0;
  overflow: hidden;
  transition: background var(--transition-fast), border-color var(--transition-fast),
              color var(--transition-fast), box-shadow var(--transition-fast);
}
.theme-toggle:hover {
  background: var(--color-bg-secondary);
  color: var(--color-accent);
  border-color: var(--color-accent-border);
  box-shadow: 0 0 0 3px rgba(232,168,56,0.12);
}

.icon-wrap {
  position: relative;
  width: 18px;
  height: 18px;
}

.icon-sun,
.icon-moon {
  position: absolute;
  inset: 0;
  width: 18px;
  height: 18px;
  transition: opacity 200ms ease, transform 200ms ease;
}

/* Light mode: show moon (click → go dark) */
.icon-sun  { opacity: 0; transform: rotate(90deg) scale(0.7); }
.icon-moon { opacity: 1; transform: rotate(0deg)  scale(1);   }

/* Dark mode: show sun (click → go light) */
.is-dark .icon-sun  { opacity: 1; transform: rotate(0deg)   scale(1);   }
.is-dark .icon-moon { opacity: 0; transform: rotate(-90deg) scale(0.7); }
</style>
