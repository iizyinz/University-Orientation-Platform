<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getDormConfig, getDormAlloc, getDormRooms, chooseDorm, getMyDorm } from '@/api/dorm'
import AppAlert from '@/components/AppAlert.vue'

const userStore = useUserStore()

const showAlert = ref(false)
const alertMessage = ref('')
const alertType = ref<'info' | 'error' | 'success'>('info')
function showModal(msg: string, type: 'info' | 'error' | 'success' = 'info') {
  alertMessage.value = msg; alertType.value = type; showAlert.value = true
}

const configStatus = ref('not_started')
const configLoading = ref(true)
const buildingList = ref<any[]>([])
const roomList = ref<any[]>([])
const selectedBuildingId = ref<number | null>(null)
const selectedBuildingName = ref('')
const selectedRoomId = ref<number | null>(null)
const selectedBedNo = ref<number | null>(null)
const myDorm = ref<any>(null)
const choosing = ref(false)
const chooseSuccess = ref(false)

const selectedRoom = computed(() => {
  if (!selectedRoomId.value) return null
  return roomList.value.find((r: any) => r.id === selectedRoomId.value)
})

async function loadConfig() {
  configLoading.value = true
  try { const res: any = await getDormConfig(); configStatus.value = res.data.status || 'not_started' }
  catch { configStatus.value = 'not_started' } finally { configLoading.value = false }
}

async function loadMyDorm() {
  try { const userId = userStore.userInfo.id; if (!userId) return
    const res: any = await getMyDorm(userId); myDorm.value = res.data
  } catch {}
}

async function loadBuildings() {
  try { const userId = userStore.userInfo.id; if (!userId) return
    const res: any = await getDormAlloc(userId); buildingList.value = res.data.buildings || []
  } catch {}
}

async function selectBuilding(b: any) {
  selectedBuildingId.value = b.id; selectedBuildingName.value = b.name
  selectedRoomId.value = null; selectedBedNo.value = null; roomList.value = []
  try { const userId = userStore.userInfo.id; const res: any = await getDormRooms(b.id, userId); roomList.value = res.data || [] } catch {}
}

function selectRoom(room: any) { if (room.available <= 0) return; selectedRoomId.value = room.id; selectedBedNo.value = null }
function selectBed(bedNo: number, used: boolean) { if (used) return; selectedBedNo.value = bedNo }

async function handleChoose() {
  if (!selectedRoomId.value || !selectedBedNo.value) return
  choosing.value = true
  try { const userId = userStore.userInfo.id; await chooseDorm({ userId, roomId: selectedRoomId.value, bedNo: selectedBedNo.value })
    chooseSuccess.value = true; await loadMyDorm(); setTimeout(() => { chooseSuccess.value = false }, 2000)
  } catch (err: any) { showModal(err.message || '选择失败', 'error') } finally { choosing.value = false }
}

onMounted(async () => {
  await loadConfig()
  await loadMyDorm()
  if (configStatus.value === 'in_progress' && !myDorm.value) await loadBuildings()
})
</script>

