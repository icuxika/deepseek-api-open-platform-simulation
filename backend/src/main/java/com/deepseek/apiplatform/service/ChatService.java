package com.deepseek.apiplatform.service;

import com.deepseek.apiplatform.dto.*;
import com.deepseek.apiplatform.entity.UsageStats;
import com.deepseek.apiplatform.repository.UsageStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class ChatService {
    private final UsageStatsRepository usageStatsRepository;
    
    private static final List<String> AVAILABLE_MODELS = Arrays.asList(
        "deepseek-chat",
        "deepseek-coder",
        "deepseek-reasoner"
    );
    
    private static final Map<String, Double> MODEL_PRICING = Map.of(
        "deepseek-chat", 0.001,
        "deepseek-coder", 0.002,
        "deepseek-reasoner", 0.003
    );

    public ChatService(UsageStatsRepository usageStatsRepository) {
        this.usageStatsRepository = usageStatsRepository;
    }
    
    public ModelListResponse listModels() {
        ModelListResponse response = new ModelListResponse();
        List<ModelResponse> models = new ArrayList<>();
        
        long created = System.currentTimeMillis() / 1000;
        for (String modelId : AVAILABLE_MODELS) {
            ModelResponse model = new ModelResponse();
            model.setId(modelId);
            model.setCreated(created);
            model.setOwnedBy("deepseek");
            models.add(model);
        }
        
        response.setData(models);
        return response;
    }
    
    @Transactional
    public ChatCompletionResponse chatCompletion(Long userId, ChatCompletionRequest request) {
        String model = request.getModel();
        if (model == null || !AVAILABLE_MODELS.contains(model)) {
            model = "deepseek-chat";
        }
        
        int promptTokens = estimateTokens(request.getMessages());
        int completionTokens = generateRandomCompletionTokens();
        int totalTokens = promptTokens + completionTokens;
        
        updateUsageStats(userId, promptTokens, completionTokens);
        
        ChatCompletionResponse response = new ChatCompletionResponse();
        response.setId("chatcmpl-" + UUID.randomUUID().toString().substring(0, 24));
        response.setCreated(System.currentTimeMillis() / 1000);
        response.setModel(model);
        
        ChatCompletionResponse.Choice choice = new ChatCompletionResponse.Choice();
        choice.setIndex(0);
        choice.setFinishReason("stop");
        
        ChatCompletionResponse.Choice.Message message = new ChatCompletionResponse.Choice.Message();
        message.setRole("assistant");
        message.setContent(generateMockResponse(request));
        choice.setMessage(message);
        
        response.setChoices(List.of(choice));
        
        ChatCompletionResponse.Usage usage = new ChatCompletionResponse.Usage();
        usage.setPromptTokens(promptTokens);
        usage.setCompletionTokens(completionTokens);
        usage.setTotalTokens(totalTokens);
        response.setUsage(usage);
        
        return response;
    }
    
    private int estimateTokens(List<ChatCompletionRequest.Message> messages) {
        if (messages == null) return 0;
        int total = 0;
        for (ChatCompletionRequest.Message msg : messages) {
            if (msg.getContent() != null) {
                total += msg.getContent().length() / 4 + 10;
            }
        }
        return Math.max(total, 10);
    }
    
    private int generateRandomCompletionTokens() {
        Random random = new Random();
        return 50 + random.nextInt(200);
    }
    
    private String generateMockResponse(ChatCompletionRequest request) {
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            return "Hello! How can I help you today?";
        }
        
        String lastMessage = "";
        for (ChatCompletionRequest.Message msg : request.getMessages()) {
            if ("user".equals(msg.getRole())) {
                lastMessage = msg.getContent();
            }
        }
        
        if (lastMessage.toLowerCase().contains("hello") || lastMessage.toLowerCase().contains("hi")) {
            return "Hello! I'm DeepSeek AI assistant. How can I help you today?";
        } else if (lastMessage.toLowerCase().contains("code") || lastMessage.toLowerCase().contains("programming")) {
            return "I'd be happy to help with coding! Here's a sample response for your programming question. In a real implementation, this would connect to the actual DeepSeek API.";
        } else {
            return "Thank you for your message! This is a simulated response from the DeepSeek API platform. In production, this would be connected to the actual DeepSeek AI models. Your question was: \"" + lastMessage + "\"";
        }
    }
    
    private void updateUsageStats(Long userId, int promptTokens, int completionTokens) {
        UsageStats stats = usageStatsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultUsageStats(userId));
        
        stats.setTotalTokens(stats.getTotalTokens() + promptTokens + completionTokens);
        stats.setPromptTokens(stats.getPromptTokens() + promptTokens);
        stats.setCompletionTokens(stats.getCompletionTokens() + completionTokens);
        stats.setRequestCount(stats.getRequestCount() + 1);
        
        usageStatsRepository.save(stats);
    }
    
    private UsageStats createDefaultUsageStats(Long userId) {
        UsageStats stats = new UsageStats();
        stats.setUserId(userId);
        stats.setTotalTokens(0L);
        stats.setPromptTokens(0L);
        stats.setCompletionTokens(0L);
        stats.setRequestCount(0L);
        return usageStatsRepository.save(stats);
    }
}
