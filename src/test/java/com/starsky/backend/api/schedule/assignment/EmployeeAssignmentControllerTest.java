package com.starsky.backend.api.schedule.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.api.TestJwtProvider;
import com.starsky.backend.api.authentication.LoginRequest;
import com.starsky.backend.api.schedule.shift.ScheduleShiftResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
import java.time.temporal.ChronoUnit;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmployeeAssignmentControllerTest extends TestJwtProvider {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private ScheduleShiftResponse shiftWithAssignments;
    private ScheduleShiftResponse shiftWithoutAssignments;

    @BeforeAll
    void setup() throws Exception {
        setupLogins(mockMvc, objectMapper, new LoginRequest("scheduling@a.com", "password"), new LoginRequest("scheduling@1.com", "password"));

        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/shifts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        shiftWithAssignments = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleShiftResponse.class);

        result = mockMvc.perform(MockMvcRequestBuilders.get("/user/shifts/6")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        shiftWithoutAssignments = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleShiftResponse.class);
    }

    @Test
    public void managerShouldGetEmployeeAssignments() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var availabilityResponse = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeAssignmentResponse[].class);
        Assertions.assertNotNull(availabilityResponse);
        Assertions.assertEquals(100, availabilityResponse.length);
    }

    @Test
    public void employeeShouldGetEmployeeAssignments() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var availabilityResponse = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeAssignmentResponse[].class);
        Assertions.assertNotNull(availabilityResponse);
        Assertions.assertEquals(100, availabilityResponse.length);
    }

    @Test
    public void shouldGetNotFoundEmployeeAssignments() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/1459054890/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldGetForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    public void shouldPutEmployeeAssignmentsToEmptyShift() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateEmployeeAssignmentRequest[]{
                        new CreateEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd(), 8, shiftWithoutAssignments.getId()),
                        new CreateEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd(), 9, shiftWithoutAssignments.getId()),
                        new CreateEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd(), 10, shiftWithoutAssignments.getId())
                }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldPutBadRequestEmployeeAssignments() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json")
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateEmployeeAssignmentRequest[]{
                        new CreateEmployeeAssignmentRequest(null, shiftWithoutAssignments.getShiftEnd(), 8, shiftWithoutAssignments.getId())
                }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateEmployeeAssignmentRequest[]{
                        new CreateEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), null, 8, shiftWithoutAssignments.getId())
                }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldGetNotFoundWhenPutEmployeeAssignments() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateEmployeeAssignmentRequest[]{
                        new CreateEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd(), 566565, shiftWithoutAssignments.getId())
                }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());

        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateEmployeeAssignmentRequest[]{
                        new CreateEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd(), 8, 38348349)
                }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());

        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1498458945/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateEmployeeAssignmentRequest[]{
                        new CreateEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd(), 8, shiftWithoutAssignments.getId())
                }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldGetUnprocessableEntityWhenPutEmployeeAssignments() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateEmployeeAssignmentRequest[]{
                        new CreateEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart().minusSeconds(60), shiftWithoutAssignments.getShiftEnd(), 8, shiftWithoutAssignments.getId())
                }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateEmployeeAssignmentRequest[]{
                        new CreateEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd().plusSeconds(60), 8, shiftWithoutAssignments.getId())
                }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateEmployeeAssignmentRequest[]{
                        new CreateEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart().minus(1, ChronoUnit.DAYS), shiftWithoutAssignments.getShiftEnd().plusSeconds(60), 8, shiftWithoutAssignments.getId())
                }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateEmployeeAssignmentRequest[]{
                        new CreateEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftEnd(), shiftWithoutAssignments.getShiftStart(), 8, shiftWithoutAssignments.getId())
                }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateEmployeeAssignmentRequest[]{
                        new CreateEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftStart(), 8, shiftWithoutAssignments.getId())
                }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateEmployeeAssignmentRequest[]{
                        new CreateEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd(), 8, shiftWithoutAssignments.getId()),
                        new CreateEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd(), 9, shiftWithoutAssignments.getId()),
                        new CreateEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart().plusSeconds(60), shiftWithoutAssignments.getShiftEnd().minusSeconds(60), 10, shiftWithoutAssignments.getId()),
                        new CreateEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd(), 10, shiftWithoutAssignments.getId()),
                }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @Transactional
    public void shouldPutEmployeeAssignmentsToAssignedShift() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateEmployeeAssignmentRequest[]{
                        new CreateEmployeeAssignmentRequest(shiftWithAssignments.getShiftStart(), shiftWithAssignments.getShiftEnd(), 8, shiftWithAssignments.getId()),
                        new CreateEmployeeAssignmentRequest(shiftWithAssignments.getShiftStart(), shiftWithAssignments.getShiftEnd(), 9, shiftWithAssignments.getId()),
                        new CreateEmployeeAssignmentRequest(shiftWithAssignments.getShiftStart(), shiftWithAssignments.getShiftEnd(), 10, shiftWithAssignments.getId())
                }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}
