<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyFee } from '@/api/profile'
import AppAlert from '@/components/AppAlert.vue'

const userStore = useUserStore()

const feeList = ref<any[]>([])
const loading = ref(true)
const showAlert = ref(false)
const alertMessage = ref('')
const alertType = ref<'info' | 'error' | 'success'>('info')

const totalFee = computed(() => {
  return feeList.value.reduce((sum, f) => sum + (f.amount || 0), 0)
})

async function loadFee() {
  loading.value = true
  try {
    const userId = userStore.userInfo.id
    const res: any = await getMyFee(userId)
    feeList.value = res.data || []
  } catch {
    feeList.value = []
  } finally {
    loading.value = false
  }
}

function handlePay() {
  const item = feeList.value.find((f: any) => f.payUrl)
  if (item?.payUrl) {
    window.open(item.payUrl, '_blank')
  } else {
    alertMessage.value = '暂未开通缴费通道'
    alertType.value = 'info'
    showAlert.value = true
  }
}

onMounted(loadFee)
</script>

<template>
  <div class="student-fee">
    <div class="page-header">
      <button class="back-btn" @click="$router.push({ path: '/student', query: { tab: 'work' } })">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
      <div>
        <h3>学费信息</h3>
        <p class="subtitle">查看您本学年的各项费用明细</p>
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>

    <template v-else>
      <div v-if="feeList.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5">
          <line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
        </svg>
        <p>暂无费用信息</p>
      </div>

      <div v-else class="fee-card">
        <div v-for="f in feeList" :key="f.id" class="fee-row">
          <div class="fee-left">
            <span class="fee-name">{{ f.itemName || f.feeItemName }}</span>
            <span v-if="f.remark" class="fee-remark">{{ f.remark }}</span>
          </div>
          <span class="fee-amount">¥{{ (f.amount || 0).toFixed(2) }}</span>
        </div>
        <div class="fee-total">
          <span>合计</span>
          <span class="total-amount">¥{{ totalFee.toFixed(2) }}</span>
        </div>
        <div class="pay-action">
          <button class="pay-btn" @click="handlePay">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
            </svg>
            去缴费
          </button>
        </div>
      </div>
    </template>

    <!-- 提示弹窗 -->
    <AppAlert v-model:show="showAlert" :message="alertMessage" :type="alertType" />
  </div>
</template>

<style scoped>
.student-fee { padding-bottom: 8px; }
.page-header { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
.back-btn {
  width: 36px; height: 36px; border-radius: 10px; border: 1px solid var(--gray-200);
  background: #fff; display: flex; align-items: center; justify-content: center;
  cursor: pointer; flex-shrink: 0; color: var(--gray-600); transition: background 0.15s;
}
.back-btn:active { background: var(--gray-100); }
.page-header h3 { font-size: 18px; font-weight: 700; color: var(--gray-900); margin: 0; }
.page-header .subtitle { font-size: 13px; color: var(--gray-400); margin: 0; }

.loading-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  min-height: 200px; color: var(--gray-400); gap: 12px;
}
.loading-spinner {
  width: 28px; height: 28px; border: 3px solid var(--gray-200);
  border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.empty-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  min-height: 200px; text-align: center; color: var(--gray-400);
}
.empty-state p { margin: 12px 0 0; font-size: 14px; }

.fee-card {
  background: #fff; border-radius: 14px; padding: 4px 0;
  border: 1px solid var(--gray-200);
}

.fee-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 20px; border-bottom: 1px solid var(--gray-100);
}
.fee-row:last-child { border-bottom: none; }
.fee-left { display: flex; flex-direction: column; }
.fee-name { font-size: 14px; font-weight: 500; color: var(--gray-600); }
.fee-remark { font-size: 12px; color: var(--gray-400); margin-top: 2px; }
.fee-amount { font-size: 16px; font-weight: 700; color: var(--gray-900); }

.fee-total {
  display: flex; justify-content: space-between; align-items: center;
  padding: 18px 20px; margin-top: 4px;
  background: #fff; border: 1px solid var(--gray-200); border-radius: 14px;
  font-size: 15px; font-weight: 600; color: var(--gray-900);
}
.total-amount { font-size: 20px; color: var(--danger); }

.pay-action { padding: 16px 0 0; }
.pay-btn {
  width: 100%; display: flex; align-items: center; justify-content: center; gap: 8px;
  padding: 14px; background: linear-gradient(135deg, var(--primary), var(--primary-deep));
  color: #fff; border: none; border-radius: 12px; font-size: 15px; font-weight: 600;
  cursor: pointer; transition: opacity 0.2s;
}
.pay-btn:active { opacity: 0.9; }
</style>
