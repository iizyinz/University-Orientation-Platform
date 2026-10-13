<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { scanUniform, distributeUniform, getUniformStats } from '@/api/volunteer'
import QrScanner from '@/components/QrScanner.vue'

const userStore = useUserStore()
const volunteerId = userStore.userInfo.id

const stats = ref({ distributed: 0 })

const scannerRef = ref<InstanceType<typeof QrScanner>>()
const scannerActive = ref(true)

const showManual = ref(false)
const manualId = ref('')
const manualError = ref('')

const studentInfo = ref<any>(null)
const loading = ref(false)
const distributing = ref(false)
const showResult = ref(false)
const resultMsg = ref('')
const resultType = ref<'success' | 'error'>('success')
const checkinSuccess = ref(false)

async function loadStats() {
  try {
    const res: any = await getUniformStats(volunteerId)
    stats.value = res.data || { distributed: 0 }
  } catch {}
}

async function handleScan(idCard: string) {
  loading.value = true
  showResult.value = false
  studentInfo.value = null
  checkinSuccess.value = false
  scannerActive.value = false
  try {
    const res: any = await scanUniform(idCard)
    studentInfo.value = res.data
  } catch (err: any) {
    resultMsg.value = err.message || '未找到该学生'
    resultType.value = 'error'
    showResult.value = true
    setTimeout(() => { showResult.value = false; resetScan() }, 2500)
  } finally {
    loading.value = false
  }
}

async function handleDistribute() {
  if (!studentInfo.value) return
  distributing.value = true
  try {
    await distributeUniform({
      studentId: studentInfo.value.studentId,
      volunteerId,
      clothingSize: studentInfo.value.clothingSize || '',
      shoeSize: studentInfo.value.shoeSize || '',
    })
    checkinSuccess.value = true
    loadStats()
  } catch (err: any) {
    resultMsg.value = err.message || '发放失败'
    resultType.value = 'error'
    showResult.value = true
    setTimeout(() => { showResult.value = false }, 2500)
  } finally {
    distributing.value = false
  }
}

async function handleManualSubmit() {
  manualError.value = ''
  if (!manualId.value.trim()) {
    manualError.value = '请输入身份证号'
    return
  }
  await handleScan(manualId.value.trim())
}

function resetScan() {
  showResult.value = false
  studentInfo.value = null
  checkinSuccess.value = false
  showManual.value = false
  manualId.value = ''
  manualError.value = ''
  scannerActive.value = true
}

onMounted(loadStats)
</script>

