package com.deepseek.apiplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @Email(message = "请输入有效的邮箱地址")
    @NotBlank(message = "邮箱不能为空")
    private String email;
    
    @NotBlank(message = "密码不能为空")
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
