<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref('home')

const tabs = [
  { key: 'home', label: '首页' },
  { key: 'scan', label: '二维码', center: true },
  { key: 'profile', label: '用户' },
]

watch(() => route.query.tab, (val) => {
  activeTab.value = (val as string) || 'home'
}, { immediate: true })

const switchTab = (key: string) => {
  activeTab.value = key
  router.push({ path: '/counselor', query: { tab: key } })
}

const showLogoutConfirm = ref(false)
const handleLogout = () => { showLogoutConfirm.value = true }
const confirmLogout = () => {
  showLogoutConfirm.value = false
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="counselor-layout">
    <main class="counselor-content">
      <router-view />
    </main>

    <!-- Bottom Tab Bar -->
    <nav class="tab-bar">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        :class="['tab-item', { active: activeTab === tab.key, center: tab.center }]"
        @click="switchTab(tab.key)"
      >
        <!-- 中间圆形二维码按钮 -->
        <div v-if="tab.center" class="center-btn">
          <div class="center-icon" v-html="getTabIcon(tab.icon || tab.key)"></div>
        </div>
        <template v-else>
          <div class="tab-icon" v-html="getTabIcon(tab.key)"></div>
          <span class="tab-label">{{ tab.label }}</span>
        </template>
      </div>
    </nav>

    <!-- Logout Confirm -->
    <teleport to="body">
      <div v-if="showLogoutConfirm" class="modal-mask" @click.self="showLogoutConfirm = false">
        <div class="confirm-box">
          <div class="confirm-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#FF7A1A" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
              <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
          </div>
          <h4 class="confirm-title">确认退出？</h4>
          <p class="confirm-desc">退出后需重新登录才能使用系统</p>
          <div class="confirm-actions">
            <button class="btn btn-gray" @click="showLogoutConfirm = false">取消</button>
            <button class="btn btn-danger" @click="confirmLogout">确认退出</button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script lang="ts">
function getTabIcon(type: string): string {
  const icons: Record<string, string> = {
    home: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>`,
    scan: `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 7V5a2 2 0 0 1 2-2h2"/><path d="M17 3h2a2 2 0 0 1 2 2v2"/><path d="M21 17v2a2 2 0 0 1-2 2h-2"/><path d="M7 21H5a2 2 0 0 1-2-2v-2"/><line x1="7" y1="12" x2="17" y2="12"/></svg>`,
    profile: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`,
  }
  return icons[type] || ''
}
</script>

<style scoped>
.counselor-layout {
  display: flex;
  flex-direction: column;
  width: 100vw;
  height: 100vh;
  max-width: 480px;
  margin: 0 auto;
  background: var(--gray-100);
  position: relative;
  overflow: hidden;
}

.counselor-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  padding-bottom: 16px;
  -webkit-overflow-scrolling: touch;
}

/* ===== Bottom Tab Bar ===== */
.tab-bar {
  display: flex;
  align-items: flex-end;
  height: 64px;
  background: #fff;
  border-top: 1px solid var(--gray-200);
  flex-shrink: 0;
  padding-bottom: env(safe-area-inset-bottom, 0);
  position: relative;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  padding: 8px 0 6px;
  cursor: pointer;
  transition: color 0.2s;
  color: var(--gray-400);
  -webkit-tap-highlight-color: transparent;
}

.tab-item.active {
  color: var(--primary);
}

/* 中间圆形按钮 */
.tab-item.center {
  padding-top: 0;
  margin-top: -20px;
}

.center-btn {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--primary-deep));
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(26, 92, 255, 0.35);
  transition: transform 0.2s, box-shadow 0.2s;
}

.tab-item.center:active .center-btn {
  transform: scale(0.92);
}

.tab-item.center.active .center-btn {
  box-shadow: 0 4px 20px rgba(26, 92, 255, 0.5);
}

.center-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.tab-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
}

.tab-label {
  font-size: 11px;
  font-weight: 500;
  line-height: 1;
}

/* ===== Confirm Dialog ===== */
.modal-mask {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.confirm-box {
  background: #fff; border-radius: 14px; padding: 28px;
  width: 320px; max-width: 85vw; text-align: center;
  box-shadow: 0 12px 40px rgba(0,0,0,0.12);
}
.confirm-icon {
  width: 56px; height: 56px; border-radius: 50%;
  background: rgba(255,122,26,0.1); display: flex; align-items: center;
  justify-content: center; margin: 0 auto 16px;
}
.confirm-title { font-size: 17px; font-weight: 600; color: var(--gray-900); margin: 0 0 6px; }
.confirm-desc { font-size: 14px; color: var(--gray-400); margin: 0 0 24px; }
.confirm-actions { display: flex; gap: 10px; justify-content: center; }
.btn {
  display: inline-flex; align-items: center; justify-content: center;
  padding: 9px 22px; border: none; border-radius: 8px;
  font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s;
}
.btn-gray { background: var(--gray-100); color: var(--gray-600); border: 1px solid var(--gray-200); }
.btn-gray:hover { background: var(--gray-200); }
.btn-danger { background: var(--danger); color: #fff; }
.btn-danger:hover { opacity: 0.9; }
</style>
