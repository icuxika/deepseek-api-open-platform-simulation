package com.deepseek.apiplatform.dto;

import java.util.List;

public class ChatCompletionRequest {
    private String model;
    private List<Message> messages;
    private Double temperature;
    private Integer maxTokens;
    private Boolean stream;
    
    public static class Message {
        private String role;
        private String content;
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
    
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    
    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
    
    public Boolean getStream() { return stream; }
    public void setStream(Boolean stream) { this.stream = stream; }
}
