package com.englishassistant.service;

import java.util.Map;

public interface AiService {
    Map<String, Object> correctEssay(String content, String title);
    Map<String, Object> generateExample(String word);
    Map<String, Object> analyzeWord(String word);
    Map<String, Object> generateTopic(String level);
    Map<String, Object> generateEssay(String topic, String level);
    Map<String, Object> translateText(String text, String direction);
}
