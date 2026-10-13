<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  getFaqCategoryList, createFaqCategory, updateFaqCategory, deleteFaqCategory,
  getFaqKnowledgeList, getFaqKnowledgeByCategory, createFaqKnowledge, updateFaqKnowledge, deleteFaqKnowledge,
  downloadFaqTemplate, importFaqKnowledge
} from '@/api/faq'

interface Category { id: number; categoryName: string; sortOrder: number; status: number; questionCount: number }
interface Knowledge { id: number; categoryId: number; categoryName: string; question: string; answer: string; keywords: string; sortOrder: number; status: number; hitCount: number }

const activeTab = ref<'category' | 'knowledge'>('category')

// Import
const importDialogVisible = ref(false)
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const importResult = ref<{ successCount: number; failCount: number; totalRows: number; errors: string[] } | null>(null)
const dragActive = ref(false)

// ===== Category =====
const categoryList = ref<Category[]>([])
const categoryLoading = ref(false)
const categoryDialog = ref(false)
const categoryIsEdit = ref(false)
const categoryForm = ref<any>({})

async function loadCategories() { categoryLoading.value = true; try { const r: any = await getFaqCategoryList(); categoryList.value = r.data } finally { categoryLoading.value = false } }
function handleCategoryAdd() { categoryIsEdit.value = false; categoryForm.value = { categoryName: '', sortOrder: 0, status: 1 }; categoryDialog.value = true }
function handleCategoryEdit(row: Category) { categoryIsEdit.value = true; categoryForm.value = { ...row }; categoryDialog.value = true }
async function handleCategorySubmit() {
  if (!categoryForm.value.categoryName?.trim()) return
  try { categoryIsEdit.value ? await updateFaqCategory(categoryForm.value) : await createFaqCategory(categoryForm.value); categoryDialog.value = false; loadCategories() } catch (e: any) { window.$message?.(e.message || '操作失败', 'error') }
}
async function handleCategoryDelete(row: Category) {
  if (!(await window.$confirm?.(`确定删除分类「${row.categoryName}」吗？该分类下的所有问答也会被删除。`))) return
  try { await deleteFaqCategory(row.id); loadCategories(); loadKnowledge() } catch (e: any) { window.$message?.(e.message || '删除失败', 'error') }
}

// ===== Knowledge =====
const knowledgeList = ref<Knowledge[]>([])
const knowledgeLoading = ref(false)
const filterCategoryId = ref<number | ''>('')
const knowledgeDialog = ref(false)
const knowledgeIsEdit = ref(false)
const knowledgeForm = ref<any>({})
const showAnswer = ref<number | null>(null)

async function loadKnowledge() {
  knowledgeLoading.value = true
  try {
    const r: any = filterCategoryId.value ? await getFaqKnowledgeByCategory(filterCategoryId.value) : await getFaqKnowledgeList()
    knowledgeList.value = r.data
  } finally { knowledgeLoading.value = false }
}

function handleKnowledgeAdd() {
  knowledgeIsEdit.value = false
  knowledgeForm.value = { categoryId: filterCategoryId.value || undefined, question: '', answer: '', keywords: '', sortOrder: 0, status: 1 }
  knowledgeDialog.value = true
}
function handleKnowledgeEdit(row: Knowledge) { knowledgeIsEdit.value = true; knowledgeForm.value = { ...row }; knowledgeDialog.value = true }
async function handleKnowledgeSubmit() {
  if (!knowledgeForm.value.question?.trim() || !knowledgeForm.value.answer?.trim()) return
  try { knowledgeIsEdit.value ? await updateFaqKnowledge(knowledgeForm.value) : await createFaqKnowledge(knowledgeForm.value); knowledgeDialog.value = false; loadKnowledge(); loadCategories() } catch (e: any) { window.$message?.(e.message || '操作失败', 'error') }
}
async function handleKnowledgeDelete(row: Knowledge) {
  if (!(await window.$confirm?.(`确定删除问答「${row.question}」吗？`))) return
  try { await deleteFaqKnowledge(row.id); loadKnowledge(); loadCategories() } catch (e: any) { window.$message?.(e.message || '删除失败', 'error') }
}

function toggleAnswer(id: number) { showAnswer.value = showAnswer.value === id ? null : id }

