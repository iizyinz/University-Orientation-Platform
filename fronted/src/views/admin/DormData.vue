<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import {
  getBuildingList, createBuilding, updateBuilding, deleteBuilding,
  getRoomList, getRoomByBuilding, createRoom, updateRoom, deleteRoom,
  batchCreateRooms
} from '@/api/dorm'
import PagePagination from '@/components/PagePagination.vue'

interface Building { id: number; buildingName: string; buildingCode: string; floorCount: number; description: string; sortOrder: number; status: number; roomCount: number; usedBedCount: number; totalBedCount: number }
interface Room { id: number; buildingId: number; buildingName: string; floorNo: number; roomNo: string; bedCount: number; usedBedCount: number; roomType: string; description: string; status: number }

const buildingList = ref<Building[]>([])
const buildingLoading = ref(false)
const buildingDialog = ref(false)
const buildingIsEdit = ref(false)
const buildingForm = ref<any>({})

const roomList = ref<Room[]>([])
const roomLoading = ref(false)
const filterBuildingId = ref<number | ''>('')
const filterFloorNo = ref<number | ''>('')
const roomDialog = ref(false)
const roomIsEdit = ref(false)
const roomForm = ref<any>({})

// 批量创建
const batchDialog = ref(false)
const batchForm = ref<any>({})
const batching = ref(false)

// 楼层筛选变化时重置页码
watch(filterFloorNo, () => { roomPage.value = 1 })

const availableFloors = computed(() => {
  if (!filterBuildingId.value) return []
  const b = buildingList.value.find(b => b.id === filterBuildingId.value)
  if (!b) return []
  return Array.from({ length: b.floorCount }, (_, i) => i + 1)
})

// 批量创建时的楼层选项
const batchAvailableFloors = computed(() => {
  if (!batchForm.value.buildingId) return []
  const b = buildingList.value.find(b => b.id === batchForm.value.buildingId)
  if (!b) return []
  return Array.from({ length: b.floorCount }, (_, i) => i + 1)
})

// 新增/编辑房间时的楼层选项
const roomAvailableFloors = computed(() => {
  if (!roomForm.value.buildingId) return []
  const b = buildingList.value.find(b => b.id === roomForm.value.buildingId)
  if (!b) return []
  return Array.from({ length: b.floorCount }, (_, i) => i + 1)
})

// 监听批量创建对话框中楼栋变化，重置楼层
watch(() => batchForm.value.buildingId, () => {
  batchForm.value.floorNo = 1
})

// 监听新增/编辑房间对话框中楼栋变化，重置楼层
watch(() => roomForm.value.buildingId, () => {
  roomForm.value.floorNo = 1
})

const filteredRoomList = computed(() => {
  if (filterFloorNo.value === '') return roomList.value
  return roomList.value.filter(r => r.floorNo === filterFloorNo.value)
})

const pageSize = 10
const roomPage = ref(1)
const paginatedRooms = computed(() => {
  const start = (roomPage.value - 1) * pageSize
  return filteredRoomList.value.slice(start, start + pageSize)
})

async function loadBuildings() { buildingLoading.value = true; try { const r: any = await getBuildingList(); buildingList.value = r.data } finally { buildingLoading.value = false } }
async function loadRooms() { roomLoading.value = true; try { const r: any = filterBuildingId.value ? await getRoomByBuilding(filterBuildingId.value) : await getRoomList(); roomList.value = r.data } finally { roomLoading.value = false } }

function onBuildingFilterChange() {
  filterFloorNo.value = ''
  roomPage.value = 1
  loadRooms()
}

