<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { Html5Qrcode } from 'html5-qrcode'

const props = defineProps<{ active: boolean }>()
const emit = defineEmits<{ (e: 'scan', idCard: string): void }>()

const uid = Math.random().toString(36).slice(2, 8)
const scannerId = 'qr-scanner-' + uid
const scannerRef = ref<HTMLDivElement>()
let html5Qr: Html5Qrcode | null = null
const scanning = ref(false)
const cameraError = ref('')
const startKey = ref(0)

async function startScan() {
  if (scanning.value) return
  cameraError.value = ''

  await nextTick()
  await new Promise(r => setTimeout(r, 150))

  let el = document.getElementById(scannerId)
  if (!el) {
    await nextTick()
    await new Promise(r => setTimeout(r, 100))
    el = document.getElementById(scannerId)
  }
  if (!el) return

  try {
    if (html5Qr) {
      try { await html5Qr.stop() } catch {}
      try { html5Qr.clear() } catch {}
      html5Qr = null
      await new Promise(r => setTimeout(r, 100))
    }

    html5Qr = new Html5Qrcode(scannerId)
    await html5Qr.start(
      { facingMode: 'environment' },
      { fps: 10, qrbox: { width: 250, height: 250 }, aspectRatio: 1.0 },
      (decodedText) => {
        emit('scan', decodedText)
        if (html5Qr) {
          try { html5Qr.stop() } catch {}
          scanning.value = false
        }
      },
      () => {}
    )
    scanning.value = true
    cameraError.value = ''
  } catch (err: any) {
    cameraError.value = '无法访问摄像头，请检查权限设置或使用手动输入'
    console.error('Camera error:', err)
  }
}

async function stopScan() {
  if (html5Qr && scanning.value) {
    try { await html5Qr.stop() } catch {}
    scanning.value = false
  }
}

async function restartScan() {
  if (html5Qr) {
    try { await html5Qr.stop() } catch {}
    try { html5Qr.clear() } catch {}
    html5Qr = null
  }
  scanning.value = false
  await nextTick()
  startKey.value++
}

watch(startKey, () => {
  if (props.active) startScan()
})

onMounted(() => {
  if (props.active) startScan()
})

onBeforeUnmount(async () => {
  if (html5Qr) {
    try { await html5Qr.stop() } catch {}
    try { html5Qr.clear() } catch {}
    html5Qr = null
    scanning.value = false
  }
})

defineExpose({ startScan, stopScan, restartScan })
</script>

<template>
  <div class="scanner-wrapper">
    <div :id="scannerId" ref="scannerRef" class="scanner-box"></div>
    <div v-if="cameraError" class="camera-error">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      {{ cameraError }}
    </div>
    <div v-if="scanning" class="scan-hint">将二维码对准框内即可自动扫描</div>
    <button v-if="cameraError" class="retry-btn" @click="startScan">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
      重新尝试
    </button>
  </div>
</template>

<style scoped>
.scanner-wrapper { width: 100%; display: flex; flex-direction: column; align-items: center; gap: 12px; }
.scanner-box { width: 100%; max-width: 340px; border-radius: 12px; overflow: hidden; background: #111; min-height: 200px; }
.scanner-box :deep(video) { border-radius: 12px; }
.camera-error { display: flex; align-items: center; gap: 8px; padding: 10px 14px; background: rgba(255,77,106,0.06); border: 1px solid rgba(255,77,106,0.15); border-radius: 8px; color: var(--danger); font-size: 13px; }
.scan-hint { font-size: 13px; color: var(--gray-400); text-align: center; }
.retry-btn {
  display: inline-flex; align-items: center; gap: 6px; padding: 8px 16px;
  background: var(--primary); color: #fff; border: none; border-radius: 8px;
  font-size: 13px; cursor: pointer; transition: opacity 0.2s;
}
.retry-btn:hover { opacity: 0.9; }
</style>
