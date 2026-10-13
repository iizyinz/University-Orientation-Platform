<script setup lang="ts">
import { ref, onBeforeUnmount } from 'vue'
import { useUserStore } from '@/stores/user'
import { scanStudent, checkin } from '@/api/counselor'
import { Html5Qrcode } from 'html5-qrcode'

const userStore = useUserStore()

const idCardInput = ref('')
const scanning = ref(false)
const scanResult = ref<any>(null)
const showConfirm = ref(false)
const showSuccess = ref(false)
const errorMsg = ref('')

const manualInput = ref(false)
const scannerRef = ref<Html5Qrcode | null>(null)
const scannerElementId = 'qr-scanner-region'

const startScan = async () => {
  errorMsg.value = ''
  scanning.value = true

  try {
    const scanner = new Html5Qrcode(scannerElementId)
    scannerRef.value = scanner

    await scanner.start(
      { facingMode: 'environment' },
      {
        fps: 10,
        qrbox: { width: 250, height: 250 },
        aspectRatio: 1,
      },
      async (decodedText) => {
        // QR code scanned successfully
        await stopScanner()
        idCardInput.value = decodedText.trim()
        await handleConfirm()
      },
      () => {},
    )
  } catch (err: any) {
    scanning.value = false
    if (err?.toString?.().includes('NotAllowedError')) {
      errorMsg.value = '摄像头权限被拒绝，请在浏览器设置中允许摄像头访问'
    } else if (err?.toString?.().includes('NotFoundError')) {
      errorMsg.value = '未检测到摄像头设备'
    } else {
      errorMsg.value = '摄像头启动失败，请尝试手动输入'
    }
  }
}

const stopScanner = async () => {
  if (scannerRef.value) {
    try {
      const state = scannerRef.value.getState()
      if (state === 2) { // SCANNING
        await scannerRef.value.stop()
      }
    } catch {}
    scannerRef.value.clear()
    scannerRef.value = null
  }
  scanning.value = false
}

const switchToManual = async () => {
  await stopScanner()
  manualInput.value = true
}

const switchToScan = () => {
  manualInput.value = false
  idCardInput.value = ''
  errorMsg.value = ''
}

const handleConfirm = async () => {
  if (!idCardInput.value) return
  errorMsg.value = ''
  try {
    const res: any = await scanStudent(idCardInput.value)
    scanResult.value = res.data
    showConfirm.value = true
  } catch (err: any) {
    errorMsg.value = err.message || '查询学生失败'
  }
}

const handleCheckin = async () => {
  if (!scanResult.value) return
  errorMsg.value = ''
  try {
    await checkin(scanResult.value.studentId)
    showConfirm.value = false
    showSuccess.value = true
    setTimeout(() => {
      showSuccess.value = false
      idCardInput.value = ''
      scanResult.value = null
      errorMsg.value = ''
    }, 2000)
  } catch (err: any) {
    errorMsg.value = err.message || '签到失败'
  }
}

onBeforeUnmount(() => {
  stopScanner()
})
</script>

