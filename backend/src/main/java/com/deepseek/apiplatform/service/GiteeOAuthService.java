package com.deepseek.apiplatform.service;

import com.deepseek.apiplatform.config.OAuthConfig;
import com.deepseek.apiplatform.dto.AuthResponse;
import com.deepseek.apiplatform.dto.GiteeTokenResponse;
import com.deepseek.apiplatform.dto.GiteeUserInfo;
import com.deepseek.apiplatform.dto.UserResponse;
import com.deepseek.apiplatform.entity.User;
import com.deepseek.apiplatform.entity.UserOAuth;
import com.deepseek.apiplatform.entity.enums.OAuthProvider;
import com.deepseek.apiplatform.repository.UserOAuthRepository;
import com.deepseek.apiplatform.repository.UserRepository;
import com.deepseek.apiplatform.security.JwtUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class GiteeOAuthService {
    private static final String GITEE_AUTHORIZE_URL = "https://gitee.com/oauth/authorize";
    private static final String GITEE_TOKEN_URL = "https://gitee.com/oauth/token";
    private static final String GITEE_USER_URL = "https://gitee.com/api/v5/user";

    private final OAuthConfig oAuthConfig;
    private final UserOAuthRepository userOAuthRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final RestTemplate restTemplate;

    public GiteeOAuthService(OAuthConfig oAuthConfig, 
                            UserOAuthRepository userOAuthRepository,
                            UserRepository userRepository,
                            JwtUtils jwtUtils) {
        this.oAuthConfig = oAuthConfig;
        this.userOAuthRepository = userOAuthRepository;
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.restTemplate = new RestTemplate();
    }

    public String getAuthorizationUrl() {
        OAuthConfig.GiteeConfig gitee = oAuthConfig.getGitee();
        return UriComponentsBuilder.fromHttpUrl(GITEE_AUTHORIZE_URL)
                .queryParam("client_id", gitee.getClientId())
                .queryParam("redirect_uri", gitee.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", "user_info")
                .build()
                .toUriString();
    }

    public GiteeTokenResponse getAccessToken(String code) {
        OAuthConfig.GiteeConfig gitee = oAuthConfig.getGitee();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("client_id", gitee.getClientId());
        params.add("redirect_uri", gitee.getRedirectUri());
        params.add("client_secret", gitee.getClientSecret());
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        ResponseEntity<GiteeTokenResponse> response = restTemplate.postForEntity(
                GITEE_TOKEN_URL, request, GiteeTokenResponse.class);
        
        return response.getBody();
    }

    public GiteeUserInfo getUserInfo(String accessToken) {
        String url = UriComponentsBuilder.fromHttpUrl(GITEE_USER_URL)
                .queryParam("access_token", accessToken)
                .build()
                .toUriString();
        
        ResponseEntity<GiteeUserInfo> response = restTemplate.getForEntity(url, GiteeUserInfo.class);
        return response.getBody();
    }

    @Transactional
    public AuthResponse loginOrRegister(GiteeUserInfo giteeUser, String accessToken) {
        String providerUserId = String.valueOf(giteeUser.getId());
        
        Optional<UserOAuth> existingOAuth = userOAuthRepository
                .findByProviderAndProviderUserId(OAuthProvider.GITEE, providerUserId);
        
        User user;
        if (existingOAuth.isPresent()) {
            user = userRepository.findById(existingOAuth.get().getUserId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            updateOAuthToken(existingOAuth.get(), accessToken, giteeUser);
        } else {
            user = createNewUser(giteeUser);
            createOAuthBinding(user.getId(), providerUserId, accessToken);
        }
        
        return buildAuthResponse(user);
    }

    @Transactional
    public void bindAccount(Long userId, GiteeUserInfo giteeUser, String accessToken) {
        String providerUserId = String.valueOf(giteeUser.getId());
        
        Optional<UserOAuth> existingOAuth = userOAuthRepository
                .findByProviderAndProviderUserId(OAuthProvider.GITEE, providerUserId);
        
        if (existingOAuth.isPresent()) {
            throw new RuntimeException("该码云账号已绑定其他用户");
        }
        
        Optional<UserOAuth> userExistingBinding = userOAuthRepository
                .findByUserIdAndProvider(userId, OAuthProvider.GITEE);
        
        if (userExistingBinding.isPresent()) {
            throw new RuntimeException("您已绑定码云账号");
        }
        
        createOAuthBinding(userId, providerUserId, accessToken);
        
        userRepository.findById(userId).ifPresent(user -> {
            if (giteeUser.getAvatarUrl() != null && user.getAvatarUrl() == null) {
                user.setAvatarUrl(giteeUser.getAvatarUrl());
                userRepository.save(user);
            }
        });
    }

    private User createNewUser(GiteeUserInfo giteeUser) {
        User user = new User();
        
        String email = giteeUser.getEmail();
        if (email == null || email.isEmpty()) {
            email = "gitee_" + giteeUser.getId() + "@placeholder.local";
        }
        
        String username = giteeUser.getLogin();
        if (userRepository.existsByUsername(username)) {
            username = username + "_" + UUID.randomUUID().toString().substring(0, 6);
        }
        
        String finalEmail = email;
        if (userRepository.existsByEmail(finalEmail)) {
            email = "gitee_" + giteeUser.getId() + "_" + System.currentTimeMillis() + "@placeholder.local";
        }
        
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(UUID.randomUUID().toString());
        user.setBalance(BigDecimal.ZERO);
        user.setAvatarUrl(giteeUser.getAvatarUrl());
        user.setTokenVersion(0);
        
        return userRepository.save(user);
    }

    private UserOAuth createOAuthBinding(Long userId, String providerUserId, String accessToken) {
        UserOAuth userOAuth = new UserOAuth();
        userOAuth.setUserId(userId);
        userOAuth.setProvider(OAuthProvider.GITEE);
        userOAuth.setProviderUserId(providerUserId);
        userOAuth.setAccessToken(accessToken);
        return userOAuthRepository.save(userOAuth);
    }

    private void updateOAuthToken(UserOAuth userOAuth, String accessToken, GiteeUserInfo giteeUser) {
        userOAuth.setAccessToken(accessToken);
        userOAuthRepository.save(userOAuth);
        
        userRepository.findById(userOAuth.getUserId()).ifPresent(user -> {
            if (giteeUser.getAvatarUrl() != null) {
                user.setAvatarUrl(giteeUser.getAvatarUrl());
                userRepository.save(user);
            }
        });
    }

    public AuthResponse buildAuthResponse(User user) {
        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getTokenVersion());
        
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setType("Bearer");
        
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setUsername(user.getUsername());
        userResponse.setBalance(user.getBalance());
        userResponse.setAvatarUrl(user.getAvatarUrl());
        response.setUser(userResponse);
        
        return response;
    }
}