function handleImport() {
  selectedFile.value = null
  importResult.value = null
  importDialogVisible.value = true
}

async function handleDownloadTemplate() {
  try {
    const res: any = await downloadFaqTemplate()
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'FAQ问答导入模板.xlsx'
    a.click()
    window.URL.revokeObjectURL(url)
  } catch {
    window.$message?.('模板下载失败', 'error')
  }
}

function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files && input.files.length > 0) {
    selectedFile.value = input.files[0]
    importResult.value = null
  }
}

function onDrop(e: DragEvent) {
  dragActive.value = false
  const files = e.dataTransfer?.files
  if (files && files.length > 0) {
    const file = files[0]
    if (file.name.endsWith('.xlsx') || file.name.endsWith('.xls')) {
      selectedFile.value = file
      importResult.value = null
    } else {
      window.$message?.('仅支持 .xlsx 或 .xls 格式的Excel文件', 'error')
    }
  }
}

function onDragOver() { dragActive.value = true }
function onDragLeave() { dragActive.value = false }

function removeFile() {
  selectedFile.value = null
  importResult.value = null
}

async function handleUpload() {
  if (!selectedFile.value) return
  uploading.value = true
  importResult.value = null
  try {
    const res: any = await importFaqKnowledge(selectedFile.value)
    importResult.value = res.data
    loadKnowledge()
    loadCategories()
  } catch (err: any) {
    window.$message?.(err.message || '导入失败', 'error')
  } finally {
    uploading.value = false
  }
}

onMounted(() => { loadCategories(); loadKnowledge() })
</script>

