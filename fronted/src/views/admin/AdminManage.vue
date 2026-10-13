<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getAdminList, createAdmin, updateAdmin, resetAdminPassword, deleteAdmin } from '@/api/people'
import PagePagination from '@/components/PagePagination.vue'

interface Admin {
  id: number
  username: string
  realName: string
  phone: string
  email: string
  status: number
  lastLoginTime: string
  createTime: string
}

const tableData = ref<Admin[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formData = ref<any>({})

const pageSize = 10
const currentPage = ref(1)
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  let data = tableData.value
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    data = data.filter(a => a.realName?.toLowerCase().includes(kw) || a.username?.toLowerCase().includes(kw) || a.phone?.includes(kw))
  }
  return data.slice(start, start + pageSize)
})

const filteredTotal = computed(() => {
  if (!searchKeyword.value) return tableData.value.length
  const kw = searchKeyword.value.toLowerCase()
  return tableData.value.filter(a => a.realName?.toLowerCase().includes(kw) || a.username?.toLowerCase().includes(kw) || a.phone?.includes(kw)).length
})

async function loadData() {
  loading.value = true
  currentPage.value = 1
  try {
    const res: any = await getAdminList()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  formData.value = { username: '', realName: '', phone: '', email: '', password: '123456' }
  dialogVisible.value = true
}

function handleEdit(row: Admin) {
  isEdit.value = true
  formData.value = { id: row.id, username: row.username, realName: row.realName, phone: row.phone || '', email: row.email || '' }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formData.value.username?.trim()) return window.$message?.('请输入用户名', 'error')
  if (!formData.value.realName?.trim()) return window.$message?.('请输入姓名', 'error')

  try {
    if (isEdit.value) {
      await updateAdmin(formData.value)
    } else {
      await createAdmin(formData.value)
    }
    dialogVisible.value = false
    loadData()
  } catch (err: any) {
    window.$message?.(err.message || '操作失败', 'error')
  }
}

async function handleResetPassword(row: Admin) {
  if (!(await window.$confirm?.(`确定重置「${row.realName}」的密码为默认密码 123456 吗？`))) return
  try {
    await resetAdminPassword({ id: row.id })
    window.$message?.('密码已重置为 123456', 'success')
  } catch (err: any) {
    window.$message?.(err.message || '重置失败', 'error')
  }
}

