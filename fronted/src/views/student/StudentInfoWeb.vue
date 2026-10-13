<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getStudentInfo, updateStudentInfo, updateUniformSize } from '@/api/student'
import { provinceData } from '@/utils/areaData'
import request from '@/utils/request'
import AppAlert from '@/components/AppAlert.vue'

const userStore = useUserStore()

const showAlert = ref(false)
const alertMessage = ref('')
const alertType = ref<'info' | 'error' | 'success'>('info')
function showModal(msg: string, type: 'info' | 'error' | 'success' = 'info') {
  alertMessage.value = msg
  alertType.value = type
  showAlert.value = true
}

const activeSection = ref('basic')
const loading = ref(true)
const saving = ref(false)
const saveSuccess = ref(false)

const infoData = ref({
  name: '', studentNo: '', idCard: '', gender: null as number | null,
  birthday: '', collegeName: '', majorName: '', className: '',
  reportStatus: 0, infoCompleted: 0,
  nation: '', politicalStatus: '',
  phone: '', email: '',
  province: '', city: '', district: '', healthStatus: '',
  emergencyContactName: '', emergencyContactRelation: '', emergencyContactPhone: '',
  clothingSize: '', shoeSize: '', hobby: '',
})

const nationList = [
  '汉族','蒙古族','回族','藏族','维吾尔族','苗族','彝族','壮族','布依族','朝鲜族',
  '满族','侗族','瑶族','白族','土家族','哈尼族','哈萨克族','傣族','黎族','傈僳族',
  '佤族','畲族','高山族','拉祜族','水族','东乡族','纳西族','景颇族','柯尔克孜族','土族',
  '达斡尔族','仫佬族','羌族','布朗族','撒拉族','毛南族','仡佬族','锡伯族','阿昌族','普米族',
  '塔吉克族','怒族','乌孜别克族','俄罗斯族','鄂温克族','德昂族','保安族','裕固族','京族','塔塔尔族',
  '独龙族','鄂伦春族','赫哲族','门巴族','珞巴族','基诺族',
]

const healthOptions = ['健康','良好','一般','患有慢性疾病','身体残疾','其他']
const healthStatusOther = ref('')

const selectedProvince = ref('')
const selectedCity = ref('')
const selectedDistrict = ref('')

const cityList = computed(() => {
  if (!selectedProvince.value) return []
  const p = provinceData.find((p: any) => p.name === selectedProvince.value)
  return p ? p.children || [] : []
})

const districtList = computed(() => {
  if (!selectedCity.value) return []
  const c = cityList.value.find((c: any) => c.name === selectedCity.value)
  return c ? c.children || [] : []
})

watch(selectedProvince, () => {
  selectedCity.value = ''
  selectedDistrict.value = ''
  infoData.value.province = selectedProvince.value
  infoData.value.city = ''
  infoData.value.district = ''
})

watch(selectedCity, () => {
  selectedDistrict.value = ''
  infoData.value.city = selectedCity.value
  infoData.value.district = ''
})

watch(selectedDistrict, () => {
  infoData.value.district = selectedDistrict.value
})

watch(() => infoData.value.healthStatus, (val) => {
  if (val !== '其他') healthStatusOther.value = ''
})

const finalHealthStatus = computed(() => {
  if (infoData.value.healthStatus === '其他') return healthStatusOther.value || '其他'
  return infoData.value.healthStatus
})

async function loadInfo() {
  loading.value = true
  try {
    const userId = userStore.userInfo.id
    if (!userId) return
    const res: any = await getStudentInfo(userId)
    infoData.value = { ...infoData.value, ...res.data }
    selectedProvince.value = infoData.value.province || ''
    selectedCity.value = infoData.value.city || ''
    selectedDistrict.value = infoData.value.district || ''
    if (infoData.value.healthStatus && !healthOptions.includes(infoData.value.healthStatus)) {
      infoData.value.healthStatus = '其他'
      healthStatusOther.value = res.data.healthStatus
    }
  } catch {} finally { loading.value = false }
}

const showSaveToast = () => { saveSuccess.value = true; setTimeout(() => { saveSuccess.value = false }, 2000) }

async function saveBasicInfo() {
  if (!infoData.value.nation) { showModal('请选择民族'); return }
  if (!infoData.value.phone?.trim()) { showModal('请填写联系电话'); return }
  if (!/^1\d{10}$/.test(infoData.value.phone)) { showModal('手机号格式不正确', 'error'); return }
  saving.value = true
  try {
    await updateStudentInfo({ userId: userStore.userInfo.id, nation: infoData.value.nation, politicalStatus: infoData.value.politicalStatus, phone: infoData.value.phone })
    showSaveToast()
  } catch (err: any) { showModal(err.message || '保存失败', 'error') } finally { saving.value = false }
}

