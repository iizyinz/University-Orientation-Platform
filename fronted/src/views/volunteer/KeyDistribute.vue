<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { scanKey, distributeKey, getKeyStats } from '@/api/volunteer'
import QrScanner from '@/components/QrScanner.vue'

const userStore = useUserStore()
const volunteerId = userStore.userInfo.id

const stats = ref({ distributed: 0 })
const studentInfo = ref<any>(null)
const loading = ref(false)
const distributing = ref(false)
const resultMsg = ref('')
const resultType = ref<'success' | 'error'>('success')
const showResult = ref(false)
const manualId = ref('')
const showManual = ref(false)
const scannerRef = ref<InstanceType<typeof QrScanner>>()

async function loadStats() {
  const res: any = await getKeyStats(volunteerId)
  stats.value = res.data
}

async function handleScan(idCard: string) {
  loading.value = true
  resultMsg.value = ''
  showResult.value = false
  studentInfo.value = null
  try {
    const res: any = await scanKey(idCard)
    studentInfo.value = res.data
  } catch (err: any) {
    resultMsg.value = err.message || '扫描失败'
    resultType.value = 'error'
    showResult.value = true
    setTimeout(() => { showResult.value = false }, 3000)
    setTimeout(() => { scannerRef.value?.restartScan() }, 500)
  } finally {
    loading.value = false
  }
}

async function handleDistribute() {
  if (!studentInfo.value) return
  distributing.value = true
  try {
    await distributeKey({
      studentId: studentInfo.value.studentId,
      roomId: studentInfo.value.roomId,
      volunteerId,
    })
    resultMsg.value = '钥匙发放成功！'
    resultType.value = 'success'
    showResult.value = true
    studentInfo.value = null
    loadStats()
    setTimeout(() => { showResult.value = false }, 2000)
    setTimeout(() => { scannerRef.value?.restartScan() }, 500)
  } catch (err: any) {
    resultMsg.value = err.message || '发放失败'
    resultType.value = 'error'
    showResult.value = true
    setTimeout(() => { scannerRef.value?.restartScan() }, 500)
  } finally {
    distributing.value = false
  }
}

function handleManualSubmit() {
  if (manualId.value.trim()) handleScan(manualId.value.trim())
}

onMounted(loadStats)
</script>

<template>
  <div class="distribute-page">
    <!-- Stats -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-value">{{ stats.distributed }}</div>
        <div class="stat-label">已发放钥匙</div>
      </div>
    </div>

    <!-- Scanner -->
    <div class="scan-section">
      <h3 class="section-title">扫码发放钥匙</h3>
      <p class="section-desc">扫描学生报到码或手动输入身份证号</p>

      <QrScanner ref="scannerRef" :active="true" @scan="handleScan" />

      <div class="or-divider">
        <span>或</span>
      </div>

      <div class="manual-input" v-if="!showManual">
        <button class="btn btn-outline" @click="showManual = true">手动输入身份证号</button>
      </div>
      <div class="manual-form" v-else>
        <input v-model="manualId" class="form-input" placeholder="输入18位身份证号" maxlength="18" @keyup.enter="handleManualSubmit" />
        <button class="btn btn-primary" @click="handleManualSubmit" :disabled="loading">
          {{ loading ? '查询中...' : '查询' }}
        </button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <span>查询中...</span>
    </div>

    <!-- Result Toast -->
    <transition name="toast">
      <div v-if="showResult" class="toast" :class="resultType">
        <svg v-if="resultType === 'success'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
        <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
        {{ resultMsg }}
      </div>
    </transition>

    <!-- Student Info Card -->
    <transition name="card">
      <div v-if="studentInfo" class="student-card">
        <div class="card-header">
          <h4>学生信息</h4>
          <span class="status-badge success">可发放</span>
        </div>
        <div class="card-body">
          <div class="info-row"><span class="label">姓名</span><span class="value bold">{{ studentInfo.name }}</span></div>
          <div class="info-row"><span class="label">身份证号</span><span class="value">{{ studentInfo.idCard }}</span></div>
          <div class="info-row"><span class="label">联系电话</span><span class="value">{{ studentInfo.phone }}</span></div>
          <div class="info-row"><span class="label">学院</span><span class="value">{{ studentInfo.collegeName }}</span></div>
          <div class="info-row"><span class="label">专业</span><span class="value">{{ studentInfo.majorName }}</span></div>
          <div class="info-row"><span class="label">班级</span><span class="value">{{ studentInfo.className }}</span></div>
          <div class="divider"></div>
          <div class="info-row"><span class="label">宿舍楼栋</span><span class="value highlight">{{ studentInfo.buildingName }}</span></div>
          <div class="info-row"><span class="label">房间号</span><span class="value highlight">{{ studentInfo.roomNo }}</span></div>
          <div class="info-row" v-if="studentInfo.bedNo"><span class="label">床位号</span><span class="value highlight">{{ studentInfo.bedNo }}号床</span></div>
        </div>
        <div class="card-footer">
          <button class="btn btn-primary btn-block" :disabled="distributing" @click="handleDistribute">
            <span v-if="distributing" class="spinner"></span>
            {{ distributing ? '发放中...' : '确认发放钥匙' }}
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.distribute-page { width: 100%; max-width: 420px; display: flex; flex-direction: column; gap: 20px; }

