package com.example.vazhipadu.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class OrderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long templeId;
    private long pushpanjaliId;
    private long archanaId;

    @BeforeEach
    void setUp() throws Exception {
        templeId = createAndReadId("/api/temples", "{\"name\":\"Sree Temple\"}");
        pushpanjaliId = createAndReadId("/api/offerings", offeringJson("Pushpanjali", "30", true));
        archanaId = createAndReadId("/api/offerings", offeringJson("Archana", "50", true));
    }

    @Test
    void twoPushpanjaliAtThirtyAndOneArchanaAtFiftyTotalOneHundredTen() throws Exception {
        String body = orderJson(
                "9876543210",
                "[{\"offeringId\":" + pushpanjaliId + ",\"quantity\":2},{\"offeringId\":" + archanaId
                        + ",\"quantity\":1}]");

        long orderId = createAndReadId("/api/orders", body);

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(110.00))
                .andExpect(jsonPath("$.devoteePhone").value("9876543210"))
                .andExpect(jsonPath("$.nakshatra").value("ROHINI"))
                .andExpect(jsonPath("$.lines.length()").value(2));
    }

    @Test
    void orderForInactiveOfferingIsRejectedWithErrorBody() throws Exception {
        mockMvc.perform(put("/api/offerings/" + archanaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Archana\",\"price\":50,\"active\":false}"))
                .andExpect(status().isOk());
        String body = orderJson("9876543210", "[{\"offeringId\":" + archanaId + ",\"quantity\":1}]");

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Offering " + archanaId + " is not active"));
    }

    @Test
    void invalidInputIsRejectedInTheCommonErrorFormat() throws Exception {
        String body = orderJson("12345", "[{\"offeringId\":" + archanaId + ",\"quantity\":0}]");

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details", hasItem("devoteePhone: must be exactly 10 digits")))
                .andExpect(jsonPath("$.details", hasItem("lines[0].quantity: must be greater than or equal to 1")));
    }

    @Test
    void largestPriceAndQuantityStillFitTheStoredTotals() throws Exception {
        long expensiveId = createAndReadId("/api/offerings", offeringJson("Maha Pooja", "99999999.99", true));
        String body = orderJson("9876543210", "[{\"offeringId\":" + expensiveId + ",\"quantity\":1000}]");

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(99999999990.00));
    }

    @Test
    void listingOfferingsWithoutTempleIsRejectedInTheCommonErrorFormat() throws Exception {
        mockMvc.perform(get("/api/offerings"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details", hasItem("templeId: is required")));
    }

    @Test
    void listingOfferingsIgnoresClientSortAndRejectsOversizedPage() throws Exception {
        mockMvc.perform(get("/api/offerings").param("templeId", String.valueOf(templeId)).param("sort", "nope,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        mockMvc.perform(get("/api/offerings").param("templeId", String.valueOf(templeId)).param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void unknownOrderReturnsNotFoundInTheCommonErrorFormat() throws Exception {
        mockMvc.perform(get("/api/orders/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/orders/999999"));
    }

    private String offeringJson(String name, String price, boolean active) {
        return "{\"templeId\":" + templeId + ",\"name\":\"" + name + "\",\"price\":" + price + ",\"active\":"
                + active + "}";
    }

    private String orderJson(String phone, String lines) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        return "{\"templeId\":" + templeId + ",\"devoteeName\":\"Anil Kumar\",\"devoteePhone\":\"" + phone
                + "\",\"nakshatra\":\"ROHINI\",\"orderDate\":\"" + today.plusDays(1) + "\",\"lines\":" + lines + "}";
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
