package com.deepseek.apiplatform.dto;

public class UsageStatsResponse {
    private Long totalTokens;
    private Long promptTokens;
    private Long completionTokens;
    private Long requestCount;

    public UsageStatsResponse() {}

    public UsageStatsResponse(Long totalTokens, Long promptTokens, Long completionTokens, Long requestCount) {
        this.totalTokens = totalTokens;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.requestCount = requestCount;
    }

    public Long getTotalTokens() { return totalTokens; }
    public void setTotalTokens(Long totalTokens) { this.totalTokens = totalTokens; }
    
    public Long getPromptTokens() { return promptTokens; }
    public void setPromptTokens(Long promptTokens) { this.promptTokens = promptTokens; }
    
    public Long getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(Long completionTokens) { this.completionTokens = completionTokens; }
    
    public Long getRequestCount() { return requestCount; }
    public void setRequestCount(Long requestCount) { this.requestCount = requestCount; }
}
