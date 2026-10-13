<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDormConfigList, createDormConfig, updateDormConfig, deleteDormConfig } from '@/api/dorm'

interface TimeConfig { id: number; configName: string; startTime: string; endTime: string; status: number; description: string }
const list = ref<TimeConfig[]>([])
const loading = ref(false)
const dialog = ref(false)
const isEdit = ref(false)
const form = ref<any>({})
const statusMap: Record<number, { label: string; cls: string }> = { 0: { label: '未开启', cls: 'inactive' }, 1: { label: '进行中', cls: 'active' }, 2: { label: '已结束', cls: 'done' } }

async function loadData() { loading.value = true; try { const r: any = await getDormConfigList(); list.value = r.data } finally { loading.value = false } }
function handleAdd() { isEdit.value = false; form.value = { configName: '', startTime: '', endTime: '', status: 0, description: '' }; dialog.value = true }
function handleEdit(row: TimeConfig) { isEdit.value = true; form.value = { ...row, startTime: row.startTime?.replace(' ', 'T').substring(0, 16), endTime: row.endTime?.replace(' ', 'T').substring(0, 16) }; dialog.value = true }
async function handleSubmit() {
  if (!form.value.configName?.trim() || !form.value.startTime || !form.value.endTime) return
  const payload = { ...form.value, startTime: form.value.startTime.replace('T', ' ') + ':00', endTime: form.value.endTime.replace('T', ' ') + ':00' }
  try { isEdit.value ? await updateDormConfig(payload) : await createDormConfig(payload); dialog.value = false; loadData() } catch (e: any) { window.$message?.(e.message || '操作失败', 'error') }
}
async function handleDelete(row: TimeConfig) {
  if (!(await window.$confirm?.(`确定删除配置「${row.configName}」吗？`))) return
  try { await deleteDormConfig(row.id); loadData() } catch (e: any) { window.$message?.(e.message || '删除失败', 'error') }
}
onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-desc">配置学生在线选房的时间窗口，未开启时学生仅可查看不可选择</span>
      <button class="btn btn-primary" @click="handleAdd">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新增配置
      </button>
    </div>
    <div class="table-card">
      <table class="data-table">
        <thead><tr><th style="width:50px">序号</th><th>配置名称</th><th>开始时间</th><th>结束时间</th><th>状态</th><th>描述</th><th style="width:130px">操作</th></tr></thead>
        <tbody>
          <tr v-if="loading"><td colspan="7" class="empty-cell">加载中...</td></tr>
          <tr v-else-if="list.length === 0"><td colspan="7" class="empty-cell">暂无数据</td></tr>
          <tr v-for="(t, i) in list" :key="t.id">
            <td>{{ i + 1 }}</td>
            <td class="cell-bold">{{ t.configName }}</td>
            <td>{{ t.startTime }}</td>
            <td>{{ t.endTime }}</td>
            <td><span class="status-tag" :class="statusMap[t.status]?.cls">{{ statusMap[t.status]?.label }}</span></td>
            <td class="cell-muted">{{ t.description || '-' }}</td>
            <td><div class="action-btns"><button class="btn-text" @click="handleEdit(t)">编辑</button><button class="btn-text danger" @click="handleDelete(t)">删除</button></div></td>
          </tr>
        </tbody>
      </table>
    </div>
    <teleport to="body">
      <div v-if="dialog" class="modal-mask" @click.self="dialog = false">
        <div class="modal-box">
          <div class="modal-header"><h4>{{ isEdit ? '编辑配置' : '新增配置' }}</h4><button class="modal-close" @click="dialog = false">&times;</button></div>
          <div class="modal-body">
            <div class="form-row"><label class="form-label">配置名称 <span class="required">*</span></label><input v-model="form.configName" class="form-input" placeholder="如：2026级新生选房" /></div>
            <div class="form-row-inline">
              <div class="form-row half"><label class="form-label">开始时间 <span class="required">*</span></label><input v-model="form.startTime" type="datetime-local" class="form-input" /></div>
              <div class="form-row half"><label class="form-label">结束时间 <span class="required">*</span></label><input v-model="form.endTime" type="datetime-local" class="form-input" /></div>
            </div>
            <div class="form-row"><label class="form-label">状态</label><select v-model="form.status" class="form-input"><option :value="0">未开启</option><option :value="1">进行中</option><option :value="2">已结束</option></select></div>
            <div class="form-row"><label class="form-label">描述</label><textarea v-model="form.description" class="form-input" rows="2" placeholder="选填"></textarea></div>
          </div>
          <div class="modal-footer"><button class="btn btn-gray" @click="dialog = false">取消</button><button class="btn btn-primary" @click="handleSubmit">确定</button></div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.page-container { max-width: 1600px; margin: 0 auto; }
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; flex-wrap: wrap; gap: 10px; }
.section-desc { font-size: 13px; color: var(--gray-400); }
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
.cell-muted { color: var(--gray-400); }
.empty-cell { text-align: center; color: var(--gray-400); padding: 36px 0 !important; }
.status-tag { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.status-tag.active { background: rgba(0,201,167,0.1); color: var(--success); }
.status-tag.inactive { background: rgba(140,149,166,0.1); color: var(--gray-400); }
.status-tag.done { background: rgba(140,149,166,0.1); color: var(--gray-400); }
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
.form-row.half { flex: 1; }
.form-row-inline { display: flex; gap: 14px; }
.form-label { display: block; font-size: 13px; font-weight: 600; color: var(--gray-600); margin-bottom: 5px; }
.required { color: var(--danger); }
.form-input { width: 100%; padding: 8px 12px; border: 1.5px solid var(--gray-200); border-radius: 8px; font-size: 14px; color: var(--gray-900); outline: none; transition: border-color 0.2s; box-sizing: border-box; font-family: inherit; }
.form-input:focus { border-color: var(--primary); }
select.form-input { appearance: auto; }
.modal-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 0 24px 20px; }
</style>
