package com.backend.controller;

import com.backend.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class AdminControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${admin.secret}")
    private String adminSecret;

    @Test
    void adminPage_shouldReturnWelcomeMessage_whenPasswordIsCorrect() throws Exception {
        mockMvc.perform(get("/api/admin")
                        .param("password", adminSecret))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("Welcome to Admin Panel"));
    }

    @Test
    void adminPage_shouldReturnForbidden_whenPasswordIsWrong() throws Exception {
        mockMvc.perform(get("/api/admin")
                        .param("password", "wrong-password"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Forbidden"));
    }

    @Test
    void adminPage_shouldReturnInternalServerError_whenPasswordIsMissing() throws Exception {
        mockMvc.perform(get("/api/admin"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void adminPage_shouldReturnForbidden_whenPasswordIsEmpty() throws Exception {
        mockMvc.perform(get("/api/admin")
                        .param("password", ""))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Forbidden"));
    }

    @Test
    void adminPage_shouldReturnInternalServerError_whenPasswordIsNull() throws Exception {
        mockMvc.perform(get("/api/admin")
                        .param("password", (String) null))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void adminPage_shouldReturnForbidden_whenPasswordHasExtraSpaces() throws Exception {
        String passwordWithSpaces = " " + adminSecret + " ";
        mockMvc.perform(get("/api/admin")
                        .param("password", passwordWithSpaces))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Forbidden"));
    }

    @Test
    void adminPage_shouldReturnForbidden_whenPasswordIsCaseDifferent() throws Exception {
        String uppercasePassword = adminSecret.toUpperCase();
        mockMvc.perform(get("/api/admin")
                        .param("password", uppercasePassword))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Forbidden"));
    }
}
