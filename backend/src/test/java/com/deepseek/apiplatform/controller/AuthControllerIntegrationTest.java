package com.deepseek.apiplatform.controller;

import com.deepseek.apiplatform.dto.LoginRequest;
import com.deepseek.apiplatform.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("用户注册成功")
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setUsername("newuser");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.type").value("Bearer"))
            .andExpect(jsonPath("$.user.email").value("newuser@example.com"))
            .andExpect(jsonPath("$.user.username").value("newuser"));
    }

    @Test
    @DisplayName("注册失败 - 邮箱已存在")
    void register_EmailExists() throws Exception {
        RegisterRequest request1 = new RegisterRequest();
        request1.setEmail("duplicate@example.com");
        request1.setUsername("user1");
        request1.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isOk());

        RegisterRequest request2 = new RegisterRequest();
        request2.setEmail("duplicate@example.com");
        request2.setUsername("user2");
        request2.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("用户登录成功")
    void login_Success() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("login@example.com");
        registerRequest.setUsername("loginuser");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@example.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.user.email").value("login@example.com"));
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void login_UserNotFound() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void login_WrongPassword() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("wrongpass@example.com");
        registerRequest.setUsername("wrongpassuser");
        registerRequest.setPassword("correctpassword");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("wrongpass@example.com");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("获取当前用户信息 - 需要认证")
    void getCurrentUser_RequiresAuth() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("获取当前用户信息 - 认证成功")
    void getCurrentUser_WithAuth() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("authuser@example.com");
        registerRequest.setUsername("authuser");
        registerRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("token").asText();

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("authuser@example.com"))
            .andExpect(jsonPath("$.username").value("authuser"));
    }
}
