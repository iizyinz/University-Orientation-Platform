<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { getStudentInfo, updateStudentInfo, updateUniformSize } from '@/api/student'
import { provinceData } from '@/utils/areaData'
import request from '@/utils/request'
import AppAlert from '@/components/AppAlert.vue'

// 证件照 API
function getDocuments(userId: number) {
  return request.get('/student/documents', { params: { userId } })
}
function deleteDocument(id: number) {
  return request.delete(`/student/documents/${id}`)
}

const userStore = useUserStore()
const router = useRouter()

// 弹窗状态
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

// 民族选择器
const showNationPicker = ref(false)
const nationSearch = ref('')

const filteredNationList = computed(() => {
  if (!nationSearch.value) return nationList
  return nationList.filter(n => n.includes(nationSearch.value))
})

function openNationPicker() {
  showNationPicker.value = true
  nationSearch.value = ''
}

function selectNation(nation: string) {
  infoData.value.nation = nation
  showNationPicker.value = false
}

// 政治面貌选择器
const showPoliticalPicker = ref(false)
const politicalOptions = ['群众', '共青团员', '中共党员', '中共预备党员']

function openPoliticalPicker() {
  showPoliticalPicker.value = true
}

function selectPolitical(status: string) {
  infoData.value.politicalStatus = status
  showPoliticalPicker.value = false
}

// 关系选择器
const showRelationPicker = ref(false)
const relationOptions = ['父亲', '母亲', '兄弟姐妹', '其他']

function openRelationPicker() {
  showRelationPicker.value = true
}

function selectRelation(relation: string) {
  infoData.value.emergencyContactRelation = relation
  showRelationPicker.value = false
}

// 衣服尺码选择器
const showClothingPicker = ref(false)
const clothingSizes = ['S', 'M', 'L', 'XL', 'XXL', 'XXXL']

function openClothingPicker() {
  showClothingPicker.value = true
}

function selectClothingSize(size: string) {
  infoData.value.clothingSize = size
  showClothingPicker.value = false
}

// 省市区选择器
const showProvincePicker = ref(false)
const showCityPicker = ref(false)
const showDistrictPicker = ref(false)
const provinceSearch = ref('')
const citySearch = ref('')
const districtSearch = ref('')

const filteredProvinceList = computed(() => {
  if (!provinceSearch.value) return provinceData
  return provinceData.filter((p: any) => p.name.includes(provinceSearch.value))
})

const filteredCityList = computed(() => {
  const p = provinceData.find((p: any) => p.name === selectedProvince.value)
  const cities = p ? p.children || [] : []
  if (!citySearch.value) return cities
  return cities.filter((c: any) => c.name.includes(citySearch.value))
})

const filteredDistrictList = computed(() => {
  const p = provinceData.find((p: any) => p.name === selectedProvince.value)
  const cities = p ? p.children || [] : []
  const c = cities.find((c: any) => c.name === selectedCity.value)
  const districts = c ? c.children || [] : []
  if (!districtSearch.value) return districts
  return districts.filter((d: any) => d.name.includes(districtSearch.value))
})

function openProvincePicker() {
  showProvincePicker.value = true
  provinceSearch.value = ''
}

function selectProvince(province: string) {
  selectedProvince.value = province
  selectedCity.value = ''
  selectedDistrict.value = ''
  infoData.value.province = province
  infoData.value.city = ''
  infoData.value.district = ''
  showProvincePicker.value = false
}

function openCityPicker() {
  if (!selectedProvince.value) return
  showCityPicker.value = true
  citySearch.value = ''
}

function selectCity(city: string) {
  selectedCity.value = city
  selectedDistrict.value = ''
  infoData.value.city = city
  infoData.value.district = ''
  showCityPicker.value = false
}

function openDistrictPicker() {
  if (!selectedCity.value) return
  showDistrictPicker.value = true
  districtSearch.value = ''
}

function selectDistrict(district: string) {
  selectedDistrict.value = district
  infoData.value.district = district
  showDistrictPicker.value = false
}

