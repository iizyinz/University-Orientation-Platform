<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getHandbookList, getHandbookDetail, getCampusMapList, getMyGroups } from '@/api/discover'
import ShadowContent from '@/components/ShadowContent.vue'

const route = useRoute()
const router = useRouter()

const subType = computed(() => {
  const tab = (route.query.tab as string) || ''
  return tab.replace('discover-', '')
})

const goBack = () => {
  const prefix = route.path.startsWith('/student-web') ? '/student-web' : '/student'
  router.push({ path: prefix, query: { tab: 'discover' } })
}

// ===== 入学手册 =====
const handbooks = ref<any[]>([])
const showHandbook = ref(false)
const currentHandbook = ref<any>(null)
const handbookLoading = ref(false)

async function loadHandbooks() {
  try { const res: any = await getHandbookList(); handbooks.value = (res.data || []).filter((h: any) => h.publishStatus === 1) } catch {}
}

async function openHandbook(id: number) {
  handbookLoading.value = true; showHandbook.value = true
  try { const res: any = await getHandbookDetail(id); currentHandbook.value = res.data }
  catch { currentHandbook.value = null } finally { handbookLoading.value = false }
}

// ===== 校园地图 =====
const maps = ref<any[]>([])
const showMapPreview = ref(false)
const currentMap = ref<any>(null)

async function loadMaps() {
  try { const res: any = await getCampusMapList(); maps.value = (res.data || []).filter((m: any) => m.status === 1) } catch {}
}

function openMap(m: any) { currentMap.value = m; showMapPreview.value = true }

// ===== 新生群 =====
const groups = ref<any[]>([])
const groupTypeMap: Record<number, string> = { 1: '生源地群', 2: '学院群', 3: '班级群' }

async function loadGroups() {
  try { const userId = userStore.userInfo.id; if (!userId) return
    const res: any = await getMyGroups(userId); groups.value = res.data || []
  } catch {}
}

const userStore = useUserStore()

onMounted(() => {
  if (subType.value === 'handbook') loadHandbooks()
  else if (subType.value === 'map') loadMaps()
  else if (subType.value === 'group') loadGroups()
})
</script>

<template>
  <div class="discover-sub">
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
      <div>
        <h3>{{ subType === 'handbook' ? '新生入学手册' : subType === 'map' ? '校园地图' : subType === 'group' ? '新生群' : '内容' }}</h3>
      </div>
    </div>

    <!-- 入学手册 -->
    <div v-if="subType === 'handbook'">
      <div v-if="handbooks.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>
        <p>暂无入学手册</p>
      </div>
      <div v-else class="card-list">
        <div v-for="h in handbooks" :key="h.id" class="list-card" @click="openHandbook(h.id)">
          <div v-if="h.coverImage" class="card-cover"><img :src="h.coverImage" :alt="h.title" /></div>
          <div v-else class="card-cover placeholder"><svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="var(--gray-400)" stroke-width="1.5"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <div class="card-info"><h4>{{ h.title }}</h4><span class="meta">{{ h.viewCount || 0 }} 次浏览</span></div>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--gray-400)" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
        </div>
      </div>
    </div>

    <!-- 校园地图 -->
    <div v-if="subType === 'map'">
      <div v-if="maps.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5"><polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"/></svg>
        <p>暂无校园地图</p>
      </div>
      <div v-else class="map-grid">
        <div v-for="m in maps" :key="m.id" class="map-card" @click="openMap(m)">
          <div class="map-preview"><img :src="m.mapImage" :alt="m.mapName" /></div>
          <div class="map-info"><span class="map-name">{{ m.mapName }}</span><span v-if="m.description" class="map-desc">{{ m.description }}</span></div>
        </div>
      </div>
    </div>

    <!-- 新生群 -->
    <div v-if="subType === 'group'">
      <div v-if="groups.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
        <p>暂无群聊信息</p>
      </div>
      <div v-else class="group-list">
        <div v-for="g in groups" :key="g.id" class="group-card">
          <div class="group-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
          </div>
          <div class="group-info">
            <span class="group-type">{{ groupTypeMap[g.groupType] || '群聊' }}</span>
            <span class="group-name">{{ g.groupName }}</span>
            <span v-if="g.groupNo" class="group-no">群号：{{ g.groupNo }}</span>
            <span v-else class="group-no">暂未设置群号</span>
            <div v-if="g.counselorName" class="counselor-info">
              <span class="counselor-label">辅导员：{{ g.counselorName }}</span>
              <span v-if="g.counselorPhone" class="counselor-detail">手机：{{ g.counselorPhone }}</span>
              <span v-if="g.counselorEmail" class="counselor-detail">邮箱：{{ g.counselorEmail }}</span>
            </div>
          </div>
          <div v-if="g.groupQrImage" class="group-qr" @click.stop="currentMap = { mapImage: g.groupQrImage, mapName: g.groupName + ' 二维码' }; showMapPreview = true">
            <img :src="g.groupQrImage" alt="群二维码" />
            <span>查看二维码</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 手册详情弹窗 -->
    <teleport to="body">
      <div v-if="showHandbook" class="modal-mask" @click.self="showHandbook = false">
        <div class="detail-modal">
          <div class="modal-header">
            <h4>{{ currentHandbook?.title || '入学手册' }}</h4>
            <button class="close-btn" @click="showHandbook = false"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>
          </div>
          <div class="modal-body">
            <div v-if="handbookLoading" class="loading-state"><div class="loading-spinner"></div></div>
            <ShadowContent v-else-if="currentHandbook?.content" :content="currentHandbook.content" />
            <div v-else class="empty-content">暂无内容</div>
          </div>
        </div>
      </div>
    </teleport>

    <!-- 地图/二维码预览弹窗 -->
    <teleport to="body">
      <div v-if="showMapPreview && currentMap" class="modal-mask" @click.self="showMapPreview = false">
        <div class="preview-modal">
          <div class="modal-header">
            <h4>{{ currentMap.mapName }}</h4>
            <button class="close-btn" @click="showMapPreview = false"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>
          </div>
          <div class="preview-body"><img :src="currentMap.mapImage" :alt="currentMap.mapName" class="preview-image" /></div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.discover-sub { padding-bottom: 8px; }
