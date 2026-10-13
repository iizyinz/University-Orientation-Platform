<template>
  <div ref="container" class="shadow-container"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'

const props = defineProps<{
  content: string
  tagName?: string
}>()

const container = ref<HTMLElement>()

function createShadowRoot() {
  if (!container.value) return
  
  container.value.innerHTML = ''
  
  const shadow = container.value.attachShadow({ mode: 'open' })
  
  const style = document.createElement('style')
  style.textContent = `
    :host {
      display: block;
      font-size: 15px;
      line-height: 1.8;
      color: var(--gray-600);
    }
    :host * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
    }
    :host img {
      max-width: 100%;
      border-radius: 8px;
      margin: 12px 0;
    }
    :host h1, :host h2, :host h3 {
      color: var(--gray-900);
      margin: 16px 0 8px;
      font-weight: 600;
    }
    :host p {
      margin: 8px 0;
    }
    :host table {
      width: 100%;
      border-collapse: collapse;
      margin: 12px 0;
    }
    :host th, :host td {
      border: 1px solid var(--gray-200);
      padding: 8px 12px;
      text-align: left;
      font-size: 14px;
    }
    :host th {
      background: var(--gray-100);
    }
    :host ul, :host ol {
      margin: 8px 0;
      padding-left: 24px;
    }
    :host li {
      margin: 4px 0;
    }
    :host a {
      color: var(--primary);
    }
    :host strong {
      font-weight: 600;
    }
    :host em {
      font-style: italic;
    }
    :host blockquote {
      border-left: 3px solid var(--primary);
      padding-left: 12px;
      margin: 12px 0;
      color: var(--gray-600);
    }
  `
  shadow.appendChild(style)
  
  const wrapper = document.createElement(props.tagName || 'div')
  wrapper.innerHTML = props.content || ''
  shadow.appendChild(wrapper)
}

watch(() => props.content, () => {
  nextTick(() => createShadowRoot())
})

onMounted(() => {
  createShadowRoot()
})
</script>

<style scoped>
.shadow-container {
  display: block;
}
</style>