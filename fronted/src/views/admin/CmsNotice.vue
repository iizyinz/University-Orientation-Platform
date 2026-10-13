<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getNoticeList, createNotice, updateNotice, deleteNotice } from '@/api/cms'

interface Notice { id: number; title: string; content: string; noticeType: number; publishStatus: number; publishTime: string; viewCount: number }

const list = ref<Notice[]>([])
const loading = ref(false)
const dialog = ref(false)
const isEdit = ref(false)
const form = ref<any>({})
const previewVisible = ref(false)
const previewItem = ref<any>(null)

const typeMap: Record<number, { label: string; cls: string }> = { 1: { label: '通知', cls: 'info' }, 2: { label: '公告', cls: 'warn' }, 3: { label: '紧急通知', cls: 'urgent' } }

async function loadData() { loading.value = true; try { const r: any = await getNoticeList(); list.value = r.data } finally { loading.value = false } }
function handleAdd() { isEdit.value = false; form.value = { title: '', content: '', noticeType: 1, publishStatus: 0, publishTime: '' }; dialog.value = true }
function handleEdit(row: Notice) { isEdit.value = true; form.value = { ...row, publishTime: row.publishTime?.replace(' ', 'T').substring(0, 16) || '' }; dialog.value = true }
async function handleSubmit() {
  if (!form.value.title?.trim()) return
  const payload = { ...form.value, publishTime: form.value.publishTime ? form.value.publishTime.replace('T', ' ') + ':00' : null }
  try { isEdit.value ? await updateNotice(payload) : await createNotice(payload); dialog.value = false; loadData() } catch (e: any) { window.$message?.(e.message || '操作失败', 'error') }
}
async function handleDelete(row: Notice) {
  if (!(await window.$confirm?.(`确定删除公告「${row.title}」吗？`))) return
  try { await deleteNotice(row.id); loadData() } catch (e: any) { window.$message?.(e.message || '删除失败', 'error') }
}
function handlePreview(row: Notice) { previewItem.value = row; previewVisible.value = true }
onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-desc">发布和管理各类通知公告，支持通知、公告、紧急通知三种类型</span>
      <button class="btn btn-primary" @click="handleAdd">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新增公告
      </button>
    </div>
    <div class="table-card">
      <table class="data-table">
        <thead><tr><th style="width:50px">序号</th><th>标题</th><th>类型</th><th>状态</th><th>发布时间</th><th>浏览量</th><th style="width:180px">操作</th></tr></thead>
        <tbody>
          <tr v-if="loading"><td colspan="7" class="empty-cell">加载中...</td></tr>
          <tr v-else-if="list.length === 0"><td colspan="7" class="empty-cell">暂无数据</td></tr>
          <tr v-for="(row, i) in list" :key="row.id">
            <td>{{ i + 1 }}</td>
            <td class="cell-bold">{{ row.title }}</td>
            <td><span class="type-tag" :class="typeMap[row.noticeType]?.cls">{{ typeMap[row.noticeType]?.label }}</span></td>
            <td><span class="status-tag" :class="row.publishStatus === 1 ? 'active' : 'inactive'">{{ row.publishStatus === 1 ? '已发布' : '草稿' }}</span></td>
            <td class="cell-muted">{{ row.publishTime || '-' }}</td>
            <td>{{ row.viewCount }}</td>
            <td><div class="action-btns">
              <button class="btn-text" @click="handlePreview(row)">预览</button>
              <button class="btn-text" @click="handleEdit(row)">编辑</button>
              <button class="btn-text danger" @click="handleDelete(row)">删除</button>
            </div></td>
          </tr>
        </tbody>
      </table>
    </div>

    <teleport to="body">
      <div v-if="dialog" class="modal-mask" @click.self="dialog = false">
        <div class="modal-box modal-wide">
          <div class="modal-header"><h4>{{ isEdit ? '编辑公告' : '新增公告' }}</h4><button class="modal-close" @click="dialog = false">&times;</button></div>
          <div class="modal-body">
            <div class="form-row"><label class="form-label">公告标题 <span class="required">*</span></label><input v-model="form.title" class="form-input" placeholder="公告标题" /></div>
            <div class="form-row-inline">
              <div class="form-row half"><label class="form-label">公告类型</label><select v-model="form.noticeType" class="form-input"><option :value="1">通知</option><option :value="2">公告</option><option :value="3">紧急通知</option></select></div>
              <div class="form-row half"><label class="form-label">发布状态</label><select v-model="form.publishStatus" class="form-input"><option :value="0">草稿</option><option :value="1">已发布</option></select></div>
            </div>
            <div class="form-row"><label class="form-label">发布时间</label><input v-model="form.publishTime" type="datetime-local" class="form-input" /></div>
            <div class="form-row"><label class="form-label">公告内容</label><textarea v-model="form.content" class="form-input content-editor" rows="8" placeholder="支持富文本内容（HTML格式）"></textarea></div>
          </div>
          <div class="modal-footer"><button class="btn btn-gray" @click="dialog = false">取消</button><button class="btn btn-primary" @click="handleSubmit">确定</button></div>
        </div>
      </div>
      <div v-if="previewVisible" class="modal-mask" @click.self="previewVisible = false">
        <div class="modal-box modal-wide">
          <div class="modal-header"><h4>{{ previewItem?.title }}</h4><button class="modal-close" @click="previewVisible = false">&times;</button></div>
          <div class="modal-body">
            <div class="preview-meta">
              <span class="type-tag" :class="typeMap[previewItem?.noticeType]?.cls">{{ typeMap[previewItem?.noticeType]?.label }}</span>
              <span class="cell-muted">{{ previewItem?.publishTime }}</span>
            </div>
            <div class="preview-content" v-html="previewItem?.content"></div>
          </div>
          <div class="modal-footer"><button class="btn btn-gray" @click="previewVisible = false">关闭</button></div>
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
.type-tag { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.type-tag.info { background: rgba(77,138,255,0.1); color: var(--info); }
.type-tag.warn { background: rgba(255,170,76,0.1); color: var(--secondary-orange); }
.type-tag.urgent { background: rgba(255,77,106,0.1); color: var(--danger); }
.status-tag { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.status-tag.active { background: rgba(0,201,167,0.1); color: var(--success); }
.status-tag.inactive { background: rgba(140,149,166,0.1); color: var(--gray-400); }
.action-btns { display: flex; gap: 4px; }
.btn-text { background: none; border: none; color: var(--primary); font-size: 13px; cursor: pointer; padding: 4px 8px; border-radius: 4px; transition: background 0.15s; }
.btn-text:hover { background: rgba(26,92,255,0.06); }
.btn-text.danger { color: var(--danger); }
.btn-text.danger:hover { background: rgba(255,77,106,0.06); }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-box { background: #fff; border-radius: 14px; width: 500px; max-width: 90vw; max-height: 85vh; overflow-y: auto; box-shadow: 0 12px 40px rgba(0,0,0,0.12); }
.modal-box.modal-wide { width: 640px; }
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
.content-editor { font-family: 'Courier New', monospace; font-size: 13px; resize: vertical; }
.modal-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 0 24px 20px; }
.preview-meta { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.preview-content { line-height: 1.8; font-size: 15px; color: var(--gray-600); }
</style>
