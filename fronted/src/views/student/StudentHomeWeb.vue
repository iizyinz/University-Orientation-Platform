<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { getNotices, getNoticeDetail } from '@/api/student'

const userStore = useUserStore()
const router = useRouter()

const notices = ref<any[]>([])
const noticeTypeMap: Record<number, string> = { 3: '紧急', 2: '公告', 1: '通知' }

// 公告详情
const showNoticeDetail = ref(false)
const currentNotice = ref<any>(null)
const noticeLoading = ref(false)

async function loadNotices() {
  try {
    const res: any = await getNotices(10)
    notices.value = res.data || []
  } catch {}
}

async function openNotice(id: number) {
  noticeLoading.value = true
  showNoticeDetail.value = true
  try {
    const res: any = await getNoticeDetail(id)
    currentNotice.value = res.data
  } catch {
    currentNotice.value = null
  } finally {
    noticeLoading.value = false
  }
}

const goTo = (tab: string) => {
  router.push({ path: '/student-web', query: { tab } })
}

onMounted(loadNotices)
</script>

<template>
  <div class="home-web">
    <!-- Welcome Banner -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h2>Hi，{{ userStore.userInfo.realName || '新生' }} 👋</h2>
        <p>欢迎来到光谷启航 · 新生一站式服务平台</p>
      </div>
    </div>

    <!-- Quick Entries -->
    <div class="quick-section">
      <h3 class="section-header">快捷入口</h3>
      <div class="quick-cards">
        <div class="quick-card" @click="goTo('info')">
          <div class="card-icon blue">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
            </svg>
          </div>
          <div class="card-info">
            <h4>信息填报</h4>
            <p>完善个人信息</p>
          </div>
        </div>

        <div class="quick-card" @click="goTo('fee')">
          <div class="card-icon green">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="1" x2="12" y2="23"/>
              <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
            </svg>
          </div>
          <div class="card-info">
            <h4>学费查看</h4>
            <p>查看缴费信息</p>
          </div>
        </div>

        <div class="quick-card" @click="goTo('dorm')">
          <div class="card-icon orange">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
              <path d="M9 22V12h6v10"/>
            </svg>
          </div>
          <div class="card-info">
            <h4>宿舍选择</h4>
            <p>在线选宿舍</p>
          </div>
        </div>

        <div class="quick-card" @click="goTo('discover-map')">
          <div class="card-icon teal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"/>
              <line x1="8" y1="2" x2="8" y2="18"/>
              <line x1="16" y1="6" x2="16" y2="22"/>
            </svg>
          </div>
          <div class="card-info">
            <h4>校园地图</h4>
            <p>探索校园</p>
          </div>
        </div>

        <div class="quick-card" @click="goTo('discover-handbook')">
          <div class="card-icon blue">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
              <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
            </svg>
          </div>
          <div class="card-info">
            <h4>新生手册</h4>
            <p>入学指南</p>
          </div>
        </div>

        <div class="quick-card" @click="goTo('discover-group')">
          <div class="card-icon orange">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </div>
          <div class="card-info">
            <h4>新生群</h4>
            <p>加入社群</p>
          </div>
        </div>

        <div class="quick-card" @click="goTo('discover-faq')">
          <div class="card-icon purple">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
              <line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
          </div>
          <div class="card-info">
            <h4>智能问答</h4>
            <p>AI助手</p>
          </div>
        </div>

        <div class="quick-card" @click="goTo('discover-common-faq')">
          <div class="card-icon gold">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
              <line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
          </div>
          <div class="card-info">
            <h4>常见问题</h4>
            <p>FAQ</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Notices -->
    <div class="notice-section">
      <div class="section-header-row">
        <h3 class="section-header">最新公告</h3>
        <span class="notice-count">{{ notices.length }} 条</span>
      </div>
      
      <div v-if="notices.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--gray-400)" stroke-width="1.5">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="16" y1="13" x2="8" y2="13"/>
          <line x1="16" y1="17" x2="8" y2="17"/>
        </svg>
        <p>暂无公告</p>
      </div>
      
      <div v-else class="notice-grid">
        <div 
          v-for="n in notices" 
          :key="n.id" 
          class="notice-card"
          :class="{ urgent: n.noticeType === 3, highlight: n.noticeType === 2 }"
          @click="openNotice(n.id)"
        >
          <div class="notice-card-header">
            <span class="type-tag" :class="{ urgent: n.noticeType === 3, highlight: n.noticeType === 2 }">
              {{ noticeTypeMap[n.noticeType] || '通知' }}
            </span>
            <span class="publish-date">{{ n.publishTime?.substring(0, 10) }}</span>
          </div>
          <h4 class="notice-card-title">{{ n.title }}</h4>
          <div class="notice-card-footer">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            <span>查看详情</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Notice Detail Modal -->
    <teleport to="body">
      <div v-if="showNoticeDetail" class="modal-mask" @click.self="showNoticeDetail = false">
        <div class="notice-modal">
          <div class="modal-header">
            <div class="modal-title-row">
              <span v-if="currentNotice" :class="['notice-badge', currentNotice.noticeType === 3 ? 'urgent' : currentNotice.noticeType === 2 ? 'highlight' : '']">
                {{ noticeTypeMap[currentNotice.noticeType] || '通知' }}
              </span>
              <h4>{{ currentNotice?.title || '公告详情' }}</h4>
            </div>
            <button class="close-btn" @click="showNoticeDetail = false">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div v-if="noticeLoading" class="loading-state">
              <div class="loading-spinner"></div>
            </div>
            <template v-else-if="currentNotice">
              <div class="notice-meta">
                <span v-if="currentNotice.publishTime">发布时间：{{ currentNotice.publishTime?.substring(0, 16).replace('T', ' ') }}</span>
                <span v-if="currentNotice.viewCount">浏览 {{ currentNotice.viewCount }} 次</span>
              </div>
              <div v-if="currentNotice.content" class="notice-content" v-html="currentNotice.content"></div>
              <div v-else class="empty-content">暂无详细内容</div>
            </template>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.home-web { 
  padding-bottom: 24px; 
  max-width: 1600px;
  margin: 0 auto;
}

