<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyGroups } from '@/api/discover'

const userStore = useUserStore()
const groups = ref<any[]>([])
const loading = ref(true)
const showQrPreview = ref(false)
const currentQr = ref<any>(null)

const groupTypeMap: Record<number, string> = { 1: '生源地群', 2: '学院群', 3: '班级群' }

async function loadGroups() {
  loading.value = true
  try {
    const userId = userStore.userInfo.id
    if (!userId) return
    const res: any = await getMyGroups(userId)
    groups.value = res.data || []
  } catch {} finally { loading.value = false }
}

function openQr(g: any) { currentQr.value = g; showQrPreview.value = true }

onMounted(loadGroups)
</script>

<template>
  <div class="group-web">
    <div class="page-header"><h2>新生群</h2><p class="subtitle">加入班级群和学院群，与同学和辅导员交流</p></div>

    <div v-if="loading" class="loading-state"><div class="loading-spinner"></div></div>

    <div v-else-if="groups.length === 0" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
      <p>暂无群聊信息</p>
    </div>

    <div v-else class="group-grid">
      <div v-for="g in groups" :key="g.id" class="group-card">
        <div class="card-header">
          <div class="group-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
          </div>
          <div class="group-info">
            <span class="group-type">{{ groupTypeMap[g.groupType] || '群聊' }}</span>
            <span class="group-name">{{ g.groupName }}</span>
          </div>
        </div>
        <div class="card-body">
          <div class="detail-row"><span class="label">群号</span><span class="value">{{ g.groupNo || '暂未设置' }}</span></div>
          <div v-if="g.counselorName" class="detail-row"><span class="label">辅导员</span><span class="value">{{ g.counselorName }}</span></div>
          <div v-if="g.counselorPhone" class="detail-row"><span class="label">联系电话</span><span class="value">{{ g.counselorPhone }}</span></div>
          <div v-if="g.counselorEmail" class="detail-row"><span class="label">邮箱</span><span class="value">{{ g.counselorEmail }}</span></div>
        </div>
        <div v-if="g.groupQrImage" class="card-footer">
          <button class="btn btn-outline btn-sm" @click="openQr(g)">查看群二维码</button>
        </div>
      </div>
    </div>

    <teleport to="body">
      <div v-if="showQrPreview && currentQr" class="modal-mask" @click.self="showQrPreview = false">
        <div class="preview-modal">
          <div class="modal-header"><h4>{{ currentQr.groupName }} - 群二维码</h4><button class="close-btn" @click="showQrPreview = false"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>
          <div class="preview-body"><img :src="currentQr.groupQrImage" :alt="currentQr.groupName" class="preview-image" /></div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.group-web { padding-bottom: 24px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: var(--gray-900); margin: 0 0 4px; }
.page-header .subtitle { font-size: 14px; color: var(--gray-400); margin: 0; }

.loading-state { display: flex; justify-content: center; padding: 60px; }
.loading-spinner { width: 28px; height: 28px; border: 3px solid var(--gray-200); border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 300px; color: var(--gray-400); }
.empty-state p { margin: 12px 0 0; font-size: 14px; }

.group-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 16px; }
.group-card { background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; overflow: hidden; }
.card-header { display: flex; align-items: center; gap: 14px; padding: 16px 20px; border-bottom: 1px solid var(--gray-100); }
.group-icon { width: 44px; height: 44px; border-radius: 12px; background: rgba(26,92,255,0.1); color: var(--primary); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.group-info { display: flex; flex-direction: column; }
.group-type { font-size: 12px; color: var(--gray-400); }
.group-name { font-size: 15px; font-weight: 600; color: var(--gray-900); }

.card-body { padding: 14px 20px; }
.detail-row { display: flex; justify-content: space-between; padding: 6px 0; }
.detail-row .label { font-size: 13px; color: var(--gray-400); }
.detail-row .value { font-size: 14px; font-weight: 500; color: var(--gray-600); }

.card-footer { padding: 12px 20px; border-top: 1px solid var(--gray-100); }
.btn { padding: 8px 16px; border-radius: 8px; font-size: 13px; font-weight: 500; cursor: pointer; transition: all 0.2s; border: none; }
.btn-sm { padding: 6px 14px; font-size: 12px; }
.btn-outline { background: #fff; color: var(--primary); border: 1px solid rgba(26,92,255,0.3); }
.btn-outline:hover { background: rgba(26,92,255,0.05); }

.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.preview-modal { background: #fff; border-radius: 14px; width: 90vw; max-width: 500px; max-height: 85vh; display: flex; flex-direction: column; box-shadow: 0 12px 40px rgba(0,0,0,0.2); }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 18px 20px; border-bottom: 1px solid var(--gray-200); }
.modal-header h4 { font-size: 16px; font-weight: 600; color: var(--gray-900); margin: 0; }
.close-btn { background: none; border: none; color: var(--gray-400); cursor: pointer; padding: 4px; border-radius: 6px; }
.close-btn:hover { background: var(--gray-100); }
.preview-body { padding: 16px; text-align: center; }
.preview-image { max-width: 100%; max-height: 70vh; border-radius: 8px; object-fit: contain; }
</style>
