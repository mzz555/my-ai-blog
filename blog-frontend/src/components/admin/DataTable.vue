<template>
  <div class="data-table-wrap">
    <el-table
      ref="tableRef"
      :data="data"
      v-loading="loading"
      :row-key="rowKey"
      class="data-table"
      :empty-text="emptyText"
      @selection-change="onSelectionChange"
    >
      <el-table-column v-if="selectable" type="selection" width="48" />
      <slot />
    </el-table>

    <div v-if="total > 0" class="dt-footer">
      <span class="dt-total">共 {{ total }} 条</span>
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  data:       { type: Array,   default: () => [] },
  loading:    { type: Boolean, default: false },
  total:      { type: Number,  default: 0 },
  page:       { type: Number,  default: 1 },
  pageSize:   { type: Number,  default: 10 },
  rowKey:     { type: String,  default: 'id' },
  emptyText:  { type: String,  default: '暂无数据' },
  selectable: { type: Boolean, default: false },
})

const emit = defineEmits(['update:page', 'page-change', 'selection-change'])

const tableRef = ref(null)

function handlePageChange(p) {
  emit('update:page', p)
  emit('page-change', p)
}

function onSelectionChange(rows) {
  emit('selection-change', rows)
}

defineExpose({
  clearSelection: () => tableRef.value?.clearSelection(),
})
</script>

<style scoped>
.data-table-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.data-table {
  background: transparent;
}
.data-table :deep(.el-table__inner-wrapper)::before { background: transparent; }
.data-table :deep(.el-table__cell) {
  background: transparent !important;
  border-bottom: 1px solid var(--color-card-border) !important;
}
.data-table :deep(thead .el-table__cell) {
  background: transparent !important;
  font-weight: 600;
  color: var(--color-text-tertiary);
  font-size: 11px;
  letter-spacing: .04em;
  text-transform: uppercase;
}
.data-table :deep(.el-table__row:hover > td) {
  background: var(--color-card-border) !important;
}

.dt-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--color-card-border);
}

.dt-total {
  font-size: 12px;
  color: var(--color-text-tertiary);
}
</style>