.welcome-banner {
  background: linear-gradient(135deg, #1A5CFF, #0033CC, #00C9A7);
  border-radius: 14px; padding: 24px 20px; color: #fff; margin-bottom: 16px;
}
.welcome-banner h2 { font-size: 20px; font-weight: 700; margin: 0 0 4px; }
.welcome-banner p { font-size: 13px; opacity: 0.8; margin: 0; }

/* Quick Section */
.quick-section {
  margin-bottom: 32px;
}

.section-header {
  font-size: 20px;
  font-weight: 700;
  color: var(--gray-900);
  margin: 0 0 20px;
}

.quick-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.quick-card {
  background: #fff;
  border: 1px solid var(--gray-200);
  border-radius: 14px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-card:hover {
  border-color: var(--primary-light);
  box-shadow: 0 4px 16px rgba(26, 92, 255, 0.1);
  transform: translateY(-2px);
}

.quick-card:active {
  transform: translateY(0);
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-icon.blue {
  background: rgba(26, 92, 255, 0.1);
  color: var(--primary);
}

.card-icon.green {
  background: rgba(0, 201, 167, 0.1);
  color: var(--success);
}

.card-icon.orange {
  background: rgba(255, 122, 26, 0.1);
  color: var(--secondary-orange);
}

.card-icon.teal {
  background: rgba(0, 201, 167, 0.1);
  color: var(--success);
}

.card-icon.purple {
  background: rgba(130, 80, 255, 0.1);
  color: #8250FF;
}

.card-icon.gold {
  background: rgba(255, 170, 76, 0.1);
  color: var(--secondary-gold);
}

.card-info {
  flex: 1;
}

.card-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--gray-900);
  margin: 0 0 4px;
}

.card-info p {
  font-size: 13px;
  color: var(--gray-400);
  margin: 0;
}

/* Notice Section */
.notice-section {
  margin-bottom: 24px;
}

.section-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.notice-count {
  font-size: 14px;
  color: var(--gray-400);
  font-weight: 500;
}

.empty-state {
  background: #fff;
  border: 1px solid var(--gray-200);
  border-radius: 14px;
  padding: 48px;
  text-align: center;
}

.empty-state svg {
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 14px;
  color: var(--gray-400);
  margin: 0;
}

.notice-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.notice-card {
  background: #fff;
  border: 1px solid var(--gray-200);
  border-radius: 14px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.notice-card:hover {
  border-color: var(--primary-light);
  box-shadow: 0 4px 16px rgba(26, 92, 255, 0.1);
  transform: translateY(-2px);
}

.notice-card.urgent {
  border-left: 4px solid var(--danger);
}

.notice-card.highlight {
  border-left: 4px solid var(--secondary-orange);
}

.notice-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.type-tag {
  font-size: 12px;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 6px;
  background: var(--gray-100);
  color: var(--gray-400);
}

.type-tag.highlight {
  background: rgba(255, 122, 26, 0.1);
  color: var(--secondary-orange);
}

.type-tag.urgent {
  background: rgba(255, 77, 106, 0.1);
  color: var(--danger);
}

.publish-date {
  font-size: 12px;
  color: var(--gray-400);
}

.notice-card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--gray-900);
  margin: 0 0 16px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-card-footer {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--primary);
  font-weight: 500;
}

