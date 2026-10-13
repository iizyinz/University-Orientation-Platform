<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCampusMapList } from '@/api/discover'

const maps = ref<any[]>([])
const loading = ref(true)
const showPreview = ref(false)
const currentMap = ref<any>(null)

async function loadMaps() {
  loading.value = true
  try { const res: any = await getCampusMapList(); maps.value = (res.data || []).filter((m: any) => m.status === 1) }
  catch {} finally { loading.value = false }
}

function openMap(m: any) { currentMap.value = m; showPreview.value = true }

onMounted(loadMaps)
</script>

<template>
  <div class="campus-map-web">
    <div class="page-header"><h2>校园地图</h2><p class="subtitle">查看校园平面导览图</p></div>

    <div v-if="loading" class="loading-state"><div class="loading-spinner"></div></div>

    <div v-else-if="maps.length === 0" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5"><polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"/></svg>
      <p>暂无校园地图</p>
    </div>

    <div v-else class="map-grid">
      <div v-for="m in maps" :key="m.id" class="map-card" @click="openMap(m)">
        <div class="map-preview"><img :src="m.mapImage" :alt="m.mapName" /></div>
        <div class="map-info">
          <span class="map-name">{{ m.mapName }}</span>
          <span v-if="m.description" class="map-desc">{{ m.description }}</span>
        </div>
      </div>
    </div>

    <teleport to="body">
      <div v-if="showPreview && currentMap" class="modal-mask" @click.self="showPreview = false">
        <div class="preview-modal">
          <div class="modal-header"><h4>{{ currentMap.mapName }}</h4><button class="close-btn" @click="showPreview = false"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>
          <div class="preview-body"><img :src="currentMap.mapImage" :alt="currentMap.mapName" class="preview-image" /></div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.campus-map-web { padding-bottom: 24px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: var(--gray-900); margin: 0 0 4px; }
.page-header .subtitle { font-size: 14px; color: var(--gray-400); margin: 0; }

.loading-state { display: flex; justify-content: center; padding: 60px; }
.loading-spinner { width: 28px; height: 28px; border: 3px solid var(--gray-200); border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 300px; color: var(--gray-400); }
.empty-state p { margin: 12px 0 0; font-size: 14px; }

.map-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; }
.map-card { background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; overflow: hidden; cursor: pointer; transition: box-shadow 0.2s; }
.map-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.06); }
.map-preview { width: 100%; aspect-ratio: 16/9; background: var(--gray-100); overflow: hidden; }
.map-preview img { width: 100%; height: 100%; object-fit: cover; }
.map-info { padding: 14px 16px; }
.map-name { display: block; font-size: 15px; font-weight: 600; color: var(--gray-900); }
.map-desc { display: block; font-size: 13px; color: var(--gray-400); margin-top: 4px; }

.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.preview-modal { background: #fff; border-radius: 14px; width: 90vw; max-width: 800px; max-height: 85vh; display: flex; flex-direction: column; box-shadow: 0 12px 40px rgba(0,0,0,0.2); }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 18px 20px; border-bottom: 1px solid var(--gray-200); }
.modal-header h4 { font-size: 16px; font-weight: 600; color: var(--gray-900); margin: 0; }
.close-btn { background: none; border: none; color: var(--gray-400); cursor: pointer; padding: 4px; border-radius: 6px; }
.close-btn:hover { background: var(--gray-100); }
.preview-body { padding: 16px; text-align: center; overflow: auto; }
.preview-image { max-width: 100%; max-height: 70vh; border-radius: 8px; object-fit: contain; }
</style>
