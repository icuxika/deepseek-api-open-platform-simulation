package com.deepseek.apiplatform.dto;

public class BillingRecordResponse {
    private Long id;
    private String type;
    private Double amount;
    private Double balance;
    private String description;
    private String createdAt;

    public BillingRecordResponse() {}

    public BillingRecordResponse(Long id, String type, Double amount, Double balance, String description, String createdAt) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.balance = balance;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
