<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  getCounselorClassList, assignCounselorClasses,
  downloadCounselorClassTemplate, importCounselorClasses,
  getCollegeList, getClassList,
} from '@/api/org'

const loading = ref(false)
const counselorList = ref<any[]>([])
const collegeList = ref<any[]>([])
const allClassList = ref<any[]>([])
const filterCollegeId = ref<number | null>(null)
const searchKeyword = ref('')

const showAssignDialog = ref(false)
const currentCounselor = ref<any>(null)
const selectedClassIds = ref<number[]>([])
const assignLoading = ref(false)

// Import
const importDialog = ref(false)
const importFile = ref<File | null>(null)
const importing = ref(false)
const importResult = ref<any>(null)
const dragActive = ref(false)

const filteredList = computed(() => {
  return counselorList.value.filter(c => {
    const matchCollege = !filterCollegeId.value || c.collegeId === filterCollegeId.value
    const matchKeyword = !searchKeyword.value ||
      c.realName?.includes(searchKeyword.value) ||
      c.workNo?.includes(searchKeyword.value)
    return matchCollege && matchKeyword
  })
})

const filteredClassList = computed(() => {
  if (!currentCounselor.value?.collegeId) return allClassList.value
  return allClassList.value.filter(c => c.collegeId === currentCounselor.value.collegeId)
})

async function loadData() {
  loading.value = true
  try {
    const [counselorsRes, collegesRes, classesRes]: any = await Promise.all([
      getCounselorClassList(),
      getCollegeList(),
      getClassList(),
    ])
    counselorList.value = counselorsRes.data || []
    collegeList.value = collegesRes.data || []
    allClassList.value = classesRes.data || []
  } catch {} finally {
    loading.value = false
  }
}

function openAssignDialog(counselor: any) {
  currentCounselor.value = counselor
  selectedClassIds.value = (counselor.classes || []).map((c: any) => c.classId)
  showAssignDialog.value = true
}

function toggleClass(classId: number) {
  const idx = selectedClassIds.value.indexOf(classId)
  if (idx > -1) {
    selectedClassIds.value.splice(idx, 1)
  } else {
    selectedClassIds.value.push(classId)
  }
}

async function handleAssign() {
  if (!currentCounselor.value) return
  assignLoading.value = true
  try {
    await assignCounselorClasses({
      counselorId: currentCounselor.value.id,
      classIds: selectedClassIds.value,
    })
    showAssignDialog.value = false
    await loadData()
  } catch {} finally {
    assignLoading.value = false
  }
}

// Import functions
function openImport() {
  importFile.value = null
  importResult.value = null
  dragActive.value = false
  importDialog.value = true
}

function handleFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  importFile.value = target.files?.[0] || null
}

function handleDrop(e: DragEvent) {
  dragActive.value = false
  importFile.value = e.dataTransfer?.files?.[0] || null
}

async function handleDownloadTemplate() {
  try {
    const res: any = await downloadCounselorClassTemplate()
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url; a.download = '班级分配导入模板.xlsx'; a.click()
    URL.revokeObjectURL(url)
  } catch {}
}

