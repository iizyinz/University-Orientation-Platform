<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getFaqCategories, getFaqByCategory, searchFaq } from '@/api/discover'

const categories = ref<any[]>([])
const faqList = ref<any[]>([])
const activeCategory = ref<number | null>(null)
const searchKeyword = ref('')
const expandedId = ref<number | null>(null)
const loading = ref(false)

async function loadCategories() {
  try { const res: any = await getFaqCategories(); categories.value = (res.data || []).filter((c: any) => c.status === 1) } catch {}
}

async function loadByCategory(categoryId: number) {
  activeCategory.value = categoryId; searchKeyword.value = ''; loading.value = true
  try { const res: any = await getFaqByCategory(categoryId); faqList.value = (res.data || []).filter((f: any) => f.status === 1) }
  catch { faqList.value = [] } finally { loading.value = false }
}

async function handleSearch() {
  const kw = searchKeyword.value.trim()
  if (!kw) { faqList.value = []; activeCategory.value = null; return }
  activeCategory.value = null; loading.value = true
  try { const res: any = await searchFaq(kw); faqList.value = (res.data || []).filter((f: any) => f.status === 1) }
  catch { faqList.value = [] } finally { loading.value = false }
}

function toggleItem(id: number) { expandedId.value = expandedId.value === id ? null : id }

onMounted(loadCategories)
</script>

<template>
  <div class="faq-web">
    <div class="page-header"><h2>常见问题</h2><p class="subtitle">浏览常见问题，快速找到答案</p></div>

    <div class="content-layout">
      <!-- 左侧分类 -->
      <div class="side-panel">
        <h4>问题分类</h4>
        <div class="cat-list">
          <div :class="['cat-item', activeCategory === null && !searchKeyword ? 'active' : '']" @click="activeCategory = null; searchKeyword = ''; faqList = []">全部</div>
          <div v-for="c in categories" :key="c.id" :class="['cat-item', activeCategory === c.id ? 'active' : '']" @click="loadByCategory(c.id)">
            {{ c.categoryName }}
            <span class="cat-count">{{ c.questionCount || 0 }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="main-panel">
        <!-- 搜索 -->
        <div class="search-bar">
          <div class="search-box">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            <input v-model="searchKeyword" placeholder="搜索问题关键词..." @keyup.enter="handleSearch" />
          </div>
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>

        <!-- FAQ 列表 -->
        <div v-if="loading" class="loading-state"><div class="loading-spinner"></div></div>
        <div v-else-if="faqList.length === 0 && (searchKeyword || activeCategory)" class="empty-hint">未找到相关问题</div>
        <div v-else-if="faqList.length > 0" class="faq-list">
          <div v-for="item in faqList" :key="item.id" class="faq-item" :class="{ expanded: expandedId === item.id }" @click="toggleItem(item.id)">
            <div class="faq-question">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
              <span>{{ item.question }}</span>
              <svg class="expand-arrow" :class="{ open: expandedId === item.id }" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
            </div>
            <transition name="expand">
              <div v-if="expandedId === item.id" class="faq-answer">{{ item.answer }}</div>
            </transition>
          </div>
        </div>
        <div v-else class="empty-hint">选择分类或搜索关键词查看问题</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.faq-web { padding-bottom: 24px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: var(--gray-900); margin: 0 0 4px; }
.page-header .subtitle { font-size: 14px; color: var(--gray-400); margin: 0; }

.content-layout { display: grid; grid-template-columns: 220px 1fr; gap: 20px; min-height: 400px; }

.side-panel { background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; padding: 16px; }
.side-panel h4 { font-size: 14px; font-weight: 600; color: var(--gray-900); margin: 0 0 12px; }
.cat-list { display: flex; flex-direction: column; gap: 4px; }
.cat-item { display: flex; align-items: center; gap: 8px; padding: 10px 12px; border-radius: 8px; cursor: pointer; transition: background 0.15s; font-size: 13px; color: var(--gray-600); }
.cat-item:active { background: var(--gray-100); }
.cat-item.active { background: rgba(26,92,255,0.1); color: var(--primary); font-weight: 500; }
.cat-count { margin-left: auto; font-size: 11px; color: var(--gray-400); }

.main-panel { background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; padding: 20px; }

.search-bar { display: flex; gap: 10px; margin-bottom: 20px; }
.search-box { flex: 1; display: flex; align-items: center; gap: 8px; border: 1px solid var(--gray-200); border-radius: 10px; padding: 0 12px; }
.search-box svg { color: var(--gray-400); flex-shrink: 0; }
.search-box input { border: none; outline: none; padding: 10px 0; font-size: 14px; width: 100%; background: transparent; }
.search-btn { padding: 10px 20px; border: none; border-radius: 10px; background: var(--primary); color: #fff; font-size: 14px; font-weight: 500; cursor: pointer; white-space: nowrap; }
.search-btn:active { opacity: 0.85; }

.faq-list { display: flex; flex-direction: column; gap: 10px; }
.faq-item { border: 1px solid var(--gray-200); border-radius: 10px; overflow: hidden; cursor: pointer; transition: box-shadow 0.15s; }
.faq-item:active { box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.faq-item.expanded { border-color: rgba(26,92,255,0.2); }
.faq-question { display: flex; align-items: center; gap: 10px; padding: 14px 16px; }
.faq-question span { flex: 1; font-size: 14px; font-weight: 500; color: var(--gray-900); }
.expand-arrow { flex-shrink: 0; transition: transform 0.2s; color: var(--gray-400); }
.expand-arrow.open { transform: rotate(180deg); }
.faq-answer { padding: 0 16px 14px 42px; font-size: 14px; line-height: 1.7; color: var(--gray-600); }
.expand-enter-active { transition: all 0.25s ease; overflow: hidden; }
.expand-leave-active { transition: all 0.2s ease; overflow: hidden; }
.expand-enter-from, .expand-leave-to { opacity: 0; max-height: 0; }
.expand-enter-to, .expand-leave-from { opacity: 1; max-height: 500px; }

.empty-hint { text-align: center; padding: 40px; color: var(--gray-400); font-size: 14px; }

.loading-state { display: flex; justify-content: center; padding: 40px; }
.loading-spinner { width: 28px; height: 28px; border: 3px solid var(--gray-200); border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
