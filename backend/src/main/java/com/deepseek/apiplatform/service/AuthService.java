package com.deepseek.apiplatform.service;

import com.deepseek.apiplatform.dto.*;
import com.deepseek.apiplatform.entity.User;
import com.deepseek.apiplatform.repository.UserRepository;
import com.deepseek.apiplatform.security.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已被使用");
        }
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setBalance(BigDecimal.ZERO);
        user.setTokenVersion(0);
        
        user = userRepository.save(user);
        
        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getTokenVersion());
        
        return new AuthResponse(token, "Bearer", toUserResponse(user));
    }
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getTokenVersion());
        
        return new AuthResponse(token, "Bearer", toUserResponse(user));
    }
    
    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        int currentVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
        user.setTokenVersion(currentVersion + 1);
        userRepository.save(user);
    }
    
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return toUserResponse(user);
    }
    
    public UserResponse updateProfile(Long id, String username, String email) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已被使用");
        }
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已被使用");
        }
        
        user.setUsername(username);
        user.setEmail(email);
        user = userRepository.save(user);
        
        return toUserResponse(user);
    }
    
    public void changePassword(Long id, String currentPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("当前密码错误");
        }
        
        int currentVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(currentVersion + 1);
        userRepository.save(user);
    }
    
    private UserResponse toUserResponse(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getUsername(),
            user.getBalance(),
            user.getAvatarUrl(),
            user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : null
        );
    }
}