const infoData = ref({
  name: '', studentNo: '', idCard: '', gender: null as number | null,
  birthday: '', collegeName: '', majorName: '', className: '',
  reportStatus: 0, infoCompleted: 0,
  nation: '', politicalStatus: '',
  phone: '', email: '',
  province: '', city: '', district: '',
  healthStatus: '',
  emergencyContactName: '', emergencyContactRelation: '', emergencyContactPhone: '',
  clothingSize: '', shoeSize: '', hobby: '',
})

// 民族数据
const nationList = [
  '汉族','蒙古族','回族','藏族','维吾尔族','苗族','彝族','壮族','布依族','朝鲜族',
  '满族','侗族','瑶族','白族','土家族','哈尼族','哈萨克族','傣族','黎族','傈僳族',
  '佤族','畲族','高山族','拉祜族','水族','东乡族','纳西族','景颇族','柯尔克孜族','土族',
  '达斡尔族','仫佬族','羌族','布朗族','撒拉族','毛南族','仡佬族','锡伯族','阿昌族','普米族',
  '塔吉克族','怒族','乌孜别克族','俄罗斯族','鄂温克族','德昂族','保安族','裕固族','京族','塔塔尔族',
  '独龙族','鄂伦春族','赫哲族','门巴族','珞巴族','基诺族',
]

// 健康状况选项
const healthOptions = ['健康','良好','一般','患有慢性疾病','身体残疾','其他']
const healthStatusOther = ref('')

// 健康状况选择器
const showHealthPicker = ref(false)

function openHealthPicker() {
  showHealthPicker.value = true
}

function selectHealth(status: string) {
  infoData.value.healthStatus = status
  if (status !== '其他') healthStatusOther.value = ''
  showHealthPicker.value = false
}

// 省市区级联
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
  if (loading.value) return
  selectedCity.value = ''
  selectedDistrict.value = ''
  infoData.value.province = selectedProvince.value
  infoData.value.city = ''
  infoData.value.district = ''
}, { flush: 'sync' })

watch(selectedCity, () => {
  if (loading.value) return
  selectedDistrict.value = ''
  infoData.value.city = selectedCity.value
  infoData.value.district = ''
}, { flush: 'sync' })

watch(selectedDistrict, () => {
  infoData.value.district = selectedDistrict.value
})

// 健康状况 watch
watch(() => infoData.value.healthStatus, (val) => {
  if (val !== '其他') healthStatusOther.value = ''
})

// ===== 证件照 =====
const documents = ref<any[]>([])
const uploadingDocType = ref<number | null>(null)
const docTypes = [
  { type: 1, label: '身份证正面', icon: '🪪' },
  { type: 2, label: '身份证反面', icon: '🪪' },
  { type: 3, label: '个人证件照（二寸）', icon: '📷' },
  { type: 4, label: '其他证件', icon: '📄' },
]

async function loadDocuments() {
  try {
    const userId = userStore.userInfo.id
    const res: any = await getDocuments(userId)
    documents.value = res.data || []
  } catch {}
}

function getDocByType(type: number) {
  return documents.value.find(d => d.docType === type)
}

