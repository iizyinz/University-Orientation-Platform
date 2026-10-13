<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { getNotices, getNoticeDetail } from '@/api/student'
import ShadowContent from '@/components/ShadowContent.vue'

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
  router.push({ path: '/student', query: { tab } })
}

onMounted(loadNotices)
</script>

<template>
  <div class="student-home">
    <!-- Welcome Banner -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h2>Hi，{{ userStore.userInfo.realName || '新生' }} 👋</h2>
        <p>欢迎来到光谷启航 · 新生一站式服务平台</p>
      </div>
    </div>

    <!-- Quick Entries -->
    <div class="section-card">
      <div class="quick-grid">
        <div class="quick-item" @click="goTo('work')">
          <div class="quick-icon blue">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
          </div>
          <span>信息填报</span>
        </div>
        <div class="quick-item" @click="goTo('work')">
          <div class="quick-icon green">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
          </div>
          <span>学费查看</span>
        </div>
        <div class="quick-item" @click="goTo('work')">
          <div class="quick-icon orange">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><path d="M9 22V12h6v10"/></svg>
          </div>
          <span>宿舍选择</span>
        </div>
        <div class="quick-item" @click="$router.push({ path: '/student', query: { tab: 'discover-map' } })">
          <div class="quick-icon teal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"/><line x1="8" y1="2" x2="8" y2="18"/><line x1="16" y1="6" x2="16" y2="22"/></svg>
          </div>
          <span>学校地图</span>
        </div>
        <div class="quick-item" @click="$router.push({ path: '/student', query: { tab: 'discover-handbook' } })">
          <div class="quick-icon blue">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>
          </div>
          <span>新生手册</span>
        </div>
        <div class="quick-item" @click="$router.push({ path: '/student', query: { tab: 'discover-group' } })">
          <div class="quick-icon orange">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
          </div>
          <span>新生群</span>
        </div>
        <div class="quick-item" @click="goTo('faq')">
          <div class="quick-icon purple">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          </div>
          <span>智能问答</span>
        </div>
        <div class="quick-item" @click="$router.push({ path: '/student', query: { tab: 'common-faq' } })">
          <div class="quick-icon orange">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          </div>
          <span>常见问题</span>
        </div>
      </div>
    </div>

    <!-- Notices -->
    <div class="section-card">
      <div class="section-title">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>
        </svg>
        最新公告
      </div>
      <div v-if="notices.length === 0" class="empty-hint">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="var(--gray-300)" stroke-width="1.5">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>
        </svg>
        <span>暂无公告</span>
      </div>
      <div v-else class="notice-list">
        <div v-for="n in notices" :key="n.id" class="notice-item" @click="openNotice(n.id)">
          <div class="notice-left">
            <div :class="['notice-dot', n.noticeType === 3 ? 'urgent' : n.noticeType === 2 ? 'highlight' : '']"></div>
            <div class="notice-info">
              <div class="notice-title">{{ n.title }}</div>
              <div class="notice-meta-row">
                <span :class="['notice-tag', n.noticeType === 3 ? 'urgent' : n.noticeType === 2 ? 'highlight' : '']">
                  {{ noticeTypeMap[n.noticeType] || '通知' }}
                </span>
                <span class="notice-time">{{ n.publishTime?.substring(0, 10) }}</span>
              </div>
            </div>
          </div>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--gray-300)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
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
              <ShadowContent v-if="currentNotice.content" :content="currentNotice.content" />
              <div v-else class="empty-content">暂无详细内容</div>
            </template>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.student-home { padding-bottom: 8px; }