async function saveContactInfo() {
  if (!infoData.value.phone?.trim()) { showModal('请填写联系电话'); return }
  if (!/^1\d{10}$/.test(infoData.value.phone)) { showModal('手机号格式不正确', 'error'); return }
  if (!selectedProvince.value) { showModal('请选择省份'); return }
  saving.value = true
  try {
    await updateStudentInfo({ userId: userStore.userInfo.id, phone: infoData.value.phone, email: infoData.value.email, province: selectedProvince.value, city: selectedCity.value, district: selectedDistrict.value, healthStatus: finalHealthStatus.value })
    showSaveToast()
  } catch (err: any) { showModal(err.message || '保存失败', 'error') } finally { saving.value = false }
}

async function saveFamilyInfo() {
  if (!infoData.value.emergencyContactName?.trim()) { showModal('请填写紧急联系人姓名'); return }
  if (!infoData.value.emergencyContactRelation?.trim()) { showModal('请填写与联系人关系'); return }
  if (!infoData.value.emergencyContactPhone?.trim()) { showModal('请填写联系人电话'); return }
  if (!/^1\d{10}$/.test(infoData.value.emergencyContactPhone)) { showModal('联系人电话格式不正确', 'error'); return }
  saving.value = true
  try {
    await updateStudentInfo({ userId: userStore.userInfo.id, emergencyContactName: infoData.value.emergencyContactName, emergencyContactRelation: infoData.value.emergencyContactRelation, emergencyContactPhone: infoData.value.emergencyContactPhone })
    showSaveToast()
  } catch (err: any) { showModal(err.message || '保存失败', 'error') } finally { saving.value = false }
}

async function saveUniformSize() {
  if (!infoData.value.clothingSize) { showModal('请选择衣服尺码'); return }
  if (!infoData.value.shoeSize) { showModal('请输入鞋码'); return }
  const shoe = Number(infoData.value.shoeSize)
  if (shoe < 35 || shoe > 46) { showModal('鞋码范围为35-46', 'error'); return }
  saving.value = true
  try {
    await updateUniformSize({ userId: userStore.userInfo.id, clothingSize: infoData.value.clothingSize, shoeSize: infoData.value.shoeSize })
    showSaveToast()
  } catch (err: any) { showModal(err.message || '保存失败', 'error') } finally { saving.value = false }
}

// 证件照
const documents = ref<any[]>([])
const uploadingDocType = ref<number | null>(null)
const docTypes = [
  { type: 1, label: '身份证正面', icon: '🪪' },
  { type: 2, label: '身份证反面', icon: '🪪' },
  { type: 3, label: '个人证件照（二寸）', icon: '📷' },
  { type: 4, label: '其他证件', icon: '📄' },
]

async function loadDocuments() {
  try { const res: any = await request.get('/student/documents', { params: { userId: userStore.userInfo.id } }); documents.value = res.data || [] } catch {}
}
function getDocByType(type: number) { return documents.value.find(d => d.docType === type) }

async function uploadDoc(docType: number, event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) { showModal('仅支持图片文件', 'error'); return }
  if (file.size > 10 * 1024 * 1024) { showModal('文件大小不能超过10MB', 'error'); return }
  uploadingDocType.value = docType
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('docType', String(docType))
    await request.post(`/student/documents/upload?userId=${userStore.userInfo.id}`, formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    await loadDocuments()
  } catch (err: any) { showModal(err.message || '上传失败', 'error') } finally { uploadingDocType.value = null; if (input) input.value = '' }
}

async function removeDoc(id: number) {
  if (!(await window.$confirm?.('确定删除该证件图片吗？'))) return
  try { await request.delete(`/student/documents/${id}`); await loadDocuments() } catch (err: any) { showModal(err.message || '删除失败', 'error') }
}

onMounted(() => { loadInfo(); loadDocuments() })
</script>

