<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRoute, useRouter } from 'vue-router'
import { getDormConfig, getDormAlloc, getDormRooms, chooseDorm, getMyDorm } from '@/api/dorm'
import AppAlert from '@/components/AppAlert.vue'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const isWebLayout = computed(() => route.path.startsWith('/student-web'))
const goBack = () => {
  if (isWebLayout.value) {
    router.push({ path: '/student-web', query: { tab: 'dorm' } })
  } else {
    router.push({ path: '/student', query: { tab: 'work' } })
  }
}

const showAlert = ref(false)
const alertMessage = ref('')
const alertType = ref<'info' | 'error' | 'success'>('info')

function showModal(msg: string, type: 'info' | 'error' | 'success' = 'info') {
  alertMessage.value = msg
  alertType.value = type
  showAlert.value = true
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
  try {
    const res: any = await getDormConfig()
    configStatus.value = res.data.status || 'not_started'
  } catch {
    configStatus.value = 'not_started'
  } finally {
    configLoading.value = false
  }
}

async function loadMyDorm() {
  try {
    const userId = userStore.userInfo.id
    if (!userId) return
    const res: any = await getMyDorm(userId)
    myDorm.value = res.data
    if (myDorm.value) {
      configStatus.value = 'ended'
    }
  } catch {}
}

async function loadBuildings() {
  try {
    const userId = userStore.userInfo.id
    if (!userId) return
    const res: any = await getDormAlloc(userId)
    buildingList.value = res.data.buildings || []
  } catch {}
}

async function selectBuilding(b: any) {
  selectedBuildingId.value = b.id
  selectedBuildingName.value = b.name
  selectedRoomId.value = null
  selectedBedNo.value = null
  roomList.value = []
  try {
    const userId = userStore.userInfo.id
    const res: any = await getDormRooms(b.id, userId)
    roomList.value = res.data || []
  } catch {}
}

function selectRoom(room: any) {
  if (room.available <= 0) return
  selectedRoomId.value = room.id
  selectedBedNo.value = null
}

function selectBed(bedNo: number, used: boolean) {
  if (used) return
  selectedBedNo.value = bedNo
}

async function handleChoose() {
  if (!selectedRoomId.value || !selectedBedNo.value) return
  choosing.value = true
  try {
    const userId = userStore.userInfo.id
    await chooseDorm({
      userId,
      roomId: selectedRoomId.value,
      bedNo: selectedBedNo.value,
    })
    chooseSuccess.value = true
    await loadMyDorm()
    setTimeout(() => { chooseSuccess.value = false }, 2000)
  } catch (err: any) {
    showModal(err.message || '选择失败', 'error')
  } finally {
    choosing.value = false
  }
}

onMounted(async () => {
  await loadConfig()
  await loadMyDorm()
  if (configStatus.value === 'in_progress' && !myDorm.value) {
    await loadBuildings()
  }
})
</script>

