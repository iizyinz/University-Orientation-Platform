<script setup lang="ts">
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { updatePassword } from '@/api/auth'
import { getReportCode } from '@/api/student'
import QRCode from 'qrcode'

const userStore = useUserStore()
const router = useRouter()
const userInfo = computed(() => userStore.userInfo)

// ===== 修改密码 =====
const showPasswordModal = ref(false)
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const passwordError = ref('')
const savingPassword = ref(false)
const showSuccess = ref(false)

const openPasswordModal = () => {
  passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  passwordError.value = ''
  showPasswordModal.value = true
}

const handleUpdatePassword = async () => {
  passwordError.value = ''
  const { oldPassword, newPassword, confirmPassword } = passwordForm.value
  if (!oldPassword) { passwordError.value = '请输入当前密码'; return }
  if (!newPassword) { passwordError.value = '请输入新密码'; return }
  if (newPassword.length < 6) { passwordError.value = '新密码至少6位'; return }
  if (newPassword === oldPassword) { passwordError.value = '新密码不能与旧密码相同'; return }
  if (newPassword !== confirmPassword) { passwordError.value = '两次密码不一致'; return }
  savingPassword.value = true
  try {
    await updatePassword({ id: userInfo.value.id, oldPassword, newPassword })
    showPasswordModal.value = false
    showSuccess.value = true
    setTimeout(() => { showSuccess.value = false }, 2000)
  } catch (err: any) {
    passwordError.value = err.message || '修改失败'
  } finally {
    savingPassword.value = false
  }
}

// ===== 报到码 =====
const showReportCodeModal = ref(false)
const qrDataUrl = ref('')
const reportLoading = ref(false)

const openReportCode = async () => {
  showReportCodeModal.value = true
  reportLoading.value = true
  try {
    const userId = userStore.userInfo.id
    const res: any = await getReportCode(userId)
    if (res.data.codeContent) {
      qrDataUrl.value = await QRCode.toDataURL(res.data.codeContent, {
        width: 200, margin: 2, color: { dark: '#1A1D26', light: '#FFFFFF' },
      })
    }
  } catch {} finally {
    reportLoading.value = false
  }
}

// ===== 退出登录 =====
const showLogoutConfirm = ref(false)
const handleLogout = () => { showLogoutConfirm.value = true }
const confirmLogout = () => {
  showLogoutConfirm.value = false
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="student-profile">
    <!-- Profile Header -->
    <div class="profile-header">
      <div class="avatar">{{ (userInfo.realName || '新').charAt(0) }}</div>
      <div class="user-info">
        <span class="user-name">{{ userInfo.realName || '新生' }}</span>
        <span class="user-meta">{{ userInfo.username || '' }}</span>
      </div>
    </div>

    <!-- Menu List -->
    <div class="menu-card">
      <div class="menu-item" @click="$router.push({ path: '/student', query: { tab: 'info' } })">
        <div class="menu-icon blue">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
        </div>
        <span class="menu-label">个人信息</span>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--gray-400)" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
      </div>
      <div class="menu-item" @click="openPasswordModal">
        <div class="menu-icon orange">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
        </div>
        <span class="menu-label">修改密码</span>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--gray-400)" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
      </div>
    </div>

    <div class="menu-card">
      <div class="menu-item" @click="openReportCode">
        <div class="menu-icon purple">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
        </div>
        <span class="menu-label">我的报到码</span>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--gray-400)" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
      </div>
    </div>

    <!-- Logout -->
    <div class="menu-card">
      <div class="menu-item danger" @click="handleLogout">
        <div class="menu-icon red">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
        </div>
        <span class="menu-label">退出登录</span>
      </div>
    </div>

    <p class="version-text">光谷启航 · 新生一站式服务平台 v1.0</p>

    <!-- 修改密码弹窗 -->
    <teleport to="body">
      <div v-if="showPasswordModal" class="modal-mask" @click.self="showPasswordModal = false">
        <div class="modal-box">
          <div class="modal-header">
            <h4>修改密码</h4>
            <button class="close-btn" @click="showPasswordModal = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="error-msg" v-if="passwordError">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
              </svg>
              {{ passwordError }}
            </div>
            <div class="form-group">
              <label>当前密码</label>
              <input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" />
            </div>
            <div class="form-group">
              <label>新密码</label>
              <input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码（至少6位）" />
            </div>
            <div class="form-group">
              <label>确认新密码</label>
              <input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" @keyup.enter="handleUpdatePassword" />
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-gray" @click="showPasswordModal = false">取消</button>
            <button class="btn btn-primary" @click="handleUpdatePassword" :disabled="savingPassword">
              {{ savingPassword ? '修改中...' : '确认修改' }}
            </button>
          </div>
        </div>
      </div>
    </teleport>

    <!-- 报到码弹窗 -->
    <teleport to="body">
      <div v-if="showReportCodeModal" class="modal-mask" @click.self="showReportCodeModal = false">
        <div class="modal-box" style="text-align: center;">
          <div class="modal-header">
            <h4>我的报到码</h4>
            <button class="close-btn" @click="showReportCodeModal = false">&times;</button>
          </div>
          <div class="modal-body">
            <div v-if="reportLoading" class="loading-state">
              <div class="loading-spinner"></div>
            </div>
            <div v-else-if="qrDataUrl" class="qr-area">
              <img :src="qrDataUrl" alt="报到码" class="qr-image" />
              <p class="qr-tip">到校后向辅导员或志愿者出示此码</p>
            </div>
            <div v-else class="empty-hint">暂无报到码</div>
          </div>
        </div>
      </div>
    </teleport>

    <!-- 退出确认 -->
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

    <!-- Success Toast -->
    <teleport to="body">
      <div v-if="showSuccess" class="success-toast">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5">
          <polyline points="20 6 9 17 4 12"/>
        </svg>
        密码修改成功
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.student-profile { padding-bottom: 8px; }