.page-header { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
.back-btn { width: 36px; height: 36px; border-radius: 10px; border: 1px solid var(--gray-200); background: #fff; display: flex; align-items: center; justify-content: center; cursor: pointer; color: var(--gray-600); }
.page-header h3 { font-size: 18px; font-weight: 700; color: var(--gray-900); margin: 0; }

.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 200px; text-align: center; color: var(--gray-400); }
.empty-state p { margin: 12px 0 0; font-size: 14px; }
.loading-state { display: flex; justify-content: center; padding: 40px; }
.loading-spinner { width: 28px; height: 28px; border: 3px solid var(--gray-200); border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* 手册 */
.card-list { display: flex; flex-direction: column; gap: 12px; }
.list-card { display: flex; align-items: center; gap: 14px; background: #fff; border: 1px solid var(--gray-200); border-radius: 14px; padding: 14px; cursor: pointer; }
.list-card:active { box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.card-cover { width: 64px; height: 64px; border-radius: 10px; overflow: hidden; background: var(--gray-100); flex-shrink: 0; display: flex; align-items: center; justify-content: center; }
.card-cover img { width: 100%; height: 100%; object-fit: cover; }
.card-cover.placeholder { color: var(--gray-400); }
.card-info { flex: 1; }
.card-info h4 { font-size: 15px; font-weight: 600; color: var(--gray-900); margin: 0 0 4px; }
.meta { font-size: 12px; color: var(--gray-400); }

/* 地图 */
.map-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.map-card { background: #fff; border: 1px solid var(--gray-200); border-radius: 14px; overflow: hidden; cursor: pointer; }
.map-card:active { box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.map-preview { width: 100%; aspect-ratio: 4/3; background: var(--gray-100); display: flex; align-items: center; justify-content: center; overflow: hidden; }
.map-preview img { width: 100%; height: 100%; object-fit: cover; }
.map-info { padding: 12px 14px; }
.map-name { display: block; font-size: 14px; font-weight: 600; color: var(--gray-900); }
.map-desc { display: block; font-size: 12px; color: var(--gray-400); margin-top: 2px; }

/* 群聊 */
.group-list { display: flex; flex-direction: column; gap: 12px; }
.group-card { display: flex; align-items: center; gap: 14px; background: #fff; border: 1px solid var(--gray-200); border-radius: 14px; padding: 16px; }
.group-icon { width: 44px; height: 44px; border-radius: 12px; background: rgba(26,92,255,0.1); color: var(--primary); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.group-info { flex: 1; display: flex; flex-direction: column; }
.group-type { font-size: 11px; color: var(--gray-400); }
.group-name { font-size: 14px; font-weight: 600; color: var(--gray-900); }
.group-no { font-size: 12px; color: var(--gray-400); margin-top: 2px; }
.counselor-info { display: flex; flex-direction: column; gap: 2px; margin-top: 6px; padding-top: 6px; border-top: 1px solid var(--gray-100); }
.counselor-label { font-size: 13px; font-weight: 500; color: var(--primary); }
.counselor-detail { font-size: 12px; color: var(--gray-400); }
.group-qr { display: flex; flex-direction: column; align-items: center; gap: 4px; cursor: pointer; }
.group-qr img { width: 48px; height: 48px; border-radius: 6px; border: 1px solid var(--gray-200); }
.group-qr span { font-size: 11px; color: var(--primary); }

/* 弹窗 */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.detail-modal { background: #fff; border-radius: 14px; width: 90vw; max-width: 600px; max-height: 80vh; display: flex; flex-direction: column; box-shadow: 0 12px 40px rgba(0,0,0,0.2); }
.preview-modal { background: #fff; border-radius: 14px; width: 90vw; max-width: 500px; max-height: 85vh; display: flex; flex-direction: column; box-shadow: 0 12px 40px rgba(0,0,0,0.2); }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 18px 20px; border-bottom: 1px solid var(--gray-200); flex-shrink: 0; }
.modal-header h4 { font-size: 16px; font-weight: 600; color: var(--gray-900); margin: 0; }
.close-btn { background: none; border: none; color: var(--gray-400); cursor: pointer; padding: 4px; border-radius: 6px; }
.close-btn:hover { background: var(--gray-100); }
.modal-body { flex: 1; overflow-y: auto; padding: 20px; }
.empty-content { text-align: center; padding: 32px; color: var(--gray-400); }

.rich-content { font-size: 15px; line-height: 1.8; color: var(--gray-600); }
.rich-content :deep(img) { max-width: 100%; border-radius: 8px; margin: 12px 0; }
.rich-content :deep(h1), .rich-content :deep(h2), .rich-content :deep(h3) { color: var(--gray-900); margin: 16px 0 8px; }
.rich-content :deep(p) { margin: 8px 0; }
.rich-content :deep(table) { width: 100%; border-collapse: collapse; margin: 12px 0; }
.rich-content :deep(th), .rich-content :deep(td) { border: 1px solid var(--gray-200); padding: 8px 12px; text-align: left; font-size: 14px; }
.rich-content :deep(th) { background: var(--gray-100); }

.preview-body { padding: 16px; text-align: center; }
.preview-image { max-width: 100%; max-height: 70vh; border-radius: 8px; object-fit: contain; }
</style>
