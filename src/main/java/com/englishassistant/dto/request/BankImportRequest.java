package com.englishassistant.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class BankImportRequest {
    private List<Long> wordIds;
    private List<String> wordList;
}
