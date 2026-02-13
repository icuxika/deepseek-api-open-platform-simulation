package com.deepseek.apiplatform.controller;

import com.deepseek.apiplatform.dto.CreateApiKeyRequest;
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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiKeyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String registerAndGetToken() throws Exception {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String json = "{\"email\":\"apikey_" + uuid + "@example.com\",\"username\":\"user_" + uuid + "\",\"password\":\"password123\"}";
        
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    @DisplayName("获取 API Keys 列表 - 空列表")
    void getApiKeys_EmptyList() throws Exception {
        String token = registerAndGetToken();
        
        mockMvc.perform(get("/api/api-keys")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("创建 API Key 成功")
    void createApiKey_Success() throws Exception {
        String token = registerAndGetToken();
        
        CreateApiKeyRequest request = new CreateApiKeyRequest();
        request.setName("Test Key");

        mockMvc.perform(post("/api/api-keys")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Test Key"))
            .andExpect(jsonPath("$.key").exists())
            .andExpect(jsonPath("$.key").value(org.hamcrest.Matchers.startsWith("sk-")))
            .andExpect(jsonPath("$.status").value("active"));
    }

    @Test
    @DisplayName("创建后获取 API Keys 列表")
    void getApiKeys_AfterCreate() throws Exception {
        String token = registerAndGetToken();
        
        CreateApiKeyRequest request = new CreateApiKeyRequest();
        request.setName("My Key");

        mockMvc.perform(post("/api/api-keys")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/api-keys")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].name").value("My Key"));
    }

    @Test
    @DisplayName("删除 API Key 成功")
    void deleteApiKey_Success() throws Exception {
        String token = registerAndGetToken();
        
        CreateApiKeyRequest request = new CreateApiKeyRequest();
        request.setName("Key to Delete");

        MvcResult result = mockMvc.perform(post("/api/api-keys")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        Long keyId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/api-keys/" + keyId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/api-keys")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("未认证访问 API Keys - 返回 403")
    void accessApiKeys_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/api-keys"))
            .andExpect(status().isForbidden());
    }
}
