package com.example.vazhipadu.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CancelAndCollectionApiIntegrationTest {

    private static final LocalDate DATE = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(1);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long templeId;
    private long pushpanjaliId;
    private long archanaId;

    @BeforeEach
    void setUp() throws Exception {
        templeId = createAndReadId("/api/temples", "{\"name\":\"Collection Temple\"}");
        pushpanjaliId = createAndReadId("/api/offerings", offeringJson("Pushpanjali", "30.10", 2));
        archanaId = createAndReadId("/api/offerings", offeringJson("Archana", "50.25", null));
    }

    @Test
    void cancelledOrderIsExcludedFromReportAndFreesItsDailyLimit() throws Exception {
        placeOrder(pushpanjaliId, 1);
        long cancelled = placeOrder(pushpanjaliId, 1);
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(pushpanjaliId, 1)))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/orders/" + cancelled + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/temples/" + templeId + "/collection").param("date", DATE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offerings.length()").value(1))
                .andExpect(jsonPath("$.offerings[0].count").value(1))
                .andExpect(jsonPath("$.offerings[0].amount").value(30.10));
        placeOrder(pushpanjaliId, 1);
    }

    @Test
    void cancellingTwiceIsRejectedWithConflict() throws Exception {
        long orderId = placeOrder(archanaId, 1);
        mockMvc.perform(post("/api/orders/" + orderId + "/cancel")).andExpect(status().isOk());

        mockMvc.perform(post("/api/orders/" + orderId + "/cancel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Order " + orderId + " is already cancelled"));
    }

    @Test
    void cancellingUnknownOrderIsNotFound() throws Exception {
        mockMvc.perform(post("/api/orders/999999/cancel")).andExpect(status().isNotFound());
    }

    @Test
    void reportTotalIsExactAndEqualsSumOfOfferingTotals() throws Exception {
        placeOrder(pushpanjaliId, 2);
        placeOrder(archanaId, 3);
        placeOrder(archanaId, 4);

        mockMvc.perform(get("/api/temples/" + templeId + "/collection").param("date", DATE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offerings.length()").value(2))
                .andExpect(jsonPath("$.offerings[0].offeringName").value("Pushpanjali"))
                .andExpect(jsonPath("$.offerings[0].count").value(2))
                .andExpect(jsonPath("$.offerings[0].amount").value(60.20))
                .andExpect(jsonPath("$.offerings[1].count").value(7))
                .andExpect(jsonPath("$.offerings[1].amount").value(351.75))
                .andExpect(jsonPath("$.totalCount").value(9))
                .andExpect(jsonPath("$.totalAmount").value(411.95));
    }

    @Test
    void reportForDateWithNoOrdersIsEmptyAndUnknownTempleIsNotFound() throws Exception {
        mockMvc.perform(get("/api/temples/" + templeId + "/collection").param("date", DATE.plusDays(30).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offerings.length()").value(0))
                .andExpect(jsonPath("$.totalAmount").value(0));

        mockMvc.perform(get("/api/temples/999999/collection").param("date", DATE.toString()))
                .andExpect(status().isNotFound());
    }

    private long placeOrder(long offeringId, int quantity) throws Exception {
        return createAndReadId("/api/orders", orderJson(offeringId, quantity));
    }

    private String offeringJson(String name, String price, Integer dailyLimit) {
        String limit = dailyLimit == null ? "" : ",\"dailyLimit\":" + dailyLimit;
        return "{\"templeId\":" + templeId + ",\"name\":\"" + name + "\",\"price\":" + price + ",\"active\":true"
                + limit + "}";
    }

    private String orderJson(long offeringId, int quantity) {
        return "{\"templeId\":" + templeId + ",\"devoteeName\":\"Anil Kumar\",\"devoteePhone\":\"9876543210\","
                + "\"nakshatra\":\"ROHINI\",\"orderDate\":\"" + DATE + "\",\"lines\":[{\"offeringId\":" + offeringId
                + ",\"quantity\":" + quantity + "}]}";
    }

    private long createAndReadId(String url, String body) throws Exception {
        String response = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return node.get("id").asLong();
    }
}
