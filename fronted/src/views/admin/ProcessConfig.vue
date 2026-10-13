<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getProcessList, createProcess, updateProcess, deleteProcess } from '@/api/process'

interface FlowItem {
  id: number
  flowName: string
  flowStep: number
  flowDesc: string
  isRequired: number
  sortOrder: number
  status: number
}

const tableData = ref<FlowItem[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增流程步骤')
const isEdit = ref(false)
const formData = ref<Partial<FlowItem>>({})

async function loadData() {
  loading.value = true
  try {
    const res: any = await getProcessList()
    tableData.value = res.data
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增流程步骤'
  const nextStep = tableData.value.length > 0
    ? Math.max(...tableData.value.map(t => t.flowStep)) + 1
    : 1
  formData.value = { flowName: '', flowStep: nextStep, flowDesc: '', isRequired: 1, sortOrder: 0, status: 1 }
  dialogVisible.value = true
}

function handleEdit(row: FlowItem) {
  isEdit.value = true
  dialogTitle.value = '编辑流程步骤'
  formData.value = { ...row }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formData.value.flowName?.trim()) return
  if (!formData.value.flowStep && formData.value.flowStep !== 0) return

  try {
    if (isEdit.value) {
      await updateProcess(formData.value)
    } else {
      await createProcess(formData.value)
    }
    dialogVisible.value = false
    loadData()
  } catch (err: any) {
    window.$message?.(err.message || '操作失败', 'error')
  }
}

async function handleDelete(row: FlowItem) {
  if (!(await window.$confirm?.(`确定删除流程步骤「${row.flowName}」吗？`))) return
  try {
    await deleteProcess(row.id)
    loadData()
  } catch (err: any) {
    window.$message?.(err.message || '删除失败', 'error')
  }
}

function moveUp(row: FlowItem, idx: number) {
  if (idx === 0) return
  const prev = tableData.value[idx - 1]
  const tempStep = row.flowStep
  row.flowStep = prev.flowStep
  prev.flowStep = tempStep
  updateProcess(row)
  updateProcess(prev)
  loadData()
}

function moveDown(row: FlowItem, idx: number) {
  if (idx === tableData.value.length - 1) return
  const next = tableData.value[idx + 1]
  const tempStep = row.flowStep
  row.flowStep = next.flowStep
  next.flowStep = tempStep
  updateProcess(row)
  updateProcess(next)
  loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h3>流程配置</h3>
      <button class="btn btn-primary" @click="handleAdd">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新增步骤
      </button>
    </div>

    <div class="flow-tip">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/>
      </svg>
      流程步骤按序号从小到大排列，学生端将按此顺序展示报到流程。拖动或点击箭头可调整顺序。
    </div>

    <!-- Flow Timeline -->
    <div class="flow-timeline" v-if="tableData.length > 0">
      <div
        v-for="(row, idx) in tableData"
        :key="row.id"
        class="flow-node"
      >
        <div class="node-left">
          <div class="node-step" :class="{ disabled: row.status === 0 }">
            {{ row.flowStep }}
          </div>
          <div class="node-line" v-if="idx < tableData.length - 1"></div>
        </div>
        <div class="node-card" :class="{ disabled: row.status === 0 }">
          <div class="card-header">
            <div class="card-title-row">
              <span class="card-title">{{ row.flowName }}</span>
              <span class="required-tag" v-if="row.isRequired === 1">必填</span>
              <span class="optional-tag" v-else>选填</span>
              <span class="status-tag" :class="row.status === 1 ? 'active' : 'inactive'">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </span>
            </div>
            <p class="card-desc" v-if="row.flowDesc">{{ row.flowDesc }}</p>
          </div>
          <div class="card-actions">
            <button class="icon-btn" title="上移" :disabled="idx === 0" @click="moveUp(row, idx)">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
            <button class="icon-btn" title="下移" :disabled="idx === tableData.length - 1" @click="moveDown(row, idx)">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"/></svg>
            </button>
            <button class="icon-btn" title="编辑" @click="handleEdit(row)">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
            </button>
            <button class="icon-btn danger" title="删除" @click="handleDelete(row)">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="!loading" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
      </svg>
      <p>暂无流程配置，请点击「新增步骤」添加</p>
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
              <label class="form-label">流程名称 <span class="required">*</span></label>
              <input v-model="formData.flowName" class="form-input" placeholder="如：在线填报信息" />
            </div>
            <div class="form-row">
              <label class="form-label">步骤序号 <span class="required">*</span></label>
              <input v-model.number="formData.flowStep" type="number" class="form-input" min="1" placeholder="如：1" />
            </div>
            <div class="form-row">
              <label class="form-label">流程描述</label>
              <textarea v-model="formData.flowDesc" class="form-input" rows="3" placeholder="该步骤的详细说明（选填）"></textarea>
            </div>
            <div class="form-row-inline">
              <div class="form-row half">
                <label class="form-label">是否必填</label>
                <select v-model="formData.isRequired" class="form-input">
                  <option :value="1">必填</option>
                  <option :value="0">选填</option>
                </select>
              </div>
              <div class="form-row half">
                <label class="form-label">排序</label>
                <input v-model.number="formData.sortOrder" type="number" class="form-input" min="0" />
              </div>
            </div>
            <div class="form-row">
              <label class="form-label">状态</label>
              <select v-model="formData.status" class="form-input">
                <option :value="1">启用</option>
                <option :value="0">禁用</option>
              </select>
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

.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-header h3 { font-size: 18px; font-weight: 600; color: var(--gray-900); margin: 0; }

.flow-tip { display: flex; align-items: center; gap: 8px; padding: 12px 14px; background: rgba(26, 92, 255, 0.04); border: 1px solid rgba(26, 92, 255, 0.12); border-radius: 8px; font-size: 13px; color: var(--primary); margin-bottom: 24px; line-height: 1.5; }
.flow-tip svg { flex-shrink: 0; }

.btn { display: inline-flex; align-items: center; gap: 6px; padding: 9px 18px; border: none; border-radius: 8px; font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s; }
.btn-primary { background: var(--primary); color: #fff; }
.btn-primary:hover { opacity: 0.9; }
.btn-gray { background: var(--gray-100); color: var(--gray-600); border: 1px solid var(--gray-200); }
.btn-gray:hover { background: var(--gray-200); }

/* Timeline */
.flow-timeline { display: flex; flex-direction: column; }

.flow-node { display: flex; gap: 0; }

.node-left { display: flex; flex-direction: column; align-items: center; width: 48px; flex-shrink: 0; }

.node-step {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
  transition: background 0.2s;
}

.node-step.disabled {
  background: var(--gray-200);
  color: var(--gray-400);
}

.node-line {
  width: 2px;
  flex: 1;
  min-height: 16px;
  background: var(--gray-200);
}

.node-card {
  flex: 1;
  background: #fff;
  border: 1px solid var(--gray-200);
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.node-card:hover {
  border-color: var(--primary);
  box-shadow: 0 2px 8px rgba(26, 92, 255, 0.06);
}

.node-card.disabled {
  opacity: 0.55;
}

.card-header { flex: 1; min-width: 0; }

.card-title-row { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }

.card-title { font-size: 15px; font-weight: 600; color: var(--gray-900); }

.required-tag {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
  background: rgba(255, 77, 106, 0.08);
  color: var(--danger);
}

.optional-tag {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
  background: var(--gray-100);
  color: var(--gray-400);
}

.status-tag {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 11px;
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

.card-desc {
  font-size: 13px;
  color: var(--gray-400);
  margin: 6px 0 0;
  line-height: 1.5;
}

.card-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.icon-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  border-radius: 6px;
  color: var(--gray-400);
  cursor: pointer;
  transition: all 0.15s;
}

.icon-btn:hover:not(:disabled) {
  background: var(--gray-100);
  color: var(--primary);
}

.icon-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.icon-btn.danger:hover:not(:disabled) {
  background: rgba(255, 77, 106, 0.08);
  color: var(--danger);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--gray-400);
}
.empty-state p { margin: 16px 0 0; font-size: 14px; }

/* Modal */
.modal-mask { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-box { background: #fff; border-radius: 14px; width: 500px; max-width: 90vw; box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12); }
.modal-header { display: flex; align-items: center; justify-content: space-between; padding: 20px 24px 0; }
.modal-header h4 { font-size: 17px; font-weight: 600; color: var(--gray-900); margin: 0; }
.modal-close { background: none; border: none; font-size: 22px; color: var(--gray-400); cursor: pointer; padding: 0 4px; line-height: 1; }
.modal-body { padding: 20px 24px; }
.form-row { margin-bottom: 16px; }
.form-row.half { flex: 1; }
.form-row-inline { display: flex; gap: 16px; }
.form-label { display: block; font-size: 13px; font-weight: 600; color: var(--gray-600); margin-bottom: 6px; }
.required { color: var(--danger); }
.form-input { width: 100%; padding: 9px 12px; border: 1.5px solid var(--gray-200); border-radius: 8px; font-size: 14px; color: var(--gray-900); outline: none; transition: border-color 0.2s; box-sizing: border-box; font-family: inherit; }
.form-input:focus { border-color: var(--primary); }
select.form-input { appearance: auto; }
.modal-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 0 24px 20px; }
</style>
