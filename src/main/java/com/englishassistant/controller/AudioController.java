package com.englishassistant.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@RestController
public class AudioController {

    private static final Logger log = LoggerFactory.getLogger(AudioController.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/api/audio/proxy")
    public ResponseEntity<?> proxy(@RequestParam String url, HttpServletResponse response) {
        if (url == null || url.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "缺少 url 参数"));
        }

        try {
            ResponseEntity<byte[]> audioResp = restTemplate.getForEntity(url, byte[].class);

            response.setContentType("audio/mpeg");
            response.setHeader("Cache-Control", "public, max-age=86400");

            byte[] body = audioResp.getBody();
            if (body != null) {
                try (OutputStream os = response.getOutputStream()) {
                    os.write(body);
                    os.flush();
                }
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Audio proxy failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "音频获取失败"));
        }
    }
}
