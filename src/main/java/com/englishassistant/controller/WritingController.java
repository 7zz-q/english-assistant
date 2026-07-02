package com.englishassistant.controller;

import com.englishassistant.dto.request.CorrectEssayRequest;
import com.englishassistant.dto.request.GenerateEssayRequest;
import com.englishassistant.dto.request.GenerateTopicRequest;
import com.englishassistant.entity.User;
import com.englishassistant.service.WritingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "AI写作", description = "AI作文批改、历史记录、AI出题、AI范文生成")
@RestController
@RequestMapping("/api/writing")
public class WritingController {

    private final WritingService writingService;

    public WritingController(WritingService writingService) {
        this.writingService = writingService;
    }

    @Operation(summary = "AI批改作文", description = "提交英文作文，AI返回评分、语法纠错、词汇升级、结构建议和润色版本")
    @PostMapping("/correct")
    public ResponseEntity<Map<String, Object>> correct(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Valid @RequestBody CorrectEssayRequest req) {
        return ResponseEntity.ok(writingService.correctEssay(user.getId(), req));
    }

    @Operation(summary = "批改历史", description = "查看当前用户的作文批改历史，按时间倒序")
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> history(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(writingService.getHistory(user.getId(), page, limit));
    }

    @Operation(summary = "作文详情", description = "查看某篇批改的完整详情，包含语法/词汇/结构建议数组")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "作文ID") @PathVariable Long id) {
        return ResponseEntity.ok(writingService.getDetail(user.getId(), id));
    }

    @Operation(summary = "AI生成作文题目", description = "根据考试等级随机生成一个真题风格的作文题目")
    @PostMapping("/topic")
    public ResponseEntity<Map<String, Object>> generateTopic(@RequestBody GenerateTopicRequest req) {
        return ResponseEntity.ok(writingService.generateTopic(req));
    }

    @Operation(summary = "AI生成范文", description = "根据指定题目生成一篇范文（150-200词），含中文翻译和高亮词组")
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateEssay(@Valid @RequestBody GenerateEssayRequest req) {
        return ResponseEntity.ok(writingService.generateEssay(req));
    }
}
