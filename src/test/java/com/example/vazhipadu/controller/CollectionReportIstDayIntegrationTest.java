package com.example.vazhipadu.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CollectionReportIstDayIntegrationTest {

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-10-25T18:45:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void orderPlacedAtQuarterPastMidnightIstIsReportedOnTheIstDateNotTheUtcDate() throws Exception {
        long templeId = createAndReadId("/api/temples", "{\"name\":\"Midnight Temple\"}");
        long offeringId = createAndReadId(
                "/api/offerings",
                "{\"templeId\":" + templeId + ",\"name\":\"Pushpanjali\",\"price\":30,\"active\":true}");
        String order = "{\"templeId\":" + templeId + ",\"devoteeName\":\"Anil Kumar\","
                + "\"devoteePhone\":\"9876543210\",\"nakshatra\":\"ROHINI\",\"orderDate\":\"2026-10-26\","
                + "\"lines\":[{\"offeringId\":" + offeringId + ",\"quantity\":1}]}";
        createAndReadId("/api/orders", order);

        mockMvc.perform(get("/api/temples/" + templeId + "/collection").param("date", "2026-10-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.totalAmount").value(30.00));
        mockMvc.perform(get("/api/temples/" + templeId + "/collection").param("date", "2026-10-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(0));
    }

    private long createAndReadId(String url, String body) throws Exception {
        String response = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("data").get("id").asLong();
    }
}
