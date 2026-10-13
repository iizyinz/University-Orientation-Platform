<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { getCounselorStudents, getCounselorClasses, getStudentDocuments, exportStudents } from '@/api/counselor'
import PagePagination from '@/components/PagePagination.vue'

const userStore = useUserStore()

interface Student {
  id: number
  studentNo: string
  name: string
  gender: number
  phone: string
  reportStatus: number
  infoCompleted: number
  className: string
  classId: number
  buildingName: string
  roomNo: string
  bedNo: number
  idCard: string
  nation: string
  province: string
  city: string
  district: string
  emergencyContactName: string
  emergencyContactPhone: string
  email: string
}

interface ClassOption {
  classId: number
  className: string
}

const searchKeyword = ref('')
const filterClassId = ref<number | null>(null)
const filterGender = ref('')
const filterReportStatus = ref('')
const classOptions = ref<ClassOption[]>([])
const studentList = ref<Student[]>([])
const loading = ref(false)

const pageSize = 10
const currentPage = ref(1)

async function loadClasses() {
  try {
    const userId = userStore.userInfo.id
    if (!userId) return
    const res: any = await getCounselorClasses(userId)
    classOptions.value = (res.data || []).map((c: any) => ({
      classId: c.classId,
      className: c.className,
    }))
  } catch {}
}

async function loadStudents() {
  loading.value = true
  try {
    const userId = userStore.userInfo.id
    if (!userId) return
    const params: any = {}
    if (filterClassId.value) params.classId = filterClassId.value
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res: any = await getCounselorStudents(userId, params)
    studentList.value = res.data || []
  } catch {
    studentList.value = []
  } finally {
    loading.value = false
  }
}

const filteredList = computed(() => {
  return studentList.value.filter(s => {
    const matchGender = !filterGender.value || s.gender === Number(filterGender.value)
    const matchStatus = !filterReportStatus.value || s.reportStatus === Number(filterReportStatus.value)
    return matchGender && matchStatus
  })
})

const filteredTotal = computed(() => filteredList.value.length)

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredList.value.slice(start, start + pageSize)
})

const getDormInfo = (s: Student) => {
  if (s.buildingName && s.roomNo) {
    return `${s.buildingName}-${s.roomNo}-${s.bedNo || '-'}床`
  }
  return '-'
}

let searchTimer: ReturnType<typeof setTimeout> | null = null
const onSearchInput = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { currentPage.value = 1; loadStudents() }, 300)
}

watch(filterClassId, () => { currentPage.value = 1; loadStudents() })
watch(filterGender, () => { currentPage.value = 1 })
watch(filterReportStatus, () => { currentPage.value = 1 })

onMounted(() => {
  loadClasses()
  loadStudents()
})

const showDetail = ref(false)
const currentStudent = ref<Student | null>(null)
const studentDocuments = ref<any[]>([])
const loadingDocs = ref(false)

const viewStudent = async (student: Student) => {
  currentStudent.value = student
  showDetail.value = true
  loadingDocs.value = true
  try {
    const res: any = await getStudentDocuments(student.id)
    studentDocuments.value = res.data || []
  } catch {
    studentDocuments.value = []
  } finally {
    loadingDocs.value = false
  }
}

const getDocByType = (type: number) => studentDocuments.value.find(d => d.docType === type)

const exporting = ref(false)
const handleExport = async () => {
  exporting.value = true
  try {
    const userId = userStore.userInfo.id
    const params: any = {}
    if (filterClassId.value) params.classId = filterClassId.value
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res: any = await exportStudents(userId, params)
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '学生信息.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    console.error(e)
  } finally {
    exporting.value = false
  }
}
</script>

