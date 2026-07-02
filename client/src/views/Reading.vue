<template>
  <div>
    <h1 class="page-title">📖 阅读 & 翻译</h1>

    <!-- Tab -->
    <div class="flex gap-2 mb-6">
      <button @click="mode = 'read'"
        class="px-5 py-2.5 rounded-lg text-sm font-medium transition"
        :class="mode === 'read' ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'">
        📖 阅读理解
      </button>
      <button @click="mode = 'translate'"
        class="px-5 py-2.5 rounded-lg text-sm font-medium transition"
        :class="mode === 'translate' ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'">
        🌐 翻译文章
      </button>
    </div>

    <!-- ==================== 阅读理解模式 ==================== -->
    <div v-if="mode === 'read'">
      <div class="card mb-6">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-3">📄 粘贴英语文章</h2>
        <textarea v-model="article" class="input-field h-40 font-mono text-sm"
          placeholder="粘贴一篇英语文章，点击文章中的任意单词即可查词并加入词库"></textarea>
        <button @click="parseArticle" class="btn-primary mt-3" :disabled="!article.trim()">📖 开始阅读</button>
      </div>

      <div class="card mb-6" v-if="!parsed">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-3">🎯 或者选择一篇真题</h2>
        <div class="space-y-2">
          <button v-for="(p, i) in presetArticles" :key="i"
            @click="article = p.content; parseArticle()"
            class="w-full text-left p-3 bg-gray-50 dark:bg-gray-800 rounded-lg hover:bg-blue-50 dark:hover:bg-blue-900/20 transition text-sm">
            <span class="font-medium text-gray-800 dark:text-gray-200">真题 {{ i + 1 }}：</span>
            <span class="text-gray-600 dark:text-gray-400">{{ p.title }}</span>
          </button>
        </div>
      </div>

      <div v-if="parsed" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div class="lg:col-span-2 card">
          <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-4">{{ currentTitle }}</h2>
          <div class="leading-relaxed text-gray-700 dark:text-gray-200">
            <span v-for="(token, i) in tokens" :key="i"
              :class="token.isWord ? 'cursor-pointer hover:bg-yellow-200 dark:hover:bg-yellow-700 rounded px-0.5 transition' : ''"
              @click="token.isWord && lookupWord(token)" :title="token.isWord ? '点击查词' : ''">{{ token.text }}</span>
          </div>
          <div class="mt-4 pt-3 border-t dark:border-gray-700 text-xs text-gray-400 dark:text-gray-500">💡 点击任意单词即可查看释义，可一键加入生词本</div>
        </div>

        <div class="card">
          <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-4">🔍 单词查询</h2>
          <div v-if="!lookedUpWord" class="text-center text-gray-400 dark:text-gray-500 py-12">👆 点击左侧文章中的单词</div>
          <div v-else class="space-y-3">
            <div class="text-3xl font-bold text-gray-800 dark:text-gray-100">{{ lookedUpWord }}</div>
            <div v-if="wordInfo" class="space-y-2">
              <div class="text-gray-500 dark:text-gray-400">{{ wordInfo.phonetic }}</div>
              <div class="font-medium text-blue-600 dark:text-blue-400">{{ wordInfo.meaning }}</div>
              <div v-if="wordInfo.example" class="text-sm italic text-gray-600 dark:text-gray-400 bg-gray-50 dark:bg-gray-800 p-3 rounded-lg">
                {{ wordInfo.example }}
                <div class="text-xs text-gray-400 dark:text-gray-500 mt-1">{{ wordInfo.example_cn }}</div>
              </div>
              <div v-if="wordInfo.collocations?.length" class="text-xs text-gray-500 dark:text-gray-400">
                <span class="font-medium">常用搭配：</span>{{ wordInfo.collocations.join('、') }}
              </div>
            </div>
            <div v-else class="text-sm text-gray-400">正在查询...</div>
            <button @click="addToWordbook" class="btn-primary w-full text-sm mt-3" :disabled="wordInfo?.meaning === '生成失败'">+ 加入生词本</button>
          </div>

          <div v-if="lookedUpWords.length > 0" class="mt-6 pt-4 border-t dark:border-gray-700">
            <h3 class="text-sm font-semibold text-gray-500 dark:text-gray-400 mb-3">本次查过的词</h3>
            <div class="flex flex-wrap gap-2">
              <span v-for="w in lookedUpWords" :key="w"
                class="text-xs px-2 py-1 bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded-full cursor-pointer hover:bg-blue-100 dark:hover:bg-blue-900/50"
                @click="lookupWord({ text: w })">{{ w }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== 翻译模式 ==================== -->
    <div v-if="mode === 'translate'" class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 输入 -->
      <div class="card">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-3">📝 粘贴要翻译的段落</h2>
        <div class="flex items-center gap-2 mb-3">
          <button @click="transDir = 'en2zh'"
            class="px-3 py-1.5 rounded-lg text-xs font-medium transition"
            :class="transDir === 'en2zh' ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300'">
            英→中
          </button>
          <button @click="transDir = 'zh2en'"
            class="px-3 py-1.5 rounded-lg text-xs font-medium transition"
            :class="transDir === 'zh2en' ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300'">
            中→英
          </button>
          <button @click="transDir = 'auto'"
            class="px-3 py-1.5 rounded-lg text-xs font-medium transition"
            :class="transDir === 'auto' ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300'">
            自动
          </button>
        </div>
        <textarea v-model="transText" class="input-field h-48 resize-none font-mono text-sm"
          placeholder="粘贴要翻译的段落...&#10;&#10;示例：&#10;• 英→中：The rapid development of AI...&#10;• 中→英：近年来，人工智能技术取得了飞速发展..."></textarea>
        <button @click="doTranslate" class="btn-primary mt-3" :disabled="translating || transText.trim().length < 2">
          {{ translating ? '⏳ 翻译中...' : '🌐 翻译' }}
        </button>
      </div>

      <!-- 结果 -->
      <div v-if="transResult" class="space-y-4">
        <div class="card">
          <div class="flex items-center justify-between mb-3">
            <h3 class="font-semibold text-blue-600 dark:text-blue-400">
              🌐 {{ transResult.source_lang }} → {{ transResult.target_lang }}
            </h3>
            <button @click="copyTranslation" class="text-xs text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
              {{ copied ? '✅ 已复制' : '📋 复制译文' }}
            </button>
          </div>
          <div class="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg text-sm leading-relaxed whitespace-pre-wrap text-gray-700 dark:text-gray-200">
            {{ transResult.translation }}
          </div>
        </div>

        <!-- 翻译要点 -->
        <div class="card" v-if="transResult.notes?.length">
          <h3 class="font-semibold text-green-600 dark:text-green-400 mb-3">💡 翻译要点</h3>
          <div v-for="(n, i) in transResult.notes" :key="i" class="mb-2 p-2 bg-green-50 dark:bg-green-900/20 rounded text-sm">
            <div class="flex items-center gap-2">
              <span class="font-medium text-gray-800 dark:text-gray-200">{{ n.original }}</span>
              <span class="text-gray-300">→</span>
              <span class="text-green-700 dark:text-green-400">{{ n.translation }}</span>
            </div>
            <div class="text-xs text-gray-500 mt-0.5">{{ n.explain }}</div>
          </div>
        </div>

        <!-- 难词 -->
        <div class="card" v-if="transResult.difficult_words?.length">
          <h3 class="font-semibold text-orange-600 dark:text-orange-400 mb-3">📝 难词解释</h3>
          <div class="flex flex-wrap gap-2">
            <span v-for="(dw, i) in transResult.difficult_words" :key="i"
              class="px-3 py-1.5 bg-orange-50 dark:bg-orange-900/20 border border-orange-200 dark:border-orange-800 rounded-lg text-sm">
              <span class="font-semibold text-orange-700 dark:text-orange-400">{{ dw.word }}</span>
              <span class="text-gray-500 ml-1">— {{ dw.meaning }}</span>
            </span>
          </div>
        </div>
      </div>

      <div v-if="!transResult && !translating" class="card flex items-center justify-center text-center text-gray-400 dark:text-gray-500 py-16">
        <div>
          <div class="text-5xl mb-4">🌐</div>
          <p>在左边粘贴段落</p>
          <p class="text-sm">AI 自动翻译并标注要点</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { wordAPI, translateAPI } from '../api'

const mode = ref('read')

// ── 阅读模式 ──
const article = ref('')
const parsed = ref(false)
const currentTitle = ref('')
const tokens = ref([])
const lookedUpWord = ref('')
const wordInfo = ref(null)
const lookedUpWords = ref([])
const addingWord = ref(false)

const presetArticles = [
  { title: '人工智能对教育的影响', content: `Artificial intelligence is transforming education in profound ways. From personalized learning platforms to automated grading systems, AI technologies are reshaping how students learn and how teachers teach. However, the integration of AI into education also raises important ethical questions about data privacy, algorithmic bias, and the risk of diminishing human interaction in the learning process. As we embrace these powerful tools, we must strike a careful balance between technological innovation and the fundamental human elements of education.` },
  { title: '终身学习的重要性', content: `In today's rapidly evolving job market, lifelong learning has become not just an advantage but a necessity. The skills that were in high demand a decade ago may now be obsolete, replaced by new technologies and ways of working. Successful professionals are those who continuously update their knowledge and adapt to changing circumstances. This shift has profound implications for educational institutions, employers, and individuals alike, as we rethink how learning is structured throughout a person's career and life.` },
]

// ── 翻译模式 ──
const transDir = ref('auto')
const transText = ref('')
const translating = ref(false)
const transResult = ref(null)
const copied = ref(false)

async function doTranslate() {
  translating.value = true
  transResult.value = null
  try {
    const { data } = await translateAPI.translate({ text: transText.value, direction: transDir.value })
    transResult.value = data
  } catch {
    alert('翻译失败，请稍后重试')
  }
  translating.value = false
}

function copyTranslation() {
  if (!transResult.value?.translation) return
  navigator.clipboard.writeText(transResult.value.translation)
  copied.value = true
  setTimeout(() => copied.value = false, 2000)
}

// ── 阅读 ──
function parseArticle() {
  if (!article.value.trim()) return
  currentTitle.value = '自定义文章'
  const words = article.value.split(/(\b\w+\b)/g)
  tokens.value = words.map(t => ({ text: t, isWord: /^\w+$/.test(t) && t.length > 1 }))
  parsed.value = true
}

async function lookupWord(token) {
  const word = token.text.toLowerCase(); lookedUpWord.value = word; wordInfo.value = null
  try { const { data } = await wordAPI.list({ page: 1, limit: 1 }); const found = data.words?.find(w => w.word.toLowerCase() === word); if (found) { wordInfo.value = found; if (!lookedUpWords.value.includes(word)) lookedUpWords.value.push(word); return } } catch { /* */ }
  try { const { data } = await wordAPI.lookup(word); wordInfo.value = data; if (!lookedUpWords.value.includes(word)) lookedUpWords.value.push(word) } catch { wordInfo.value = { phonetic: '', meaning: '查询失败', example: '' } }
}

async function addToWordbook() {
  if (addingWord.value || !wordInfo.value) return; addingWord.value = true
  try { await wordAPI.add({ word: lookedUpWord.value, meaning: wordInfo.value.meaning }); alert(`✅ "${lookedUpWord.value}" 已加入词库`) }
  catch (err) { alert(err.response?.status === 409 ? '该单词已在词库中' : '添加失败') }
  finally { addingWord.value = false }
}
</script>
