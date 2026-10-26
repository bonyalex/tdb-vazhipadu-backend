package com.example.vazhipadu.dto.response;

public record LoginResponse(String accessToken, String tokenType, long expiresInSeconds) {}
