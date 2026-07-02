package com.englishassistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.englishassistant.common.BusinessException;
import com.englishassistant.dto.request.LoginRequest;
import com.englishassistant.dto.request.RegisterRequest;
import com.englishassistant.entity.User;
import com.englishassistant.mapper.UserMapper;
import com.englishassistant.service.AuthService;
import com.englishassistant.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public Map<String, Object> register(RegisterRequest req) {
        // Check for duplicate username or email
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
            .eq(User::getUsername, req.getUsername())
            .or()
            .eq(User::getEmail, req.getEmail()));
        if (count > 0) {
            throw new BusinessException(409, "用户名或邮箱已存在");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        userMapper.insert(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("email", user.getEmail());
        result.put("user", userMap);
        return result;
    }

    @Override
    public Map<String, Object> login(LoginRequest req) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
            .eq(User::getUsername, req.getUsername()));

        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("email", user.getEmail());
        result.put("user", userMap);
        return result;
    }

    @Override
    public Map<String, Object> getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("email", user.getEmail());
        userMap.put("created_at", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user", userMap);
        return result;
    }
}
