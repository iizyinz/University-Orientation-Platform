<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 全屏
const isFullscreen = ref(false)

function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

function onFullscreenChange() {
  isFullscreen.value = !!document.fullscreenElement
}

// 实时时间
const currentTime = ref('')
let timeTimer: any = null

function updateTime() {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const h = String(now.getHours()).padStart(2, '0')
  const min = String(now.getMinutes()).padStart(2, '0')
  const s = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${y}-${m}-${d} ${h}:${min}:${s}`
}

// 统计数据
const stats = ref({
  totalStudents: 0,
  reportedStudents: 0,
  unreportedStudents: 0,
  reportRate: 0,
  dormRate: 0,
  totalBeds: 0,
  usedBeds: 0,
})

// 图表数据
const collegeData = ref<any[]>([])
const genderData = ref<any[]>([])
const dormData = ref<any[]>([])
const majorData = ref<any[]>([])

// ECharts 实例
const collegeChartRef = ref<HTMLDivElement>()
const genderChartRef = ref<HTMLDivElement>()
const dormChartRef = ref<HTMLDivElement>()
const majorChartRef = ref<HTMLDivElement>()
let collegeChart: echarts.ECharts | null = null
let genderChart: echarts.ECharts | null = null
let dormChart: echarts.ECharts | null = null
let majorChart: echarts.ECharts | null = null

// 加载数据
async function loadData() {
  try {
    const [statsRes, collegeRes, genderRes, dormRes, majorRes]: any = await Promise.all([
      fetch('/api/dashboard/stats').then(r => r.json()),
      fetch('/api/dashboard/chart/college-distribution').then(r => r.json()),
      fetch('/api/dashboard/chart/gender-distribution').then(r => r.json()),
      fetch('/api/dashboard/chart/dorm-occupancy').then(r => r.json()),
      fetch('/api/dashboard/chart/report-trend').then(r => r.json()),
    ])
    stats.value = statsRes.data || {}
    collegeData.value = collegeRes.data || []
    genderData.value = genderRes.data || []
    dormData.value = dormRes.data || []
    majorData.value = majorRes.data || []

    initCharts()
  } catch (err) {
    console.error('加载数据失败:', err)
  }
}

function initCharts() {
  // 学院分布饼图
  if (collegeChartRef.value) {
    collegeChart = echarts.init(collegeChartRef.value)
    collegeChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
      legend: { orient: 'vertical', right: 10, top: 'center', textStyle: { color: '#8C95A6', fontSize: 12 } },
      color: ['#1A5CFF', '#00C9A7', '#FF7A1A', '#FFAA4C', '#8250FF', '#FF4D6A', '#4D8AFF', '#0033CC'],
      series: [{
        type: 'pie', radius: ['40%', '70%'], center: ['35%', '50%'],
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        data: collegeData.value.map((d: any) => ({ name: d.name || '未分配', value: d.value || 0 })),
      }],
    })
  }

  // 性别分布环形图
  if (genderChartRef.value) {
    genderChart = echarts.init(genderChartRef.value)
    genderChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
      color: ['#1A5CFF', '#FF4D6A'],
      series: [{
        type: 'pie', radius: ['50%', '75%'], center: ['50%', '50%'],
        label: { show: true, formatter: '{b}\n{d}%', fontSize: 12 },
        data: genderData.value.map((d: any) => ({ name: d.name || '未知', value: d.value || 0 })),
      }],
    })
  }

  // 宿舍入住率柱状图
  if (dormChartRef.value) {
    dormChart = echarts.init(dormChartRef.value)
    dormChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { data: ['总床位', '已入住'], textStyle: { color: '#8C95A6', fontSize: 12 } },
      grid: { left: 60, right: 20, bottom: 30, top: 40 },
      xAxis: { type: 'category', data: dormData.value.map((d: any) => d.name || ''), axisLabel: { color: '#8C95A6', fontSize: 11 } },
      yAxis: { type: 'value', axisLabel: { color: '#8C95A6', fontSize: 11 }, splitLine: { lineStyle: { color: '#E8ECF1' } } },
      series: [
        { name: '总床位', type: 'bar', barWidth: 20, itemStyle: { color: 'rgba(26,92,255,0.2)', borderRadius: [4, 4, 0, 0] }, data: dormData.value.map((d: any) => d.total || 0) },
        { name: '已入住', type: 'bar', barWidth: 20, itemStyle: { color: '#1A5CFF', borderRadius: [4, 4, 0, 0] }, data: dormData.value.map((d: any) => d.used || 0) },
      ],
    })
  }

  // 各专业报到进度
  if (majorChartRef.value) {
    majorChart = echarts.init(majorChartRef.value)
    const top10 = majorData.value.slice(0, 10)
    majorChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { data: ['已报到', '未报到'], textStyle: { color: '#8C95A6', fontSize: 12 } },
      grid: { left: 100, right: 20, bottom: 30, top: 40 },
      xAxis: { type: 'value', axisLabel: { color: '#8C95A6', fontSize: 11 }, splitLine: { lineStyle: { color: '#E8ECF1' } } },
      yAxis: { type: 'category', data: top10.map((d: any) => d.name || '').reverse(), axisLabel: { color: '#8C95A6', fontSize: 11 } },
      series: [
        { name: '已报到', type: 'bar', stack: 'total', barWidth: 16, itemStyle: { color: '#00C9A7', borderRadius: [0, 0, 0, 0] }, data: top10.map((d: any) => d.reported || 0).reverse() },
        { name: '未报到', type: 'bar', stack: 'total', barWidth: 16, itemStyle: { color: 'rgba(26,92,255,0.2)', borderRadius: [0, 4, 4, 0] }, data: top10.map((d: any) => (d.total || 0) - (d.reported || 0)).reverse() },
      ],
    })
  }
}

function handleResize() {
  collegeChart?.resize()
  genderChart?.resize()
  dormChart?.resize()
  majorChart?.resize()
}

onMounted(() => {
  updateTime()
  timeTimer = setInterval(updateTime, 1000)
  loadData()
  window.addEventListener('resize', handleResize)
  document.addEventListener('fullscreenchange', onFullscreenChange)
})

onBeforeUnmount(() => {
  clearInterval(timeTimer)
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('fullscreenchange', onFullscreenChange)
  collegeChart?.dispose()
  genderChart?.dispose()
  dormChart?.dispose()
  majorChart?.dispose()
})
</script>

<template>
  <div class="dashboard-screen">
    <!-- 顶部栏 -->
    <header class="screen-header">
      <div class="header-left">
        <div class="logo-icon">
          <svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M8 22L16 8L24 22" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="16" cy="11" r="2" fill="#FF7A1A"/>
            <path d="M11 19H21" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </div>
        <span class="logo-text">光谷启航</span>
      </div>
      <h1 class="header-title">迎新数据分析平台</h1>
      <div class="header-right">
        <button class="back-btn" @click="router.push('/admin')" title="返回管理后台">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="15 18 9 12 15 6"/>
          </svg>
        </button>
        <button class="fullscreen-btn" @click="toggleFullscreen" :title="isFullscreen ? '退出全屏' : '全屏显示'">
          <svg v-if="!isFullscreen" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="15 3 21 3 21 9"/><polyline points="9 21 3 21 3 15"/>
            <line x1="21" y1="3" x2="14" y2="10"/><line x1="3" y1="21" x2="10" y2="14"/>
          </svg>
          <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="4 14 10 14 10 20"/><polyline points="20 10 14 10 14 4"/>
            <line x1="14" y1="10" x2="21" y2="3"/><line x1="3" y1="21" x2="10" y2="14"/>
          </svg>
        </button>
        <span class="time-display">{{ currentTime }}</span>
      </div>
    </header>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card blue">
        <div class="stat-icon"><svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
        <div class="stat-info"><span class="stat-value">{{ stats.totalStudents }}</span><span class="stat-label">新生总数</span></div>
      </div>
      <div class="stat-card green">
        <div class="stat-icon"><svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></div>
        <div class="stat-info"><span class="stat-value">{{ stats.reportedStudents }}</span><span class="stat-label">已报到 ({{ stats.reportRate }}%)</span></div>
      </div>
      <div class="stat-card orange">
        <div class="stat-icon"><svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></div>
        <div class="stat-info"><span class="stat-value">{{ stats.unreportedStudents }}</span><span class="stat-label">未报到</span></div>
      </div>
      <div class="stat-card purple">
        <div class="stat-icon"><svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><path d="M9 22V12h6v10"/></svg></div>
        <div class="stat-info"><span class="stat-value">{{ stats.dormRate }}%</span><span class="stat-label">宿舍入住率</span></div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <div class="chart-card">
        <h3>学院新生分布</h3>
        <div ref="collegeChartRef" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <h3>性别分布</h3>
        <div ref="genderChartRef" class="chart-box"></div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card wide">
        <h3>各楼栋宿舍入住情况</h3>
        <div ref="dormChartRef" class="chart-box"></div>
      </div>
      <div class="chart-card wide">
        <h3>各专业报到进度</h3>
        <div ref="majorChartRef" class="chart-box"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-screen {
  width: 100vw; height: 100vh; overflow: hidden;
  background: linear-gradient(135deg, #0a1628 0%, #0f2847 50%, #0a1628 100%);
  display: flex; flex-direction: column; color: #fff;
}

/* 顶部栏 */
.screen-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 32px; background: rgba(255,255,255,0.05);
  border-bottom: 1px solid rgba(255,255,255,0.08); flex-shrink: 0;
}
.header-left { display: flex; align-items: center; gap: 12px; }
.logo-icon { width: 32px; height: 32px; }
.logo-icon svg { width: 100%; height: 100%; }
.logo-text { font-size: 16px; font-weight: 700; color: #fff; }
.header-title {
  font-size: 22px; font-weight: 700; letter-spacing: 4px;
  background: linear-gradient(90deg, #4D8AFF, #00C9A7);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
.header-right { display: flex; align-items: center; gap: 16px; }
.fullscreen-btn {
  display: flex; align-items: center; justify-content: center;
  width: 36px; height: 36px; border-radius: 8px; border: 1px solid rgba(255,255,255,0.15);
  background: rgba(255,255,255,0.06); color: rgba(255,255,255,0.7);
  cursor: pointer; transition: all 0.2s;
}
.fullscreen-btn:hover { background: rgba(255,255,255,0.12); color: #fff; }
.back-btn {
  display: flex; align-items: center; justify-content: center;
  width: 36px; height: 36px; border-radius: 8px; border: 1px solid rgba(255,255,255,0.15);
  background: rgba(255,255,255,0.06); color: rgba(255,255,255,0.7);
  cursor: pointer; transition: all 0.2s;
}
.back-btn:hover { background: rgba(255,255,255,0.12); color: #fff; }
.time-display {
  font-size: 14px; color: rgba(255,255,255,0.7);
  font-family: 'Courier New', monospace; letter-spacing: 1px;
}

/* 统计卡片 */
.stats-row {
  display: grid; grid-template-columns: repeat(4, 1fr);
  gap: 16px; padding: 20px 32px; flex-shrink: 0;
}
.stat-card {
  display: flex; align-items: center; gap: 14px;
  padding: 18px 20px; border-radius: 12px;
  background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.08);
  backdrop-filter: blur(10px);
}
.stat-icon {
  width: 48px; height: 48px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.stat-card.blue .stat-icon { background: rgba(26,92,255,0.2); color: #4D8AFF; }
.stat-card.green .stat-icon { background: rgba(0,201,167,0.2); color: #00C9A7; }
.stat-card.orange .stat-icon { background: rgba(255,122,26,0.2); color: #FF7A1A; }
.stat-card.purple .stat-icon { background: rgba(130,80,255,0.2); color: #8250FF; }
.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 28px; font-weight: 700; color: #fff; }
.stat-label { font-size: 13px; color: rgba(255,255,255,0.5); margin-top: 2px; }

/* 图表区域 */
.charts-row {
  display: grid; grid-template-columns: 1fr 1fr;
  gap: 16px; padding: 0 32px 20px; flex: 1; min-height: 0;
}
.chart-card {
  background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.08);
  border-radius: 12px; padding: 16px; display: flex; flex-direction: column;
  backdrop-filter: blur(10px);
}
.chart-card h3 {
  font-size: 14px; font-weight: 600; color: rgba(255,255,255,0.8);
  margin: 0 0 12px; padding-left: 8px;
  border-left: 3px solid #4D8AFF;
}
.chart-box { flex: 1; min-height: 200px; }
</style>