<template>
  <div class="dorm-web">
    <div class="page-header">
      <h2>宿舍选择</h2>
    </div>

    <div v-if="configLoading" class="loading-state">
      <div class="loading-spinner"></div><span>加载中...</span>
    </div>

    <template v-else>
      <!-- Status Banner -->
      <div :class="['status-banner', myDorm ? 'ended' : configStatus]">
        <template v-if="myDorm">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
          <span>已选择宿舍</span>
        </template>
        <template v-else-if="configStatus === 'not_started'">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
          <span>选房尚未开放，请耐心等待</span>
        </template>
        <template v-else-if="configStatus === 'in_progress'">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
          <span>选房进行中，请尽快选择宿舍</span>
        </template>
        <template v-else>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
          <span>选房已结束</span>
        </template>
      </div>

      <!-- My Dorm Result -->
      <div class="card" v-if="myDorm">
        <div class="card-header"><h3>我的宿舍</h3><span class="badge-green">已分配</span></div>
        <div class="info-grid">
          <div class="info-item"><span class="label">楼栋</span><span class="value">{{ myDorm.buildingName }}</span></div>
          <div class="info-item"><span class="label">楼层</span><span class="value">{{ myDorm.floorNo }}层</span></div>
          <div class="info-item"><span class="label">房间</span><span class="value">{{ myDorm.roomNo }}室</span></div>
          <div class="info-item"><span class="label">床位</span><span class="value">{{ myDorm.bedNo }}号床</span></div>
          <div class="info-item"><span class="label">房间类型</span><span class="value">{{ myDorm.roomType || '-' }}</span></div>
        </div>
      </div>

      <div v-if="!myDorm && configStatus === 'ended'" class="empty-card">
        <p>您未在选房期间选择宿舍，请联系辅导员</p>
      </div>

      <!-- Selection Interface -->
      <template v-if="configStatus === 'in_progress' && !myDorm">
        <!-- Building List -->
        <div class="card">
          <div class="card-header"><h3>选择楼栋</h3></div>
          <div v-if="buildingList.length === 0" class="empty-hint">暂无可选楼栋</div>
          <div class="grid-2" v-else>
            <div v-for="b in buildingList" :key="b.id" :class="['select-card', selectedBuildingId === b.id ? 'active' : '']" @click="selectBuilding(b)">
              <div class="card-top">
                <span class="card-name">{{ b.name }}</span>
                <span class="card-badge">{{ b.floorCount }}层</span>
              </div>
              <div class="card-avail">
                <span class="avail-num">{{ b.available }}</span>
                <span class="avail-label">空余床位</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Room List -->
        <div class="card" v-if="selectedBuildingId">
          <div class="card-header"><h3>{{ selectedBuildingName }} - 房间列表</h3></div>
          <div v-if="roomList.length === 0" class="empty-hint">暂无可选房间</div>
          <div class="room-grid" v-else>
            <div v-for="r in roomList" :key="r.id" :class="['room-card', selectedRoomId === r.id ? 'active' : '', r.available <= 0 ? 'full' : '']" @click="selectRoom(r)">
              <div class="room-top">
                <span class="room-no">{{ r.roomNo }}室</span>
                <span class="room-type">{{ r.roomType || r.floorNo + '层' }}</span>
              </div>
              <div class="room-beds">
                <span v-for="i in r.bedCount" :key="i" :class="['bed-dot', r.usedBeds && r.usedBeds.includes(i) ? 'taken' : '', selectedRoomId === r.id && selectedBedNo === i ? 'selected' : '']"
                  @click.stop="selectRoom(r); selectBed(i, r.usedBeds && r.usedBeds.includes(i))"></span>
              </div>
              <span :class="['room-avail', r.available <= 0 ? 'full' : '']">{{ r.available > 0 ? r.available + '床空余' : '已满' }}</span>
            </div>
          </div>
        </div>

        <!-- Bed Selection -->
        <div class="card" v-if="selectedRoom">
          <div class="card-header"><h3>选择床位 - {{ selectedRoom.roomNo }}室</h3></div>
          <div class="bed-grid">
            <div v-for="i in selectedRoom.bedCount" :key="i"
              :class="['bed-item', selectedRoom.usedBeds && selectedRoom.usedBeds.includes(i) ? 'taken' : '', selectedBedNo === i ? 'selected' : '']"
              @click="selectBed(i, selectedRoom.usedBeds && selectedRoom.usedBeds.includes(i))">
              <span class="bed-no">{{ i }}号床</span>
              <span v-if="selectedRoom.usedBeds && selectedRoom.usedBeds.includes(i)" class="bed-status">已选</span>
              <span v-else-if="selectedBedNo === i" class="bed-status selected">已选中</span>
              <span v-else class="bed-status free">可选</span>
            </div>
          </div>
        </div>

        <!-- Confirm Button -->
        <button class="confirm-btn" :disabled="choosing || !selectedRoomId || !selectedBedNo" @click="handleChoose">
          {{ choosing ? '选择中...' : '确认选择' }}
        </button>
      </template>
    </template>

    <AppAlert :show="showAlert" :message="alertMessage" :type="alertType" @update:show="showAlert = $event" />
  </div>
</template>

<style scoped>
.dorm-web { padding-bottom: 24px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: var(--gray-900); margin: 0; }

