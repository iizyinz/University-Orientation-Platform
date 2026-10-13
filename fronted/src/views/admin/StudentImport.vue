<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getStudentList, getStudentStats, createStudent, deleteStudent, resetStudentPassword, downloadStudentTemplate, importStudents } from '@/api/people'
import { getCollegeList, getMajorByCollege, getClassByMajor } from '@/api/org'
import PagePagination from '@/components/PagePagination.vue'

interface Student {
  id: number
  userId: number
  studentNo: string
  examNo: string
  idCard: string
  name: string
  gender: number
  phone: string
  email: string
  collegeId: number
  majorId: number
  classId: number
  collegeName: string
  majorName: string
  className: string
  reportStatus: number
  infoCompleted: number
}

interface College { id: number; collegeName: string }
interface Major { id: number; majorName: string }
interface ClassItem { id: number; className: string }

const tableData = ref<Student[]>([])
const stats = ref({ total: 0, reported: 0 })
const collegeList = ref<College[]>([])
const majorList = ref<Major[]>([])
const classList = ref<ClassItem[]>([])
const filterCollegeId = ref<number | ''>('')
const filterMajorId = ref<number | ''>('')
const searchKeyword = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formData = ref<any>({})

const importDialogVisible = ref(false)
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const importResult = ref<{ successCount: number; failCount: number; totalRows: number; errors: string[] } | null>(null)
const dragActive = ref(false)

const pageSize = 10
const currentPage = ref(1)
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  let data = tableData.value
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    data = data.filter(s => s.name?.toLowerCase().includes(kw) || s.studentNo?.toLowerCase().includes(kw) || s.idCard?.includes(kw) || s.phone?.includes(kw))
  }
  return data.slice(start, start + pageSize)
})

const filteredTotal = computed(() => {
  if (!searchKeyword.value) return tableData.value.length
  const kw = searchKeyword.value.toLowerCase()
  return tableData.value.filter(s => s.name?.toLowerCase().includes(kw) || s.studentNo?.toLowerCase().includes(kw) || s.idCard?.includes(kw) || s.phone?.includes(kw)).length
})

async function loadOptions() {
  const res: any = await getCollegeList()
  collegeList.value = res.data
}

async function onFilterCollegeChange() {
  filterMajorId.value = ''
  if (filterCollegeId.value) {
    const res: any = await getMajorByCollege(filterCollegeId.value)
    majorList.value = res.data
  } else {
    majorList.value = []
  }
  loadData()
}

async function loadData() {
  loading.value = true
  currentPage.value = 1
  try {
    const [stuRes, statsRes]: any = await Promise.all([getStudentList(), getStudentStats()])
    let data = stuRes.data
    if (filterCollegeId.value) data = data.filter((s: Student) => s.collegeId === filterCollegeId.value)
    if (filterMajorId.value) data = data.filter((s: Student) => s.majorId === filterMajorId.value)
    tableData.value = data
    stats.value = statsRes.data
  } finally {
    loading.value = false
  }
}

async function onFormCollegeChange() {
  formData.value.majorId = undefined
  formData.value.classId = undefined
  if (formData.value.collegeId) {
    const res: any = await getMajorByCollege(formData.value.collegeId)
    majorList.value = res.data
  } else {
    majorList.value = []
  }
  classList.value = []
}

async function onFormMajorChange() {
  formData.value.classId = undefined
  if (formData.value.majorId) {
    const res: any = await getClassByMajor(formData.value.majorId)
    classList.value = res.data
  } else {
    classList.value = []
  }
}

function handleAdd() {
  isEdit.value = false
  formData.value = { name: '', studentNo: '', examNo: '', idCard: '', gender: 1, phone: '', collegeId: undefined, majorId: undefined, classId: undefined }
  majorList.value = []
  classList.value = []
  dialogVisible.value = true
}

