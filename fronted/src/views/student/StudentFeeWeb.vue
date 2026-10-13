<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyFee } from '@/api/profile'

const userStore = useUserStore()

const feeList = ref<any[]>([])
const loading = ref(true)

const totalFee = computed(() => {
  return feeList.value.reduce((sum: number, f: any) => sum + (f.amount || 0), 0)
})

const paidCount = computed(() => {
  return feeList.value.filter((f: any) => f.amount > 0).length
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

function formatMoney(val: number) {
  return '¥' + Number(val || 0).toFixed(2)
}

const hasPayUrl = computed(() => feeList.value.some((f: any) => f.payUrl))

function handlePay() {
  const item = feeList.value.find((f: any) => f.payUrl)
  if (item?.payUrl) {
    window.open(item.payUrl, '_blank')
  } else {
    window.$message?.('缴费通道未开启', 'info')
  }
}

onMounted(loadFee)
</script>

<template>
  <div class="fee-web">
    <div class="page-header">
      <h2>学费信息</h2>
      <p class="subtitle">查看您本学年的各项费用明细</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon blue">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ loading ? '--' : feeList.length }}</span>
          <span class="stat-label">费用项目</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ loading ? '--' : formatMoney(totalFee) }}</span>
          <span class="stat-label">费用合计</span>
        </div>
      </div>
    </div>

    <!-- 费用明细表格 -->
    <div class="table-card">
      <div class="table-header">
        <h3>费用明细</h3>
      </div>
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
      </div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th style="width:60px">序号</th>
            <th>费用项目</th>
            <th>金额</th>
            <th>备注</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="feeList.length === 0">
            <td colspan="4" class="empty-cell">暂无费用信息</td>
          </tr>
          <tr v-for="(f, i) in feeList" :key="f.id">
            <td>{{ i + 1 }}</td>
            <td class="cell-bold">{{ f.itemName || f.feeItemName || '-' }}</td>
            <td class="cell-amount">{{ formatMoney(f.amount) }}</td>
            <td class="cell-muted">{{ f.remark || '-' }}</td>
          </tr>
          <tr v-if="feeList.length > 0" class="total-row">
            <td colspan="2" class="cell-bold">合计</td>
            <td class="cell-total">{{ formatMoney(totalFee) }}</td>
            <td></td>
          </tr>
        </tbody>
      </table>
      <div v-if="feeList.length > 0" class="pay-action">
        <button class="pay-btn" @click="handlePay">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
          </svg>
          去缴费
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fee-web { padding-bottom: 24px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: var(--gray-900); margin: 0 0 4px; }
.page-header .subtitle { font-size: 14px; color: var(--gray-400); margin: 0; }

/* Stats */
.stats-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px; }
@media (max-width: 600px) { .stats-row { grid-template-columns: 1fr; } }
.stat-card {
  background: #fff; border: 1px solid var(--gray-200); border-radius: 12px;
  padding: 20px; display: flex; align-items: center; gap: 16px;
}
.stat-icon {
  width: 48px; height: 48px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.stat-icon.blue { background: rgba(26,92,255,0.1); color: var(--primary); }
.stat-icon.orange { background: rgba(255,122,26,0.1); color: var(--secondary-orange); }
.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 22px; font-weight: 700; color: var(--gray-900); }
.stat-label { font-size: 13px; color: var(--gray-400); margin-top: 2px; }

/* Table */
.table-card {
  background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; overflow: hidden;
}
.table-header {
  padding: 16px 20px; border-bottom: 1px solid var(--gray-100);
}
.table-header h3 { font-size: 15px; font-weight: 600; color: var(--gray-900); margin: 0; }

.loading-state { display: flex; justify-content: center; padding: 40px; }
.loading-spinner {
  width: 28px; height: 28px; border: 3px solid var(--gray-200);
  border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.data-table { width: 100%; border-collapse: collapse; }
.data-table th {
  text-align: left; padding: 12px 20px; font-size: 13px; font-weight: 600;
  color: var(--gray-400); background: var(--gray-100); border-bottom: 1px solid var(--gray-200);
}
.data-table td {
  padding: 14px 20px; font-size: 14px; color: var(--gray-600);
  border-bottom: 1px solid var(--gray-100);
}
.data-table tbody tr:hover { background: rgba(26,92,255,0.02); }
.cell-bold { font-weight: 600; color: var(--gray-900); }
.cell-muted { color: var(--gray-400); }
.cell-amount { font-weight: 600; color: var(--gray-900); }
.cell-total { font-size: 16px; font-weight: 700; color: var(--danger); }
.empty-cell { text-align: center; color: var(--gray-400); padding: 40px 0 !important; }
.total-row td { border-top: 2px solid var(--gray-200); padding-top: 14px; }

.pay-action { padding: 20px; display: flex; justify-content: center; }
.pay-btn {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 12px 32px; background: linear-gradient(135deg, var(--primary), var(--primary-deep));
  color: #fff; border: none; border-radius: 10px; font-size: 15px; font-weight: 600;
  cursor: pointer; transition: opacity 0.2s;
}
.pay-btn:hover { opacity: 0.9; }
</style>