/* Notice Modal */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.notice-modal {
  background: #fff;
  border-radius: 16px;
  width: 90vw;
  max-width: 600px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px 24px;
  border-bottom: 1px solid var(--gray-200);
  flex-shrink: 0;
}

.modal-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.modal-title-row h4 {
  font-size: 17px;
  font-weight: 600;
  color: var(--gray-900);
  margin: 0;
}

.notice-badge {
  font-size: 12px;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 6px;
  background: var(--gray-100);
  color: var(--gray-400);
  flex-shrink: 0;
}

.notice-badge.highlight {
  background: rgba(255, 122, 26, 0.1);
  color: var(--secondary-orange);
}

.notice-badge.urgent {
  background: rgba(255, 77, 106, 0.1);
  color: var(--danger);
}

.close-btn {
  background: none;
  border: none;
  color: var(--gray-400);
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  flex-shrink: 0;
  transition: all 0.2s;
}

.close-btn:hover {
  background: var(--gray-100);
  color: var(--gray-600);
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.notice-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: var(--gray-400);
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--gray-100);
}

.notice-content {
  font-size: 15px;
  line-height: 1.8;
  color: var(--gray-600);
}

.notice-content :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 12px 0;
}

.notice-content :deep(h1),
.notice-content :deep(h2),
.notice-content :deep(h3) {
  color: var(--gray-900);
  margin: 20px 0 10px;
}

.notice-content :deep(p) {
  margin: 10px 0;
}

.notice-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}

.notice-content :deep(th),
.notice-content :deep(td) {
  border: 1px solid var(--gray-200);
  padding: 10px 14px;
  text-align: left;
  font-size: 14px;
}

.notice-content :deep(th) {
  background: var(--gray-100);
}

.empty-content {
  text-align: center;
  padding: 40px;
  color: var(--gray-400);
}

.loading-state {
  display: flex;
  justify-content: center;
  padding: 40px;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--gray-200);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .welcome-banner {
    padding: 24px;
  }

  .banner-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .greeting h1 {
    font-size: 22px;
  }

  .banner-decoration {
    display: none;
  }

  .quick-cards {
    grid-template-columns: 1fr;
  }

  .notice-grid {
    grid-template-columns: 1fr;
  }
}
</style>
