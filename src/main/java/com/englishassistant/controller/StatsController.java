package com.englishassistant.controller;

import com.englishassistant.entity.User;
import com.englishassistant.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "学习统计", description = "学习概览、每日统计、连续打卡天数、各等级进度")
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @Operation(summary = "学习概览", description = "返回总单词数、已掌握数、作文总数、平均分")
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> overview(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(statsService.getOverview(user.getId()));
    }

    @Operation(summary = "每日统计", description = "最近30天的每日学习数据（热力图用）")
    @GetMapping("/daily")
    public ResponseEntity<Object> daily(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(statsService.getDailyStats(user.getId()));
    }

    @Operation(summary = "连续打卡", description = "计算当前用户的连续学习天数")
    @GetMapping("/streak")
    public ResponseEntity<Map<String, Object>> streak(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(statsService.getStreak(user.getId()));
    }

    @Operation(summary = "各等级学习进度", description = "CET-4/6/专四/专八/TOEFL/GRE各等级的学习进度与词库总量对比")
    @GetMapping("/by-level")
    public ResponseEntity<Map<String, Object>> byLevel(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(statsService.getStatsByLevel(user.getId()));
    }
}
