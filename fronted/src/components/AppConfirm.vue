<script setup lang="ts">
import { ref } from 'vue'

const show = ref(false)
const message = ref('')
const title = ref('')
let resolveFn: ((val: boolean) => void) | null = null

function confirm(msg: string, ttl?: string): Promise<boolean> {
  message.value = msg
  title.value = ttl || '确认操作'
  show.value = true
  return new Promise(resolve => {
    resolveFn = resolve
  })
}

function onConfirm() {
  show.value = false
  resolveFn?.(true)
}

function onCancel() {
  show.value = false
  resolveFn?.(false)
}

defineExpose({ confirm })
</script>

<template>
  <teleport to="body">
    <transition name="fade">
      <div v-if="show" class="confirm-mask" @click.self="onCancel">
        <div class="confirm-box">
          <div class="confirm-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
          </div>
          <h3 class="confirm-title">{{ title }}</h3>
          <p class="confirm-message">{{ message }}</p>
          <div class="confirm-actions">
            <button class="btn-cancel" @click="onCancel">取消</button>
            <button class="btn-confirm" @click="onConfirm">确定</button>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<style scoped>
.confirm-mask {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center; z-index: 1001;
}
.confirm-box {
  background: #fff; border-radius: 14px; padding: 24px 20px;
  width: 320px; max-width: 85vw;
  box-shadow: 0 12px 40px rgba(0,0,0,0.15);
}
.confirm-icon {
  width: 52px; height: 52px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 16px;
  background: rgba(255, 170, 76, 0.1);
  color: var(--secondary-orange);
}
.confirm-title { font-size: 16px; font-weight: 600; color: var(--gray-900); margin: 0 0 8px; text-align: center; }
.confirm-message { font-size: 14px; color: var(--gray-600); margin: 0 0 20px; text-align: center; line-height: 1.5; }
.confirm-actions { display: flex; gap: 12px; }
.btn-cancel {
  flex: 1; padding: 10px; border: 1px solid var(--gray-200); border-radius: 10px;
  background: #fff; color: var(--gray-600); font-size: 14px; font-weight: 500; cursor: pointer;
}
.btn-cancel:hover { background: var(--gray-50); }
.btn-confirm {
  flex: 1; padding: 10px; border: none; border-radius: 10px;
  background: var(--secondary-orange); color: #fff; font-size: 14px; font-weight: 500; cursor: pointer;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>