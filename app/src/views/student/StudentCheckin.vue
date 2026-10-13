<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getReportCode, getFlowStatus } from '@/api/student'
import QRCode from 'qrcode'

const userStore = useUserStore()

const reportStatus = ref(0)
const qrDataUrl = ref('')
const codeContent = ref('')
const loading = ref(true)

interface FlowStep {
  step: number
  name: string
  done: boolean
}
const flowSteps = ref<FlowStep[]>([])

let pollTimer: ReturnType<typeof setInterval> | null = null

async function loadReportCode() {
  try {
    const userId = userStore.userInfo.id
    if (!userId) return
    const res: any = await getReportCode(userId)
    reportStatus.value = res.data.reportStatus || 0
    codeContent.value = res.data.codeContent || ''
    if (codeContent.value) {
      qrDataUrl.value = await QRCode.toDataURL(codeContent.value, {
        width: 200, margin: 2, color: { dark: '#1A1D26', light: '#FFFFFF' },
      })
    }
  } catch {}
}

async function loadFlowStatus() {
  try {
    const userId = userStore.userInfo.id
    if (!userId) return
    const res: any = await getFlowStatus(userId)
    reportStatus.value = res.data.reportStatus || 0
    flowSteps.value = (res.data.steps || []) as FlowStep[]
  } catch {
    flowSteps.value = [
      { step: 1, name: '在线填报信息', done: false },
      { step: 2, name: '在线选择宿舍', done: false },
      { step: 3, name: '现场扫码报到', done: false },
      { step: 4, name: '领取宿舍钥匙', done: false },
      { step: 5, name: '领取军训服装', done: false },
    ]
  }
}

async function loadAll() {
  await Promise.all([loadReportCode(), loadFlowStatus()])
  loading.value = false
}

function startPolling() {
  if (pollTimer) return
  pollTimer = setInterval(loadAll, 10000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

function handleVisibilityChange() {
  if (document.hidden) {
    stopPolling()
  } else {
    loadAll()
    startPolling()
  }
}

const getCurrentStepIndex = () => {
  for (let i = 0; i < flowSteps.value.length; i++) {
    if (!flowSteps.value[i].done) return i
  }
  return flowSteps.value.length
}

const isLastDone = () => {
  return flowSteps.value.length > 0 && flowSteps.value[flowSteps.value.length - 1].done
}

onMounted(async () => {
  await loadAll()
  startPolling()
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  stopPolling()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<template>
  <div class="student-checkin">
    <!-- Report Code Card -->
    <div class="report-code-card">
      <div class="code-header">
        <span class="code-title">我的报到码</span>
        <span :class="['code-status', reportStatus === 1 ? 'done' : 'pending']">
          {{ reportStatus === 1 ? '已报到' : '待报到' }}
        </span>
      </div>
      <div class="code-area">
        <div v-if="loading" class="qr-loading">
          <div class="loading-spinner"></div>
        </div>
        <div v-else-if="qrDataUrl" class="qr-wrapper">
          <img :src="qrDataUrl" alt="报到码" class="qr-image" />
        </div>
        <div v-else class="qr-placeholder">
          <svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="var(--gray-400)" stroke-width="1.5">
            <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
            <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
          </svg>
          <p>暂无报到码</p>
        </div>
        <p class="code-tip">到校后向辅导员或志愿者出示此码完成签到</p>
      </div>
    </div>

    <!-- Flow Progress -->
    <div class="section-card">
      <div class="section-title">报到流程</div>
      <div class="flow-list">
        <div v-for="(step, idx) in flowSteps" :key="step.step" class="flow-item">
          <div
            class="flow-step"
            :class="{
              done: step.done,
              current: !step.done && idx === getCurrentStepIndex() && !isLastDone()
            }"
          >
            <svg v-if="step.done" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="3">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
            <span v-else>{{ step.step }}</span>
          </div>
          <div class="flow-info">
            <span class="flow-name">{{ step.name }}</span>
            <span v-if="step.done" class="flow-status done">已完成</span>
            <span v-else-if="!step.done && idx === getCurrentStepIndex() && !isLastDone()" class="flow-status current">进行中</span>
            <span v-else class="flow-status pending">待完成</span>
          </div>
          <div v-if="idx < flowSteps.length - 1" class="flow-line" :class="{ done: step.done }"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.student-checkin { padding-bottom: 8px; }

.report-code-card {
  background: #fff; border-radius: 14px; padding: 20px;
  border: 1px solid var(--gray-200); margin-bottom: 16px;
}
.code-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.code-title { font-size: 15px; font-weight: 600; color: var(--gray-900); }
.code-status {
  font-size: 12px; font-weight: 500; padding: 3px 10px; border-radius: 10px;
}
.code-status.done { background: rgba(0,201,167,0.1); color: var(--success); }
.code-status.pending { background: rgba(255,170,76,0.1); color: var(--secondary-orange); }

.code-area { text-align: center; }

.qr-loading {
  width: 160px; height: 160px; margin: 0 auto 12px;
  display: flex; align-items: center; justify-content: center;
}
.loading-spinner {
  width: 28px; height: 28px; border: 3px solid var(--gray-200);
  border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.qr-wrapper {
  display: inline-block; padding: 12px; background: #fff;
  border: 1px solid var(--gray-200); border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.qr-image { width: 160px; height: 160px; display: block; }

.qr-placeholder {
  width: 160px; height: 160px; margin: 0 auto 12px;
  border: 2px dashed var(--gray-200); border-radius: 12px;
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px;
}
.qr-placeholder p { font-size: 12px; color: var(--gray-400); margin: 0; }

.code-tip { font-size: 12px; color: var(--gray-400); margin: 12px 0 0; }

.section-card {
  background: #fff; border-radius: 14px; padding: 18px;
  border: 1px solid var(--gray-200); margin-bottom: 16px;
}
.section-title {
  font-size: 15px; font-weight: 600; color: var(--gray-900); margin-bottom: 14px;
}

/* Flow */
.flow-list { position: relative; }
.flow-item { display: flex; align-items: center; gap: 12px; position: relative; padding-bottom: 20px; }
.flow-item:last-child { padding-bottom: 0; }
.flow-step {
  width: 28px; height: 28px; border-radius: 50%; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 600; color: var(--gray-400);
  background: var(--gray-100); border: 2px solid var(--gray-200);
  position: relative; z-index: 1;
}
.flow-step.done { background: var(--success); border-color: var(--success); color: #fff; }
.flow-step.current { background: var(--primary); border-color: var(--primary); color: #fff; }
.flow-info { flex: 1; display: flex; justify-content: space-between; align-items: center; }
.flow-name { font-size: 14px; color: var(--gray-600); }
.flow-status { font-size: 12px; }
.flow-status.done { color: var(--success); }
.flow-status.current { color: var(--primary); }
.flow-status.pending { color: var(--gray-400); }
.flow-line {
  position: absolute; left: 13px; top: 28px; width: 2px; height: calc(100% - 28px);
  background: var(--gray-200);
}
.flow-line.done { background: var(--success); }
</style>
