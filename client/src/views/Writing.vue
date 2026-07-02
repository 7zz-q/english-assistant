<template>
  <div>
    <h1 class="page-title">✍️ AI 写作</h1>

    <!-- 模式切换 Tab -->
    <div class="flex gap-2 mb-6">
      <button @click="mode = 'correct'"
        class="px-5 py-2.5 rounded-lg text-sm font-medium transition"
        :class="mode === 'correct' ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'">
        📝 批改作文
      </button>
      <button @click="mode = 'topic'"
        class="px-5 py-2.5 rounded-lg text-sm font-medium transition"
        :class="mode === 'topic' ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'">
        🎲 AI 出题
      </button>
      <button @click="mode = 'sample'"
        class="px-5 py-2.5 rounded-lg text-sm font-medium transition"
        :class="mode === 'sample' ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'">
        📖 AI 范文
      </button>
    </div>

    <!-- ==================== 模式 1：批改作文 ==================== -->
    <div v-if="mode === 'correct'" class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="card">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-3">📝 写作文</h2>
        <div class="mb-3">
          <label class="text-sm font-medium text-gray-700 dark:text-gray-300">题目（可选）</label>
          <input v-model="title" class="input-field mt-1" placeholder="如：The Impact of AI on Education" />
        </div>
        <div>
          <label class="text-sm font-medium text-gray-700 dark:text-gray-300">作文内容</label>
          <textarea v-model="content" class="input-field mt-1 h-64 resize-none font-mono text-sm"
            placeholder="在这里写下你的英语作文（至少 20 个字符）&#10;&#10;建议话题：&#10;• The importance of lifelong learning&#10;• My view on online education"></textarea>
        </div>
        <div class="flex items-center justify-between mt-3">
          <span class="text-xs text-gray-400 dark:text-gray-500">字数：{{ content.length }}</span>
          <button @click="submitEssay" class="btn-primary" :disabled="correcting || content.length < 20">
            {{ correcting ? '⏳ AI 批改中...' : '🤖 提交批改' }}
          </button>
        </div>
        <p v-if="correctError" class="text-red-500 text-sm mt-2">{{ correctError }}</p>
      </div>

      <div v-if="result" class="space-y-4">
        <div class="card text-center">
          <div class="text-sm text-gray-500 dark:text-gray-400 mb-1">综合评分</div>
          <div class="text-6xl font-bold" :class="scoreColor">{{ result.score }}<span class="text-2xl text-gray-400">/100</span></div>
          <div class="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2.5 mt-3">
            <div class="h-2.5 rounded-full transition-all duration-1000" :class="scoreBarColor" :style="{ width: (result.score || 0) + '%' }"></div>
          </div>
        </div>
        <div class="card" v-if="result.grammar?.length">
          <h3 class="font-semibold text-red-600 dark:text-red-400 mb-3">🔍 语法错误（{{ result.grammar.length }}）</h3>
          <div v-for="(g,i) in result.grammar" :key="i" class="mb-3 p-3 bg-red-50 dark:bg-red-900/20 rounded-lg text-sm">
            <div class="flex items-center gap-2 mb-1">
              <span class="line-through text-red-400">{{ g.error }}</span><span class="text-gray-300">→</span>
              <span class="font-medium text-green-600 dark:text-green-400">{{ g.fix }}</span>
              <span class="text-xs px-1.5 py-0.5 rounded" :class="g.severity==='high'?'bg-red-200 dark:bg-red-800 text-red-700 dark:text-red-300':'bg-yellow-200 dark:bg-yellow-800 text-yellow-700 dark:text-yellow-300'">{{ g.severity==='high'?'严重':'一般' }}</span>
            </div>
            <div class="text-xs text-gray-500 dark:text-gray-400">{{ g.explain }}</div>
          </div>
        </div>
        <div class="card" v-if="result.vocabulary?.length">
          <h3 class="font-semibold text-blue-600 dark:text-blue-400 mb-3">💎 词汇升级（{{ result.vocabulary.length }}）</h3>
          <div v-for="(v,i) in result.vocabulary" :key="i" class="flex items-start gap-2 mb-2 p-2 bg-blue-50 dark:bg-blue-900/20 rounded-lg text-sm">
            <span class="text-gray-400 line-through shrink-0">{{ v.original }}</span><span class="text-gray-300">→</span>
            <div><span class="font-medium text-blue-700 dark:text-blue-400">{{ v.suggestion }}</span><div class="text-xs text-gray-500">{{ v.reason }}</div></div>
          </div>
        </div>
        <div class="card" v-if="result.structure?.length">
          <h3 class="font-semibold text-purple-600 dark:text-purple-400 mb-3">📐 结构建议</h3>
          <div v-for="(s,i) in result.structure" :key="i" class="mb-3 p-3 bg-purple-50 dark:bg-purple-900/20 rounded-lg text-sm">
            <span class="text-xs px-2 py-0.5 bg-purple-200 dark:bg-purple-800 text-purple-700 dark:text-purple-300 rounded mr-2">{{ s.type }}</span>
            <span class="text-gray-600 dark:text-gray-300">{{ s.comment }}</span>
            <div v-if="s.improved" class="mt-1 text-xs text-gray-500 italic">改进：{{ s.improved }}</div>
          </div>
        </div>
        <div class="card" v-if="result.corrected">
          <h3 class="font-semibold text-green-600 dark:text-green-400 mb-3">✨ 润色后全文</h3>
          <div class="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg text-sm leading-relaxed whitespace-pre-wrap text-gray-700 dark:text-gray-200">{{ result.corrected }}</div>
        </div>
      </div>
      <div v-if="!result && !correcting" class="card flex items-center justify-center text-center text-gray-400 dark:text-gray-500 py-16">
        <div><div class="text-5xl mb-4">🤖</div><p>在左边写下英语作文</p><p class="text-sm">AI 批改语法、推荐高级词汇、润色全文</p></div>
      </div>
    </div>

    <!-- ==================== 模式 2：AI 出题 → 我写 → 批改 ==================== -->
    <div v-if="mode === 'topic'" class="max-w-2xl mx-auto space-y-6">
      <!-- 出题设置 -->
      <div class="card">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-3">🎲 AI 随机出题</h2>
        <div class="flex items-center gap-3 mb-4">
          <select v-model="topicLevel" class="input-field w-auto py-1.5 text-sm">
            <option value="cet4">CET-4</option>
            <option value="cet6">CET-6</option>
            <option value="postgrad">考研</option>
          </select>
          <button @click="fetchTopic" class="btn-primary text-sm py-1.5" :disabled="topicLoading">
            {{ topicLoading ? '生成中...' : '🎲 生成题目' }}
          </button>
        </div>

        <!-- 生成的题目 -->
        <div v-if="generatedTopic" class="p-4 bg-yellow-50 dark:bg-yellow-900/20 rounded-xl border border-yellow-200 dark:border-yellow-800">
          <h3 class="font-bold text-gray-800 dark:text-gray-100 mb-2">{{ generatedTopic.title }}</h3>
          <p class="text-xs text-gray-500 mb-2">{{ generatedTopic.title_cn }}</p>
          <p class="text-sm text-gray-600 dark:text-gray-300 mb-3">{{ generatedTopic.requirement }}</p>
          <div class="flex flex-wrap gap-1.5 mb-3">
            <span v-for="kw in generatedTopic.keywords" :key="kw" class="text-xs px-2 py-0.5 bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-400 rounded-full">{{ kw }}</span>
          </div>
          <details class="text-xs text-gray-500">
            <summary class="cursor-pointer font-medium">💡 思路提示</summary>
            <ul class="mt-2 space-y-1 pl-4">
              <li v-for="(o,i) in generatedTopic.outline_cn" :key="i">{{ o }}</li>
            </ul>
          </details>
        </div>
      </div>

      <!-- 写完作文后提交批改 -->
      <div v-if="generatedTopic" class="card">
        <h3 class="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-3">✍️ 写作文</h3>
        <textarea v-model="topicEssay" class="input-field h-48 resize-none font-mono text-sm"
          placeholder="在这里写你的作文..."></textarea>
        <div class="flex items-center justify-between mt-3">
          <span class="text-xs text-gray-400">字数：{{ topicEssay.length }}</span>
          <button @click="submitTopicEssay" class="btn-primary" :disabled="topicCorrecting || topicEssay.length < 20">
            {{ topicCorrecting ? '⏳ 批改中...' : '🤖 提交批改' }}
          </button>
        </div>
      </div>

      <!-- 批改结果 -->
      <div v-if="topicResult" class="space-y-3">
        <div class="card text-center">
          <div class="text-sm text-gray-500 dark:text-gray-400 mb-1">综合评分</div>
          <div class="text-5xl font-bold" :class="scoreColor2">{{ topicResult.score }}<span class="text-xl text-gray-400">/100</span></div>
        </div>
        <div class="card" v-if="topicResult.grammar?.length">
          <h3 class="font-semibold text-red-600 dark:text-red-400 mb-3">🔍 语法错误</h3>
          <div v-for="(g,i) in topicResult.grammar" :key="i" class="mb-2 p-2 bg-red-50 dark:bg-red-900/20 rounded text-sm">
            <span class="line-through text-red-400">{{ g.error }}</span> → <span class="font-medium text-green-600 dark:text-green-400">{{ g.fix }}</span>
            <div class="text-xs text-gray-500 mt-1">{{ g.explain }}</div>
          </div>
        </div>
        <div class="card" v-if="topicResult.corrected">
          <h3 class="font-semibold text-green-600 dark:text-green-400 mb-3">✨ 润色全文</h3>
          <div class="p-3 bg-gray-50 dark:bg-gray-800 rounded text-sm whitespace-pre-wrap text-gray-700 dark:text-gray-200">{{ topicResult.corrected }}</div>
        </div>
      </div>
    </div>

    <!-- ==================== 模式 3：我给题目 → AI 写范文 ==================== -->
    <div v-if="mode === 'sample'" class="max-w-2xl mx-auto space-y-6">
      <div class="card">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-3">📖 我要一篇范文</h2>
        <div class="mb-3">
          <label class="text-sm font-medium text-gray-700 dark:text-gray-300">作文题目</label>
          <input v-model="sampleTopic" class="input-field mt-1" placeholder="如：Should college students be encouraged to start businesses?" />
        </div>
        <div class="flex items-center gap-3">
          <select v-model="sampleLevel" class="input-field w-auto py-1.5 text-sm">
            <option value="cet4">CET-4</option>
            <option value="cet6">CET-6</option>
            <option value="postgrad">考研</option>
          </select>
          <button @click="fetchSample" class="btn-primary text-sm py-1.5" :disabled="sampleLoading || !sampleTopic.trim()">
            {{ sampleLoading ? '⏳ 生成中...' : '🤖 生成范文' }}
          </button>
        </div>
      </div>

      <!-- 范文结果 -->
      <div v-if="sampleResult" class="space-y-4">
        <div class="card">
          <h3 class="font-semibold text-blue-600 dark:text-blue-400 mb-3">📖 范文</h3>
          <div class="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg text-sm leading-relaxed whitespace-pre-wrap text-gray-700 dark:text-gray-200">{{ sampleResult.essay }}</div>
        </div>
        <div class="card" v-if="sampleResult.essay_cn">
          <h3 class="font-semibold text-purple-600 dark:text-purple-400 mb-3">🌐 中文翻译</h3>
          <div class="text-sm text-gray-600 dark:text-gray-400 leading-relaxed">{{ sampleResult.essay_cn }}</div>
        </div>
        <div class="card" v-if="sampleResult.structure">
          <h3 class="font-semibold text-gray-700 dark:text-gray-300 mb-3">📐 结构分析</h3>
          <div class="text-sm text-gray-600 dark:text-gray-400">{{ sampleResult.structure }}</div>
        </div>
        <div class="card" v-if="sampleResult.highlights?.length">
          <h3 class="font-semibold text-green-600 dark:text-green-400 mb-3">💎 好词好句</h3>
          <div v-for="(h,i) in sampleResult.highlights" :key="i" class="mb-2 p-2 bg-green-50 dark:bg-green-900/20 rounded text-sm">
            <span class="font-medium text-green-700 dark:text-green-400">{{ h.phrase }}</span>
            <div class="text-xs text-gray-500 mt-0.5">{{ h.explain }}</div>
          </div>
        </div>
        <div class="card" v-if="sampleResult.vocabulary?.length">
          <h3 class="font-semibold text-orange-600 dark:text-orange-400 mb-3">📝 高级词汇</h3>
          <div v-for="(v,i) in sampleResult.vocabulary" :key="i" class="mb-2 p-2 bg-orange-50 dark:bg-orange-900/20 rounded text-sm">
            <span class="font-bold text-orange-700 dark:text-orange-400">{{ v.word }}</span>
            <span class="text-gray-500 ml-1">— {{ v.meaning }}</span>
            <div class="text-xs text-gray-400 mt-0.5 italic">"{{ v.sentence }}"</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 历史记录 -->
    <div class="card mt-6">
      <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-4">📋 历史作文</h2>
      <div v-if="history.length === 0" class="text-center text-gray-400 dark:text-gray-500 py-6">暂无记录</div>
      <div v-else class="space-y-2">
        <div v-for="h in history" :key="h.id"
          class="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-800 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer transition"
          @click="loadEssay(h.id)">
          <div><span class="font-medium text-gray-700 dark:text-gray-300">{{ h.title || '无标题' }}</span><span class="text-xs text-gray-400 ml-2">{{ h.created_at }}</span></div>
          <span class="text-sm font-semibold" :class="scoreColorSmall(h.score)">{{ h.score }}分</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { writingAPI } from '../api'

