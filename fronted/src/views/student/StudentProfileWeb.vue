<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateProfile, updatePassword } from '@/api/auth'

const userStore = useUserStore()

const profileForm = reactive({
  id: 0,
  realName: '',
  phone: '',
  email: '',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const loading = ref(false)
const saving = ref(false)
const passwordSaving = ref(false)
const activeSection = ref<'info' | 'password'>('info')
const successMsg = ref('')
const errorMsg = ref('')

async function loadInfo() {
  loading.value = true
  try {
    const res: any = await getUserInfo(userStore.userInfo.id)
    const data = res.data
    profileForm.id = data.id
    profileForm.realName = data.realName || ''
    profileForm.phone = data.phone || ''
    profileForm.email = data.email || ''
  } finally {
    loading.value = false
  }
}

async function handleSaveProfile() {
  errorMsg.value = ''
  successMsg.value = ''

  passwordSaving.value = false
  saving.value = true
  try {
    const res: any = await updateProfile(profileForm)
    userStore.setUserInfo(res.data)
    successMsg.value = '个人信息更新成功'
    setTimeout(() => { successMsg.value = '' }, 3000)
  } catch (e: any) {
    errorMsg.value = e.message || '更新失败'
  } finally {
    saving.value = false
  }
}

async function handleChangePassword() {
  errorMsg.value = ''
  successMsg.value = ''

  if (!passwordForm.oldPassword) { errorMsg.value = '请输入原密码'; return }
  if (!passwordForm.newPassword) { errorMsg.value = '请输入新密码'; return }
  if (passwordForm.newPassword.length < 6) { errorMsg.value = '新密码至少6位'; return }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) { errorMsg.value = '两次输入的新密码不一致'; return }

  saving.value = false
  passwordSaving.value = true
  try {
    await updatePassword({
      id: profileForm.id,
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    successMsg.value = '密码修改成功'
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    setTimeout(() => { successMsg.value = '' }, 3000)
  } catch (e: any) {
    errorMsg.value = e.message || '修改失败'
  } finally {
    passwordSaving.value = false
  }
}

onMounted(loadInfo)
</script>

<template>
  <div class="page-container">
    <div class="profile-layout">
      <!-- Left: Avatar & Nav -->
      <div class="profile-sidebar">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <div class="avatar-placeholder">{{ (profileForm.realName || '学').charAt(0) }}</div>
          </div>
          <h3 class="user-name">{{ profileForm.realName || '学生' }}</h3>
          <p class="user-role">学生用户</p>
        </div>
        <nav class="side-nav">
          <div class="nav-item" :class="{ active: activeSection === 'info' }" @click="activeSection = 'info'">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
            </svg>
            个人信息
          </div>
          <div class="nav-item" :class="{ active: activeSection === 'password' }" @click="activeSection = 'password'">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            修改密码
          </div>
        </nav>
      </div>

      <!-- Right: Content -->
      <div class="profile-content">
        <!-- Messages -->
        <transition name="fade">
          <div v-if="successMsg" class="msg success">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            {{ successMsg }}
          </div>
        </transition>
        <transition name="fade">
          <div v-if="errorMsg" class="msg error" @click="errorMsg = ''">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
            {{ errorMsg }}
            <svg class="close-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </div>
        </transition>

        <!-- Section: Profile Info -->
        <div v-if="activeSection === 'info'" class="section-card">
          <h3 class="section-title">个人信息</h3>
          <p class="section-desc">管理您的基本账户信息</p>

          <div class="form-row">
            <label class="form-label">登录账号</label>
            <input class="form-input disabled" :value="userStore.userInfo.username" disabled />
            <span class="field-hint">登录账号由管理员创建，不可自行修改</span>
          </div>

          <div class="form-row-inline">
            <div class="form-row half">
              <label class="form-label">姓名</label>
              <input class="form-input disabled" :value="profileForm.realName" disabled />
              <span class="field-hint">姓名由管理员创建，不可自行修改</span>
            </div>
            <div class="form-row half">
              <label class="form-label">手机号</label>
              <input v-model="profileForm.phone" class="form-input" placeholder="手机号" />
            </div>
          </div>

          <div class="form-row">
            <label class="form-label">邮箱</label>
            <input v-model="profileForm.email" class="form-input" placeholder="工作邮箱" />
          </div>

          <div class="form-actions">
            <button class="btn btn-primary" :disabled="saving" @click="handleSaveProfile">
              <span v-if="saving" class="spinner"></span>
              {{ saving ? '保存中...' : '保存修改' }}
            </button>
          </div>
        </div>

        <!-- Section: Change Password -->
        <div v-if="activeSection === 'password'" class="section-card">
          <h3 class="section-title">修改密码</h3>
          <p class="section-desc">定期修改密码以保障账户安全</p>

          <div class="form-row">
            <label class="form-label">原密码 <span class="required">*</span></label>
            <input v-model="passwordForm.oldPassword" type="password" class="form-input" placeholder="请输入当前密码" autocomplete="current-password" />
          </div>

          <div class="form-row">
            <label class="form-label">新密码 <span class="required">*</span></label>
            <input v-model="passwordForm.newPassword" type="password" class="form-input" placeholder="请输入新密码（至少6位）" autocomplete="new-password" />
          </div>

          <div class="form-row">
            <label class="form-label">确认新密码 <span class="required">*</span></label>
            <input v-model="passwordForm.confirmPassword" type="password" class="form-input" placeholder="请再次输入新密码" autocomplete="new-password" />
          </div>

          <div class="form-actions">
            <button class="btn btn-primary" :disabled="passwordSaving" @click="handleChangePassword">
              <span v-if="passwordSaving" class="spinner"></span>
              {{ passwordSaving ? '修改中...' : '确认修改' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-container { max-width: 1400px; margin: 0 auto; }

.profile-layout { display: flex; gap: 24px; }

.profile-sidebar { width: 240px; flex-shrink: 0; }
.avatar-section { background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; padding: 28px 20px; text-align: center; margin-bottom: 12px; }
.avatar-wrapper { width: 80px; height: 80px; margin: 0 auto 14px; }
.avatar-img { width: 100%; height: 100%; border-radius: 50%; object-fit: cover; border: 3px solid var(--gray-200); }
.avatar-placeholder { width: 100%; height: 100%; border-radius: 50%; background: linear-gradient(135deg, var(--primary), var(--primary-deep)); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 28px; font-weight: 700; }
.user-name { font-size: 17px; font-weight: 600; color: var(--gray-900); margin: 0 0 4px; }
.user-role { font-size: 13px; color: var(--gray-400); margin: 0; }

.side-nav { background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; overflow: hidden; }
.nav-item { display: flex; align-items: center; gap: 10px; padding: 13px 18px; font-size: 14px; color: var(--gray-600); cursor: pointer; transition: all 0.15s; border-left: 3px solid transparent; }
.nav-item:hover { background: var(--gray-100); color: var(--gray-900); }
.nav-item.active { background: rgba(26,92,255,0.04); color: var(--primary); border-left-color: var(--primary); font-weight: 500; }

.profile-content { flex: 1; min-width: 0; }

.msg { display: flex; align-items: center; gap: 8px; padding: 12px 16px; border-radius: 8px; margin-bottom: 16px; font-size: 14px; cursor: pointer; }
.msg.success { background: rgba(0,201,167,0.08); color: var(--success); border: 1px solid rgba(0,201,167,0.2); }
.msg.error { background: rgba(255,77,106,0.06); color: var(--danger); border: 1px solid rgba(255,77,106,0.15); }
.msg .close-icon { margin-left: auto; opacity: 0.5; }

.section-card { background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; padding: 28px; }
.section-title { font-size: 17px; font-weight: 600; color: var(--gray-900); margin: 0 0 4px; }
.section-desc { font-size: 13px; color: var(--gray-400); margin: 0 0 24px; }

.form-row { margin-bottom: 18px; }
.form-row.half { flex: 1; }
.form-row-inline { display: flex; gap: 16px; }
.form-label { display: block; font-size: 13px; font-weight: 600; color: var(--gray-600); margin-bottom: 6px; }
.required { color: var(--danger); }
.form-input { width: 100%; padding: 9px 12px; border: 1.5px solid var(--gray-200); border-radius: 8px; font-size: 14px; color: var(--gray-900); outline: none; transition: border-color 0.2s; box-sizing: border-box; font-family: inherit; }
.form-input:focus { border-color: var(--primary); }
.form-input.disabled { background: var(--gray-100); color: var(--gray-400); cursor: not-allowed; }
.field-hint { display: block; font-size: 12px; color: var(--gray-400); margin-top: 4px; }

.form-actions { margin-top: 24px; padding-top: 20px; border-top: 1px solid var(--gray-100); }
.btn { display: inline-flex; align-items: center; gap: 6px; padding: 10px 24px; border: none; border-radius: 8px; font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s; }
.btn-primary { background: var(--primary); color: #fff; }
.btn-primary:hover:not(:disabled) { opacity: 0.9; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

.spinner { width: 16px; height: 16px; border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff; border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.fade-enter-active, .fade-leave-active { transition: all 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(-4px); }

@media (max-width: 768px) {
  .profile-layout { flex-direction: column; }
  .profile-sidebar { width: 100%; }
  .avatar-section { padding: 20px; }
  .side-nav { display: flex; }
  .nav-item { flex: 1; justify-content: center; border-left: none; border-bottom: 3px solid transparent; }
  .nav-item.active { border-bottom-color: var(--primary); border-left: none; }
}
</style>
