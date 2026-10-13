<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  getHandbookList, getHandbookDetail, getCampusMapList, getMyGroups,
  getFaqCategories, searchFaq, getFaqByCategory, hitFaq
} from '@/api/discover'
import ShadowContent from '@/components/ShadowContent.vue'

const route = useRoute()
const userStore = useUserStore()

// 数据
const handbooks = ref<any[]>([])
const campusMaps = ref<any[]>([])
const groups = ref<any[]>([])

// FAQ 数据
const faqCategories = ref<any[]>([])
const faqList = ref<any[]>([])
const faqKeyword = ref('')
const faqSelectedCategory = ref<number | null>(null)
const faqExpandedId = ref<number | null>(null)
const faqSearching = ref(false)

// 弹窗状态
const showHandbook = ref(false)
const currentHandbook = ref<any>(null)
const handbookLoading = ref(false)

const showMapPreview = ref(false)
const currentMap = ref<any>(null)

const activeTab = ref('handbook')

// 加载数据
async function loadHandbooks() {
  try {
    const res: any = await getHandbookList()
    handbooks.value = res.data || []
  } catch {}
}

async function loadCampusMaps() {
  try {
    const res: any = await getCampusMapList()
    campusMaps.value = res.data || []
  } catch {}
}

async function loadGroups() {
  try {
    const userId = userStore.userInfo.id
    if (!userId) return
    const res: any = await getMyGroups(userId)
    groups.value = res.data || []
  } catch {}
}

// FAQ
async function loadFaqCategories() {
  try {
    const res: any = await getFaqCategories()
    faqCategories.value = res.data || []
  } catch {}
}

async function loadFaqByCategory(categoryId: number) {
  faqSelectedCategory.value = categoryId
  faqKeyword.value = ''
  faqSearching.value = true
  try {
    const res: any = await getFaqByCategory(categoryId)
    faqList.value = res.data || []
  } catch {
    faqList.value = []
  } finally {
    faqSearching.value = false
  }
}

async function handleFaqSearch() {
  if (!faqKeyword.value.trim()) {
    if (faqSelectedCategory.value) {
      await loadFaqByCategory(faqSelectedCategory.value)
    } else {
      faqList.value = []
    }
    return
  }
  faqSelectedCategory.value = null
  faqSearching.value = true
  try {
    const res: any = await searchFaq(faqKeyword.value.trim())
    faqList.value = res.data || []
  } catch {
    faqList.value = []
  } finally {
    faqSearching.value = false
  }
}

function toggleFaq(item: any) {
  if (faqExpandedId.value === item.id) {
    faqExpandedId.value = null
  } else {
    faqExpandedId.value = item.id
    // 计数+1
    hitFaq(item.id).catch(() => {})
  }
}

// 手册详情
async function openHandbook(id: number) {
  handbookLoading.value = true
  showHandbook.value = true
  try {
    const res: any = await getHandbookDetail(id)
    currentHandbook.value = res.data
  } catch {
    currentHandbook.value = null
  } finally {
    handbookLoading.value = false
  }
}

// 地图预览
function openMap(map: any) {
  currentMap.value = map
  showMapPreview.value = true
}

// 根据路由参数设置 activeTab
function updateActiveTabFromRoute() {
  const tab = route.query.tab as string || 'discover'
  if (tab.startsWith('discover-')) {
    const subTab = tab.replace('discover-', '')
    // 映射子标签
    const tabMap: Record<string, string> = {
      'handbook': 'handbook',
      'map': 'map',
      'group': 'group',
      'faq': 'faq',
      'common-faq': 'faq' // 常见问题也显示在 FAQ 标签下
    }
    activeTab.value = tabMap[subTab] || 'handbook'
  } else if (tab === 'discover') {
    activeTab.value = 'handbook'
  }
}

// 监听路由变化
watch(() => route.query.tab, () => {
  updateActiveTabFromRoute()
}, { immediate: true })

onMounted(() => {
  loadHandbooks()
  loadCampusMaps()
  loadGroups()
  loadFaqCategories()
})

const groupTypeMap: Record<number, string> = { 1: '生源地群', 2: '学院群', 3: '班级群' }
</script>