const mode = ref('correct')

// ── 模式 1：批改 ──
const title = ref('')
const content = ref('')
const result = ref(null)
const correcting = ref(false)
const correctError = ref('')

// ── 模式 2：AI 出题 ──
const topicLevel = ref('cet6')
const topicLoading = ref(false)
const generatedTopic = ref(null)
const topicEssay = ref('')
const topicCorrecting = ref(false)
const topicResult = ref(null)

// ── 模式 3：AI 范文 ──
const sampleTopic = ref('')
const sampleLevel = ref('cet6')
const sampleLoading = ref(false)
const sampleResult = ref(null)

const history = ref([])

onMounted(() => loadHistory())

const scoreColor = computed(() => result.value?.score >= 80 ? 'text-green-500' : result.value?.score >= 60 ? 'text-yellow-500' : 'text-red-500')
const scoreBarColor = computed(() => result.value?.score >= 80 ? 'bg-green-500' : result.value?.score >= 60 ? 'bg-yellow-500' : 'bg-red-500')
const scoreColor2 = computed(() => topicResult.value?.score >= 80 ? 'text-green-500' : topicResult.value?.score >= 60 ? 'text-yellow-500' : 'text-red-500')
function scoreColorSmall(s) { return s >= 80 ? 'text-green-600' : s >= 60 ? 'text-yellow-600' : 'text-red-600' }