<template>
  <div class="scan-checkin">
    <div class="page-header">
      <h3>扫码签到</h3>
      <p class="subtitle">{{ userStore.userInfo.realName || '辅导员' }} · 扫描学生报到码完成签到</p>
    </div>

    <div class="scan-container">
      <!-- Camera Area -->
      <div class="camera-area" v-if="!manualInput">
        <div class="camera-frame">
          <div class="frame-corner tl"></div>
          <div class="frame-corner tr"></div>
          <div class="frame-corner bl"></div>
          <div class="frame-corner br"></div>
          <div class="scan-line" v-if="scanning"></div>
          <!-- QR Scanner Region -->
          <div :id="scannerElementId" class="scanner-region"></div>
          <div class="camera-placeholder" v-if="!scanning">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="1.5">
              <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
              <circle cx="12" cy="13" r="4"/>
            </svg>
            <p>点击下方按钮启动摄像头扫码</p>
          </div>
        </div>
        <div class="error-msg" v-if="errorMsg && !scanning">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="15" y1="9" x2="9" y2="15"/>
            <line x1="9" y1="9" x2="15" y2="15"/>
          </svg>
          {{ errorMsg }}
        </div>
        <div class="scan-actions">
          <button v-if="!scanning" class="btn btn-primary" @click="startScan">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 7V5a2 2 0 0 1 2-2h2"/><path d="M17 3h2a2 2 0 0 1 2 2v2"/>
              <path d="M21 17v2a2 2 0 0 1-2 2h-2"/><path d="M7 21H5a2 2 0 0 1-2-2v-2"/>
            </svg>
            启动摄像头
          </button>
          <button v-if="scanning" class="btn btn-danger" @click="stopScanner">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="6" y="6" width="12" height="12" rx="2"/>
            </svg>
            停止扫码
          </button>
          <button class="btn btn-outline" @click="switchToManual">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
            </svg>
            手动输入
          </button>
        </div>
      </div>

      <!-- Manual Input Area -->
      <div class="manual-area" v-else>
        <div class="input-card">
          <label>身份证号</label>
          <div class="error-msg" v-if="errorMsg">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
            {{ errorMsg }}
          </div>
          <div class="input-row">
            <input v-model="idCardInput" placeholder="请输入学生身份证号" maxlength="18" @keyup.enter="handleConfirm" />
            <button class="btn btn-primary" @click="handleConfirm">确认</button>
          </div>
          <button class="btn-link" @click="switchToScan">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="15 18 9 12 15 6"/>
            </svg>
            返回扫码
          </button>
        </div>
      </div>
    </div>

    <!-- Confirm Dialog -->
    <teleport to="body">
      <div v-if="showConfirm && scanResult" class="modal-mask" @click.self="showConfirm = false">
        <div class="confirm-modal">
          <div class="modal-header">
            <h4>确认学生信息</h4>
            <button class="close-btn" @click="showConfirm = false">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <!-- 证件照区域 -->
            <div v-if="scanResult.photo || scanResult.idCardFront" class="doc-photos">
              <div v-if="scanResult.photo" class="doc-photo">
                <img :src="scanResult.photo" alt="证件照" />
                <span class="doc-label">证件照</span>
              </div>
              <div v-if="scanResult.idCardFront" class="doc-photo">
                <img :src="scanResult.idCardFront" alt="身份证正面" />
                <span class="doc-label">身份证正面</span>
              </div>
              <div v-if="scanResult.idCardBack" class="doc-photo">
                <img :src="scanResult.idCardBack" alt="身份证反面" />
                <span class="doc-label">身份证反面</span>
              </div>
            </div>

            <div class="info-grid">
              <div class="info-item"><span class="label">姓名</span><span class="value">{{ scanResult.name }}</span></div>
              <div class="info-item"><span class="label">学院</span><span class="value">{{ scanResult.collegeName }}</span></div>
              <div class="info-item"><span class="label">班级</span><span class="value">{{ scanResult.className }}</span></div>
              <div class="info-item"><span class="label">手机号</span><span class="value">{{ scanResult.phone }}</span></div>
            </div>
            <div v-if="scanResult.reportStatus === 1" class="already-checked">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--warning)" stroke-width="2">
                <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              该学生已完成报到
            </div>
            <div class="error-msg" v-if="errorMsg" style="margin-top: 12px;">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="15" y1="9" x2="9" y2="15"/>
                <line x1="9" y1="9" x2="15" y2="15"/>
              </svg>
              {{ errorMsg }}
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-gray" @click="showConfirm = false; errorMsg = ''">取消</button>
            <button class="btn btn-primary" :disabled="scanResult.reportStatus === 1" @click="handleCheckin">
              {{ scanResult.reportStatus === 1 ? '已报到' : '确认报到' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Success Toast -->
      <div v-if="showSuccess" class="success-toast">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
        </svg>
        <span>报到成功！</span>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.scan-checkin { max-width: 600px; }
.page-header { margin-bottom: 24px; }
.page-header h3 { font-size: 20px; font-weight: 600; color: var(--gray-900); margin: 0 0 4px; }
.subtitle { font-size: 14px; color: var(--gray-400); margin: 0; }

.camera-area { display: flex; flex-direction: column; align-items: center; }
.camera-frame {
  width: 100%; max-width: 400px; aspect-ratio: 1; border-radius: 16px;
  background: var(--gray-900); position: relative; overflow: hidden;
  display: flex; align-items: center; justify-content: center;
}
.scanner-region { position: absolute; inset: 0; z-index: 2; }
.scanner-region :deep(video) { object-fit: cover; width: 100% !important; height: 100% !important; border-radius: 16px; }
.scanner-region :deep(img[alt="Info icon"]) { display: none; }

.frame-corner { position: absolute; width: 24px; height: 24px; border-color: var(--primary); border-style: solid; z-index: 3; pointer-events: none; }
.frame-corner.tl { top: 16px; left: 16px; border-width: 3px 0 0 3px; border-radius: 4px 0 0 0; }
.frame-corner.tr { top: 16px; right: 16px; border-width: 3px 3px 0 0; border-radius: 0 4px 0 0; }
.frame-corner.bl { bottom: 16px; left: 16px; border-width: 0 0 3px 3px; border-radius: 0 0 0 4px; }
.frame-corner.br { bottom: 16px; right: 16px; border-width: 0 3px 3px 0; border-radius: 0 0 4px 0; }

.scan-line {
  position: absolute; left: 16px; right: 16px; height: 2px; z-index: 4; pointer-events: none;
  background: linear-gradient(90deg, transparent, var(--primary), transparent);
  animation: scan 2s ease-in-out infinite;
}
@keyframes scan { 0%,100% { top: 16px; } 50% { top: calc(100% - 16px); } }

.camera-placeholder { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center; color: rgba(255,255,255,0.5); z-index: 1; }
.camera-placeholder p { margin: 12px 0 0; font-size: 13px; }

.scan-actions { display: flex; gap: 12px; margin-top: 20px; }

.error-msg {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 12px; background: #FFF0F0; border: 1px solid #FFD4D4; border-radius: 6px;
  color: #FF4D6A; font-size: 13px; margin-bottom: 12px; width: 100%; max-width: 400px;
}

.already-checked {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 14px; background: rgba(255,170,76,0.1); border: 1px solid rgba(255,170,76,0.3); border-radius: 8px;
  color: var(--secondary-orange); font-size: 13px; margin-top: 12px;
}

/* Buttons */
.btn {
  display: inline-flex; align-items: center; justify-content: center; gap: 6px;
  padding: 10px 20px; border: none; border-radius: 8px;
  font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s;
}
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-primary { background: var(--primary); color: #fff; }
.btn-primary:hover:not(:disabled) { background: var(--primary-deep); }
.btn-danger { background: #FF4D6A; color: #fff; }
.btn-danger:hover { background: #e63e5a; }
.btn-outline { background: #fff; color: var(--gray-600); border: 1px solid var(--gray-200); }
.btn-outline:hover { background: var(--gray-100); }
.btn-gray { background: var(--gray-100); color: var(--gray-600); border: 1px solid var(--gray-200); }
.btn-gray:hover { background: var(--gray-200); }
.btn-link { background: none; border: none; color: var(--primary); cursor: pointer; font-size: 13px; display: inline-flex; align-items: center; gap: 4px; margin-top: 12px; }
.btn-link:hover { text-decoration: underline; }

/* Manual Input */
.manual-area { display: flex; justify-content: center; }
.input-card { background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; padding: 24px; width: 100%; max-width: 400px; }
.input-card label { display: block; font-size: 13px; font-weight: 500; color: var(--gray-600); margin-bottom: 8px; }
.input-row { display: flex; gap: 8px; }
.input-row input {
  flex: 1; padding: 10px 14px; border: 1px solid var(--gray-200); border-radius: 8px;
  font-size: 14px; outline: none;
}
.input-row input:focus { border-color: var(--primary); }

/* Modal */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.confirm-modal { background: #fff; border-radius: 14px; width: 420px; max-width: 90vw; box-shadow: 0 12px 40px rgba(0,0,0,0.12); }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px; border-bottom: 1px solid var(--gray-200); }
.modal-header h4 { font-size: 16px; font-weight: 600; color: var(--gray-900); margin: 0; }
.close-btn { background: none; border: none; color: var(--gray-400); cursor: pointer; padding: 4px; border-radius: 6px; }
.close-btn:hover { background: var(--gray-100); }
.modal-body { padding: 20px 24px; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.info-item { display: flex; flex-direction: column; }
.info-item .label { font-size: 12px; color: var(--gray-400); margin-bottom: 4px; }
.info-item .value { font-size: 14px; font-weight: 500; color: var(--gray-600); }

/* Document Photos */
.doc-photos {
  display: flex; gap: 10px; margin-bottom: 16px;
  padding-bottom: 16px; border-bottom: 1px solid var(--gray-100);
  overflow-x: auto;
}
.doc-photo {
  display: flex; flex-direction: column; align-items: center; gap: 6px;
  flex-shrink: 0;
}
.doc-photo img {
  width: 72px; height: 72px; border-radius: 8px; object-fit: cover;
  border: 1px solid var(--gray-200);
}
.doc-photo .doc-label { font-size: 11px; color: var(--gray-400); }
.modal-footer { display: flex; gap: 10px; justify-content: flex-end; padding: 16px 24px; border-top: 1px solid var(--gray-200); }

/* Success Toast */
.success-toast {
  position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%);
  background: var(--success); color: #fff; padding: 20px 32px;
  border-radius: 12px; display: flex; align-items: center; gap: 10px;
  font-size: 16px; font-weight: 600; box-shadow: 0 8px 32px rgba(0,201,167,0.3);
  z-index: 1001; animation: fadeInUp 0.3s ease;
}
@keyframes fadeInUp { from { opacity: 0; transform: translate(-50%, -40%); } to { opacity: 1; transform: translate(-50%, -50%); } }
</style>
