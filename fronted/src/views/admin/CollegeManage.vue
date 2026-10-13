<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getCollegeList, createCollege, updateCollege, deleteCollege } from '@/api/org'
import PagePagination from '@/components/PagePagination.vue'

interface College {
  id: number
  collegeName: string
  collegeCode: string
  description: string
  sortOrder: number
  status: number
}

const tableData = ref<College[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增学院')
const isEdit = ref(false)

const formData = ref<Partial<College>>({})
const formRef = ref<HTMLFormElement>()

const rules = {
  collegeName: [{ required: true, message: '请输入学院名称', trigger: 'blur' }],
  collegeCode: [{ required: true, message: '请输入学院编码', trigger: 'blur' }],
}

const pageSize = 10
const currentPage = ref(1)
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return tableData.value.slice(start, start + pageSize)
})

async function loadData() {
  loading.value = true
  currentPage.value = 1
  try {
    const res: any = await getCollegeList()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增学院'
  formData.value = { collegeName: '', collegeCode: '', description: '', sortOrder: 0, status: 1 }
  dialogVisible.value = true
}

function handleEdit(row: College) {
  isEdit.value = true
  dialogTitle.value = '编辑学院'
  formData.value = { ...row }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formData.value.collegeName?.trim()) return
  if (!formData.value.collegeCode?.trim()) return

  try {
    if (isEdit.value) {
      await updateCollege(formData.value)
    } else {
      await createCollege(formData.value)
    }
    dialogVisible.value = false
    loadData()
  } catch (err: any) {
    window.$message?.(err.message || '操作失败', 'error')
  }
}

async function handleDelete(row: College) {
  if (!(await window.$confirm?.(`确定删除学院「${row.collegeName}」吗？`))) return
  try {
    await deleteCollege(row.id)
    loadData()
  } catch (err: any) {
    window.$message?.(err.message || '删除失败', 'error')
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h3>学院管理</h3>
      <button class="btn btn-primary" @click="handleAdd">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新增学院
      </button>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th style="width:60px">序号</th>
            <th>学院名称</th>
            <th>学院编码</th>
            <th>描述</th>
            <th style="width:80px">排序</th>
            <th style="width:80px">状态</th>
            <th style="width:160px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="empty-cell">加载中...</td>
          </tr>
          <tr v-else-if="tableData.length === 0">
            <td colspan="7" class="empty-cell">暂无数据</td>
          </tr>
          <tr v-for="(row, idx) in paginatedData" :key="row.id">
            <td>{{ (currentPage - 1) * pageSize + idx + 1 }}</td>
            <td class="cell-bold">{{ row.collegeName }}</td>
            <td><code class="code-tag">{{ row.collegeCode }}</code></td>
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

    <!-- Dialog -->
    <teleport to="body">
      <div v-if="dialogVisible" class="modal-mask" @click.self="dialogVisible = false">
        <div class="modal-box">
          <div class="modal-header">
            <h4>{{ dialogTitle }}</h4>
            <button class="modal-close" @click="dialogVisible = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-row">
              <label class="form-label">学院名称 <span class="required">*</span></label>
              <input v-model="formData.collegeName" class="form-input" placeholder="如：计算机学院" />
            </div>
            <div class="form-row">
              <label class="form-label">学院编码 <span class="required">*</span></label>
              <input v-model="formData.collegeCode" class="form-input" placeholder="如：CS" />
            </div>
            <div class="form-row">
              <label class="form-label">描述</label>
              <textarea v-model="formData.description" class="form-input" rows="3" placeholder="学院描述（选填）"></textarea>
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
  max-width: 260px;
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
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

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
</style>