async function uploadDoc(docType: number, event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    showModal('仅支持图片文件', 'error')
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    showModal('文件大小不能超过10MB', 'error')
    return
  }

  uploadingDocType.value = docType
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('docType', String(docType))
    const userId = userStore.userInfo.id
    await request.post(`/student/documents/upload?userId=${userId}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    await loadDocuments()
  } catch (err: any) {
    showModal(err.message || '上传失败', 'error')
  } finally {
    uploadingDocType.value = null
    if (input) input.value = ''
  }
}

async function removeDoc(id: number) {
  if (!confirm('确定删除该证件图片吗？')) return
  try {
    await deleteDocument(id)
    await loadDocuments()
  } catch (err: any) {
    showModal(err.message || '删除失败', 'error')
  }
}

const finalHealthStatus = computed(() => {
  if (infoData.value.healthStatus === '其他') {
    return healthStatusOther.value || '其他'
  }
  return infoData.value.healthStatus
})

async function loadInfo() {
  loading.value = true
  try {
    const userId = userStore.userInfo.id
    if (!userId) return
    const res: any = await getStudentInfo(userId)
    infoData.value = { ...infoData.value, ...res.data }
    // 回填省市区
    selectedProvince.value = infoData.value.province || ''
    selectedCity.value = infoData.value.city || ''
    selectedDistrict.value = infoData.value.district || ''
    // 回填健康状况
    if (infoData.value.healthStatus && !healthOptions.includes(infoData.value.healthStatus)) {
      infoData.value.healthStatus = '其他'
      healthStatusOther.value = res.data.healthStatus
    }
  } catch {
  } finally {
    loading.value = false
  }
}

const showSaveToast = () => {
  saveSuccess.value = true
  setTimeout(() => { saveSuccess.value = false }, 2000)
}

async function saveBasicInfo() {
  if (!infoData.value.nation) { showModal('请选择民族'); return }
  if (!infoData.value.phone?.trim()) { showModal('请填写联系电话'); return }
  if (!/^1\d{10}$/.test(infoData.value.phone)) { showModal('手机号格式不正确'); return }
  saving.value = true
  try {
    await updateStudentInfo({
      userId: userStore.userInfo.id,
      nation: infoData.value.nation,
      politicalStatus: infoData.value.politicalStatus,
      phone: infoData.value.phone,
    })
    showSaveToast()
  } catch (err: any) {
    showModal(err.message || '保存失败', 'error')
  } finally {
    saving.value = false
  }
}

async function saveContactInfo() {
  if (!infoData.value.phone?.trim()) { showModal('请填写联系电话'); return }
  if (!/^1\d{10}$/.test(infoData.value.phone)) { showModal('手机号格式不正确'); return }
  if (!selectedProvince.value) { showModal('请选择省份'); return }
  saving.value = true
  try {
    await updateStudentInfo({
      userId: userStore.userInfo.id,
      phone: infoData.value.phone,
      email: infoData.value.email,
      province: selectedProvince.value,
      city: selectedCity.value,
      district: selectedDistrict.value,
      healthStatus: finalHealthStatus.value,
    })
    showSaveToast()
  } catch (err: any) {
    showModal(err.message || '保存失败', 'error')
  } finally {
    saving.value = false
  }
}

async function saveFamilyInfo() {
  if (!infoData.value.emergencyContactName?.trim()) { showModal('请填写紧急联系人姓名'); return }
  if (!infoData.value.emergencyContactRelation?.trim()) { showModal('请填写与联系人关系'); return }
  if (!infoData.value.emergencyContactPhone?.trim()) { showModal('请填写联系人电话'); return }
  if (!/^1\d{10}$/.test(infoData.value.emergencyContactPhone)) { showModal('联系人电话格式不正确'); return }
  saving.value = true
  try {
    await updateStudentInfo({
      userId: userStore.userInfo.id,
      emergencyContactName: infoData.value.emergencyContactName,
      emergencyContactRelation: infoData.value.emergencyContactRelation,
      emergencyContactPhone: infoData.value.emergencyContactPhone,
    })
    showSaveToast()
  } catch (err: any) {
    showModal(err.message || '保存失败', 'error')
  } finally {
    saving.value = false
  }
}

async function saveUniformSize() {
  if (!infoData.value.clothingSize) { showModal('请选择衣服尺码'); return }
  if (!infoData.value.shoeSize) { showModal('请输入鞋码'); return }
  const shoe = Number(infoData.value.shoeSize)
  if (shoe < 35 || shoe > 46) { showModal('鞋码范围为35-46'); return }
  saving.value = true
  try {
    await updateUniformSize({
      userId: userStore.userInfo.id,
      clothingSize: infoData.value.clothingSize,
      shoeSize: infoData.value.shoeSize,
    })
    showSaveToast()
  } catch (err: any) {
    showModal(err.message || '保存失败', 'error')
  } finally {
    saving.value = false
  }
}

const handleSave = () => {
  switch (activeSection.value) {
    case 'basic': saveBasicInfo(); break
    case 'contact': saveContactInfo(); break
    case 'family': saveFamilyInfo(); break
    case 'uniform': saveUniformSize(); break
  }
}

onMounted(() => {
  loadInfo()
  loadDocuments()
})
</script>

<template>
  <div class="student-info">
    <div class="page-header">
      <button class="back-btn" @click="$router.push({ path: '/student', query: { tab: 'work' } })">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
      <div>
        <h3>本人信息</h3>
        <p class="subtitle">请认真填写个人信息，带 * 号为必填项</p>
      </div>
    </div>

    <div class="section-tabs">
      <span :class="['tab', activeSection === 'basic' ? 'active' : '']" @click="activeSection = 'basic'">基础信息</span>
      <span :class="['tab', activeSection === 'contact' ? 'active' : '']" @click="activeSection = 'contact'">联系方式</span>
      <span :class="['tab', activeSection === 'family' ? 'active' : '']" @click="activeSection = 'family'">家庭资料</span>
      <span :class="['tab', activeSection === 'uniform' ? 'active' : '']" @click="activeSection = 'uniform'">军训服</span>
      <span :class="['tab', activeSection === 'document' ? 'active' : '']" @click="activeSection = 'document'">证件照</span>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>

    <template v-else>
      <!-- 基础信息 -->
      <div class="info-card" v-if="activeSection === 'basic'">
        <div class="readonly-row">
          <span class="label">姓名</span>
          <span class="value">{{ infoData.name || '待导入' }}</span>
          <span class="lock-tag">仅查看</span>
        </div>
        <div class="readonly-row">
          <span class="label">学号</span>
          <span class="value">{{ infoData.studentNo || '待导入' }}</span>
          <span class="lock-tag">仅查看</span>
        </div>
        <div class="readonly-row">
          <span class="label">身份证号</span>
          <span class="value">{{ infoData.idCard || '待导入' }}</span>
          <span class="lock-tag">仅查看</span>
        </div>
        <div class="readonly-row">
          <span class="label">性别</span>
          <span class="value">{{ infoData.gender === 1 ? '男' : infoData.gender === 2 ? '女' : '待导入' }}</span>
          <span class="lock-tag">仅查看</span>
        </div>
        <div class="form-row form-row-clickable" @click="openNationPicker">
          <span class="label required">民族</span>
          <span class="value" :class="{ placeholder: !infoData.nation }">
            {{ infoData.nation || '请选择民族' }}
          </span>
          <svg class="select-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="6 9 12 15 18 9"/>
          </svg>
        </div>
        <div class="form-row form-row-clickable" @click="openPoliticalPicker">
          <span class="label">政治面貌</span>
          <span class="value" :class="{ placeholder: !infoData.politicalStatus }">
            {{ infoData.politicalStatus || '请选择' }}
          </span>
          <svg class="select-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="6 9 12 15 18 9"/>
          </svg>
        </div>
        <div class="readonly-row">
          <span class="label">学院</span>
          <span class="value">{{ infoData.collegeName || '待分配' }}</span>
        </div>
        <div class="readonly-row">
          <span class="label">专业</span>
          <span class="value">{{ infoData.majorName || '待分配' }}</span>
        </div>
        <div class="readonly-row">
          <span class="label">班级</span>
          <span class="value">{{ infoData.className || '待分配' }}</span>
        </div>
      </div>

      <!-- 联系方式 -->
      <div class="info-card" v-if="activeSection === 'contact'">
        <div class="form-row">
          <span class="label required">联系电话</span>
          <input v-model="infoData.phone" placeholder="请输入11位手机号" maxlength="11" />
        </div>
        <div class="form-row">
          <span class="label">邮箱</span>
          <input v-model="infoData.email" placeholder="请输入邮箱（选填）" />
        </div>
        <div class="form-row form-row-clickable" @click="openProvincePicker">
          <span class="label required">省份</span>
          <span class="value" :class="{ placeholder: !selectedProvince }">
            {{ selectedProvince || '请选择省份' }}
          </span>
          <svg class="select-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="6 9 12 15 18 9"/>
          </svg>
        </div>
        <div class="form-row form-row-clickable" :class="{ disabled: !selectedProvince }" @click="selectedProvince && openCityPicker()">
          <span class="label">城市</span>
          <span class="value" :class="{ placeholder: !selectedCity, disabled: !selectedProvince }">
            {{ selectedCity || '请选择城市' }}
          </span>
          <svg class="select-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="6 9 12 15 18 9"/>
          </svg>
        </div>
        <div class="form-row form-row-clickable" :class="{ disabled: !selectedCity }" @click="selectedCity && openDistrictPicker()">
          <span class="label">区县</span>
          <span class="value" :class="{ placeholder: !selectedDistrict, disabled: !selectedCity }">
            {{ selectedDistrict || '请选择区县' }}
          </span>
          <svg class="select-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="6 9 12 15 18 9"/>
          </svg>
        </div>
        <div class="form-row form-row-clickable" @click="openHealthPicker">
          <span class="label">健康状况</span>
          <span class="value" :class="{ placeholder: !infoData.healthStatus }">
            {{ infoData.healthStatus || '请选择' }}
          </span>
          <svg class="select-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="6 9 12 15 18 9"/>
          </svg>
        </div>
        <div class="form-row" v-if="infoData.healthStatus === '其他'">
          <span class="label required">具体情况</span>
          <input v-model="healthStatusOther" placeholder="请填写健康状况" />
        </div>
      </div>

      <!-- 家庭资料 -->
      <div class="info-card" v-if="activeSection === 'family'">
        <div class="form-row">
          <span class="label required">紧急联系人</span>
          <input v-model="infoData.emergencyContactName" placeholder="请输入姓名" />
        </div>
        <div class="form-row form-row-clickable" @click="openRelationPicker">
          <span class="label required">关系</span>
          <span class="value" :class="{ placeholder: !infoData.emergencyContactRelation }">
            {{ infoData.emergencyContactRelation || '请选择' }}
          </span>
          <svg class="select-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="6 9 12 15 18 9"/>
          </svg>
        </div>
        <div class="form-row">
          <span class="label required">联系人电话</span>
          <input v-model="infoData.emergencyContactPhone" placeholder="请输入11位手机号" maxlength="11" />
        </div>
      </div>

      <!-- 军训服 -->
      <div class="info-card" v-if="activeSection === 'uniform'">
        <div class="form-row form-row-clickable" @click="openClothingPicker">
          <span class="label required">衣服尺码</span>
          <span class="value" :class="{ placeholder: !infoData.clothingSize }">
            {{ infoData.clothingSize || '请选择' }}
          </span>
          <svg class="select-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="6 9 12 15 18 9"/>
          </svg>
        </div>
        <div class="form-row">
          <span class="label required">鞋码</span>
          <input v-model="infoData.shoeSize" type="number" placeholder="请输入鞋码（35-46）" min="35" max="46" />
        </div>
        <p class="tip">尺码信息将用于军训服发放，请认真填写</p>
      </div>

      <!-- 证件照 -->
      <div v-if="activeSection === 'document'" class="doc-section">
        <div v-for="d in docTypes" :key="d.type" class="doc-card">
          <div class="doc-info">
            <span class="doc-icon">{{ d.icon }}</span>
            <div class="doc-text">
              <span class="doc-label">{{ d.label }}</span>
              <span v-if="getDocByType(d.type)" class="doc-status uploaded">已上传</span>
              <span v-else class="doc-status pending">待上传</span>
            </div>
          </div>
          <div class="doc-actions">
            <div v-if="getDocByType(d.type)" class="doc-preview">
              <img :src="getDocByType(d.type).fileUrl" :alt="d.label" />
            </div>
            <label class="upload-btn" :class="{ uploading: uploadingDocType === d.type }">
              <input type="file" accept="image/*" style="display:none" @change="uploadDoc(d.type, $event)" />
              <span v-if="uploadingDocType === d.type">上传中...</span>
              <span v-else>{{ getDocByType(d.type) ? '更换' : '上传' }}</span>
            </label>
            <button v-if="getDocByType(d.type)" class="remove-btn" @click="removeDoc(getDocByType(d.type).id)">删除</button>
          </div>
        </div>
        <p class="tip">支持 JPG、PNG 格式，单张不超过 10MB</p>
      </div>

      <button class="save-btn" :disabled="saving" @click="handleSave" v-if="activeSection !== 'document'">
        {{ saving ? '保存中...' : '保存信息' }}
      </button>
    </template>

    <teleport to="body">
      <div v-if="saveSuccess" class="success-toast">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5">
          <polyline points="20 6 9 17 4 12"/>
        </svg>
        保存成功
      </div>
    </teleport>

    <AppAlert :show="showAlert" :message="alertMessage" :type="alertType" @update:show="showAlert = $event" />

    <!-- 民族选择器 -->
    <teleport to="body">
      <div v-if="showNationPicker" class="picker-overlay" @click="showNationPicker = false">
        <div class="picker-modal" @click.stop>
          <div class="picker-header">
            <span class="picker-title">选择民族</span>
            <button class="picker-close" @click="showNationPicker = false">×</button>
          </div>
          <div class="picker-search">
            <input v-model="nationSearch" placeholder="搜索民族" />
          </div>
          <div class="picker-list">
            <div
              v-for="n in filteredNationList"
              :key="n"
              class="picker-item"
              :class="{ active: infoData.nation === n }"
              @click="selectNation(n)"
            >
              {{ n }}
              <svg v-if="infoData.nation === n" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
            </div>
            <div v-if="filteredNationList.length === 0" class="picker-empty">无匹配结果</div>
          </div>
        </div>
      </div>

      <!-- 政治面貌选择器 -->
      <div v-if="showPoliticalPicker" class="picker-overlay" @click="showPoliticalPicker = false">
        <div class="picker-modal" @click.stop>
          <div class="picker-header">
            <span class="picker-title">选择政治面貌</span>
            <button class="picker-close" @click="showPoliticalPicker = false">×</button>
          </div>
          <div class="picker-list">
            <div
              v-for="p in politicalOptions"
              :key="p"
              class="picker-item"
              :class="{ active: infoData.politicalStatus === p }"
              @click="selectPolitical(p)"
            >
              {{ p }}
              <svg v-if="infoData.politicalStatus === p" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- 健康状况选择器 -->
      <div v-if="showHealthPicker" class="picker-overlay" @click="showHealthPicker = false">
        <div class="picker-modal" @click.stop>
          <div class="picker-header">
            <span class="picker-title">选择健康状况</span>
            <button class="picker-close" @click="showHealthPicker = false">×</button>
          </div>
          <div class="picker-list">
            <div
              v-for="h in healthOptions"
              :key="h"
              class="picker-item"
              :class="{ active: infoData.healthStatus === h }"
              @click="selectHealth(h)"
            >
              {{ h }}
              <svg v-if="infoData.healthStatus === h" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- 省份选择器 -->
      <div v-if="showProvincePicker" class="picker-overlay" @click="showProvincePicker = false">
        <div class="picker-modal" @click.stop>
          <div class="picker-header">
            <span class="picker-title">选择省份</span>
            <button class="picker-close" @click="showProvincePicker = false">×</button>
          </div>
          <div class="picker-search">
            <input v-model="provinceSearch" placeholder="搜索省份" />
          </div>
          <div class="picker-list">
            <div
              v-for="p in filteredProvinceList"
              :key="p.name"
              class="picker-item"
              :class="{ active: selectedProvince === p.name }"
              @click="selectProvince(p.name)"
            >
              {{ p.name }}
              <svg v-if="selectedProvince === p.name" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- 城市选择器 -->
      <div v-if="showCityPicker" class="picker-overlay" @click="showCityPicker = false">
        <div class="picker-modal" @click.stop>
          <div class="picker-header">
            <span class="picker-title">选择城市</span>
            <button class="picker-close" @click="showCityPicker = false">×</button>
          </div>
          <div class="picker-search">
            <input v-model="citySearch" placeholder="搜索城市" />
          </div>
          <div class="picker-list">
            <div
              v-for="c in filteredCityList"
              :key="c.name"
              class="picker-item"
              :class="{ active: selectedCity === c.name }"
              @click="selectCity(c.name)"
            >
              {{ c.name }}
              <svg v-if="selectedCity === c.name" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- 区县选择器 -->
      <div v-if="showDistrictPicker" class="picker-overlay" @click="showDistrictPicker = false">
        <div class="picker-modal" @click.stop>
          <div class="picker-header">
            <span class="picker-title">选择区县</span>
            <button class="picker-close" @click="showDistrictPicker = false">×</button>
          </div>
          <div class="picker-search">
            <input v-model="districtSearch" placeholder="搜索区县" />
          </div>
          <div class="picker-list">
            <div
              v-for="d in filteredDistrictList"
              :key="d.name"
              class="picker-item"
              :class="{ active: selectedDistrict === d.name }"
              @click="selectDistrict(d.name)"
            >
              {{ d.name }}
              <svg v-if="selectedDistrict === d.name" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- 关系选择器 -->
      <div v-if="showRelationPicker" class="picker-overlay" @click="showRelationPicker = false">
        <div class="picker-modal" @click.stop>
          <div class="picker-header">
            <span class="picker-title">选择关系</span>
            <button class="picker-close" @click="showRelationPicker = false">×</button>
          </div>
          <div class="picker-list">
            <div
              v-for="r in relationOptions"
              :key="r"
              class="picker-item"
              :class="{ active: infoData.emergencyContactRelation === r }"
              @click="selectRelation(r)"
            >
              {{ r }}
              <svg v-if="infoData.emergencyContactRelation === r" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- 衣服尺码选择器 -->
      <div v-if="showClothingPicker" class="picker-overlay" @click="showClothingPicker = false">
        <div class="picker-modal" @click.stop>
          <div class="picker-header">
            <span class="picker-title">选择衣服尺码</span>
            <button class="picker-close" @click="showClothingPicker = false">×</button>
          </div>
          <div class="picker-list">
            <div
              v-for="s in clothingSizes"
              :key="s"
              class="picker-item"
              :class="{ active: infoData.clothingSize === s }"
              @click="selectClothingSize(s)"
            >
              {{ s }}
              <svg v-if="infoData.clothingSize === s" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
            </div>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.student-info { padding-bottom: 8px; }
.page-header { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
.back-btn {
  width: 36px; height: 36px; border-radius: 10px; border: 1px solid var(--gray-200);
  background: #fff; display: flex; align-items: center; justify-content: center;
  cursor: pointer; flex-shrink: 0; color: var(--gray-600); transition: background 0.15s;
}
.back-btn:active { background: var(--gray-100); }
.page-header h3 { font-size: 18px; font-weight: 700; color: var(--gray-900); margin: 0; }
.page-header .subtitle { font-size: 13px; color: var(--gray-400); margin: 0; }

.loading-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  min-height: 200px; color: var(--gray-400); gap: 12px;
}
.loading-spinner {
  width: 28px; height: 28px; border: 3px solid var(--gray-200);
  border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.section-tabs {
  display: flex; gap: 8px; margin-bottom: 16px; overflow-x: auto;
  -webkit-overflow-scrolling: touch; scrollbar-width: none;
}
.section-tabs::-webkit-scrollbar { display: none; }
.tab {
  padding: 7px 16px; border-radius: 20px; font-size: 13px; font-weight: 500;
  white-space: nowrap; cursor: pointer; transition: all 0.2s;
  background: #fff; color: var(--gray-600); border: 1px solid var(--gray-200);
}
.tab.active { background: var(--primary); color: #fff; border-color: var(--primary); }

.info-card {
  background: #fff; border-radius: 14px; padding: 4px 0;
  border: 1px solid var(--gray-200); margin-bottom: 16px;
}

.readonly-row, .form-row {
  display: flex; align-items: center; padding: 14px 18px;
  border-bottom: 1px solid var(--gray-100);
}
.readonly-row:last-child, .form-row:last-child { border-bottom: none; }

.label {
  width: 80px; flex-shrink: 0; font-size: 13px; color: var(--gray-400);
}
.label.required::before { content: '* '; color: var(--danger); }
.lock-tag {
  font-size: 11px; padding: 1px 6px; border-radius: 4px;
  background: var(--gray-100); color: var(--gray-400); margin-left: auto;
}
.value { font-size: 14px; color: var(--gray-600); font-weight: 500; }

.form-row input, .form-row select {
  flex: 1; border: none; outline: none; font-size: 14px; color: var(--gray-600);
  text-align: right; background: transparent;
}
.form-row input::placeholder { color: var(--gray-400); }
.form-row select {
  cursor: pointer; text-align: right;
  padding-right: 4px;
}
.form-row select:disabled { color: var(--gray-400); opacity: 0.5; }

/* 可点击行 */
.form-row-clickable {
  cursor: pointer;
  user-select: none;
  transition: background 0.15s;
}
.form-row-clickable:active {
  background: var(--gray-50);
}
.form-row-clickable.disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
.form-row-clickable.disabled:active {
  background: transparent;
}
.form-row-clickable .select-arrow {
  margin-left: auto;
  color: var(--gray-400);
  flex-shrink: 0;
}
.form-row-clickable .value.placeholder {
  color: var(--gray-400);
}
.form-row-clickable .value.disabled {
  color: var(--gray-400);
  opacity: 0.5;
}

.tip { font-size: 12px; color: var(--secondary-orange); padding: 0 18px 14px; margin: 0; }

/* 可点击行 */
.form-row-clickable {
  cursor: pointer;
  user-select: none;
  transition: background 0.15s;
}
.form-row-clickable:active {
  background: var(--gray-50);
}
.form-row-clickable .select-arrow {
  margin-left: auto;
  color: var(--gray-400);
  flex-shrink: 0;
}
.form-row-clickable .value.placeholder {
  color: var(--gray-400);
}

/* 弹出式选择器 */
.picker-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.picker-modal {
  width: 100%;
  max-height: 70vh;
  background: #fff;
  border-radius: 16px 16px 0 0;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease;
}
@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}
.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--gray-100);
}
.picker-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--gray-900);
}
.picker-close {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: var(--gray-100);
  color: var(--gray-600);
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.picker-close:active {
  background: var(--gray-200);
}
.picker-search {
  padding: 12px 20px;
  border-bottom: 1px solid var(--gray-100);
}
.picker-search input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--gray-200);
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
}
.picker-search input:focus {
  border-color: var(--primary);
}
.picker-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
  -webkit-overflow-scrolling: touch;
}
.picker-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  font-size: 15px;
  color: var(--gray-700);
  cursor: pointer;
  transition: background 0.15s;
}
.picker-item:active {
  background: var(--gray-50);
}
.picker-item.active {
  color: var(--primary);
  font-weight: 500;
}
.picker-item svg {
  color: var(--primary);
}
.picker-empty {
  padding: 40px 20px;
  text-align: center;
  color: var(--gray-400);
  font-size: 14px;
}

/* Document Upload */
.doc-section { display: flex; flex-direction: column; gap: 12px; }
.doc-card {
  background: #fff; border: 1px solid var(--gray-200); border-radius: 12px;
  padding: 14px 16px; display: flex; align-items: center; justify-content: space-between;
}
.doc-info { display: flex; align-items: center; gap: 12px; }
.doc-icon { font-size: 24px; }
.doc-text { display: flex; flex-direction: column; }
.doc-label { font-size: 14px; font-weight: 500; color: var(--gray-900); }
.doc-status { font-size: 12px; }
.doc-status.uploaded { color: var(--success); }
.doc-status.pending { color: var(--gray-400); }

.doc-actions { display: flex; align-items: center; gap: 8px; }
.doc-preview {
  width: 40px; height: 40px; border-radius: 6px; overflow: hidden;
  border: 1px solid var(--gray-200); flex-shrink: 0;
}
.doc-preview img { width: 100%; height: 100%; object-fit: cover; }

.upload-btn {
  padding: 6px 14px; border-radius: 6px; border: none;
  background: var(--primary); color: #fff; font-size: 12px; font-weight: 500;
  cursor: pointer; transition: opacity 0.2s;
}
.upload-btn.uploading { opacity: 0.6; cursor: not-allowed; }

.remove-btn {
  padding: 6px 10px; border-radius: 6px; border: 1px solid var(--gray-200);
  background: #fff; color: var(--danger); font-size: 12px; cursor: pointer;
}
.remove-btn:hover { background: #FFF0F0; }

.save-btn {
  width: 100%; padding: 14px; border: none; border-radius: 12px;
  background: var(--primary); color: #fff; font-size: 15px; font-weight: 600;
  cursor: pointer; transition: opacity 0.2s;
}
.save-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.save-btn:active:not(:disabled) { opacity: 0.85; }

.success-toast {
  position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%);
  background: var(--success); color: #fff; padding: 14px 28px;
  border-radius: 12px; display: flex; align-items: center; gap: 8px;
  font-size: 15px; font-weight: 600; box-shadow: 0 8px 32px rgba(0,201,167,0.3);
  z-index: 1001; animation: fadeInUp 0.3s ease;
}
@keyframes fadeInUp { from { opacity: 0; transform: translate(-50%, -40%); } to { opacity: 1; transform: translate(-50%, -50%); } }
</style>