// ── 模式 1 ──
async function submitEssay() {
  correctError.value = ''; correcting.value = true; result.value = null
  try { const { data } = await writingAPI.correct({ title: title.value, content: content.value }); result.value = data; loadHistory() }
  catch (err) { correctError.value = err.response?.data?.error || '批改失败' }
  finally { correcting.value = false }
}

// ── 模式 2 ──
async function fetchTopic() {
  topicLoading.value = true; generatedTopic.value = null; topicEssay.value = ''; topicResult.value = null
  try { const { data } = await writingAPI.topic({ level: topicLevel.value }); generatedTopic.value = data }
  catch { alert('题目生成失败') }
  finally { topicLoading.value = false }
}

async function submitTopicEssay() {
  topicCorrecting.value = true; topicResult.value = null
  try {
    const { data } = await writingAPI.correct({ title: generatedTopic.value.title, content: topicEssay.value })
    topicResult.value = data; loadHistory()
  } catch (err) { alert(err.response?.data?.error || '批改失败') }
  finally { topicCorrecting.value = false }
}

// ── 模式 3 ──
async function fetchSample() {
  sampleLoading.value = true; sampleResult.value = null
  try { const { data } = await writingAPI.generate({ topic: sampleTopic.value, level: sampleLevel.value }); sampleResult.value = data }
  catch { alert('范文生成失败') }
  finally { sampleLoading.value = false }
}

async function loadHistory() { try { const { data } = await writingAPI.history(); history.value = data.essays } catch { /* */ } }
async function loadEssay(id) {
  try { const { data } = await writingAPI.detail(id); result.value = data.essay; title.value = data.essay.title || ''; content.value = data.essay.content; mode.value = 'correct'; window.scrollTo({ top: 0, behavior: 'smooth' }) } catch { /* */ }
}
</script>