function handleEdit(row: Student) {
  isEdit.value = true
  formData.value = { ...row }
  onFormCollegeChange().then(() => {
    formData.value.majorId = row.majorId
    onFormMajorChange().then(() => {
      formData.value.classId = row.classId
    })
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formData.value.name?.trim()) return
  if (!formData.value.idCard?.trim()) return

  try {
    if (!isEdit.value) {
      await createStudent(formData.value)
    }
    dialogVisible.value = false
    loadData()
  } catch (err: any) {
    window.$message?.(err.message || '操作失败', 'error')
  }
}

async function handleDelete(row: Student) {
  if (!(await window.$confirm?.(`确定删除学生「${row.name}」吗？`))) return
  try {
    await deleteStudent(row.id)
    loadData()
  } catch (err: any) {
    window.$message?.(err.message || '删除失败', 'error')
  }
}

async function handleResetPassword(row: Student) {
  if (!row.idCard) { window.$message?.('该学生无身份证号，无法重置密码', 'error'); return }
  if (!(await window.$confirm?.(`确定重置学生「${row.name}」的密码吗？`))) return
  try {
    const newPwd = row.idCard.slice(-6)
    const res: any = await resetStudentPassword({ id: row.id, newPassword: newPwd })
    window.$message?.(res.message || `密码已重置为: ${newPwd}`, 'success')
  } catch (err: any) {
    window.$message?.(err.message || '重置失败', 'error')
  }
}

// ===== Excel Import =====

function handleImport() {
  selectedFile.value = null
  importResult.value = null
  importDialogVisible.value = true
}

async function handleDownloadTemplate() {
  try {
    const res: any = await downloadStudentTemplate()
    const blob = res.data
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '新生导入模板.xlsx'
    a.click()
    window.URL.revokeObjectURL(url)
  } catch {
    window.$message?.('模板下载失败', 'error')
  }
}

function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files && input.files.length > 0) {
    selectedFile.value = input.files[0]
    importResult.value = null
  }
}

function onDrop(e: DragEvent) {
  dragActive.value = false
  const files = e.dataTransfer?.files
  if (files && files.length > 0) {
    const file = files[0]
    if (file.name.endsWith('.xlsx') || file.name.endsWith('.xls')) {
      selectedFile.value = file
      importResult.value = null
    } else {
      window.$message?.('仅支持 .xlsx 或 .xls 格式的Excel文件', 'error')
    }
  }
}

function onDragOver() {
  dragActive.value = true
}

function onDragLeave() {
  dragActive.value = false
}

function removeFile() {
  selectedFile.value = null
  importResult.value = null
}

async function handleUpload() {
  if (!selectedFile.value) return
  uploading.value = true
  importResult.value = null
  try {
    const res: any = await importStudents(selectedFile.value)
    importResult.value = res.data
    loadData()
  } catch (err: any) {
    window.$message?.(err.message || '导入失败', 'error')
  } finally {
    uploading.value = false
  }
}

onMounted(() => {
  loadOptions()
  loadData()
})
</script>

