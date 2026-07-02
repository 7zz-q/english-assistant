<template>
  <div>
    <h1 class="page-title">📋 历年真题</h1>

    <div class="card mb-4">
      <div class="flex flex-wrap items-center gap-3">
        <button v-for="t in types" :key="t.value"
          @click="filterType = t.value; loadPapers(1)"
          class="px-4 py-2 rounded-lg text-sm font-medium transition"
          :class="filterType === t.value ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'">
          {{ t.label }} ({{ t.count }})
        </button>
      </div>
    </div>

    <div v-if="papers.length === 0" class="card text-center text-gray-400 dark:text-gray-500 py-16">📭 暂无真题</div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4 mb-6">
      <div v-for="p in papers" :key="p.id"
        class="card hover:shadow-md dark:hover:border-gray-500 transition cursor-pointer border-2"
        :class="selectedPaper?.id === p.id ? 'border-blue-500 ring-2 ring-blue-100 dark:ring-blue-900' : 'hover:border-blue-300 dark:hover:border-blue-600'"
        @click="selectPaper(p)">
        <div class="flex items-start justify-between mb-2">
          <span class="text-xs px-2 py-0.5 rounded-full font-medium" :class="typeBadge(p.type)">{{ p.type.toUpperCase() }}</span>
          <span class="text-xs text-gray-400">{{ p.year }}/{{ String(p.month).padStart(2,'0') }}</span>
        </div>
        <h3 class="font-semibold text-gray-800 dark:text-gray-100 text-sm leading-snug">{{ p.title }}</h3>
      </div>
    </div>

    <!-- 试卷详情 -->
    <div v-if="selectedPaper" class="card border-2 border-blue-200 dark:border-blue-800">
      <div class="flex flex-wrap items-center justify-between mb-6 gap-3">
        <div>
          <h2 class="text-xl font-bold text-gray-800 dark:text-gray-100">{{ selectedPaper.title }}</h2>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">{{ selectedPaper.sections.length }} 部分 · {{ totalQuestions }} 题 · 已答 {{ answeredCount }} 题</p>
        </div>
        <div class="flex items-center gap-2 flex-wrap">
          <button v-if="!checked && answeredCount > 0" @click="checkAnswers"
            class="px-4 py-2 rounded-lg text-sm font-medium bg-yellow-500 text-white hover:bg-yellow-600 transition">📝 对答案</button>
          <button v-if="checked && !revealed" @click="revealAll"
            class="px-4 py-2 rounded-lg text-sm font-medium bg-purple-500 text-white hover:bg-purple-600 transition">🔍 查看全部答案</button>
          <button v-if="revealed" @click="hideAnswers"
            class="px-4 py-2 rounded-lg text-sm font-medium bg-gray-300 dark:bg-gray-600 text-gray-700 dark:text-gray-200 hover:bg-gray-400 dark:hover:bg-gray-500 transition">🙈 隐藏答案</button>
          <button @click="resetPaper" class="px-3 py-2 rounded-lg text-sm text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 transition">🔄 重做</button>
          <button @click="selectedPaper = null" class="btn-secondary text-sm">关闭</button>
        </div>
      </div>

      <!-- 得分条 -->
      <div v-if="checked" class="mb-6 p-4 rounded-xl text-center"
        :class="correctRate >= 80 ? 'bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800' : correctRate >= 60 ? 'bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800' : 'bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800'">
        <span class="text-lg font-bold" :class="correctRate >= 80 ? 'text-green-600 dark:text-green-400' : correctRate >= 60 ? 'text-yellow-600 dark:text-yellow-400' : 'text-red-600 dark:text-red-400'">
          得分：{{ correctCount }} / {{ answeredCount }}（正确率 {{ correctRate }}%）
        </span>
        <div v-if="revealed" class="text-xs text-gray-400 mt-1">全部答案已显示</div>
      </div>

      <!-- 音频播放器（有听力 section 时显示） -->
      <div v-if="selectedPaper.sections.some(s => s.type === 'listening')"
           class="mb-6 p-4 bg-gray-50 dark:bg-gray-800 rounded-xl border dark:border-gray-700">
        <div class="flex items-center gap-3 mb-2 flex-wrap">
          <button @click="playAudio(currentListeningUrl)"
                  class="w-10 h-10 rounded-full flex items-center justify-center bg-blue-500 text-white hover:bg-blue-600 transition text-lg">
            <span v-if="!audioPlaying">▶</span>
            <span v-else>⏸</span>
          </button>
          <span class="text-sm text-gray-500 dark:text-gray-400 font-mono">
            {{ formatTime(audioCurrentTime) }} / {{ formatTime(audioDuration) }}
          </span>
          <span class="text-xs text-gray-400 dark:text-gray-500 ml-2 truncate max-w-xs">{{ currentListeningLabel }}</span>
        </div>
        <div class="w-full h-2 bg-gray-200 dark:bg-gray-600 rounded-full cursor-pointer"
             @click="seekAudio($event.offsetX / $event.target.offsetWidth * 100)">
          <div class="h-2 bg-blue-500 rounded-full transition-all duration-100"
               :style="{ width: audioDuration ? (audioCurrentTime / audioDuration * 100) + '%' : '0%' }"></div>
        </div>
        <audio ref="audioRef" :src="activeAudioUrl" preload="auto"
               @timeupdate="onAudioTimeUpdate" @loadedmetadata="onAudioLoadedMetadata"
               @ended="onAudioEnded" class="hidden"></audio>
      </div>

      <!-- 各部分 -->
      <div v-for="(section, si) in selectedPaper.sections" :key="si" class="mb-8 last:mb-0">
        <!-- 写作 -->
        <template v-if="section.type === 'writing'">
          <h3 class="text-lg font-semibold text-orange-600 dark:text-orange-400 mb-3">✍️ {{ section.title }}</h3>
          <div v-for="q in section.questions" :key="q.id" class="p-4 bg-orange-50 dark:bg-orange-900/20 rounded-lg text-sm text-gray-700 dark:text-gray-300 leading-relaxed whitespace-pre-wrap">{{ q.stem }}</div>
        </template>

        <!-- 选词填空 (无stem) -->
        <template v-if="section.type === 'reading' && !section.questions?.[0]?.stem">
          <h3 class="text-lg font-semibold text-blue-600 dark:text-blue-400 mb-3">📖 {{ section.title }}</h3>
          <div v-if="section.passage" class="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg mb-4 text-sm text-gray-700 dark:text-gray-200 leading-relaxed whitespace-pre-wrap">{{ section.passage }}</div>
          <div class="grid grid-cols-5 md:grid-cols-10 gap-1.5 text-xs text-gray-500 dark:text-gray-400 mb-4">
            <div v-for="q in section.questions" :key="'opt-'+q.id" class="text-center">
              <span class="font-bold">{{ q.id }}.</span>
              <span v-if="revealed" class="font-bold text-green-600 dark:text-green-400 ml-1">{{ q.answer }}</span>
              <span v-else-if="userAnswer(si, q.id)" class="text-blue-600 dark:text-blue-400 ml-1">{{ userAnswer(si, q.id) }}</span>
            </div>
          </div>
          <div class="flex flex-wrap gap-1.5">
            <button v-for="q in section.questions" :key="q.id" @click="toggleAnswer(si, q)"
              class="w-10 h-10 rounded-lg text-xs font-bold transition border-2" :class="getClozeBtnClass(si, q)" :disabled="checked && revealed">
              {{ q.id }}
            </button>
          </div>
          <div v-if="activeCloze" class="mt-3 p-3 bg-white dark:bg-gray-800 border dark:border-gray-600 rounded-lg flex flex-wrap gap-2">
            <button v-for="opt in activeClozeQ.options" :key="opt"
              @click="userAnswers[`${activeClozeSi}_${activeClozeQ.id}`] = opt.charAt(0); activeCloze = null"
              class="px-3 py-1.5 rounded-lg text-xs font-medium bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-blue-100 dark:hover:bg-blue-900/40 transition">{{ opt }}</button>
            <button @click="activeCloze = null" class="text-xs text-gray-400 dark:text-gray-500 hover:text-gray-600 ml-2">取消</button>
          </div>
        </template>

        <!-- 仔细阅读 (有stem) -->
        <template v-if="section.type === 'reading' && section.questions?.[0]?.stem">
          <h3 class="text-lg font-semibold text-blue-600 dark:text-blue-400 mb-3">📖 {{ section.title }}</h3>
          <div v-if="section.passage" class="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg mb-4 text-sm text-gray-700 dark:text-gray-200 leading-relaxed whitespace-pre-wrap">{{ section.passage }}</div>
          <div class="space-y-3">
            <div v-for="q in section.questions" :key="q.id" class="p-3 border dark:border-gray-700 rounded-lg" :class="qBoxClass(si, q)">
              <div class="flex items-start gap-3">
                <span class="font-bold text-gray-400 dark:text-gray-500 w-6 shrink-0">{{ q.id }}.</span>
                <div class="flex-1">
                  <div v-if="q.stem" class="text-sm text-gray-700 dark:text-gray-200 mb-2">{{ q.stem }}</div>
                  <div class="flex flex-wrap gap-2">
                    <button v-for="opt in q.options" :key="opt" @click="userAnswers[`${si}_${q.id}`] = opt.charAt(0)"
                      class="px-3 py-1.5 rounded-lg text-xs font-medium transition" :class="getOptClass(si, q, opt)">{{ opt }}</button>
                  </div>
                </div>
              </div>
              <div v-if="checked" class="mt-2 text-xs pl-9">
                <span v-if="userAnswer(si, q.id) === q.answer" class="text-green-600 dark:text-green-400">✅ 正确！</span>
                <span v-else class="text-red-600 dark:text-red-400">❌ {{ userAnswer(si, q.id) ? '你的答案：' + userAnswer(si, q.id) + '，' : '未作答，' }}正确答案：{{ q.options?.find(o => o.startsWith(q.answer)) || q.answer }}</span>
              </div>
            </div>
          </div>
        </template>

        <!-- 段落匹配 -->
        <template v-if="section.type === 'matching'">
          <h3 class="text-lg font-semibold text-teal-600 dark:text-teal-400 mb-3">📑 {{ section.title }}</h3>
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <div class="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg text-xs text-gray-600 dark:text-gray-400 leading-relaxed max-h-96 overflow-y-auto">
              <div v-for="p in section.passage.split('\n').filter(l => l.trim())" :key="p.charAt(0)"
                class="mb-2 p-2 rounded" :class="getMatchingParaClass(si, p)">{{ p }}</div>
            </div>
            <div class="space-y-2 max-h-96 overflow-y-auto">
              <div v-for="q in section.questions" :key="q.id" class="p-2 text-xs border dark:border-gray-700 rounded-lg" :class="qBoxClass(si, q)">
                <div class="font-medium text-gray-700 dark:text-gray-200 mb-1">{{ q.id }}. {{ q.stem }}</div>
                <select v-model="userAnswers[`${si}_${q.id}`]" class="text-xs border dark:border-gray-600 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 rounded px-2 py-1 w-full">
                  <option value="">-- 选择段落 --</option>
                  <option v-for="p in section.passage.split('\n').filter(l => l.trim())" :key="p.charAt(0)" :value="p.charAt(0)">{{ p.charAt(0) }}</option>
                </select>
                <div v-if="checked" class="mt-1">
                  <span v-if="userAnswer(si, q.id) === q.answer" class="text-green-600 dark:text-green-400">✅ 正确！</span>
                  <span v-else class="text-red-600 dark:text-red-400">❌ {{ userAnswer(si, q.id) ? '你的答案：' + userAnswer(si, q.id) + '，' : '未作答，' }}正确：{{ q.answer }}</span>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 翻译 -->
        <template v-if="section.type === 'translation'">
          <h3 class="text-lg font-semibold text-purple-600 dark:text-purple-400 mb-3">🌐 {{ section.title }}</h3>
          <div v-for="q in section.questions" :key="q.id" class="space-y-3">
            <div class="p-4 bg-purple-50 dark:bg-purple-900/20 rounded-lg text-sm text-gray-700 dark:text-gray-300 leading-relaxed">
              <div class="font-medium text-xs text-purple-500 dark:text-purple-400 mb-1">原文</div>{{ q.stem }}
            </div>
            <div v-if="!revealed && !showAnswer[`${si}_${q.id}`]">
              <button @click="showAnswer[`${si}_${q.id}`] = true" class="btn-primary text-sm py-1.5">💡 查看参考译文</button>
            </div>
            <div v-if="revealed || showAnswer[`${si}_${q.id}`]" class="p-4 bg-green-50 dark:bg-green-900/20 rounded-lg text-sm text-gray-700 dark:text-gray-300 leading-relaxed">
              <div class="font-medium text-xs text-green-500 dark:text-green-400 mb-1">参考译文</div>{{ q.answer }}
            </div>
          </div>
        </template>

        <!-- 听力：选择题 (CET-6 ABC / TEM-4,8 Conversations) -->
        <template v-if="section.type === 'listening' && section.subType === 'choice'">
          <div class="flex items-center gap-3 mb-3">
            <h3 class="text-lg font-semibold text-green-600 dark:text-green-400">🎧 {{ section.title }}</h3>
            <button v-if="section.audio_url" @click="setActiveListening(si)"
              class="px-3 py-1 text-xs rounded-lg transition"
              :class="activeListeningSi === si ? 'bg-green-500 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-green-100 dark:hover:bg-green-900/40'">
              {{ activeListeningSi === si && audioPlaying ? '⏸ 暂停' : '▶ 播放音频' }}
            </button>
          </div>
          <!-- 听力原文 -->
          <details v-if="section.transcriptSegments?.length" class="mb-3" open>
            <summary class="cursor-pointer text-xs text-gray-500 dark:text-gray-400 mb-2 select-none">
              📄 听力原文 {{ activeSegment[si] >= 0 ? '— 跟读中...' : '' }}
            </summary>
            <div class="max-h-52 overflow-y-auto p-3 bg-gray-50 dark:bg-gray-800 rounded-lg text-sm leading-relaxed border-l-2 border-green-300 dark:border-green-700">
              <p v-for="(seg, idx) in section.transcriptSegments" :key="idx"
                 class="py-1 px-2 rounded transition-colors duration-200 cursor-pointer flex items-start gap-2"
                 :class="activeSegment[si] === idx
                   ? 'bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-300 font-medium'
                   : checked && getAnsQids(si, idx).length
                     ? 'bg-yellow-100 dark:bg-yellow-900/40 text-yellow-800 dark:text-yellow-300 font-medium'
                     : 'text-gray-600 dark:text-gray-400'"
                 @click="seekToSegment(seg.start)">
                <span class="flex-1">{{ seg.text }}</span>
                <span v-if="checked && getAnsQids(si, idx).length"
                  class="shrink-0 text-xs font-bold text-yellow-600 dark:text-yellow-400 bg-yellow-200 dark:bg-yellow-800/60 px-1.5 py-0.5 rounded">
                  🎯 Q{{ getAnsQids(si, idx).join('/') }}
                </span>
              </p>
            </div>
          </details>
          <div class="space-y-3">
            <div v-for="q in section.questions" :key="q.id" class="p-3 border dark:border-gray-700 rounded-lg" :class="qBoxClass(si, q)">
              <div class="flex items-start gap-3">
                <span class="font-bold text-gray-400 dark:text-gray-500 w-6 shrink-0">{{ q.id }}.</span>
                <div class="flex-1">
                  <div v-if="q.stem" class="text-sm text-gray-700 dark:text-gray-200 mb-2">{{ q.stem }}</div>
                  <div class="flex flex-wrap gap-2">
                    <button v-for="opt in q.options" :key="opt" @click="userAnswers[`${si}_${q.id}`] = opt.charAt(0)"
                      class="px-3 py-1.5 rounded-lg text-xs font-medium transition" :class="getOptClass(si, q, opt)">{{ opt }}</button>
                  </div>
                </div>
              </div>
              <div v-if="checked" class="mt-2 text-xs pl-9">
                <span v-if="userAnswer(si, q.id) === q.answer" class="text-green-600 dark:text-green-400">✅ 正确！</span>
                <span v-else class="text-red-600 dark:text-red-400">❌ {{ userAnswer(si, q.id) ? '你的答案：' + userAnswer(si, q.id) + '，' : '未作答，' }}正确答案：{{ q.options?.find(o => o.startsWith(q.answer)) || q.answer }}</span>
              </div>
            </div>
          </div>
        </template>

        <!-- 听力：填空 (TEM-4 Talk, TEM-8 Mini-lecture) -->
        <template v-if="section.type === 'listening' && section.subType === 'gap-fill'">
          <div class="flex items-center gap-3 mb-3">
            <h3 class="text-lg font-semibold text-indigo-600 dark:text-indigo-400">📝 {{ section.title }}</h3>
            <button v-if="section.audio_url" @click="setActiveListening(si)"
              class="px-3 py-1 text-xs rounded-lg transition"
              :class="activeListeningSi === si ? 'bg-indigo-500 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-indigo-100 dark:hover:bg-indigo-900/40'">
              {{ activeListeningSi === si && audioPlaying ? '⏸ 暂停' : '▶ 播放音频' }}
            </button>
          </div>
          <!-- 听力原文 -->
          <details v-if="section.transcriptSegments?.length" class="mb-3" open>
            <summary class="cursor-pointer text-xs text-gray-500 dark:text-gray-400 mb-2 select-none">
              📄 听力原文 {{ activeSegment[si] >= 0 ? '— 跟读中...' : '' }}
            </summary>
            <div class="max-h-52 overflow-y-auto p-3 bg-gray-50 dark:bg-gray-800 rounded-lg text-sm leading-relaxed border-l-2 border-indigo-300 dark:border-indigo-700">
              <p v-for="(seg, idx) in section.transcriptSegments" :key="idx"
                 class="py-1 px-2 rounded transition-colors duration-200 cursor-pointer flex items-start gap-2"
                 :class="activeSegment[si] === idx
                   ? 'bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-300 font-medium'
                   : checked && getAnsQids(si, idx).length
                     ? 'bg-yellow-100 dark:bg-yellow-900/40 text-yellow-800 dark:text-yellow-300 font-medium'
                     : 'text-gray-600 dark:text-gray-400'"
                 @click="seekToSegment(seg.start)">
                <span class="flex-1">{{ seg.text }}</span>
                <span v-if="checked && getAnsQids(si, idx).length"
                  class="shrink-0 text-xs font-bold text-yellow-600 dark:text-yellow-400 bg-yellow-200 dark:bg-yellow-800/60 px-1.5 py-0.5 rounded">
                  🎯 Q{{ getAnsQids(si, idx).join('/') }}
                </span>
              </p>
            </div>
          </details>
          <div class="space-y-1.5 p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
            <div v-for="q in section.questions" :key="q.id" class="py-1.5 text-sm border-b dark:border-gray-700 last:border-0" :class="qBoxClass(si, q)">
              <div class="flex flex-wrap items-start gap-2">
                <span class="font-bold text-gray-400 dark:text-gray-500 text-xs shrink-0 mt-1">({{ q.id }})</span>
                <div class="flex-1 text-gray-700 dark:text-gray-200 text-xs leading-relaxed">
                  <template v-for="(part, idx) in q.stem.split('______')" :key="idx">
                    <span>{{ part }}</span>
                    <span v-if="idx < q.stem.split('______').length - 1" class="inline-flex items-center mx-1 align-baseline">
                      <input v-model="userAnswers[`${si}_${q.id}`]"
                        class="px-2 py-px rounded border text-xs w-28"
                        :class="getGapFillInputClass(si, q)"
                        :placeholder="'(填空)'" />
                    </span>
                  </template>
                </div>
              </div>
              <div v-if="checked" class="mt-1 ml-6">
                <span v-if="isGapFillCorrect(si, q)" class="text-green-600 dark:text-green-400 text-xs">✅ 正确！</span>
                <span v-else class="text-red-600 dark:text-red-400 text-xs">
                  ❌ {{ userAnswer(si, q.id) ? '你的答案：' + userAnswer(si, q.id) + '，' : '未作答，' }}正确答案：{{ q.answer }}
                </span>
              </div>
            </div>
          </div>
        </template>

        <!-- 听力：听写 (TEM-4 Dictation) -->
        <template v-if="section.type === 'listening' && section.subType === 'dictation'">
          <div class="flex items-center gap-3 mb-3">
            <h3 class="text-lg font-semibold text-pink-600 dark:text-pink-400">✏️ {{ section.title }}</h3>
            <button v-if="section.audio_url" @click="setActiveListening(si)"
              class="px-3 py-1 text-xs rounded-lg transition"
              :class="activeListeningSi === si ? 'bg-pink-500 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-pink-100 dark:hover:bg-pink-900/40'">
              {{ activeListeningSi === si && audioPlaying ? '⏸ 暂停' : '▶ 播放音频' }}
            </button>
          </div>
          <!-- 听力原文 -->
          <details v-if="section.transcriptSegments?.length" class="mb-3" open>
            <summary class="cursor-pointer text-xs text-gray-500 dark:text-gray-400 mb-2 select-none">
              📄 听力原文 {{ activeSegment[si] >= 0 ? '— 跟读中...' : '' }}
            </summary>
            <div class="max-h-52 overflow-y-auto p-3 bg-gray-50 dark:bg-gray-800 rounded-lg text-sm leading-relaxed border-l-2 border-pink-300 dark:border-pink-700">
              <p v-for="(seg, idx) in section.transcriptSegments" :key="idx"
                 class="py-1 px-2 rounded transition-colors duration-200 cursor-pointer flex items-start gap-2"
                 :class="activeSegment[si] === idx
                   ? 'bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-300 font-medium'
                   : checked && getAnsQids(si, idx).length
                     ? 'bg-yellow-100 dark:bg-yellow-900/40 text-yellow-800 dark:text-yellow-300 font-medium'
                     : 'text-gray-600 dark:text-gray-400'"
                 @click="seekToSegment(seg.start)">
                <span class="flex-1">{{ seg.text }}</span>
                <span v-if="checked && getAnsQids(si, idx).length"
                  class="shrink-0 text-xs font-bold text-yellow-600 dark:text-yellow-400 bg-yellow-200 dark:bg-yellow-800/60 px-1.5 py-0.5 rounded">
                  🎯 Q{{ getAnsQids(si, idx).join('/') }}
                </span>
              </p>
            </div>
          </details>
          <div v-for="q in section.questions" :key="q.id" class="space-y-3">
            <p class="text-xs text-gray-500 dark:text-gray-400">{{ q.stem }}</p>
            <textarea v-model="userAnswers[`${si}_${q.id}`]"
              class="w-full h-40 p-4 rounded-lg border dark:border-gray-600 bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 text-sm resize-y"
              placeholder="请在此输入你听到的内容..."></textarea>
            <div v-if="checked" class="p-4 rounded-lg text-sm" :class="getDictationBoxClass(si, q)">
              <div class="font-medium text-xs mb-2" :class="dictationResultColor(si, q)">
                {{ dictationResultLabel(si, q) }} — 相似度：{{ dictationSimilarity(si, q) }}%
              </div>
              <details v-if="dictationSimilarity(si, q) < 90">
                <summary class="cursor-pointer text-xs text-gray-500 dark:text-gray-400">查看原文对比 ▼</summary>
                <div class="mt-2 p-3 bg-white dark:bg-gray-900 rounded text-xs leading-relaxed">
                  <div class="font-medium text-green-600 dark:text-green-400 mb-1">标准原文：</div>
                  <pre class="whitespace-pre-wrap text-gray-700 dark:text-gray-200">{{ q.answer }}</pre>
                  <div class="font-medium text-blue-600 dark:text-blue-400 mt-3 mb-1">你的作答：</div>
                  <pre class="whitespace-pre-wrap text-gray-700 dark:text-gray-200">{{ userAnswer(si, q.id) || '(空)' }}</pre>
                </div>
              </details>
            </div>
          </div>
        </template>
      </div>
    </div>

    <div v-if="!selectedPaper && papers.length > 0" class="card text-center text-gray-400 dark:text-gray-500 py-8">👆 点击上方试卷卡片开始答题</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { papersAPI } from '../api'