.stats-row { display: flex; gap: 12px; }
.stat-card { background: #fff; border: 1px solid var(--gray-200); border-radius: 10px; padding: 14px 20px; flex: 1; text-align: center; }
.stat-value { font-size: 28px; font-weight: 700; color: var(--primary); }
.stat-label { font-size: 12px; color: var(--gray-400); margin-top: 2px; }

.scan-section { background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; padding: 20px; text-align: center; }
.section-title { font-size: 16px; font-weight: 600; color: var(--gray-900); margin: 0 0 4px; }
.section-desc { font-size: 13px; color: var(--gray-400); margin: 0 0 16px; }

.or-divider { display: flex; align-items: center; gap: 12px; margin: 16px 0; color: var(--gray-400); font-size: 13px; }
.or-divider::before, .or-divider::after { content: ''; flex: 1; height: 1px; background: var(--gray-200); }

.manual-form { display: flex; gap: 8px; }
.form-input { flex: 1; padding: 9px 12px; border: 1.5px solid var(--gray-200); border-radius: 8px; font-size: 14px; outline: none; }
.form-input:focus { border-color: var(--primary); }

.btn { display: inline-flex; align-items: center; justify-content: center; gap: 6px; padding: 9px 18px; border: none; border-radius: 8px; font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s; }
.btn-primary { background: var(--primary); color: #fff; }
.btn-primary:hover:not(:disabled) { opacity: 0.9; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-outline { background: #fff; color: var(--gray-600); border: 1.5px solid var(--gray-200); }
.btn-outline:hover { border-color: var(--primary); color: var(--primary); }
.btn-block { width: 100%; padding: 12px; font-size: 15px; border-radius: 10px; }

.loading-overlay { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 20px; color: var(--gray-400); font-size: 13px; }
.loading-spinner { width: 32px; height: 32px; border: 3px solid var(--gray-200); border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.toast { position: fixed; top: 20px; left: 50%; transform: translateX(-50%); display: flex; align-items: center; gap: 8px; padding: 12px 20px; border-radius: 10px; font-size: 14px; font-weight: 500; z-index: 1000; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.toast.success { background: var(--success); color: #fff; }
.toast.error { background: var(--danger); color: #fff; }
.toast-enter-active { transition: all 0.25s ease; }
.toast-leave-active { transition: all 0.15s ease; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateX(-50%) translateY(-10px); }

.student-card { background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; overflow: hidden; }
.card-header { display: flex; align-items: center; justify-content: space-between; padding: 14px 18px; border-bottom: 1px solid var(--gray-100); }
.card-header h4 { font-size: 15px; font-weight: 600; color: var(--gray-900); margin: 0; }
.status-badge { padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.status-badge.success { background: rgba(0,201,167,0.1); color: var(--success); }

.card-body { padding: 16px 18px; }
.info-row { display: flex; align-items: center; justify-content: space-between; padding: 7px 0; }
.info-row .label { font-size: 13px; color: var(--gray-400); }
.info-row .value { font-size: 14px; color: var(--gray-900); }
.info-row .value.bold { font-weight: 600; }
.info-row .value.highlight { color: var(--primary); font-weight: 600; font-size: 15px; }
.divider { height: 1px; background: var(--gray-100); margin: 8px 0; }

.card-footer { padding: 14px 18px; border-top: 1px solid var(--gray-100); }
.spinner { width: 16px; height: 16px; border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff; border-radius: 50%; animation: spin 0.7s linear infinite; }

.card-enter-active { transition: all 0.3s ease; }
.card-leave-active { transition: all 0.2s ease; }
.card-enter-from { opacity: 0; transform: translateY(10px); }
.card-leave-to { opacity: 0; transform: translateY(-10px); }
</style>