<template>
  <div class="student-discover">
    <div class="page-title">发现</div>

    <!-- Tab 切换 -->
    <div class="tab-bar">
      <span :class="['tab-item', activeTab === 'handbook' ? 'active' : '']" @click="activeTab = 'handbook'">入学手册</span>
      <span :class="['tab-item', activeTab === 'map' ? 'active' : '']" @click="activeTab = 'map'">校园地图</span>
      <span :class="['tab-item', activeTab === 'group' ? 'active' : '']" @click="activeTab = 'group'">新生群</span>
      <span :class="['tab-item', activeTab === 'faq' ? 'active' : '']" @click="activeTab = 'faq'">智能问答</span>
    </div>

    <!-- 入学手册 -->
    <div v-if="activeTab === 'handbook'">
      <div v-if="handbooks.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5">
          <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
        </svg>
        <p>暂无入学手册</p>
      </div>
      <div v-else class="card-list">
        <div v-for="h in handbooks" :key="h.id" class="handbook-card" @click="openHandbook(h.id)">
          <div v-if="h.coverImage" class="cover">
            <img :src="h.coverImage" :alt="h.title" />
          </div>
          <div v-else class="cover placeholder">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="var(--gray-400)" stroke-width="1.5">
              <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
            </svg>
          </div>
          <div class="handbook-info">
            <h4>{{ h.title }}</h4>
          </div>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--gray-400)" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
        </div>
      </div>
    </div>

    <!-- 校园地图 -->
    <div v-if="activeTab === 'map'">
      <div v-if="campusMaps.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5">
          <polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"/>
          <line x1="8" y1="2" x2="8" y2="18"/><line x1="16" y1="6" x2="16" y2="22"/>
        </svg>
        <p>暂无校园地图</p>
      </div>
      <div v-else class="map-grid">
        <div v-for="m in campusMaps" :key="m.id" class="map-card" @click="openMap(m)">
          <div class="map-preview">
            <img :src="m.mapImage" :alt="m.mapName" />
          </div>
          <div class="map-info">
            <span class="map-name">{{ m.mapName }}</span>
            <span v-if="m.description" class="map-desc">{{ m.description }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 新生群 -->
    <div v-if="activeTab === 'group'">
      <div v-if="groups.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <circle cx="9" cy="7" r="4"/>
          <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
          <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
        </svg>
        <p>暂无群聊信息</p>
      </div>
      <div v-else class="group-list">
        <div v-for="g in groups" :key="g.id" class="group-card">
          <div class="group-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </div>
          <div class="group-info">
            <span class="group-type">{{ groupTypeMap[g.groupType] || '群聊' }}</span>
            <span class="group-name">{{ g.groupName }}</span>
            <span v-if="g.groupNo" class="group-no">群号：{{ g.groupNo }}</span>
            <span v-else class="group-no">暂未设置群号</span>
            <!-- 辅导员信息 -->
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

    <!-- 智能问答 -->
    <div v-if="activeTab === 'faq'" class="faq-entry">
      <div class="faq-promo" @click="$router.push({ path: '/student', query: { tab: 'faq' } })">
        <div class="faq-promo-icon">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
        </div>
        <div class="faq-promo-info">
          <h4>智能问答助手</h4>
          <p>输入问题，AI 助手帮你快速找到答案</p>
        </div>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--gray-400)" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
      </div>
    </div>

    <!-- 手册详情弹窗 -->
    <teleport to="body">
      <div v-if="showHandbook" class="modal-mask" @click.self="showHandbook = false">
        <div class="detail-modal">
          <div class="modal-header">
            <h4>{{ currentHandbook?.title || '入学手册' }}</h4>
            <button class="close-btn" @click="showHandbook = false">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div v-if="handbookLoading" class="loading-state">
              <div class="loading-spinner"></div>
              <span>加载中...</span>
            </div>
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
          <div class="preview-header">
            <h4>{{ currentMap.mapName }}</h4>
            <button class="close-btn" @click="showMapPreview = false">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="preview-body">
            <img :src="currentMap.mapImage" :alt="currentMap.mapName" class="preview-image" />
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.student-discover { padding-bottom: 8px; }
.page-title { font-size: 18px; font-weight: 700; color: var(--gray-900); margin-bottom: 16px; }

