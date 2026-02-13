package com.deepseek.apiplatform.service;

import com.deepseek.apiplatform.dto.ApiKeyResponse;
import com.deepseek.apiplatform.dto.CreateApiKeyRequest;
import com.deepseek.apiplatform.entity.ApiKey;
import com.deepseek.apiplatform.repository.ApiKeyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }
    
    public List<ApiKeyResponse> getUserApiKeys(Long userId) {
        return apiKeyRepository.findByUserId(userId).stream()
                .map(this::toApiKeyResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ApiKeyResponse createApiKey(Long userId, CreateApiKeyRequest request) {
        ApiKey apiKey = new ApiKey();
        apiKey.setUserId(userId);
        apiKey.setName(request.getName());
        apiKey.setApiKey(generateApiKey());
        apiKey.setStatus(ApiKey.KeyStatus.ACTIVE);
        
        apiKey = apiKeyRepository.save(apiKey);
        return toApiKeyResponse(apiKey);
    }
    
    @Transactional
    public void deleteApiKey(Long userId, Long keyId) {
        apiKeyRepository.deleteByIdAndUserId(keyId, userId);
    }
    
    private String generateApiKey() {
        StringBuilder sb = new StringBuilder("sk-");
        for (int i = 0; i < 32; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
    
    private ApiKeyResponse toApiKeyResponse(ApiKey apiKey) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return new ApiKeyResponse(
            apiKey.getId(),
            apiKey.getName(),
            apiKey.getApiKey(),
            apiKey.getStatus().name().toLowerCase(),
            apiKey.getCreatedAt() != null ? apiKey.getCreatedAt().format(formatter) : null,
            apiKey.getLastUsedAt() != null ? apiKey.getLastUsedAt().format(formatter) : null
        );
    }
}
