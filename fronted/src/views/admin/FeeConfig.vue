<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  getFeeItemList, createFeeItem, updateFeeItem, deleteFeeItem,
  getFeeConfigList, createFeeConfig, updateFeeConfig, deleteFeeConfig,
  downloadFeeConfigTemplate, importFeeConfigs
} from '@/api/fee'
import { getMajorList } from '@/api/org'

interface FeeItem {
  id: number
  itemName: string
  itemCode: string
  description: string
  sortOrder: number
  status: number
}

interface FeeConfig {
  id: number
  majorId: number
  feeItemId: number
  majorName: string
  collegeName: string
  itemName: string
  amount: number
  payUrl: string
  remark: string
}

interface Major { id: number; majorName: string; collegeName: string }

const activeTab = ref<'item' | 'config'>('item')

// ===== Fee Item =====
const itemList = ref<FeeItem[]>([])
const itemLoading = ref(false)
const itemDialogVisible = ref(false)
const itemIsEdit = ref(false)
const itemForm = ref<Partial<FeeItem>>({})

async function loadItems() {
  itemLoading.value = true
  try {
    const res: any = await getFeeItemList()
    itemList.value = res.data
  } finally {
    itemLoading.value = false
  }
}

function handleItemAdd() {
  itemIsEdit.value = false
  itemForm.value = { itemName: '', itemCode: '', description: '', sortOrder: 0, status: 1 }
  itemDialogVisible.value = true
}

function handleItemEdit(row: FeeItem) {
  itemIsEdit.value = true
  itemForm.value = { ...row }
  itemDialogVisible.value = true
}

async function handleItemSubmit() {
  if (!itemForm.value.itemName?.trim()) return
  if (!itemForm.value.itemCode?.trim()) return
  try {
    if (itemIsEdit.value) {
      await updateFeeItem(itemForm.value)
    } else {
      await createFeeItem(itemForm.value)
    }
    itemDialogVisible.value = false
    loadItems()
  } catch (err: any) {
    window.$message?.(err.message || '操作失败', 'error')
  }
}

async function handleItemDelete(row: FeeItem) {
  if (!(await window.$confirm?.(`确定删除费用项目「${row.itemName}」吗？`))) return
  try {
    await deleteFeeItem(row.id)
    loadItems()
  } catch (err: any) {
    window.$message?.(err.message || '删除失败', 'error')
  }
}

// ===== Fee Config =====
const configList = ref<FeeConfig[]>([])
const majorList = ref<Major[]>([])
const filterMajorId = ref<number | ''>('')
const configLoading = ref(false)
const configDialogVisible = ref(false)
const configIsEdit = ref(false)
const configForm = ref<Partial<FeeConfig>>({})

async function loadConfigs() {
  configLoading.value = true
  try {
    const res: any = await getFeeConfigList()
    let data = res.data
    if (filterMajorId.value) data = data.filter((c: FeeConfig) => c.majorId === filterMajorId.value)
    configList.value = data
  } finally {
    configLoading.value = false
  }
}

async function loadMajorOptions() {
  const res: any = await getMajorList()
  majorList.value = res.data
}

function handleConfigAdd() {
  configIsEdit.value = false
  configForm.value = { majorId: undefined, feeItemId: undefined, amount: 0, payUrl: '', remark: '' }
  configDialogVisible.value = true
}

function handleConfigEdit(row: FeeConfig) {
  configIsEdit.value = true
  configForm.value = { ...row }
  configDialogVisible.value = true
}

async function handleConfigSubmit() {
  if (!configForm.value.majorId) return
  if (!configForm.value.feeItemId) return
  if (!configForm.value.amount && configForm.value.amount !== 0) return
  try {
    if (configIsEdit.value) {
      await updateFeeConfig(configForm.value)
    } else {
      await createFeeConfig(configForm.value)
    }
    configDialogVisible.value = false
    loadConfigs()
  } catch (err: any) {
    window.$message?.(err.message || '操作失败', 'error')
  }
}

async function handleConfigDelete(row: FeeConfig) {
  if (!(await window.$confirm?.(`确定删除「${row.majorName} - ${row.itemName}」的费用配置吗？`))) return
  try {
    await deleteFeeConfig(row.id)
    loadConfigs()
  } catch (err: any) {
    window.$message?.(err.message || '删除失败', 'error')
  }
}

