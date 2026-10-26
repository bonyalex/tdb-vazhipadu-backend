package com.example.vazhipadu.support;

import java.time.Duration;
import java.time.Instant;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@TestConfiguration
public class AdminTokenMockMvcConfig {

    @Bean
    MockMvcBuilderCustomizer adminTokenCustomizer(JwtEncoder jwtEncoder) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("admin@temple.test")
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofHours(1)))
                .build();
        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return builder -> builder.defaultRequest(
                MockMvcRequestBuilders.get("/").header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }
}
