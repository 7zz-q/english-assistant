package com.englishassistant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> resp = new LinkedHashMap<>();
        resp.put("status", "ok");
        resp.put("time", LocalDateTime.now().toString());
        return ResponseEntity.ok(resp);
    }
}
