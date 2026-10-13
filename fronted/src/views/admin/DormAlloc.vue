<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { getAllocList, createAlloc, updateAlloc, deleteAlloc, getRoomList, getBuildingList, downloadAllocTemplate, importAllocs } from '@/api/dorm'
import { getMajorList } from '@/api/org'
import PagePagination from '@/components/PagePagination.vue'

interface Alloc { id: number; majorId: number; roomId: number; gender: number; majorName: string; collegeName: string; buildingName: string; roomNo: string; allocBedCount: number; usedBedCount: number }
interface Major { id: number; majorName: string; collegeName: string }
interface Room { id: number; buildingId: number; buildingName: string; floorNo: number; roomNo: string; bedCount: number }
interface Building { id: number; buildingName: string; buildingCode: string; floorCount: number }

const allocList = ref<Alloc[]>([])
const allocLoading = ref(false)
const majorList = ref<Major[]>([])
const buildingList = ref<Building[]>([])
const roomList = ref<Room[]>([])
const allocDialog = ref(false)
const allocIsEdit = ref(false)
const allocForm = ref<any>({})

const genderMap: Record<number, string> = { 1: '男', 2: '女' }

// 根据选中的楼栋过滤房间
const filteredRoomsByBuilding = computed(() => {
  if (!allocForm.value.buildingId) return []
  return roomList.value.filter(r => r.buildingId === allocForm.value.buildingId)
})

// 根据选中的楼栋和楼层过滤房间
const filteredRoomsByFloor = computed(() => {
  if (!allocForm.value.buildingId || allocForm.value.floorNo == null) return []
  return roomList.value.filter(r => r.buildingId === allocForm.value.buildingId && r.floorNo === allocForm.value.floorNo)
})

// 当前选中楼栋的可用楼层列表
const availableFloors = computed(() => {
  if (!allocForm.value.buildingId) return []
  const building = buildingList.value.find(b => b.id === allocForm.value.buildingId)
  if (!building) return []
  return Array.from({ length: building.floorCount }, (_, i) => i + 1)
})

// 监听楼栋变化，重置楼层和房间
watch(() => allocForm.value.buildingId, () => {
  allocForm.value.floorNo = undefined
  allocForm.value.roomId = undefined
})

// 监听楼层变化，重置房间
watch(() => allocForm.value.floorNo, () => {
  allocForm.value.roomId = undefined
})

const pageSize = 10
const currentPage = ref(1)
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return allocList.value.slice(start, start + pageSize)
})

async function loadAllocs() { allocLoading.value = true; currentPage.value = 1; try { const r: any = await getAllocList(); allocList.value = r.data } finally { allocLoading.value = false } }
async function loadMajors() { const r: any = await getMajorList(); majorList.value = r.data }
async function loadBuildings() { const r: any = await getBuildingList(); buildingList.value = r.data }
async function loadRooms() { const r: any = await getRoomList(); roomList.value = r.data }

function handleAdd() {
  allocIsEdit.value = false
  allocForm.value = { majorId: undefined, buildingId: undefined, floorNo: undefined, roomId: undefined, gender: 1, allocBedCount: 4 }
  allocDialog.value = true
}
function handleEdit(row: Alloc) {
  allocIsEdit.value = true
  // 查找对应的房间信息，设置楼栋和楼层
  const room = roomList.value.find(r => r.id === row.roomId)
  allocForm.value = { 
    ...row,
    buildingId: room?.buildingId || undefined,
    floorNo: room?.floorNo || undefined
  }
  allocDialog.value = true
}

async function handleSubmit() {
  if (!allocForm.value.majorId || !allocForm.value.roomId || allocForm.value.gender == null) return
  const payload = {
    id: allocForm.value.id || undefined,
    majorId: Number(allocForm.value.majorId),
    roomId: Number(allocForm.value.roomId),
    gender: Number(allocForm.value.gender),
    allocBedCount: Number(allocForm.value.allocBedCount) || 4,
  }
  try {
    allocIsEdit.value ? await updateAlloc(payload) : await createAlloc(payload)
    allocDialog.value = false
    loadAllocs()
  } catch (e: any) { window.$message?.(e.message || '操作失败', 'error') }
}

async function handleDelete(row: Alloc) {
  if (!(await window.$confirm?.(`确定删除「${row.majorName}(${genderMap[row.gender] || '未知'}) → ${row.buildingName} ${row.roomNo}」的分配吗？`))) return
  try { await deleteAlloc(row.id); loadAllocs() } catch (e: any) { window.$message?.(e.message || '删除失败', 'error') }
}

onMounted(() => { loadAllocs(); loadMajors(); loadBuildings(); loadRooms() })

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
    const res: any = await downloadAllocTemplate()
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url; a.download = '宿舍分配导入模板.xlsx'; a.click()
    URL.revokeObjectURL(url)
  } catch {}
}

