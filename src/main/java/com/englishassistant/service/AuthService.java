package com.englishassistant.service;

import com.englishassistant.dto.request.LoginRequest;
import com.englishassistant.dto.request.RegisterRequest;
import java.util.Map;

public interface AuthService {
    Map<String, Object> register(RegisterRequest req);
    Map<String, Object> login(LoginRequest req);
    Map<String, Object> getCurrentUser(Long userId);
}
