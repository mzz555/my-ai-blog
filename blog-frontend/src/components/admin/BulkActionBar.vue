<template>
  <Transition name="bba-slide">
    <div v-if="count > 0" class="bulk-action-bar">
      <div class="bba-left">
        <span class="bba-check-icon">✓</span>
        <span class="bba-count">已选 {{ count }} 项</span>
      </div>
      <div class="bba-right">
        <slot />
        <button class="bba-cancel" @click="$emit('cancel')">取消</button>
      </div>
    </div>
  </Transition>
</template>

<script setup>
defineProps({
  count: { type: Number, required: true },
})

defineEmits(['cancel'])
</script>

<style scoped>
.bulk-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: rgba(232, 168, 56, 0.08);
  border: 1px solid rgba(232, 168, 56, 0.25);
  border-radius: var(--radius-md);
  transition: background var(--transition-base), border-color var(--transition-base);
}

.bba-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #E8A838;
  font-weight: 600;
}

.bba-check-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(232, 168, 56, 0.2);
  font-size: 11px;
}

.bba-right {
  display: flex;
  gap: 8px;
  align-items: center;
}

.bba-cancel {
  display: inline-flex;
  align-items: center;
  height: 30px;
  padding: 0 14px;
  border-radius: 6px;
  background: var(--color-card-surface);
  border: 1px solid var(--color-card-border);
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: opacity var(--transition-fast), border-color var(--transition-fast);
}
.bba-cancel:hover { border-color: var(--color-text-tertiary); }

.bba-slide-enter-active, .bba-slide-leave-active {
  transition: opacity 200ms ease, transform 200ms ease, max-height 200ms ease;
  overflow: hidden;
}
.bba-slide-enter-from, .bba-slide-leave-to {
  opacity: 0;
  transform: translateY(-6px);
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
  border-width: 0;
}
.bba-slide-enter-to, .bba-slide-leave-from {
  opacity: 1;
  transform: translateY(0);
  max-height: 60px;
}

:slotted(.bba-action) {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 30px;
  padding: 0 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid;
  transition: opacity var(--transition-fast);
}
:slotted(.bba-action:hover) { opacity: 0.8; }
:slotted(.bba-action--del) {
  background: rgba(239, 68, 68, 0.12);
  border-color: rgba(239, 68, 68, 0.35);
  color: #EF4444;
}
:slotted(.bba-action--approve) {
  background: rgba(34, 197, 94, 0.12);
  border-color: rgba(34, 197, 94, 0.35);
  color: #22C55E;
}
:slotted(.bba-action--reject) {
  background: rgba(107, 114, 128, 0.1);
  border-color: rgba(107, 114, 128, 0.3);
  color: var(--color-text-secondary);
}
</style>