async function handleImport() {
  if (!importFile.value) return
  importing.value = true
  importResult.value = null
  try {
    const res: any = await importAllocs(importFile.value)
    importResult.value = res.data
    loadAllocs()
  } catch (err: any) {
    window.$message?.(err.message || '导入失败', 'error')
  } finally {
    importing.value = false
  }
}
</script>

<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-desc">按专业+性别将宿舍房间预分配给学生，学生只能选择已分配的房间</span>
      <div class="header-actions">
        <button class="btn btn-outline" @click="openImport">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
          批量导入
        </button>
        <button class="btn btn-primary" @click="handleAdd">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          新增分配
        </button>
      </div>
    </div>
    <div class="table-card">
      <table class="data-table">
        <thead><tr><th style="width:50px">序号</th><th>学院</th><th>专业</th><th>性别</th><th>楼栋</th><th>房间号</th><th>分配床位</th><th>已选床位</th><th style="width:130px">操作</th></tr></thead>
        <tbody>
          <tr v-if="allocLoading"><td colspan="9" class="empty-cell">加载中...</td></tr>
          <tr v-else-if="paginatedData.length === 0"><td colspan="9" class="empty-cell">暂无数据</td></tr>
          <tr v-for="(a, i) in paginatedData" :key="a.id">
            <td>{{ (currentPage - 1) * pageSize + i + 1 }}</td>
            <td><span class="college-tag">{{ a.collegeName }}</span></td>
            <td class="cell-bold">{{ a.majorName }}</td>
            <td><span class="gender-tag" :class="'gender-' + a.gender">{{ genderMap[a.gender] || '未知' }}</span></td>
            <td>{{ a.buildingName }}</td>
            <td class="cell-bold">{{ a.roomNo }}</td>
            <td>{{ a.allocBedCount }}</td>
            <td><span :class="{ 'text-warn': a.usedBedCount >= a.allocBedCount }">{{ a.usedBedCount }}</span>/{{ a.allocBedCount }}</td>
            <td><div class="action-btns"><button class="btn-text" @click="handleEdit(a)">编辑</button><button class="btn-text danger" @click="handleDelete(a)">删除</button></div></td>
          </tr>
        </tbody>
      </table>
      <PagePagination :total="allocList.length" :page="currentPage" :page-size="pageSize" @update:page="currentPage = $event" />
    </div>
    <teleport to="body">
      <!-- Import Dialog -->
      <div v-if="importDialog" class="modal-mask" @click.self="importDialog = false">
        <div class="modal-box modal-wide">
          <div class="modal-header">
            <h4>批量导入宿舍分配</h4>
            <button class="modal-close" @click="importDialog = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="template-tip">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="16" x2="12" y2="12"/>
                <line x1="12" y1="8" x2="12.01" y2="8"/>
              </svg>
              <span>请先下载模板，按模板格式填写数据后上传。字段：<code>学号</code>、<code>房间号</code>（如1-1001）、<code>床位号</code></span>
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

      <div v-if="allocDialog" class="modal-mask" @click.self="allocDialog = false">
        <div class="modal-box">
          <div class="modal-header"><h4>{{ allocIsEdit ? '编辑分配' : '新增分配' }}</h4><button class="modal-close" @click="allocDialog = false">&times;</button></div>
          <div class="modal-body">
            <div class="form-row"><label class="form-label">所属专业 <span class="required">*</span></label><select v-model="allocForm.majorId" class="form-input"><option :value="undefined" disabled>请选择</option><option v-for="m in majorList" :key="m.id" :value="m.id">{{ m.collegeName }} - {{ m.majorName }}</option></select></div>
            <div class="form-row"><label class="form-label">性别 <span class="required">*</span></label><select v-model="allocForm.gender" class="form-input"><option :value="1">男</option><option :value="2">女</option></select></div>
            <div class="form-row"><label class="form-label">楼栋 <span class="required">*</span></label><select v-model="allocForm.buildingId" class="form-input"><option :value="undefined" disabled>请选择楼栋</option><option v-for="b in buildingList" :key="b.id" :value="b.id">{{ b.buildingName }}</option></select></div>
            <div class="form-row"><label class="form-label">楼层 <span class="required">*</span></label><select v-model.number="allocForm.floorNo" class="form-input" :disabled="!allocForm.buildingId"><option :value="undefined" disabled>请选择楼层</option><option v-for="f in availableFloors" :key="f" :value="f">{{ f }}F</option></select></div>
            <div class="form-row"><label class="form-label">房间 <span class="required">*</span></label><select v-model="allocForm.roomId" class="form-input" :disabled="!allocForm.buildingId || allocForm.floorNo == null"><option :value="undefined" disabled>请选择房间</option><option v-for="r in filteredRoomsByFloor" :key="r.id" :value="r.id">{{ r.roomNo }} ({{ r.bedCount }}人间)</option></select></div>
            <div class="form-row"><label class="form-label">分配床位数 <span class="required">*</span></label><input v-model.number="allocForm.allocBedCount" type="number" class="form-input" min="1" /></div>
          </div>
          <div class="modal-footer"><button class="btn btn-gray" @click="allocDialog = false">取消</button><button class="btn btn-primary" @click="handleSubmit">确定</button></div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.page-container { max-width: 1600px; margin: 0 auto; }
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; flex-wrap: wrap; gap: 10px; }
.section-desc { font-size: 13px; color: var(--gray-400); }
.header-actions { display: flex; gap: 8px; }
.btn { display: inline-flex; align-items: center; gap: 5px; padding: 8px 16px; border: none; border-radius: 8px; font-size: 13px; font-weight: 500; cursor: pointer; transition: all 0.2s; }
.btn-outline { background: #fff; color: var(--gray-600); border: 1px solid var(--gray-200); }
.btn-outline:hover { background: var(--gray-100); }
.btn { display: inline-flex; align-items: center; gap: 5px; padding: 8px 16px; border: none; border-radius: 8px; font-size: 13px; font-weight: 500; cursor: pointer; transition: all 0.2s; }
.btn-primary { background: var(--primary); color: #fff; }
.btn-primary:hover { opacity: 0.9; }
.btn-gray { background: var(--gray-100); color: var(--gray-600); border: 1px solid var(--gray-200); }
.btn-gray:hover { background: var(--gray-200); }
.table-card { background: #fff; border-radius: 12px; border: 1px solid var(--gray-200); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { text-align: left; padding: 12px 14px; font-size: 13px; font-weight: 600; color: var(--gray-400); background: var(--gray-100); border-bottom: 1px solid var(--gray-200); white-space: nowrap; }
.data-table td { padding: 11px 14px; font-size: 13px; color: var(--gray-600); border-bottom: 1px solid var(--gray-100); }
.data-table tbody tr:hover { background: rgba(26,92,255,0.02); }
.cell-bold { font-weight: 600; color: var(--gray-900); }
.empty-cell { text-align: center; color: var(--gray-400); padding: 36px 0 !important; }
.text-warn { color: var(--secondary-orange); font-weight: 600; }
.college-tag { display: inline-block; padding: 2px 8px; border-radius: 12px; font-size: 12px; background: rgba(26,92,255,0.08); color: var(--primary); }
.gender-tag { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.gender-tag.gender-1 { background: rgba(26,92,255,0.1); color: var(--primary); }
.gender-tag.gender-2 { background: rgba(255,77,106,0.1); color: var(--danger); }
.action-btns { display: flex; gap: 4px; }
.btn-text { background: none; border: none; color: var(--primary); font-size: 13px; cursor: pointer; padding: 4px 8px; border-radius: 4px; transition: background 0.15s; }
.btn-text:hover { background: rgba(26,92,255,0.06); }
.btn-text.danger { color: var(--danger); }
.btn-text.danger:hover { background: rgba(255,77,106,0.06); }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-box { background: #fff; border-radius: 14px; width: 500px; max-width: 90vw; max-height: 85vh; overflow-y: auto; box-shadow: 0 12px 40px rgba(0,0,0,0.12); }
.modal-wide { width: 600px; }
.modal-header { display: flex; align-items: center; justify-content: space-between; padding: 20px 24px 0; }
.modal-header h4 { font-size: 17px; font-weight: 600; color: var(--gray-900); margin: 0; }
.modal-close { background: none; border: none; font-size: 22px; color: var(--gray-400); cursor: pointer; padding: 0 4px; line-height: 1; }
.modal-body { padding: 20px 24px; }
.form-row { margin-bottom: 14px; }
.form-label { display: block; font-size: 13px; font-weight: 600; color: var(--gray-600); margin-bottom: 5px; }
.required { color: var(--danger); }
.form-input { width: 100%; padding: 8px 12px; border: 1.5px solid var(--gray-200); border-radius: 8px; font-size: 14px; color: var(--gray-900); outline: none; transition: border-color 0.2s; box-sizing: border-box; font-family: inherit; }
.form-input:focus { border-color: var(--primary); }
select.form-input { appearance: auto; }
.modal-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 0 24px 20px; }

/* Import */
.import-tip { background: var(--gray-100); border-radius: 8px; padding: 14px; margin-bottom: 16px; }
.import-tip p { font-size: 13px; color: var(--gray-600); margin: 0 0 8px; }
.import-tip p:last-child { margin-bottom: 12px; }
.btn-sm { padding: 6px 14px; font-size: 12px; }
.import-result { padding: 12px 14px; border-radius: 8px; margin-top: 12px; }
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
