<template>
  <div class="custom-select" ref="selectRef">
    <div class="select-display" :class="{ active: isOpen, disabled: disabled }" @click="toggle">
      <span :class="['select-value', !modelValue ? 'placeholder' : '']">
        {{ displayText }}
      </span>
      <svg class="arrow" :class="{ open: isOpen }" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="6 9 12 15 18 9"/>
      </svg>
    </div>
    <div v-show="isOpen" class="dropdown-panel">
      <div class="dropdown-search" v-if="searchable">
        <input ref="searchInput" v-model="searchText" placeholder="搜索..." @click.stop />
      </div>
      <div class="dropdown-list">
        <div
          v-for="item in filteredOptions"
          :key="item.value"
          class="dropdown-item"
          :class="{ selected: item.value === modelValue }"
          @click="selectItem(item)"
        >
          <span>{{ item.label }}</span>
          <svg v-if="item.value === modelValue" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
        </div>
        <div v-if="filteredOptions.length === 0" class="dropdown-empty">暂无数据</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'

interface Option {
  label: string
  value: string | number
}

const props = defineProps<{
  modelValue: string | number
  options: Option[]
  placeholder?: string
  disabled?: boolean
  searchable?: boolean
}>()

const emit = defineEmits(['update:modelValue'])

const selectRef = ref<HTMLElement>()
const searchInput = ref<HTMLInputElement>()
const isOpen = ref(false)
const searchText = ref('')

const displayText = computed(() => {
  if (!props.modelValue) return props.placeholder || '请选择'
  const item = props.options.find(o => o.value === props.modelValue)
  return item?.label || String(props.modelValue)
})

const filteredOptions = computed(() => {
  if (!searchText.value) return props.options
  const kw = searchText.value.toLowerCase()
  return props.options.filter(o => o.label.toLowerCase().includes(kw))
})

function toggle() {
  if (props.disabled) return
  isOpen.value = !isOpen.value
  if (isOpen.value && props.searchable) {
    nextTick(() => searchInput.value?.focus())
  }
}

function selectItem(item: Option) {
  emit('update:modelValue', item.value)
  isOpen.value = false
  searchText.value = ''
}

function handleClickOutside(e: MouseEvent) {
  if (selectRef.value && !selectRef.value.contains(e.target as Node)) {
    isOpen.value = false
    searchText.value = ''
  }
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onBeforeUnmount(() => document.removeEventListener('click', handleClickOutside))
</script>

<style scoped>
.custom-select { position: relative; flex: 1; min-width: 0; }

.select-display {
  display: flex; align-items: center; justify-content: flex-end; gap: 6px;
  cursor: pointer; min-height: 24px;
}
.select-display.disabled { opacity: 0.5; cursor: not-allowed; }

.select-value { font-size: 14px; color: var(--gray-600); text-align: right; }
.select-value.placeholder { color: var(--gray-400); }

.arrow { color: var(--gray-400); transition: transform 0.2s; flex-shrink: 0; }
.arrow.open { transform: rotate(180deg); }

.dropdown-panel {
  position: absolute; top: calc(100% + 4px); right: 0; z-index: 100;
  background: #fff; border-radius: 12px; width: 100%; min-width: 160px; max-width: 280px;
  box-shadow: 0 6px 24px rgba(0,0,0,0.12); border: 1px solid var(--gray-200);
  overflow: hidden;
}

.dropdown-search { padding: 8px; border-bottom: 1px solid var(--gray-100); }
.dropdown-search input {
  width: 100%; padding: 8px 12px; border: 1px solid var(--gray-200); border-radius: 8px;
  font-size: 13px; outline: none; background: var(--gray-100);
}
.dropdown-search input:focus { border-color: var(--primary); background: #fff; }

.dropdown-list { max-height: 260px; overflow-y: auto; padding: 4px; }
.dropdown-list::-webkit-scrollbar { width: 4px; }
.dropdown-list::-webkit-scrollbar-thumb { background: var(--gray-200); border-radius: 2px; }

.dropdown-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 14px; border-radius: 8px; cursor: pointer;
  font-size: 14px; color: var(--gray-600); transition: background 0.15s;
}
.dropdown-item:hover { background: var(--gray-100); }
.dropdown-item.selected { color: var(--primary); font-weight: 500; background: rgba(26,92,255,0.04); }

.dropdown-empty { text-align: center; padding: 20px; color: var(--gray-400); font-size: 13px; }
</style>
