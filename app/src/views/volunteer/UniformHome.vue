<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { getUniformStats } from '@/api/volunteer'

const userStore = useUserStore()
const router = useRouter()
const userInfo = computed(() => userStore.userInfo)

const stats = ref({ distributed: 0 })

async function loadStats() {
  try {
    const res: any = await getUniformStats(userInfo.value.id)
    stats.value = res.data || { distributed: 0 }
  } catch {}
}

const goTo = (tab: string) => {
  router.push({ path: '/volunteer/uniform', query: { tab } })
}

onMounted(loadStats)
</script>

<template>
  <div class="uniform-home">
    <!-- Welcome Banner -->
    <div class="welcome-banner">
      <div class="banner-top">
        <div class="avatar">{{ (userInfo.realName || '志').charAt(0) }}</div>
        <div class="banner-text">
          <h2>{{ userInfo.realName || '志愿者' }}，您好 👋</h2>
          <p>军训服发放工作台</p>
        </div>
      </div>
    </div>

    <!-- Stats -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-value">{{ stats.distributed }}</div>
        <div class="stat-label">已发放军训服</div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="section-card">
      <div class="section-title">快捷操作</div>
      <div class="quick-grid">
        <div class="quick-item" @click="goTo('distribute')">
          <div class="quick-icon blue">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 7V5a2 2 0 0 1 2-2h2"/><path d="M17 3h2a2 2 0 0 1 2 2v2"/>
              <path d="M21 17v2a2 2 0 0 1-2 2h-2"/><path d="M7 21H5a2 2 0 0 1-2-2v-2"/>
              <line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/>
            </svg>
          </div>
          <span>扫码发放</span>
        </div>
        <div class="quick-item" @click="goTo('profile')">
          <div class="quick-icon orange">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          </div>
          <span>个人中心</span>
        </div>
      </div>
    </div>

    <!-- Tips -->
    <div class="tip-card">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/>
      </svg>
      <div class="tip-text">
        <strong>操作提示</strong>
        <p>点击底部「扫码」按钮，扫描学生报到码后选择对应尺码即可发放军训服。</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.uniform-home { padding-bottom: 8px; }

.welcome-banner {
  background: linear-gradient(135deg, #1A5CFF, #0033CC, #00C9A7);
  border-radius: 14px; padding: 20px; color: #fff; margin-bottom: 16px;
}
.banner-top { display: flex; align-items: center; gap: 14px; }
.avatar {
  width: 48px; height: 48px; border-radius: 50%;
  background: rgba(255,255,255,0.2); display: flex; align-items: center;
  justify-content: center; font-size: 20px; font-weight: 700; flex-shrink: 0;
}
.banner-text { flex: 1; }
.banner-text h2 { font-size: 18px; font-weight: 700; margin: 0 0 2px; }
.banner-text p { font-size: 13px; opacity: 0.8; margin: 0; }

.stats-row { display: flex; gap: 12px; margin-bottom: 16px; }
.stat-card {
  flex: 1; background: #fff; border-radius: 12px; padding: 16px;
  text-align: center; border: 1px solid var(--gray-200);
}
.stat-value { font-size: 28px; font-weight: 700; color: var(--primary); }
.stat-label { font-size: 12px; color: var(--gray-400); margin-top: 2px; }

.section-card {
  background: #fff; border-radius: 14px; padding: 18px;
  border: 1px solid var(--gray-200); margin-bottom: 16px;
}
.section-title {
  font-size: 15px; font-weight: 600; color: var(--gray-900); margin-bottom: 14px;
}

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

.tip-card {
  display: flex; gap: 10px; padding: 14px; border-radius: 12px;
  background: rgba(26,92,255,0.06); border: 1px solid rgba(26,92,255,0.12);
}
.tip-text { flex: 1; }
.tip-text strong { font-size: 13px; color: var(--gray-900); display: block; margin-bottom: 2px; font-weight: 600; }
.tip-text p { font-size: 12px; color: var(--gray-500); margin: 0; line-height: 1.6; }
</style>
