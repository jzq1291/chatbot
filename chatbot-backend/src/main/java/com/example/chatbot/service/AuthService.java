package com.example.chatbot.service;

import com.example.chatbot.dto.AuthRequest;
import com.example.chatbot.dto.AuthResponse;
import com.example.chatbot.entity.User;
import com.example.chatbot.entity.UserRole;
import com.example.chatbot.repository.UserRepository;
import com.example.chatbot.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse authenticate(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtTokenProvider.generateToken(authentication);
        List<String> roles = user.getRoles().stream()
            .map(Enum::name)
            .collect(Collectors.toList());
        return new AuthResponse(token, user.getUsername(), roles);
    }

    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRoles(Set.of(UserRole.ROLE_USER));

        user = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = jwtTokenProvider.generateToken(authentication);
        List<String> roles = user.getRoles().stream()
            .map(Enum::name)
            .collect(Collectors.toList());
        return new AuthResponse(token, user.getUsername(), roles);
    }

    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // 获取 token 的过期时间
            long expirationTime = jwtTokenProvider.getExpirationTime(token);
            // 将 token 加入黑名单
            tokenBlacklistService.addToBlacklist(token, expirationTime);
        }
    }
} 