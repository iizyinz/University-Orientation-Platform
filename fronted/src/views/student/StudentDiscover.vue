<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const isWebLayout = computed(() => route.path.startsWith('/student-web'))

const modules = [
  { key: 'handbook', title: '新生入学手册', desc: '了解学校概况与入学须知', icon: 'book', color: 'blue' },
  { key: 'map', title: '校园地图', desc: '查看校园平面导览', icon: 'map', color: 'green' },
  { key: 'group', title: '新生群', desc: '加入班级群和学院群', icon: 'group', color: 'orange' },
  { key: 'faq', title: '智能问答', desc: '有问题？AI助手帮你解答', icon: 'faq', color: 'purple' },
  { key: 'common-faq', title: '常见问题', desc: '浏览常见问题快速找答案', icon: 'common', color: 'teal' },
]

function goToSubPage(key: string) {
  if (isWebLayout.value) {
    router.push({ path: '/student-web', query: { tab: `discover-${key}` } })
  } else {
    router.push({ path: '/student', query: { tab: `discover-${key}` } })
  }
}
</script>

<template>
  <div class="student-discover">
    <div class="page-title">发现</div>

    <div class="module-list">
      <div v-for="m in modules" :key="m.key" class="module-card" @click="goToSubPage(m.key)">
        <div :class="['module-icon', m.color]">
          <div v-html="getModuleIcon(m.icon)"></div>
        </div>
        <div class="module-info">
          <span class="module-title">{{ m.title }}</span>
          <span class="module-desc">{{ m.desc }}</span>
        </div>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--gray-400)" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
function getModuleIcon(type: string): string {
  const icons: Record<string, string> = {
    book: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>`,
    map: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"/><line x1="8" y1="2" x2="8" y2="18"/><line x1="16" y1="6" x2="16" y2="22"/></svg>`,
    group: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
    faq: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>`,
    common: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>`,
  }
  return icons[type] || ''
}
</script>

<style scoped>
.student-discover { padding-bottom: 8px; }
.page-title { font-size: 18px; font-weight: 700; color: var(--gray-900); margin-bottom: 16px; }

.module-list { display: flex; flex-direction: column; gap: 10px; }

.module-card {
  display: flex; align-items: center; gap: 14px;
  background: #fff; border: 1px solid var(--gray-200); border-radius: 14px;
  padding: 16px; cursor: pointer; transition: box-shadow 0.2s;
}
.module-card:active { box-shadow: 0 2px 8px rgba(0,0,0,0.04); }

.module-icon {
  width: 44px; height: 44px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.module-icon.blue { background: rgba(26,92,255,0.1); color: var(--primary); }
.module-icon.green { background: rgba(0,201,167,0.1); color: var(--success); }
.module-icon.orange { background: rgba(255,122,26,0.1); color: var(--secondary-orange); }
.module-icon.purple { background: rgba(130,80,255,0.1); color: #8250FF; }
.module-icon.teal { background: rgba(0,201,167,0.1); color: var(--success); }

.module-info { flex: 1; display: flex; flex-direction: column; }
.module-title { font-size: 14px; font-weight: 600; color: var(--gray-900); }
.module-desc { font-size: 12px; color: var(--gray-400); margin-top: 2px; }
</style>
