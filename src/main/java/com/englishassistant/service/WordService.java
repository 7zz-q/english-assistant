package com.englishassistant.service;

import com.englishassistant.dto.request.AddWordRequest;
import com.englishassistant.entity.Word;
import java.util.List;
import java.util.Map;

public interface WordService {
    Map<String, Object> listWords(Long userId, Integer status, String level, int page, int limit);
    Map<String, Object> getReviewWords(Long userId);
    Map<String, Object> addWord(Long userId, AddWordRequest req);
    Map<String, Object> reviewWord(Long userId, Long wordId, int quality);
    void deleteWord(Long userId, Long wordId);
    Map<String, Object> analyzeWord(String word);
    Map<String, Object> lookupWord(String word);
}