<template>
  <div class="my-student">
    <div class="page-header">
      <div class="header-left">
        <h3>我的学生</h3>
        <p class="subtitle">查看和管理您负责班级的学生信息</p>
      </div>
      <button class="export-btn" @click="handleExport" :disabled="exporting">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
        </svg>
        {{ exporting ? '导出中...' : '导出Excel' }}
      </button>
    </div>

    <!-- Filters -->
    <div class="filter-bar">
      <div class="search-box">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <input v-model="searchKeyword" placeholder="搜索姓名、学号或身份证号..." @input="onSearchInput" />
      </div>
      <select v-model="filterClassId" class="filter-select">
        <option :value="null">全部班级</option>
        <option v-for="c in classOptions" :key="c.classId" :value="c.classId">{{ c.className }}</option>
      </select>
      <select v-model="filterGender" class="filter-select">
        <option value="">全部性别</option>
        <option value="1">男</option>
        <option value="2">女</option>
      </select>
      <select v-model="filterReportStatus" class="filter-select">
        <option value="">全部状态</option>
        <option value="1">已报到</option>
        <option value="0">未报到</option>
      </select>
    </div>

    <!-- Table -->
    <div class="table-card">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>#</th>
            <th>学号</th>
            <th>姓名</th>
            <th>性别</th>
            <th>联系电话</th>
            <th>宿舍信息</th>
            <th>报到状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(s, idx) in paginatedData" :key="s.id">
            <td>{{ (currentPage - 1) * pageSize + idx + 1 }}</td>
            <td>{{ s.studentNo }}</td>
            <td><span class="name-link" @click="viewStudent(s)">{{ s.name }}</span></td>
            <td>{{ s.gender === 1 ? '男' : '女' }}</td>
            <td>{{ s.phone || '-' }}</td>
            <td>{{ getDormInfo(s) }}</td>
            <td>
              <span :class="['status-tag', s.reportStatus === 1 ? 'success' : 'pending']">
                {{ s.reportStatus === 1 ? '已报到' : '未报到' }}
              </span>
            </td>
            <td>
              <button class="btn-link" @click="viewStudent(s)">详情</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="!loading && filteredList.length === 0" class="empty-state">
        <p>暂无匹配的学生数据</p>
      </div>
      <PagePagination v-if="filteredTotal > 0" :total="filteredTotal" :page="currentPage" :page-size="pageSize" @update:page="currentPage = $event" />
    </div>

    <!-- Student Detail Modal -->
    <teleport to="body">
      <div v-if="showDetail && currentStudent" class="modal-mask" @click.self="showDetail = false">
        <div class="detail-modal">
          <div class="modal-header">
            <h4>学生详情</h4>
            <button class="close-btn" @click="showDetail = false">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <!-- 证件照区域 -->
            <div v-if="studentDocuments.length > 0" class="detail-section">
              <h5>证件照</h5>
              <div class="doc-photos">
                <div v-if="getDocByType(3)" class="doc-photo">
                  <img :src="getDocByType(3).fileUrl" alt="证件照" />
                  <span class="doc-label">证件照</span>
                </div>
                <div v-if="getDocByType(1)" class="doc-photo">
                  <img :src="getDocByType(1).fileUrl" alt="身份证正面" />
                  <span class="doc-label">身份证正面</span>
                </div>
                <div v-if="getDocByType(2)" class="doc-photo">
                  <img :src="getDocByType(2).fileUrl" alt="身份证反面" />
                  <span class="doc-label">身份证反面</span>
                </div>
              </div>
            </div>
            <div class="detail-section">
              <h5>基本信息</h5>
              <div class="detail-grid">
                <div class="detail-row"><span class="label">姓名</span><span class="value">{{ currentStudent.name }}</span></div>
                <div class="detail-row"><span class="label">学号</span><span class="value">{{ currentStudent.studentNo }}</span></div>
                <div class="detail-row"><span class="label">性别</span><span class="value">{{ currentStudent.gender === 1 ? '男' : '女' }}</span></div>
                <div class="detail-row"><span class="label">民族</span><span class="value">{{ currentStudent.nation || '-' }}</span></div>
                <div class="detail-row"><span class="label">联系电话</span><span class="value">{{ currentStudent.phone || '-' }}</span></div>
                <div class="detail-row"><span class="label">邮箱</span><span class="value">{{ currentStudent.email || '-' }}</span></div>
                <div class="detail-row full"><span class="label">身份证号</span><span class="value">{{ currentStudent.idCard }}</span></div>
                <div class="detail-row full"><span class="label">生源地</span><span class="value">{{ [currentStudent.province, currentStudent.city, currentStudent.district].filter(Boolean).join(' ') || '-' }}</span></div>
              </div>
            </div>
            <div class="detail-section">
              <h5>报到信息</h5>
              <div class="detail-grid">
                <div class="detail-row"><span class="label">报到状态</span>
                  <span :class="['status-tag', currentStudent.reportStatus === 1 ? 'success' : 'pending']">
                    {{ currentStudent.reportStatus === 1 ? '已报到' : '未报到' }}
                  </span>
                </div>
                <div class="detail-row"><span class="label">宿舍</span><span class="value">{{ getDormInfo(currentStudent) }}</span></div>
                <div class="detail-row full"><span class="label">紧急联系人</span><span class="value">{{ currentStudent.emergencyContactName || '-' }} {{ currentStudent.emergencyContactPhone ? '(' + currentStudent.emergencyContactPhone + ')' : '' }}</span></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.my-student { max-width: 1600px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-left { display: flex; flex-direction: column; }
.page-header h3 { font-size: 20px; font-weight: 600; color: var(--gray-900); margin: 0 0 4px; }
.subtitle { font-size: 14px; color: var(--gray-400); margin: 0; }

.export-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px; background: var(--primary); color: #fff;
  border: none; border-radius: 8px; font-size: 14px; cursor: pointer;
}
.export-btn:hover:not(:disabled) { background: var(--primary-deep); }
.export-btn:disabled { opacity: 0.6; cursor: not-allowed; }

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
  text-align: left; padding: 12px 16px; font-size: 12px; font-weight: 600;
  color: var(--gray-400); text-transform: uppercase; background: var(--gray-100);
  border-bottom: 1px solid var(--gray-200);
}
.data-table td { padding: 14px 16px; font-size: 14px; color: var(--gray-600); border-bottom: 1px solid var(--gray-100); }
.data-table tr:last-child td { border-bottom: none; }
.data-table tr:hover td { background: var(--gray-100); }

