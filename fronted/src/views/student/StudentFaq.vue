<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getFaqCategories, searchFaq, getFaqByCategory } from '@/api/discover'

const route = useRoute()
const router = useRouter()

const isWebLayout = computed(() => route.path.startsWith('/student-web'))
const goBack = () => {
  if (isWebLayout.value) {
    router.push({ path: '/student-web', query: { tab: 'faq' } })
  } else {
    router.push({ path: '/student', query: { tab: 'home' } })
  }
}

interface ChatMessage {
  id: number
  type: 'user' | 'bot'
  content: string
  time: string
}

const messages = ref<ChatMessage[]>([])
const inputText = ref('')
const searching = ref(false)
const categories = ref<any[]>([])
const showCategories = ref(true)

async function loadCategories() {
  try {
    const res: any = await getFaqCategories()
    categories.value = (res.data || []).filter((c: any) => c.status === 1)
  } catch {}
}

async function handleSend() {
  const text = inputText.value.trim()
  if (!text || searching.value) return

  // 添加用户消息
  messages.value.push({
    id: Date.now(),
    type: 'user',
    content: text,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
  })
  inputText.value = ''
  showCategories.value = false
  await nextTick()
  scrollToBottom()

  // 搜索答案
  searching.value = true
  try {
    const res: any = await searchFaq(text)
    const results = res.data || []

    if (results.length > 0) {
      // 显示最相关的答案
      const best = results[0]
      messages.value.push({
        id: Date.now() + 1,
        type: 'bot',
        content: best.answer,
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
      })

      // 如果有多个结果，显示相关问题列表
      if (results.length > 1) {
        const related = results.slice(1, 4).map((r: any) => `• ${r.question}`).join('\n')
        messages.value.push({
          id: Date.now() + 2,
          type: 'bot',
          content: `您可能还想了解：\n${related}`,
          time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
        })
      }
    } else {
      messages.value.push({
        id: Date.now() + 1,
        type: 'bot',
        content: '抱歉，暂时没有找到相关答案。您可以尝试换一个关键词搜索，或联系辅导员获取帮助。',
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
      })
    }
  } catch {
    messages.value.push({
      id: Date.now() + 1,
      type: 'bot',
      content: '网络异常，请稍后重试。',
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    })
  } finally {
    searching.value = false
    await nextTick()
    scrollToBottom()
  }
}

async function handleCategoryClick(categoryId: number, categoryName: string) {
  messages.value.push({
    id: Date.now(),
    type: 'user',
    content: `查看「${categoryName}」相关问题`,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
  })
  showCategories.value = false
  await nextTick()
  scrollToBottom()

  searching.value = true
  try {
    const res: any = await getFaqByCategory(categoryId)
    const results = (res.data || []).filter((f: any) => f.status === 1)

    if (results.length > 0) {
      const list = results.map((r: any) => `Q: ${r.question}\nA: ${r.answer}`).join('\n\n')
      messages.value.push({
        id: Date.now() + 1,
        type: 'bot',
        content: `以下是「${categoryName}」相关的问题：\n\n${list}`,
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
      })
    } else {
      messages.value.push({
        id: Date.now() + 1,
        type: 'bot',
        content: `暂无「${categoryName}」相关的问题。`,
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
      })
    }
  } catch {
    messages.value.push({
      id: Date.now() + 1,
      type: 'bot',
      content: '网络异常，请稍后重试。',
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    })
  } finally {
    searching.value = false
    await nextTick()
    scrollToBottom()
  }
}

function scrollToBottom() {
  const el = document.querySelector('.chat-messages')
  if (el) el.scrollTop = el.scrollHeight
}
</script>

<template>
  <div class="student-faq">
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
      <div>
        <h3>智能问答</h3>
        <p class="subtitle">输入问题，AI助手帮你解答</p>
      </div>
    </div>

    <!-- Chat Messages -->
    <div class="chat-messages">
      <!-- Welcome -->
      <div v-if="messages.length === 0 && showCategories" class="welcome-msg">
        <div class="welcome-avatar">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
        </div>
        <p>你好！我是智能问答助手，有什么关于入学的问题可以问我~</p>
      </div>

      <!-- Quick Categories -->
      <div v-if="showCategories && categories.length > 0" class="quick-categories">
        <span class="cat-label">热门分类</span>
        <div class="cat-tags">
          <span v-for="c in categories" :key="c.id" class="cat-tag" @click="handleCategoryClick(c.id, c.categoryName)">
            {{ c.categoryName }}
          </span>
        </div>
      </div>

      <!-- Messages -->
      <div v-for="msg in messages" :key="msg.id" :class="['chat-msg', msg.type]">
        <div v-if="msg.type === 'bot'" class="msg-avatar">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
        </div>
        <div class="msg-bubble">
          <div class="msg-text" v-html="msg.content.replace(/\n/g, '<br/>')"></div>
          <span class="msg-time">{{ msg.time }}</span>
        </div>
      </div>

      <!-- Typing indicator -->
      <div v-if="searching" class="chat-msg bot">
        <div class="msg-avatar">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
        </div>
        <div class="msg-bubble typing">
          <span class="dot"></span><span class="dot"></span><span class="dot"></span>
        </div>
      </div>
    </div>

    <!-- Input -->
    <div class="chat-input">
      <input
        v-model="inputText"
        placeholder="输入你的问题..."
        @keyup.enter="handleSend"
        :disabled="searching"
      />
      <button class="send-btn" @click="handleSend" :disabled="!inputText.trim() || searching">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<style scoped>