<template>
  <div class="page-container">
    <div class="page-header"><h3>FAQ知识库</h3></div>

    <div class="tab-bar">
      <div class="tab-item" :class="{ active: activeTab === 'category' }" @click="activeTab = 'category'">分类管理</div>
      <div class="tab-item" :class="{ active: activeTab === 'knowledge' }" @click="activeTab = 'knowledge'">问答管理</div>
    </div>

    <!-- ========== Category ========== -->
    <template v-if="activeTab === 'category'">
      <div class="section-header">
        <span class="section-desc">管理问答分类，分类将展示在学生端智能问答助手的分类导航中</span>
        <button class="btn btn-primary" @click="handleCategoryAdd">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          新增分类
        </button>
      </div>
      <div class="table-card">
        <table class="data-table">
          <thead><tr><th style="width:50px">序号</th><th>分类名称</th><th>问答数</th><th>排序</th><th>状态</th><th style="width:130px">操作</th></tr></thead>
          <tbody>
            <tr v-if="categoryLoading"><td colspan="6" class="empty-cell">加载中...</td></tr>
            <tr v-else-if="categoryList.length === 0"><td colspan="6" class="empty-cell">暂无数据</td></tr>
            <tr v-for="(row, i) in categoryList" :key="row.id">
              <td>{{ i + 1 }}</td>
              <td class="cell-bold">{{ row.categoryName }}</td>
              <td><span class="count-badge">{{ row.questionCount }}</span></td>
              <td>{{ row.sortOrder }}</td>
              <td><span class="status-tag" :class="row.status === 1 ? 'active' : 'inactive'">{{ row.status === 1 ? '启用' : '禁用' }}</span></td>
              <td><div class="action-btns"><button class="btn-text" @click="handleCategoryEdit(row)">编辑</button><button class="btn-text danger" @click="handleCategoryDelete(row)">删除</button></div></td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- ========== Knowledge ========== -->
    <template v-if="activeTab === 'knowledge'">
      <div class="section-header">
        <div class="section-left">
          <select v-model="filterCategoryId" class="filter-select" @change="loadKnowledge">
            <option value="">全部分类</option>
            <option v-for="c in categoryList" :key="c.id" :value="c.id">{{ c.categoryName }}</option>
          </select>
        </div>
        <button class="btn btn-outline" @click="handleImport">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
          Excel导入
        </button>
        <button class="btn btn-primary" @click="handleKnowledgeAdd">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          新增问答
        </button>
      </div>

      <div class="faq-list" v-if="knowledgeList.length || knowledgeLoading">
        <div v-if="knowledgeLoading" class="empty-text">加载中...</div>
        <div v-for="row in knowledgeList" :key="row.id" class="faq-item">
          <div class="faq-question" @click="toggleAnswer(row.id)">
            <div class="q-left">
              <span class="q-icon">Q</span>
              <span class="q-text">{{ row.question }}</span>
              <span class="cat-tag" v-if="row.categoryName">{{ row.categoryName }}</span>
            </div>
            <div class="q-right">
              <span class="hit-count" v-if="row.hitCount">命中 {{ row.hitCount }} 次</span>
              <svg class="arrow" :class="{ open: showAnswer === row.id }" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
            </div>
          </div>
          <transition name="expand">
            <div v-if="showAnswer === row.id" class="faq-answer">
              <div class="a-content">
                <span class="a-icon">A</span>
                <div class="a-text" v-html="row.answer"></div>
              </div>
              <div class="a-keywords" v-if="row.keywords">
                <span class="kw-label">关键词：</span>
                <span class="kw-tag" v-for="kw in row.keywords.split(',')" :key="kw">{{ kw.trim() }}</span>
              </div>
              <div class="faq-actions">
                <button class="btn-text" @click="handleKnowledgeEdit(row)">编辑</button>
                <button class="btn-text danger" @click="handleKnowledgeDelete(row)">删除</button>
              </div>
            </div>
          </transition>
        </div>
      </div>
      <div v-else-if="!knowledgeLoading" class="empty-text">暂无问答数据</div>
    </template>

    <!-- ===== Dialogs ===== -->
    <teleport to="body">
      <!-- Category Dialog -->
      <div v-if="categoryDialog" class="modal-mask" @click.self="categoryDialog = false">
        <div class="modal-box">
          <div class="modal-header"><h4>{{ categoryIsEdit ? '编辑分类' : '新增分类' }}</h4><button class="modal-close" @click="categoryDialog = false">&times;</button></div>
          <div class="modal-body">
            <div class="form-row"><label class="form-label">分类名称 <span class="required">*</span></label><input v-model="categoryForm.categoryName" class="form-input" placeholder="如：报到相关" /></div>
            <div class="form-row-inline">
              <div class="form-row half"><label class="form-label">排序</label><input v-model.number="categoryForm.sortOrder" type="number" class="form-input" min="0" /></div>
              <div class="form-row half"><label class="form-label">状态</label><select v-model="categoryForm.status" class="form-input"><option :value="1">启用</option><option :value="0">禁用</option></select></div>
            </div>
          </div>
          <div class="modal-footer"><button class="btn btn-gray" @click="categoryDialog = false">取消</button><button class="btn btn-primary" @click="handleCategorySubmit">确定</button></div>
        </div>
      </div>

      <!-- Knowledge Dialog -->
      <div v-if="knowledgeDialog" class="modal-mask" @click.self="knowledgeDialog = false">
        <div class="modal-box modal-wide">
          <div class="modal-header"><h4>{{ knowledgeIsEdit ? '编辑问答' : '新增问答' }}</h4><button class="modal-close" @click="knowledgeDialog = false">&times;</button></div>
          <div class="modal-body">
            <div class="form-row"><label class="form-label">所属分类</label><select v-model="knowledgeForm.categoryId" class="form-input"><option :value="undefined">不分类</option><option v-for="c in categoryList" :key="c.id" :value="c.id">{{ c.categoryName }}</option></select></div>
            <div class="form-row"><label class="form-label">问题 <span class="required">*</span></label><input v-model="knowledgeForm.question" class="form-input" placeholder="学生可能提出的问题" /></div>
            <div class="form-row"><label class="form-label">答案 <span class="required">*</span></label><textarea v-model="knowledgeForm.answer" class="form-input" rows="5" placeholder="标准答案"></textarea></div>
            <div class="form-row"><label class="form-label">关键词</label><input v-model="knowledgeForm.keywords" class="form-input" placeholder="多个关键词用逗号分隔，用于匹配检索" /></div>
            <div class="form-row-inline">
              <div class="form-row half"><label class="form-label">排序</label><input v-model.number="knowledgeForm.sortOrder" type="number" class="form-input" min="0" /></div>
              <div class="form-row half"><label class="form-label">状态</label><select v-model="knowledgeForm.status" class="form-input"><option :value="1">启用</option><option :value="0">禁用</option></select></div>
            </div>
          </div>
          <div class="modal-footer"><button class="btn btn-gray" @click="knowledgeDialog = false">取消</button><button class="btn btn-primary" @click="handleKnowledgeSubmit">确定</button></div>
        </div>
      </div>

      <!-- Import Dialog -->
      <div v-if="importDialogVisible" class="modal-mask" @click.self="importDialogVisible = false">
        <div class="modal-box modal-wide">
          <div class="modal-header">
            <h4>Excel批量导入FAQ问答</h4>
            <button class="modal-close" @click="importDialogVisible = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="template-tip">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="16" x2="12" y2="12"/>
                <line x1="12" y1="8" x2="12.01" y2="8"/>
              </svg>
              <span>请先下载模板，按模板格式填写数据后上传。字段：<code>问题</code>、<code>答案</code>、<code>关键词</code>、<code>分类名称</code>、<code>排序</code>、<code>状态</code>（启用/禁用）</span>
              <button class="link-btn" @click="handleDownloadTemplate">下载模板</button>
            </div>

            <div
              class="upload-area"
              :class="{ active: dragActive, 'has-file': !!selectedFile }"
              @dragover.prevent="onDragOver"
              @dragleave="onDragLeave"
              @drop.prevent="onDrop"
              @click="($refs.fileInput as HTMLInputElement).click()"
            >
              <input
                ref="fileInput"
                type="file"
                accept=".xlsx,.xls"
                style="display:none"
                @change="onFileChange"
              />
              <template v-if="!selectedFile">
                <div class="upload-icon">
                  <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                    <polyline points="17 8 12 3 7 8"/>
                    <line x1="12" y1="3" x2="12" y2="15"/>
                  </svg>
                </div>
                <p class="upload-text">拖拽Excel文件到此处，或 <span class="upload-link">点击选择</span></p>
                <p class="upload-hint">支持 .xlsx / .xls 格式</p>
              </template>
              <template v-else>
                <div class="file-info">
                  <div class="file-icon">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                      <polyline points="14 2 14 8 20 8"/>
                    </svg>
                  </div>
                  <div class="file-detail">
                    <span class="file-name">{{ selectedFile.name }}</span>
                    <span class="file-size">{{ (selectedFile.size / 1024).toFixed(1) }} KB</span>
                  </div>
                  <button class="file-remove" @click.stop="removeFile">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                  </button>
                </div>
              </template>
            </div>

            <button
              class="btn btn-primary btn-block"
              :disabled="!selectedFile || uploading"
              @click="handleUpload"
              style="margin-top: 16px;"
            >
              <span v-if="uploading" class="spinner"></span>
              <span v-else>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                  <polyline points="17 8 12 3 7 8"/>
                  <line x1="12" y1="3" x2="12" y2="15"/>
                </svg>
              </span>
              {{ uploading ? '正在导入...' : '开始导入' }}
            </button>

            <div v-if="importResult" class="import-result">
              <div class="result-header">
                <svg v-if="importResult.failCount === 0" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--success)" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--warning)" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                <span class="result-title">导入完成</span>
              </div>
              <div class="result-stats">
                <span class="result-line success">成功：{{ importResult.successCount }} 条</span>
                <span class="result-line fail" v-if="importResult.failCount > 0">失败：{{ importResult.failCount }} 条</span>
                <span class="result-line total">共处理：{{ importResult.totalRows }} 条</span>
              </div>
              <div v-if="importResult.errors.length" class="result-errors">
                <div class="errors-title">错误详情：</div>
                <div v-for="(err, i) in importResult.errors" :key="i" class="error-item">{{ err }}</div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-gray" @click="importDialogVisible = false">关闭</button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.page-container { max-width: 1600px; margin: 0 auto; }
