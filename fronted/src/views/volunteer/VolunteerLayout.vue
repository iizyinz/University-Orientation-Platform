<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const currentUser = computed(() => userStore.userInfo)

const portalType = computed(() => (route.meta.portal as string) || 'key')
const portalTitle = computed(() => portalType.value === 'key' ? '宿舍钥匙发放端' : '军训服发放端')

const showDropdown = ref(false)
const showLogoutConfirm = ref(false)
const dropdownRef = ref<HTMLDivElement>()

const handleLogout = () => {
  showDropdown.value = false
  showLogoutConfirm.value = true
}

const confirmLogout = () => {
  showLogoutConfirm.value = false
  userStore.logout()
  router.push('/login')
}

const goTo = (type: string) => {
  const path = type === 'key' ? '/volunteer/key' : '/volunteer/uniform'
  router.push(path)
}

function handleClickOutside(e: MouseEvent) {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target as Node)) {
    showDropdown.value = false
  }
}

onMounted(() => { document.addEventListener('click', handleClickOutside) })
onBeforeUnmount(() => { document.removeEventListener('click', handleClickOutside) })
</script>

<template>
  <div class="vol-layout">
    <!-- Top Bar -->
    <header class="vol-topbar">
      <div class="topbar-left">
        <div class="logo-icon">
          <svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M8 22L16 8L24 22" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="16" cy="11" r="2" fill="#FF7A1A"/>
            <path d="M11 19H21" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </div>
        <span class="brand-name">光谷启航</span>
        <span class="portal-badge">{{ portalTitle }}</span>
      </div>
      <div class="topbar-right">
        <div class="tab-switch">
          <span class="switch-item" :class="{ active: portalType === 'key' }" @click="goTo('key')">钥匙发放</span>
          <span class="switch-item" :class="{ active: portalType === 'uniform' }" @click="goTo('uniform')">军训服发放</span>
        </div>
        <div class="user-menu-wrap" ref="dropdownRef">
          <div class="user-menu" @click.stop="showDropdown = !showDropdown">
            <div class="user-avatar">{{ (currentUser.realName || '志').charAt(0) }}</div>
            <span class="user-name">{{ currentUser.realName }}</span>
            <svg class="arrow" :class="{ open: showDropdown }" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
          </div>
          <transition name="dropdown">
            <div v-if="showDropdown" class="dropdown-menu" @click.stop>
              <div class="dropdown-header">
                <div class="dd-avatar">{{ (currentUser.realName || '志').charAt(0) }}</div>
                <div class="dd-info">
                  <div class="dd-name">{{ currentUser.realName }}</div>
                  <div class="dd-role">志愿者</div>
                </div>
              </div>
              <div class="dropdown-divider"></div>
              <div class="dropdown-item danger" @click="handleLogout">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                  <polyline points="16 17 21 12 16 7"/>
                  <line x1="21" y1="12" x2="9" y2="12"/>
                </svg>
                退出登录
              </div>
            </div>
          </transition>
        </div>
      </div>
    </header>

    <!-- Content -->
    <main class="vol-content">
      <router-view />
    </main>

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

<style scoped>
.vol-layout { min-height: 100vh; background: var(--gray-100); display: flex; flex-direction: column; }

.vol-topbar {
  height: 56px; background: var(--gray-900); display: flex; align-items: center;
  justify-content: space-between; padding: 0 20px; flex-shrink: 0; position: relative;
}
.topbar-left { display: flex; align-items: center; gap: 10px; }
.logo-icon { width: 28px; height: 28px; }
.logo-icon svg { width: 100%; height: 100%; }
.brand-name { font-size: 15px; font-weight: 700; color: #fff; }
.portal-badge { font-size: 12px; color: rgba(255,255,255,0.5); padding: 2px 10px; background: rgba(255,255,255,0.08); border-radius: 10px; }

.topbar-right { display: flex; align-items: center; gap: 16px; }
.tab-switch { display: flex; background: rgba(255,255,255,0.08); border-radius: 8px; overflow: hidden; }
.switch-item { padding: 6px 14px; font-size: 13px; color: rgba(255,255,255,0.5); cursor: pointer; transition: all 0.2s; }
.switch-item.active { background: var(--primary); color: #fff; }
.switch-item:hover:not(.active) { color: rgba(255,255,255,0.8); }

.user-menu-wrap { position: relative; }
.user-menu { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 4px 10px; border-radius: 6px; transition: background 0.15s; }
.user-menu:hover { background: rgba(255,255,255,0.1); }
.user-avatar { width: 30px; height: 30px; border-radius: 50%; background: var(--primary); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 600; }
.user-name { font-size: 13px; color: rgba(255,255,255,0.85); }
.arrow { transition: transform 0.2s; color: rgba(255,255,255,0.5); }
.arrow.open { transform: rotate(180deg); }

/* Dropdown */
.dropdown-menu {
  position: absolute; top: 100%; right: 0; margin-top: 8px; width: 180px;
  background: #fff; border-radius: 10px; box-shadow: 0 6px 24px rgba(0,0,0,0.15);
  border: 1px solid var(--gray-200); padding: 6px; z-index: 200;
}
.dropdown-header { display: flex; align-items: center; gap: 10px; padding: 10px 12px; }
.dd-avatar { width: 34px; height: 34px; border-radius: 50%; background: linear-gradient(135deg, var(--primary), var(--primary-deep)); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 600; flex-shrink: 0; }
.dd-info { min-width: 0; }
.dd-name { font-size: 14px; font-weight: 600; color: var(--gray-900); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.dd-role { font-size: 12px; color: var(--gray-400); }
.dropdown-divider { height: 1px; background: var(--gray-200); margin: 4px 0; }
.dropdown-item { display: flex; align-items: center; gap: 8px; padding: 9px 12px; border-radius: 6px; font-size: 14px; color: var(--gray-600); cursor: pointer; transition: background 0.15s; }
.dropdown-item:hover { background: var(--gray-100); }
.dropdown-item.danger { color: var(--danger); }
.dropdown-item.danger:hover { background: #FFF0F0; }

.dropdown-enter-active { transition: all 0.15s ease; }
.dropdown-leave-active { transition: all 0.1s ease; }
.dropdown-enter-from, .dropdown-leave-to { opacity: 0; transform: translateY(-6px); }

/* Confirm Dialog */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.confirm-box { background: #fff; border-radius: 14px; padding: 28px; width: 340px; max-width: 90vw; text-align: center; box-shadow: 0 12px 40px rgba(0,0,0,0.12); }
.confirm-icon { width: 56px; height: 56px; border-radius: 50%; background: rgba(255,122,26,0.1); display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; }
.confirm-title { font-size: 17px; font-weight: 600; color: var(--gray-900); margin: 0 0 6px; }
.confirm-desc { font-size: 14px; color: var(--gray-400); margin: 0 0 24px; }
.confirm-actions { display: flex; gap: 10px; justify-content: center; }
.btn { display: inline-flex; align-items: center; justify-content: center; padding: 9px 22px; border: none; border-radius: 8px; font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s; }
.btn-gray { background: var(--gray-100); color: var(--gray-600); border: 1px solid var(--gray-200); }
.btn-gray:hover { background: var(--gray-200); }
.btn-danger { background: var(--danger); color: #fff; }
.btn-danger:hover { opacity: 0.9; }

.vol-content { flex: 1; display: flex; align-items: flex-start; justify-content: center; padding: 24px 16px; }
</style>