async function handleImport() {
  if (!importFile.value) return
  importing.value = true
  importResult.value = null
  try {
    const res: any = await importCounselorClasses(importFile.value)
    importResult.value = res.data
    loadData()
  } catch (err: any) {
    window.$message?.(err.message || '导入失败', 'error')
  } finally {
    importing.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="class-assign">
    <div class="page-header">
      <h3>班级分配</h3>
      <p class="subtitle">管理辅导员与班级的关联关系</p>
    </div>

    <div class="filter-bar">
      <div class="search-box">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <input v-model="searchKeyword" placeholder="搜索姓名或工号..." />
      </div>
      <select v-model="filterCollegeId" class="filter-select">
        <option :value="null">全部学院</option>
        <option v-for="c in collegeList" :key="c.id" :value="c.id">{{ c.collegeName }}</option>
      </select>
      <button class="btn btn-outline" @click="openImport">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
        批量导入
      </button>
    </div>

    <div class="table-card">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>姓名</th>
            <th>工号</th>
            <th>学院</th>
            <th>已分配班级</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in filteredList" :key="c.id">
            <td>{{ c.realName }}</td>
            <td>{{ c.workNo || '-' }}</td>
            <td>{{ c.collegeName || '-' }}</td>
            <td>
              <div v-if="c.classes && c.classes.length > 0" class="tag-list">
                <span v-for="cls in c.classes" :key="cls.classId" class="tag">{{ cls.className }}</span>
              </div>
              <span v-else class="empty-text">未分配</span>
            </td>
            <td>
              <button class="btn-link" @click="openAssignDialog(c)">分配班级</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="!loading && filteredList.length === 0" class="empty-state">
        <p>暂无辅导员数据</p>
      </div>
    </div>

    <!-- Assign Dialog -->
    <teleport to="body">
      <div v-if="showAssignDialog" class="modal-mask" @click.self="showAssignDialog = false">
        <div class="assign-modal">
          <div class="modal-header">
            <h4>分配班级 - {{ currentCounselor?.realName }}</h4>
            <button class="close-btn" @click="showAssignDialog = false">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div class="assign-hint">选择需要分配给该辅导员的班级：</div>
            <div class="class-checkbox-grid">
              <label
                v-for="cls in filteredClassList"
                :key="cls.id"
                class="checkbox-item"
                :class="{ checked: selectedClassIds.includes(cls.id) }"
              >
                <input type="checkbox" :checked="selectedClassIds.includes(cls.id)" @change="toggleClass(cls.id)" />
                <span class="checkbox-label">{{ cls.className }}</span>
              </label>
            </div>
            <div v-if="filteredClassList.length === 0" class="empty-hint">该学院暂无班级</div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-gray" @click="showAssignDialog = false">取消</button>
            <button class="btn btn-primary" :disabled="assignLoading" @click="handleAssign">
              {{ assignLoading ? '保存中...' : '保存' }}
            </button>
          </div>
        </div>
      </div>
    </teleport>

    <!-- Import Dialog -->
    <teleport to="body">
      <div v-if="importDialog" class="modal-mask" @click.self="importDialog = false">
        <div class="modal-box modal-wide">
          <div class="modal-header">
            <h4>批量导入班级分配</h4>
            <button class="modal-close" @click="importDialog = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="template-tip">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="16" x2="12" y2="12"/>
                <line x1="12" y1="8" x2="12.01" y2="8"/>
              </svg>
              <span>请先下载模板，按模板格式填写数据后上传。字段：<code>辅导员工号</code>、<code>班级编码</code></span>
              <button class="link-btn" @click="handleDownloadTemplate">下载模板</button>
            </div>

            <div
              class="upload-area"
              :class="{ active: dragActive, 'has-file': !!importFile }"
              @dragover.prevent="dragActive = true"
              @dragleave="dragActive = false"
              @drop.prevent="handleDrop"
              @click="($refs.fileInput as HTMLInputElement).click()"
            >
              <input ref="fileInput" type="file" accept=".xlsx,.xls" style="display:none" @change="handleFileChange" />
              <template v-if="!importFile">
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
                    <span class="file-name">{{ importFile.name }}</span>
                    <span class="file-size">{{ (importFile.size / 1024).toFixed(1) }} KB</span>
                  </div>
                  <button class="file-remove" @click.stop="importFile = null">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                  </button>
                </div>
              </template>
            </div>

            <button
              class="btn btn-primary btn-block"
              :disabled="!importFile || importing"
              @click="handleImport"
            >
              <span v-if="importing" class="spinner"></span>
              <span v-else>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                  <polyline points="17 8 12 3 7 8"/>
                  <line x1="12" y1="3" x2="12" y2="15"/>
                </svg>
              </span>
              {{ importing ? '正在导入...' : '开始导入' }}
            </button>

            <div v-if="importResult" class="import-result">
              <div class="result-header">
                <svg v-if="!importResult.errors?.length" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--success)" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--warning)" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                <span class="result-title">导入完成</span>
              </div>
              <div class="result-stats">
                <span class="result-line success">成功：{{ importResult.successCount }} 条</span>
                <span class="result-line fail" v-if="importResult.errors?.length">失败：{{ importResult.errors.length }} 条</span>
                <span class="result-line total">共处理：{{ importResult.totalRows }} 条</span>
              </div>
              <div v-if="importResult.errors?.length" class="result-errors">
                <div class="errors-title">错误详情：</div>
                <div v-for="(err, i) in importResult.errors" :key="i" class="error-item">{{ err }}</div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-gray" @click="importDialog = false">关闭</button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.class-assign { max-width: 1600px; margin: 0 auto; }
.page-header { margin-bottom: 20px; }
.page-header h3 { font-size: 20px; font-weight: 600; color: var(--gray-900); margin: 0 0 4px; }
.subtitle { font-size: 14px; color: var(--gray-400); margin: 0; }

.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.search-box {
  display: flex; align-items: center; gap: 8px;
  background: #fff; border: 1px solid var(--gray-200); border-radius: 8px;
  padding: 0 12px; flex: 1; min-width: 200px;
}
.search-box svg { color: var(--gray-400); flex-shrink: 0; }
.search-box input { border: none; outline: none; padding: 10px 0; font-size: 14px; width: 100%; background: transparent; }

.filter-select {
  padding: 10px 12px; border: 1px solid var(--gray-200); border-radius: 8px;
  font-size: 14px; color: var(--gray-600); background: #fff; min-width: 120px;
  outline: none; cursor: pointer;
}
.filter-select:focus { border-color: var(--primary); }

.btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 10px 18px; border: none; border-radius: 8px;
  font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s;
}
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-outline { background: #fff; color: var(--gray-600); border: 1px solid var(--gray-200); }
.btn-outline:hover { background: var(--gray-100); }
.btn-primary { background: var(--primary); color: #fff; }
.btn-primary:hover:not(:disabled) { background: var(--primary-deep); }
.btn-gray { background: var(--gray-100); color: var(--gray-600); border: 1px solid var(--gray-200); }
.btn-gray:hover { background: var(--gray-200); }
.btn-block { width: 100%; justify-content: center; margin-top: 16px; }

.table-card { background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; overflow: hidden; }

.loading-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  min-height: 200px; color: var(--gray-400); gap: 12px;
}
.loading-spinner {
  width: 28px; height: 28px; border: 3px solid var(--gray-200);
  border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.data-table { width: 100%; border-collapse: collapse; }
.data-table th {
  padding: 12px 16px; text-align: left; font-size: 12px; font-weight: 600;
  color: var(--gray-400); text-transform: uppercase; background: var(--gray-100);
  border-bottom: 1px solid var(--gray-200);
}
.data-table td { padding: 14px 16px; font-size: 14px; color: var(--gray-600); border-bottom: 1px solid var(--gray-100); }
.data-table tr:last-child td { border-bottom: none; }
.data-table tr:hover td { background: var(--gray-100); }

.tag-list { display: flex; flex-wrap: wrap; gap: 6px; }
.tag {
  display: inline-block; padding: 2px 10px; border-radius: 6px; font-size: 12px;
  background: rgba(26,92,255,0.08); color: var(--primary);
}
.empty-text { color: var(--gray-400); font-size: 13px; }
.btn-link { background: none; border: none; color: var(--primary); cursor: pointer; font-size: 13px; font-weight: 500; }
.btn-link:hover { text-decoration: underline; }

.empty-state { padding: 40px; text-align: center; color: var(--gray-400); font-size: 14px; }

/* Assign Modal */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.assign-modal { background: #fff; border-radius: 14px; width: 520px; max-width: 90vw; max-height: 80vh; overflow-y: auto; box-shadow: 0 12px 40px rgba(0,0,0,0.12); }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px; border-bottom: 1px solid var(--gray-200); position: sticky; top: 0; background: #fff; z-index: 1; }
.modal-header h4 { font-size: 16px; font-weight: 600; color: var(--gray-900); margin: 0; }
.close-btn { background: none; border: none; color: var(--gray-400); cursor: pointer; padding: 4px; border-radius: 6px; }
.close-btn:hover { background: var(--gray-100); }

.modal-body { padding: 20px 24px; }
.assign-hint { font-size: 14px; color: var(--gray-600); margin-bottom: 16px; }

.class-checkbox-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.checkbox-item {
  display: flex; align-items: center; gap: 8px; padding: 10px 12px;
  border: 1px solid var(--gray-200); border-radius: 8px; cursor: pointer; transition: all 0.15s;
}
.checkbox-item:hover { border-color: var(--primary-light); }
.checkbox-item.checked { border-color: var(--primary); background: rgba(26,92,255,0.04); }
.checkbox-item input[type="checkbox"] { accent-color: var(--primary); width: 16px; height: 16px; }
.checkbox-label { font-size: 14px; color: var(--gray-600); }

.empty-hint { text-align: center; color: var(--gray-400); font-size: 14px; padding: 20px 0; }

.modal-footer { display: flex; gap: 10px; justify-content: flex-end; padding: 16px 24px; border-top: 1px solid var(--gray-200); }

/* Import Modal */
.modal-box { background: #fff; border-radius: 14px; box-shadow: 0 12px 40px rgba(0,0,0,0.12); }
.modal-wide { width: 560px; max-width: 90vw; max-height: 85vh; overflow-y: auto; }
.modal-close { background: none; border: none; font-size: 22px; color: var(--gray-400); cursor: pointer; padding: 0 4px; }
.modal-close:hover { color: var(--gray-600); }

.template-tip {
  display: flex; align-items: center; gap: 8px; padding: 12px 14px;
  background: rgba(26,92,255,0.04); border: 1px solid rgba(26,92,255,0.15); border-radius: 8px;
  font-size: 13px; color: var(--gray-600); margin-bottom: 16px;
}
.template-tip code { background: var(--gray-100); padding: 1px 6px; border-radius: 4px; font-size: 12px; color: var(--primary); }
.link-btn {
  margin-left: auto; background: none; border: none; color: var(--primary);
  cursor: pointer; font-size: 13px; font-weight: 500; white-space: nowrap;
}
.link-btn:hover { text-decoration: underline; }

.upload-area {
  border: 2px dashed var(--gray-200); border-radius: 12px; padding: 32px 20px;
  text-align: center; cursor: pointer; transition: all 0.2s;
}
.upload-area:hover, .upload-area.active { border-color: var(--primary); background: rgba(26,92,255,0.02); }
.upload-area.has-file { border-style: solid; border-color: var(--gray-200); background: var(--gray-100); padding: 16px 20px; }

.upload-icon { margin-bottom: 12px; }
.upload-text { font-size: 14px; color: var(--gray-600); margin: 0 0 4px; }
.upload-link { color: var(--primary); font-weight: 500; }
.upload-hint { font-size: 12px; color: var(--gray-400); margin: 0; }

.file-info { display: flex; align-items: center; gap: 12px; }
.file-detail { flex: 1; text-align: left; }
.file-name { display: block; font-size: 14px; font-weight: 500; color: var(--gray-900); }
.file-size { display: block; font-size: 12px; color: var(--gray-400); }
.file-remove { background: none; border: none; color: var(--gray-400); cursor: pointer; padding: 4px; border-radius: 6px; }
.file-remove:hover { color: var(--danger); background: rgba(255,77,106,0.08); }

.spinner {
  display: inline-block; width: 16px; height: 16px; border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff; border-radius: 50%; animation: spin 0.6s linear infinite;
}

.import-result { margin-top: 16px; padding: 14px; border-radius: 10px; background: var(--gray-100); }
.result-header { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.result-title { font-size: 14px; font-weight: 600; color: var(--gray-900); }
.result-stats { display: flex; gap: 16px; flex-wrap: wrap; }
.result-line { font-size: 13px; }
.result-line.success { color: var(--success); }
.result-line.fail { color: var(--danger); }
.result-line.total { color: var(--gray-400); }
.result-errors { margin-top: 12px; }
.errors-title { font-size: 13px; font-weight: 600; color: var(--danger); margin-bottom: 6px; }
.error-item { font-size: 12px; color: var(--gray-600); padding: 4px 0; border-bottom: 1px solid var(--gray-200); }
.error-item:last-child { border-bottom: none; }
</style>
