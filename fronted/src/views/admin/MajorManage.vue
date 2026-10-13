<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getMajorList, getMajorByCollege, createMajor, updateMajor, deleteMajor, downloadMajorTemplate, importMajors } from '@/api/org'
import { getCollegeList } from '@/api/org'
import PagePagination from '@/components/PagePagination.vue'

interface Major {
  id: number
  collegeId: number
  collegeName: string
  majorName: string
  majorCode: string
  description: string
  sortOrder: number
  status: number
}

interface College {
  id: number
  collegeName: string
}

const tableData = ref<Major[]>([])
const collegeList = ref<College[]>([])
const filterCollegeId = ref<number | ''>('')
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增专业')
const isEdit = ref(false)
const formData = ref<Partial<Major>>({})

const pageSize = 10
const currentPage = ref(1)
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return tableData.value.slice(start, start + pageSize)
})

// Excel 导入
const importDialog = ref(false)
const importFile = ref<File | null>(null)
const importing = ref(false)
const importResult = ref<any>(null)
const dragActive = ref(false)

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
    const res: any = await downloadMajorTemplate()
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url; a.download = '专业导入模板.xlsx'; a.click()
    URL.revokeObjectURL(url)
  } catch {}
}

async function handleImport() {
  if (!importFile.value) return
  importing.value = true
  importResult.value = null
  try {
    const res: any = await importMajors(importFile.value)
    importResult.value = res.data
    loadData()
  } catch (err: any) {
    window.$message?.(err.message || '导入失败', 'error')
  } finally {
    importing.value = false
  }
}

async function loadColleges() {
  const res: any = await getCollegeList()
  collegeList.value = res.data
}

async function loadData() {
  loading.value = true
  currentPage.value = 1
  try {
    let res: any
    if (filterCollegeId.value) {
      res = await getMajorByCollege(filterCollegeId.value)
    } else {
      res = await getMajorList()
    }
    tableData.value = res.data
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增专业'
  formData.value = { collegeId: undefined, majorName: '', majorCode: '', description: '', sortOrder: 0, status: 1 }
  dialogVisible.value = true
}

function handleEdit(row: Major) {
  isEdit.value = true
  dialogTitle.value = '编辑专业'
  formData.value = { ...row }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formData.value.collegeId) return
  if (!formData.value.majorName?.trim()) return
  if (!formData.value.majorCode?.trim()) return

  try {
    if (isEdit.value) {
      await updateMajor(formData.value)
    } else {
      await createMajor(formData.value)
    }
    dialogVisible.value = false
    loadData()
  } catch (err: any) {
    window.$message?.(err.message || '操作失败', 'error')
  }
}

async function handleDelete(row: Major) {
  if (!(await window.$confirm?.(`确定删除专业「${row.majorName}」吗？`))) return
  try {
    await deleteMajor(row.id)
    loadData()
  } catch (err: any) {
    window.$message?.(err.message || '删除失败', 'error')
  }
}

onMounted(() => {
  loadColleges()
  loadData()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h3>专业管理</h3>
      <div class="header-actions">
        <select v-model="filterCollegeId" class="filter-select" @change="loadData">
          <option value="">全部学院</option>
          <option v-for="c in collegeList" :key="c.id" :value="c.id">{{ c.collegeName }}</option>
        </select>
        <button class="btn btn-outline" @click="openImport">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
          导入
        </button>
        <button class="btn btn-primary" @click="handleAdd">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          新增专业
        </button>
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th style="width:60px">序号</th>
            <th>专业名称</th>
            <th>专业编码</th>
            <th>所属学院</th>
            <th>描述</th>
            <th style="width:80px">排序</th>
            <th style="width:80px">状态</th>
            <th style="width:160px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="8" class="empty-cell">加载中...</td>
          </tr>
          <tr v-else-if="tableData.length === 0">
            <td colspan="8" class="empty-cell">暂无数据</td>
          </tr>
          <tr v-for="(row, idx) in paginatedData" :key="row.id">
            <td>{{ (currentPage - 1) * pageSize + idx + 1 }}</td>
            <td class="cell-bold">{{ row.majorName }}</td>
            <td><code class="code-tag">{{ row.majorCode }}</code></td>
            <td>
              <span class="college-tag">{{ row.collegeName }}</span>
            </td>
            <td class="cell-muted">{{ row.description || '-' }}</td>
            <td>{{ row.sortOrder }}</td>
            <td>
              <span class="status-tag" :class="row.status === 1 ? 'active' : 'inactive'">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td>
              <div class="action-btns">
                <button class="btn-text" @click="handleEdit(row)">编辑</button>
                <button class="btn-text danger" @click="handleDelete(row)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <PagePagination :total="tableData.length" :page="currentPage" :page-size="pageSize" @update:page="currentPage = $event" />
    </div>

    <!-- Import Dialog -->
    <teleport to="body">
      <div v-if="importDialog" class="modal-mask" @click.self="importDialog = false">
        <div class="modal-box modal-wide">
          <div class="modal-header">
            <h4>批量导入专业</h4>
            <button class="modal-close" @click="importDialog = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="template-tip">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="16" x2="12" y2="12"/>
                <line x1="12" y1="8" x2="12.01" y2="8"/>
              </svg>
              <span>请先下载模板，按模板格式填写数据后上传。字段：<code>专业名称</code>、<code>专业编码</code>、<code>学院编码</code></span>
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

    <!-- Edit Dialog -->
    <teleport to="body">
      <div v-if="dialogVisible" class="modal-mask" @click.self="dialogVisible = false">
        <div class="modal-box">
          <div class="modal-header">
            <h4>{{ dialogTitle }}</h4>
            <button class="modal-close" @click="dialogVisible = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-row">
              <label class="form-label">所属学院 <span class="required">*</span></label>
              <select v-model="formData.collegeId" class="form-input">
                <option :value="undefined" disabled>请选择学院</option>
                <option v-for="c in collegeList" :key="c.id" :value="c.id">{{ c.collegeName }}</option>
              </select>
            </div>
            <div class="form-row">
              <label class="form-label">专业名称 <span class="required">*</span></label>
              <input v-model="formData.majorName" class="form-input" placeholder="如：计算机科学与技术" />
            </div>
            <div class="form-row">
              <label class="form-label">专业编码 <span class="required">*</span></label>
              <input v-model="formData.majorCode" class="form-input" placeholder="如：CS01" />
            </div>
            <div class="form-row">
              <label class="form-label">描述</label>
              <textarea v-model="formData.description" class="form-input" rows="3" placeholder="专业描述（选填）"></textarea>
            </div>
            <div class="form-row-inline">
              <div class="form-row half">
                <label class="form-label">排序</label>
                <input v-model.number="formData.sortOrder" type="number" class="form-input" min="0" />
              </div>
              <div class="form-row half">
                <label class="form-label">状态</label>
                <select v-model="formData.status" class="form-input">
                  <option :value="1">启用</option>
                  <option :value="0">禁用</option>
                </select>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-gray" @click="dialogVisible = false">取消</button>
            <button class="btn btn-primary" @click="handleSubmit">确定</button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.page-container {
  max-width: 1600px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--gray-900);
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-select {
  padding: 9px 12px;
  border: 1.5px solid var(--gray-200);
  border-radius: 8px;
  font-size: 14px;
  color: var(--gray-600);
  background: #fff;
  outline: none;
  cursor: pointer;
}

.filter-select:focus {
  border-color: var(--primary);
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 18px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: var(--primary);
  color: #fff;
}

.btn-primary:hover {
  opacity: 0.9;
}

.btn-gray {
  background: var(--gray-100);
  color: var(--gray-600);
  border: 1px solid var(--gray-200);
}

.btn-gray:hover {
  background: var(--gray-200);
}

.table-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--gray-200);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 13px 16px;
  font-size: 13px;
  font-weight: 600;
  color: var(--gray-400);
  background: var(--gray-100);
  border-bottom: 1px solid var(--gray-200);
}

