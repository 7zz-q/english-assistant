package com.englishassistant.controller;

import com.englishassistant.dto.request.LoginRequest;
import com.englishassistant.dto.request.RegisterRequest;
import com.englishassistant.entity.User;
import com.englishassistant.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "认证管理", description = "用户注册、登录、个人信息接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "用户注册", description = "创建新账号，返回JWT令牌")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @Operation(summary = "用户登录", description = "验证账号密码，返回JWT令牌（7天有效）")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @Operation(summary = "获取当前用户信息", description = "通过JWT令牌获取登录用户的基本信息")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authService.getCurrentUser(user.getId()));
    }
}
