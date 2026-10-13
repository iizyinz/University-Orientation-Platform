<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getCounselorGroups, getCounselorClasses, createCounselorGroup, updateCounselorGroup, uploadGroupQr } from '@/api/counselor'

const userStore = useUserStore()

interface GroupItem {
  id: number
  groupName: string
  groupNo: string
  groupType: number
  groupQrImage: string
  targetId: number
  targetName: string
  description: string
  status: number
}

interface ClassOption {
  classId: number
  className: string
}

const groupList = ref<GroupItem[]>([])
const classOptions = ref<ClassOption[]>([])
const loading = ref(true)

// 已建群的班级ID列表
const existingClassIds = ref<Set<number>>(new Set())

// 可新增的班级（排除已建群的）
const availableClasses = ref<ClassOption[]>([])

const showEdit = ref(false)
const showCreate = ref(false)
const editForm = ref<{
  id: number
  groupName: string
  groupNo: string
  targetName: string
  groupQrImage: string
}>({ id: 0, groupName: '', groupNo: '', targetName: '', groupQrImage: '' })

const createForm = ref<{
  targetId: number | null
  groupName: string
  groupNo: string
}>({ targetId: null, groupName: '', groupNo: '' })

const uploading = ref(false)
const uploadPreview = ref('')
const fileInput = ref<HTMLInputElement | null>(null)
const creating = ref(false)

// 创建时暂存待上传的文件
const pendingFile = ref<File | null>(null)

async function loadClasses() {
  try {
    const userId = userStore.userInfo.id
    if (!userId) return
    const res: any = await getCounselorClasses(userId)
    classOptions.value = (res.data || []).map((c: any) => ({
      classId: c.classId,
      className: c.className,
    }))
  } catch {}
}

async function loadGroups() {
  loading.value = true
  try {
    const userId = userStore.userInfo.id
    if (!userId) return
    const res: any = await getCounselorGroups(userId)
    groupList.value = res.data || []
    // 记录已建群的班级ID
    existingClassIds.value = new Set(groupList.value.map(g => g.targetId))
    // 过滤出可新增的班级
    availableClasses.value = classOptions.value.filter(c => !existingClassIds.value.has(c.classId))
  } catch {
    groupList.value = []
  } finally {
    loading.value = false
  }
}

const editGroup = (group: GroupItem) => {
  editForm.value = {
    id: group.id,
    groupName: group.groupName || '',
    groupNo: group.groupNo || '',
    targetName: group.targetName || '',
    groupQrImage: group.groupQrImage || '',
  }
  uploadPreview.value = group.groupQrImage || ''
  showEdit.value = true
}

const openCreate = () => {
  createForm.value = { targetId: null, groupName: '', groupNo: '' }
  uploadPreview.value = ''
  pendingFile.value = null
  showCreate.value = true
}

const handleCreate = async () => {
  if (!createForm.value.targetId) return
  creating.value = true
  try {
    const userId = userStore.userInfo.id
    const res: any = await createCounselorGroup({
      targetId: createForm.value.targetId,
      groupName: createForm.value.groupName,
      groupNo: createForm.value.groupNo,
    }, userId)
    const newGroupId = res.data.id
    // 如果有暂存的二维码文件，创建成功后上传
    if (pendingFile.value && newGroupId) {
      uploading.value = true
      try {
        await uploadGroupQr(pendingFile.value, newGroupId)
      } catch (err: any) {
        window.$message?.('群创建成功，但二维码上传失败：' + (err.message || ''), 'error')
      } finally {
        uploading.value = false
      }
    }
    showCreate.value = false
    await loadGroups()
  } catch (err: any) {
    window.$message?.(err.message || '创建失败', 'error')
  } finally {
    creating.value = false
    pendingFile.value = null
  }
}

const triggerFileInput = () => {
  fileInput.value?.click()
}

