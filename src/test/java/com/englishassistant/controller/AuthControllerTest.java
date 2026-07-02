package com.englishassistant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static String token;

    @Test @Order(1)
    void testRegister() throws Exception {
        String body = objectMapper.writeValueAsString(
            Map.of("username", "mvctest", "email", "mvc@test.com", "password", "123456"));

        var result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.user.username").value("mvctest"))
            .andReturn();

        token = objectMapper.readTree(result.getResponse().getContentAsString())
            .get("token").asText();
    }

    @Test @Order(2)
    void testRegisterDuplicateReturns409() throws Exception {
        String body = objectMapper.writeValueAsString(
            Map.of("username", "mvctest", "email", "mvc2@test.com", "password", "123456"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().is(409))
            .andExpect(jsonPath("$.error").value(containsString("已存在")));
    }

    @Test @Order(3)
    void testRegisterValidationFails() throws Exception {
        String body = objectMapper.writeValueAsString(
            Map.of("username", "", "email", "bad", "password", "12"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.error").exists());
    }

    @Test @Order(4)
    void testLogin() throws Exception {
        String body = objectMapper.writeValueAsString(
            Map.of("username", "mvctest", "password", "123456"));

        var result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.user.username").value("mvctest"))
            .andReturn();

        token = objectMapper.readTree(result.getResponse().getContentAsString())
            .get("token").asText();
    }

    @Test @Order(5)
    void testLoginWrongPassword() throws Exception {
        String body = objectMapper.writeValueAsString(
            Map.of("username", "mvctest", "password", "wrongpass"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().is(401))
            .andExpect(jsonPath("$.error").value("用户名或密码错误"));
    }

    @Test @Order(6)
    void testMeWithToken() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.username").value("mvctest"))
            .andExpect(jsonPath("$.user.email").value("mvc@test.com"));
    }

    @Test @Order(7)
    void testMeWithoutTokenReturns403() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().is(403));
    }

    @Test @Order(8)
    void testHealthPublic() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"))
            .andExpect(jsonPath("$.time").exists());
    }
}
