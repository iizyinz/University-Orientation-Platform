<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getCounselorClasses } from '@/api/counselor'

const userStore = useUserStore()

interface ClassItem {
  id: number
  classId: number
  className: string
  majorName: string
  collegeName: string
  studentCount: number
  reportedCount: number
}

const classList = ref<ClassItem[]>([])
const loading = ref(true)

const getReportRate = (reported: number, total: number) => {
  return total > 0 ? Math.round((reported / total) * 100) : 0
}

async function loadClasses() {
  loading.value = true
  try {
    const userId = userStore.userInfo.id
    if (!userId) return
    const res: any = await getCounselorClasses(userId)
    classList.value = (res.data || []).map((c: any) => ({
      id: c.id,
      classId: c.classId,
      className: c.className,
      majorName: c.majorName || '-',
      collegeName: c.collegeName || '-',
      studentCount: c.studentCount || 0,
      reportedCount: c.reportedCount || 0,
    }))
  } catch {
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadClasses()
})
</script>

<template>
  <div class="my-class">
    <div class="page-header">
      <h3>我的班级</h3>
      <p class="subtitle">查看您负责的班级信息及报到进度</p>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>

    <div v-else-if="classList.length === 0" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5">
        <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
      </svg>
      <p>暂无分配的班级</p>
    </div>

    <div v-else class="class-grid">
      <div v-for="cls in classList" :key="cls.classId" class="class-card">
        <div class="card-header">
          <div class="class-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
            </svg>
          </div>
          <div class="class-info">
            <h4>{{ cls.className }}</h4>
            <span class="meta">{{ cls.collegeName }} · {{ cls.majorName }}</span>
          </div>
        </div>

        <div class="card-stats">
          <div class="stat-row">
            <span class="stat-label">班级人数</span>
            <span class="stat-value">{{ cls.studentCount }}人</span>
          </div>
          <div class="stat-row">
            <span class="stat-label">已报到</span>
            <span class="stat-value success">{{ cls.reportedCount }}人</span>
          </div>
          <div class="stat-row">
            <span class="stat-label">未报到</span>
            <span class="stat-value warning">{{ cls.studentCount - cls.reportedCount }}人</span>
          </div>
        </div>

        <div class="progress-section">
          <div class="progress-header">
            <span>报到进度</span>
            <span class="progress-rate">{{ getReportRate(cls.reportedCount, cls.studentCount) }}%</span>
          </div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: getReportRate(cls.reportedCount, cls.studentCount) + '%' }"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.my-class { max-width: 1600px; margin: 0 auto; }
.page-header { margin-bottom: 24px; }
.page-header h3 { font-size: 20px; font-weight: 600; color: var(--gray-900); margin: 0 0 4px; }
.subtitle { font-size: 14px; color: var(--gray-400); margin: 0; }

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: var(--gray-400);
  gap: 12px;
}
.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--gray-200);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  text-align: center;
  color: var(--gray-400);
}
.empty-state p { margin: 16px 0 0; font-size: 14px; }

.class-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 16px; }

.class-card {
  background: #fff;
  border: 1px solid var(--gray-200);
  border-radius: 12px;
  padding: 20px;
  transition: box-shadow 0.2s;
}
.class-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.06); }

.card-header { display: flex; align-items: flex-start; gap: 14px; margin-bottom: 16px; }
.class-icon {
  width: 44px; height: 44px; border-radius: 10px;
  background: rgba(26,92,255,0.1); color: var(--primary);
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.class-info h4 { font-size: 15px; font-weight: 600; color: var(--gray-900); margin: 0 0 4px; }
.meta { font-size: 12px; color: var(--gray-400); }

.card-stats { display: flex; flex-direction: column; gap: 8px; margin-bottom: 16px; }
.stat-row { display: flex; justify-content: space-between; align-items: center; }
.stat-label { font-size: 13px; color: var(--gray-400); }
.stat-value { font-size: 14px; font-weight: 600; color: var(--gray-600); }
.stat-value.success { color: var(--success); }
.stat-value.warning { color: var(--secondary-orange); }

.progress-section { border-top: 1px solid var(--gray-200); padding-top: 12px; }
.progress-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
.progress-header span { font-size: 12px; color: var(--gray-400); }
.progress-rate { font-weight: 600; color: var(--primary) !important; }

.progress-bar { height: 6px; background: var(--gray-100); border-radius: 3px; overflow: hidden; }
.progress-fill { height: 100%; background: linear-gradient(90deg, var(--primary), var(--success)); border-radius: 3px; transition: width 0.3s; }
</style>
