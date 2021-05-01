package com.starsky.backend.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.api.TestJwtProvider;
import com.starsky.backend.api.authentication.LoginRequest;
import org.junit.jupiter.api.*;
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmployeeControllerTest extends TestJwtProvider {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    void setup() throws Exception {
        setupLogins(mockMvc, objectMapper, new LoginRequest("a@a.com", "password"), new LoginRequest("t@t.com", "password"));
    }

    @Test
    @DisplayName("Should get employees")
    public void testGetAuthenticatedUserEmployees() throws Exception {
        var result = mockMvc.perform(
                MockMvcRequestBuilders.get("/user/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var employees = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse[].class);
        Assertions.assertEquals(2, employees.length);
        Assertions.assertEquals("t@t.com", employees[0].getEmail());
        Assertions.assertEquals("Test Employee", employees[0].getName());
        Assertions.assertEquals("Animator", employees[0].getJobTitle());
        Assertions.assertEquals("EMPLOYEE", employees[0].getRole());
        Assertions.assertEquals("EMAIL", employees[0].getNotificationType());
        Assertions.assertNull(employees[0].getPhoneNumber());
    }

    @Test
    @DisplayName("Unauthenticated user should get forbidden")
    public void testGetNonAuthenticatedUser() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/user/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("Employee role should get forbidden")
    public void testGetEmployeeRole() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/user/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();
    }
}