<template>
  <div class="student-dorm">
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
      <h3>宿舍选择</h3>
    </div>

    <div v-if="configLoading" class="loading-state">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
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
          <span>{{ myDorm ? '选房已结束' : '选房已结束' }}</span>
        </template>
      </div>

      <!-- My Dorm Result -->
      <div class="my-dorm-card" v-if="myDorm">
        <div class="dorm-badge">已分配</div>
        <div class="dorm-detail">
          <div class="dorm-row"><span class="label">楼栋</span><span class="value">{{ myDorm.buildingName }}</span></div>
          <div class="dorm-row"><span class="label">楼层</span><span class="value">{{ myDorm.floorNo }}层</span></div>
          <div class="dorm-row"><span class="label">房间</span><span class="value">{{ myDorm.roomNo }}室</span></div>
          <div class="dorm-row"><span class="label">床位</span><span class="value">{{ myDorm.bedNo }}号床</span></div>
          <div class="dorm-row"><span class="label">房间类型</span><span class="value">{{ myDorm.roomType || '-' }}</span></div>
        </div>
      </div>

      <!-- No dorm yet + ended -->
      <div v-if="!myDorm && configStatus === 'ended'" class="empty-dorm">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        <p>您未在选房期间选择宿舍，请联系辅导员</p>
      </div>

      <!-- Building List -->
      <div class="section-card" v-if="configStatus === 'in_progress' && !myDorm">
        <div class="section-title">选择楼栋</div>
        <div v-if="buildingList.length === 0" class="empty-hint">暂无可选楼栋</div>
        <div class="building-list" v-else>
          <div
            v-for="b in buildingList" :key="b.id"
            :class="['building-item', selectedBuildingId === b.id ? 'active' : '']"
            @click="selectBuilding(b)"
          >
            <div class="building-info">
              <span class="building-name">{{ b.name }}</span>
              <span class="building-meta">{{ b.floorCount }}层</span>
            </div>
            <div class="building-avail">
              <span class="avail-count">{{ b.available }}</span>
              <span class="avail-label">空余</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Room List -->
      <div class="section-card" v-if="selectedBuildingId && configStatus === 'in_progress' && !myDorm">
        <div class="section-title">{{ selectedBuildingName }} - 房间列表</div>
        <div v-if="roomList.length === 0" class="empty-hint">暂无可选房间</div>
        <div class="room-list" v-else>
          <div
            v-for="r in roomList" :key="r.id"
            :class="['room-item', selectedRoomId === r.id ? 'active' : '', r.available <= 0 ? 'full' : '']"
            @click="selectRoom(r)"
          >
            <div class="room-info">
              <span class="room-no">{{ r.roomNo }}室</span>
              <span class="room-type">{{ r.roomType || r.floorNo + '层' }}</span>
            </div>
            <div class="room-beds">
              <span
                v-for="i in r.bedCount" :key="i"
                :class="[
                  'bed-dot',
                  r.usedBeds && r.usedBeds.includes(i) ? 'taken' : '',
                  selectedRoomId === r.id && selectedBedNo === i ? 'selected' : ''
                ]"
                @click.stop="selectRoom(r); selectBed(i, r.usedBeds && r.usedBeds.includes(i))"
              ></span>
            </div>
            <span :class="['room-avail', r.available <= 0 ? 'full' : '']">
              {{ r.available > 0 ? r.available + '床空余' : '已满' }}
            </span>
          </div>
        </div>
      </div>

      <!-- Bed Selection -->
      <div class="section-card" v-if="selectedRoom && configStatus === 'in_progress' && !myDorm">
        <div class="section-title">选择床位 - {{ selectedRoom.roomNo }}室</div>
        <div class="bed-grid">
          <div
            v-for="i in selectedRoom.bedCount" :key="i"
            :class="[
              'bed-item',
              selectedRoom.usedBeds && selectedRoom.usedBeds.includes(i) ? 'taken' : '',
              selectedBedNo === i ? 'selected' : ''
            ]"
            @click="selectBed(i, selectedRoom.usedBeds && selectedRoom.usedBeds.includes(i))"
          >
            <span class="bed-no">{{ i }}号床</span>
            <span v-if="selectedRoom.usedBeds && selectedRoom.usedBeds.includes(i)" class="bed-status">已选</span>
            <span v-else-if="selectedBedNo === i" class="bed-status selected">已选中</span>
            <span v-else class="bed-status free">可选</span>
          </div>
        </div>
      </div>

      <!-- Confirm Button -->
      <button
        v-if="configStatus === 'in_progress' && selectedRoomId && selectedBedNo && !myDorm"
        class="confirm-btn"
        :disabled="choosing"
        @click="handleChoose"
      >
        {{ choosing ? '选择中...' : '确认选择' }}
      </button>
    </template>

    <!-- Success Toast -->
    <teleport to="body">
      <div v-if="chooseSuccess" class="success-toast">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5">
          <polyline points="20 6 9 17 4 12"/>
        </svg>
        宿舍选择成功
      </div>
    </teleport>

    <AppAlert :show="showAlert" :message="alertMessage" :type="alertType" @update:show="showAlert = $event" />
  </div>
</template>

