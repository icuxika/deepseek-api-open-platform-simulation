package com.deepseek.apiplatform.security;

import com.deepseek.apiplatform.entity.ApiKey;
import java.security.Principal;

public class ApiKeyPrincipal implements Principal {
    private final ApiKey apiKey;
    
    public ApiKeyPrincipal(ApiKey apiKey) {
        this.apiKey = apiKey;
    }
    
    @Override
    public String getName() {
        return apiKey.getName();
    }
    
    public Long getUserId() {
        return apiKey.getUserId();
    }
    
    public Long getKeyId() {
        return apiKey.getId();
    }
    
    public String getApiKey() {
        return apiKey.getApiKey();
    }
}
