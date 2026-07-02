package com.englishassistant.service;

import com.englishassistant.dto.request.LoginRequest;
import com.englishassistant.dto.request.RegisterRequest;
import com.englishassistant.entity.User;
import com.englishassistant.mapper.UserMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceTest {

    @Autowired private AuthService authService;
    @Autowired private UserMapper userMapper;

    private static final String TEST_USER = "test_auth_user";
    private static final String TEST_EMAIL = "test_auth@test.com";
    private static final String TEST_PASS = "123456";

    @AfterAll
    static void cleanup(@Autowired UserMapper userMapper) {
        userMapper.delete(null); // clean test data
    }

    @Test @Order(1)
    void testRegister() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(TEST_USER);
        req.setEmail(TEST_EMAIL);
        req.setPassword(TEST_PASS);

        Map<String, Object> result = authService.register(req);

        assertNotNull(result.get("token"));
        assertNotNull(result.get("user"));
        Map<String, Object> u = (Map<String, Object>) result.get("user");
        assertEquals(TEST_USER, u.get("username"));
    }

    @Test @Order(2)
    void testRegisterDuplicate() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(TEST_USER);
        req.setEmail(TEST_EMAIL);
        req.setPassword(TEST_PASS);

        try {
            authService.register(req);
            fail("Should throw for duplicate user");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("已存在"));
        }
    }

    @Test @Order(3)
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest();
        req.setUsername(TEST_USER);
        req.setPassword(TEST_PASS);

        Map<String, Object> result = authService.login(req);

        assertNotNull(result.get("token"));
        assertTrue(((String) result.get("token")).length() > 50);
    }

    @Test @Order(4)
    void testLoginWrongPassword() {
        LoginRequest req = new LoginRequest();
        req.setUsername(TEST_USER);
        req.setPassword("wrongpass");

        try {
            authService.login(req);
            fail("Should throw for wrong password");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("用户名或密码错误"));
        }
    }
}
