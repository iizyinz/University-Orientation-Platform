<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  show: boolean
  message: string
  type?: 'info' | 'error' | 'success'
}>()

const emit = defineEmits<{
  (e: 'update:show', val: boolean): void
}>()

const close = () => { emit('update:show', false) }
</script>

<template>
  <teleport to="body">
    <transition name="fade">
      <div v-if="show" class="alert-mask" @click.self="close">
        <div class="alert-box">
          <div class="alert-icon" :class="type || 'info'">
            <svg v-if="type === 'error'" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
            <svg v-else-if="type === 'success'" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
            <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>
          </div>
          <p class="alert-message">{{ message }}</p>
          <button class="alert-btn" @click="close">确定</button>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<style scoped>
.alert-mask {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center; z-index: 1001;
}
.alert-box {
  background: #fff; border-radius: 14px; padding: 28px 24px 20px;
  width: 300px; max-width: 85vw; text-align: center;
  box-shadow: 0 12px 40px rgba(0,0,0,0.15);
}
.alert-icon {
  width: 52px; height: 52px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 14px;
}
.alert-icon.info { background: rgba(26,92,255,0.1); color: var(--primary); }
.alert-icon.error { background: rgba(255,77,106,0.1); color: var(--danger); }
.alert-icon.success { background: rgba(0,201,167,0.1); color: var(--success); }
.alert-message { font-size: 15px; color: var(--gray-900); line-height: 1.5; margin: 0 0 18px; }
.alert-btn {
  width: 100%; padding: 10px; border: none; border-radius: 10px;
  background: var(--primary); color: #fff; font-size: 14px; font-weight: 500;
  cursor: pointer;
}
.alert-btn:active { opacity: 0.85; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
