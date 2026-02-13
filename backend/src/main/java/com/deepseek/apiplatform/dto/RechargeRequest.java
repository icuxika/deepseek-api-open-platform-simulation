package com.deepseek.apiplatform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RechargeRequest {
    @NotNull(message = "充值金额不能为空")
    @Min(value = 1, message = "充值金额必须大于0")
    private Double amount;
    
    private String paymentMethod = "alipay";

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
