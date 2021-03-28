package com.starsky.backend.api.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.config.JwtConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtConfig jwtConfig;

    @Test
    @DisplayName("Try login, should get jwt token")
    public void testCorrectLogin() throws Exception {
        var result = mockMvc.perform(
                MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("a@a.com", "password"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), TokenResponse.class);
        Assertions.assertEquals("Bearer", response.getTokenType());
        Assertions.assertNotNull(response.getAccessToken());
        Assertions.assertNotNull(response.getExpiresOn());
        Assertions.assertEquals(response.getExpiresIn(), jwtConfig.getExpirationTime().getSeconds());
    }

    @Test
    @DisplayName("Try invalid login, should be unauthorized")
    public void testInvalidLogin() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("david@starsky.com", "password"))))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Try bad login requests, should be unauthorized")
    public void testMalformedRequest() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"bad\":\"json\""))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        mockMvc.perform(
                MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("really bad text"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        mockMvc.perform(
                MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("a@a.com", "short"))))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
