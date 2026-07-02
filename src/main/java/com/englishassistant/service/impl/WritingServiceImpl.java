package com.englishassistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.englishassistant.common.BusinessException;
import com.englishassistant.dto.request.CorrectEssayRequest;
import com.englishassistant.dto.request.GenerateEssayRequest;
import com.englishassistant.dto.request.GenerateTopicRequest;
import com.englishassistant.entity.Essay;
import com.englishassistant.mapper.EssayMapper;
import com.englishassistant.service.AiService;
import com.englishassistant.service.DailyStatService;
import com.englishassistant.service.WritingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class WritingServiceImpl implements WritingService {

    private static final Logger log = LoggerFactory.getLogger(WritingServiceImpl.class);

    private final EssayMapper essayMapper;
    private final AiService aiService;
    private final DailyStatService dailyStatService;
    private final ObjectMapper objectMapper;

    public WritingServiceImpl(EssayMapper essayMapper, AiService aiService,
                             DailyStatService dailyStatService, ObjectMapper objectMapper) {
        this.essayMapper = essayMapper;
        this.aiService = aiService;
        this.dailyStatService = dailyStatService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Map<String, Object> correctEssay(Long userId, CorrectEssayRequest req) {
        Map<String, Object> aiResult = aiService.correctEssay(req.getContent(), req.getTitle());

        // Persist to DB
        Essay essay = new Essay();
        essay.setUserId(userId);
        essay.setTitle(req.getTitle());
        essay.setContent(req.getContent());
        essay.setScore(toDouble(aiResult.get("score")));
        essay.setCorrected((String) aiResult.get("corrected"));
        try {
            essay.setGrammar(objectMapper.writeValueAsString(aiResult.get("grammar")));
            essay.setVocabulary(objectMapper.writeValueAsString(aiResult.get("vocabulary")));
            essay.setStructure(objectMapper.writeValueAsString(aiResult.get("structure")));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize AI result JSON: {}", e.getMessage());
        }

        essayMapper.insert(essay);
        dailyStatService.incrementEssay(userId);

        return aiResult;
    }

    @Override
    public Map<String, Object> getHistory(Long userId, int page, int limit) {
        Page<Essay> result = essayMapper.selectPage(new Page<>(page, limit),
            new LambdaQueryWrapper<Essay>()
                .eq(Essay::getUserId, userId)
                .orderByDesc(Essay::getCreatedAt)
                .select(Essay::getId, Essay::getTitle, Essay::getScore, Essay::getCreatedAt));

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("essays", result.getRecords());
        resp.put("total", result.getTotal());
        resp.put("page", page);
        return resp;
    }

    @Override
    public Map<String, Object> getDetail(Long userId, Long essayId) {
        Essay essay = essayMapper.selectOne(new LambdaQueryWrapper<Essay>()
            .eq(Essay::getId, essayId)
            .eq(Essay::getUserId, userId));
        if (essay == null) {
            throw new BusinessException(404, "作文不存在");
        }

        // Parse JSON string fields back to objects
        Map<String, Object> essayMap = new LinkedHashMap<>();
        essayMap.put("id", essay.getId());
        essayMap.put("user_id", essay.getUserId());
        essayMap.put("title", essay.getTitle());
        essayMap.put("content", essay.getContent());
        essayMap.put("corrected", essay.getCorrected());
        essayMap.put("score", essay.getScore());
        essayMap.put("created_at", essay.getCreatedAt() != null ? essay.getCreatedAt().toString() : null);

        try {
            essayMap.put("grammar", essay.getGrammar() != null ?
                objectMapper.readValue(essay.getGrammar(), List.class) : Collections.emptyList());
            essayMap.put("vocabulary", essay.getVocabulary() != null ?
                objectMapper.readValue(essay.getVocabulary(), List.class) : Collections.emptyList());
            essayMap.put("structure", essay.getStructure() != null ?
                objectMapper.readValue(essay.getStructure(), List.class) : Collections.emptyList());
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse essay JSON fields: {}", e.getMessage());
            essayMap.put("grammar", Collections.emptyList());
            essayMap.put("vocabulary", Collections.emptyList());
            essayMap.put("structure", Collections.emptyList());
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("essay", essayMap);
        return resp;
    }

    @Override
    public Map<String, Object> generateTopic(GenerateTopicRequest req) {
        return aiService.generateTopic(req.getLevel());
    }

    @Override
    public Map<String, Object> generateEssay(GenerateEssayRequest req) {
        return aiService.generateEssay(req.getTopic(), req.getLevel());
    }

    private Double toDouble(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
