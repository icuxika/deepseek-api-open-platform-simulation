package com.deepseek.apiplatform.service;

import com.deepseek.apiplatform.config.OAuthConfig;
import com.deepseek.apiplatform.dto.AuthResponse;
import com.deepseek.apiplatform.dto.GitHubTokenResponse;
import com.deepseek.apiplatform.dto.GitHubUserInfo;
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
import java.util.Optional;
import java.util.UUID;

@Service
public class GitHubOAuthService {
    private static final String GITHUB_AUTHORIZE_URL = "https://github.com/login/oauth/authorize";
    private static final String GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_USER_URL = "https://api.github.com/user";

    private final OAuthConfig oAuthConfig;
    private final UserOAuthRepository userOAuthRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final RestTemplate restTemplate;

    public GitHubOAuthService(OAuthConfig oAuthConfig, 
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
        OAuthConfig.GithubConfig github = oAuthConfig.getGithub();
        return UriComponentsBuilder.fromHttpUrl(GITHUB_AUTHORIZE_URL)
                .queryParam("client_id", github.getClientId())
                .queryParam("redirect_uri", github.getRedirectUri())
                .queryParam("scope", "user:email")
                .build()
                .toUriString();
    }

    public GitHubTokenResponse getAccessToken(String code) {
        OAuthConfig.GithubConfig github = oAuthConfig.getGithub();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", "application/json");
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", github.getClientId());
        params.add("client_secret", github.getClientSecret());
        params.add("code", code);
        params.add("redirect_uri", github.getRedirectUri());
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        ResponseEntity<GitHubTokenResponse> response = restTemplate.postForEntity(
                GITHUB_TOKEN_URL, request, GitHubTokenResponse.class);
        
        return response.getBody();
    }

    public GitHubUserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/json");
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        ResponseEntity<GitHubUserInfo> response = restTemplate.exchange(
                GITHUB_USER_URL, HttpMethod.GET, request, GitHubUserInfo.class);
        
        return response.getBody();
    }

    @Transactional
    public AuthResponse loginOrRegister(GitHubUserInfo githubUser, String accessToken) {
        String providerUserId = String.valueOf(githubUser.getId());
        
        Optional<UserOAuth> existingOAuth = userOAuthRepository
                .findByProviderAndProviderUserId(OAuthProvider.GITHUB, providerUserId);
        
        User user;
        if (existingOAuth.isPresent()) {
            user = userRepository.findById(existingOAuth.get().getUserId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            updateOAuthToken(existingOAuth.get(), accessToken, githubUser);
        } else {
            user = createNewUser(githubUser);
            createOAuthBinding(user.getId(), providerUserId, accessToken);
        }
        
        return buildAuthResponse(user);
    }

    @Transactional
    public void bindAccount(Long userId, GitHubUserInfo githubUser, String accessToken) {
        String providerUserId = String.valueOf(githubUser.getId());
        
        Optional<UserOAuth> existingOAuth = userOAuthRepository
                .findByProviderAndProviderUserId(OAuthProvider.GITHUB, providerUserId);
        
        if (existingOAuth.isPresent()) {
            throw new RuntimeException("该 GitHub 账号已绑定其他用户");
        }
        
        Optional<UserOAuth> userExistingBinding = userOAuthRepository
                .findByUserIdAndProvider(userId, OAuthProvider.GITHUB);
        
        if (userExistingBinding.isPresent()) {
            throw new RuntimeException("您已绑定 GitHub 账号");
        }
        
        createOAuthBinding(userId, providerUserId, accessToken);
        
        userRepository.findById(userId).ifPresent(user -> {
            if (githubUser.getAvatarUrl() != null && user.getAvatarUrl() == null) {
                user.setAvatarUrl(githubUser.getAvatarUrl());
                userRepository.save(user);
            }
        });
    }

    private User createNewUser(GitHubUserInfo githubUser) {
        User user = new User();
        
        String email = githubUser.getEmail();
        if (email == null || email.isEmpty()) {
            email = "github_" + githubUser.getId() + "@placeholder.local";
        }
        
        String username = githubUser.getLogin();
        if (userRepository.existsByUsername(username)) {
            username = username + "_" + UUID.randomUUID().toString().substring(0, 6);
        }
        
        String finalEmail = email;
        if (userRepository.existsByEmail(finalEmail)) {
            email = "github_" + githubUser.getId() + "_" + System.currentTimeMillis() + "@placeholder.local";
        }
        
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(UUID.randomUUID().toString());
        user.setBalance(BigDecimal.ZERO);
        user.setAvatarUrl(githubUser.getAvatarUrl());
        
        return userRepository.save(user);
    }

    private UserOAuth createOAuthBinding(Long userId, String providerUserId, String accessToken) {
        UserOAuth userOAuth = new UserOAuth();
        userOAuth.setUserId(userId);
        userOAuth.setProvider(OAuthProvider.GITHUB);
        userOAuth.setProviderUserId(providerUserId);
        userOAuth.setAccessToken(accessToken);
        return userOAuthRepository.save(userOAuth);
    }

    private void updateOAuthToken(UserOAuth userOAuth, String accessToken, GitHubUserInfo githubUser) {
        userOAuth.setAccessToken(accessToken);
        userOAuthRepository.save(userOAuth);
        
        userRepository.findById(userOAuth.getUserId()).ifPresent(user -> {
            if (githubUser.getAvatarUrl() != null) {
                user.setAvatarUrl(githubUser.getAvatarUrl());
                userRepository.save(user);
            }
        });
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtUtils.generateToken(user.getId(), user.getEmail());
        
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
