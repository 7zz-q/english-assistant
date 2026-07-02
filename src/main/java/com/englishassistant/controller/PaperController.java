package com.englishassistant.controller;

import com.englishassistant.service.PaperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "历年真题", description = "CET-4/6/专四/专八历年真题试卷浏览")
@RestController
@RequestMapping("/api/papers")
public class PaperController {

    private final PaperService paperService;

    public PaperController(PaperService paperService) {
        this.paperService = paperService;
    }

    @Operation(summary = "试卷列表", description = "分页浏览试卷，可按类型筛选，按年份倒序")
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @Parameter(description = "试卷类型筛选") @RequestParam(required = false) String type,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(paperService.listPapers(type, page, limit));
    }

    @Operation(summary = "试卷类型统计", description = "各类型试卷的数量统计")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(paperService.getStats());
    }

    @Operation(summary = "试卷详情", description = "查看某套试卷的完整内容，包含所有题型和题目")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(
            @Parameter(description = "试卷ID") @PathVariable Long id) {
        return ResponseEntity.ok(paperService.getDetail(id));
    }
}
