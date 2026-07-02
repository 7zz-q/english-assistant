<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="page-title mb-0">📝 背单词</h1>
      <div class="flex items-center gap-3">
        <button @click="showWordList = !showWordList" class="text-sm text-gray-500 dark:text-gray-400 hover:text-gray-700 transition">
          {{ showWordList ? '收起词表' : '📋 词表' }}
        </button>
        <button @click="showAddModal = true" class="btn-primary text-sm py-1.5">+ 添加单词</button>
      </div>
    </div>

    <!-- ====== 学习准备页 ====== -->
    <div v-if="!learning" class="space-y-6">
      <!-- 1. 选择词源 -->
      <div class="card">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-3">📚 选择词库</h2>
        <div class="flex flex-wrap gap-2">
          <button @click="source = 'my'; loadSourceWords()"
            class="px-4 py-2.5 rounded-lg text-sm font-medium transition"
            :class="source === 'my' ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'">
            📝 我的词本 ({{ stats.todayReview }} 待复习)
          </button>
          <button v-for="lv in bankLevels" :key="lv.level"
            @click="source = lv.level; loadSourceWords()"
            class="px-4 py-2.5 rounded-lg text-sm font-medium transition"
            :class="source === lv.level ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'">
            {{ lv.label }} <span class="opacity-60 text-xs">({{ lv.count }})</span>
          </button>
        </div>
      </div>

      <!-- 2. 顺序 / 乱序 -->
      <div class="card">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-3">🔀 顺序选择</h2>
        <div class="flex gap-2">
          <button @click="randomMode = false"
            class="px-6 py-2.5 rounded-lg text-sm font-medium transition"
            :class="!randomMode ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'">
            📋 顺序背诵
          </button>
          <button @click="randomMode = true"
            class="px-6 py-2.5 rounded-lg text-sm font-medium transition"
            :class="randomMode ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'">
            🎲 乱序背诵
          </button>
        </div>
      </div>

      <!-- 3. 数量 -->
      <div class="card">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-3">🎯 数量</h2>
        <div class="flex flex-wrap gap-2">
          <button v-for="n in [5, 10, 15, 20, 30, 50]" :key="n"
            @click="dailyCount = n"
            class="px-5 py-2.5 rounded-lg text-sm font-medium transition"
            :class="dailyCount === n ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'">
            {{ n }} 个
          </button>
        </div>
      </div>

      <!-- 4. 开始按钮 + 来源统计 -->
      <div class="text-center py-6">
        <div class="text-sm text-gray-500 dark:text-gray-400 mb-3">
          {{ sourceLabel }} · {{ randomMode ? '乱序' : '顺序' }} ·
          可用 {{ availableCount }} 词 · 本次拟学 {{ Math.min(dailyCount, availableCount) }} 个
        </div>
        <button @click="startLearning"
          class="px-10 py-4 bg-blue-600 text-white text-lg font-bold rounded-2xl hover:bg-blue-700 transition shadow-lg hover:shadow-xl disabled:opacity-50"
          :disabled="availableCount === 0 || importingWords">
          {{ importingWords ? '⏳ 准备中...' : availableCount > 0 ? `🚀 开始学习（${Math.min(dailyCount, availableCount)} 个）` : '📭 该词库暂无可用单词' }}
        </button>
        <p v-if="source === 'my' && availableCount === 0 && stats.total > 0"
          class="text-sm text-gray-400 mt-3">所有单词都已掌握或不在复习周期内 🎉</p>
        <p v-if="source === 'my' && stats.total === 0"
          class="text-sm text-gray-400 mt-3">去 <router-link to="/wordbank" class="text-blue-500 underline">词库</router-link> 添加单词</p>
      </div>

      <!-- 我的词本（可折叠，任何词源下都能看到） -->
      <div v-if="showWordList" class="card">
        <div class="flex items-center gap-3 mb-4 flex-wrap">
          <select v-model="wordFilter" class="input-field w-auto py-1.5 text-sm">
            <option value="">全部</option>
            <option value="0">新学</option>
            <option value="1">学习中</option>
            <option value="2">已掌握</option>
          </select>
          <span class="text-xs text-gray-400">{{ filteredWords.length }} 个</span>
          <button @click="wordFilter = '2'" v-if="wordFilter !== '2' && stats.mastered > 0"
            class="text-xs text-green-600 dark:text-green-400 hover:underline shrink-0">
            🟢 已学会 {{ stats.mastered }}
          </button>
          <div class="flex-1"></div>
          <button @click="selectingWords = !selectingWords"
            class="text-xs px-2.5 py-1 rounded border border-gray-200 dark:border-gray-600 text-gray-500 hover:bg-gray-50 dark:hover:bg-gray-800 transition">
            {{ selectingWords ? '取消' : '☐ 批量操作' }}
          </button>
        </div>

        <!-- 批量操作栏 -->
        <div v-if="selectingWords" class="flex items-center gap-2 mb-3 p-2 bg-blue-50 dark:bg-blue-900/20 rounded-lg text-xs">
          <label class="flex items-center gap-1 cursor-pointer">
            <input type="checkbox" :checked="allFilteredSelected" @change="toggleAllFiltered" />
            <span class="text-gray-600 dark:text-gray-300">全选本页</span>
          </label>
          <span class="text-gray-400">|</span>
          <button @click="batchDelete" :disabled="selectedWordIds.size === 0"
            class="px-2 py-1 rounded bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-400 hover:bg-red-100 disabled:opacity-30">
            🗑 批量删除 ({{ selectedWordIds.size }})
          </button>
          <button @click="batchReset" :disabled="selectedWordIds.size === 0"
            class="px-2 py-1 rounded bg-yellow-50 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-400 hover:bg-yellow-100 disabled:opacity-30">
            🔄 重置进度 ({{ selectedWordIds.size }})
          </button>
          <button @click="batchUnMaster" :disabled="selectedWordIds.size === 0"
            class="px-2 py-1 rounded bg-green-50 dark:bg-green-900/30 text-green-700 dark:text-green-400 hover:bg-green-100 disabled:opacity-30">
            ↩ 取消掌握 ({{ selectedWordIds.size }})
          </button>
          <button @click="selectedWordIds = new Set()" v-if="selectedWordIds.size > 0"
            class="px-2 py-1 text-gray-400 hover:text-gray-600">清空选择</button>
        </div>

        <div class="space-y-1 max-h-96 overflow-y-auto">
          <div v-for="w in filteredWords" :key="w.id"
            class="flex items-center justify-between p-2.5 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 transition text-sm">
            <div class="flex items-center gap-2">
              <input v-if="selectingWords" type="checkbox" :checked="selectedWordIds.has(w.id)" @change="toggleWordSelect(w.id)"
                class="shrink-0" />
              <div>
                <span class="font-semibold text-gray-800 dark:text-gray-100">{{ w.word }}</span>
                <span class="text-gray-400 ml-2">{{ w.phonetic }}</span>
                <span class="text-gray-500 ml-2">{{ w.meaning?.slice(0, 20) }}{{ w.meaning?.length > 20 ? '…' : '' }}</span>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-xs px-2 py-0.5 rounded-full" :class="statusBadge(w.status)">{{ statusLabel(w.status) }}</span>
              <button v-if="w.status === 2" @click="unMasterWord(w.id)" class="text-xs text-green-500 hover:text-green-700" title="取消掌握，回到背诵列表">↩</button>
              <button @click="deleteWord(w.id)" class="text-gray-300 dark:text-gray-600 hover:text-red-500 text-xs">✕</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 答题模式 ====== -->
    <div v-if="learning" class="max-w-2xl mx-auto space-y-6">
      <div class="flex items-center gap-3">
        <span class="text-sm text-gray-500 shrink-0">{{ currentIndex + 1 }} / {{ learnList.length }}</span>
        <div class="flex-1 bg-gray-200 dark:bg-gray-700 rounded-full h-2">
          <div class="h-2 rounded-full bg-blue-500 transition-all duration-300"
            :style="{ width: ((currentIndex / learnList.length) * 100) + '%' }"></div>
        </div>
        <button @click="endLearning" class="text-sm text-gray-400 hover:text-gray-600">结束</button>
      </div>

      <div v-if="currentWord" class="card text-center !p-8">
        <div class="mb-2">
          <div class="text-5xl font-bold text-gray-800 dark:text-gray-100 mb-1">{{ currentWord.word }}</div>
          <div class="flex items-center justify-center gap-2">
            <span class="text-gray-500 dark:text-gray-400 text-lg">{{ data?.phonetic || currentWord.phonetic }}</span>
            <button @click="speak" class="text-2xl hover:scale-110 transition" title="发音">🔊</button>
          </div>
        </div>

        <div v-if="loading" class="text-sm text-gray-400 py-8 animate-pulse">⏳ AI 正在准备题目...</div>
        <div v-else-if="data" class="mt-4">
          <div class="p-4 bg-gray-50 dark:bg-gray-800 rounded-xl text-sm italic text-gray-600 dark:text-gray-300 leading-relaxed">
            📖 {{ data.example }}
            <div class="text-xs text-gray-400 mt-1 not-italic">{{ data.example_cn }}</div>
          </div>
        </div>

        <div v-if="!loading && data" class="mt-6 grid grid-cols-1 gap-2.5">
          <button v-for="(opt, oi) in shuffledOptions" :key="oi"
            @click="selectOption(opt)"
            class="w-full text-left px-5 py-4 rounded-xl text-sm font-medium transition border-2"
            :class="getOptionClass(opt)" :disabled="optionSelected">
            <span class="font-mono font-bold mr-2 text-xs opacity-50">{{ letters[oi] }}</span>
            {{ opt.text }}
            <span v-if="optionSelected && opt.correct" class="float-right text-green-500">✓ 正确</span>
            <span v-if="optionSelected && opt === selectedOption && !opt.correct" class="float-right text-red-400">✗</span>
          </button>
        </div>

        <div v-if="optionSelected && data" class="mt-6 pt-6 border-t dark:border-gray-700 text-left space-y-5">
          <div>
            <h4 class="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">📖 全部释义</h4>
            <div class="space-y-1.5">
              <div v-for="(m, mi) in data.meanings" :key="mi"
                class="flex items-start gap-2 text-sm p-2 rounded"
                :class="m.isKey ? 'bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800' : ''">
                <span v-if="m.isKey" class="text-yellow-500 text-xs shrink-0 mt-0.5">⭐</span>
                <span v-else class="w-4 shrink-0"></span>
                <div>
                  <span class="text-xs text-gray-400">{{ m.type }}.</span>
                  <span class="text-gray-800 dark:text-gray-200 ml-1">{{ m.text }}</span>
                  <span v-if="m.isKey" class="text-xs text-yellow-600 dark:text-yellow-400 ml-1">（常考）</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="data.similar_words?.length">
            <h4 class="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">🔍 形近词辨析</h4>
            <div class="space-y-2">
              <div v-for="(s, si) in data.similar_words" :key="si"
                class="flex items-start gap-2 p-2 bg-orange-50 dark:bg-orange-900/20 rounded-lg text-sm">
                <span class="font-bold text-orange-600 dark:text-orange-400 shrink-0">{{ s.word }}</span>
                <div>
                  <span class="text-gray-600 dark:text-gray-300">{{ s.meaning }}</span>
                  <div class="text-xs text-gray-400 mt-0.5">👉 {{ s.difference }}</div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="data.derived_words?.length">
            <h4 class="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">🌱 派生词</h4>
            <div class="flex flex-wrap gap-2">
              <span v-for="(d, di) in data.derived_words" :key="di"
                class="px-3 py-1.5 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg text-sm">
                <span class="font-semibold text-green-700 dark:text-green-400">{{ d.word }}</span>
                <span class="text-xs text-gray-400 ml-1">{{ d.type }}</span>
                <span class="text-gray-600 dark:text-gray-300 ml-1">{{ d.meaning }}</span>
              </span>
            </div>
          </div>

          <div class="flex items-center gap-2 pt-2">
            <button @click="markMastered"
              class="py-2.5 px-4 rounded-lg text-sm font-medium transition border-2
                border-green-300 dark:border-green-700 text-green-600 dark:text-green-400
                hover:bg-green-50 dark:hover:bg-green-900/20">
              ✅ 已学会
            </button>
            <div class="flex-1 flex items-center gap-3">
              <button @click="goToPrev" :disabled="currentIndex === 0"
                class="flex-1 py-3 rounded-lg text-sm font-medium transition border-2 disabled:opacity-30 disabled:cursor-not-allowed
                  border-gray-200 dark:border-gray-600 text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-800">
                ← 上一题
              </button>
              <button @click="goToNext"
                class="flex-1 py-3 rounded-lg text-sm font-medium text-white transition"
                :class="currentIndex < learnList.length - 1 ? 'bg-blue-600 hover:bg-blue-700' : 'bg-green-600 hover:bg-green-700'">
                {{ currentIndex < learnList.length - 1 ? '下一题 →' : '🎉 完成学习' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 学习完成 -->
    <div v-if="finished" class="max-w-md mx-auto card text-center !p-10 space-y-4">
      <div class="text-6xl">🎉</div>
      <h2 class="text-2xl font-bold text-gray-800 dark:text-gray-100">学习完成！</h2>
      <div class="text-gray-500 dark:text-gray-400 text-sm space-y-1">
        <p>本次学习 <span class="font-bold text-blue-600">{{ learnList.length }}</span> 个单词</p>
        <p>词源：{{ sourceLabel }} · {{ randomMode ? '乱序' : '顺序' }}</p>
        <p>已记录到艾宾浩斯复习计划中</p>
      </div>
      <button @click="finishAndExit" class="btn-primary w-full mt-4">返回</button>
    </div>

    <!-- 添加单词弹窗 -->
    <div v-if="showAddModal" class="fixed inset-0 bg-black/40 z-50 flex items-center justify-center" @click.self="showAddModal = false">
      <div class="bg-white dark:bg-gray-900 rounded-xl p-6 w-full max-w-sm shadow-xl">
        <h3 class="text-lg font-semibold text-gray-800 dark:text-gray-100 mb-4">➕ 添加新单词</h3>
        <form @submit.prevent="addWord" class="space-y-3">
          <input v-model="newWord.word" class="input-field" placeholder="输入单词（必填）" required />
          <input v-model="newWord.meaning" class="input-field" placeholder="释义（留空 AI 补全）" />
          <div class="flex gap-2 pt-2">
            <button type="submit" class="btn-primary flex-1 text-sm" :disabled="addingWord">添加</button>
            <button type="button" @click="showAddModal = false" class="btn-secondary text-sm">取消</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { wordAPI } from '../api'

const learning = ref(false)
const finished = ref(false)
const showWordList = ref(false)
const showAddModal = ref(false)
const loading = ref(false)
const optionSelected = ref(false)
const selectedOption = ref(null)
const importingWords = ref(false)

const source = ref('my')           // 'my' | 'cet4' | 'cet6' | 'tem4' | 'tem8' | ...
const randomMode = ref(true)       // true=乱序 false=顺序
const dailyCount = ref(10)
const wordFilter = ref('')
const allWords = ref([])
const reviewWords = ref([])
const selectedWordIds = ref(new Set())
const selectingWords = ref(false)
const learnList = ref([])
const currentIndex = ref(0)
const sourceWordCache = ref([])    // 当前选中词源的原始词列表

const data = ref(null)
const shuffledOptions = ref([])
const cache = {}
const newWord = ref({ word: '', meaning: '' })
const addingWord = ref(false)
const letters = ['A', 'B', 'C', 'D']
const bankLevels = ref([])

onMounted(async () => {
  try { const r = await wordAPI.list({ limit: 5000 }); allWords.value = r.data.words } catch { /* */ }
  try { const r = await wordAPI.reviewList(); reviewWords.value = r.data.words } catch { /* */ }
  try { const r = await wordAPI.bankLevels(); bankLevels.value = r.data.stats.map(s => {
    const labels = { cet4:'CET-4', cet6:'CET-6', tem4:'专四', tem8:'专八', toefl:'TOEFL', gre:'GRE', junior:'中考', cet46:'四六级' }
    return { level: s.level, count: s.count, label: labels[s.level] || s.level }
  }) } catch { /* */ }
  loadSourceWords()
})

const stats = computed(() => ({
  total: allWords.value.length,
  mastered: allWords.value.filter(w => w.status === 2).length,
  learning: allWords.value.filter(w => w.status === 1).length,
  todayReview: reviewWords.value.length,
}))

const filteredWords = computed(() => {
  if (!wordFilter.value) return allWords.value
  return allWords.value.filter(w => w.status === Number(wordFilter.value))
})

const currentWord = computed(() => learnList.value[currentIndex.value] || null)

const sourceLabel = computed(() => {
  if (source.value === 'my') return '我的词本'
  const lv = bankLevels.value.find(l => l.level === source.value)
  return lv?.label || source.value
})

const availableCount = computed(() => {
  if (source.value === 'my') return reviewWords.value.length
  return sourceWordCache.value.length
})

// 切换词源时加载
async function loadSourceWords() {
  if (source.value === 'my') {
    sourceWordCache.value = reviewWords.value
  } else {
    try {
      const { data } = await wordAPI.bankWords({ level: source.value, limit: 50000 })
      sourceWordCache.value = data.words || []
    } catch { sourceWordCache.value = [] }
  }
}

// 开始学习
async function startLearning() {
  let words = []
  if (source.value === 'my') {
    words = reviewWords.value.slice(0, dailyCount.value)
    if (words.length === 0) return
    learnList.value = words
  } else {
    // 公共词库 → 先批量自动导入到个人词本
    importingWords.value = true
    const raw = sourceWordCache.value.slice(0, dailyCount.value * 2) // 多取一些，防止导入失败不够
    const wordList = raw.map(w => w.word)
    try {
      await wordAPI.bankImport({ wordList })
    } catch { /* */ }
    // 重新从个人词本获取刚导入的词
    try {
      const { data } = await wordAPI.reviewList()
      reviewWords.value = data.words || []
      words = reviewWords.value.slice(0, dailyCount.value)
    } catch { words = [] }
    importingWords.value = false
    if (words.length === 0) return
  }

  // 乱序
  if (randomMode.value) {
    learnList.value = shuffleArray([...words])
  } else {
    learnList.value = [...words]
  }

  submittedIndices.value = {}
  savedAnswers.value = {}
  learning.value = true
  finished.value = false
  currentIndex.value = 0
  await loadCurrentWord()
}

async function loadCurrentWord() {
  const word = currentWord.value
  if (!word) return
  loading.value = true
  optionSelected.value = false
  selectedOption.value = null
  data.value = null
  shuffledOptions.value = []

  if (cache[word.word]) {
    data.value = cache[word.word]
    shuffledOptions.value = shuffleArray([...data.value.options])
    loading.value = false
    const saved = savedAnswers.value[currentIndex.value]
    if (saved) { selectedOption.value = saved; optionSelected.value = true }
    prefetchNext(currentIndex.value)
    return
  }

  try {
    const res = await wordAPI.analyze(word.word)
    data.value = res.data
    cache[word.word] = res.data
    shuffledOptions.value = shuffleArray([...res.data.options])
  } catch {
    data.value = {
      phonetic: word.phonetic || '',
      meanings: [{ text: word.meaning || '释义暂缺', isKey: true, type: '' }],
      example: word.example || '',
      example_cn: '',
      similar_words: [],
      derived_words: [],
      options: [
        { text: word.meaning || '正确释义', correct: true },
        { text: '（干扰项暂缺）', correct: false },
        { text: '（干扰项暂缺）', correct: false },
        { text: '（干扰项暂缺）', correct: false },
      ],
    }
    shuffledOptions.value = shuffleArray([...data.value.options])
  }
  const saved = savedAnswers.value[currentIndex.value]
  if (saved) { selectedOption.value = saved; optionSelected.value = true }
  loading.value = false
  prefetchNext(currentIndex.value)
}

function prefetchNext(idx) {
  const end = Math.min(idx + 4, learnList.value.length)
  for (let i = idx + 1; i < end; i++) {
    const w = learnList.value[i]
    if (!w || cache[w.word]) continue
    wordAPI.analyze(w.word).then(r => { cache[w.word] = r.data }).catch(() => {})
  }
}

function selectOption(opt) {
  if (optionSelected.value) return
  selectedOption.value = opt
  optionSelected.value = true
  savedAnswers.value[currentIndex.value] = opt
}

function getOptionClass(opt) {
  if (!optionSelected.value) {
    return 'border-gray-200 dark:border-gray-600 hover:border-blue-400 hover:bg-blue-50 dark:hover:bg-blue-900/20 text-gray-700 dark:text-gray-200'
  }
  if (opt.correct) return 'border-green-400 bg-green-50 dark:bg-green-900/30 text-green-700 dark:text-green-300'
  if (opt === selectedOption.value && !opt.correct) return 'border-red-400 bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-300'
  return 'border-gray-200 dark:border-gray-600 text-gray-400'
}

const submittedIndices = ref({})
const savedAnswers = ref({})

async function goToNext() {
  if (!submittedIndices.value[currentIndex.value]) {
    const quality = selectedOption.value?.correct ? 3 : 1
    try { await wordAPI.review(currentWord.value.id, quality) } catch { /* */ }
    submittedIndices.value[currentIndex.value] = true
  }

  if (currentIndex.value < learnList.value.length - 1) {
    currentIndex.value++
    await loadCurrentWord()
  } else {
    learning.value = false
    finished.value = true
  }
}

function goToPrev() {
  if (currentIndex.value > 0) {
    currentIndex.value--
    loadCurrentWord()
  }
}

async function markMastered() {
  // 标记为已掌握（艾宾浩斯中 interval=999, status=2）
  try { await wordAPI.review(currentWord.value.id, 4) } catch { /* */ }
  // 刷新数据
  try { const r = await wordAPI.list({ limit: 5000 }); allWords.value = r.data.words } catch { /* */ }
  try { const r = await wordAPI.reviewList(); reviewWords.value = r.data.words } catch { /* */ }
  // 自动跳到下一个
  if (currentIndex.value < learnList.value.length - 1) {
    currentIndex.value++
    await loadCurrentWord()
  } else {
    learning.value = false
    finished.value = true
  }
}

function endLearning() { learning.value = false; finished.value = false }
async function finishAndExit() {
  finished.value = false
  try { const r = await wordAPI.list({ limit: 50000 }); allWords.value = r.data.words } catch { /* */ }
  try { const r = await wordAPI.reviewList(); reviewWords.value = r.data.words } catch { /* */ }
  // 自动切到我的词本 + 打开词表，方便查看刚掌握的单词
  source.value = 'my'
  showWordList.value = true
}

function speak() {
  const word = currentWord.value?.word
  if (!word || !window.speechSynthesis) return
  const u = new SpeechSynthesisUtterance(word)
  u.lang = 'en-US'; u.rate = 0.8
  speechSynthesis.speak(u)
}

async function addWord() {
  addingWord.value = true
  try { await wordAPI.add(newWord.value); showAddModal.value = false; newWord.value = { word: '', meaning: '' }; const r = await wordAPI.list({ limit: 5000 }); allWords.value = r.data.words; const r2 = await wordAPI.reviewList(); reviewWords.value = r2.data.words } catch(e) { alert(e.response?.data?.error || '添加失败') }
  addingWord.value = false
}

async function deleteWord(id) {
  if (!confirm('确定删除？')) return
  await wordAPI.remove(id)
  allWords.value = allWords.value.filter(w => w.id !== id)
  reviewWords.value = reviewWords.value.filter(w => w.id !== id)
}

function statusBadge(s) { return s === 2 ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400' : s === 1 ? 'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-400' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400' }
function statusLabel(s) { return s === 2 ? '已掌握' : s === 1 ? '学习中' : '新学' }

// ── 批量操作 ──
const allFilteredSelected = computed(() => {
  if (filteredWords.value.length === 0) return false
  return filteredWords.value.every(w => selectedWordIds.value.has(w.id))
})

function toggleAllFiltered(e) {
  if (e.target.checked) {
    const s = new Set(selectedWordIds.value)
    filteredWords.value.forEach(w => s.add(w.id))
    selectedWordIds.value = s
  } else {
    const s = new Set(selectedWordIds.value)
    filteredWords.value.forEach(w => s.delete(w.id))
    selectedWordIds.value = s
  }
}

function toggleWordSelect(id) {
  const s = new Set(selectedWordIds.value)
  if (s.has(id)) s.delete(id); else s.add(id)
  selectedWordIds.value = s
}

async function batchDelete() {
  if (selectedWordIds.value.size === 0) return
  if (!confirm(`确定删除选中的 ${selectedWordIds.value.size} 个单词？`)) return
  for (const id of selectedWordIds.value) {
    try { await wordAPI.remove(id) } catch { /* */ }
  }
  allWords.value = allWords.value.filter(w => !selectedWordIds.value.has(w.id))
  reviewWords.value = reviewWords.value.filter(w => !selectedWordIds.value.has(w.id))
  selectedWordIds.value = new Set()
}

async function batchReset() {
  if (selectedWordIds.value.size === 0) return
  if (!confirm(`确定重置选中 ${selectedWordIds.value.size} 个单词的学习进度？（回到"新学"状态）`)) return
  for (const id of selectedWordIds.value) {
    try { await wordAPI.review(id, 0) } catch { /* */ }
  }
  // refresh
  try { const r = await wordAPI.list({ limit: 5000 }); allWords.value = r.data.words } catch { /* */ }
  try { const r = await wordAPI.reviewList(); reviewWords.value = r.data.words } catch { /* */ }
  selectedWordIds.value = new Set()
}

async function batchUnMaster() {
  if (selectedWordIds.value.size === 0) return
  if (!confirm(`确定将选中 ${selectedWordIds.value.size} 个单词取消掌握？（回到背诵列表）`)) return
  for (const id of selectedWordIds.value) {
    try { await wordAPI.review(id, 0) } catch { /* */ }
  }
  try { const r = await wordAPI.list({ limit: 5000 }); allWords.value = r.data.words } catch { /* */ }
  try { const r = await wordAPI.reviewList(); reviewWords.value = r.data.words } catch { /* */ }
  selectedWordIds.value = new Set()
}

async function unMasterWord(id) {
  if (!confirm('将此单词取消掌握，重新加入背诵列表？')) return
  try { await wordAPI.review(id, 0) } catch { /* */ }
  try { const r = await wordAPI.list({ limit: 50000 }); allWords.value = r.data.words } catch { /* */ }
  try { const r = await wordAPI.reviewList(); reviewWords.value = r.data.words } catch { /* */ }
}

function shuffleArray(arr) {
  const a = [...arr]
  for (let i = a.length - 1; i > 0; i--) { const j = Math.floor(Math.random() * (i + 1)); [a[i], a[j]] = [a[j], a[i]] }
  return a
}
</script>
