<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const collapsed = ref(false)
const showDropdown = ref(false)
const showLogoutConfirm = ref(false)
const dropdownRef = ref<HTMLDivElement>()

const currentUser = computed(() => userStore.userInfo)

const toggleSidebar = () => { collapsed.value = !collapsed.value }

const handleLogout = () => {
  showDropdown.value = false
  showLogoutConfirm.value = true
}

const confirmLogout = () => {
  showLogoutConfirm.value = false
  userStore.logout()
  router.push('/login')
}

const goToProfile = () => {
  showDropdown.value = false
  router.push({ path: '/student-web', query: { tab: 'profile' } })
}

function handleClickOutside(e: MouseEvent) {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target as Node)) {
    showDropdown.value = false
  }
}

onMounted(() => { document.addEventListener('click', handleClickOutside) })
onBeforeUnmount(() => { document.removeEventListener('click', handleClickOutside) })

interface MenuItem {
  key: string
  label: string
  icon: string
  children?: { key: string; label: string }[]
}

const menuItems: MenuItem[] = [
  { key: 'home', label: '首页', icon: 'home' },
  { key: 'info', label: '信息填报', icon: 'info' },
  { key: 'fee', label: '学费查看', icon: 'fee' },
  { key: 'dorm', label: '宿舍选择', icon: 'dorm' },
  {
    key: 'discover',
    label: '发现',
    icon: 'discover',
    children: [
      { key: 'discover-handbook', label: '新生入学手册' },
      { key: 'discover-map', label: '校园地图' },
      { key: 'discover-group', label: '新生群' },
      { key: 'discover-faq', label: '智能问答' },
      { key: 'discover-common-faq', label: '常见问题' },
    ],
  },
  { key: 'profile', label: '个人中心', icon: 'profile' },
]

const expandedKeys = ref<string[]>([])

const toggleExpand = (key: string) => {
  const idx = expandedKeys.value.indexOf(key)
  if (idx > -1) expandedKeys.value.splice(idx, 1)
  else expandedKeys.value.push(key)
}

const activeKey = computed(() => (route.query.tab as string) || 'home')

const handleMenuClick = (key: string, parentKey?: string) => {
  router.push({ path: '/student-web', query: { tab: key } })
}

const pageTitleMap: Record<string, string> = {
  home: '首页', info: '信息填报', fee: '学费查看', dorm: '宿舍选择',
  profile: '个人中心', discover: '发现',
  'discover-handbook': '新生入学手册', 'discover-map': '校园地图',
  'discover-group': '新生群', 'discover-faq': '智能问答', 'discover-common-faq': '常见问题',
}

function getCurrentTitle() {
  return pageTitleMap[activeKey.value] || '学生端'
}
</script>

