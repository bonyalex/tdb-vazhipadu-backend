package com.example.vazhipadu.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OfferingDailyLimitApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long templeId;

    @BeforeEach
    void setUp() throws Exception {
        String response = mockMvc.perform(post("/api/temples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sree Temple\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        templeId = objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void createdOfferingReturnsItsDailyLimit() throws Exception {
        long id = createOffering("{\"templeId\":" + templeId + ",\"name\":\"Archana\",\"price\":50,\"dailyLimit\":50}");

        mockMvc.perform(get("/api/offerings/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyLimit").value(50));
    }

    @Test
    void offeringCreatedWithoutDailyLimitHasNone() throws Exception {
        long id = createOffering("{\"templeId\":" + templeId + ",\"name\":\"Archana\",\"price\":50}");

        mockMvc.perform(get("/api/offerings/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyLimit").doesNotExist());
    }

    @Test
    void updateReplacesAndThenClearsTheDailyLimit() throws Exception {
        long id = createOffering(
                "{\"templeId\":" + templeId + ",\"name\":\"Archana\",\"price\":50,\"dailyLimit\":50}");

        mockMvc.perform(put("/api/offerings/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Archana\",\"price\":50,\"active\":true,\"dailyLimit\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyLimit").value(10));
        mockMvc.perform(put("/api/offerings/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Archana\",\"price\":50,\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyLimit").doesNotExist());
    }

    @Test
    void dailyLimitOfZeroIsRejectedOnCreate() throws Exception {
        mockMvc.perform(post("/api/offerings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"templeId\":" + templeId + ",\"name\":\"Archana\",\"price\":50,\"dailyLimit\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dailyLimitOfZeroIsRejectedOnUpdate() throws Exception {
        long id = createOffering("{\"templeId\":" + templeId + ",\"name\":\"Archana\",\"price\":50}");

        mockMvc.perform(put("/api/offerings/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Archana\",\"price\":50,\"active\":true,\"dailyLimit\":0}"))
                .andExpect(status().isBadRequest());
    }

    private long createOffering(String body) throws Exception {
        String response = mockMvc.perform(
                        post("/api/offerings").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return node.get("id").asLong();
    }
}