.loading-state { display: flex; align-items: center; justify-content: center; min-height: 300px; color: var(--gray-400); gap: 12px; }
.loading-spinner { width: 28px; height: 28px; border: 3px solid var(--gray-200); border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.status-banner {
  display: flex; align-items: center; gap: 10px; padding: 14px 20px;
  border-radius: 12px; font-size: 14px; font-weight: 500; margin-bottom: 20px;
}
.status-banner.not_started { background: rgba(255,170,76,0.1); color: var(--secondary-orange); }
.status-banner.in_progress { background: rgba(26,92,255,0.1); color: var(--primary); }
.status-banner.ended { background: rgba(0,201,167,0.1); color: var(--success); }

.card { background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; margin-bottom: 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; border-bottom: 1px solid var(--gray-100); }
.card-header h3 { font-size: 15px; font-weight: 600; color: var(--gray-900); margin: 0; }
.badge-green { padding: 3px 10px; border-radius: 10px; font-size: 12px; font-weight: 500; background: rgba(0,201,167,0.1); color: var(--success); }
.empty-hint { padding: 24px; text-align: center; font-size: 13px; color: var(--gray-400); }
.empty-card { background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; padding: 32px; text-align: center; margin-bottom: 16px; }
.empty-card p { font-size: 14px; color: var(--gray-400); margin: 0; }

.info-grid { padding: 16px 20px; display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-item .label { font-size: 12px; color: var(--gray-400); }
.info-item .value { font-size: 15px; font-weight: 600; color: var(--gray-900); }

.grid-2 { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 12px; padding: 16px 20px; }
.select-card {
  border: 1px solid var(--gray-200); border-radius: 10px; padding: 16px;
  cursor: pointer; transition: all 0.2s;
}
.select-card.active { border-color: var(--primary); background: rgba(26,92,255,0.04); }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.card-name { font-size: 15px; font-weight: 600; color: var(--gray-900); }
.card-badge { font-size: 12px; padding: 2px 8px; border-radius: 6px; background: var(--gray-100); color: var(--gray-400); }
.card-avail { text-align: center; }
.avail-num { font-size: 24px; font-weight: 700; color: var(--primary); display: block; }
.avail-label { font-size: 12px; color: var(--gray-400); }

.room-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 12px; padding: 16px 20px; }
.room-card {
  border: 1px solid var(--gray-200); border-radius: 10px; padding: 14px;
  cursor: pointer; transition: all 0.2s;
}
.room-card.active { border-color: var(--primary); background: rgba(26,92,255,0.04); }
.room-card.full { opacity: 0.5; cursor: not-allowed; }
.room-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.room-no { font-size: 14px; font-weight: 600; color: var(--gray-900); }
.room-type { font-size: 12px; color: var(--gray-400); }
.room-beds { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 8px; }
.bed-dot {
  width: 24px; height: 24px; border-radius: 6px; font-size: 10px;
  display: flex; align-items: center; justify-content: center;
  background: var(--success); color: #fff; cursor: pointer; font-weight: 600;
}
.bed-dot.taken { background: var(--gray-200); color: var(--gray-400); cursor: not-allowed; }
.bed-dot.selected { background: var(--primary); color: #fff; transform: scale(1.1); }
.room-avail { font-size: 12px; color: var(--gray-400); }
.room-avail.full { color: var(--danger); }

.bed-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); gap: 10px; padding: 16px 20px; }
.bed-item {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  padding: 14px 8px; border-radius: 10px; border: 2px solid var(--gray-200);
  cursor: pointer; transition: all 0.2s;
}
.bed-item.taken { opacity: 0.4; cursor: not-allowed; }
.bed-item.selected { border-color: var(--primary); background: rgba(26,92,255,0.04); }
.bed-no { font-size: 14px; font-weight: 600; color: var(--gray-900); }
.bed-status { font-size: 11px; }
.bed-status.free { color: var(--success); }
.bed-status.selected { color: var(--primary); font-weight: 600; }

.confirm-btn {
  width: 100%; max-width: 400px; padding: 14px; border: none; border-radius: 12px;
  background: var(--primary); color: #fff; font-size: 15px; font-weight: 600;
  cursor: pointer; transition: opacity 0.2s;
}
.confirm-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.confirm-btn:active:not(:disabled) { opacity: 0.85; }
</style>
