<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRoute } from 'vue-router'
import { computed } from 'vue'
import { getDashboardStats } from '@/api/dashboard'
import CollegeManage from './CollegeManage.vue'
import MajorManage from './MajorManage.vue'
import ClassManage from './ClassManage.vue'
import CounselorManage from './CounselorManage.vue'
import VolunteerManage from './VolunteerManage.vue'
import AdminManage from './AdminManage.vue'
import StudentImport from './StudentImport.vue'

import FeeConfig from './FeeConfig.vue'
import DormData from './DormData.vue'
import DormAlloc from './DormAlloc.vue'
import DormTime from './DormTime.vue'
import CmsHandbook from './CmsHandbook.vue'
import CmsMap from './CmsMap.vue'
import CmsNotice from './CmsNotice.vue'
import GroupManage from './GroupManage.vue'
import FaqManage from './FaqManage.vue'
import PersonalCenter from './PersonalCenter.vue'
import ClassAssign from './ClassAssign.vue'

const userStore = useUserStore()
const route = useRoute()

const tab = computed(() => (route.query.tab as string) || 'dashboard')

const stats = ref({
  totalStudents: 0,
  reportedStudents: 0,
  unreportedStudents: 0,
  reportRate: 0,
  dormRate: 0,
  totalColleges: 0,
  totalCounselors: 0,
  totalVolunteers: 0,
})

async function loadStats() {
  try {
    const res: any = await getDashboardStats()
    stats.value = res.data
  } catch {}
}

onMounted(() => {
  if (tab.value === 'dashboard') loadStats()
})

const componentMap: Record<string, any> = {
  college: CollegeManage,
  major: MajorManage,
  class: ClassManage,
  'class-assign': ClassAssign,
  counselor: CounselorManage,
  volunteer: VolunteerManage,
  admin: AdminManage,
  'student-import': StudentImport,
  
  fee: FeeConfig,
  'dorm-data': DormData,
  'dorm-alloc': DormAlloc,
  'dorm-time': DormTime,
  handbook: CmsHandbook,
  'campus-map': CmsMap,
  notice: CmsNotice,
  group: GroupManage,
  faq: FaqManage,
  profile: PersonalCenter,
}

const currentComponent = computed(() => componentMap[tab.value] || null)

const pageTitles: Record<string, string> = {
  dashboard: '工作台',
  'dashboard-screen': '数据大屏',
  college: '学院管理',
  major: '专业管理',
  class: '班级管理',
  'class-assign': '班级分配',
  counselor: '辅导员管理',
  volunteer: '志愿者管理',
  admin: '管理员管理',
  'student-import': '新生数据导入',
  
  fee: '费用配置',
  'dorm-data': '宿舍数据',
  'dorm-alloc': '分配管理',
  'dorm-time': '选房时间管理',
  handbook: '迎新手册',
  'campus-map': '校园地图',
  notice: '公告管理',
  group: '新生群管理',
  faq: 'FAQ知识库',
  profile: '个人中心',
}
</script>

<template>
  <div class="admin-content">
    <!-- Dashboard -->
    <template v-if="tab === 'dashboard'">
      <div class="welcome-card">
        <h2>欢迎回来，{{ userStore.userInfo.realName || '管理员' }}</h2>
        <p>光谷启航 · 新生一站式服务平台管理后台</p>
      </div>

      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon blue">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.totalStudents.toLocaleString() }}</span>
            <span class="stat-label">新生总数</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon green">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.reportedStudents.toLocaleString() }}</span>
            <span class="stat-label">已报到 ({{ stats.reportRate }}%)</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon orange">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.unreportedStudents.toLocaleString() }}</span>
            <span class="stat-label">未报到</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon purple">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
              <polyline points="9 22 9 12 15 12 15 22"/>
            </svg>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.dormRate }}%</span>
            <span class="stat-label">宿舍入住率</span>
          </div>
        </div>
      </div>

      <!-- Extra stats row -->
      <div class="stats-grid extra">
        <div class="stat-card mini">
          <div class="mini-value">{{ stats.totalColleges }}</div>
          <div class="mini-label">学院数</div>
        </div>
        <div class="stat-card mini">
          <div class="mini-value">{{ stats.totalCounselors }}</div>
          <div class="mini-label">辅导员</div>
        </div>
        <div class="stat-card mini">
          <div class="mini-value">{{ stats.totalVolunteers }}</div>
          <div class="mini-label">志愿者</div>
        </div>
      </div>
    </template>

    <!-- Dynamic component -->
    <component v-else-if="currentComponent" :is="currentComponent" />

    <!-- Placeholder -->
    <template v-else>
      <div class="placeholder-page">
        <div class="placeholder-icon">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
        </div>
        <h3>{{ pageTitles[tab] || tab }}</h3>
        <p>该功能模块正在开发中，敬请期待</p>
      </div>
    </template>
  </div>
</template>

<style scoped>
.admin-content {
  max-width: 1800px;
  margin: 0 auto;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 22px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid var(--gray-200);
  transition: box-shadow 0.2s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon.blue {
  background: rgba(26, 92, 255, 0.1);
  color: var(--primary);
}

.stat-icon.green {
  background: rgba(0, 201, 167, 0.1);
  color: var(--success);
}

.stat-icon.orange {
  background: rgba(255, 122, 26, 0.1);
  color: var(--secondary-orange);
}

.stat-icon.purple {
  background: rgba(130, 80, 255, 0.1);
  color: #8250FF;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--gray-900);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--gray-400);
  margin-top: 4px;
}

.welcome-card {
  background: linear-gradient(135deg, #1A5CFF 0%, #0033CC 50%, #00C9A7 100%);
  border-radius: 14px;
  padding: 36px 40px;
  color: #fff;
}

.welcome-card h2 {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 6px 0;
}

.welcome-card p {
  font-size: 14px;
  opacity: 0.75;
  margin: 0;
}

.stats-grid.extra {
  grid-template-columns: repeat(3, 1fr);
  margin-bottom: 20px;
}

.stat-card.mini {
  padding: 16px 20px;
  text-align: center;
}

.mini-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--gray-900);
  line-height: 1.2;
}

.mini-label {
  font-size: 13px;
  color: var(--gray-400);
  margin-top: 4px;
}

.placeholder-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  text-align: center;
}

.placeholder-icon {
  margin-bottom: 16px;
  opacity: 0.6;
}

.placeholder-page h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--gray-900);
  margin: 0 0 8px 0;
}

.placeholder-page p {
  font-size: 14px;
  color: var(--gray-400);
  margin: 0;
}

@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