.welcome-banner {
  background: linear-gradient(135deg, #1A5CFF, #0033CC, #00C9A7);
  border-radius: 14px; padding: 24px 20px; color: #fff; margin-bottom: 16px;
}
.welcome-banner h2 { font-size: 20px; font-weight: 700; margin: 0 0 4px; }
.welcome-banner p { font-size: 13px; opacity: 0.8; margin: 0; }

.section-card {
  background: #fff; border-radius: 14px; padding: 18px;
  border: 1px solid var(--gray-200); margin-bottom: 16px;
}
.section-title {
  font-size: 15px; font-weight: 600; color: var(--gray-900); margin-bottom: 14px;
}
.empty-hint { text-align: center; padding: 16px; font-size: 13px; color: var(--gray-400); }

/* Quick Grid */
.quick-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.quick-item {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 12px 4px; border-radius: 10px; cursor: pointer; transition: background 0.2s;
}
.quick-item:active { background: var(--gray-100); }
.quick-icon {
  width: 42px; height: 42px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
}
.quick-icon.blue { background: rgba(26,92,255,0.1); color: var(--primary); }
.quick-icon.orange { background: rgba(255,122,26,0.1); color: var(--secondary-orange); }
.quick-icon.green { background: rgba(0,201,167,0.1); color: var(--success); }
.quick-icon.teal { background: rgba(0,201,167,0.1); color: var(--success); }
.quick-icon.purple { background: rgba(130,80,255,0.1); color: #8250FF; }
.quick-item span { font-size: 12px; color: var(--gray-600); }

/* Notices */
.notice-list { display: flex; flex-direction: column; gap: 2px; }
.notice-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 4px; border-bottom: 1px solid var(--gray-100);
  cursor: pointer; transition: background 0.15s;
}
.notice-item:last-child { border-bottom: none; }
.notice-item:active { background: var(--gray-50, #fafafa); }
.notice-left { display: flex; align-items: center; gap: 12px; flex: 1; min-width: 0; }
.notice-dot {
  width: 8px; height: 8px; border-radius: 50%; background: var(--primary);
  flex-shrink: 0;
}
.notice-dot.urgent { background: var(--danger); }
.notice-dot.highlight { background: var(--secondary-orange); }
.notice-info { flex: 1; min-width: 0; }
.notice-title {
  font-size: 14px; font-weight: 500; color: var(--gray-900);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 4px;
}
.notice-meta-row { display: flex; align-items: center; gap: 8px; }
.notice-tag {
  font-size: 11px; font-weight: 500; padding: 1px 6px; border-radius: 4px;
  background: rgba(26,92,255,0.08); color: var(--primary);
}
.notice-tag.urgent { background: rgba(255,77,106,0.1); color: var(--danger); }
.notice-tag.highlight { background: rgba(255,122,26,0.1); color: var(--secondary-orange); }
.notice-time { font-size: 12px; color: var(--gray-400); }

/* Notice Modal */
.modal-mask {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: flex-end; justify-content: center; z-index: 1000;
}
.notice-modal {
  background: #fff; border-radius: 16px 16px 0 0; width: 100%; max-width: 560px;
  max-height: 85vh; display: flex; flex-direction: column;
  box-shadow: 0 -4px 24px rgba(0,0,0,0.12);
  animation: slideUp 0.25s ease;
}
@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}
.modal-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  padding: 20px 20px 16px; border-bottom: 1px solid var(--gray-100); flex-shrink: 0;
}
.modal-title-row { display: flex; align-items: center; gap: 8px; flex: 1; }
.modal-title-row h4 { font-size: 17px; font-weight: 600; color: var(--gray-900); margin: 0; }
.notice-badge {
  font-size: 11px; font-weight: 500; padding: 2px 8px; border-radius: 4px;
  background: rgba(26,92,255,0.08); color: var(--primary); flex-shrink: 0;
}
.notice-badge.highlight { background: rgba(255,122,26,0.1); color: var(--secondary-orange); }
.notice-badge.urgent { background: rgba(255,77,106,0.1); color: var(--danger); }
.close-btn {
  background: var(--gray-100); border: none; color: var(--gray-500); cursor: pointer;
  padding: 6px; border-radius: 50%; flex-shrink: 0; margin-left: 12px;
}
.close-btn:active { background: var(--gray-200); }
.modal-body { flex: 1; overflow-y: auto; padding: 20px; }
.notice-meta {
  display: flex; gap: 16px; font-size: 12px; color: var(--gray-400);
  margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--gray-100);
}
.empty-content { text-align: center; padding: 32px; color: var(--gray-400); }

.loading-state { display: flex; justify-content: center; padding: 30px; }
.loading-spinner {
  width: 28px; height: 28px; border: 3px solid var(--gray-200);
  border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