const onFileChange = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (file.size > 5 * 1024 * 1024) {
    window.$message?.('文件大小不能超过5MB', 'error')
    return
  }
  if (!file.type.startsWith('image/')) {
    window.$message?.('仅支持图片文件', 'error')
    return
  }

  // Show preview
  const reader = new FileReader()
  reader.onload = (ev) => {
    uploadPreview.value = ev.target?.result as string
  }
  reader.readAsDataURL(file)

  // 编辑模式：直接上传到已有群
  const targetGroupId = editForm.value.id
  if (targetGroupId) {
    uploading.value = true
    try {
      const res: any = await uploadGroupQr(file, targetGroupId)
      editForm.value.groupQrImage = res.data.url
    } catch (err: any) {
      window.$message?.(err.message || '上传失败', 'error')
      uploadPreview.value = editForm.value.groupQrImage || ''
    } finally {
      uploading.value = false
      if (fileInput.value) fileInput.value.value = ''
    }
  } else {
    // 创建模式：暂存文件，等创建成功后再上传
    pendingFile.value = file
  }
}

const removeQrImage = () => {
  uploadPreview.value = ''
  pendingFile.value = null
  if (editForm.value.id) {
    editForm.value.groupQrImage = ''
  }
}

// 监听创建弹窗中班级选择变化，自动填充群名称
const onClassChange = () => {
  if (!createForm.value.targetId) return
  const selected = classOptions.value.find(c => c.classId === createForm.value.targetId)
  if (selected && !createForm.value.groupName) {
    createForm.value.groupName = selected.className + '班级群'
  }
}

onMounted(async () => {
  await loadClasses()
  await loadGroups()
})
</script>

