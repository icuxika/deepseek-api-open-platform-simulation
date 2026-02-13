package com.deepseek.apiplatform.service;

import com.deepseek.apiplatform.dto.ChatCompletionRequest;
import com.deepseek.apiplatform.dto.ChatCompletionResponse;
import com.deepseek.apiplatform.dto.ModelListResponse;
import com.deepseek.apiplatform.entity.UsageStats;
import com.deepseek.apiplatform.repository.UsageStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private UsageStatsRepository usageStatsRepository;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(usageStatsRepository);
    }

    @Test
    @DisplayName("获取模型列表")
    void listModels_Success() {
        ModelListResponse response = chatService.listModels();

        assertNotNull(response);
        assertEquals("list", response.getObject());
        assertEquals(3, response.getData().size());
        
        var modelIds = response.getData().stream().map(m -> m.getId()).toList();
        assertTrue(modelIds.contains("deepseek-chat"));
        assertTrue(modelIds.contains("deepseek-coder"));
        assertTrue(modelIds.contains("deepseek-reasoner"));
    }

    @Test
    @DisplayName("Chat Completion 成功")
    void chatCompletion_Success() {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel("deepseek-chat");
        
        ChatCompletionRequest.Message msg = new ChatCompletionRequest.Message();
        msg.setRole("user");
        msg.setContent("Hello");
        request.setMessages(List.of(msg));

        UsageStats stats = new UsageStats();
        stats.setUserId(1L);
        stats.setTotalTokens(0L);
        stats.setPromptTokens(0L);
        stats.setCompletionTokens(0L);
        stats.setRequestCount(0L);

        when(usageStatsRepository.findByUserId(1L)).thenReturn(Optional.of(stats));
        when(usageStatsRepository.save(any(UsageStats.class))).thenReturn(stats);

        ChatCompletionResponse response = chatService.chatCompletion(1L, request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("chat.completion", response.getObject());
        assertEquals("deepseek-chat", response.getModel());
        assertNotNull(response.getChoices());
        assertEquals(1, response.getChoices().size());
        assertNotNull(response.getChoices().get(0).getMessage());
        assertEquals("assistant", response.getChoices().get(0).getMessage().getRole());
        assertNotNull(response.getChoices().get(0).getMessage().getContent());
        assertNotNull(response.getUsage());
        assertTrue(response.getUsage().getTotalTokens() > 0);

        verify(usageStatsRepository).save(any(UsageStats.class));
    }

    @Test
    @DisplayName("Chat Completion - 无用量统计时创建")
    void chatCompletion_CreatesUsageStats_WhenNotExists() {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel("deepseek-chat");
        
        ChatCompletionRequest.Message msg = new ChatCompletionRequest.Message();
        msg.setRole("user");
        msg.setContent("Test");
        request.setMessages(List.of(msg));

        when(usageStatsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(usageStatsRepository.save(any(UsageStats.class))).thenAnswer(invocation -> {
            UsageStats s = invocation.getArgument(0);
            s.setId(1L);
            return s;
        });

        ChatCompletionResponse response = chatService.chatCompletion(1L, request);

        assertNotNull(response);
        verify(usageStatsRepository, times(2)).save(any(UsageStats.class));
    }

    @Test
    @DisplayName("Chat Completion - 默认模型")
    void chatCompletion_DefaultModel() {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel("invalid-model");
        
        ChatCompletionRequest.Message msg = new ChatCompletionRequest.Message();
        msg.setRole("user");
        msg.setContent("Test");
        request.setMessages(List.of(msg));

        UsageStats stats = new UsageStats();
        stats.setUserId(1L);
        stats.setTotalTokens(0L);
        stats.setPromptTokens(0L);
        stats.setCompletionTokens(0L);
        stats.setRequestCount(0L);

        when(usageStatsRepository.findByUserId(1L)).thenReturn(Optional.of(stats));
        when(usageStatsRepository.save(any(UsageStats.class))).thenReturn(stats);

        ChatCompletionResponse response = chatService.chatCompletion(1L, request);

        assertEquals("deepseek-chat", response.getModel());
    }

    @Test
    @DisplayName("Chat Completion - 空消息列表")
    void chatCompletion_EmptyMessages() {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel("deepseek-chat");
        request.setMessages(new ArrayList<>());

        UsageStats stats = new UsageStats();
        stats.setUserId(1L);
        stats.setTotalTokens(0L);
        stats.setPromptTokens(0L);
        stats.setCompletionTokens(0L);
        stats.setRequestCount(0L);

        when(usageStatsRepository.findByUserId(1L)).thenReturn(Optional.of(stats));
        when(usageStatsRepository.save(any(UsageStats.class))).thenReturn(stats);

        ChatCompletionResponse response = chatService.chatCompletion(1L, request);

        assertNotNull(response);
        assertNotNull(response.getChoices().get(0).getMessage().getContent());
    }
}
