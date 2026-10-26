package com.example.vazhipadu.config;

import com.example.vazhipadu.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class SecurityConfig {

    private static final String LOGIN_PATH = "/api/auth/login";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationEntryPoint entryPoint) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.POST, LOGIN_PATH)
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(oauth ->
                        oauth.jwt(Customizer.withDefaults()).authenticationEntryPoint(entryPoint))
                .exceptionHandling(handling -> handling.authenticationEntryPoint(entryPoint))
                .build();
    }

    @Bean
    AuthenticationEntryPoint unauthorizedEntryPoint(ObjectMapper objectMapper, Clock clock) {
        return (request, response, exception) -> {
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            ApiError body = new ApiError(
                    clock.instant(),
                    status.value(),
                    status.getReasonPhrase(),
                    "Authentication required",
                    request.getRequestURI(),
                    List.of());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), body);
        };
    }

    @Bean
    JwtEncoder jwtEncoder(AuthProperties properties) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(properties)));
    }

    @Bean
    JwtDecoder jwtDecoder(AuthProperties properties) {
        return NimbusJwtDecoder.withSecretKey(secretKey(properties))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    private SecretKey secretKey(AuthProperties properties) {
        return new SecretKeySpec(properties.signingKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
