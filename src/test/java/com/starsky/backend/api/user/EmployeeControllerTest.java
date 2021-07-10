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

import javax.transaction.Transactional;

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
    @Transactional
    public void shouldUpdateEmployee() throws Exception {
        var result = mockMvc.perform(
                MockMvcRequestBuilders.get("/user/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var employees = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse[].class);
        Assertions.assertEquals(2, employees.length);

        result = mockMvc.perform(
                MockMvcRequestBuilders.patch("/user/employees/%d".formatted(employees[0].getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateEmployeeRequest("new name", "new@email.com", "new job title")))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var employee = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse.class);
        Assertions.assertEquals("new@email.com", employee.getEmail());
        Assertions.assertEquals("new name", employee.getName());
        Assertions.assertEquals("new job title", employee.getJobTitle());
    }

    @Test
    public void shouldGetConflictWhenUpdateEmployeeWithExistingEmail() throws Exception {
        var result = mockMvc.perform(
                MockMvcRequestBuilders.get("/user/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var employees = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse[].class);
        Assertions.assertEquals(2, employees.length);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/user/employees/%d".formatted(employees[0].getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateEmployeeRequest("new name", "a@a.com", "new job title")))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldGetNotFoundWhenUpdatingEmployee() throws Exception {
        var result = mockMvc.perform(
                MockMvcRequestBuilders.get("/user/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var employees = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse[].class);
        Assertions.assertEquals(2, employees.length);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/user/employees/85789457985479")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateEmployeeRequest("new name", "new@email.com", "new job title")))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void employeeShouldGetForbiddenWhenUpdatingEmployee() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.patch("/user/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateEmployeeRequest("new name", "new@email.com", "new job title")))
                        .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
        mockMvc.perform(
                MockMvcRequestBuilders.patch("/user/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateEmployeeRequest("new name", "new@email.com", "new job title"))))
                .andDo(print())
                .andExpect(status().isForbidden());
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
