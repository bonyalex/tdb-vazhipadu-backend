package com.example.vazhipadu.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DailyLimitConcurrencyTest {

    private static final int REQUESTS = 20;
    private static final int LIMIT = 5;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void twentySimultaneousOrdersOfOneAgainstLimitOfFiveYieldExactlyFiveSuccesses() throws Exception {
        long templeId = createAndReadId("/api/temples", "{\"name\":\"Sree Temple\"}");
        long offeringId = createAndReadId(
                "/api/offerings",
                "{\"templeId\":" + templeId + ",\"name\":\"Udayasthamana\",\"price\":100,\"dailyLimit\":" + LIMIT
                        + "}");
        String body = orderJson(templeId, offeringId);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(REQUESTS);
        List<Future<Integer>> results = new ArrayList<>();
        for (int i = 0; i < REQUESTS; i++) {
            results.add(executor.submit(() -> {
                start.await();
                return mockMvc.perform(post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andReturn()
                        .getResponse()
                        .getStatus();
            }));
        }

        start.countDown();
        int created = 0;
        int rejected = 0;
        for (Future<Integer> result : results) {
            int status = result.get();
            if (status == 201) {
                created++;
            } else if (status == 409) {
                rejected++;
            }
        }
        executor.shutdown();

        assertEquals(LIMIT, created);
        assertEquals(REQUESTS - LIMIT, rejected);
    }

    private String orderJson(long templeId, long offeringId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        return "{\"templeId\":" + templeId
                + ",\"devoteeName\":\"Devotee\",\"devoteePhone\":\"9876543210\",\"nakshatra\":\"ROHINI\","
                + "\"orderDate\":\"" + today + "\",\"lines\":[{\"offeringId\":" + offeringId
                + ",\"quantity\":1}]}";
    }

    private long createAndReadId(String url, String body) throws Exception {
        String response = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return node.get("id").asLong();
    }
}
