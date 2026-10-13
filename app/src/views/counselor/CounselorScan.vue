<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import QrScanner from '@/components/QrScanner.vue'
import { scanStudent, checkin } from '@/api/counselor'

const userStore = useUserStore()

// 扫描模式
const scannerActive = ref(true)
const scannerRef = ref<InstanceType<typeof QrScanner>>()

// 手动输入
const showManualInput = ref(false)
const idCardInput = ref('')
const manualLoading = ref(false)
const manualError = ref('')

// 扫描/查询结果
const scanResult = ref<any>(null)
const showResult = ref(false)
const checkinLoading = ref(false)
const checkinSuccess = ref(false)

// 查询到的学生信息
const scannedStudent = ref<any>(null)

async function handleScan(idCard: string) {
  scannerActive.value = false
  scanResult.value = null
  showResult.value = true
  try {
    const res: any = await scanStudent(idCard)
    scannedStudent.value = res.data
    scanResult.value = { success: true, message: '查找到学生信息' }
  } catch (err: any) {
    scanResult.value = { success: false, message: err.message || '未找到该学生' }
    scannedStudent.value = null
  }
}

async function handleManualLookup() {
  manualError.value = ''
  if (!idCardInput.value.trim()) {
    manualError.value = '请输入身份证号'
    return
  }
  manualLoading.value = true
  showResult.value = true
  scanResult.value = null
  try {
    const res: any = await scanStudent(idCardInput.value.trim())
    scannedStudent.value = res.data
    scanResult.value = { success: true, message: '查找到学生信息' }
  } catch (err: any) {
    scanResult.value = { success: false, message: err.message || '未找到该学生' }
    scannedStudent.value = null
  } finally {
    manualLoading.value = false
  }
}

async function confirmCheckin() {
  if (!scannedStudent.value?.studentId) return
  checkinLoading.value = true
  try {
    await checkin(scannedStudent.value.studentId)
    scannedStudent.value.reportStatus = 1
    checkinSuccess.value = true
  } catch (err: any) {
    scanResult.value = { success: false, message: err.message || '报到失败' }
  } finally {
    checkinLoading.value = false
  }
}

function resetScan() {
  showResult.value = false
  scanResult.value = null
  scannedStudent.value = null
  checkinSuccess.value = false
  showManualInput.value = false
  idCardInput.value = ''
  manualError.value = ''
  scannerActive.value = true
}
</script>

<template>
  <div class="counselor-scan">
    <!-- Scanner Area -->
    <div class="scan-card">
      <div class="scan-title">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 7V5a2 2 0 0 1 2-2h2"/><path d="M17 3h2a2 2 0 0 1 2 2v2"/>
          <path d="M21 17v2a2 2 0 0 1-2 2h-2"/><path d="M7 21H5a2 2 0 0 1-2-2v-2"/>
          <line x1="7" y1="12" x2="17" y2="12"/>
        </svg>
        扫描学生报到码
      </div>
      <div v-if="scannerActive" class="scanner-area">
        <QrScanner ref="scannerRef" :active="scannerActive" @scan="handleScan" />
        <p class="scan-tip">将学生的报到二维码对准框内即可自动识别</p>
      </div>
      <div v-else class="scan-placeholder" @click="resetScan">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="var(--gray-400)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 7V5a2 2 0 0 1 2-2h2"/><path d="M17 3h2a2 2 0 0 1 2 2v2"/>
          <path d="M21 17v2a2 2 0 0 1-2 2h-2"/><path d="M7 21H5a2 2 0 0 1-2-2v-2"/>
          <line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/>
        </svg>
        <span>点击重新扫描</span>
      </div>
    </div>

    <!-- Manual Input Toggle -->
    <div class="manual-toggle" @click="showManualInput = !showManualInput">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
      {{ showManualInput ? '收起手动输入' : '手动输入身份证号查询' }}
    </div>

    <!-- Manual Input Area -->
    <div v-if="showManualInput" class="manual-card">
      <div class="manual-row">
        <input
          v-model="idCardInput"
          type="text"
          placeholder="请输入学生身份证号"
          class="manual-input"
          @keyup.enter="handleManualLookup"
        />
        <button class="lookup-btn" @click="handleManualLookup" :disabled="manualLoading">
          {{ manualLoading ? '查询中...' : '查询' }}
        </button>
      </div>
      <p v-if="manualError" class="manual-error">{{ manualError }}</p>
    </div>

    <!-- Scan Result -->
    <div v-if="showResult" class="result-card">
      <!-- Error -->
      <div v-if="scanResult && !scanResult.success" class="result-error">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--danger)" stroke-width="2">
          <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
        </svg>
        <p>{{ scanResult.message }}</p>
        <button class="action-btn" @click="resetScan">重新扫描</button>
      </div>

      <!-- Success Student Info -->
      <div v-if="scanResult && scanResult.success && scannedStudent" class="student-info-card">
