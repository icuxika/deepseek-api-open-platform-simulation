package com.deepseek.apiplatform.controller;

import com.deepseek.apiplatform.dto.*;
import com.deepseek.apiplatform.entity.UserOAuth;
import com.deepseek.apiplatform.entity.enums.OAuthProvider;
import com.deepseek.apiplatform.repository.UserOAuthRepository;
import com.deepseek.apiplatform.security.UserPrincipal;
import com.deepseek.apiplatform.service.GiteeOAuthService;
import com.deepseek.apiplatform.service.GitHubOAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/auth/oauth")
public class OAuthController {
    private final GiteeOAuthService giteeOAuthService;
    private final GitHubOAuthService gitHubOAuthService;
    private final UserOAuthRepository userOAuthRepository;

    public OAuthController(GiteeOAuthService giteeOAuthService, 
                          GitHubOAuthService gitHubOAuthService,
                          UserOAuthRepository userOAuthRepository) {
        this.giteeOAuthService = giteeOAuthService;
        this.gitHubOAuthService = gitHubOAuthService;
        this.userOAuthRepository = userOAuthRepository;
    }

    @GetMapping("/gitee")
    public ResponseEntity<OAuthUrlResponse> getGiteeAuthUrl() {
        OAuthUrlResponse response = new OAuthUrlResponse();
        response.setUrl(giteeOAuthService.getAuthorizationUrl());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/gitee/callback")
    public ResponseEntity<AuthResponse> giteeCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        GiteeTokenResponse tokenResponse = giteeOAuthService.getAccessToken(code);
        
        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            throw new RuntimeException("获取访问令牌失败");
        }
        
        GiteeUserInfo userInfo = giteeOAuthService.getUserInfo(tokenResponse.getAccessToken());
        
        if (userInfo == null || userInfo.getId() == null) {
            throw new RuntimeException("获取用户信息失败");
        }
        
        if ("bind".equals(state) && principal != null) {
            giteeOAuthService.bindAccount(principal.getId(), userInfo, tokenResponse.getAccessToken());
            return ResponseEntity.ok(giteeOAuthService.loginOrRegister(userInfo, tokenResponse.getAccessToken()));
        } else {
            return ResponseEntity.ok(giteeOAuthService.loginOrRegister(userInfo, tokenResponse.getAccessToken()));
        }
    }

    @GetMapping("/github")
    public ResponseEntity<OAuthUrlResponse> getGithubAuthUrl() {
        OAuthUrlResponse response = new OAuthUrlResponse();
        response.setUrl(gitHubOAuthService.getAuthorizationUrl());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/github/callback")
    public ResponseEntity<AuthResponse> githubCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        GitHubTokenResponse tokenResponse = gitHubOAuthService.getAccessToken(code);
        
        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            throw new RuntimeException("获取访问令牌失败");
        }
        
        GitHubUserInfo userInfo = gitHubOAuthService.getUserInfo(tokenResponse.getAccessToken());
        
        if (userInfo == null || userInfo.getId() == null) {
            throw new RuntimeException("获取用户信息失败");
        }
        
        if ("bind".equals(state) && principal != null) {
            gitHubOAuthService.bindAccount(principal.getId(), userInfo, tokenResponse.getAccessToken());
            return ResponseEntity.ok(gitHubOAuthService.loginOrRegister(userInfo, tokenResponse.getAccessToken()));
        } else {
            return ResponseEntity.ok(gitHubOAuthService.loginOrRegister(userInfo, tokenResponse.getAccessToken()));
        }
    }

    @GetMapping("/bindings")
    public ResponseEntity<List<OAuthBindingResponse>> getBindings(@AuthenticationPrincipal UserPrincipal principal) {
        List<UserOAuth> bindings = userOAuthRepository.findByUserId(principal.getId());
        List<OAuthBindingResponse> response = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (UserOAuth binding : bindings) {
            OAuthBindingResponse item = new OAuthBindingResponse();
            item.setProvider(binding.getProvider());
            item.setCreatedAt(binding.getCreatedAt() != null ? binding.getCreatedAt().format(formatter) : null);
            response.add(item);
        }
        
        return ResponseEntity.ok(response);
    }

    @Transactional
    @DeleteMapping("/bindings/{provider}")
    public ResponseEntity<Void> unbind(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable String provider) {
        OAuthProvider providerEnum;
        try {
            providerEnum = OAuthProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("不支持的登录方式: " + provider);
        }
        
        userOAuthRepository.deleteByUserIdAndProvider(principal.getId(), providerEnum);
        return ResponseEntity.ok().build();
    }
}
