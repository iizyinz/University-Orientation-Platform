<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getGroupList, createGroup, updateGroup, deleteGroup } from '@/api/group'
import { getCollegeList, getMajorByCollege, getClassByMajor } from '@/api/org'
import { provinceData } from '@/utils/areaData'
import ImageUpload from '@/components/ImageUpload.vue'

interface GroupItem { id: number; groupType: number; groupName: string; groupNo: string; groupQrImage: string; targetId: number; targetName: string; province: string; description: string; sortOrder: number; status: number }
interface College { id: number; collegeName: string }
interface Major { id: number; majorName: string }
interface ClassItem { id: number; className: string }

const list = ref<GroupItem[]>([])
const loading = ref(false)
const dialog = ref(false)
const isEdit = ref(false)
const form = ref<any>({})

const collegeList = ref<College[]>([])
const majorList = ref<Major[]>([])
const classList = ref<ClassItem[]>([])

const typeMap: Record<number, string> = { 1: '生源地群', 2: '学院群', 3: '班级群' }
const typeColorMap: Record<number, string> = { 1: 'type-1', 2: 'type-2', 3: 'type-3' }

async function loadData() { loading.value = true; try { const r: any = await getGroupList(); list.value = r.data } finally { loading.value = false } }
async function loadColleges() { const r: any = await getCollegeList(); collegeList.value = r.data }

async function onFormTypeChange() {
  form.value.targetId = undefined
  majorList.value = []
  classList.value = []
  if (form.value.groupType === 2) {
    // 学院群 - load colleges
  } else if (form.value.groupType === 3) {
    // 班级群 - load colleges then majors
    const r: any = await getCollegeList()
    collegeList.value = r.data
  }
}

async function onFormCollegeChange() {
  form.value.targetId = undefined
  classList.value = []
  if (form.value.formCollegeId) {
    const r: any = await getMajorByCollege(form.value.formCollegeId)
    majorList.value = r.data
  }
}

async function onFormMajorChange() {
  if (form.value.formMajorId) {
    const r: any = await getClassByMajor(form.value.formMajorId)
    classList.value = r.data
  }
}

function handleAdd() {
  isEdit.value = false
  form.value = { groupType: 1, groupName: '', groupNo: '', groupQrImage: '', targetId: undefined, province: '', description: '', sortOrder: 0, status: 1, formCollegeId: undefined, formMajorId: undefined }
  majorList.value = []; classList.value = []
  dialog.value = true
}

function handleEdit(row: GroupItem) {
  isEdit.value = true
  form.value = { ...row, formCollegeId: undefined, formMajorId: undefined }
  dialog.value = true
}

async function handleSubmit() {
  if (!form.value.groupName?.trim()) return
  // For type 2 (学院群), set targetId from formCollegeId
  if (form.value.groupType === 2) {
    form.value.targetId = form.value.formCollegeId
  }
  // For type 3 (班级群), set targetId from classList selection
  if (form.value.groupType === 3) {
    // targetId should already be set from class selection
  }
  try {
    isEdit.value ? await updateGroup(form.value) : await createGroup(form.value)
    dialog.value = false; loadData()
  } catch (e: any) { window.$message?.(e.message || '操作失败', 'error') }
}

async function handleDelete(row: GroupItem) {
  if (!(await window.$confirm?.(`确定删除群「${row.groupName}」吗？`))) return
  try { await deleteGroup(row.id); loadData() } catch (e: any) { window.$message?.(e.message || '删除失败', 'error') }
}

onMounted(() => { loadData(); loadColleges() })
</script>

