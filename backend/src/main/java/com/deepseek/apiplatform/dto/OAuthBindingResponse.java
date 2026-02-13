package com.deepseek.apiplatform.dto;

import com.deepseek.apiplatform.entity.enums.OAuthProvider;

public class OAuthBindingResponse {
    private OAuthProvider provider;
    private String providerUsername;
    private String avatarUrl;
    private String createdAt;

    public OAuthProvider getProvider() { return provider; }
    public void setProvider(OAuthProvider provider) { this.provider = provider; }

    public String getProviderUsername() { return providerUsername; }
    public void setProviderUsername(String providerUsername) { this.providerUsername = providerUsername; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
