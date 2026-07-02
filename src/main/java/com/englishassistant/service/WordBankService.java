package com.englishassistant.service;

import com.englishassistant.dto.request.BankImportRequest;
import java.util.List;
import java.util.Map;

public interface WordBankService {
    Map<String, Object> browseBank(Long userId, String level, String search, int page, int limit);
    Map<String, Object> getBankWords(String level, int limit);
    List<Map<String, Object>> getBankLevels();
    Map<String, Object> importToPersonal(Long userId, BankImportRequest req);
}