<template>
  <div class="page-container">
    <!-- Stats -->
    <div class="stats-row">
      <div class="stat-mini">
        <span class="stat-mini-value">{{ stats.total }}</span>
        <span class="stat-mini-label">新生总数</span>
      </div>
      <div class="stat-mini green">
        <span class="stat-mini-value">{{ stats.reported }}</span>
        <span class="stat-mini-label">已报到</span>
      </div>
      <div class="stat-mini orange">
        <span class="stat-mini-value">{{ stats.total - stats.reported }}</span>
        <span class="stat-mini-label">未报到</span>
      </div>
    </div>

    <div class="page-header">
      <h3>新生数据管理</h3>
      <div class="header-actions">
        <div class="search-box">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <input v-model="searchKeyword" placeholder="搜索姓名/学号/身份证..." @input="currentPage = 1" />
        </div>
        <select v-model="filterCollegeId" class="filter-select" @change="onFilterCollegeChange">
          <option value="">全部学院</option>
          <option v-for="c in collegeList" :key="c.id" :value="c.id">{{ c.collegeName }}</option>
        </select>
        <select v-model="filterMajorId" class="filter-select" @change="loadData">
          <option value="">全部专业</option>
          <option v-for="m in majorList" :key="m.id" :value="m.id">{{ m.majorName }}</option>
        </select>
        <button class="btn btn-outline" @click="handleImport">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
          Excel导入
        </button>
        <button class="btn btn-primary" @click="handleAdd">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          新增学生
        </button>
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th style="width:60px">序号</th>
            <th>姓名</th>
            <th>学号</th>
            <th>身份证号</th>
            <th>性别</th>
            <th>学院</th>
            <th>专业</th>
            <th>班级</th>
            <th>报到</th>
            <th style="width:120px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="10" class="empty-cell">加载中...</td>
          </tr>
          <tr v-else-if="tableData.length === 0">
            <td colspan="10" class="empty-cell">暂无数据</td>
          </tr>
          <tr v-for="(row, idx) in paginatedData" :key="row.id">
            <td>{{ (currentPage - 1) * pageSize + idx + 1 }}</td>
            <td class="cell-bold">{{ row.name }}</td>
            <td><code class="code-tag">{{ row.studentNo || '-' }}</code></td>
            <td class="cell-muted">{{ row.idCard }}</td>
            <td>{{ row.gender === 1 ? '男' : '女' }}</td>
            <td><span class="college-tag">{{ row.collegeName }}</span></td>
            <td class="cell-muted">{{ row.majorName }}</td>
            <td class="cell-muted">{{ row.className }}</td>
            <td>
              <span class="status-tag" :class="row.reportStatus === 1 ? 'active' : 'inactive'">
                {{ row.reportStatus === 1 ? '已报到' : '未报到' }}
              </span>
            </td>
            <td>
              <div class="action-btns">
                <button class="btn-text" @click="handleEdit(row)">编辑</button>
                <button class="btn-text warning" @click="handleResetPassword(row)">重置密码</button>
                <button class="btn-text danger" @click="handleDelete(row)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <PagePagination :total="filteredTotal" :page="currentPage" :page-size="pageSize" @update:page="currentPage = $event" />
    </div>
    <teleport to="body">
      <div v-if="dialogVisible" class="modal-mask" @click.self="dialogVisible = false">
        <div class="modal-box modal-wide">
          <div class="modal-header">
            <h4>{{ isEdit ? '编辑学生' : '新增学生' }}</h4>
            <button class="modal-close" @click="dialogVisible = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-row-inline">
              <div class="form-row half">
                <label class="form-label">姓名 <span class="required">*</span></label>
                <input v-model="formData.name" class="form-input" placeholder="学生姓名" />
              </div>
              <div class="form-row half">
                <label class="form-label">性别</label>
                <select v-model="formData.gender" class="form-input">
                  <option :value="1">男</option>
                  <option :value="2">女</option>
                </select>
              </div>
            </div>
            <div class="form-row-inline">
              <div class="form-row half">
                <label class="form-label">身份证号 <span class="required">*</span></label>
                <input v-model="formData.idCard" class="form-input" placeholder="18位身份证号" maxlength="18" />
              </div>
              <div class="form-row half">
                <label class="form-label">学号</label>
                <input v-model="formData.studentNo" class="form-input" placeholder="学号" />
              </div>
            </div>
            <div class="form-row-inline">
              <div class="form-row half">
                <label class="form-label">联系电话</label>
                <input v-model="formData.phone" class="form-input" placeholder="手机号" />
              </div>
              <div class="form-row half">
                <label class="form-label">邮箱</label>
                <input v-model="formData.email" class="form-input" placeholder="邮箱（选填）" />
              </div>
            </div>
            <div class="form-row-inline">
              <div class="form-row third">
                <label class="form-label">学院</label>
                <select v-model="formData.collegeId" class="form-input" @change="onFormCollegeChange">
                  <option :value="undefined">请选择</option>
                  <option v-for="c in collegeList" :key="c.id" :value="c.id">{{ c.collegeName }}</option>
                </select>
              </div>
              <div class="form-row third">
                <label class="form-label">专业</label>
                <select v-model="formData.majorId" class="form-input" @change="onFormMajorChange">
                  <option :value="undefined">请选择</option>
                  <option v-for="m in majorList" :key="m.id" :value="m.id">{{ m.majorName }}</option>
                </select>
              </div>
              <div class="form-row third">
                <label class="form-label">班级</label>
                <select v-model="formData.classId" class="form-input">
                  <option :value="undefined">请选择</option>
                  <option v-for="cl in classList" :key="cl.id" :value="cl.id">{{ cl.className }}</option>
                </select>
              </div>
            </div>
            <div v-if="!isEdit" class="form-hint">初始密码为身份证号后六位</div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-gray" @click="dialogVisible = false">取消</button>
            <button class="btn btn-primary" @click="handleSubmit">确定</button>
          </div>
        </div>
      </div>
    </teleport>

    <!-- Import Dialog -->
    <teleport to="body">
      <div v-if="importDialogVisible" class="modal-mask" @click.self="importDialogVisible = false">
        <div class="modal-box modal-wide">
          <div class="modal-header">
            <h4>Excel批量导入新生</h4>
            <button class="modal-close" @click="importDialogVisible = false">&times;</button>
          </div>
          <div class="modal-body">
            <!-- Template download -->
            <div class="template-tip">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="16" x2="12" y2="12"/>
                <line x1="12" y1="8" x2="12.01" y2="8"/>
              </svg>
              <span>请先下载模板，按模板格式填写数据后上传。字段：<code>姓名</code>、<code>学号</code>、<code>考生号</code>、<code>身份证号</code>、<code>性别</code>（男/女）、<code>联系电话</code>、<code>邮箱</code>、<code>学院编码</code>、<code>专业编码</code>、<code>班级编码</code></span>
              <button class="link-btn" @click="handleDownloadTemplate">下载模板</button>
            </div>

            <!-- Upload area -->
            <div
              class="upload-area"
              :class="{ active: dragActive, 'has-file': !!selectedFile }"
              @dragover.prevent="onDragOver"
              @dragleave="onDragLeave"
              @drop.prevent="onDrop"
              @click="($refs.fileInput as HTMLInputElement).click()"
            >
              <input
                ref="fileInput"
                type="file"
                accept=".xlsx,.xls"
                style="display:none"
                @change="onFileChange"
              />
              <template v-if="!selectedFile">
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
                    <span class="file-name">{{ selectedFile.name }}</span>
                    <span class="file-size">{{ (selectedFile.size / 1024).toFixed(1) }} KB</span>
                  </div>
                  <button class="file-remove" @click.stop="removeFile">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                  </button>
                </div>
              </template>
            </div>

            <!-- Upload button -->
            <button
              class="btn btn-primary btn-block"
              :disabled="!selectedFile || uploading"
              @click="handleUpload"
              style="margin-top: 16px;"
            >
              <span v-if="uploading" class="spinner"></span>
              <span v-else>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                  <polyline points="17 8 12 3 7 8"/>
                  <line x1="12" y1="3" x2="12" y2="15"/>
                </svg>
              </span>
              {{ uploading ? '正在导入...' : '开始导入' }}
            </button>

            <!-- Import Result -->
            <div v-if="importResult" class="import-result">
              <div class="result-header">
                <svg v-if="importResult.failCount === 0" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--success)" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--warning)" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                <span class="result-title">导入完成</span>
              </div>
              <div class="result-stats">
                <span class="result-line success">成功：{{ importResult.successCount }} 条</span>
                <span class="result-line fail" v-if="importResult.failCount > 0">失败：{{ importResult.failCount }} 条</span>
                <span class="result-line total">共处理：{{ importResult.totalRows }} 条</span>
              </div>
              <div v-if="importResult.errors.length" class="result-errors">
                <div class="errors-title">错误详情：</div>
                <div v-for="(err, i) in importResult.errors" :key="i" class="error-item">{{ err }}</div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-gray" @click="importDialogVisible = false">关闭</button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.page-container { max-width: 1800px; margin: 0 auto; }

