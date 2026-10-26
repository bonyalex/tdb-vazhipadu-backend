package com.example.vazhipadu.service;

import com.example.vazhipadu.config.AuthProperties;
import com.example.vazhipadu.dto.request.LoginRequest;
import com.example.vazhipadu.dto.response.LoginResponse;
import com.example.vazhipadu.exception.InvalidCredentialsException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthProperties properties;
    private final TokenService tokenService;

    public LoginResponse login(LoginRequest request) {
        boolean emailMatches = constantTimeEquals(
                properties.adminEmail().trim().toLowerCase(),
                request.email().trim().toLowerCase());
        boolean passwordMatches = constantTimeEquals(properties.adminPassword(), request.password());
        if (!emailMatches || !passwordMatches) {
            log.warn("Login failed");
            throw new InvalidCredentialsException();
        }
        String token = tokenService.issue(properties.adminEmail());
        log.info("Admin login succeeded");
        return new LoginResponse(token, "Bearer", properties.tokenTtl().toSeconds());
    }

    private boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }
}