<template>
  <div class="group-manage">
    <div class="page-header">
      <div class="header-left">
        <h3>新生群管理</h3>
        <p class="subtitle">配置和管理班级群信息，学生端将自动展示</p>
      </div>
      <button v-if="availableClasses.length > 0" class="btn btn-primary" @click="openCreate">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        新增班级群
      </button>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>

    <div v-else-if="groupList.length === 0" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8C95A6" stroke-width="1.5">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
        <circle cx="9" cy="7" r="4"/>
        <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
        <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
      </svg>
      <p>暂无分配的班级群</p>
      <button v-if="availableClasses.length > 0" class="btn btn-primary" style="margin-top: 12px" @click="openCreate">新增班级群</button>
    </div>

    <div v-else class="group-list">
      <div v-for="group in groupList" :key="group.id" class="group-card">
        <div class="card-left">
          <div class="group-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </div>
          <div class="group-info">
            <h4>{{ group.groupName || group.targetName + '班级群' }}</h4>
            <span class="meta">{{ group.targetName }}</span>
            <div class="group-detail">
              <span v-if="group.groupNo">群号：{{ group.groupNo }}</span>
              <span v-else class="no-data">未设置群号</span>
              <span :class="['status-dot', group.groupQrImage ? 'active' : '']"></span>
              <span class="qr-status">{{ group.groupQrImage ? '已上传二维码' : '未上传二维码' }}</span>
            </div>
          </div>
        </div>
        <div class="card-right">
          <span :class="['status-tag', group.status === 1 ? 'success' : 'inactive']">
            {{ group.status === 1 ? '已启用' : '未启用' }}
          </span>
          <button class="btn btn-outline" @click="editGroup(group)">编辑</button>
        </div>
      </div>
    </div>

    <!-- Create Modal -->
    <teleport to="body">
      <div v-if="showCreate" class="modal-mask" @click.self="showCreate = false">
        <div class="edit-modal">
          <div class="modal-header">
            <h4>新增班级群</h4>
            <button class="close-btn" @click="showCreate = false">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>选择班级 <span class="required">*</span></label>
              <select v-model="createForm.targetId" class="form-select" @change="onClassChange">
                <option :value="null" disabled>请选择班级</option>
                <option v-for="c in availableClasses" :key="c.classId" :value="c.classId">{{ c.className }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>群名称</label>
              <input v-model="createForm.groupName" placeholder="请输入群名称" />
            </div>
            <div class="form-group">
              <label>群号</label>
              <input v-model="createForm.groupNo" placeholder="请输入QQ群号/微信群号" />
            </div>
            <div class="form-group">
              <label>群二维码（可选）</label>
              <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="onFileChange" />
              <div v-if="uploadPreview" class="qr-preview">
                <img :src="uploadPreview" alt="群二维码" />
                <div class="qr-overlay">
                  <button class="qr-action" @click="triggerFileInput" :disabled="uploading">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                    更换
                  </button>
                  <button class="qr-action danger" @click="removeQrImage">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                    </svg>
                    删除
                  </button>
                </div>
              </div>
              <div v-else class="upload-area" @click="triggerFileInput">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="var(--gray-400)" stroke-width="1.5">
                  <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
                  <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
                </svg>
                <p>点击上传群二维码图片</p>
                <span>支持 JPG、PNG 格式，不超过 5MB</span>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-gray" @click="showCreate = false">取消</button>
            <button class="btn btn-primary" @click="handleCreate" :disabled="!createForm.targetId || creating">
              {{ creating ? '创建中...' : '创建' }}
            </button>
          </div>
        </div>
      </div>
    </teleport>

    <!-- Edit Modal -->
    <teleport to="body">
      <div v-if="showEdit" class="modal-mask" @click.self="showEdit = false">
        <div class="edit-modal">
          <div class="modal-header">
            <h4>编辑班级群</h4>
            <button class="close-btn" @click="showEdit = false">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>班级</label>
              <input v-model="editForm.targetName" disabled />
            </div>
            <div class="form-group">
              <label>群名称</label>
              <input v-model="editForm.groupName" placeholder="请输入群名称" />
            </div>
            <div class="form-group">
              <label>群号</label>
              <input v-model="editForm.groupNo" placeholder="请输入QQ群号/微信群号" />
            </div>
            <div class="form-group">
              <label>群二维码</label>
              <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="onFileChange" />
              <div v-if="uploadPreview" class="qr-preview">
                <img :src="uploadPreview" alt="群二维码" />
                <div class="qr-overlay">
                  <button class="qr-action" @click="triggerFileInput" :disabled="uploading">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                    更换
                  </button>
                  <button class="qr-action danger" @click="removeQrImage">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                    </svg>
                    删除
                  </button>
                </div>
                <div v-if="uploading" class="qr-uploading">
                  <div class="loading-spinner small"></div>
                </div>
              </div>
              <div v-else class="upload-area" @click="triggerFileInput">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="var(--gray-400)" stroke-width="1.5">
                  <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
                  <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
                </svg>
                <p>点击上传群二维码图片</p>
                <span>支持 JPG、PNG 格式，不超过 5MB</span>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-gray" @click="showEdit = false">取消</button>
            <button class="btn btn-primary" @click="saveGroup" :disabled="uploading">保存</button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.group-manage { max-width: 1600px; margin: 0 auto; }
.page-header { margin-bottom: 24px; display: flex; justify-content: space-between; align-items: flex-start; }
.header-left h3 { font-size: 20px; font-weight: 600; color: var(--gray-900); margin: 0 0 4px; }
.subtitle { font-size: 14px; color: var(--gray-400); margin: 0; }

.loading-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  min-height: 200px; color: var(--gray-400); gap: 12px;
}
.loading-spinner {
  width: 28px; height: 28px; border: 3px solid var(--gray-200);
  border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite;
}
.loading-spinner.small { width: 20px; height: 20px; border-width: 2px; }
@keyframes spin { to { transform: rotate(360deg); } }

.empty-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  min-height: 200px; text-align: center; color: var(--gray-400);
}
.empty-state p { margin: 16px 0 0; font-size: 14px; }

.group-list { display: flex; flex-direction: column; gap: 12px; }
.group-card {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; border: 1px solid var(--gray-200); border-radius: 12px;
  padding: 16px 20px; transition: box-shadow 0.2s;
}
.group-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.04); }