async function handleDelete(row: Admin) {
  if (!(await window.$confirm?.(`确定删除管理员「${row.realName}」吗？`))) return
  try {
    await deleteAdmin(row.id)
    loadData()
  } catch (err: any) {
    window.$message?.(err.message || '删除失败', 'error')
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <div class="section-header">
      <div class="header-left">
        <span class="section-desc">管理系统管理员账号，初始密码为 123456</span>
        <div class="search-box">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <input v-model="searchKeyword" placeholder="搜索姓名/用户名/电话..." @input="currentPage = 1" />
        </div>
      </div>
      <button class="btn btn-primary" @click="handleAdd">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新增管理员
      </button>
    </div>
    <div class="table-card">
      <table class="data-table">
        <thead><tr><th style="width:50px">序号</th><th>用户名</th><th>姓名</th><th>联系电话</th><th>邮箱</th><th>最近登录</th><th>创建时间</th><th style="width:200px">操作</th></tr></thead>
        <tbody>
          <tr v-if="loading"><td colspan="8" class="empty-cell">加载中...</td></tr>
          <tr v-else-if="paginatedData.length === 0"><td colspan="8" class="empty-cell">暂无数据</td></tr>
          <tr v-for="(row, i) in paginatedData" :key="row.id">
            <td>{{ (currentPage - 1) * pageSize + i + 1 }}</td>
            <td><code class="code-tag">{{ row.username }}</code></td>
            <td class="cell-bold">{{ row.realName }}</td>
            <td>{{ row.phone || '-' }}</td>
            <td class="cell-muted">{{ row.email || '-' }}</td>
            <td>{{ row.lastLoginTime ? row.lastLoginTime.substring(0, 16).replace('T', ' ') : '未登录' }}</td>
            <td>{{ row.createTime ? row.createTime.substring(0, 10) : '-' }}</td>
            <td>
              <div class="action-btns">
                <button class="btn-text" @click="handleEdit(row)">编辑</button>
                <button class="btn-text" @click="handleResetPassword(row)">重置密码</button>
                <button class="btn-text danger" @click="handleDelete(row)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <PagePagination :total="filteredTotal" :page="currentPage" :page-size="pageSize" @update:page="currentPage = $event" />
    </div>

    <!-- Dialog -->
    <teleport to="body">
      <div v-if="dialogVisible" class="modal-mask" @click.self="dialogVisible = false">
        <div class="modal-box">
          <div class="modal-header">
            <h4>{{ isEdit ? '编辑管理员' : '新增管理员' }}</h4>
            <button class="modal-close" @click="dialogVisible = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-row">
              <label class="form-label">用户名 <span class="required">*</span></label>
              <input v-model="formData.username" class="form-input" placeholder="登录账号" :disabled="isEdit" />
            </div>
            <div class="form-row">
              <label class="form-label">姓名 <span class="required">*</span></label>
              <input v-model="formData.realName" class="form-input" placeholder="真实姓名" />
            </div>
            <div class="form-row" v-if="!isEdit">
              <label class="form-label">初始密码</label>
              <input v-model="formData.password" class="form-input" placeholder="默认123456" />
            </div>
            <div class="form-row-inline">
              <div class="form-row half">
                <label class="form-label">联系电话</label>
                <input v-model="formData.phone" class="form-input" placeholder="选填" />
              </div>
              <div class="form-row half">
                <label class="form-label">邮箱</label>
                <input v-model="formData.email" class="form-input" placeholder="选填" />
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
.page-container { max-width: 1600px; margin: 0 auto; }
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; flex-wrap: wrap; gap: 10px; }
.header-left { display: flex; align-items: center; gap: 12px; }
.section-desc { font-size: 13px; color: var(--gray-400); }
.search-box { display: flex; align-items: center; gap: 6px; padding: 0 10px; border: 1.5px solid var(--gray-200); border-radius: 8px; background: #fff; }
.search-box svg { color: var(--gray-400); flex-shrink: 0; }
.search-box input { border: none; outline: none; padding: 8px 0; font-size: 13px; background: transparent; width: 200px; }
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
.cell-muted { color: var(--gray-400); max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.empty-cell { text-align: center; color: var(--gray-400); padding: 36px 0 !important; }
.code-tag { background: var(--gray-100); padding: 3px 8px; border-radius: 4px; font-size: 13px; color: var(--primary); }
.action-btns { display: flex; gap: 4px; }
.btn-text { background: none; border: none; color: var(--primary); font-size: 13px; cursor: pointer; padding: 4px 8px; border-radius: 4px; transition: background 0.15s; }
.btn-text:hover { background: rgba(26,92,255,0.06); }
.btn-text.danger { color: var(--danger); }
.btn-text.danger:hover { background: rgba(255,77,106,0.06); }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-box { background: #fff; border-radius: 14px; width: 500px; max-width: 90vw; box-shadow: 0 12px 40px rgba(0,0,0,0.12); }
.modal-header { display: flex; align-items: center; justify-content: space-between; padding: 20px 24px 0; }
.modal-header h4 { font-size: 17px; font-weight: 600; color: var(--gray-900); margin: 0; }
.modal-close { background: none; border: none; font-size: 22px; color: var(--gray-400); cursor: pointer; padding: 0 4px; line-height: 1; }
.modal-body { padding: 20px 24px; }
.form-row { margin-bottom: 14px; }
.form-label { display: block; font-size: 13px; font-weight: 600; color: var(--gray-600); margin-bottom: 5px; }
.required { color: var(--danger); }
.form-input { width: 100%; padding: 8px 12px; border: 1.5px solid var(--gray-200); border-radius: 8px; font-size: 14px; color: var(--gray-900); outline: none; transition: border-color 0.2s; box-sizing: border-box; font-family: inherit; }
.form-input:focus { border-color: var(--primary); }
.form-input:disabled { background: var(--gray-100); color: var(--gray-400); }
.form-row-inline { display: flex; gap: 16px; }
.form-row.half { flex: 1; }
.modal-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 0 24px 20px; }
</style>
