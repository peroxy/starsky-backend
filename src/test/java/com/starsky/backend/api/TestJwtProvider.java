package com.starsky.backend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.api.authentication.LoginRequest;
import com.starsky.backend.api.authentication.TokenResponse;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class TestJwtProvider {
    private String managerJwtHeader;
    private String employeeJwtHeader;

    protected void setupLogins(MockMvc mockMvc, ObjectMapper objectMapper, LoginRequest manager, LoginRequest employee) throws Exception {
        var result = mockMvc.perform(
                MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(manager)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var tokenResponse = objectMapper.readValue(result.getResponse().getContentAsString(), TokenResponse.class);
        Assertions.assertEquals("Bearer", tokenResponse.getTokenType());
        Assertions.assertNotNull(tokenResponse.getAccessToken());
        Assertions.assertNotNull(tokenResponse.getExpiresOn());
        managerJwtHeader = "%s %s".formatted(tokenResponse.getTokenType(), tokenResponse.getAccessToken());

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        tokenResponse = objectMapper.readValue(result.getResponse().getContentAsString(), TokenResponse.class);
        Assertions.assertEquals("Bearer", tokenResponse.getTokenType());
        Assertions.assertNotNull(tokenResponse.getAccessToken());
        Assertions.assertNotNull(tokenResponse.getExpiresOn());
        employeeJwtHeader = "%s %s".formatted(tokenResponse.getTokenType(), tokenResponse.getAccessToken());
    }

    protected String getManagerJwtHeader() {
        return managerJwtHeader;
    }

    protected String getEmployeeJwtHeader() {
        return employeeJwtHeader;
    }
}