<template>
  <div class="info-web">
    <div class="page-header">
      <h2>信息填报</h2>
      <p class="subtitle">请认真填写个人信息，带 <span class="required-mark">*</span> 号为必填项</p>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>

    <template v-else>
      <!-- 基础信息 -->
      <div class="card">
        <div class="card-header"><h3>基础信息</h3></div>
        <div class="form-grid">
          <div class="form-item readonly"><label>姓名</label><span>{{ infoData.name || '待导入' }}</span><span class="tag">仅查看</span></div>
          <div class="form-item readonly"><label>学号</label><span>{{ infoData.studentNo || '待导入' }}</span><span class="tag">仅查看</span></div>
          <div class="form-item readonly"><label>身份证号</label><span>{{ infoData.idCard || '待导入' }}</span><span class="tag">仅查看</span></div>
          <div class="form-item readonly"><label>性别</label><span>{{ infoData.gender === 1 ? '男' : infoData.gender === 2 ? '女' : '待导入' }}</span><span class="tag">仅查看</span></div>
          <div class="form-item"><label>民族 <span class="required">*</span></label>
            <select v-model="infoData.nation"><option value="" disabled>请选择民族</option><option v-for="n in nationList" :key="n" :value="n">{{ n }}</option></select>
          </div>
          <div class="form-item"><label>政治面貌</label>
            <select v-model="infoData.politicalStatus"><option value="">请选择</option><option value="群众">群众</option><option value="共青团员">共青团员</option><option value="中共党员">中共党员</option><option value="中共预备党员">中共预备党员</option></select>
          </div>
          <div class="form-item readonly"><label>学院</label><span>{{ infoData.collegeName || '待分配' }}</span></div>
          <div class="form-item readonly"><label>专业</label><span>{{ infoData.majorName || '待分配' }}</span></div>
          <div class="form-item readonly"><label>班级</label><span>{{ infoData.className || '待分配' }}</span></div>
        </div>
        <div class="card-footer"><button class="btn btn-primary" :disabled="saving" @click="saveBasicInfo">{{ saving ? '保存中...' : '保存基础信息' }}</button></div>
      </div>

      <!-- 联系方式 -->
      <div class="card">
        <div class="card-header"><h3>联系方式</h3></div>
        <div class="form-grid">
          <div class="form-item"><label>联系电话 <span class="required">*</span></label><input v-model="infoData.phone" placeholder="请输入11位手机号" maxlength="11" /></div>
          <div class="form-item"><label>邮箱</label><input v-model="infoData.email" placeholder="请输入邮箱（选填）" /></div>
          <div class="form-item"><label>省份 <span class="required">*</span></label>
            <select v-model="selectedProvince"><option value="" disabled>请选择省份</option><option v-for="p in provinceData" :key="p.name" :value="p.name">{{ p.name }}</option></select>
          </div>
          <div class="form-item"><label>城市</label>
            <select v-model="selectedCity" :disabled="!selectedProvince"><option value="" disabled>请选择城市</option><option v-for="c in cityList" :key="c.name" :value="c.name">{{ c.name }}</option></select>
          </div>
          <div class="form-item"><label>区县</label>
            <select v-model="selectedDistrict" :disabled="!selectedCity"><option value="" disabled>请选择区县</option><option v-for="d in districtList" :key="d.name" :value="d.name">{{ d.name }}</option></select>
          </div>
          <div class="form-item"><label>健康状况</label>
            <select v-model="infoData.healthStatus"><option value="" disabled>请选择</option><option v-for="h in healthOptions" :key="h" :value="h">{{ h }}</option></select>
          </div>
          <div class="form-item" v-if="infoData.healthStatus === '其他'"><label>具体情况 <span class="required">*</span></label><input v-model="healthStatusOther" placeholder="请填写健康状况" /></div>
        </div>
        <div class="card-footer"><button class="btn btn-primary" :disabled="saving" @click="saveContactInfo">{{ saving ? '保存中...' : '保存联系方式' }}</button></div>
      </div>

      <!-- 家庭资料 -->
      <div class="card">
        <div class="card-header"><h3>家庭资料</h3></div>
        <div class="form-grid">
          <div class="form-item"><label>紧急联系人 <span class="required">*</span></label><input v-model="infoData.emergencyContactName" placeholder="请输入姓名" /></div>
          <div class="form-item"><label>关系 <span class="required">*</span></label>
            <select v-model="infoData.emergencyContactRelation"><option value="" disabled>请选择</option><option value="父亲">父亲</option><option value="母亲">母亲</option><option value="兄弟姐妹">兄弟姐妹</option><option value="其他">其他</option></select>
          </div>
          <div class="form-item"><label>联系人电话 <span class="required">*</span></label><input v-model="infoData.emergencyContactPhone" placeholder="请输入11位手机号" maxlength="11" /></div>
        </div>
        <div class="card-footer"><button class="btn btn-primary" :disabled="saving" @click="saveFamilyInfo">{{ saving ? '保存中...' : '保存家庭资料' }}</button></div>
      </div>

      <!-- 军训服尺码 -->
      <div class="card">
        <div class="card-header"><h3>军训服尺码</h3><span class="card-hint">尺码将用于军训服发放</span></div>
        <div class="form-grid">
          <div class="form-item"><label>衣服尺码 <span class="required">*</span></label>
            <select v-model="infoData.clothingSize"><option value="" disabled>请选择</option><option value="S">S</option><option value="M">M</option><option value="L">L</option><option value="XL">XL</option><option value="XXL">XXL</option><option value="XXXL">XXXL</option></select>
          </div>
          <div class="form-item"><label>鞋码 <span class="required">*</span></label><input v-model="infoData.shoeSize" type="number" placeholder="35-46" min="35" max="46" /></div>
        </div>
        <div class="card-footer"><button class="btn btn-primary" :disabled="saving" @click="saveUniformSize">{{ saving ? '保存中...' : '保存尺码' }}</button></div>
      </div>

      <!-- 证件照片 -->
      <div class="card">
        <div class="card-header"><h3>证件照片</h3></div>
        <div class="doc-grid">
          <div v-for="d in docTypes" :key="d.type" class="doc-item">
            <div class="doc-preview" v-if="getDocByType(d.type)">
              <img :src="getDocByType(d.type).fileUrl" :alt="d.label" />
            </div>
            <div class="doc-placeholder" v-else>
              <span class="doc-icon">{{ d.icon }}</span>
            </div>
            <span class="doc-label">{{ d.label }}</span>
            <span :class="['doc-status', getDocByType(d.type) ? 'uploaded' : '']">{{ getDocByType(d.type) ? '已上传' : '待上传' }}</span>
            <div class="doc-actions">
              <label class="upload-link">
                <input type="file" accept="image/*" style="display:none" @change="uploadDoc(d.type, $event)" />
                {{ getDocByType(d.type) ? '更换' : '上传' }}
              </label>
              <span v-if="getDocByType(d.type)" class="remove-link" @click="removeDoc(getDocByType(d.type).id)">删除</span>
            </div>
          </div>
        </div>
      </div>
    </template>

    <AppAlert :show="showAlert" :message="alertMessage" :type="alertType" @update:show="showAlert = $event" />
  </div>