.card-left { display: flex; align-items: center; gap: 14px; }
.group-icon {
  width: 44px; height: 44px; border-radius: 10px;
  background: rgba(26,92,255,0.1); color: var(--primary);
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.group-info h4 { font-size: 15px; font-weight: 600; color: var(--gray-900); margin: 0 0 4px; }
.meta { font-size: 12px; color: var(--gray-400); }
.group-detail { display: flex; align-items: center; gap: 8px; margin-top: 6px; font-size: 12px; color: var(--gray-400); }
.no-data { color: var(--gray-400); }
.status-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--gray-300); }
.status-dot.active { background: var(--success); }
.qr-status { font-size: 12px; }

.card-right { display: flex; align-items: center; gap: 12px; }
.status-tag { padding: 4px 12px; border-radius: 10px; font-size: 12px; font-weight: 500; }
.status-tag.success { background: rgba(0,201,167,0.1); color: var(--success); }
.status-tag.inactive { background: var(--gray-100); color: var(--gray-400); }

.btn { padding: 8px 16px; border-radius: 8px; font-size: 13px; font-weight: 500; cursor: pointer; transition: all 0.2s; border: none; display: inline-flex; align-items: center; gap: 6px; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-outline { background: #fff; color: var(--gray-600); border: 1px solid var(--gray-200); }
.btn-outline:hover { background: var(--gray-100); }
.btn-primary { background: var(--primary); color: #fff; }
.btn-primary:hover:not(:disabled) { background: var(--primary-deep); }
.btn-gray { background: var(--gray-100); color: var(--gray-600); border: 1px solid var(--gray-200); }
.btn-gray:hover { background: var(--gray-200); }

/* Modal */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.edit-modal { background: #fff; border-radius: 14px; width: 480px; max-width: 90vw; box-shadow: 0 12px 40px rgba(0,0,0,0.12); }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px; border-bottom: 1px solid var(--gray-200); }
.modal-header h4 { font-size: 16px; font-weight: 600; color: var(--gray-900); margin: 0; }
.close-btn { background: none; border: none; color: var(--gray-400); cursor: pointer; padding: 4px; border-radius: 6px; }
.close-btn:hover { background: var(--gray-100); }
.modal-body { padding: 20px 24px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; font-weight: 500; color: var(--gray-600); margin-bottom: 6px; }
.required { color: #FF4D6A; }
.form-group input, .form-select {
  width: 100%; padding: 10px 14px; border: 1px solid var(--gray-200); border-radius: 8px;
  font-size: 14px; outline: none; background: #fff;
}
.form-group input:focus, .form-select:focus { border-color: var(--primary); }
.form-group input:disabled { background: var(--gray-100); color: var(--gray-400); }
.form-select { cursor: pointer; appearance: auto; }

.upload-area {
  border: 2px dashed var(--gray-200); border-radius: 10px; padding: 24px;
  text-align: center; cursor: pointer; transition: border-color 0.2s;
}
.upload-area:hover { border-color: var(--primary); }
.upload-area p { margin: 8px 0 4px; font-size: 13px; color: var(--gray-600); }
.upload-area span { font-size: 12px; color: var(--gray-400); }

.qr-preview {
  position: relative; width: 180px; height: 180px; border-radius: 10px; overflow: hidden;
  border: 1px solid var(--gray-200);
}
.qr-preview img { width: 100%; height: 100%; object-fit: cover; }
.qr-overlay {
  position: absolute; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center; gap: 8px;
  opacity: 0; transition: opacity 0.2s;
}
.qr-preview:hover .qr-overlay { opacity: 1; }
.qr-action {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 6px 12px; border-radius: 6px; border: none;
  font-size: 12px; font-weight: 500; cursor: pointer; color: #fff;
  background: rgba(255,255,255,0.2); backdrop-filter: blur(4px);
  transition: background 0.2s;
}
.qr-action:hover { background: rgba(255,255,255,0.35); }
.qr-action.danger:hover { background: rgba(255,77,106,0.8); }
.qr-uploading {
  position: absolute; inset: 0; background: rgba(255,255,255,0.8);
  display: flex; align-items: center; justify-content: center;
}

.modal-footer { display: flex; gap: 10px; justify-content: flex-end; padding: 16px 24px; border-top: 1px solid var(--gray-200); }
</style>
