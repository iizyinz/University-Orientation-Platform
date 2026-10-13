<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const MOBILE_APP_URL = 'https://ggqhapp.iizyinz.cn'

const showVolunteerRedirect = ref(false)

function goToMobileApp() {
  window.location.href = MOBILE_APP_URL
}

const loginForm = reactive({
  username: '',
  password: '',
})

const loading = ref(false)
const errorMsg = ref('')

async function handleLogin() {
  errorMsg.value = ''

  if (!loginForm.username.trim()) {
    errorMsg.value = '请输入账号（身份证号）'
    return
  }
  if (!loginForm.password.trim()) {
    errorMsg.value = '请输入密码'
    return
  }

  loading.value = true
  try {
    const res: any = await login({
      username: loginForm.username,
      password: loginForm.password,
    })
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data)
    if (res.data.userType === 4 || res.data.userType === 5) {
      showVolunteerRedirect.value = true
    } else if (res.data.userType === 1) {
      router.push('/student-web')
    } else if (res.data.userType === 2) {
      router.push('/counselor')
    } else if (res.data.userType === 3) {
      router.push('/admin')
    } else {
      router.push('/home')
    }
  } catch (err: any) {
    errorMsg.value = err.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}

function handleKeyup(e: KeyboardEvent) {
  if (e.key === 'Enter') {
    handleLogin()
  }
}
</script>

<template>
  <div class="login-container">
    <!-- Background with Particles -->
    <div class="page-background">
      <div class="particles-flow"></div>
    </div>

    <!-- Main Content: Left Logo + Right Form -->
    <div class="login-main">
      <!-- Left: Brand -->
      <div class="login-brand">
        <div class="brand-logo">
          <div class="logo-icon">
            <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="24" cy="24" r="22" stroke="#1A5CFF" stroke-width="2" fill="none" opacity="0.3"/>
              <path d="M14 32L24 14L34 32" stroke="#1A5CFF" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="24" cy="18" r="3" fill="#FF7A1A"/>
              <path d="M18 28H30" stroke="#1A5CFF" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h1 class="brand-title">光谷启航</h1>
          <p class="brand-subtitle">新生一站式服务平台</p>
        </div>
      </div>

      <!-- Right: Login Form -->
      <div class="login-form-wrapper">
        <div class="form-card">
          <div class="form-header">
            <h2>欢迎回来</h2>
            <p>请登录您的账号以继续</p>
          </div>

          <form class="login-form" @submit.prevent="handleLogin">
          <div class="error-msg" v-if="errorMsg">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
            {{ errorMsg }}
          </div>

          <div class="form-group">
            <label for="username">账号</label>
            <div class="input-wrapper">
              <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              <input
                id="username"
                v-model="loginForm.username"
                type="text"
                placeholder="请输入身份证号"
                autocomplete="username"
                @keyup="handleKeyup"
              />
            </div>
          </div>

          <div class="form-group">
            <label for="password">密码</label>
            <div class="input-wrapper">
              <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
              <input
                id="password"
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码（身份证后六位）"
                autocomplete="current-password"
                @keyup="handleKeyup"
              />
            </div>
          </div>

          <button
            type="submit"
            class="login-btn"
            :disabled="loading"
          >
            <span v-if="loading" class="loading-spinner"></span>
            <span v-else>登 录</span>
          </button>
        </form>

          <div class="form-footer">
            <p>忘记密码？请联系辅导员重置</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Volunteer Redirect Dialog -->
    <teleport to="body">
      <div v-if="showVolunteerRedirect" class="modal-mask">
        <div class="redirect-dialog">
          <div class="redirect-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#FF7A1A" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
              <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
          </div>
          <h4>志愿者请使用手机端</h4>
          <p>志愿者功能仅支持在手机端使用，请通过手机浏览器访问以下地址：</p>
          <div class="url-box">ggqhapp.iizyinz.cn</div>
          <div class="redirect-actions">
            <button class="btn-cancel" @click="showVolunteerRedirect = false">返回登录</button>
            <button class="btn-go" @click="goToMobileApp">立即跳转</button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.login-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

/* ===== Full Page Background with Particles ===== */
.page-background {
  position: absolute;
  inset: 0;
  background: #fff;
  z-index: 0;
}

.particles-flow {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(circle at 20% 30%, rgba(26, 92, 255, 0.06) 0%, transparent 40%),
    radial-gradient(circle at 80% 70%, rgba(0, 201, 167, 0.05) 0%, transparent 40%),
    radial-gradient(circle at 50% 50%, rgba(255, 122, 26, 0.04) 0%, transparent 35%);
}

.particles-flow::before,
.particles-flow::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  animation: particleMove 12s ease-in-out infinite;
}

.particles-flow::before {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(26, 92, 255, 0.08) 0%, transparent 70%);
  top: -100px;
  left: 10%;
}

.particles-flow::after {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(0, 201, 167, 0.06) 0%, transparent 70%);
  bottom: -50px;
  right: 10%;
  animation-delay: -5s;
}