.name-link { color: var(--primary); cursor: pointer; font-weight: 500; }
.name-link:hover { text-decoration: underline; }

.status-tag {
  display: inline-block; padding: 2px 10px; border-radius: 10px;
  font-size: 12px; font-weight: 500;
}
.status-tag.success { background: rgba(0,201,167,0.1); color: var(--success); }
.status-tag.pending { background: rgba(255,170,76,0.1); color: var(--secondary-orange); }

.btn-link { background: none; border: none; color: var(--primary); cursor: pointer; font-size: 13px; font-weight: 500; }
.btn-link:hover { text-decoration: underline; }

.empty-state { padding: 40px; text-align: center; color: var(--gray-400); font-size: 14px; }

/* Modal */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.detail-modal { background: #fff; border-radius: 14px; width: 560px; max-width: 90vw; max-height: 80vh; overflow-y: auto; box-shadow: 0 12px 40px rgba(0,0,0,0.12); }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px; border-bottom: 1px solid var(--gray-200); position: sticky; top: 0; background: #fff; z-index: 1; }
.modal-header h4 { font-size: 16px; font-weight: 600; color: var(--gray-900); margin: 0; }
.close-btn { background: none; border: none; color: var(--gray-400); cursor: pointer; padding: 4px; border-radius: 6px; }
.close-btn:hover { background: var(--gray-100); }
.modal-body { padding: 20px 24px; }

.detail-section { margin-bottom: 20px; }
.detail-section:last-child { margin-bottom: 0; }
.detail-section h5 { font-size: 13px; font-weight: 600; color: var(--gray-400); text-transform: uppercase; margin: 0 0 12px; letter-spacing: 0.5px; }

.doc-photos { display: flex; gap: 16px; flex-wrap: wrap; }
.doc-photo { text-align: center; }
.doc-photo img { width: 100px; height: 130px; object-fit: cover; border-radius: 8px; border: 1px solid var(--gray-200); }
.doc-label { display: block; font-size: 12px; color: var(--gray-400); margin-top: 6px; }

.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0; }
.detail-row { display: flex; padding: 8px 0; border-bottom: 1px solid var(--gray-100); }
.detail-row.full { grid-column: 1 / -1; }
.detail-row .label { width: 85px; flex-shrink: 0; font-size: 13px; color: var(--gray-400); }
.detail-row .value { font-size: 14px; color: var(--gray-600); font-weight: 500; }
</style>
