<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getHandbookList, getHandbookDetail } from '@/api/discover'
import ShadowContent from '@/components/ShadowContent.vue'

const handbooks = ref<any[]>([])
const loading = ref(true)
const showDetail = ref(false)
const currentHandbook = ref<any>(null)
const detailLoading = ref(false)

async function loadHandbooks() {
  loading.value = true
  try {
    const res: any = await getHandbookList()
    handbooks.value = (res.data || []).filter((h: any) => h.publishStatus === 1)
  } catch {} finally { loading.value = false }
}

async function openHandbook(id: number) {
  detailLoading.value = true; showDetail.value = true
  try { const res: any = await getHandbookDetail(id); currentHandbook.value = res.data }
  catch { currentHandbook.value = null } finally { detailLoading.value = false }
}

onMounted(loadHandbooks)
</script>

<template>
  <div class="handbook-web">
    <div class="page-header"><h2>新生入学手册</h2><p class="subtitle">了解学校概况与入学须知</p></div>

    <div v-if="loading" class="loading-state"><div class="loading-spinner"></div></div>

    <div v-else-if="handbooks.length === 0" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>
      <p>暂无入学手册</p>
    </div>

    <div v-else class="content-grid">
      <div class="list-panel">
        <div v-for="h in handbooks" :key="h.id" class="list-item" :class="{ active: currentHandbook?.id === h.id }" @click="openHandbook(h.id)">
          <div v-if="h.coverImage" class="item-cover"><img :src="h.coverImage" :alt="h.title" /></div>
          <div v-else class="item-cover placeholder"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--gray-400)" stroke-width="1.5"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <div class="item-info"><h4>{{ h.title }}</h4><span class="meta">{{ h.viewCount || 0 }} 次浏览</span></div>
        </div>
      </div>
      <div class="detail-panel">
        <div v-if="detailLoading" class="loading-state"><div class="loading-spinner"></div></div>
        <ShadowContent v-else-if="currentHandbook?.content" :content="currentHandbook.content" />
        <div v-else class="empty-hint">选择左侧手册查看详情</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.handbook-web { padding-bottom: 24px; }

.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: var(--gray-900); margin: 0 0 4px; }
.page-header .subtitle { font-size: 14px; color: var(--gray-400); margin: 0; }

.loading-state { display: flex; justify-content: center; padding: 60px; }
.loading-spinner { width: 28px; height: 28px; border: 3px solid var(--gray-200); border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 300px; color: var(--gray-400); }
.empty-state p { margin: 12px 0 0; font-size: 14px; }

.content-grid { display: grid; grid-template-columns: 280px 1fr; gap: 20px; min-height: 400px; }
.list-panel { display: flex; flex-direction: column; gap: 8px; }
.list-item {
  display: flex; align-items: center; gap: 12px; padding: 12px;
  background: #fff; border: 1px solid var(--gray-200); border-radius: 10px;
  cursor: pointer; transition: all 0.15s;
}
.list-item.active { border-color: var(--primary); background: rgba(26,92,255,0.04); }
.item-cover { width: 48px; height: 48px; border-radius: 8px; overflow: hidden; background: var(--gray-100); flex-shrink: 0; display: flex; align-items: center; justify-content: center; }
.item-cover img { width: 100%; height: 100%; object-fit: cover; }
.item-cover.placeholder { color: var(--gray-400); }
.item-info { flex: 1; }
.item-info h4 { font-size: 14px; font-weight: 600; color: var(--gray-900); margin: 0 0 2px; }
.meta { font-size: 12px; color: var(--gray-400); }

.detail-panel { background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; padding: 24px; overflow-y: auto; }
.empty-hint { display: flex; align-items: center; justify-content: center; min-height: 300px; color: var(--gray-400); font-size: 14px; }

.rich-content { font-size: 15px; line-height: 1.8; color: var(--gray-600); }
.rich-content :deep(img) { max-width: 100%; border-radius: 8px; margin: 12px 0; }
.rich-content :deep(h1), .rich-content :deep(h2), .rich-content :deep(h3) { color: var(--gray-900); margin: 16px 0 8px; }
.rich-content :deep(p) { margin: 8px 0; }
.rich-content :deep(table) { width: 100%; border-collapse: collapse; margin: 12px 0; }
.rich-content :deep(th), .rich-content :deep(td) { border: 1px solid var(--gray-200); padding: 8px 12px; text-align: left; font-size: 14px; }
.rich-content :deep(th) { background: var(--gray-100); }
</style>
