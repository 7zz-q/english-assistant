package com.englishassistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.englishassistant.common.BusinessException;
import com.englishassistant.dto.request.AddWordRequest;
import com.englishassistant.entity.Word;
import com.englishassistant.entity.WordAnalysis;
import com.englishassistant.mapper.WordAnalysisMapper;
import com.englishassistant.mapper.WordMapper;
import com.englishassistant.service.AiService;
import com.englishassistant.service.DailyStatService;
import com.englishassistant.service.WordService;
import com.englishassistant.util.EbbinghausUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class WordServiceImpl implements WordService {

    private static final Logger log = LoggerFactory.getLogger(WordServiceImpl.class);

    private final WordMapper wordMapper;
    private final WordAnalysisMapper wordAnalysisMapper;
    private final AiService aiService;
    private final DailyStatService dailyStatService;
    private final ObjectMapper objectMapper;

    public WordServiceImpl(WordMapper wordMapper,
                          WordAnalysisMapper wordAnalysisMapper,
                          AiService aiService,
                          DailyStatService dailyStatService,
                          ObjectMapper objectMapper) {
        this.wordMapper = wordMapper;
        this.wordAnalysisMapper = wordAnalysisMapper;
        this.aiService = aiService;
        this.dailyStatService = dailyStatService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> listWords(Long userId, Integer status, String level, int page, int limit) {
        LambdaQueryWrapper<Word> wrapper = new LambdaQueryWrapper<Word>()
            .eq(Word::getUserId, userId);
        if (status != null) wrapper.eq(Word::getStatus, status);
        if (level != null && !level.isEmpty()) wrapper.eq(Word::getLevel, level);
        wrapper.orderByAsc(Word::getNextReview);

        Page<Word> result = wordMapper.selectPage(new Page<>(page, limit), wrapper);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("words", result.getRecords());
        resp.put("total", result.getTotal());
        resp.put("page", page);
        return resp;
    }

    @Override
    public Map<String, Object> getReviewWords(Long userId) {
        List<Word> words = wordMapper.selectReviewWords(userId);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("words", words);
        resp.put("count", words.size());
        return resp;
    }

    @Override
    @Transactional
    public Map<String, Object> addWord(Long userId, AddWordRequest req) {
        String word = req.getWord().trim();
        Long exists = wordMapper.selectCount(new LambdaQueryWrapper<Word>()
            .eq(Word::getUserId, userId).eq(Word::getWord, word));
        if (exists > 0) throw new BusinessException(409, "单词已存在于词库中");

        String phonetic = "";
        String meaning = req.getMeaning();
        String example = "";
        String level = req.getLevel() != null ? req.getLevel() : "cet6";

        try {
            Map<String, Object> aiResult = aiService.generateExample(word);
            if (meaning == null || meaning.isEmpty()) meaning = (String) aiResult.getOrDefault("meaning", "");
            phonetic = (String) aiResult.getOrDefault("phonetic", "");
            example = (String) aiResult.getOrDefault("example", "");
        } catch (Exception e) {
            log.warn("AI auto-complete failed for '{}': {}", word, e.getMessage());
        }

        Word entity = new Word();
        entity.setUserId(userId);
        entity.setWord(word);
        entity.setPhonetic(phonetic);
        entity.setMeaning(meaning);
        entity.setExample(example);
        entity.setLevel(level);
        entity.setStatus(0);
        entity.setReviewCount(0);
        entity.setEaseFactor(2.5);
        entity.setInterval(0);
        entity.setNextReview(LocalDate.now());
        wordMapper.insert(entity);
        dailyStatService.incrementReview(userId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id", entity.getId());
        resp.put("word", entity.getWord());
        resp.put("phonetic", entity.getPhonetic());
        resp.put("meaning", entity.getMeaning());
        resp.put("example", entity.getExample());
        resp.put("level", entity.getLevel());
        return resp;
    }

    @Override
    @Transactional
    public Map<String, Object> reviewWord(Long userId, Long wordId, int quality) {
        Word word = wordMapper.selectOne(new LambdaQueryWrapper<Word>()
            .eq(Word::getId, wordId).eq(Word::getUserId, userId));
        if (word == null) throw new BusinessException(404, "单词不存在");

        EbbinghausUtil.recalculate(word, quality);
        wordMapper.updateById(word);
        dailyStatService.incrementReview(userId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("interval", word.getInterval());
        resp.put("ease_factor", word.getEaseFactor());
        resp.put("next_review", word.getNextReview() != null ? word.getNextReview().toString() : null);
        resp.put("status", word.getStatus());
        return resp;
    }

    @Override
    public void deleteWord(Long userId, Long wordId) {
        int deleted = wordMapper.delete(new LambdaQueryWrapper<Word>()
            .eq(Word::getId, wordId).eq(Word::getUserId, userId));
        if (deleted == 0) throw new BusinessException(404, "单词不存在");
    }

    /**
     * AI word analysis with Spring Cache (Redis-backed).
     * @Cacheable handles Redis get/set automatically — 7 day TTL.
     * MySQL word_analysis table is the cold-start fallback.
     */
    @Override
    @Cacheable(value = "wordAnalysis", key = "#word", unless = "#result == null")
    public Map<String, Object> analyzeWord(String word) {
        // 1. Try MySQL fallback (cold start after Redis flush)
        WordAnalysis dbCache = wordAnalysisMapper.selectById(word);
        if (dbCache != null) {
            try {
                return objectMapper.readValue(dbCache.getData(), Map.class);
            } catch (Exception e) {
                log.warn("Failed to parse cached analysis for '{}'", word);
            }
        }

        // 2. Call AI
        Map<String, Object> aiResult = aiService.analyzeWord(word);

        // 3. Persist to MySQL for cold-start resilience
        try {
            WordAnalysis entity = new WordAnalysis();
            entity.setWord(word);
            entity.setData(objectMapper.writeValueAsString(aiResult));
            wordAnalysisMapper.insert(entity);
        } catch (Exception e) {
            if (e.getMessage() == null || !e.getMessage().contains("Duplicate")) {
                log.warn("Failed to persist word analysis for '{}': {}", word, e.getMessage());
            }
        }

        return aiResult;
    }

    @Override
    public Map<String, Object> lookupWord(String word) {
        return aiService.generateExample(word);
    }
}
