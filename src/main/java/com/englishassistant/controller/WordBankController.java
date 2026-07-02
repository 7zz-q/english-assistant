package com.englishassistant.controller;

import com.englishassistant.dto.request.BankImportRequest;
import com.englishassistant.entity.User;
import com.englishassistant.service.WordBankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "公共词库", description = "浏览CET-4/6/专四/专八/TOEFL/GRE等公共词库，批量导入个人词本")
@RestController
@RequestMapping("/api/words/bank")
public class WordBankController {

    private final WordBankService wordBankService;

    public WordBankController(WordBankService wordBankService) {
        this.wordBankService = wordBankService;
    }

    @Operation(summary = "浏览公共词库", description = "分页浏览，支持按等级筛选和搜索，返回每个词是否已导入")
    @GetMapping
    public ResponseEntity<Map<String, Object>> browse(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "等级筛选") @RequestParam(required = false) String level,
            @Parameter(description = "搜索关键词（匹配单词和释义）") @RequestParam(required = false) String search,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(wordBankService.browseBank(user.getId(), level, search, page, limit));
    }

    @Operation(summary = "获取词库单词列表", description = "返回指定等级的原始单词列表，用于背单词时选择词源")
    @GetMapping("/words")
    public ResponseEntity<Map<String, Object>> bankWords(
            @Parameter(description = "词库等级（必填）") @RequestParam String level,
            @Parameter(description = "返回条数上限") @RequestParam(defaultValue = "500") int limit) {
        return ResponseEntity.ok(wordBankService.getBankWords(level, limit));
    }

    @Operation(summary = "词库等级统计", description = "返回各等级词库的单词数量统计")
    @GetMapping("/levels")
    public ResponseEntity<Map<String, Object>> bankLevels() {
        List<Map<String, Object>> stats = wordBankService.getBankLevels();
        return ResponseEntity.ok(Map.of("stats", stats));
    }

    @Operation(summary = "批量导入个人词本", description = "从公共词库批量导入单词到个人词本，自动跳过已存在的单词")
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> bankImport(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestBody BankImportRequest req) {
        return ResponseEntity.ok(wordBankService.importToPersonal(user.getId(), req));
    }
}
