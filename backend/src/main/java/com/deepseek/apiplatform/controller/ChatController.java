package com.deepseek.apiplatform.controller;

import com.deepseek.apiplatform.dto.*;
import com.deepseek.apiplatform.security.ApiKeyPrincipal;
import com.deepseek.apiplatform.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }
    
    @GetMapping("/models")
    public ResponseEntity<ModelListResponse> listModels() {
        return ResponseEntity.ok(chatService.listModels());
    }
    
    @PostMapping("/chat/completions")
    public ResponseEntity<ChatCompletionResponse> chatCompletion(
            @AuthenticationPrincipal ApiKeyPrincipal principal,
            @RequestBody ChatCompletionRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        
        ChatCompletionResponse response = chatService.chatCompletion(
            principal.getUserId(), 
            request
        );
        
        return ResponseEntity.ok(response);
    }
}