<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-desc">管理新生交流群信息，包括生源地群、学院群、班级群，支持上传群二维码图片</span>
      <button class="btn btn-primary" @click="handleAdd">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新增群
      </button>
    </div>

    <div class="group-grid" v-if="list.length || loading">
      <div v-if="loading" class="empty-text">加载中...</div>
      <div v-for="row in list" :key="row.id" class="group-card">
        <div class="card-top">
          <span class="type-tag" :class="typeColorMap[row.groupType]">{{ typeMap[row.groupType] }}</span>
          <span class="status-tag" :class="row.status === 1 ? 'active' : 'inactive'">{{ row.status === 1 ? '启用' : '禁用' }}</span>
        </div>
        <div class="card-qr" v-if="row.groupQrImage">
          <img :src="row.groupQrImage" class="qr-img" />
        </div>
        <div class="card-qr empty" v-else>
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>
          <span>暂无二维码</span>
        </div>
        <div class="card-info">
          <h4 class="card-name">{{ row.groupName }}</h4>
          <p class="card-no" v-if="row.groupNo">群号：{{ row.groupNo }}</p>
          <p class="card-target" v-if="row.province">省份：{{ row.province }}</p>
          <p class="card-target" v-else-if="row.targetName">关联：{{ row.targetName }}</p>
          <p class="card-desc" v-if="row.description">{{ row.description }}</p>
        </div>
        <div class="card-actions">
          <button class="btn-text" @click="handleEdit(row)">编辑</button>
          <button class="btn-text danger" @click="handleDelete(row)">删除</button>
        </div>
      </div>
    </div>
    <div v-else-if="!loading" class="empty-text">暂无群数据</div>

    <teleport to="body">
      <div v-if="dialog" class="modal-mask" @click.self="dialog = false">
        <div class="modal-box modal-wide">
          <div class="modal-header"><h4>{{ isEdit ? '编辑群' : '新增群' }}</h4><button class="modal-close" @click="dialog = false">&times;</button></div>
          <div class="modal-body">
            <div class="form-row-inline">
              <div class="form-row half">
                <label class="form-label">群类型 <span class="required">*</span></label>
                <select v-model="form.groupType" class="form-input" @change="onFormTypeChange">
                  <option :value="1">生源地群</option>
                  <option :value="2">学院群</option>
                  <option :value="3">班级群</option>
                </select>
              </div>
              <div class="form-row half">
                <label class="form-label">排序</label>
                <input v-model.number="form.sortOrder" type="number" class="form-input" min="0" />
              </div>
            </div>

            <!-- 学院群 - 选学院 -->
            <div class="form-row" v-if="form.groupType === 2">
              <label class="form-label">所属学院 <span class="required">*</span></label>
              <select v-model="form.formCollegeId" class="form-input">
                <option :value="undefined" disabled>请选择学院</option>
                <option v-for="c in collegeList" :key="c.id" :value="c.id">{{ c.collegeName }}</option>
              </select>
            </div>

            <!-- 班级群 - 选学院→专业→班级 -->
            <template v-if="form.groupType === 3">
              <div class="form-row">
                <label class="form-label">所属学院 <span class="required">*</span></label>
                <select v-model="form.formCollegeId" class="form-input" @change="onFormCollegeChange">
                  <option :value="undefined" disabled>请选择学院</option>
                  <option v-for="c in collegeList" :key="c.id" :value="c.id">{{ c.collegeName }}</option>
                </select>
              </div>
              <div class="form-row">
                <label class="form-label">所属专业 <span class="required">*</span></label>
                <select v-model="form.formMajorId" class="form-input" @change="onFormMajorChange">
                  <option :value="undefined" disabled>请选择专业</option>
                  <option v-for="m in majorList" :key="m.id" :value="m.id">{{ m.majorName }}</option>
                </select>
              </div>
              <div class="form-row">
                <label class="form-label">所属班级 <span class="required">*</span></label>
                <select v-model="form.targetId" class="form-input">
                  <option :value="undefined" disabled>请选择班级</option>
                  <option v-for="cl in classList" :key="cl.id" :value="cl.id">{{ cl.className }}</option>
                </select>
              </div>
            </template>

            <!-- 生源地群 - 选省份 -->
            <div class="form-row" v-if="form.groupType === 1">
              <label class="form-label">省份 <span class="required">*</span></label>
              <select v-model="form.province" class="form-input">
                <option :value="undefined" disabled>请选择省份</option>
                <option v-for="p in provinceData" :key="p.name" :value="p.name">{{ p.name }}</option>
              </select>
            </div>

            <div class="form-row" v-if="form.groupType === 1">
              <label class="form-label">群描述</label>
              <input v-model="form.description" class="form-input" placeholder="如：湖北省新生交流群" />
            </div>

            <div class="form-row"><label class="form-label">群名称 <span class="required">*</span></label><input v-model="form.groupName" class="form-input" placeholder="如：2026级CS学院群" /></div>
            <div class="form-row"><label class="form-label">群号</label><input v-model="form.groupNo" class="form-input" placeholder="QQ群号/微信群号" /></div>
            <div class="form-row"><label class="form-label">群二维码</label><ImageUpload v-model="form.groupQrImage" dir="group-qr" /></div>
            <div class="form-row-inline">
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
.empty-text { text-align: center; color: var(--gray-400); padding: 30px; font-size: 13px; }

.group-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }
.group-card { background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; overflow: hidden; transition: box-shadow 0.2s; }
.group-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.06); }
.card-top { display: flex; align-items: center; justify-content: space-between; padding: 12px 14px 0; }
.card-qr { padding: 12px 14px; display: flex; justify-content: center; }
.card-qr.empty { flex-direction: column; align-items: center; gap: 6px; padding: 20px 14px; color: var(--gray-400); font-size: 12px; }
.qr-img { width: 120px; height: 120px; object-fit: cover; border-radius: 8px; border: 1px solid var(--gray-200); }
.card-info { padding: 0 14px 12px; }
.card-name { font-size: 15px; font-weight: 600; color: var(--gray-900); margin: 0 0 4px; }
.card-no { font-size: 13px; color: var(--gray-600); margin: 2px 0; }
.card-target { font-size: 12px; color: var(--primary); margin: 2px 0; }
.card-desc { font-size: 12px; color: var(--gray-400); margin: 4px 0 0; }
.card-actions { display: flex; gap: 4px; padding: 0 14px 12px; }

.type-tag { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 11px; font-weight: 500; }
.type-tag.type-1 { background: rgba(0,201,167,0.1); color: var(--success); }
.type-tag.type-2 { background: rgba(26,92,255,0.1); color: var(--primary); }
.type-tag.type-3 { background: rgba(255,122,26,0.1); color: var(--secondary-orange); }
.status-tag { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 11px; font-weight: 500; }
.status-tag.active { background: rgba(0,201,167,0.1); color: var(--success); }
.status-tag.inactive { background: rgba(140,149,166,0.1); color: var(--gray-400); }

.btn-text { background: none; border: none; color: var(--primary); font-size: 13px; cursor: pointer; padding: 4px 8px; border-radius: 4px; transition: background 0.15s; }
.btn-text:hover { background: rgba(26,92,255,0.06); }
.btn-text.danger { color: var(--danger); }
.btn-text.danger:hover { background: rgba(255,77,106,0.06); }

.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-box { background: #fff; border-radius: 14px; width: 500px; max-width: 90vw; max-height: 85vh; overflow-y: auto; box-shadow: 0 12px 40px rgba(0,0,0,0.12); }
.modal-box.modal-wide { width: 560px; }
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
