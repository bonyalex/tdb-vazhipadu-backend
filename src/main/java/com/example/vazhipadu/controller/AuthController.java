package com.example.vazhipadu.controller;

import com.example.vazhipadu.dto.request.LoginRequest;
import com.example.vazhipadu.dto.response.ApiResponse;
import com.example.vazhipadu.dto.response.LoginResponse;
import com.example.vazhipadu.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Received admin login request");
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }
}
