<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import CounselorDashboard from './CounselorDashboard.vue'
import MyClass from './MyClass.vue'
import MyStudent from './MyStudent.vue'
import ScanCheckin from './ScanCheckin.vue'
import GroupManage from './GroupManage.vue'
import PersonalCenter from './PersonalCenter.vue'

const userStore = useUserStore()
const route = useRoute()
const tab = computed(() => (route.query.tab as string) || 'dashboard')

const componentMap: Record<string, any> = {
  dashboard: CounselorDashboard,
  'my-class': MyClass,
  'my-student': MyStudent,
  'scan-checkin': ScanCheckin,
  'group-manage': GroupManage,
  profile: PersonalCenter,
}

const currentComponent = computed(() => componentMap[tab.value] || null)
</script>

<template>
  <div class="counselor-content">
    <component :is="currentComponent" v-if="currentComponent" />
    <div v-else class="placeholder">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <p>该功能模块正在开发中，敬请期待</p>
    </div>
  </div>
</template>

<style scoped>
.counselor-content { max-width: 1600px; margin: 0 auto; }
.placeholder { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 400px; text-align: center; color: var(--gray-400); }
.placeholder p { margin: 16px 0 0; font-size: 14px; }
</style>
