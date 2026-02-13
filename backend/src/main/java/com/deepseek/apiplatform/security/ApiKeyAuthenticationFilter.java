package com.deepseek.apiplatform.security;

import com.deepseek.apiplatform.entity.ApiKey;
import com.deepseek.apiplatform.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyAuthenticationFilter(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        String apiKey = getApiKeyFromRequest(request);
        
        if (StringUtils.hasText(apiKey)) {
            ApiKey keyEntity = apiKeyRepository.findByApiKey(apiKey).orElse(null);
            
            if (keyEntity != null && keyEntity.getStatus() == ApiKey.KeyStatus.ACTIVE) {
                keyEntity.setLastUsedAt(LocalDateTime.now());
                apiKeyRepository.save(keyEntity);
                
                ApiKeyPrincipal principal = new ApiKeyPrincipal(keyEntity);
                
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getApiKeyFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            if (token.startsWith("sk-")) {
                return token;
            }
        }
        return null;
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/v1/");
    }
}
