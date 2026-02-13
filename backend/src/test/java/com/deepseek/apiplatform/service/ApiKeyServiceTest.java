package com.deepseek.apiplatform.service;

import com.deepseek.apiplatform.dto.ApiKeyResponse;
import com.deepseek.apiplatform.dto.CreateApiKeyRequest;
import com.deepseek.apiplatform.entity.ApiKey;
import com.deepseek.apiplatform.repository.ApiKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    private ApiKeyService apiKeyService;

    @BeforeEach
    void setUp() {
        apiKeyService = new ApiKeyService(apiKeyRepository);
    }

    @Test
    @DisplayName("获取用户 API Keys 列表")
    void getUserApiKeys_Success() {
        ApiKey key1 = new ApiKey();
        key1.setId(1L);
        key1.setUserId(1L);
        key1.setName("Key 1");
        key1.setApiKey("sk-key1");
        key1.setStatus(ApiKey.KeyStatus.ACTIVE);

        ApiKey key2 = new ApiKey();
        key2.setId(2L);
        key2.setUserId(1L);
        key2.setName("Key 2");
        key2.setApiKey("sk-key2");
        key2.setStatus(ApiKey.KeyStatus.ACTIVE);

        when(apiKeyRepository.findByUserId(1L)).thenReturn(Arrays.asList(key1, key2));

        List<ApiKeyResponse> responses = apiKeyService.getUserApiKeys(1L);

        assertEquals(2, responses.size());
        assertEquals("Key 1", responses.get(0).getName());
        assertEquals("Key 2", responses.get(1).getName());
    }

    @Test
    @DisplayName("创建 API Key 成功")
    void createApiKey_Success() {
        CreateApiKeyRequest request = new CreateApiKeyRequest();
        request.setName("Test Key");

        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(invocation -> {
            ApiKey key = invocation.getArgument(0);
            key.setId(1L);
            return key;
        });

        ApiKeyResponse response = apiKeyService.createApiKey(1L, request);

        assertNotNull(response);
        assertEquals("Test Key", response.getName());
        assertTrue(response.getKey().startsWith("sk-"));
        assertEquals("active", response.getStatus());

        verify(apiKeyRepository).save(any(ApiKey.class));
    }

    @Test
    @DisplayName("删除 API Key 成功")
    void deleteApiKey_Success() {
        doNothing().when(apiKeyRepository).deleteByIdAndUserId(1L, 1L);

        apiKeyService.deleteApiKey(1L, 1L);

        verify(apiKeyRepository).deleteByIdAndUserId(1L, 1L);
    }

    @Test
    @DisplayName("API Key 格式正确")
    void createApiKey_FormatCorrect() {
        CreateApiKeyRequest request = new CreateApiKeyRequest();
        request.setName("Test");

        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(invocation -> {
            ApiKey key = invocation.getArgument(0);
            key.setId(1L);
            return key;
        });

        ApiKeyResponse response = apiKeyService.createApiKey(1L, request);

        assertTrue(response.getKey().startsWith("sk-"));
        assertEquals(35, response.getKey().length());
    }
}
