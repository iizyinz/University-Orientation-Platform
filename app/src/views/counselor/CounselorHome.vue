<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { getCounselorDashboardStats, getCounselorClasses } from '@/api/counselor'
import { getNotices, getNoticeDetail } from '@/api/student'
import ShadowContent from '@/components/ShadowContent.vue'

const userStore = useUserStore()
const router = useRouter()
const userInfo = computed(() => userStore.userInfo)

const stats = ref({
  studentCount: 0,
  classCount: 0,
  checkedInCount: 0,
  pendingCount: 0,
  reportRate: 0,
  counselorName: '',
  collegeName: '',
})
const classList = ref<any[]>([])
const notices = ref<any[]>([])
const statsLoading = ref(true)

const noticeTypeMap: Record<number, string> = { 3: '紧急', 2: '公告', 1: '通知' }

// 公告详情
const showNoticeDetail = ref(false)
const currentNotice = ref<any>(null)
const noticeLoading = ref(false)

async function loadDashboard() {
  statsLoading.value = true
  try {
    const userId = userInfo.value.id
    console.log('[CounselorHome] userId:', userId)
    
    const [statsRes, classesRes, noticesRes]: any = await Promise.all([
      getCounselorDashboardStats(userId),
      getCounselorClasses(userId),
      getNotices(10),
    ])
    
    console.log('[CounselorHome] statsRes:', statsRes)
    console.log('[CounselorHome] classesRes:', classesRes)

    // Handle response data...
    if (statsRes.data) {
      const s = statsRes.data
      stats.value = {
        studentCount: s.totalStudents || 0,
        classCount: s.totalClasses || 0,
        checkedInCount: s.reportedStudents || 0,
        pendingCount: s.unreportedStudents || 0,
        reportRate: s.reportRate || 0,
        counselorName: s.counselorName || '',
        collegeName: s.collegeName || '',
      }
    }

    classList.value = classesRes.data || []
    notices.value = noticesRes.data || []
  } catch (e) {
    console.error('[CounselorHome] LoadDashboard error:', e)
  } finally {
    statsLoading.value = false
  }
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
  router.push({ path: '/counselor', query: { tab } })
}

onMounted(loadDashboard)
</script>

<template>
  <div class="counselor-home">
    <!-- Welcome Banner -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h2>{{ userInfo.realName || '辅导员' }}，您好 👋</h2>
        <p>欢迎使用辅导员工作台</p>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-value">{{ stats.studentCount }}</div>
        <div class="stat-label">学生总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.classCount }}</div>
        <div class="stat-label">班级数</div>
      </div>
      <div class="stat-card success">
        <div class="stat-value">{{ stats.checkedInCount }}</div>
        <div class="stat-label">已报到</div>
      </div>
      <div class="stat-card warning">
        <div class="stat-value">{{ stats.pendingCount }}</div>
        <div class="stat-label">待报到</div>
      </div>
    </div>

    <!-- My Classes -->
    <div class="section-card">
      <div class="section-title">我的班级</div>
      <div v-if="classList.length === 0" class="empty-hint">暂无班级数据</div>
      <div v-else class="class-grid">
        <div v-for="cls in classList" :key="cls.id" class="class-card">
          <div class="class-name">{{ cls.className || cls.name }}</div>
          <div class="class-meta">{{ cls.studentCount || 0 }} 人</div>
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
.counselor-home { padding-bottom: 8px; }

.welcome-banner {
  background: linear-gradient(135deg, #1A5CFF, #0033CC, #00C9A7);
  border-radius: 14px; padding: 24px 20px; color: #fff; margin-bottom: 16px;
}
.welcome-banner h2 { font-size: 20px; font-weight: 700; margin: 0 0 4px; }
.welcome-banner p { font-size: 13px; opacity: 0.8; margin: 0; }

/* Stats Grid */
.stats-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin-bottom: 16px;
}
.stat-card {
  background: #fff; border-radius: 12px; padding: 14px 8px; text-align: center;
  border: 1px solid var(--gray-200);
}
.stat-value { font-size: 22px; font-weight: 700; color: var(--primary); }
.stat-card.success .stat-value { color: var(--success); }
.stat-card.warning .stat-value { color: var(--secondary-orange); }
.stat-label { font-size: 11px; color: var(--gray-400); margin-top: 4px; }

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
.quick-item span { font-size: 12px; color: var(--gray-600); }

/* Class List */
.class-list { display: flex; flex-direction: column; gap: 10px; }
.class-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 14px; background: var(--gray-100); border-radius: 10px;
}
.class-name { font-size: 14px; font-weight: 500; color: var(--gray-600); }
.class-meta { font-size: 12px; color: var(--gray-400); }

/* Class Grid - Double Column */
.class-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.class-card {
  padding: 14px; background: var(--gray-100); border-radius: 10px;
}
.class-card .class-name { font-size: 14px; font-weight: 500; color: var(--gray-900); }
.class-card .class-meta { font-size: 12px; color: var(--gray-400); margin-top: 4px; }

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
