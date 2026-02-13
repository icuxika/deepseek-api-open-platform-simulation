package com.deepseek.apiplatform.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateApiKeyRequest {
    @NotBlank(message = "Key名称不能为空")
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