<template>
  <div class="uniform-distribute">
    <!-- Stats Card -->
    <div class="stats-card">
      <div class="stats-icon">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>
          <path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
        </svg>
      </div>
      <div class="stats-body">
        <div class="stats-number">{{ stats.distributed }}</div>
        <div class="stats-label">已发放军训服</div>
      </div>
    </div>

    <!-- Section Header -->
    <div class="section-header">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M3 7V5a2 2 0 0 1 2-2h2"/><path d="M17 3h2a2 2 0 0 1 2 2v2"/>
        <path d="M21 17v2a2 2 0 0 1-2 2h-2"/><path d="M7 21H5a2 2 0 0 1-2-2v-2"/>
        <line x1="7" y1="12" x2="17" y2="12"/>
      </svg>
      扫码发放军训服
    </div>

    <!-- Scan Card -->
    <div class="scan-card">
      <div v-if="scannerActive" class="scanner-area">
        <QrScanner ref="scannerRef" :active="scannerActive" @scan="handleScan" />
        <p class="scan-tip">扫描学生报到码即可查询发放</p>
      </div>
      <div v-else class="scan-placeholder" @click="resetScan">
        <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="var(--gray-400)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 7V5a2 2 0 0 1 2-2h2"/><path d="M17 3h2a2 2 0 0 1 2 2v2"/>
          <path d="M21 17v2a2 2 0 0 1-2 2h-2"/><path d="M7 21H5a2 2 0 0 1-2-2v-2"/>
          <line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/>
        </svg>
        <span>点击重新扫码</span>
      </div>

      <div class="manual-toggle" @click="showManual = !showManual">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
        {{ showManual ? '收起' : '手动输入身份证号' }}
      </div>

      <div v-if="showManual" class="manual-form">
        <div class="manual-row">
          <input v-model="manualId" class="manual-input" placeholder="输入18位身份证号" maxlength="18" @keyup.enter="handleManualSubmit" />
          <button class="lookup-btn" @click="handleManualSubmit" :disabled="loading">
            {{ loading ? '查询中' : '查询' }}
          </button>
        </div>
        <p v-if="manualError" class="manual-error">{{ manualError }}</p>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <span>查询中...</span>
    </div>

    <!-- Toast -->
    <transition name="toast">
      <div v-if="showResult && !studentInfo" class="toast" :class="resultType">
        <svg v-if="resultType === 'success'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
        <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
        {{ resultMsg }}
      </div>
    </transition>

    <!-- Student Info Card -->
    <transition name="card">
      <div v-if="studentInfo" class="student-card">
        <div class="student-card-header">
          <div class="student-avatar">{{ (studentInfo.name || '学').charAt(0) }}</div>
          <div>
            <div class="student-name">{{ studentInfo.name }}</div>
            <div class="student-idno">{{ studentInfo.idCard }}</div>
          </div>
          <span class="status-badge success">可发放</span>
        </div>

        <div class="student-card-body">
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">联系电话</span>
              <span class="info-value">{{ studentInfo.phone || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">学院</span>
              <span class="info-value">{{ studentInfo.collegeName || '-' }}</span>
            </div>
          </div>

          <div class="size-display">
            <div class="size-box">
              <div class="size-box-label">军训服尺码</div>
              <div class="size-box-value">{{ studentInfo.clothingSize || '-' }}</div>
            </div>
            <div class="size-box">
              <div class="size-box-label">鞋子尺码</div>
              <div class="size-box-value">{{ studentInfo.shoeSize || '-' }}</div>
            </div>
          </div>
        </div>

        <div class="student-card-footer">
          <button v-if="!checkinSuccess" class="distribute-btn" @click="handleDistribute" :disabled="distributing">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
            {{ distributing ? '发放中...' : '确认发放军训服' }}
          </button>

          <div v-if="checkinSuccess" class="success-banner">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><polyline points="20 6 9 17 4 12"/></svg>
            军训服发放成功！
          </div>

          <button class="continue-btn" @click="resetScan">继续发放</button>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.uniform-distribute { padding-bottom: 8px; }

.stats-card {
  display: flex; align-items: center; gap: 16px;
  background: linear-gradient(135deg, #1A5CFF, #0033CC);
  border-radius: 14px; padding: 20px; color: #fff; margin-bottom: 16px;
}
.stats-icon {
  width: 48px; height: 48px; border-radius: 12px;
  background: rgba(255,255,255,0.15); display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.stats-body { flex: 1; }
.stats-number { font-size: 32px; font-weight: 700; line-height: 1.1; }
.stats-label { font-size: 13px; opacity: 0.8; margin-top: 2px; }

.section-header {
  display: flex; align-items: center; gap: 8px;
  font-size: 15px; font-weight: 600; color: var(--gray-900); margin-bottom: 12px;
}

.scan-card {
  background: #fff; border-radius: 14px; padding: 18px;
  border: 1px solid var(--gray-200); margin-bottom: 16px;
}
.scanner-area { display: flex; flex-direction: column; align-items: center; }
.scan-tip { font-size: 12px; color: var(--gray-400); text-align: center; margin-top: 10px; }

.scan-placeholder {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 32px; border: 2px dashed var(--gray-200); border-radius: 12px;
  cursor: pointer; color: var(--gray-400); font-size: 13px;
  transition: background 0.2s;
}
.scan-placeholder:active { background: var(--gray-100); }

.manual-toggle {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 12px 0 0; font-size: 13px; color: var(--primary); cursor: pointer;
}
.manual-toggle:active { opacity: 0.7; }

.manual-form { padding-top: 12px; }
.manual-row { display: flex; gap: 8px; }
.manual-input {
  flex: 1; padding: 9px 12px; border: 1px solid var(--gray-200); border-radius: 8px;
  font-size: 14px; outline: none;
}
.manual-input:focus { border-color: var(--primary); }
.lookup-btn {
  padding: 9px 16px; background: var(--primary); color: #fff; border: none;
  border-radius: 8px; font-size: 14px; cursor: pointer; white-space: nowrap;
}
.lookup-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.manual-error { font-size: 12px; color: var(--danger); margin-top: 6px; }

.loading-state { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 20px; color: var(--gray-400); font-size: 13px; }
.loading-spinner { width: 28px; height: 28px; border: 3px solid var(--gray-200); border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.toast {
  position: fixed; top: 20px; left: 50%; transform: translateX(-50%);
  display: flex; align-items: center; gap: 8px; padding: 10px 18px;
  border-radius: 10px; font-size: 14px; font-weight: 500; z-index: 1000;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}
.toast.success { background: var(--secondary-green); color: #fff; }
.toast.error { background: var(--danger); color: #fff; }
.toast-enter-active { transition: all 0.25s ease; }
.toast-leave-active { transition: all 0.15s ease; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateX(-50%) translateY(-10px); }

.student-card { background: #fff; border-radius: 14px; border: 1px solid var(--gray-200); overflow: hidden; margin-bottom: 16px; }
.student-card-header {
  display: flex; align-items: center; gap: 12px;
  padding: 16px 18px; border-bottom: 1px solid var(--gray-100);
}
.student-avatar {
  width: 44px; height: 44px; border-radius: 50%;
  background: linear-gradient(135deg, #1A5CFF, #0033CC);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 700; flex-shrink: 0;
}
.student-name { font-size: 15px; font-weight: 600; color: var(--gray-900); }
.student-idno { font-size: 12px; color: var(--gray-400); margin-top: 1px; }
.status-badge {
  margin-left: auto; padding: 3px 10px; border-radius: 10px;
  font-size: 12px; font-weight: 500;
}
.status-badge.success { background: rgba(0,201,167,0.1); color: var(--success); }

.student-card-body { padding: 14px 18px; }
.info-grid { display: flex; flex-direction: column; gap: 10px; }
.info-item { display: flex; justify-content: space-between; align-items: center; }
.info-label { font-size: 13px; color: var(--gray-400); }
.info-value { font-size: 13px; color: var(--gray-600); font-weight: 500; }

.size-display {
  display: flex; gap: 12px; margin-top: 14px; padding-top: 14px;
  border-top: 1px solid var(--gray-100);
}
.size-box {
  flex: 1; background: linear-gradient(135deg, #f0f9ff, #e0f2fe);
  border-radius: 12px; padding: 16px; text-align: center;
  border: 1px solid #bae6fd;
}
.size-box-label { font-size: 12px; color: var(--gray-500); margin-bottom: 6px; }
.size-box-value { font-size: 28px; font-weight: 700; color: #0369a1; }

.size-section { padding-top: 8px; }
.size-divider {
  display: flex; align-items: center; gap: 6px;
  font-size: 13px; font-weight: 600; color: var(--gray-900);
  padding: 10px 0 12px; margin-top: 6px;
  border-top: 1px solid var(--gray-100);
}
.size-group { margin-bottom: 14px; }
.size-label { display: block; font-size: 13px; font-weight: 500; color: var(--gray-600); margin-bottom: 8px; }
.size-options { display: flex; flex-wrap: wrap; gap: 8px; }
.size-tag {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 40px; padding: 6px 12px; border-radius: 8px;
  font-size: 13px; font-weight: 500; cursor: pointer;
  background: var(--gray-100); color: var(--gray-600); transition: all 0.2s;
  border: 1.5px solid transparent;
}
.size-tag:hover { border-color: var(--primary); }
.size-tag.active {
  background: var(--primary); color: #fff; border-color: var(--primary);
}

.student-card-footer { padding: 0 18px 16px; display: flex; flex-direction: column; gap: 10px; }

.distribute-btn {
  width: 100%; padding: 12px; border: none; border-radius: 10px;
  background: linear-gradient(135deg, var(--success), #00a88b);
  color: #fff; font-size: 15px; font-weight: 600;
  cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px;
  transition: opacity 0.2s;
}
.distribute-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.distribute-btn:active:not(:disabled) { opacity: 0.9; }

.success-banner {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 12px; background: rgba(0,201,167,0.08); border-radius: 10px;
  font-size: 15px; font-weight: 600; color: var(--success);
}

.continue-btn {
  width: 100%; padding: 10px; border: 1px solid var(--gray-200); border-radius: 10px;
  background: var(--gray-100); color: var(--gray-600); font-size: 14px;
  cursor: pointer; transition: background 0.2s;
}
.continue-btn:active { background: var(--gray-200); }

.card-enter-active { transition: all 0.3s ease; }
.card-leave-active { transition: all 0.2s ease; }
.card-enter-from { opacity: 0; transform: translateY(10px); }
.card-leave-to { opacity: 0; transform: translateY(-10px); }
</style>