<div class="info-header">
          <div class="student-avatar" v-if="!scannedStudent.photo">{{ (scannedStudent.name || '学').charAt(0) }}</div>
          <img v-else class="student-photo" :src="scannedStudent.photo" alt="证件照" />
          <div class="student-detail">
            <div class="student-name">{{ scannedStudent.name }}</div>
            <div class="student-id">学号：{{ scannedStudent.studentNo || '-' }}</div>
          </div>
        </div>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">身份证号</span>
            <span class="info-value">{{ scannedStudent.idCard || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">班级</span>
            <span class="info-value">{{ scannedStudent.className || scannedStudent.className || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">报到状态</span>
            <span class="info-value">
              <span v-if="scannedStudent.reportStatus === 1" class="status-tag success">已报到</span>
              <span v-else class="status-tag pending">未报到</span>
            </span>
          </div>
        </div>

        <!-- Checkin Button -->
        <div v-if="scannedStudent.reportStatus !== 1 && !checkinSuccess" class="checkin-section">
          <button class="checkin-btn" @click="confirmCheckin" :disabled="checkinLoading">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
            {{ checkinLoading ? '处理中...' : '确认报到' }}
          </button>
        </div>

        <!-- Already Checked In -->
        <div v-if="scannedStudent.reportStatus === 1" class="already-checked">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--success)" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
          该学生已完成报到
        </div>

        <!-- Checkin Success -->
        <div v-if="checkinSuccess" class="checkin-success">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--success)" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><polyline points="20 6 9 17 4 12"/></svg>
          报到成功！
        </div>

        <button class="action-btn secondary" @click="resetScan">继续扫描</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.counselor-scan { padding-bottom: 8px; }

.scan-card {
  background: #fff; border-radius: 14px; padding: 20px;
  border: 1px solid var(--gray-200); margin-bottom: 12px;
}
.scan-title {
  display: flex; align-items: center; gap: 8px;
  font-size: 15px; font-weight: 600; color: var(--gray-900); margin-bottom: 16px;
}
.scanner-area { display: flex; flex-direction: column; align-items: center; }
.scan-tip { font-size: 12px; color: var(--gray-400); text-align: center; margin-top: 12px; }

.scan-placeholder {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 40px; border: 2px dashed var(--gray-200); border-radius: 12px;
  cursor: pointer; color: var(--gray-400); font-size: 13px;
  transition: background 0.2s;
}
.scan-placeholder:active { background: var(--gray-100); }

/* Manual Toggle */
.manual-toggle {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 12px; font-size: 13px; color: var(--primary); cursor: pointer;
  margin-bottom: 12px;
}
.manual-toggle:active { opacity: 0.7; }

.manual-card {
  background: #fff; border-radius: 14px; padding: 18px;
  border: 1px solid var(--gray-200); margin-bottom: 12px;
}
.manual-row { display: flex; gap: 10px; }
.manual-input {
  flex: 1; padding: 10px 14px; border: 1px solid var(--gray-200); border-radius: 8px;
  font-size: 14px; outline: none;
}
.manual-input:focus { border-color: var(--primary); }
.lookup-btn {
  padding: 10px 18px; background: var(--primary); color: #fff; border: none;
  border-radius: 8px; font-size: 14px; cursor: pointer; white-space: nowrap;
}
.lookup-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.manual-error { font-size: 12px; color: var(--danger); margin-top: 8px; }

/* Result */
.result-card { margin-bottom: 12px; }

.result-error {
  display: flex; flex-direction: column; align-items: center; gap: 12px;
  padding: 24px; background: #fff; border-radius: 14px;
  border: 1px solid var(--gray-200); text-align: center;
}
.result-error p { font-size: 14px; color: var(--danger); margin: 0; }

.student-info-card {
  background: #fff; border-radius: 14px; padding: 20px;
  border: 1px solid var(--gray-200);
}
.info-header { display: flex; align-items: center; gap: 14px; margin-bottom: 16px; }
.student-avatar {
  width: 48px; height: 48px; border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--primary-deep));
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 700; flex-shrink: 0;
}
.student-photo {
  width: 48px; height: 48px; border-radius: 50%; object-fit: cover; flex-shrink: 0;
}
.student-name { font-size: 16px; font-weight: 600; color: var(--gray-900); }
.student-id { font-size: 12px; color: var(--gray-400); margin-top: 2px; }

.info-grid { display: flex; flex-direction: column; gap: 12px; margin-bottom: 16px; }
.info-item { display: flex; justify-content: space-between; align-items: center; }
.info-label { font-size: 13px; color: var(--gray-400); }
.info-value { font-size: 13px; color: var(--gray-600); font-weight: 500; }

.status-tag {
  display: inline-block; padding: 2px 10px; border-radius: 6px; font-size: 12px;
}
.status-tag.success { background: rgba(0,201,167,0.1); color: var(--success); }
.status-tag.pending { background: rgba(255,122,26,0.1); color: var(--secondary-orange); }

.checkin-section { margin-bottom: 12px; }
.checkin-btn {
  width: 100%; padding: 12px; background: linear-gradient(135deg, var(--success), #00a88b);
  color: #fff; border: none; border-radius: 10px; font-size: 15px; font-weight: 600;
  cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px;
  transition: opacity 0.2s;
}
.checkin-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.checkin-btn:active:not(:disabled) { opacity: 0.9; }

.already-checked {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 12px; background: rgba(0,201,167,0.06); border-radius: 10px;
  font-size: 14px; color: var(--gray-600); margin-bottom: 12px;
}

.checkin-success {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 12px; background: rgba(0,201,167,0.08); border-radius: 10px;
  font-size: 15px; font-weight: 600; color: var(--success); margin-bottom: 12px;
}

.action-btn {
  width: 100%; padding: 11px; border: none; border-radius: 10px;
  font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s;
  background: var(--primary); color: #fff;
}
.action-btn.secondary {
  background: var(--gray-100); color: var(--gray-600); border: 1px solid var(--gray-200);
  margin-top: 12px;
}
.action-btn.secondary:active { background: var(--gray-200); }
</style>
