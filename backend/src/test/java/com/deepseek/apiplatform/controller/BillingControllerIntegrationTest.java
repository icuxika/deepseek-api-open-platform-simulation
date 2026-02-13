package com.deepseek.apiplatform.controller;

import com.deepseek.apiplatform.dto.RechargeRequest;
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
class BillingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String registerAndGetToken() throws Exception {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String json = "{\"email\":\"billing_" + uuid + "@example.com\",\"username\":\"user_" + uuid + "\",\"password\":\"password123\"}";
        
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    @DisplayName("获取用量统计 - 新用户")
    void getUsageStats_NewUser() throws Exception {
        String token = registerAndGetToken();
        
        mockMvc.perform(get("/api/billing/usage")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTokens").value(0))
            .andExpect(jsonPath("$.promptTokens").value(0))
            .andExpect(jsonPath("$.completionTokens").value(0))
            .andExpect(jsonPath("$.requestCount").value(0));
    }

    @Test
    @DisplayName("充值成功")
    void recharge_Success() throws Exception {
        String token = registerAndGetToken();
        
        RechargeRequest request = new RechargeRequest();
        request.setAmount(100.0);
        request.setPaymentMethod("alipay");

        mockMvc.perform(post("/api/billing/recharge")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("recharge"))
            .andExpect(jsonPath("$.amount").value(100.0))
            .andExpect(jsonPath("$.balance").value(100.0));
    }

    @Test
    @DisplayName("多次充值累加余额")
    void recharge_Multiple() throws Exception {
        String token = registerAndGetToken();
        
        RechargeRequest request1 = new RechargeRequest();
        request1.setAmount(50.0);
        request1.setPaymentMethod("alipay");

        mockMvc.perform(post("/api/billing/recharge")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(50.0));

        RechargeRequest request2 = new RechargeRequest();
        request2.setAmount(30.0);
        request2.setPaymentMethod("wechat");

        mockMvc.perform(post("/api/billing/recharge")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(80.0));
    }

    @Test
    @DisplayName("获取账单记录")
    void getBillingRecords() throws Exception {
        String token = registerAndGetToken();
        
        RechargeRequest request = new RechargeRequest();
        request.setAmount(100.0);
        request.setPaymentMethod("alipay");

        mockMvc.perform(post("/api/billing/recharge")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/billing/records")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].type").value("recharge"))
            .andExpect(jsonPath("$[0].amount").value(100.0));
    }

    @Test
    @DisplayName("充值后用户余额更新")
    void recharge_UpdatesUserBalance() throws Exception {
        String token = registerAndGetToken();
        
        RechargeRequest request = new RechargeRequest();
        request.setAmount(200.0);
        request.setPaymentMethod("alipay");

        mockMvc.perform(post("/api/billing/recharge")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(200));
    }

    @Test
    @DisplayName("未认证访问 - 返回 403")
    void accessWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/billing/usage"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/billing/records"))
            .andExpect(status().isForbidden());
    }
}