.page-header { display: flex; align-items: center; margin-bottom: 12px; }
.page-header h3 { font-size: 18px; font-weight: 600; color: var(--gray-900); margin: 0; }

.tab-bar { display: flex; gap: 0; border-bottom: 2px solid var(--gray-200); margin-bottom: 20px; }
.tab-item { padding: 10px 20px; font-size: 14px; font-weight: 500; color: var(--gray-400); cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all 0.2s; }
.tab-item:hover { color: var(--gray-600); }
.tab-item.active { color: var(--primary); border-bottom-color: var(--primary); }

.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; flex-wrap: wrap; gap: 10px; }
.section-left { display: flex; align-items: center; gap: 10px; }
.section-desc { font-size: 13px; color: var(--gray-400); }
.filter-select { padding: 8px 10px; border: 1.5px solid var(--gray-200); border-radius: 8px; font-size: 13px; color: var(--gray-600); background: #fff; outline: none; cursor: pointer; }
.filter-select:focus { border-color: var(--primary); }
.btn { display: inline-flex; align-items: center; gap: 5px; padding: 8px 16px; border: none; border-radius: 8px; font-size: 13px; font-weight: 500; cursor: pointer; transition: all 0.2s; }
.btn-primary { background: var(--primary); color: #fff; }
.btn-primary:hover { opacity: 0.9; }
.btn-gray { background: var(--gray-100); color: var(--gray-600); border: 1px solid var(--gray-200); }
.btn-gray:hover { background: var(--gray-200); }
.empty-text { text-align: center; color: var(--gray-400); padding: 30px; font-size: 13px; }

.table-card { background: #fff; border-radius: 12px; border: 1px solid var(--gray-200); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { text-align: left; padding: 12px 14px; font-size: 13px; font-weight: 600; color: var(--gray-400); background: var(--gray-100); border-bottom: 1px solid var(--gray-200); white-space: nowrap; }
.data-table td { padding: 11px 14px; font-size: 13px; color: var(--gray-600); border-bottom: 1px solid var(--gray-100); }
.data-table tbody tr:hover { background: rgba(26,92,255,0.02); }
.cell-bold { font-weight: 600; color: var(--gray-900); }
.empty-cell { text-align: center; color: var(--gray-400); padding: 36px 0 !important; }
.count-badge { display: inline-block; min-width: 22px; height: 22px; line-height: 22px; text-align: center; border-radius: 11px; background: rgba(26,92,255,0.08); color: var(--primary); font-size: 12px; font-weight: 600; padding: 0 6px; }
.status-tag { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.status-tag.active { background: rgba(0,201,167,0.1); color: var(--success); }
.status-tag.inactive { background: rgba(140,149,166,0.1); color: var(--gray-400); }
.action-btns { display: flex; gap: 4px; }
.btn-text { background: none; border: none; color: var(--primary); font-size: 13px; cursor: pointer; padding: 4px 8px; border-radius: 4px; transition: background 0.15s; }
.btn-text:hover { background: rgba(26,92,255,0.06); }
.btn-text.danger { color: var(--danger); }
.btn-text.danger:hover { background: rgba(255,77,106,0.06); }

/* FAQ List */
.faq-list { display: flex; flex-direction: column; gap: 8px; }
.faq-item { background: #fff; border: 1px solid var(--gray-200); border-radius: 10px; overflow: hidden; transition: border-color 0.2s; }
.faq-item:hover { border-color: var(--primary); }
.faq-question { display: flex; align-items: center; justify-content: space-between; padding: 14px 16px; cursor: pointer; gap: 12px; }
.q-left { display: flex; align-items: center; gap: 10px; flex: 1; min-width: 0; }
.q-icon { width: 28px; height: 28px; border-radius: 50%; background: var(--primary); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 700; flex-shrink: 0; }
.q-text { font-size: 14px; font-weight: 500; color: var(--gray-900); }
.cat-tag { display: inline-block; padding: 1px 8px; border-radius: 10px; font-size: 11px; background: rgba(26,92,255,0.08); color: var(--primary); flex-shrink: 0; }
.q-right { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.hit-count { font-size: 12px; color: var(--gray-400); }
.arrow { transition: transform 0.2s; color: var(--gray-400); }
.arrow.open { transform: rotate(180deg); }

.faq-answer { padding: 0 16px 14px; border-top: 1px solid var(--gray-100); }
.a-content { display: flex; gap: 10px; padding-top: 14px; }
.a-icon { width: 28px; height: 28px; border-radius: 50%; background: var(--success); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 700; flex-shrink: 0; }
.a-text { font-size: 14px; color: var(--gray-600); line-height: 1.7; }
.a-keywords { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; margin-top: 10px; padding-left: 38px; }
.kw-label { font-size: 12px; color: var(--gray-400); }
.kw-tag { display: inline-block; padding: 2px 8px; border-radius: 10px; font-size: 11px; background: var(--gray-100); color: var(--gray-600); }
.faq-actions { display: flex; gap: 4px; padding-left: 38px; margin-top: 10px; }

.expand-enter-active, .expand-leave-active { transition: all 0.25s ease; overflow: hidden; }
.expand-enter-from, .expand-leave-to { max-height: 0; opacity: 0; padding-top: 0; padding-bottom: 0; }
.expand-enter-to, .expand-leave-from { max-height: 400px; opacity: 1; }

/* Modal */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-box { background: #fff; border-radius: 14px; width: 500px; max-width: 90vw; max-height: 85vh; overflow-y: auto; box-shadow: 0 12px 40px rgba(0,0,0,0.12); }
.modal-box.modal-wide { width: 580px; }
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

.btn-outline { background: #fff; color: var(--gray-600); border: 1.5px solid var(--gray-200); }
.btn-outline:hover { border-color: var(--primary); color: var(--primary); }
.btn-block { width: 100%; justify-content: center; padding: 11px 18px; }

/* Template tip */
.template-tip { display: flex; align-items: center; gap: 10px; padding: 12px 14px; background: var(--gray-100); border-radius: 8px; font-size: 13px; color: var(--gray-600); line-height: 1.6; margin-bottom: 16px; }
.template-tip svg { flex-shrink: 0; color: var(--info); }
.template-tip code { background: #fff; padding: 1px 6px; border-radius: 3px; font-size: 12px; color: var(--primary); border: 1px solid var(--gray-200); }
.link-btn { background: none; border: none; color: var(--primary); font-size: 13px; font-weight: 600; cursor: pointer; white-space: nowrap; flex-shrink: 0; text-decoration: underline; }
.link-btn:hover { opacity: 0.8; }

/* Upload area */
.upload-area { border: 2px dashed var(--gray-200); border-radius: 12px; padding: 36px 20px; text-align: center; cursor: pointer; transition: all 0.2s; }
.upload-area:hover, .upload-area.active { border-color: var(--primary); background: rgba(26, 92, 255, 0.02); }
.upload-area.has-file { border-style: solid; border-color: var(--gray-200); background: var(--gray-100); padding: 16px 20px; }
.upload-icon { margin-bottom: 10px; }
.upload-text { font-size: 14px; color: var(--gray-600); margin: 0 0 4px; }
.upload-link { color: var(--primary); font-weight: 500; }
.upload-hint { font-size: 12px; color: var(--gray-400); margin: 0; }

.file-info { display: flex; align-items: center; gap: 12px; }
.file-icon { flex-shrink: 0; }
.file-detail { flex: 1; text-align: left; }
.file-name { display: block; font-size: 14px; font-weight: 500; color: var(--gray-900); }
.file-size { font-size: 12px; color: var(--gray-400); }
.file-remove { background: none; border: none; color: var(--gray-400); cursor: pointer; padding: 4px; border-radius: 4px; transition: all 0.15s; flex-shrink: 0; }
.file-remove:hover { color: var(--danger); background: rgba(255, 77, 106, 0.08); }

.spinner { display: inline-block; width: 16px; height: 16px; border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff; border-radius: 50%; animation: spin 0.7s linear infinite; margin-right: 6px; vertical-align: middle; }
@keyframes spin { to { transform: rotate(360deg); } }

/* Import Result */
.import-result { margin-top: 18px; padding: 16px; border-radius: 10px; background: var(--gray-100); }
.result-header { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.result-title { font-size: 15px; font-weight: 600; color: var(--gray-900); }
.result-stats { display: flex; gap: 18px; margin-bottom: 8px; }
.result-line { font-size: 14px; font-weight: 600; }
.result-line.success { color: var(--success); }
.result-line.fail { color: var(--danger); }
.result-line.total { color: var(--gray-400); font-weight: 400; font-size: 13px; }
.result-errors { margin-top: 10px; }
.errors-title { font-size: 13px; font-weight: 600; color: var(--danger); margin-bottom: 4px; }
.error-item { font-size: 12px; color: var(--danger); padding: 3px 0; line-height: 1.5; }
</style>
