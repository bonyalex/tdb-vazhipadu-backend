package com.example.vazhipadu.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        @NotBlank String adminEmail,
        @NotBlank String adminPassword,
        @NotBlank @Size(min = 32) String signingKey,
        @NotNull Duration tokenTtl) {}