function handleBuildingAdd() { buildingIsEdit.value = false; buildingForm.value = { buildingName: '', buildingCode: '', floorCount: 6, description: '', sortOrder: 0, status: 1 }; buildingDialog.value = true }
function handleBuildingEdit(row: Building) { buildingIsEdit.value = true; buildingForm.value = { ...row }; buildingDialog.value = true }
async function handleBuildingSubmit() {
  if (!buildingForm.value.buildingName?.trim() || !buildingForm.value.buildingCode?.trim()) return
  try { buildingIsEdit.value ? await updateBuilding(buildingForm.value) : await createBuilding(buildingForm.value); buildingDialog.value = false; loadBuildings() } catch (e: any) { window.$message?.(e.message || '操作失败', 'error') }
}
async function handleBuildingDelete(row: Building) {
  if (!(await window.$confirm?.(`确定删除楼栋「${row.buildingName}」吗？`))) return
  try { await deleteBuilding(row.id); loadBuildings() } catch (e: any) { window.$message?.(e.message || '删除失败', 'error') }
}

function handleRoomAdd() { roomIsEdit.value = false; roomForm.value = { buildingId: filterBuildingId.value || undefined, floorNo: 1, roomNo: '', bedCount: 4, roomType: '四人间', description: '', status: 1 }; roomDialog.value = true }

function handleBatchAdd() {
  batchForm.value = { buildingId: filterBuildingId.value || undefined, floorNo: 1, count: 10, bedCount: 4, roomType: '四人间' }
  batchDialog.value = true
}

async function handleBatchSubmit() {
  if (!batchForm.value.buildingId || !batchForm.value.floorNo || !batchForm.value.count) return
  if (batchForm.value.count < 1 || batchForm.value.count > 200) { window.$message?.('数量范围1-200', 'error'); return }
  batching.value = true
  try {
    const res: any = await batchCreateRooms(batchForm.value)
    const { created, skipped, total } = res.data
    window.$message?.(`批量创建完成：成功 ${created} 间，跳过 ${skipped} 间（已存在），共 ${total} 间`, 'success')
    batchDialog.value = false
    loadRooms()
  } catch (e: any) {
    window.$message?.(e.message || '操作失败', 'error')
  } finally {
    batching.value = false
  }
}
function handleRoomEdit(row: Room) { roomIsEdit.value = true; roomForm.value = { ...row }; roomDialog.value = true }
async function handleRoomSubmit() {
  if (!roomForm.value.buildingId || !roomForm.value.roomNo?.trim()) return
  try { roomIsEdit.value ? await updateRoom(roomForm.value) : await createRoom(roomForm.value); roomDialog.value = false; loadRooms() } catch (e: any) { window.$message?.(e.message || '操作失败', 'error') }
}
async function handleRoomDelete(row: Room) {
  if (!(await window.$confirm?.(`确定删除房间「${row.buildingName} ${row.roomNo}」吗？`))) return
  try { await deleteRoom(row.id); loadRooms() } catch (e: any) { window.$message?.(e.message || '删除失败', 'error') }
}

onMounted(() => { loadBuildings(); loadRooms() })
</script>

