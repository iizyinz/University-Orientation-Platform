<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'

interface ChatMessage {
  id: number
  type: 'user' | 'bot'
  content: string
  time: string
}

import { getFaqCategories, searchFaq, getFaqByCategory } from '@/api/discover'

const messages = ref<ChatMessage[]>([])
const inputText = ref('')
const searching = ref(false)
const categories = ref<any[]>([])
const showWelcome = ref(true)

async function loadCategories() {
  try { const res: any = await getFaqCategories(); categories.value = (res.data || []).filter((c: any) => c.status === 1) } catch {}
}

async function handleSend() {
  const text = inputText.value.trim()
  if (!text || searching.value) return
  messages.value.push({ id: Date.now(), type: 'user', content: text, time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) })
  inputText.value = ''
  showWelcome.value = false
  await nextTick(); scrollToBottom()

  searching.value = true
  try {
    const res: any = await searchFaq(text)
    const results = res.data || []
    if (results.length > 0) {
      messages.value.push({ id: Date.now() + 1, type: 'bot', content: results[0].answer, time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) })
      if (results.length > 1) {
        const related = results.slice(1, 4).map((r: any) => `• ${r.question}`).join('\n')
        messages.value.push({ id: Date.now() + 2, type: 'bot', content: `您可能还想了解：\n${related}`, time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) })
      }
    } else {
      messages.value.push({ id: Date.now() + 1, type: 'bot', content: '抱歉，暂时没有找到相关答案。您可以尝试换一个关键词搜索，或联系辅导员获取帮助。', time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) })
    }
  } catch {
    messages.value.push({ id: Date.now() + 1, type: 'bot', content: '网络异常，请稍后重试。', time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) })
  } finally { searching.value = false; await nextTick(); scrollToBottom() }
}

async function handleCategoryClick(categoryId: number, categoryName: string) {
  messages.value.push({ id: Date.now(), type: 'user', content: `查看「${categoryName}」相关问题`, time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) })
  showWelcome.value = false
  await nextTick(); scrollToBottom()
  searching.value = true
  try {
    const res: any = await getFaqByCategory(categoryId)
    const results = (res.data || []).filter((f: any) => f.status === 1)
    if (results.length > 0) {
      const list = results.map((r: any) => `Q: ${r.question}\nA: ${r.answer}`).join('\n\n')
      messages.value.push({ id: Date.now() + 1, type: 'bot', content: `以下是「${categoryName}」相关的问题：\n\n${list}`, time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) })
    } else {
      messages.value.push({ id: Date.now() + 1, type: 'bot', content: `暂无「${categoryName}」相关的问题。`, time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) })
    }
  } catch {
    messages.value.push({ id: Date.now() + 1, type: 'bot', content: '网络异常，请稍后重试。', time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) })
  } finally { searching.value = false; await nextTick(); scrollToBottom() }
}

function scrollToBottom() { const el = document.querySelector('.chat-messages'); if (el) el.scrollTop = el.scrollHeight }

onMounted(loadCategories)
</script>

