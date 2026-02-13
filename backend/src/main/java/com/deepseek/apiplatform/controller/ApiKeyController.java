package com.deepseek.apiplatform.controller;

import com.deepseek.apiplatform.dto.ApiKeyResponse;
import com.deepseek.apiplatform.dto.CreateApiKeyRequest;
import com.deepseek.apiplatform.security.UserPrincipal;
import com.deepseek.apiplatform.service.ApiKeyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/api-keys")
public class ApiKeyController {
    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }
    
    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> getApiKeys(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(apiKeyService.getUserApiKeys(principal.getId()));
    }
    
    @PostMapping
    public ResponseEntity<ApiKeyResponse> createApiKey(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateApiKeyRequest request) {
        return ResponseEntity.ok(apiKeyService.createApiKey(principal.getId(), request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApiKey(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        apiKeyService.deleteApiKey(principal.getId(), id);
        return ResponseEntity.ok().build();
    }
}
