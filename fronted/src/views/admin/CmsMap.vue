<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMapList, createMap, updateMap, deleteMap } from '@/api/cms'
import ImageUpload from '@/components/ImageUpload.vue'

interface CampusMap { id: number; mapName: string; mapImage: string; description: string; sortOrder: number; status: number }

const list = ref<CampusMap[]>([])
const loading = ref(false)
const dialog = ref(false)
const isEdit = ref(false)
const form = ref<any>({})

async function loadData() { loading.value = true; try { const r: any = await getMapList(); list.value = r.data } finally { loading.value = false } }
function handleAdd() { isEdit.value = false; form.value = { mapName: '', mapImage: '', description: '', sortOrder: 0, status: 1 }; dialog.value = true }
function handleEdit(row: CampusMap) { isEdit.value = true; form.value = { ...row }; dialog.value = true }
async function handleSubmit() {
  if (!form.value.mapName?.trim() || !form.value.mapImage?.trim()) return
  try { isEdit.value ? await updateMap(form.value) : await createMap(form.value); dialog.value = false; loadData() } catch (e: any) { window.$message?.(e.message || '操作失败', 'error') }
}
async function handleDelete(row: CampusMap) {
  if (!(await window.$confirm?.(`确定删除地图「${row.mapName}」吗？`))) return
  try { await deleteMap(row.id); loadData() } catch (e: any) { window.$message?.(e.message || '删除失败', 'error') }
}
onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-desc">管理校园地图资源，支持多张地图上传，学生端可查看和缩放</span>
      <button class="btn btn-primary" @click="handleAdd">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新增地图
      </button>
    </div>
    <div class="map-grid" v-if="list.length || loading">
      <div v-if="loading" class="loading-text">加载中...</div>
      <div v-for="row in list" :key="row.id" class="map-card">
        <div class="map-img-wrapper">
          <img :src="row.mapImage" class="map-img" />
          <div class="map-overlay">
            <button class="btn-icon" @click="handleEdit(row)"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button>
            <button class="btn-icon danger" @click="handleDelete(row)"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg></button>
          </div>
        </div>
        <div class="map-info">
          <span class="map-name">{{ row.mapName }}</span>
          <span class="status-tag" :class="row.status === 1 ? 'active' : 'inactive'">{{ row.status === 1 ? '启用' : '禁用' }}</span>
        </div>
        <p class="map-desc" v-if="row.description">{{ row.description }}</p>
      </div>
    </div>
    <div v-else class="empty-text">暂无地图数据</div>

    <teleport to="body">
      <div v-if="dialog" class="modal-mask" @click.self="dialog = false">
        <div class="modal-box">
          <div class="modal-header"><h4>{{ isEdit ? '编辑地图' : '新增地图' }}</h4><button class="modal-close" @click="dialog = false">&times;</button></div>
          <div class="modal-body">
            <div class="form-row"><label class="form-label">地图名称 <span class="required">*</span></label><input v-model="form.mapName" class="form-input" placeholder="如：校园总平面图" /></div>
            <div class="form-row"><label class="form-label">地图图片 <span class="required">*</span></label><ImageUpload v-model="form.mapImage" dir="campus-map" /></div>
            <div class="form-row"><label class="form-label">描述</label><textarea v-model="form.description" class="form-input" rows="2" placeholder="选填"></textarea></div>
            <div class="form-row-inline">
              <div class="form-row half"><label class="form-label">排序</label><input v-model.number="form.sortOrder" type="number" class="form-input" min="0" /></div>
              <div class="form-row half"><label class="form-label">状态</label><select v-model="form.status" class="form-input"><option :value="1">启用</option><option :value="0">禁用</option></select></div>
            </div>
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
.loading-text, .empty-text { text-align: center; color: var(--gray-400); padding: 30px; font-size: 13px; }

.map-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.map-card { background: #fff; border: 1px solid var(--gray-200); border-radius: 10px; overflow: hidden; transition: box-shadow 0.2s; }
.map-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.06); }
.map-img-wrapper { position: relative; height: 180px; background: var(--gray-100); overflow: hidden; }
.map-img { width: 100%; height: 100%; object-fit: cover; }
.map-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; gap: 10px; opacity: 0; transition: opacity 0.2s; }
.map-card:hover .map-overlay { opacity: 1; }
.btn-icon { width: 36px; height: 36px; border-radius: 50%; border: none; background: rgba(255,255,255,0.2); backdrop-filter: blur(4px); cursor: pointer; display: flex; align-items: center; justify-content: center; transition: background 0.15s; }
.btn-icon:hover { background: rgba(255,255,255,0.35); }
.btn-icon.danger:hover { background: rgba(255,77,106,0.6); }
.map-info { padding: 12px 14px 4px; display: flex; align-items: center; justify-content: space-between; }
.map-name { font-size: 15px; font-weight: 600; color: var(--gray-900); }
.map-desc { padding: 0 14px 12px; font-size: 13px; color: var(--gray-400); margin: 4px 0 0; }
.status-tag { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.status-tag.active { background: rgba(0,201,167,0.1); color: var(--success); }
.status-tag.inactive { background: rgba(140,149,166,0.1); color: var(--gray-400); }

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