.data-table td {
  padding: 13px 16px;
  font-size: 14px;
  color: var(--gray-600);
  border-bottom: 1px solid var(--gray-100);
}

.data-table tbody tr:hover {
  background: rgba(26, 92, 255, 0.02);
}

.cell-bold {
  font-weight: 600;
  color: var(--gray-900);
}

.cell-muted {
  color: var(--gray-400);
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-cell {
  text-align: center;
  color: var(--gray-400);
  padding: 40px 0 !important;
}

.code-tag {
  background: var(--gray-100);
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 13px;
  color: var(--primary);
}

.college-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  background: rgba(26, 92, 255, 0.08);
  color: var(--primary);
}

.status-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.active {
  background: rgba(0, 201, 167, 0.1);
  color: var(--success);
}

.status-tag.inactive {
  background: rgba(140, 149, 166, 0.1);
  color: var(--gray-400);
}

.action-btns {
  display: flex;
  gap: 4px;
}

.btn-text {
  background: none;
  border: none;
  color: var(--primary);
  font-size: 13px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.15s;
}

.btn-text:hover {
  background: rgba(26, 92, 255, 0.06);
}

.btn-text.danger {
  color: var(--danger);
}

.btn-text.danger:hover {
  background: rgba(255, 77, 106, 0.06);
}

/* Modal */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-box {
  background: #fff;
  border-radius: 14px;
  width: 500px;
  max-width: 90vw;
  max-height: 85vh;
  overflow-y: auto;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

.modal-wide { width: 600px; }

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px 0;
}

.modal-header h4 {
  font-size: 17px;
  font-weight: 600;
  color: var(--gray-900);
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  font-size: 22px;
  color: var(--gray-400);
  cursor: pointer;
  padding: 0 4px;
  line-height: 1;
}

.modal-body {
  padding: 20px 24px;
}

.form-row {
  margin-bottom: 16px;
}

.form-row.half {
  flex: 1;
}

.form-row-inline {
  display: flex;
  gap: 16px;
}

.form-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--gray-600);
  margin-bottom: 6px;
}

.required {
  color: var(--danger);
}

.form-input {
  width: 100%;
  padding: 9px 12px;
  border: 1.5px solid var(--gray-200);
  border-radius: 8px;
  font-size: 14px;
  color: var(--gray-900);
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
  font-family: inherit;
}

.form-input:focus {
  border-color: var(--primary);
}

select.form-input {
  appearance: auto;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 0 24px 20px;
}

/* Import */
.import-tip {
  background: var(--gray-100); border-radius: 8px; padding: 14px; margin-bottom: 16px;
}
.import-tip p { font-size: 13px; color: var(--gray-600); margin: 0 0 8px; }
.import-tip p:last-child { margin-bottom: 12px; }
.btn-sm { padding: 6px 14px; font-size: 12px; }

.import-result {
  padding: 12px 14px; border-radius: 8px; margin-top: 12px;
}
.import-result.success { background: rgba(0,201,167,0.06); border: 1px solid rgba(0,201,167,0.2); }
.import-result.has-error { background: rgba(255,77,106,0.06); border: 1px solid rgba(255,77,106,0.2); }
.import-result p { font-size: 13px; color: var(--gray-600); margin: 0; }
.error-list { display: flex; flex-direction: column; gap: 4px; margin-top: 8px; }
.error-list span { font-size: 12px; color: var(--danger); }

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