.stats-row { display: flex; gap: 16px; margin-bottom: 20px; }
.stat-mini { background: #fff; border: 1px solid var(--gray-200); border-radius: 10px; padding: 14px 22px; display: flex; flex-direction: column; align-items: center; min-width: 110px; }
.stat-mini-value { font-size: 24px; font-weight: 700; color: var(--primary); }
.stat-mini.green .stat-mini-value { color: var(--success); }
.stat-mini.orange .stat-mini-value { color: var(--secondary-orange); }
.stat-mini-label { font-size: 12px; color: var(--gray-400); margin-top: 2px; }

.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
.page-header h3 { font-size: 18px; font-weight: 600; color: var(--gray-900); margin: 0; }
.header-actions { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.filter-select { padding: 9px 12px; border: 1.5px solid var(--gray-200); border-radius: 8px; font-size: 14px; color: var(--gray-600); background: #fff; outline: none; cursor: pointer; }
.filter-select:focus { border-color: var(--primary); }
.search-box { display: flex; align-items: center; gap: 6px; padding: 0 10px; border: 1.5px solid var(--gray-200); border-radius: 8px; background: #fff; }
.search-box svg { color: var(--gray-400); flex-shrink: 0; }
.search-box input { border: none; outline: none; padding: 8px 0; font-size: 13px; background: transparent; width: 180px; }

.btn { display: inline-flex; align-items: center; gap: 6px; padding: 9px 18px; border: none; border-radius: 8px; font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s; }
.btn-primary { background: var(--primary); color: #fff; }
.btn-primary:hover:not(:disabled) { opacity: 0.9; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-outline { background: #fff; color: var(--gray-600); border: 1.5px solid var(--gray-200); }
.btn-outline:hover { border-color: var(--primary); color: var(--primary); }
.btn-gray { background: var(--gray-100); color: var(--gray-600); border: 1px solid var(--gray-200); }
.btn-gray:hover { background: var(--gray-200); }
.btn-block { width: 100%; justify-content: center; padding: 11px 18px; }

.table-card { background: #fff; border-radius: 12px; border: 1px solid var(--gray-200); overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; min-width: 900px; }
.data-table th { text-align: left; padding: 13px 14px; font-size: 13px; font-weight: 600; color: var(--gray-400); background: var(--gray-100); border-bottom: 1px solid var(--gray-200); white-space: nowrap; }
.data-table td { padding: 12px 14px; font-size: 13px; color: var(--gray-600); border-bottom: 1px solid var(--gray-100); white-space: nowrap; }
.data-table tbody tr:hover { background: rgba(26, 92, 255, 0.02); }
.cell-bold { font-weight: 600; color: var(--gray-900); }
.cell-muted { color: var(--gray-400); }
.empty-cell { text-align: center; color: var(--gray-400); padding: 40px 0 !important; }
.code-tag { background: var(--gray-100); padding: 3px 8px; border-radius: 4px; font-size: 12px; color: var(--primary); }
.college-tag { display: inline-block; padding: 2px 8px; border-radius: 12px; font-size: 12px; background: rgba(26, 92, 255, 0.08); color: var(--primary); }
.status-tag { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.status-tag.active { background: rgba(0, 201, 167, 0.1); color: var(--success); }
.status-tag.inactive { background: rgba(140, 149, 166, 0.1); color: var(--gray-400); }
.action-btns { display: flex; gap: 4px; }
.btn-text { background: none; border: none; color: var(--primary); font-size: 13px; cursor: pointer; padding: 4px 8px; border-radius: 4px; transition: background 0.15s; }
.btn-text:hover { background: rgba(26, 92, 255, 0.06); }
.btn-text.danger { color: var(--danger); }
.btn-text.danger:hover { background: rgba(255, 77, 106, 0.06); }
.btn-text.warning { color: var(--secondary-orange); }
.btn-text.warning:hover { background: rgba(255, 170, 76, 0.1); }
.form-hint { font-size: 12px; color: var(--secondary-orange); margin-top: 4px; padding: 8px 12px; background: rgba(255, 170, 76, 0.08); border-radius: 6px; }

.modal-mask { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-box { background: #fff; border-radius: 14px; width: 500px; max-width: 90vw; max-height: 85vh; overflow-y: auto; box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12); }
.modal-box.modal-wide { width: 600px; }
.modal-header { display: flex; align-items: center; justify-content: space-between; padding: 20px 24px 0; }
.modal-header h4 { font-size: 17px; font-weight: 600; color: var(--gray-900); margin: 0; }
.modal-close { background: none; border: none; font-size: 22px; color: var(--gray-400); cursor: pointer; padding: 0 4px; line-height: 1; }
.modal-body { padding: 20px 24px; }
.form-row { margin-bottom: 14px; }
.form-row.half { flex: 1; }
.form-row.third { flex: 1; }
.form-row-inline { display: flex; gap: 14px; }
.form-label { display: block; font-size: 13px; font-weight: 600; color: var(--gray-600); margin-bottom: 5px; }
.required { color: var(--danger); }
.form-input { width: 100%; padding: 8px 12px; border: 1.5px solid var(--gray-200); border-radius: 8px; font-size: 14px; color: var(--gray-900); outline: none; transition: border-color 0.2s; box-sizing: border-box; font-family: inherit; }
.form-input:focus { border-color: var(--primary); }
select.form-input { appearance: auto; }
.modal-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 0 24px 20px; }

/* Template tip */
.template-tip { display: flex; align-items: center; gap: 10px; padding: 12px 14px; background: var(--gray-100); border-radius: 8px; font-size: 13px; color: var(--gray-600); line-height: 1.6; margin-bottom: 16px; }
.template-tip svg { flex-shrink: 0; color: var(--info); }
.template-tip code { background: #fff; padding: 1px 6px; border-radius: 3px; font-size: 12px; color: var(--primary); border: 1px solid var(--gray-200); }
.link-btn { background: none; border: none; color: var(--primary); font-size: 13px; font-weight: 600; cursor: pointer; white-space: nowrap; flex-shrink: 0; text-decoration: underline; }
.link-btn:hover { opacity: 0.8; }

/* Upload area */
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

/* Import Result */
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