<template>
  <div class="web-layout">
    <!-- Sidebar -->
    <aside class="sidebar" :class="{ collapsed }">
      <div class="sidebar-logo">
        <div class="logo-icon">
          <svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M8 22L16 8L24 22" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="16" cy="11" r="2" fill="#FF7A1A"/>
            <path d="M11 19H21" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </div>
        <transition name="fade">
          <div v-if="!collapsed" class="logo-text">
            <span class="logo-title">光谷启航</span>
            <span class="logo-sub">学生端</span>
          </div>
        </transition>
      </div>

      <nav class="sidebar-menu">
        <div
          v-for="item in menuItems"
          :key="item.key"
          class="menu-group"
        >
          <!-- 单菜单项（无子菜单） -->
          <div
            v-if="!item.children"
            class="menu-item"
            :class="{ active: activeKey === item.key }"
            @click="handleMenuClick(item.key)"
          >
            <div class="menu-icon" v-html="getIcon(item.icon)"></div>
            <transition name="fade">
              <span v-if="!collapsed" class="menu-label">{{ item.label }}</span>
            </transition>
          </div>

          <!-- 有子菜单的父项 -->
          <template v-else>
            <div
              class="menu-item parent-item"
              :class="{ expanded: expandedKeys.includes(item.key) }"
              @click="toggleExpand(item.key)"
            >
              <div class="menu-icon" v-html="getIcon(item.icon)"></div>
              <transition name="fade">
                <span v-if="!collapsed" class="menu-label">{{ item.label }}</span>
              </transition>
              <transition name="fade">
                <svg
                  v-if="!collapsed"
                  class="arrow-icon"
                  :class="{ rotated: expandedKeys.includes(item.key) }"
                  width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
                >
                  <polyline points="6 9 12 15 18 9"/>
                </svg>
              </transition>
            </div>
            <transition name="submenu">
              <div
                v-if="expandedKeys.includes(item.key) && !collapsed"
                class="submenu"
              >
                <div
                  v-for="child in item.children"
                  :key="child.key"
                  class="submenu-item"
                  :class="{ active: activeKey === child.key }"
                  @click="handleMenuClick(child.key, item.key)"
                >
                  {{ child.label }}
                </div>
              </div>
            </transition>
          </template>
        </div>
      </nav>

      <div class="sidebar-footer">
        <div class="collapse-btn" @click="toggleSidebar">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
            :style="{ transform: collapsed ? 'rotate(180deg)' : '' }">
            <polyline points="11 17 6 12 11 7"/>
            <polyline points="18 17 13 12 18 7"/>
          </svg>
        </div>
      </div>
    </aside>

    <!-- Main Area -->
    <div class="main-area">
      <header class="topbar">
        <div class="topbar-left">
          <span class="page-title">{{ getCurrentTitle() }}</span>
        </div>
        <div class="topbar-right" ref="dropdownRef">
          <div class="user-info" @click.stop="showDropdown = !showDropdown">
            <div class="user-avatar">{{ (currentUser.realName || '学').charAt(0) }}</div>
            <span class="user-name">{{ currentUser.realName || '学生' }}</span>
            <svg class="dropdown-arrow" :class="{ open: showDropdown }" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          </div>
          <transition name="dropdown">
            <div v-if="showDropdown" class="dropdown-menu" @click.stop>
              <div class="dropdown-header">
                <div class="dd-avatar">{{ (currentUser.realName || '学').charAt(0) }}</div>
                <div class="dd-info">
                  <div class="dd-name">{{ currentUser.realName || '学生' }}</div>
                  <div class="dd-role">学生</div>
                </div>
              </div>
              <div class="dropdown-divider"></div>
              <div class="dropdown-item" @click="goToProfile">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                个人中心
              </div>
              <div class="dropdown-divider"></div>
              <div class="dropdown-item danger" @click="handleLogout">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
                退出登录
              </div>
            </div>
          </transition>
        </div>
      </header>

      <main class="content-area">
        <router-view />
      </main>
    </div>

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
function getIcon(type: string): string {
  const icons: Record<string, string> = {
    home: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>`,
    info: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>`,
    fee: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>`,
    dorm: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><path d="M9 22V12h6v10"/></svg>`,
    discover: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76"/></svg>`,
    profile: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`,
  }
  return icons[type] || ''
}
</script>

<style scoped>
.web-layout { display: flex; width: 100vw; height: 100vh; overflow: hidden; background: var(--gray-100); }

.sidebar {
  width: 240px; height: 100vh; background: var(--gray-900); display: flex;
  flex-direction: column; transition: width 0.3s cubic-bezier(0.4,0,0.2,1);
  overflow: hidden; flex-shrink: 0; position: relative; z-index: 100;
}
.sidebar.collapsed { width: 64px; }

.sidebar-logo {
  display: flex; align-items: center; gap: 12px; padding: 20px 16px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
.logo-icon { width: 32px; height: 32px; flex-shrink: 0; }
.logo-icon svg { width: 100%; height: 100%; }
.logo-text { display: flex; flex-direction: column; white-space: nowrap; overflow: hidden; }
.logo-title { font-size: 16px; font-weight: 700; color: #fff; line-height: 1.2; }
.logo-sub { font-size: 11px; color: rgba(255,255,255,0.45); line-height: 1.2; }

.sidebar-menu { flex: 1; overflow-y: auto; padding: 8px 0; }
.menu-item {
  display: flex; align-items: center; gap: 10px; padding: 10px 16px; margin: 2px 8px;
  border-radius: 8px; color: rgba(255,255,255,0.6); cursor: pointer; transition: all 0.2s;
  white-space: nowrap; user-select: none;
}
.menu-item:hover { color: #fff; background: rgba(255,255,255,0.06); }
.menu-item.active { color: #fff; background: var(--primary); }
.menu-icon { display: flex; align-items: center; justify-content: center; width: 20px; height: 20px; flex-shrink: 0; }
.menu-label { flex: 1; font-size: 14px; overflow: hidden; text-overflow: ellipsis; }

.parent-item { margin-bottom: 0; }

.arrow-icon {
  flex-shrink: 0; transition: transform 0.3s; opacity: 0.5;
}
.arrow-icon.rotated { transform: rotate(180deg); }

/* Submenu */
.submenu { overflow: hidden; }
.submenu-item {
  padding: 8px 16px 8px 46px;
  border-radius: 6px;
  color: rgba(255,255,255,0.45);
  font-size: 13px;
  cursor: pointer; transition: all 0.2s;
  white-space: nowrap;
}
.submenu-item:hover { color: rgba(255,255,255,0.85); background: rgba(255,255,255,0.04); }
.submenu-item.active { color: #fff; background: rgba(26,92,255,0.3); }

.submenu-enter-active { transition: all 0.25s ease; overflow: hidden; }
.submenu-leave-active { transition: all 0.2s ease; overflow: hidden; }
.submenu-enter-from, .submenu-leave-to { max-height: 0; opacity: 0; }
.submenu-enter-to, .submenu-leave-from { max-height: 300px; opacity: 1; }

.sidebar-footer { border-top: 1px solid rgba(255,255,255,0.06); padding: 12px; display: flex; justify-content: center; }
.collapse-btn {
  width: 36px; height: 36px; display: flex; align-items: center; justify-content: center;
  border-radius: 8px; color: rgba(255,255,255,0.5); cursor: pointer; transition: all 0.2s;
}
.collapse-btn:hover { color: #fff; background: rgba(255,255,255,0.08); }
.collapse-btn svg { transition: transform 0.3s; }

/* Main */
.main-area { flex: 1; display: flex; flex-direction: column; overflow: hidden; min-width: 0; }
.topbar {
  height: 60px; background: #fff; border-bottom: 1px solid var(--gray-200);
  display: flex; align-items: center; justify-content: space-between; padding: 0 24px; flex-shrink: 0;
}
.topbar-left { display: flex; align-items: center; }
.page-title { font-size: 17px; font-weight: 600; color: var(--gray-900); }
.topbar-right { position: relative; }
.user-info {
  display: flex; align-items: center; gap: 10px; padding: 6px 12px;
  border-radius: 8px; cursor: pointer; transition: background 0.2s;
}
.user-info:hover { background: var(--gray-100); }
.user-avatar {
  width: 34px; height: 34px; border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--primary-deep));
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 600;
}
.user-name { font-size: 14px; font-weight: 500; color: var(--gray-600); }
.dropdown-arrow { transition: transform 0.2s; color: var(--gray-400); }
.dropdown-arrow.open { transform: rotate(180deg); }

/* Dropdown */
.dropdown-menu {
  position: absolute; top: 100%; right: 0; margin-top: 8px; width: 180px;
  background: #fff; border-radius: 10px; box-shadow: 0 6px 24px rgba(0,0,0,0.1);
  border: 1px solid var(--gray-200); padding: 6px; z-index: 200;
}
.dropdown-header { display: flex; align-items: center; gap: 10px; padding: 10px 12px; }
.dd-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--primary-deep));
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 600; flex-shrink: 0;
}
.dd-info { min-width: 0; }
.dd-name { font-size: 14px; font-weight: 600; color: var(--gray-900); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.dd-role { font-size: 12px; color: var(--gray-400); }
.dropdown-divider { height: 1px; background: var(--gray-200); margin: 4px 0; }
.dropdown-item {
  display: flex; align-items: center; gap: 8px; padding: 9px 12px;
  border-radius: 6px; font-size: 14px; color: var(--gray-600); cursor: pointer; transition: background 0.15s;
}
.dropdown-item:hover { background: var(--gray-100); }
.dropdown-item.danger { color: var(--danger); }
.dropdown-item.danger:hover { background: #FFF0F0; }
.dropdown-enter-active { transition: all 0.15s ease; }
.dropdown-leave-active { transition: all 0.1s ease; }
.dropdown-enter-from, .dropdown-leave-to { opacity: 0; transform: translateY(-6px); }

/* Confirm */
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

.content-area { flex: 1; overflow-y: auto; padding: 24px; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
