package com.deepseek.apiplatform.controller;

import com.deepseek.apiplatform.dto.UserResponse;
import com.deepseek.apiplatform.security.UserPrincipal;
import com.deepseek.apiplatform.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }
    
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(authService.getUserById(principal.getId()));
    }
}
