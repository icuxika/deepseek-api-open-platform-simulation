package com.deepseek.apiplatform.dto;

import java.math.BigDecimal;

public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private BigDecimal balance;
    private String avatarUrl;
    private String createdAt;

    public UserResponse() {}

    public UserResponse(Long id, String email, String username, BigDecimal balance, String avatarUrl, String createdAt) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.balance = balance;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