<style scoped>
.student-dorm { padding-bottom: 8px; }
.page-header { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
.back-btn {
  width: 36px; height: 36px; border-radius: 10px; border: 1px solid var(--gray-200);
  background: #fff; display: flex; align-items: center; justify-content: center;
  cursor: pointer; flex-shrink: 0; color: var(--gray-600); transition: background 0.15s;
}
.back-btn:active { background: var(--gray-100); }
.page-header h3 { font-size: 18px; font-weight: 700; color: var(--gray-900); margin: 0; }

.loading-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  min-height: 200px; color: var(--gray-400); gap: 12px;
}
.loading-spinner {
  width: 28px; height: 28px; border: 3px solid var(--gray-200);
  border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.status-banner {
  display: flex; align-items: center; gap: 10px; padding: 14px 16px;
  border-radius: 12px; font-size: 14px; font-weight: 500; margin-bottom: 16px;
}
.status-banner.not_started { background: rgba(255,170,76,0.1); color: var(--secondary-orange); }
.status-banner.in_progress { background: rgba(26,92,255,0.1); color: var(--primary); }
.status-banner.ended { background: rgba(140,149,166,0.1); color: var(--gray-400); }

.my-dorm-card {
  background: #fff; border-radius: 14px; padding: 20px;
  border: 1px solid var(--gray-200); margin-bottom: 16px;
}
.dorm-badge {
  display: inline-block; padding: 3px 10px; border-radius: 10px;
  font-size: 12px; font-weight: 500; background: rgba(0,201,167,0.1);
  color: var(--success); margin-bottom: 14px;
}
.dorm-detail { display: flex; flex-direction: column; gap: 10px; }
.dorm-row { display: flex; justify-content: space-between; }
.dorm-row .label { font-size: 13px; color: var(--gray-400); }
.dorm-row .value { font-size: 14px; font-weight: 500; color: var(--gray-600); }

.empty-dorm {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  min-height: 150px; text-align: center; color: var(--gray-400);
}
.empty-dorm p { margin: 12px 0 0; font-size: 13px; }

.section-card {
  background: #fff; border-radius: 14px; padding: 18px;
  border: 1px solid var(--gray-200); margin-bottom: 16px;
}
.section-title { font-size: 15px; font-weight: 600; color: var(--gray-900); margin-bottom: 14px; }
.empty-hint { text-align: center; padding: 20px; font-size: 13px; color: var(--gray-400); }

.building-list, .room-list { display: flex; flex-direction: column; gap: 10px; }

.building-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 16px; border-radius: 10px; border: 1px solid var(--gray-200);
  cursor: pointer; transition: all 0.2s;
}
.building-item.active { border-color: var(--primary); background: rgba(26,92,255,0.04); }
.building-info { display: flex; flex-direction: column; }
.building-name { font-size: 14px; font-weight: 600; color: var(--gray-900); }
.building-meta { font-size: 12px; color: var(--gray-400); margin-top: 2px; }
.building-avail { text-align: right; }
.avail-count { font-size: 20px; font-weight: 700; color: var(--primary); }
.avail-label { display: block; font-size: 11px; color: var(--gray-400); }

.room-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 14px; border-radius: 10px; border: 1px solid var(--gray-200);
  cursor: pointer; transition: all 0.2s;
}
.room-item.active { border-color: var(--primary); background: rgba(26,92,255,0.04); }
.room-item.full { opacity: 0.5; cursor: not-allowed; }
.room-info { flex: 1; }
.room-no { font-size: 14px; font-weight: 600; color: var(--gray-900); }
.room-type { display: block; font-size: 12px; color: var(--gray-400); }
.room-beds { display: flex; gap: 6px; flex-wrap: wrap; }
.bed-dot {
  width: 24px; height: 24px; border-radius: 6px; font-size: 10px;
  display: flex; align-items: center; justify-content: center;
  background: var(--success); color: #fff; cursor: pointer;
  transition: all 0.15s; font-weight: 600;
}
.bed-dot.taken { background: var(--gray-200); color: var(--gray-400); cursor: not-allowed; }
.bed-dot.selected { background: var(--primary); color: #fff; transform: scale(1.1); }
.room-avail { font-size: 12px; color: var(--gray-400); white-space: nowrap; }
.room-avail.full { color: var(--danger); }

.bed-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); gap: 10px; }
.bed-item {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  padding: 14px 8px; border-radius: 10px; border: 2px solid var(--gray-200);
  cursor: pointer; transition: all 0.2s;
}
.bed-item.taken { opacity: 0.4; cursor: not-allowed; border-color: var(--gray-200); }
.bed-item.selected { border-color: var(--primary); background: rgba(26,92,255,0.04); }
.bed-no { font-size: 14px; font-weight: 600; color: var(--gray-900); }
.bed-status { font-size: 11px; }
.bed-status.free { color: var(--success); }
.bed-status.selected { color: var(--primary); font-weight: 600; }
.bed-status { color: var(--gray-400); }

.confirm-btn {
  width: 100%; padding: 14px; border: none; border-radius: 12px;
  background: var(--primary); color: #fff; font-size: 15px; font-weight: 600;
  cursor: pointer; transition: opacity 0.2s;
}
.confirm-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.confirm-btn:active:not(:disabled) { opacity: 0.85; }

.success-toast {
  position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%);
  background: var(--success); color: #fff; padding: 14px 28px;
  border-radius: 12px; display: flex; align-items: center; gap: 8px;
  font-size: 15px; font-weight: 600; box-shadow: 0 8px 32px rgba(0,201,167,0.3);
  z-index: 1001; animation: fadeInUp 0.3s ease;
}
@keyframes fadeInUp { from { opacity: 0; transform: translate(-50%, -40%); } to { opacity: 1; transform: translate(-50%, -50%); } }
</style>
