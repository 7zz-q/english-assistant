package com.englishassistant.controller;

import com.englishassistant.dto.request.TranslateRequest;
import com.englishassistant.service.TranslateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "AI翻译", description = "中英双向翻译，自动检测语言方向")
@RestController
@RequestMapping("/api/translate")
public class TranslateController {

    private final TranslateService translateService;

    public TranslateController(TranslateService translateService) {
        this.translateService = translateService;
    }

    @Operation(summary = "AI翻译", description = "自动检测语言方向进行翻译（英→中 / 中→英），返回译文、翻译要点、难词解释")
    @PostMapping
    public ResponseEntity<Map<String, Object>> translate(@Valid @RequestBody TranslateRequest req) {
        return ResponseEntity.ok(translateService.translate(req));
    }
}
