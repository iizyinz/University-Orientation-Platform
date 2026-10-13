<script setup lang="ts">
import { ref } from 'vue'
import { RouterView } from 'vue-router'
import AppMessage from './components/AppMessage.vue'
import AppConfirm from './components/AppConfirm.vue'

const messageRef = ref<InstanceType<typeof AppMessage>>()
const confirmRef = ref<InstanceType<typeof AppConfirm>>()

function showMessage(msg: string, type: 'info' | 'error' | 'success' = 'info') {
  messageRef.value?.toast(msg, type)
}

function showConfirm(msg: string, title?: string): Promise<boolean> {
  return confirmRef.value?.confirm(msg, title) || Promise.resolve(false)
}

;(window as any).$message = showMessage
;(window as any).$confirm = showConfirm
</script>

<template>
  <RouterView />
  <AppMessage ref="messageRef" />
  <AppConfirm ref="confirmRef" />
</template>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
</style>