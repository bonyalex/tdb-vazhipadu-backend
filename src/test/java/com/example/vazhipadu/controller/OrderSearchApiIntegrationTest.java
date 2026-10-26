package com.example.vazhipadu.controller;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OrderSearchApiIntegrationTest {

    private static final LocalDate TODAY = LocalDate.now(ZoneId.of("Asia/Kolkata"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long templeId;
    private long otherTempleId;
    private long offeringId;
    private long otherOfferingId;

    @BeforeEach
    void setUp() throws Exception {
        templeId = createAndReadId("/api/temples", "{\"name\":\"Search Temple\"}");
        otherTempleId = createAndReadId("/api/temples", "{\"name\":\"Other Temple\"}");
        offeringId = createAndReadId("/api/offerings", offeringJson(templeId));
        otherOfferingId = createAndReadId("/api/offerings", offeringJson(otherTempleId));
    }

    @Test
    void phoneSearchReturnsOnlyThatDevoteesOrders() throws Exception {
        placeOrder(templeId, offeringId, "9000000001", TODAY.plusDays(1));
        placeOrder(templeId, offeringId, "9000000001", TODAY.plusDays(2));
        placeOrder(templeId, offeringId, "9000000002", TODAY.plusDays(1));
        placeOrder(otherTempleId, otherOfferingId, "9000000001", TODAY.plusDays(1));

        mockMvc.perform(get("/api/orders").param("templeId", String.valueOf(templeId)).param("phone", "9000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].devoteePhone", everyItem(is("9000000001"))))
                .andExpect(jsonPath("$.content[*].templeId", everyItem(is((int) templeId))));
    }

    @Test
    void phoneSearchIsAnExactMatchNotAPrefix() throws Exception {
        placeOrder(templeId, offeringId, "9000000011", TODAY.plusDays(1));

        mockMvc.perform(get("/api/orders").param("templeId", String.valueOf(templeId)).param("phone", "9000000010"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void secondPageFollowsFirstWithNoRepeatsOrGaps() throws Exception {
        List<Long> placed = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            placed.add(placeOrder(templeId, offeringId, "9000000003", TODAY.plusDays(1 + i % 2)));
        }

        List<Long> seen = new ArrayList<>();
        for (int page = 0; page < 3; page++) {
            String body = mockMvc.perform(get("/api/orders")
                            .param("templeId", String.valueOf(templeId))
                            .param("size", "3")
                            .param("page", String.valueOf(page)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            for (JsonNode order : objectMapper.readTree(body).get("content")) {
                seen.add(order.get("id").asLong());
            }
        }

        assertEquals(7, seen.size());
        assertEquals(new HashSet<>(placed), new HashSet<>(seen));
        Set<Long> distinct = new HashSet<>(seen);
        assertEquals(seen.size(), distinct.size());
    }

    @Test
    void dateRangeFiltersOrdersByOrderDate() throws Exception {
        placeOrder(templeId, offeringId, "9000000004", TODAY.plusDays(1));
        placeOrder(templeId, offeringId, "9000000004", TODAY.plusDays(5));
        placeOrder(templeId, offeringId, "9000000004", TODAY.plusDays(9));

        mockMvc.perform(get("/api/orders")
                        .param("templeId", String.valueOf(templeId))
                        .param("from", TODAY.plusDays(2).toString())
                        .param("to", TODAY.plusDays(8).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].orderDate").value(TODAY.plusDays(5).toString()));
    }

    @Test
    void statusFilterReturnsOnlyOrdersWithThatStatus() throws Exception {
        placeOrder(templeId, offeringId, "9000000005", TODAY.plusDays(1));

        mockMvc.perform(get("/api/orders").param("templeId", String.valueOf(templeId)).param("status", "PLACED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].status", everyItem(is("PLACED"))));
        mockMvc.perform(get("/api/orders").param("templeId", String.valueOf(templeId)).param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void resultsAreNewestOrderDateFirstWithIdAsTieBreaker() throws Exception {
        long first = placeOrder(templeId, offeringId, "9000000006", TODAY.plusDays(3));
        long second = placeOrder(templeId, offeringId, "9000000006", TODAY.plusDays(3));
        long latest = placeOrder(templeId, offeringId, "9000000006", TODAY.plusDays(4));

        mockMvc.perform(get("/api/orders").param("templeId", String.valueOf(templeId)))
                .andExpect(jsonPath("$.content[0].id").value(latest))
                .andExpect(jsonPath("$.content[1].id").value(second))
                .andExpect(jsonPath("$.content[2].id").value(first));
    }

    @Test
    void pageSizeAboveTheCapIsRejected() throws Exception {
        mockMvc.perform(get("/api/orders").param("templeId", String.valueOf(templeId)).param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void malformedPhoneIsRejected() throws Exception {
        mockMvc.perform(get("/api/orders").param("templeId", String.valueOf(templeId)).param("phone", "98"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fromAfterToIsRejected() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .param("templeId", String.valueOf(templeId))
                        .param("from", TODAY.plusDays(5).toString())
                        .param("to", TODAY.plusDays(1).toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownTempleReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/orders").param("templeId", "999999")).andExpect(status().isNotFound());
    }

    @Test
    void missingTempleIdIsRejected() throws Exception {
        mockMvc.perform(get("/api/orders")).andExpect(status().isBadRequest());
    }

    private String offeringJson(long forTemple) {
        return "{\"templeId\":" + forTemple + ",\"name\":\"Archana\",\"price\":50,\"active\":true}";
    }

    private long placeOrder(long forTemple, long forOffering, String phone, LocalDate date) throws Exception {
        String body = "{\"templeId\":" + forTemple + ",\"devoteeName\":\"Anil Kumar\",\"devoteePhone\":\"" + phone
                + "\",\"nakshatra\":\"ROHINI\",\"orderDate\":\"" + date + "\",\"lines\":[{\"offeringId\":"
                + forOffering + ",\"quantity\":1}]}";
        return createAndReadId("/api/orders", body);
    }

    private long createAndReadId(String url, String body) throws Exception {
        String response = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}