.student-faq {
  display: flex; flex-direction: column; height: calc(100vh - 72px);
  max-width: 480px; margin: 0 auto;
}

.page-header {
  display: flex; align-items: center; gap: 10px;
  padding: 0 0 12px; flex-shrink: 0;
}
.back-btn {
  width: 36px; height: 36px; border-radius: 10px; border: 1px solid var(--gray-200);
  background: #fff; display: flex; align-items: center; justify-content: center;
  cursor: pointer; flex-shrink: 0; color: var(--gray-600);
}
.page-header h3 { font-size: 18px; font-weight: 700; color: var(--gray-900); margin: 0; }
.page-header .subtitle { font-size: 13px; color: var(--gray-400); margin: 0; }

/* Chat Messages */
.chat-messages {
  flex: 1; overflow-y: auto; padding: 8px 0;
  display: flex; flex-direction: column; gap: 12px;
  -webkit-overflow-scrolling: touch;
}

.welcome-msg {
  display: flex; align-items: flex-start; gap: 10px;
  padding: 12px 0;
}
.welcome-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: var(--primary); display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.welcome-msg p {
  font-size: 14px; color: var(--gray-600); line-height: 1.6;
  background: #fff; padding: 12px 16px; border-radius: 12px;
  border: 1px solid var(--gray-200); margin: 0;
}

.quick-categories { padding: 4px 0 8px; }
.cat-label { font-size: 12px; color: var(--gray-400); display: block; margin-bottom: 8px; }
.cat-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.cat-tag {
  padding: 6px 14px; border-radius: 20px; font-size: 13px;
  background: #fff; color: var(--primary); border: 1px solid rgba(26,92,255,0.2);
  cursor: pointer; transition: all 0.2s;
}
.cat-tag:active { background: rgba(26,92,255,0.1); }

/* Chat Message */
.chat-msg { display: flex; align-items: flex-start; gap: 10px; }
.chat-msg.user { flex-direction: row-reverse; }
.msg-avatar {
  width: 32px; height: 32px; border-radius: 50%; flex-shrink: 0;
  background: var(--primary); display: flex; align-items: center; justify-content: center;
}
.msg-bubble {
  max-width: 80%; padding: 10px 14px; border-radius: 12px;
  font-size: 14px; line-height: 1.6; position: relative;
}
.chat-msg.bot .msg-bubble { background: #fff; border: 1px solid var(--gray-200); color: var(--gray-600); }
.chat-msg.user .msg-bubble { background: var(--primary); color: #fff; }
.msg-time { display: block; font-size: 10px; color: var(--gray-400); margin-top: 4px; }
.chat-msg.user .msg-time { text-align: right; color: rgba(255,255,255,0.6); }

/* Typing */
.typing { display: flex; gap: 4px; padding: 14px 16px; }
.dot {
  width: 6px; height: 6px; border-radius: 50%; background: var(--gray-400);
  animation: typing 1.2s infinite;
}
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing { 0%, 60%, 100% { transform: translateY(0); } 30% { transform: translateY(-4px); } }

/* Input */
.chat-input {
  display: flex; gap: 8px; padding: 12px 0; flex-shrink: 0;
  border-top: 1px solid var(--gray-200);
}
.chat-input input {
  flex: 1; padding: 10px 14px; border: 1px solid var(--gray-200); border-radius: 10px;
  font-size: 14px; outline: none; background: #fff;
}
.chat-input input:focus { border-color: var(--primary); }
.send-btn {
  width: 42px; height: 42px; border-radius: 10px; border: none;
  background: var(--primary); color: #fff; display: flex; align-items: center;
  justify-content: center; cursor: pointer; transition: opacity 0.2s; flex-shrink: 0;
}
.send-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.send-btn:active:not(:disabled) { opacity: 0.85; }
</style>
