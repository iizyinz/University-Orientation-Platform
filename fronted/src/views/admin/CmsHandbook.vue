<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getHandbookList, createHandbook, updateHandbook, deleteHandbook } from '@/api/cms'
import ImageUpload from '@/components/ImageUpload.vue'
import ShadowContent from '@/components/ShadowContent.vue'

interface Handbook { id: number; title: string; content: string; coverImage: string; sortOrder: number; publishStatus: number; viewCount: number }

const list = ref<Handbook[]>([])
const loading = ref(false)
const dialog = ref(false)
const isEdit = ref(false)
const form = ref<any>({})
const previewVisible = ref(false)
const previewItem = ref<any>(null)

async function loadData() { loading.value = true; try { const r: any = await getHandbookList(); list.value = r.data } finally { loading.value = false } }
function handleAdd() { isEdit.value = false; form.value = { title: '', content: '', coverImage: '', sortOrder: 0, publishStatus: 0 }; dialog.value = true }
function handleEdit(row: Handbook) { isEdit.value = true; form.value = { ...row }; dialog.value = true }
async function handleSubmit() {
  if (!form.value.title?.trim()) return
  try { isEdit.value ? await updateHandbook(form.value) : await createHandbook(form.value); dialog.value = false; loadData() } catch (e: any) { window.$message?.(e.message || '操作失败', 'error') }
}
async function handleDelete(row: Handbook) {
  if (!(await window.$confirm?.(`确定删除手册「${row.title}」吗？`))) return
  try { await deleteHandbook(row.id); loadData() } catch (e: any) { window.$message?.(e.message || '删除失败', 'error') }
}
function handlePreview(row: Handbook) { previewItem.value = row; previewVisible.value = true }
onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-desc">管理新生入学手册内容，支持富文本编辑，发布后学生端可在线阅读</span>
      <button class="btn btn-primary" @click="handleAdd">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新增手册
      </button>
    </div>
    <div class="table-card">
      <table class="data-table">
        <thead><tr><th style="width:50px">序号</th><th>标题</th><th>封面</th><th>排序</th><th>状态</th><th>浏览量</th><th style="width:180px">操作</th></tr></thead>
        <tbody>
          <tr v-if="loading"><td colspan="7" class="empty-cell">加载中...</td></tr>
          <tr v-else-if="list.length === 0"><td colspan="7" class="empty-cell">暂无数据</td></tr>
          <tr v-for="(row, i) in list" :key="row.id">
            <td>{{ i + 1 }}</td>
            <td class="cell-bold">{{ row.title }}</td>
            <td><img v-if="row.coverImage" :src="row.coverImage" class="cover-thumb" /><span v-else class="cell-muted">无封面</span></td>
            <td>{{ row.sortOrder }}</td>
            <td><span class="status-tag" :class="row.publishStatus === 1 ? 'active' : 'inactive'">{{ row.publishStatus === 1 ? '已发布' : '草稿' }}</span></td>
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
          <div class="modal-header"><h4>{{ isEdit ? '编辑手册' : '新增手册' }}</h4><button class="modal-close" @click="dialog = false">&times;</button></div>
          <div class="modal-body">
            <div class="form-row"><label class="form-label">手册标题 <span class="required">*</span></label><input v-model="form.title" class="form-input" placeholder="如：2026级新生入学指南" /></div>
            <div class="form-row"><label class="form-label">封面图片</label><ImageUpload v-model="form.coverImage" dir="handbook" /></div>
            <div class="form-row"><label class="form-label">手册内容</label><textarea v-model="form.content" class="form-input content-editor" rows="10" placeholder="支持富文本内容（HTML格式）"></textarea></div>
            <div class="form-row-inline">
              <div class="form-row half"><label class="form-label">排序</label><input v-model.number="form.sortOrder" type="number" class="form-input" min="0" /></div>
              <div class="form-row half"><label class="form-label">发布状态</label><select v-model="form.publishStatus" class="form-input"><option :value="0">草稿</option><option :value="1">已发布</option></select></div>
            </div>
          </div>
          <div class="modal-footer"><button class="btn btn-gray" @click="dialog = false">取消</button><button class="btn btn-primary" @click="handleSubmit">确定</button></div>
        </div>
      </div>
      <div v-if="previewVisible" class="modal-mask" @click.self="previewVisible = false">
        <div class="modal-box modal-wide">
          <div class="modal-header"><h4>{{ previewItem?.title }}</h4><button class="modal-close" @click="previewVisible = false">&times;</button></div>
          <div class="modal-body"><ShadowContent v-if="previewItem?.content" :content="previewItem.content" /></div>
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
.cover-thumb { width: 48px; height: 32px; object-fit: cover; border-radius: 4px; border: 1px solid var(--gray-200); }
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
.preview-content { line-height: 1.8; font-size: 15px; color: var(--gray-600); }
</style>