.profile-header {
  display: flex; align-items: center; gap: 16px;
  background: linear-gradient(135deg, var(--primary), var(--primary-deep));
  border-radius: 14px; padding: 24px 20px; margin-bottom: 16px;
}
.avatar {
  width: 56px; height: 56px; border-radius: 50%;
  background: rgba(255,255,255,0.2); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; font-weight: 700; flex-shrink: 0;
}
.user-info { display: flex; flex-direction: column; }
.user-name { font-size: 18px; font-weight: 700; color: #fff; }
.user-meta { font-size: 13px; color: rgba(255,255,255,0.7); margin-top: 2px; }

.menu-card {
  background: #fff; border-radius: 14px; overflow: hidden;
  border: 1px solid var(--gray-200); margin-bottom: 12px;
}
.menu-item {
  display: flex; align-items: center; gap: 14px;
  padding: 16px 18px; cursor: pointer; transition: background 0.15s;
}
.menu-item:not(:last-child) { border-bottom: 1px solid var(--gray-100); }
.menu-item:active { background: var(--gray-100); }
.menu-item.danger .menu-label { color: var(--danger); }

.menu-icon {
  width: 36px; height: 36px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.menu-icon.blue { background: rgba(26,92,255,0.1); color: var(--primary); }
.menu-icon.orange { background: rgba(255,122,26,0.1); color: var(--secondary-orange); }
.menu-icon.green { background: rgba(0,201,167,0.1); color: var(--success); }
.menu-icon.purple { background: rgba(130,80,255,0.1); color: #8250FF; }
.menu-icon.red { background: rgba(255,77,106,0.1); color: var(--danger); }

.menu-label { flex: 1; font-size: 14px; font-weight: 500; color: var(--gray-600); }

.version-text {
  text-align: center; font-size: 12px; color: var(--gray-400);
  margin: 20px 0; padding: 0;
}

/* Modal */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-box { background: #fff; border-radius: 14px; width: 420px; max-width: 90vw; box-shadow: 0 12px 40px rgba(0,0,0,0.12); }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 18px 20px; border-bottom: 1px solid var(--gray-200); }
.modal-header h4 { font-size: 16px; font-weight: 600; color: var(--gray-900); margin: 0; }
.close-btn { background: none; border: none; font-size: 22px; color: var(--gray-400); cursor: pointer; padding: 0 4px; line-height: 1; }
.modal-body { padding: 20px; }
.modal-footer { display: flex; gap: 10px; justify-content: flex-end; padding: 0 20px 20px; }

.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; font-weight: 500; color: var(--gray-600); margin-bottom: 6px; }
.form-group input {
  width: 100%; padding: 10px 14px; border: 1px solid var(--gray-200); border-radius: 8px;
  font-size: 14px; outline: none; box-sizing: border-box;
}
.form-group input:focus { border-color: var(--primary); }

.error-msg {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 14px; background: #FFF0F0; border: 1px solid #FFD4D4; border-radius: 8px;
  color: #FF4D6A; font-size: 13px; margin-bottom: 16px;
}

.loading-state { display: flex; justify-content: center; padding: 30px; }
.loading-spinner {
  width: 28px; height: 28px; border: 3px solid var(--gray-200);
  border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.empty-hint { text-align: center; padding: 24px; font-size: 13px; color: var(--gray-400); }

.qr-area { padding: 16px 0; }
.qr-image { width: 180px; height: 180px; border-radius: 12px; border: 1px solid var(--gray-200); }
.qr-tip { font-size: 13px; color: var(--gray-400); margin: 12px 0 0; }

/* Confirm */
.confirm-box { background: #fff; border-radius: 14px; padding: 28px; width: 320px; max-width: 85vw; text-align: center; box-shadow: 0 12px 40px rgba(0,0,0,0.12); }
.confirm-icon { width: 56px; height: 56px; border-radius: 50%; background: rgba(255,122,26,0.1); display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; }
.confirm-title { font-size: 17px; font-weight: 600; color: var(--gray-900); margin: 0 0 6px; }
.confirm-desc { font-size: 14px; color: var(--gray-400); margin: 0 0 24px; }
.confirm-actions { display: flex; gap: 10px; justify-content: center; }
.btn { display: inline-flex; align-items: center; justify-content: center; padding: 9px 22px; border: none; border-radius: 8px; font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-gray { background: var(--gray-100); color: var(--gray-600); border: 1px solid var(--gray-200); }
.btn-gray:hover { background: var(--gray-200); }
.btn-primary { background: var(--primary); color: #fff; }
.btn-primary:hover:not(:disabled) { background: var(--primary-deep); }
.btn-danger { background: var(--danger); color: #fff; }
.btn-danger:hover { opacity: 0.9; }

.success-toast {
  position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%);
  background: var(--success); color: #fff; padding: 14px 28px;
  border-radius: 12px; display: flex; align-items: center; gap: 8px;
  font-size: 15px; font-weight: 600; box-shadow: 0 8px 32px rgba(0,201,167,0.3);
  z-index: 1001; animation: fadeInUp 0.3s ease;
}
@keyframes fadeInUp { from { opacity: 0; transform: translate(-50%, -40%); } to { opacity: 1; transform: translate(-50%, -50%); } }
</style>