<template>
  <div class="page-container">
    <div class="section-header">
      <span class="section-title">楼栋管理</span>
      <button class="btn btn-primary btn-sm" @click="handleBuildingAdd">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新增楼栋
      </button>
    </div>
    <div class="building-grid" v-if="buildingList.length || buildingLoading">
      <div v-if="buildingLoading" class="loading-text">加载中...</div>
      <div v-for="b in buildingList" :key="b.id" class="building-card">
        <div class="building-top">
          <span class="building-name">{{ b.buildingName }}</span>
          <span class="status-tag" :class="b.status === 1 ? 'active' : 'inactive'">{{ b.status === 1 ? '启用' : '禁用' }}</span>
        </div>
        <div class="building-meta"><code>{{ b.buildingCode }}</code> · {{ b.floorCount }}层</div>
        <div class="building-stats"><span>{{ b.roomCount }} 间房</span><span>{{ b.usedBedCount }}/{{ b.totalBedCount }} 床位</span></div>
        <div class="building-actions"><button class="btn-text" @click="handleBuildingEdit(b)">编辑</button><button class="btn-text danger" @click="handleBuildingDelete(b)">删除</button></div>
      </div>
    </div>
    <div v-else class="empty-text">暂无楼栋数据</div>

    <div class="section-header" style="margin-top:28px">
      <div class="section-left">
        <span class="section-title">房间管理</span>
        <select v-model="filterBuildingId" class="filter-select" @change="onBuildingFilterChange">
          <option value="">全部楼栋</option>
          <option v-for="b in buildingList" :key="b.id" :value="b.id">{{ b.buildingName }}</option>
        </select>
        <select v-model="filterFloorNo" class="filter-select" :disabled="!filterBuildingId">
          <option value="">全部楼层</option>
          <option v-for="f in availableFloors" :key="f" :value="f">{{ f }}F</option>
        </select>
      </div>
      <div class="room-actions">
        <button class="btn btn-outline btn-sm" @click="handleBatchAdd">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>
          批量创建
        </button>
        <button class="btn btn-primary btn-sm" @click="handleRoomAdd">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          新增房间
        </button>
      </div>
    </div>
    <div class="table-card">
      <table class="data-table">
        <thead><tr><th style="width:50px">序号</th><th>楼栋</th><th>楼层</th><th>房间号</th><th>床位</th><th>已用</th><th>类型</th><th>状态</th><th style="width:130px">操作</th></tr></thead>
        <tbody>
          <tr v-if="roomLoading"><td colspan="9" class="empty-cell">加载中...</td></tr>
          <tr v-else-if="filteredRoomList.length === 0"><td colspan="9" class="empty-cell">暂无数据</td></tr>
          <tr v-for="(r, i) in paginatedRooms" :key="r.id">
            <td>{{ (roomPage - 1) * pageSize + i + 1 }}</td><td>{{ r.buildingName }}</td><td>{{ r.floorNo }}F</td>
            <td class="cell-bold">{{ r.roomNo }}</td><td>{{ r.bedCount }}</td>
            <td><span :class="{ 'text-danger': r.usedBedCount >= r.bedCount }">{{ r.usedBedCount }}</span>/{{ r.bedCount }}</td>
            <td>{{ r.roomType || '-' }}</td>
            <td><span class="status-tag" :class="r.status === 1 ? 'active' : 'inactive'">{{ r.status === 1 ? '可用' : '不可用' }}</span></td>
            <td><div class="action-btns"><button class="btn-text" @click="handleRoomEdit(r)">编辑</button><button class="btn-text danger" @click="handleRoomDelete(r)">删除</button></div></td>
          </tr>
        </tbody>
      </table>
      <PagePagination :total="filteredRoomList.length" :page="roomPage" :page-size="pageSize" @update:page="roomPage = $event" />
    </div>

    <teleport to="body">
      <!-- Batch Create Dialog -->
      <div v-if="batchDialog" class="modal-mask" @click.self="batchDialog = false">
        <div class="modal-box">
          <div class="modal-header"><h4>批量创建房间</h4><button class="modal-close" @click="batchDialog = false">&times;</button></div>
          <div class="modal-body">
            <div class="batch-tip">房间命名规则：楼栋编码+楼层+序号，如 1栋1层第1间 → <code>1-1001</code></div>
            <div class="form-row"><label class="form-label">所属楼栋 <span class="required">*</span></label><select v-model="batchForm.buildingId" class="form-input"><option :value="undefined" disabled>请选择</option><option v-for="b in buildingList" :key="b.id" :value="b.id">{{ b.buildingName }}</option></select></div>
            <div class="form-row-inline">
              <div class="form-row half"><label class="form-label">楼层 <span class="required">*</span></label><select v-model.number="batchForm.floorNo" class="form-input" :disabled="!batchForm.buildingId"><option :value="undefined" disabled>请选择楼栋</option><option v-for="f in batchAvailableFloors" :key="f" :value="f">{{ f }}F</option></select></div>
              <div class="form-row half"><label class="form-label">创建数量 <span class="required">*</span></label><input v-model.number="batchForm.count" type="number" class="form-input" min="1" max="200" placeholder="1-200" /></div>
            </div>
            <div class="form-row-inline">
              <div class="form-row half"><label class="form-label">床位数</label><input v-model.number="batchForm.bedCount" type="number" class="form-input" min="1" /></div>
              <div class="form-row half"><label class="form-label">房间类型</label><select v-model="batchForm.roomType" class="form-input"><option>四人间</option><option>六人间</option><option>八人间</option></select></div>
            </div>
          </div>
          <div class="modal-footer"><button class="btn btn-gray" @click="batchDialog = false">取消</button><button class="btn btn-primary" @click="handleBatchSubmit" :disabled="batching">{{ batching ? '创建中...' : '批量创建' }}</button></div>
        </div>
      </div>

      <div v-if="buildingDialog" class="modal-mask" @click.self="buildingDialog = false">
        <div class="modal-box">
          <div class="modal-header"><h4>{{ buildingIsEdit ? '编辑楼栋' : '新增楼栋' }}</h4><button class="modal-close" @click="buildingDialog = false">&times;</button></div>
          <div class="modal-body">
            <div class="form-row"><label class="form-label">楼栋名称 <span class="required">*</span></label><input v-model="buildingForm.buildingName" class="form-input" placeholder="如：1号楼" /></div>
            <div class="form-row"><label class="form-label">楼栋编码 <span class="required">*</span></label><input v-model="buildingForm.buildingCode" class="form-input" placeholder="如：B01" :disabled="buildingIsEdit" /></div>
            <div class="form-row"><label class="form-label">楼层数 <span class="required">*</span></label><input v-model.number="buildingForm.floorCount" type="number" class="form-input" min="1" /></div>
            <div class="form-row"><label class="form-label">描述</label><textarea v-model="buildingForm.description" class="form-input" rows="2" placeholder="选填"></textarea></div>
            <div class="form-row-inline">
              <div class="form-row half"><label class="form-label">排序</label><input v-model.number="buildingForm.sortOrder" type="number" class="form-input" min="0" /></div>
              <div class="form-row half"><label class="form-label">状态</label><select v-model="buildingForm.status" class="form-input"><option :value="1">启用</option><option :value="0">禁用</option></select></div>
            </div>
          </div>
          <div class="modal-footer"><button class="btn btn-gray" @click="buildingDialog = false">取消</button><button class="btn btn-primary" @click="handleBuildingSubmit">确定</button></div>
        </div>
      </div>
      <div v-if="roomDialog" class="modal-mask" @click.self="roomDialog = false">
        <div class="modal-box">
          <div class="modal-header"><h4>{{ roomIsEdit ? '编辑房间' : '新增房间' }}</h4><button class="modal-close" @click="roomDialog = false">&times;</button></div>
          <div class="modal-body">
            <div class="form-row"><label class="form-label">所属楼栋 <span class="required">*</span></label><select v-model="roomForm.buildingId" class="form-input"><option :value="undefined" disabled>请选择</option><option v-for="b in buildingList" :key="b.id" :value="b.id">{{ b.buildingName }}</option></select></div>
            <div class="form-row-inline">
              <div class="form-row half"><label class="form-label">楼层 <span class="required">*</span></label><select v-model.number="roomForm.floorNo" class="form-input" :disabled="!roomForm.buildingId"><option :value="undefined" disabled>请选择楼栋</option><option v-for="f in roomAvailableFloors" :key="f" :value="f">{{ f }}F</option></select></div>
              <div class="form-row half"><label class="form-label">房间号 <span class="required">*</span></label><input v-model="roomForm.roomNo" class="form-input" placeholder="如：101" /></div>
            </div>
            <div class="form-row-inline">
              <div class="form-row half"><label class="form-label">床位数</label><input v-model.number="roomForm.bedCount" type="number" class="form-input" min="1" /></div>
              <div class="form-row half"><label class="form-label">房间类型</label><select v-model="roomForm.roomType" class="form-input"><option>四人间</option><option>六人间</option><option>八人间</option></select></div>
            </div>
            <div class="form-row"><label class="form-label">状态</label><select v-model="roomForm.status" class="form-input"><option :value="1">可用</option><option :value="0">不可用</option></select></div>
          </div>
          <div class="modal-footer"><button class="btn btn-gray" @click="roomDialog = false">取消</button><button class="btn btn-primary" @click="handleRoomSubmit">确定</button></div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.page-container { max-width: 1600px; margin: 0 auto; }
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; flex-wrap: wrap; gap: 10px; }
.section-left { display: flex; align-items: center; gap: 10px; }
.room-actions { display: flex; gap: 8px; }
.batch-tip { background: var(--gray-100); border-radius: 8px; padding: 10px 14px; font-size: 13px; color: var(--gray-600); margin-bottom: 16px; }
.batch-tip code { background: var(--gray-200); padding: 2px 6px; border-radius: 4px; font-size: 12px; color: var(--primary); }
.btn-outline { background: #fff; color: var(--gray-600); border: 1px solid var(--gray-200); }
.btn-outline:hover { background: var(--gray-100); }
.section-title { font-size: 15px; font-weight: 600; color: var(--gray-900); }
.filter-select { padding: 8px 10px; border: 1.5px solid var(--gray-200); border-radius: 8px; font-size: 13px; color: var(--gray-600); background: #fff; outline: none; cursor: pointer; }
.filter-select:focus { border-color: var(--primary); }
.btn { display: inline-flex; align-items: center; gap: 5px; padding: 8px 16px; border: none; border-radius: 8px; font-size: 13px; font-weight: 500; cursor: pointer; transition: all 0.2s; }
.btn-sm { padding: 7px 14px; font-size: 13px; }
.btn-primary { background: var(--primary); color: #fff; }
.btn-primary:hover { opacity: 0.9; }
.btn-gray { background: var(--gray-100); color: var(--gray-600); border: 1px solid var(--gray-200); }
.btn-gray:hover { background: var(--gray-200); }
.building-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 14px; margin-bottom: 8px; }
.building-card { background: #fff; border: 1px solid var(--gray-200); border-radius: 10px; padding: 16px; transition: box-shadow 0.2s; }
.building-card:hover { box-shadow: 0 3px 10px rgba(0,0,0,0.05); }
.building-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
.building-name { font-size: 15px; font-weight: 600; color: var(--gray-900); }
.building-meta { font-size: 12px; color: var(--gray-400); margin-bottom: 10px; }
.building-meta code { color: var(--primary); background: rgba(26,92,255,0.06); padding: 1px 6px; border-radius: 3px; font-size: 11px; }
.building-stats { display: flex; gap: 14px; font-size: 13px; color: var(--gray-600); margin-bottom: 10px; }
.building-actions { display: flex; gap: 4px; }
.loading-text, .empty-text { text-align: center; color: var(--gray-400); padding: 30px; font-size: 13px; }
.table-card { background: #fff; border-radius: 12px; border: 1px solid var(--gray-200); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { text-align: left; padding: 12px 14px; font-size: 13px; font-weight: 600; color: var(--gray-400); background: var(--gray-100); border-bottom: 1px solid var(--gray-200); white-space: nowrap; }
.data-table td { padding: 11px 14px; font-size: 13px; color: var(--gray-600); border-bottom: 1px solid var(--gray-100); }
.data-table tbody tr:hover { background: rgba(26,92,255,0.02); }
.cell-bold { font-weight: 600; color: var(--gray-900); }
.empty-cell { text-align: center; color: var(--gray-400); padding: 36px 0 !important; }
.text-danger { color: var(--danger); font-weight: 600; }
.status-tag { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.status-tag.active { background: rgba(0,201,167,0.1); color: var(--success); }
.status-tag.inactive { background: rgba(140,149,166,0.1); color: var(--gray-400); }
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
