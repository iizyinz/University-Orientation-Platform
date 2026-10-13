<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  total: number
  page: number
  pageSize: number
}>()

const emit = defineEmits<{
  (e: 'update:page', val: number): void
}>()

const totalPages = computed(() => Math.ceil(props.total / props.pageSize) || 1)

const pages = computed(() => {
  const result: (number | '...')[] = []
  const tp = totalPages.value
  const cp = props.page
  if (tp <= 7) {
    for (let i = 1; i <= tp; i++) result.push(i)
  } else {
    result.push(1)
    if (cp > 4) result.push('...')
    for (let i = Math.max(2, cp - 1); i <= Math.min(tp - 1, cp + 1); i++) {
      result.push(i)
    }
    if (cp < tp - 3) result.push('...')
    result.push(tp)
  }
  return result
})

function go(p: number) {
  if (p < 1 || p > totalPages.value || p === props.page) return
  emit('update:page', p)
}
</script>

<template>
  <div class="pagination">
    <button class="page-btn" :disabled="page <= 1" @click="go(page - 1)">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
    </button>
    <template v-for="(p, i) in pages" :key="i">
      <span v-if="p === '...'" class="page-ellipsis">...</span>
      <button v-else :class="['page-btn', 'num', { active: p === page }]" @click="go(p as number)">{{ p }}</button>
    </template>
    <button class="page-btn" :disabled="page >= totalPages" @click="go(page + 1)">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
    </button>
    <span class="page-info">共 {{ total }} 条</span>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 16px;
}

.page-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  border: 1px solid var(--gray-200);
  border-radius: 6px;
  background: #fff;
  color: var(--gray-600);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}

.page-btn:hover:not(:disabled):not(.active) {
  border-color: var(--primary);
  color: var(--primary);
}

.page-btn.active {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-ellipsis {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  color: var(--gray-400);
  font-size: 13px;
}

.page-info {
  margin-left: 12px;
  font-size: 13px;
  color: var(--gray-400);
}
</style>