@keyframes particleMove {
  0%, 100% { transform: translate(0, 0) rotate(0deg); opacity: 0.6; }
  25% { transform: translate(40px, -60px) rotate(90deg); opacity: 0.8; }
  50% { transform: translate(20px, -100px) rotate(180deg); opacity: 0.5; }
  75% { transform: translate(-20px, -50px) rotate(270deg); opacity: 0.7; }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== Main Content: Left Brand + Right Form ===== */
.login-main {
  position: relative;
  z-index: 1;
  display: flex;
  width: 100%;
  height: 100%;
}

/* Left: Brand */
.login-brand {
  width: 45%;
  min-width: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.brand-logo {
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: fadeInUp 0.6s ease;
}

.logo-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, rgba(26, 92, 255, 0.08), rgba(0, 201, 167, 0.08));
  border-radius: 20px;
  padding: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
}

.brand-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #1A5CFF;
  letter-spacing: 4px;
}

.brand-subtitle {
  font-size: 14px;
  color: #5B678A;
  margin: 0;
  letter-spacing: 2px;
}

/* Right: Form */
.login-form-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.form-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 20px;
  padding: 44px 40px;
  box-shadow: 0 8px 40px rgba(26, 92, 255, 0.08), 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(26, 92, 255, 0.06);
  animation: fadeInUp 0.6s ease 0.1s both;
  position: relative;
}

.form-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #1A5CFF, #00C9A7);
  border-radius: 20px 20px 0 0;
}

.form-header {
  margin-bottom: 32px;
  text-align: center;
}

.form-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: #1A1D26;
  margin: 0 0 6px 0;
}

.form-header p {
  font-size: 14px;
  color: #8C95A6;
  margin: 0;
}

.error-msg {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #FFF5F5;
  border: 1px solid #FFD4D4;
  border-radius: 10px;
  color: #E84444;
  font-size: 13px;
  margin-bottom: 20px;
  animation: shakeX 0.3s ease;
}

@keyframes shakeX {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-4px); }
  75% { transform: translateX(4px); }
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #3D465C;
  margin-bottom: 8px;
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #8C95A6;
  pointer-events: none;
  transition: color 0.2s;
}

.input-wrapper:focus-within .input-icon {
  color: #1A5CFF;
}

.input-wrapper input {
  width: 100%;
  height: 50px;
  padding: 0 14px 0 44px;
  border: 1.5px solid #E8ECF1;
  border-radius: 10px;
  font-size: 15px;
  color: #3D465C;
  background: #F9FAFB;
  outline: none;
  transition: all 0.25s ease;
  box-sizing: border-box;
}

.input-wrapper input::placeholder {
  color: #8C95A6;
}

.input-wrapper input:focus {
  border-color: #1A5CFF;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(26, 92, 255, 0.1);
}

.login-btn {
  width: 100%;
  height: 50px;
  margin-top: 8px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #1A5CFF, #0033CC);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 6px;
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 14px rgba(26, 92, 255, 0.25);
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(26, 92, 255, 0.35);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2.5px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.form-footer {
  margin-top: 24px;
  text-align: center;
}

.form-footer p {
  font-size: 13px;
  color: #8C95A6;
  margin: 0;
}

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .login-main {
    flex-direction: column;
  }

  .login-brand {
    width: 100%;
    min-width: unset;
    padding: 40px 24px 20px;
  }

  .login-form-wrapper {
    padding: 20px 24px 40px;
  }

  .form-card {
    max-width: 100%;
    }
}

@media (max-width: 480px) {
  .form-card {
    padding: 32px 24px;
    border-radius: 16px;
  }

  .form-header h2 {
    font-size: 22px;
  }

  .input-wrapper input {
    height: 46px;
    font-size: 14px;
  }

  .login-btn {
    height: 46px;
    font-size: 15px;
  }
}

/* Volunteer Redirect Dialog */
.modal-mask {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center; z-index: 9999;
}
.redirect-dialog {
  background: #fff; border-radius: 16px; padding: 36px 32px; width: 380px; max-width: 90vw;
  text-align: center; box-shadow: 0 12px 40px rgba(0,0,0,0.15);
  animation: fadeInUp 0.3s ease;
}
.redirect-icon {
  width: 72px; height: 72px; border-radius: 50%; background: rgba(255,122,26,0.1);
  display: flex; align-items: center; justify-content: center; margin: 0 auto 16px;
}
.redirect-dialog h4 { font-size: 18px; font-weight: 700; color: #1A1D26; margin: 0 0 8px; }
.redirect-dialog p { font-size: 14px; color: #8C95A6; margin: 0 0 16px; line-height: 1.6; }
.url-box {
  padding: 12px 16px; background: #F5F7FA; border: 1px solid #E8ECF1; border-radius: 8px;
  font-size: 15px; font-weight: 600; color: #1A5CFF; margin-bottom: 24px; letter-spacing: 0.5px;
}
.redirect-actions { display: flex; gap: 12px; }
.btn-cancel, .btn-go {
  flex: 1; padding: 12px; border-radius: 10px; font-size: 14px; font-weight: 600;
  cursor: pointer; transition: all 0.2s; border: none;
}
.btn-cancel { background: #F5F7FA; color: #3D465C; border: 1px solid #E8ECF1; }
.btn-cancel:hover { background: #E8ECF1; }
.btn-go { background: linear-gradient(135deg, #1A5CFF, #0033CC); color: #fff; }
.btn-go:hover { opacity: 0.9; }
</style>
