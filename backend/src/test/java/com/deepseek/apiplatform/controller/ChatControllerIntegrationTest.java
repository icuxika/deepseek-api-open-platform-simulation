package com.deepseek.apiplatform.controller;

import com.deepseek.apiplatform.dto.ChatCompletionRequest;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String[] registerAndGetTokenAndApiKey() throws Exception {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String json = "{\"email\":\"chat_" + uuid + "@example.com\",\"username\":\"user_" + uuid + "\",\"password\":\"password123\"}";
        
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();

        String keyJson = "{\"name\":\"Chat Key\"}";
        MvcResult keyResult = mockMvc.perform(post("/api/api-keys")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(keyJson))
            .andExpect(status().isOk())
            .andReturn();

        String apiKey = objectMapper.readTree(keyResult.getResponse().getContentAsString()).get("key").asText();
        
        return new String[]{token, apiKey};
    }

    @Test
    @DisplayName("获取模型列表 - 无需认证")
    void listModels_NoAuth() throws Exception {
        mockMvc.perform(get("/v1/models"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.object").value("list"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].id").exists());
    }

    @Test
    @DisplayName("Chat Completion - 使用 API Key 认证")
    void chatCompletion_WithApiKey() throws Exception {
        String[] creds = registerAndGetTokenAndApiKey();
        String apiKey = creds[1];
        
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel("deepseek-chat");
        
        ChatCompletionRequest.Message msg = new ChatCompletionRequest.Message();
        msg.setRole("user");
        msg.setContent("Hello");
        request.setMessages(List.of(msg));

        mockMvc.perform(post("/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.object").value("chat.completion"))
            .andExpect(jsonPath("$.model").value("deepseek-chat"))
            .andExpect(jsonPath("$.choices[0].message.role").value("assistant"))
            .andExpect(jsonPath("$.choices[0].message.content").exists())
            .andExpect(jsonPath("$.usage.totalTokens").exists());
    }

    @Test
    @DisplayName("Chat Completion - 无认证返回 403")
    void chatCompletion_WithoutAuth() throws Exception {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel("deepseek-chat");
        request.setMessages(new ArrayList<>());

        mockMvc.perform(post("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Chat Completion - 无效 API Key 返回 403")
    void chatCompletion_InvalidApiKey() throws Exception {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel("deepseek-chat");
        request.setMessages(new ArrayList<>());

        mockMvc.perform(post("/v1/chat/completions")
                .header("Authorization", "Bearer sk-invalid-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Chat Completion 后检查用量统计")
    void chatCompletion_UpdatesUsageStats() throws Exception {
        String[] creds = registerAndGetTokenAndApiKey();
        String token = creds[0];
        String apiKey = creds[1];
        
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel("deepseek-chat");
        
        ChatCompletionRequest.Message msg = new ChatCompletionRequest.Message();
        msg.setRole("user");
        msg.setContent("Test message");
        request.setMessages(List.of(msg));

        mockMvc.perform(post("/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/billing/usage")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTokens").value(org.hamcrest.Matchers.greaterThan(0)))
            .andExpect(jsonPath("$.requestCount").value(1));
    }

    @Test
    @DisplayName("API Key 无法访问 /api/billing/usage - 返回 403")
    void apiKeyCannotAccessBillingUsage() throws Exception {
        String[] creds = registerAndGetTokenAndApiKey();
        String apiKey = creds[1];
        
        mockMvc.perform(get("/api/billing/usage")
                .header("Authorization", "Bearer " + apiKey))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("API Key 无法访问 /api/billing/records - 返回 403")
    void apiKeyCannotAccessBillingRecords() throws Exception {
        String[] creds = registerAndGetTokenAndApiKey();
        String apiKey = creds[1];
        
        mockMvc.perform(get("/api/billing/records")
                .header("Authorization", "Bearer " + apiKey))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("API Key 无法访问 /api/billing/recharge - 返回 403")
    void apiKeyCannotAccessRecharge() throws Exception {
        String[] creds = registerAndGetTokenAndApiKey();
        String apiKey = creds[1];
        
        mockMvc.perform(post("/api/billing/recharge")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":100,\"paymentMethod\":\"alipay\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("API Key 无法访问 /api/api-keys - 返回 403")
    void apiKeyCannotAccessApiKeysList() throws Exception {
        String[] creds = registerAndGetTokenAndApiKey();
        String apiKey = creds[1];
        
        mockMvc.perform(get("/api/api-keys")
                .header("Authorization", "Bearer " + apiKey))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("API Key 无法访问 /api/auth/me - 返回 4xx")
    void apiKeyCannotAccessUserInfo() throws Exception {
        String[] creds = registerAndGetTokenAndApiKey();
        String apiKey = creds[1];
        
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + apiKey))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("JWT Token 可以访问 /api/billing/usage")
    void jwtTokenCanAccessBillingUsage() throws Exception {
        String[] creds = registerAndGetTokenAndApiKey();
        String token = creds[0];
        
        mockMvc.perform(get("/api/billing/usage")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("JWT Token 无法访问 /v1/chat/completions (非 sk- 开头) - 返回 4xx")
    void jwtTokenCannotAccessChatCompletion() throws Exception {
        String[] creds = registerAndGetTokenAndApiKey();
        String token = creds[0];
        
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel("deepseek-chat");
        request.setMessages(new ArrayList<>());

        mockMvc.perform(post("/v1/chat/completions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is4xxClientError());
    }
}