</template>

<style scoped>
.info-web { padding-bottom: 24px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: var(--gray-900); margin: 0 0 4px; }
.page-header .subtitle { font-size: 14px; color: var(--gray-400); margin: 0; }
.required-mark { color: var(--danger); }

.loading-state { display: flex; align-items: center; justify-content: center; min-height: 300px; color: var(--gray-400); gap: 12px; }
.loading-spinner { width: 28px; height: 28px; border: 3px solid var(--gray-200); border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.card { background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; overflow: hidden; margin-bottom: 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; border-bottom: 1px solid var(--gray-100); }
.card-header h3 { font-size: 15px; font-weight: 600; color: var(--gray-900); margin: 0; }
.card-hint { font-size: 12px; color: var(--gray-400); }
.card-footer { padding: 14px 20px; border-top: 1px solid var(--gray-100); text-align: right; }

.form-grid { padding: 16px 20px; display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
@media (max-width: 600px) { .form-grid { grid-template-columns: 1fr; } }
.form-item { display: flex; flex-direction: column; gap: 6px; }
.form-item.readonly { opacity: 0.7; }
.form-item label { font-size: 13px; font-weight: 500; color: var(--gray-600); }
.form-item label .required { color: var(--danger); }
.form-item input, .form-item select {
  padding: 9px 12px; border: 1.5px solid var(--gray-200); border-radius: 8px;
  font-size: 14px; color: var(--gray-900); outline: none; background: #fff;
}
.form-item input:focus, .form-item select:focus { border-color: var(--primary); }
.form-item span:not(.tag) { font-size: 14px; color: var(--gray-600); font-weight: 500; padding: 9px 0; }
.tag { font-size: 11px; padding: 2px 6px; border-radius: 4px; background: var(--gray-100); color: var(--gray-400); margin-left: auto; align-self: center; }

.btn { padding: 10px 24px; border: none; border-radius: 8px; font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s; }
.btn-primary { background: var(--primary); color: #fff; }
.btn-primary:hover:not(:disabled) { opacity: 0.9; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* 证件照 */
.doc-grid { padding: 16px 20px; display: grid; grid-template-columns: repeat(2, 1fr); gap: 14px; }
.doc-item { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 16px; border: 1px solid var(--gray-200); border-radius: 10px; text-align: center; }
.doc-preview { width: 80px; height: 80px; border-radius: 8px; overflow: hidden; border: 1px solid var(--gray-200); }
.doc-preview img { width: 100%; height: 100%; object-fit: cover; }
.doc-placeholder { width: 80px; height: 80px; border-radius: 8px; background: var(--gray-100); display: flex; align-items: center; justify-content: center; }
.doc-icon { font-size: 28px; }
.doc-label { font-size: 13px; font-weight: 500; color: var(--gray-600); }
.doc-status { font-size: 11px; }
.doc-status.uploaded { color: var(--success); }
.upload-link, .remove-link { font-size: 12px; cursor: pointer; }
.upload-link { color: var(--primary); }
.remove-link { color: var(--danger); margin-left: 8px; }
</style>
