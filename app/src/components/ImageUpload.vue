<script setup lang="ts">
import { ref, watch } from 'vue'
import { uploadImage } from '@/api/file'

const props = defineProps<{
  modelValue: string
  dir?: string
  accept?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const uploading = ref(false)
const previewUrl = ref(props.modelValue || '')
const dragActive = ref(false)
const fileInput = ref<HTMLInputElement>()

watch(() => props.modelValue, (val) => { previewUrl.value = val || '' })

function onDrop(e: DragEvent) {
  dragActive.value = false
  const files = e.dataTransfer?.files
  if (files && files.length > 0) handleFile(files[0])
}

function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files && input.files.length > 0) handleFile(input.files[0])
}

async function handleFile(file: File) {
  if (!file.type.startsWith('image/')) { alert('仅支持图片文件'); return }
  if (file.size > 10 * 1024 * 1024) { alert('文件大小不能超过10MB'); return }

  uploading.value = true
  try {
    const res: any = await uploadImage(file, props.dir || 'common')
    const url = res.data.url
    previewUrl.value = url
    emit('update:modelValue', url)
  } catch (err: any) {
    alert(err.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

function clearImage() {
  previewUrl.value = ''
  emit('update:modelValue', '')
}
</script>

<template>
  <div class="upload-wrapper">
    <div
      v-if="!previewUrl"
      class="upload-area"
      :class="{ active: dragActive }"
      @dragover.prevent="dragActive = true"
      @dragleave="dragActive = false"
      @drop.prevent="onDrop"
      @click="fileInput?.click()"
    >
      <input ref="fileInput" type="file" :accept="accept || 'image/*'" style="display:none" @change="onFileChange" />
      <template v-if="!uploading">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/>
        </svg>
        <span class="upload-text">拖拽图片或 <span class="link">点击上传</span></span>
      </template>
      <template v-else>
        <span class="spinner"></span>
        <span class="upload-text">上传中...</span>
      </template>
    </div>

    <div v-else class="preview-area">
      <img :src="previewUrl" class="preview-img" />
      <div class="preview-actions">
        <button class="preview-btn" @click="fileInput?.click()">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
          更换
        </button>
        <button class="preview-btn danger" @click="clearImage">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          删除
        </button>
      </div>
      <input ref="fileInput" type="file" :accept="accept || 'image/*'" style="display:none" @change="onFileChange" />
    </div>
  </div>
</template>

<style scoped>
.upload-wrapper { width: 100%; }
.upload-area {
  border: 2px dashed var(--gray-200); border-radius: 10px; padding: 28px 16px;
  text-align: center; cursor: pointer; transition: all 0.2s;
  display: flex; flex-direction: column; align-items: center; gap: 8px;
}
.upload-area:hover, .upload-area.active { border-color: var(--primary); background: rgba(26,92,255,0.02); }
.upload-text { font-size: 13px; color: var(--gray-400); }
.link { color: var(--primary); font-weight: 500; }
.spinner { width: 20px; height: 20px; border: 2px solid var(--gray-200); border-top-color: var(--primary); border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.preview-area { display: flex; flex-direction: column; gap: 8px; }
.preview-img { width: 100%; max-height: 180px; object-fit: cover; border-radius: 8px; border: 1px solid var(--gray-200); }
.preview-actions { display: flex; gap: 8px; }
.preview-btn {
  display: inline-flex; align-items: center; gap: 4px; padding: 5px 10px;
  border: 1px solid var(--gray-200); border-radius: 6px; background: #fff;
  font-size: 12px; color: var(--gray-600); cursor: pointer; transition: all 0.15s;
}
.preview-btn:hover { border-color: var(--primary); color: var(--primary); }
.preview-btn.danger:hover { border-color: var(--danger); color: var(--danger); }
</style>
