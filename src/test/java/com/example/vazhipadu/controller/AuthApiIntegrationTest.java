package com.example.vazhipadu.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
class AuthApiIntegrationTest {

    @Value("${app.auth.admin-email}")
    private String adminEmail;

    @Value("${app.auth.admin-password}")
    private String adminCredential;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void successfulLoginReturnsAccessToken() throws Exception {
        mockMvc.perform(login(adminEmail, adminCredential))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void wrongPasswordAndWrongEmailGiveTheSameUnauthorizedMessage() throws Exception {
        String wrongPassword = mockMvc.perform(login(adminEmail, "nope"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.status").value(401))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String wrongEmail = mockMvc.perform(login("someone@else.test", adminCredential))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String messageForPassword = objectMapper.readTree(wrongPassword).get("error").get("message").asText();
        String messageForEmail = objectMapper.readTree(wrongEmail).get("error").get("message").asText();

        assertThat(messageForPassword).isEqualTo(messageForEmail);
        assertThat(wrongPassword).doesNotContain(adminCredential);
    }

    @Test
    void protectedEndpointWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/offerings"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.status").value(401))
                .andExpect(jsonPath("$.error.path").value("/api/offerings"));
    }

    @Test
    void protectedEndpointWithGarbageTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/offerings").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithTokenFromLoginIsAllowed() throws Exception {
        String body = mockMvc.perform(login(adminEmail, adminCredential))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String token = objectMapper.readTree(body).get("data").get("accessToken").asText();

        mockMvc.perform(get("/api/orders/999999").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private MockHttpServletRequestBuilder login(String email, String secret) throws Exception {
        String json = objectMapper.writeValueAsString(Map.of("email", email, "password", secret));
        return post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(json);
    }
}