const papers = ref([]); const types = ref([]); const filterType = ref('cet6')
const selectedPaper = ref(null); const userAnswers = ref({}); const showAnswer = ref({})
const checked = ref(false); const revealed = ref(false)
const activeCloze = ref(null); const activeClozeSi = ref(0); const activeClozeQ = ref(null)
// 音频播放状态
const audioRef = ref(null); const audioPlaying = ref(false)
const audioCurrentTime = ref(0); const audioDuration = ref(0)
const activeAudioUrl = ref(''); const activeListeningSi = ref(-1)
const activeSegment = ref({})  // { sectionIndex: segmentIndex } 原文跟读高亮

onMounted(() => { loadPapers(); loadStats() })
watch(filterType, () => { selectedPaper.value = null; loadPapers() })

async function loadPapers() { try { const { data } = await papersAPI.list({ type: filterType.value }); papers.value = data.papers } catch { /* */ } }
async function loadStats() { try { const { data } = await papersAPI.stats(); const map = { cet4:'CET-4',cet6:'CET-6',tem4:'专四',tem8:'专八' }; types.value = data.stats.map(s=>({value:s.type,count:s.count,label:map[s.type]||s.type})) } catch { /* */ } }
async function selectPaper(p) { resetState(); try { const { data } = await papersAPI.detail(p.id); selectedPaper.value = data.paper } catch { /* */ } }
function resetState() { userAnswers.value = {}; showAnswer.value = {}; checked.value = false; revealed.value = false; activeCloze.value = null; if (audioRef.value) { audioRef.value.pause(); audioRef.value.currentTime = 0 }; audioPlaying.value = false; audioCurrentTime.value = 0; audioDuration.value = 0; activeAudioUrl.value = ''; activeListeningSi.value = -1; activeSegment.value = {} }
function resetPaper() { if (selectedPaper.value) resetState() }
function userAnswer(si, qid) { return userAnswers.value[`${si}_${qid}`] }
const totalQuestions = computed(() => { if (!selectedPaper.value) return 0; return selectedPaper.value.sections.reduce((s,sc)=>s+(sc.questions?.length||0),0) })
const answeredCount = computed(() => Object.keys(userAnswers.value).length)
const correctCount = computed(() => { if (!selectedPaper.value) return 0; let c=0; for(const[k,v]of Object.entries(userAnswers.value)){const[si,qid]=k.split('_').map(Number);const sec=selectedPaper.value.sections[si];const q=sec?.questions?.find(q=>q.id===qid);if(!q)continue; if(sec?.type==='listening'&&sec?.subType==='dictation')continue; if(sec?.type==='listening'&&sec?.subType==='gap-fill'){ if(isGapFillCorrect(si,q))c++; continue } if(v===q.answer)c++ } return c })
const correctRate = computed(() => { if(answeredCount.value===0) return 0; return Math.round((correctCount.value/answeredCount.value)*100) })
function checkAnswers() { checked.value = true }
function revealAll() { checked.value = true; revealed.value = true }
function hideAnswers() { checked.value = false; revealed.value = false }
function toggleAnswer(si, q) { if(checked.value&&revealed.value) return; if(activeCloze.value===q.id&&activeClozeSi.value===si){activeCloze.value=null;return} activeClozeSi.value=si; activeClozeQ.value=q; activeCloze.value=q.id }