<template>
  <div class="faq-web">
    <div class="page-header"><h2>智能问答</h2><p class="subtitle">输入问题，AI助手帮你解答</p></div>

    <div class="chat-layout">
      <!-- 左侧：分类 -->
      <div class="category-panel">
        <h4>问题分类</h4>
        <div class="cat-list">
          <div v-for="c in categories" :key="c.id" class="cat-item" @click="handleCategoryClick(c.id, c.categoryName)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/></svg>
            <span>{{ c.categoryName }}</span>
            <span class="cat-count">{{ c.questionCount || 0 }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧：对话 -->
      <div class="chat-panel">
        <div class="chat-messages">
          <div v-if="showWelcome" class="welcome-msg">
            <div class="welcome-avatar"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div>
            <p>你好！我是智能问答助手，有什么关于入学的问题可以问我~</p>
          </div>

          <div v-for="msg in messages" :key="msg.id" :class="['chat-msg', msg.type]">
            <div v-if="msg.type === 'bot'" class="msg-avatar"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div>
            <div class="msg-bubble"><div class="msg-text" v-html="msg.content.replace(/\n/g, '<br/>')"></div><span class="msg-time">{{ msg.time }}</span></div>
          </div>

          <div v-if="searching" class="chat-msg bot">
            <div class="msg-avatar"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div>
            <div class="msg-bubble typing"><span class="dot"></span><span class="dot"></span><span class="dot"></span></div>
          </div>
        </div>

        <div class="chat-input">
          <input v-model="inputText" placeholder="输入你的问题..." @keyup.enter="handleSend" :disabled="searching" />
          <button class="send-btn" @click="handleSend" :disabled="!inputText.trim() || searching"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg></button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.faq-web { padding-bottom: 24px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: var(--gray-900); margin: 0 0 4px; }
.page-header .subtitle { font-size: 14px; color: var(--gray-400); margin: 0; }

.chat-layout { display: grid; grid-template-columns: 220px 1fr; gap: 20px; min-height: 500px; }
@media (max-width: 768px) { .chat-layout { grid-template-columns: 1fr; } }

.category-panel { background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; padding: 16px; }
.category-panel h4 { font-size: 14px; font-weight: 600; color: var(--gray-900); margin: 0 0 12px; }
.cat-list { display: flex; flex-direction: column; gap: 4px; }
.cat-item { display: flex; align-items: center; gap: 8px; padding: 10px 12px; border-radius: 8px; cursor: pointer; transition: background 0.15s; font-size: 13px; color: var(--gray-600); }
.cat-item:active { background: var(--gray-100); }
.cat-count { margin-left: auto; font-size: 11px; color: var(--gray-400); }

.chat-panel { background: #fff; border: 1px solid var(--gray-200); border-radius: 12px; display: flex; flex-direction: column; }
.chat-messages { flex: 1; overflow-y: auto; padding: 20px; display: flex; flex-direction: column; gap: 12px; }

.welcome-msg { display: flex; align-items: flex-start; gap: 12px; padding: 12px 0; }
.welcome-avatar { width: 36px; height: 36px; border-radius: 50%; background: var(--primary); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.welcome-msg p { font-size: 14px; color: var(--gray-600); line-height: 1.6; background: var(--gray-100); padding: 12px 16px; border-radius: 12px; margin: 0; }

.chat-msg { display: flex; align-items: flex-start; gap: 10px; }
.chat-msg.user { flex-direction: row-reverse; }
.msg-avatar { width: 32px; height: 32px; border-radius: 50%; flex-shrink: 0; background: var(--primary); display: flex; align-items: center; justify-content: center; }
.msg-bubble { max-width: 70%; padding: 10px 14px; border-radius: 12px; font-size: 14px; line-height: 1.6; }
.chat-msg.bot .msg-bubble { background: var(--gray-100); color: var(--gray-600); }
.chat-msg.user .msg-bubble { background: var(--primary); color: #fff; }
.msg-time { display: block; font-size: 10px; color: var(--gray-400); margin-top: 4px; }
.chat-msg.user .msg-time { text-align: right; color: rgba(255,255,255,0.6); }

.typing { display: flex; gap: 4px; padding: 14px 16px; }
.dot { width: 6px; height: 6px; border-radius: 50%; background: var(--gray-400); animation: typing 1.2s infinite; }
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing { 0%, 60%, 100% { transform: translateY(0); } 30% { transform: translateY(-4px); } }

.chat-input { display: flex; gap: 10px; padding: 16px 20px; border-top: 1px solid var(--gray-200); }
.chat-input input { flex: 1; padding: 10px 14px; border: 1px solid var(--gray-200); border-radius: 10px; font-size: 14px; outline: none; }
.chat-input input:focus { border-color: var(--primary); }
.send-btn { width: 42px; height: 42px; border-radius: 10px; border: none; background: var(--primary); color: #fff; display: flex; align-items: center; justify-content: center; cursor: pointer; flex-shrink: 0; }
.send-btn:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
