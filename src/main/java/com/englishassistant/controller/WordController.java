package com.englishassistant.controller;

import com.englishassistant.dto.request.AddWordRequest;
import com.englishassistant.dto.request.ReviewRequest;
import com.englishassistant.entity.User;
import com.englishassistant.service.WordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "词汇管理", description = "个人单词本CRUD、艾宾浩斯复习、AI词汇分析")
@RestController
@RequestMapping("/api/words")
public class WordController {

    private final WordService wordService;

    public WordController(WordService wordService) {
        this.wordService = wordService;
    }

    @Operation(summary = "获取个人单词列表", description = "支持按状态和等级筛选，分页返回")
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "单词状态: 0=新学 1=学习中 2=已掌握") @RequestParam(required = false) Integer status,
            @Parameter(description = "单词等级: cet4/cet6/toefl/gre等") @RequestParam(required = false) String level,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(wordService.listWords(user.getId(), status, level, page, limit));
    }

    @Operation(summary = "获取待复习单词", description = "返回当前到期需要复习的单词（最多30个），按复习时间升序")
    @GetMapping("/review")
    public ResponseEntity<Map<String, Object>> reviewList(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(wordService.getReviewWords(user.getId()));
    }

    @Operation(summary = "AI单词分析", description = "AI全面分析单词（音标/释义/形近词/派生词/选择题选项），走Redis缓存7天")
    @GetMapping("/analyze/{word}")
    public ResponseEntity<Map<String, Object>> analyze(
            @Parameter(description = "英语单词") @PathVariable String word) {
        return ResponseEntity.ok(wordService.analyzeWord(word));
    }

    @Operation(summary = "AI单词查词", description = "AI查词返回音标/释义/例句/常用搭配，不走缓存")
    @GetMapping("/lookup/{word}")
    public ResponseEntity<Map<String, Object>> lookup(
            @Parameter(description = "英语单词") @PathVariable String word) {
        return ResponseEntity.ok(wordService.lookupWord(word));
    }

    @Operation(summary = "添加单词到个人词本", description = "添加新单词，AI自动补全音标/释义/例句")
    @PostMapping
    public ResponseEntity<Map<String, Object>> add(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Valid @RequestBody AddWordRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wordService.addWord(user.getId(), req));
    }

    @Operation(summary = "记录复习结果", description = "艾宾浩斯SM-2算法更新间隔: 0=忘了 1=模糊 2=记得 3=很熟 4=已学会")
    @PatchMapping("/{id}/review")
    public ResponseEntity<Map<String, Object>> review(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "单词ID") @PathVariable Long id,
            @Valid @RequestBody ReviewRequest req) {
        return ResponseEntity.ok(wordService.reviewWord(user.getId(), id, req.getQuality()));
    }

    @Operation(summary = "删除单词", description = "从个人词本中移除单词")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "单词ID") @PathVariable Long id) {
        wordService.deleteWord(user.getId(), id);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