function qBoxClass(si,q){const k=`${si}_${q.id}`;if(!checked.value)return 'border-gray-200 dark:border-gray-700';if(!userAnswers.value[k])return 'border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800/50';if(userAnswers.value[k]===q.answer)return 'border-green-300 dark:border-green-700 bg-green-50 dark:bg-green-900/20';return 'border-red-300 dark:border-red-700 bg-red-50 dark:bg-red-900/20'}
function getOptClass(si,q,opt){const k=`${si}_${q.id}`;const s=userAnswers.value[k];const l=opt.charAt(0);if(!checked.value){if(l===s)return 'bg-blue-500 text-white';return 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-blue-100 dark:hover:bg-blue-900/40'} if(l===q.answer)return 'bg-green-500 text-white';if(l===s&&s!==q.answer)return 'bg-red-400 text-white';return 'bg-gray-100 dark:bg-gray-700 text-gray-400'}
function getClozeBtnClass(si,q){const k=`${si}_${q.id}`;const s=userAnswers.value[k];if(activeCloze.value===q.id)return 'border-blue-500 bg-blue-50 dark:bg-blue-900/40 text-blue-700 dark:text-blue-400';if(!checked.value){if(s)return 'border-blue-400 bg-blue-500 text-white';return 'border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-800 text-gray-500 dark:text-gray-400 hover:border-blue-400'}if(!s)return 'border-gray-300 dark:border-gray-600 bg-gray-100 dark:bg-gray-700 text-gray-400';if(s===q.answer)return 'border-green-400 bg-green-500 text-white';return 'border-red-400 bg-red-500 text-white'}
function getMatchingParaClass(si,p){if(!checked.value||!revealed.value)return '';const sec=selectedPaper.value.sections[si];const has=sec.questions?.some(q=>q.answer===p.charAt(0));return has?'bg-yellow-100 dark:bg-yellow-900/40 font-bold':''}
function typeBadge(t){const m={cet4:'bg-green-100 dark:bg-green-900/40 text-green-700 dark:text-green-400',cet6:'bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-400',tem4:'bg-yellow-100 dark:bg-yellow-900/40 text-yellow-700 dark:text-yellow-400',tem8:'bg-red-100 dark:bg-red-900/40 text-red-700 dark:text-red-400'};return m[t]||'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400'}

// ── 听力辅助函数 ──
const currentListeningUrl = computed(() => {
  if (activeListeningSi.value >= 0 && selectedPaper.value) {
    return selectedPaper.value.sections[activeListeningSi.value]?.audio_url || ''
  }
  const first = selectedPaper.value?.sections.find(s => s.type === 'listening' && s.audio_url)
  return first?.audio_url || ''
})
const currentListeningLabel = computed(() => {
  if (activeListeningSi.value >= 0 && selectedPaper.value) {
    return selectedPaper.value.sections[activeListeningSi.value]?.title || ''
  }
  return ''
})
function setActiveListening(si) {
  activeListeningSi.value = si
  const sec = selectedPaper.value.sections[si]
  if (sec?.audio_url) playAudio(sec.audio_url)
}
function playAudio(url) {
  if (activeAudioUrl.value !== url) {
    activeAudioUrl.value = url
    nextTick(() => { if (audioRef.value) { audioRef.value.play().catch(() => {}); audioPlaying.value = true } })
  } else if (audioRef.value) {
    if (audioRef.value.paused) { audioRef.value.play(); audioPlaying.value = true }
    else { audioRef.value.pause(); audioPlaying.value = false }
  }
}
function onAudioTimeUpdate() {
  if (audioRef.value) audioCurrentTime.value = audioRef.value.currentTime
  // 定位当前播放到的原文段落（跟读高亮）
  if (activeListeningSi.value >= 0 && selectedPaper.value) {
    const sec = selectedPaper.value.sections[activeListeningSi.value]
    const segs = sec?.transcriptSegments
    if (segs?.length) {
      const t = audioRef.value?.currentTime || 0
      const idx = segs.findIndex(s => t >= s.start && t < s.end)
      activeSegment.value = { [activeListeningSi.value]: idx }
    }
  }
}
function onAudioLoadedMetadata() { if (audioRef.value) audioDuration.value = audioRef.value.duration }
function onAudioEnded() { audioPlaying.value = false }
function seekAudio(pct) { if (audioRef.value && audioDuration.value) audioRef.value.currentTime = (pct / 100) * audioDuration.value }
function formatTime(s) { const m = Math.floor(s / 60); const sec = Math.floor(s % 60); return `${m}:${String(sec).padStart(2, '0')}` }
function seekToSegment(seconds) { if (audioRef.value) { audioRef.value.currentTime = seconds; audioCurrentTime.value = seconds } }
// 获取该 section 中哪些题目的得分点落在第 segIdx 段
function getAnsQids(si, segIdx) {
  const sec = selectedPaper.value?.sections[si]
  if (!sec?.questions) return []
  return sec.questions.filter(q => q.transcriptIdx === segIdx).map(q => q.id)
}

// gap-fill 灵活匹配
function isGapFillCorrect(si, q) {
  const user = (userAnswers.value[`${si}_${q.id}`] || '').trim().toLowerCase()
  const ans = (q.answer || '').trim().toLowerCase()
  if (!user || !ans) return false
  if (ans.includes(',')) return ans.split(',').map(a => a.trim()).includes(user)
  const pm = ans.match(/^(.+?)\s*\((.+?)\)$/); if (pm) return user === pm[1].trim() || user === pm[2].trim()
  return user === ans
}
function getGapFillInputClass(si, q) {
  if (!checked.value) return 'border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100'
  return isGapFillCorrect(si, q)
    ? 'border-green-400 bg-green-50 dark:bg-green-900/30 text-green-700 dark:text-green-300'
    : 'border-red-400 bg-red-50 dark:bg-red-900/30 text-red-700 dark:text-red-300'
}

// dictation 相似度
function simpleEditDistance(a, b) {
  const m=a.length,n=b.length; const dp=Array.from({length:m+1},(_,i)=>[i])
  for(let j=0;j<=n;j++)dp[0][j]=j
  for(let i=1;i<=m;i++)for(let j=1;j<=n;j++)dp[i][j]=a[i-1]===b[j-1]?dp[i-1][j-1]:1+Math.min(dp[i-1][j],dp[i][j-1],dp[i-1][j-1])
  return dp[m][n]
}
function dictationSimilarity(si, q) {
  const user = (userAnswers.value[`${si}_${q.id}`] || '').trim().toLowerCase()
  const ans = (q.answer || '').trim().toLowerCase()
  if (!user) return 0; if (user === ans) return 100
  const ut = user.split(/\s+/).filter(Boolean); const at = ans.split(/\s+/).filter(Boolean)
  if (at.length === 0) return 0
  const as = new Set(at); let matches = 0
  for (const t of ut) { if (as.has(t)) matches++; else { for (const atk of at) { if (t.length > 3 && atk.length > 3 && simpleEditDistance(t, atk) <= 2) { matches += 0.5; break } } } }
  return Math.min(100, Math.round((matches / Math.max(ut.length, at.length)) * 100))
}
function dictationResultLabel(si, q) {
  const s = dictationSimilarity(si, q)
  if (s >= 90) return '✅ 优秀'
  if (s >= 70) return '⚠️ 一般'
  return '❌ 需加强'
}
function dictationResultColor(si, q) {
  const s = dictationSimilarity(si, q)
  if (s >= 90) return 'text-green-600 dark:text-green-400'
  if (s >= 70) return 'text-yellow-600 dark:text-yellow-400'
  return 'text-red-600 dark:text-red-400'
}
function getDictationBoxClass(si, q) {
  const s = dictationSimilarity(si, q)
  if (s >= 90) return 'border-green-200 dark:border-green-800 bg-green-50 dark:bg-green-900/20'
  if (s >= 70) return 'border-yellow-200 dark:border-yellow-800 bg-yellow-50 dark:bg-yellow-900/20'
  return 'border-red-200 dark:border-red-800 bg-red-50 dark:bg-red-900/20'
}
</script>
