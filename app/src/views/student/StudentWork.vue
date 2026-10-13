<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const modules = [
  { key: 'info', title: '信息填报', desc: '完善个人基础信息、联系方式、家庭资料', icon: 'info', color: 'blue' },
  { key: 'fee', title: '学费查看', desc: '查看各项费用明细和缴费状态', icon: 'fee', color: 'green' },
  { key: 'dorm', title: '宿舍选择', desc: '在线选择宿舍和床位', icon: 'dorm', color: 'orange' },
]

const goTo = (key: string) => {
  if (key === 'info' || key === 'fee' || key === 'dorm') {
    router.push({ path: '/student', query: { tab: key } })
  }
}
</script>

<template>
  <div class="student-work">
    <div class="page-title">办事大厅</div>
    <p class="page-desc">在线完成入学相关事项</p>

    <div class="module-list">
      <div v-for="m in modules" :key="m.key" class="module-card" @click="goTo(m.key)">
        <div :class="['module-icon', m.color]">
          <div v-html="getModuleIcon(m.icon)"></div>
        </div>
        <div class="module-info">
          <h4>{{ m.title }}</h4>
          <p>{{ m.desc }}</p>
        </div>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--gray-400)" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
function getModuleIcon(type: string): string {
  const icons: Record<string, string> = {
    info: `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>`,
    fee: `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>`,
    dorm: `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><path d="M9 22V12h6v10"/></svg>`,
  }
  return icons[type] || ''
}
</script>

<style scoped>
.student-work { padding-bottom: 8px; }
.page-title { font-size: 18px; font-weight: 700; color: var(--gray-900); margin-bottom: 4px; }
.page-desc { font-size: 13px; color: var(--gray-400); margin: 0 0 20px; }

.module-list { display: flex; flex-direction: column; gap: 12px; }

.module-card {
  display: flex; align-items: center; gap: 16px;
  background: #fff; border: 1px solid var(--gray-200); border-radius: 14px;
  padding: 18px; cursor: pointer; transition: box-shadow 0.2s;
}
.module-card:active { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }

.module-icon {
  width: 50px; height: 50px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.module-icon.blue { background: rgba(26,92,255,0.1); color: var(--primary); }
.module-icon.green { background: rgba(0,201,167,0.1); color: var(--success); }
.module-icon.orange { background: rgba(255,122,26,0.1); color: var(--secondary-orange); }

.module-info { flex: 1; display: flex; flex-direction: column; }
.module-info h4 { font-size: 15px; font-weight: 600; color: var(--gray-900); margin: 0 0 4px; }
.module-info p { font-size: 12px; color: var(--gray-400); margin: 0; }
</style>
