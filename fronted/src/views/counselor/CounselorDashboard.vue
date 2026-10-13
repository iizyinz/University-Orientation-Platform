<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getCounselorDashboardStats } from '@/api/counselor'

const userStore = useUserStore()

const stats = ref({
  totalStudents: 0,
  reportedStudents: 0,
  unreportedStudents: 0,
  reportRate: 0,
  totalClasses: 0,
  counselorName: '',
  collegeName: '',
})

const loading = ref(true)

async function loadStats() {
  loading.value = true
  try {
    const userId = userStore.userInfo.id
    if (!userId) return
    const res: any = await getCounselorDashboardStats(userId)
    stats.value = res.data
  } catch {
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
})
</script>

<template>
  <div class="dashboard">
    <div class="welcome-card">
      <h2>欢迎回来，{{ stats.counselorName || userStore.userInfo.realName || '辅导员' }}</h2>
      <p>光谷启航 · 新生一站式服务平台辅导员端{{ stats.collegeName ? ' · ' + stats.collegeName : '' }}</p>
    </div>
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon blue">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ loading ? '--' : stats.totalStudents }}</span>
          <span class="stat-label">所带学生总数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ loading ? '--' : stats.reportedStudents }}</span>
          <span class="stat-label">已报到</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ loading ? '--' : stats.unreportedStudents }}</span>
          <span class="stat-label">未报到</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ loading ? '--' : stats.totalClasses }}</span>
          <span class="stat-label">负责班级</span>
        </div>
      </div>
    </div>

    <div class="report-rate-card">
      <div class="rate-header">
        <span class="rate-title">整体报到率</span>
        <span class="rate-value">{{ loading ? '--' : stats.reportRate }}%</span>
      </div>
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: (loading ? 0 : stats.reportRate) + '%' }"></div>
      </div>
      <div class="rate-detail">
        <span>已报到 <strong>{{ loading ? '--' : stats.reportedStudents }}</strong> 人</span>
        <span>未报到 <strong>{{ loading ? '--' : stats.unreportedStudents }}</strong> 人</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard { max-width: 1600px; margin: 0 auto; }

.welcome-card {
  background: linear-gradient(135deg, #1A5CFF, #0033CC, #00C9A7);
  border-radius: 14px;
  padding: 32px 36px;
  color: #fff;
  margin-bottom: 20px;
}
.welcome-card h2 { font-size: 20px; font-weight: 600; margin: 0 0 6px; }
.welcome-card p { font-size: 14px; opacity: 0.75; margin: 0; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border: 1px solid var(--gray-200);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  transition: box-shadow 0.2s;
}
.stat-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.05); }

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-icon.blue { background: rgba(26,92,255,0.1); color: var(--primary); }
.stat-icon.green { background: rgba(0,201,167,0.1); color: var(--success); }
.stat-icon.orange { background: rgba(255,122,26,0.1); color: var(--secondary-orange); }
.stat-icon.purple { background: rgba(130,80,255,0.1); color: #8250FF; }

.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 24px; font-weight: 700; color: var(--gray-900); }
.stat-label { font-size: 12px; color: var(--gray-400); margin-top: 2px; }

.report-rate-card {
  background: #fff;
  border: 1px solid var(--gray-200);
  border-radius: 12px;
  padding: 24px;
}

.rate-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.rate-title { font-size: 15px; font-weight: 600; color: var(--gray-900); }
.rate-value { font-size: 20px; font-weight: 700; color: var(--primary); }

.progress-bar {
  height: 10px;
  background: var(--gray-100);
  border-radius: 5px;
  overflow: hidden;
  margin-bottom: 12px;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary), var(--success));
  border-radius: 5px;
  transition: width 0.5s ease;
}

.rate-detail {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--gray-400);
}
.rate-detail strong { color: var(--gray-600); font-weight: 600; }

@media (max-width: 900px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
