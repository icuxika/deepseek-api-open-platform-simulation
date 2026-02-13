package com.deepseek.apiplatform.dto;

public class ApiKeyResponse {
    private Long id;
    private String name;
    private String key;
    private String status;
    private String createdAt;
    private String lastUsedAt;

    public ApiKeyResponse() {}

    public ApiKeyResponse(Long id, String name, String key, String status, String createdAt, String lastUsedAt) {
        this.id = id;
        this.name = name;
        this.key = key;
        this.status = status;
        this.createdAt = createdAt;
        this.lastUsedAt = lastUsedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(String lastUsedAt) { this.lastUsedAt = lastUsedAt; }
}
