package com.englishassistant.service.impl;

import com.englishassistant.dto.request.TranslateRequest;
import com.englishassistant.service.AiService;
import com.englishassistant.service.TranslateService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TranslateServiceImpl implements TranslateService {

    private final AiService aiService;

    public TranslateServiceImpl(AiService aiService) {
        this.aiService = aiService;
    }

    @Override
    public Map<String, Object> translate(TranslateRequest req) {
        return aiService.translateText(req.getText(), req.getDirection());
    }
}
