package com.deepseek.apiplatform.service;

import com.deepseek.apiplatform.dto.AuthResponse;
import com.deepseek.apiplatform.dto.LoginRequest;
import com.deepseek.apiplatform.dto.RegisterRequest;
import com.deepseek.apiplatform.dto.UserResponse;
import com.deepseek.apiplatform.entity.User;
import com.deepseek.apiplatform.repository.UserRepository;
import com.deepseek.apiplatform.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtUtils);
    }

    @Test
    @DisplayName("用户注册成功")
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtUtils.generateToken(anyLong(), anyString(), anyInt())).thenReturn("test-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertNotNull(response.getUser());
        assertEquals("test@example.com", response.getUser().getEmail());
        assertEquals("testuser", response.getUser().getUsername());

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("注册失败 - 邮箱已存在")
    void register_EmailExists_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(request);
        });

        assertEquals("邮箱已被注册", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("注册失败 - 用户名已存在")
    void register_UsernameExists_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("existinguser");
        request.setPassword("password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(request);
        });

        assertEquals("用户名已被使用", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("用户登录成功")
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setBalance(BigDecimal.valueOf(100));
        user.setTokenVersion(0);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtils.generateToken(user.getId(), user.getEmail(), user.getTokenVersion())).thenReturn("test-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        assertNotNull(response.getUser());
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void login_UserNotFound_ThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void login_WrongPassword_ThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertEquals("密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("获取用户信息成功")
    void getUserById_Success() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setBalance(BigDecimal.valueOf(100));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = authService.getUserById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    @DisplayName("获取用户信息失败 - 用户不存在")
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.getUserById(999L);
        });

        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("修改密码成功")
    void changePassword_Success() {
        User user = new User();
        user.setId(1L);
        user.setPassword("oldEncodedPassword");
        user.setTokenVersion(0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "oldEncodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        assertDoesNotThrow(() -> {
            authService.changePassword(1L, "oldPassword", "newPassword");
        });

        verify(userRepository).save(any(User.class));
        assertEquals(1, user.getTokenVersion());
    }

    @Test
    @DisplayName("修改密码失败 - 当前密码错误")
    void changePassword_WrongCurrentPassword_ThrowsException() {
        User user = new User();
        user.setId(1L);
        user.setPassword("oldEncodedPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "oldEncodedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.changePassword(1L, "wrongPassword", "newPassword");
        });

        assertEquals("当前密码错误", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("登出成功 - tokenVersion 增加")
    void logout_Success() {
        User user = new User();
        user.setId(1L);
        user.setTokenVersion(0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authService.logout(1L);

        assertEquals(1, user.getTokenVersion());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("登出失败 - 用户不存在")
    void logout_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.logout(999L);
        });

        assertEquals("用户不存在", exception.getMessage());
    }
}
