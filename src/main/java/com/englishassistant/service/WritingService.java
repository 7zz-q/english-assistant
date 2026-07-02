package com.englishassistant.service;

import com.englishassistant.dto.request.CorrectEssayRequest;
import com.englishassistant.dto.request.GenerateEssayRequest;
import com.englishassistant.dto.request.GenerateTopicRequest;
import java.util.Map;

public interface WritingService {
    Map<String, Object> correctEssay(Long userId, CorrectEssayRequest req);
    Map<String, Object> getHistory(Long userId, int page, int limit);
    Map<String, Object> getDetail(Long userId, Long essayId);
    Map<String, Object> generateTopic(GenerateTopicRequest req);
    Map<String, Object> generateEssay(GenerateEssayRequest req);
}