/* Tab */
.tab-bar {
  display: flex; gap: 8px; margin-bottom: 16px; background: #fff;
  border-radius: 12px; padding: 4px; border: 1px solid var(--gray-200);
}
.tab-item {
  flex: 1; text-align: center; padding: 10px 8px; border-radius: 10px;
  font-size: 14px; font-weight: 500; color: var(--gray-400);
  cursor: pointer; transition: all 0.2s;
}
.tab-item.active { background: var(--primary); color: #fff; }

/* Empty */
.empty-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  min-height: 200px; text-align: center; color: var(--gray-400);
}
.empty-state p { margin: 12px 0 0; font-size: 14px; }

/* Handbook */
.card-list { display: flex; flex-direction: column; gap: 12px; }
.handbook-card {
  display: flex; align-items: center; gap: 14px;
  background: #fff; border: 1px solid var(--gray-200); border-radius: 14px;
  padding: 14px; cursor: pointer; transition: box-shadow 0.2s;
}
.handbook-card:active { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.cover {
  width: 64px; height: 64px; border-radius: 10px; overflow: hidden;
  background: var(--gray-100); flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
}
.cover img { width: 100%; height: 100%; object-fit: cover; }
.cover.placeholder { color: var(--gray-400); }
.handbook-info { flex: 1; display: flex; flex-direction: column; }
.handbook-info h4 { font-size: 15px; font-weight: 600; color: var(--gray-900); margin: 0 0 4px; }
.meta { font-size: 12px; color: var(--gray-400); }

/* Map */
.map-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.map-card {
  background: #fff; border: 1px solid var(--gray-200); border-radius: 14px;
  overflow: hidden; cursor: pointer; transition: box-shadow 0.2s;
}
.map-card:active { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.map-preview {
  width: 100%; aspect-ratio: 4/3; background: var(--gray-100);
  display: flex; align-items: center; justify-content: center; overflow: hidden;
}
.map-preview img { width: 100%; height: 100%; object-fit: cover; }
.map-info { padding: 12px 14px; }
.map-name { display: block; font-size: 14px; font-weight: 600; color: var(--gray-900); }
.map-desc { display: block; font-size: 12px; color: var(--gray-400); margin-top: 2px; }

/* Group */
.group-list { display: flex; flex-direction: column; gap: 12px; }
.group-card {
  display: flex; align-items: center; gap: 14px;
  background: #fff; border: 1px solid var(--gray-200); border-radius: 14px;
  padding: 16px;
}
.group-icon {
  width: 44px; height: 44px; border-radius: 12px;
  background: rgba(26,92,255,0.1); color: var(--primary);
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.group-info { flex: 1; display: flex; flex-direction: column; }
.group-type { font-size: 11px; color: var(--gray-400); }
.group-name { font-size: 14px; font-weight: 600; color: var(--gray-900); }
.group-no { font-size: 12px; color: var(--gray-400); margin-top: 2px; }
.counselor-info {
  display: flex; flex-direction: column; gap: 2px;
  margin-top: 8px; padding-top: 8px; border-top: 1px solid var(--gray-100);
}
.counselor-label { font-size: 13px; font-weight: 500; color: var(--primary); }
.counselor-detail { font-size: 12px; color: var(--gray-400); }
.group-qr {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  cursor: pointer;
}
.group-qr img { width: 48px; height: 48px; border-radius: 6px; border: 1px solid var(--gray-200); }
.group-qr span { font-size: 11px; color: var(--primary); }

/* FAQ */
.faq-search { display: flex; gap: 10px; margin-bottom: 14px; }
.search-box {
  flex: 1; display: flex; align-items: center; gap: 8px;
  background: #fff; border: 1px solid var(--gray-200); border-radius: 10px;
  padding: 0 12px;
}
.search-box svg { color: var(--gray-400); flex-shrink: 0; }
.search-box input { border: none; outline: none; padding: 10px 0; font-size: 14px; width: 100%; background: transparent; }
.search-btn {
  padding: 10px 20px; border: none; border-radius: 10px;
  background: var(--primary); color: #fff; font-size: 14px; font-weight: 500;
  cursor: pointer; white-space: nowrap;
}
.search-btn:active { opacity: 0.85; }

.faq-categories { display: flex; gap: 8px; margin-bottom: 14px; flex-wrap: wrap; }
.cat-tag {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 6px 14px; border-radius: 20px; font-size: 13px; font-weight: 500;
  background: #fff; color: var(--gray-600); border: 1px solid var(--gray-200);
  cursor: pointer; transition: all 0.2s;
}
.cat-tag.active { background: var(--primary); color: #fff; border-color: var(--primary); }
.cat-count { font-size: 11px; opacity: 0.7; }

.faq-list { display: flex; flex-direction: column; gap: 10px; }
.faq-item {
  background: #fff; border: 1px solid var(--gray-200); border-radius: 12px;
  overflow: hidden; cursor: pointer; transition: box-shadow 0.2s;
}
.faq-item:active { box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.faq-item.expanded { border-color: rgba(26,92,255,0.2); }
.faq-question {
  display: flex; align-items: center; gap: 10px;
  padding: 14px 16px;
}
.faq-question span { flex: 1; font-size: 14px; font-weight: 500; color: var(--gray-900); }
.expand-arrow { flex-shrink: 0; transition: transform 0.2s; color: var(--gray-400); }
.expand-arrow.open { transform: rotate(180deg); }
.faq-answer {
  padding: 0 16px 14px 42px;
  font-size: 14px; line-height: 1.7; color: var(--gray-600);
}
.expand-enter-active { transition: all 0.25s ease; overflow: hidden; }
.expand-leave-active { transition: all 0.2s ease; overflow: hidden; }
.expand-enter-from, .expand-leave-to { opacity: 0; max-height: 0; }
.expand-enter-to, .expand-leave-from { opacity: 1; max-height: 500px; }

.faq-hint { text-align: center; padding: 32px; color: var(--gray-400); }
.faq-hint p { font-size: 13px; margin: 0; }

.faq-entry { padding: 0; }
.faq-promo {
  display: flex; align-items: center; gap: 16px;
  background: linear-gradient(135deg, var(--primary), var(--primary-deep));
  border-radius: 14px; padding: 24px 20px; color: #fff;
  cursor: pointer; transition: transform 0.2s;
}
.faq-promo:active { transform: scale(0.98); }
.faq-promo-icon {
  width: 56px; height: 56px; border-radius: 50%;
  background: rgba(255,255,255,0.2); display: flex; align-items: center;
  justify-content: center; flex-shrink: 0;
}
.faq-promo-info { flex: 1; }
.faq-promo-info h4 { font-size: 16px; font-weight: 700; margin: 0 0 4px; }
.faq-promo-info p { font-size: 13px; opacity: 0.8; margin: 0; }

/* Modals */
.modal-mask {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.detail-modal {
  background: #fff; border-radius: 14px; width: 90vw; max-width: 600px;
  max-height: 80vh; display: flex; flex-direction: column;
  box-shadow: 0 12px 40px rgba(0,0,0,0.2);
}
.preview-modal {
  background: #fff; border-radius: 14px; width: 90vw; max-width: 500px;
  max-height: 85vh; display: flex; flex-direction: column;
  box-shadow: 0 12px 40px rgba(0,0,0,0.2);
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 18px 20px; border-bottom: 1px solid var(--gray-200); flex-shrink: 0;
}
.modal-header h4 { font-size: 16px; font-weight: 600; color: var(--gray-900); margin: 0; }
.close-btn {
  background: none; border: none; color: var(--gray-400); cursor: pointer;
  padding: 4px; border-radius: 6px;
}
.close-btn:hover { background: var(--gray-100); }
.modal-body { flex: 1; overflow-y: auto; padding: 20px; }

.loading-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  min-height: 200px; color: var(--gray-400); gap: 12px;
}
.loading-spinner {
  width: 28px; height: 28px; border: 3px solid var(--gray-200);
  border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.handbook-content { font-size: 15px; line-height: 1.8; color: var(--gray-600); }
.handbook-content :deep(img) { max-width: 100%; border-radius: 8px; margin: 12px 0; }
.handbook-content :deep(h1),
.handbook-content :deep(h2),
.handbook-content :deep(h3) { color: var(--gray-900); margin: 16px 0 8px; }
.handbook-content :deep(p) { margin: 8px 0; }
.handbook-content :deep(table) { width: 100%; border-collapse: collapse; margin: 12px 0; }
.handbook-content :deep(th),
.handbook-content :deep(td) { border: 1px solid var(--gray-200); padding: 8px 12px; text-align: left; font-size: 14px; }
.handbook-content :deep(th) { background: var(--gray-100); }
.empty-content { text-align: center; padding: 40px; color: var(--gray-400); }

.preview-body { padding: 16px; text-align: center; }
.preview-image { max-width: 100%; max-height: 70vh; border-radius: 8px; object-fit: contain; }
</style>
