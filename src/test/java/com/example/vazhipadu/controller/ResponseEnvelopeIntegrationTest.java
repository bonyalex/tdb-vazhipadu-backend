package com.example.vazhipadu.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.vazhipadu.support.AuthenticatedAsAdmin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AuthenticatedAsAdmin
@AutoConfigureMockMvc
class ResponseEnvelopeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void successResponseCarriesDataAndNullError() throws Exception {
        mockMvc.perform(post("/api/temples").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Sree Temple\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Sree Temple"))
                .andExpect(jsonPath("$.error").value(nullValue()));
    }

    @Test
    void errorResponseCarriesErrorAndNullData() throws Exception {
        mockMvc.perform(get("/api/temples/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.status").value(404))
                .andExpect(jsonPath("$.error.path").value("/api/temples/999999"));
    }

    @Test
    void validationErrorUsesErrorEnvelopeWithDetails() throws Exception {
        mockMvc.perform(post("/api/temples").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.details").isNotEmpty());
    }
}
