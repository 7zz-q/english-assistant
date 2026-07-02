package com.englishassistant.service;

import java.util.Map;

public interface PaperService {
    Map<String, Object> listPapers(String type, int page, int limit);
    Map<String, Object> getStats();
    Map<String, Object> getDetail(Long id);
}
