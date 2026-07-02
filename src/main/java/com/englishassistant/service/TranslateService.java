package com.englishassistant.service;

import com.englishassistant.dto.request.TranslateRequest;
import java.util.Map;

public interface TranslateService {
    Map<String, Object> translate(TranslateRequest req);
}