function formatAmount(val: number) {
  return '¥' + Number(val).toFixed(2)
}

// ===== Fee Config Import =====
const configImportDialog = ref(false)
const configImportFile = ref<File | null>(null)
const configImporting = ref(false)
const configImportResult = ref<any>(null)
const dragActive = ref(false)

function openConfigImport() {
  configImportFile.value = null
  configImportResult.value = null
  dragActive.value = false
  configImportDialog.value = true
}

function handleFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  configImportFile.value = target.files?.[0] || null
}

function handleDrop(e: DragEvent) {
  dragActive.value = false
  configImportFile.value = e.dataTransfer?.files?.[0] || null
}

async function handleDownloadConfigTemplate() {
  try {
    const res: any = await downloadFeeConfigTemplate()
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url; a.download = '费用配置导入模板.xlsx'; a.click()
    URL.revokeObjectURL(url)
  } catch {}
}

async function handleConfigImport() {
  if (!configImportFile.value) return
  configImporting.value = true
  configImportResult.value = null
  try {
    const res: any = await importFeeConfigs(configImportFile.value)
    configImportResult.value = res.data
    loadConfigs()
  } catch (err: any) {
    window.$message?.(err.message || '导入失败', 'error')
  } finally {
    configImporting.value = false
  }
}

onMounted(() => {
  loadItems()
  loadConfigs()
  loadMajorOptions()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h3>费用配置</h3>
    </div>

    <!-- Tabs -->
    <div class="tab-bar">
      <div class="tab-item" :class="{ active: activeTab === 'item' }" @click="activeTab = 'item'">费用项目管理</div>
      <div class="tab-item" :class="{ active: activeTab === 'config' }" @click="activeTab = 'config'">专业费用配置</div>
    </div>

    <!-- Tab: Fee Items -->
    <template v-if="activeTab === 'item'">
      <div class="section-header">
        <p class="section-desc">管理平台支持的费用类型（如学费、住宿费等）</p>
        <button class="btn btn-primary" @click="handleItemAdd">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          新增费用项目
        </button>
      </div>
      <div class="table-card">
        <table class="data-table">
          <thead>
            <tr>
              <th style="width:60px">序号</th>
              <th>费用名称</th>
              <th>费用编码</th>
              <th>描述</th>
              <th style="width:80px">排序</th>
              <th style="width:80px">状态</th>
              <th style="width:160px">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="itemLoading">
              <td colspan="7" class="empty-cell">加载中...</td>
            </tr>
            <tr v-else-if="itemList.length === 0">
              <td colspan="7" class="empty-cell">暂无数据</td>
            </tr>
            <tr v-for="(row, idx) in itemList" :key="row.id">
              <td>{{ idx + 1 }}</td>
              <td class="cell-bold">{{ row.itemName }}</td>
              <td><code class="code-tag">{{ row.itemCode }}</code></td>
              <td class="cell-muted">{{ row.description || '-' }}</td>
              <td>{{ row.sortOrder }}</td>
              <td>
                <span class="status-tag" :class="row.status === 1 ? 'active' : 'inactive'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </span>
              </td>
              <td>
                <div class="action-btns">
                  <button class="btn-text" @click="handleItemEdit(row)">编辑</button>
                  <button class="btn-text danger" @click="handleItemDelete(row)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- Tab: Fee Config -->
    <template v-if="activeTab === 'config'">
      <div class="section-header">
        <div class="section-left">
          <p class="section-desc">为各专业配置具体费用金额和缴费链接</p>
          <select v-model="filterMajorId" class="filter-select" @change="loadConfigs">
            <option value="">全部专业</option>
            <option v-for="m in majorList" :key="m.id" :value="m.id">{{ m.collegeName }} - {{ m.majorName }}</option>
          </select>
        </div>
        <div class="header-actions">
          <button class="btn btn-outline" @click="openConfigImport">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
            批量导入
          </button>
          <button class="btn btn-primary" @click="handleConfigAdd">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            新增费用配置
          </button>
        </div>
      </div>
      <div class="table-card">
        <table class="data-table">
          <thead>
            <tr>
              <th style="width:60px">序号</th>
              <th>所属学院</th>
              <th>专业</th>
              <th>费用项目</th>
              <th style="width:120px">金额</th>
              <th>缴费链接</th>
              <th>备注</th>
              <th style="width:160px">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="configLoading">
              <td colspan="8" class="empty-cell">加载中...</td>
            </tr>
            <tr v-else-if="configList.length === 0">
              <td colspan="8" class="empty-cell">暂无数据</td>
            </tr>
            <tr v-for="(row, idx) in configList" :key="row.id">
              <td>{{ idx + 1 }}</td>
              <td><span class="college-tag">{{ row.collegeName }}</span></td>
              <td class="cell-bold">{{ row.majorName }}</td>
              <td>
                <span class="item-tag">{{ row.itemName }}</span>
              </td>
              <td class="amount-cell">{{ formatAmount(row.amount) }}</td>
              <td class="cell-muted">
                <a v-if="row.payUrl" :href="row.payUrl" target="_blank" class="link">查看链接</a>
                <span v-else>-</span>
              </td>
              <td class="cell-muted">{{ row.remark || '-' }}</td>
              <td>
                <div class="action-btns">
                  <button class="btn-text" @click="handleConfigEdit(row)">编辑</button>
                  <button class="btn-text danger" @click="handleConfigDelete(row)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- Item Dialog -->
    <teleport to="body">
      <div v-if="itemDialogVisible" class="modal-mask" @click.self="itemDialogVisible = false">
        <div class="modal-box">
          <div class="modal-header">
            <h4>{{ itemIsEdit ? '编辑费用项目' : '新增费用项目' }}</h4>
            <button class="modal-close" @click="itemDialogVisible = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-row">
              <label class="form-label">费用名称 <span class="required">*</span></label>
              <input v-model="itemForm.itemName" class="form-input" placeholder="如：学费" />
            </div>
            <div class="form-row">
              <label class="form-label">费用编码 <span class="required">*</span></label>
              <input v-model="itemForm.itemCode" class="form-input" placeholder="如：TUITION" :disabled="itemIsEdit" />
            </div>
            <div class="form-row">
              <label class="form-label">描述</label>
              <textarea v-model="itemForm.description" class="form-input" rows="2" placeholder="费用描述（选填）"></textarea>
            </div>
            <div class="form-row-inline">
              <div class="form-row half">
                <label class="form-label">排序</label>
                <input v-model.number="itemForm.sortOrder" type="number" class="form-input" min="0" />
              </div>
              <div class="form-row half">
                <label class="form-label">状态</label>
                <select v-model="itemForm.status" class="form-input">
                  <option :value="1">启用</option>
                  <option :value="0">禁用</option>
                </select>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-gray" @click="itemDialogVisible = false">取消</button>
            <button class="btn btn-primary" @click="handleItemSubmit">确定</button>
          </div>
        </div>
      </div>
    </teleport>

    <!-- Config Import Dialog -->
    <teleport to="body">
      <div v-if="configImportDialog" class="modal-mask" @click.self="configImportDialog = false">
        <div class="modal-box modal-wide">
          <div class="modal-header">
            <h4>批量导入费用配置</h4>
            <button class="modal-close" @click="configImportDialog = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="template-tip">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="16" x2="12" y2="12"/>
                <line x1="12" y1="8" x2="12.01" y2="8"/>
              </svg>
              <span>请先下载模板，按模板格式填写数据后上传。字段：<code>专业编码</code>、<code>费用项目编码</code>、<code>金额</code>（已存在的配置会自动更新）</span>
              <button class="link-btn" @click="handleDownloadConfigTemplate">下载模板</button>
            </div>

            <div
              class="upload-area"
              :class="{ active: dragActive, 'has-file': !!configImportFile }"
              @dragover.prevent="dragActive = true"
              @dragleave="dragActive = false"
              @drop.prevent="handleDrop"
              @click="($refs.fileInput as HTMLInputElement).click()"
            >
              <input ref="fileInput" type="file" accept=".xlsx,.xls" style="display:none" @change="handleFileChange" />
              <template v-if="!configImportFile">
                <div class="upload-icon">
                  <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                    <polyline points="17 8 12 3 7 8"/>
                    <line x1="12" y1="3" x2="12" y2="15"/>
                  </svg>
                </div>
                <p class="upload-text">拖拽Excel文件到此处，或 <span class="upload-link">点击选择</span></p>
                <p class="upload-hint">支持 .xlsx / .xls 格式</p>
              </template>
              <template v-else>
                <div class="file-info">
                  <div class="file-icon">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                      <polyline points="14 2 14 8 20 8"/>
                    </svg>
                  </div>
                  <div class="file-detail">
                    <span class="file-name">{{ configImportFile.name }}</span>
                    <span class="file-size">{{ (configImportFile.size / 1024).toFixed(1) }} KB</span>
                  </div>
                  <button class="file-remove" @click.stop="configImportFile = null">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                  </button>
                </div>
              </template>
            </div>

            <button
              class="btn btn-primary btn-block"
              :disabled="!configImportFile || configImporting"
              @click="handleConfigImport"
            >
              <span v-if="configImporting" class="spinner"></span>
              <span v-else>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                  <polyline points="17 8 12 3 7 8"/>
                  <line x1="12" y1="3" x2="12" y2="15"/>
                </svg>
              </span>
              {{ configImporting ? '正在导入...' : '开始导入' }}
            </button>

            <div v-if="configImportResult" class="import-result">
              <div class="result-header">
                <svg v-if="!configImportResult.errors?.length" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--success)" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--warning)" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                <span class="result-title">导入完成</span>
              </div>
              <div class="result-stats">
                <span class="result-line success">成功：{{ configImportResult.successCount }} 条</span>
                <span class="result-line fail" v-if="configImportResult.errors?.length">失败：{{ configImportResult.errors.length }} 条</span>
                <span class="result-line total">共处理：{{ configImportResult.totalRows }} 条</span>
              </div>
              <div v-if="configImportResult.errors?.length" class="result-errors">
                <div class="errors-title">错误详情：</div>
                <div v-for="(err, i) in configImportResult.errors" :key="i" class="error-item">{{ err }}</div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-gray" @click="configImportDialog = false">关闭</button>
          </div>
        </div>
      </div>
    </teleport>

    <!-- Config Dialog -->
    <teleport to="body">
      <div v-if="configDialogVisible" class="modal-mask" @click.self="configDialogVisible = false">
        <div class="modal-box">
          <div class="modal-header">
            <h4>{{ configIsEdit ? '编辑费用配置' : '新增费用配置' }}</h4>
            <button class="modal-close" @click="configDialogVisible = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-row">
              <label class="form-label">所属专业 <span class="required">*</span></label>
              <select v-model="configForm.majorId" class="form-input">
                <option :value="undefined" disabled>请选择专业</option>
                <option v-for="m in majorList" :key="m.id" :value="m.id">{{ m.collegeName }} - {{ m.majorName }}</option>
              </select>
            </div>
            <div class="form-row">
              <label class="form-label">费用项目 <span class="required">*</span></label>
              <select v-model="configForm.feeItemId" class="form-input">
                <option :value="undefined" disabled>请选择费用项目</option>
                <option v-for="fi in itemList" :key="fi.id" :value="fi.id">{{ fi.itemName }}</option>
              </select>
            </div>
            <div class="form-row">
              <label class="form-label">金额（元）<span class="required">*</span></label>
              <input v-model.number="configForm.amount" type="number" class="form-input" min="0" step="0.01" placeholder="0.00" />
            </div>
            <div class="form-row">
              <label class="form-label">缴费跳转链接</label>
              <input v-model="configForm.payUrl" class="form-input" placeholder="https://..." />
            </div>
            <div class="form-row">
              <label class="form-label">备注</label>
              <textarea v-model="configForm.remark" class="form-input" rows="2" placeholder="备注信息（选填）"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-gray" @click="configDialogVisible = false">取消</button>
            <button class="btn btn-primary" @click="handleConfigSubmit">确定</button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.page-container { max-width: 1600px; margin: 0 auto; }

.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-header h3 { font-size: 18px; font-weight: 600; color: var(--gray-900); margin: 0; }

.tab-bar { display: flex; gap: 0; border-bottom: 2px solid var(--gray-200); margin-bottom: 20px; }
.tab-item { padding: 10px 20px; font-size: 14px; font-weight: 500; color: var(--gray-400); cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all 0.2s; }
.tab-item:hover { color: var(--gray-600); }
.tab-item.active { color: var(--primary); border-bottom-color: var(--primary); }

.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; flex-wrap: wrap; gap: 12px; }
.section-left { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.section-desc { font-size: 13px; color: var(--gray-400); margin: 0; }
.header-actions { display: flex; gap: 8px; }
.btn-outline { background: #fff; color: var(--gray-600); border: 1px solid var(--gray-200); }
.btn-outline:hover { background: var(--gray-100); }
.btn-sm { padding: 6px 14px; font-size: 12px; }

/* Import */
.import-tip { background: var(--gray-100); border-radius: 8px; padding: 14px; margin-bottom: 16px; }
.import-tip p { font-size: 13px; color: var(--gray-600); margin: 0 0 8px; }
.import-tip p:last-child { margin-bottom: 12px; }
.import-result { padding: 12px 14px; border-radius: 8px; margin-top: 12px; }
.import-result.success { background: rgba(0,201,167,0.06); border: 1px solid rgba(0,201,167,0.2); }
.import-result.has-error { background: rgba(255,77,106,0.06); border: 1px solid rgba(255,77,106,0.2); }
.import-result p { font-size: 13px; color: var(--gray-600); margin: 0; }
.error-list { display: flex; flex-direction: column; gap: 4px; margin-top: 8px; }
.error-list span { font-size: 12px; color: var(--danger); }

.filter-select { padding: 9px 12px; border: 1.5px solid var(--gray-200); border-radius: 8px; font-size: 14px; color: var(--gray-600); background: #fff; outline: none; cursor: pointer; }
.filter-select:focus { border-color: var(--primary); }

.btn { display: inline-flex; align-items: center; gap: 6px; padding: 9px 18px; border: none; border-radius: 8px; font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s; }
.btn-primary { background: var(--primary); color: #fff; }
.btn-primary:hover { opacity: 0.9; }
.btn-gray { background: var(--gray-100); color: var(--gray-600); border: 1px solid var(--gray-200); }
.btn-gray:hover { background: var(--gray-200); }

.table-card { background: #fff; border-radius: 12px; border: 1px solid var(--gray-200); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { text-align: left; padding: 13px 14px; font-size: 13px; font-weight: 600; color: var(--gray-400); background: var(--gray-100); border-bottom: 1px solid var(--gray-200); white-space: nowrap; }
.data-table td { padding: 12px 14px; font-size: 13px; color: var(--gray-600); border-bottom: 1px solid var(--gray-100); }
.data-table tbody tr:hover { background: rgba(26, 92, 255, 0.02); }
.cell-bold { font-weight: 600; color: var(--gray-900); }
.cell-muted { color: var(--gray-400); }
.empty-cell { text-align: center; color: var(--gray-400); padding: 40px 0 !important; }
.code-tag { background: var(--gray-100); padding: 3px 8px; border-radius: 4px; font-size: 12px; color: var(--primary); }
.status-tag { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.status-tag.active { background: rgba(0, 201, 167, 0.1); color: var(--success); }
.status-tag.inactive { background: rgba(140, 149, 166, 0.1); color: var(--gray-400); }
.college-tag { display: inline-block; padding: 2px 8px; border-radius: 12px; font-size: 12px; background: rgba(26, 92, 255, 0.08); color: var(--primary); }
.item-tag { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 12px; background: rgba(255, 122, 26, 0.08); color: var(--secondary-orange); }
.amount-cell { font-weight: 600; color: var(--secondary-orange); white-space: nowrap; }
.link { color: var(--primary); text-decoration: none; font-size: 13px; }
.link:hover { text-decoration: underline; }
.action-btns { display: flex; gap: 4px; }
.btn-text { background: none; border: none; color: var(--primary); font-size: 13px; cursor: pointer; padding: 4px 8px; border-radius: 4px; transition: background 0.15s; }
.btn-text:hover { background: rgba(26, 92, 255, 0.06); }
.btn-text.danger { color: var(--danger); }
.btn-text.danger:hover { background: rgba(255, 77, 106, 0.06); }

.modal-mask { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-box { background: #fff; border-radius: 14px; width: 500px; max-width: 90vw; max-height: 85vh; overflow-y: auto; box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12); }
.modal-wide { width: 600px; }
.modal-header { display: flex; align-items: center; justify-content: space-between; padding: 20px 24px 0; }
.modal-header h4 { font-size: 17px; font-weight: 600; color: var(--gray-900); margin: 0; }
.modal-close { background: none; border: none; font-size: 22px; color: var(--gray-400); cursor: pointer; padding: 0 4px; line-height: 1; }
.modal-body { padding: 20px 24px; }
.form-row { margin-bottom: 16px; }
.form-row.half { flex: 1; }
.form-row-inline { display: flex; gap: 16px; }
.form-label { display: block; font-size: 13px; font-weight: 600; color: var(--gray-600); margin-bottom: 6px; }
.required { color: var(--danger); }
.form-input { width: 100%; padding: 9px 12px; border: 1.5px solid var(--gray-200); border-radius: 8px; font-size: 14px; color: var(--gray-900); outline: none; transition: border-color 0.2s; box-sizing: border-box; font-family: inherit; }
.form-input:focus { border-color: var(--primary); }
.form-input:disabled { background: var(--gray-100); color: var(--gray-400); cursor: not-allowed; }
select.form-input { appearance: auto; }
.modal-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 0 24px 20px; }

.template-tip { display: flex; align-items: center; gap: 10px; padding: 12px 14px; background: var(--gray-100); border-radius: 8px; font-size: 13px; color: var(--gray-600); line-height: 1.6; margin-bottom: 16px; }
.template-tip svg { flex-shrink: 0; color: var(--info); }
.template-tip code { background: #fff; padding: 1px 6px; border-radius: 3px; font-size: 12px; color: var(--primary); border: 1px solid var(--gray-200); }
.link-btn { background: none; border: none; color: var(--primary); font-size: 13px; font-weight: 600; cursor: pointer; white-space: nowrap; flex-shrink: 0; text-decoration: underline; }
.link-btn:hover { opacity: 0.8; }

.upload-area { border: 2px dashed var(--gray-200); border-radius: 12px; padding: 36px 20px; text-align: center; cursor: pointer; transition: all 0.2s; }
.upload-area:hover, .upload-area.active { border-color: var(--primary); background: rgba(26, 92, 255, 0.02); }
.upload-area.has-file { border-style: solid; border-color: var(--gray-200); background: var(--gray-100); padding: 16px 20px; }
.upload-icon { margin-bottom: 10px; }
.upload-text { font-size: 14px; color: var(--gray-600); margin: 0 0 4px; }
.upload-link { color: var(--primary); font-weight: 500; }
.upload-hint { font-size: 12px; color: var(--gray-400); margin: 0; }
.file-info { display: flex; align-items: center; gap: 12px; }
.file-icon { flex-shrink: 0; }
.file-detail { flex: 1; text-align: left; }
.file-name { display: block; font-size: 14px; font-weight: 500; color: var(--gray-900); }
.file-size { font-size: 12px; color: var(--gray-400); }
.file-remove { background: none; border: none; color: var(--gray-400); cursor: pointer; padding: 4px; border-radius: 4px; transition: all 0.15s; flex-shrink: 0; }
.file-remove:hover { color: var(--danger); background: rgba(255, 77, 106, 0.08); }

.spinner { display: inline-block; width: 16px; height: 16px; border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff; border-radius: 50%; animation: spin 0.7s linear infinite; margin-right: 6px; vertical-align: middle; }
@keyframes spin { to { transform: rotate(360deg); } }

.import-result { margin-top: 18px; padding: 16px; border-radius: 10px; background: var(--gray-100); }
.result-header { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.result-title { font-size: 15px; font-weight: 600; color: var(--gray-900); }
.result-stats { display: flex; gap: 18px; margin-bottom: 8px; }
.result-line { font-size: 14px; font-weight: 600; }
.result-line.success { color: var(--success); }
.result-line.fail { color: var(--danger); }
.result-line.total { color: var(--gray-400); font-weight: 400; font-size: 13px; }
.result-errors { margin-top: 10px; }
.errors-title { font-size: 13px; font-weight: 600; color: var(--danger); margin-bottom: 4px; }
.error-item { font-size: 12px; color: var(--danger); padding: 3px 0; line-height: 1.5; }
</style>
